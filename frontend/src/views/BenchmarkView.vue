<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AskService from '@/services/AskService.js'
import LlmModelService from '@/services/LlmModelService.js'
import NavigationService from '@/services/NavigationService.js'
import ToneService from '@/services/ToneService.js'

const MAX_ANSWER_LENGTH = 2000 // sama piir mis backendi AskHistoryEntry.answer
const ERROR_ANSWER = 'Kahjuks ei oska ma sellele küsimusele vastata'
const NO_VALUE = '–'
const MIN_TEMPERATURE = 0
const MAX_TEMPERATURE = 2
const DEFAULT_TEMPERATURE = 1
const PANEL_COUNT = 4
const DEFAULT_TONE_NAME = 'NEUTRAL'
const CACHED_TOKENS_HINT =
  'Input tokenite osa, mis tuli providerile vahemälust - soodushind on input kulus juba arvestatud'
// TODO: thinking level on praegu ainult UI marker - backendile veel ei saadeta.
// Väärtused Gemini thinkingLevel järgi; null = mudeli vaikimisi käitumine
const THINKING_LEVELS = ['MINIMAL', 'LOW', 'MEDIUM', 'HIGH']

export default {
  name: 'BenchmarkView',
  components: { AlertDanger },
  beforeMount() {
    this.getLlmModels()
    this.getTones()
  },
  data() {
    return {
      errorMessage: '',
      question: '',

      llmModels: [],
      tones: [],
      thinkingLevels: THINKING_LEVELS,
      CACHED_TOKENS_HINT: CACHED_TOKENS_HINT,

      // iga paneeli kohta eraldi vestlus, sest LLM-i kontekstiks läheb vaid selle paneeli ajalugu
      panels: Array.from({ length: PANEL_COUNT }, (_, index) => ({
        panelId: index + 1,
        selectedLlmModelId: null,
        // täidetakse NEUTRAL tooniga, kui toonid on backendist laetud
        selectedToneId: null,
        selectedThinkingLevel: null,
        // backend hoiab selle id järgi vestluse andmestikku; uus id = uus vestlus (vt clearPanelChat)
        conversationId: crypto.randomUUID(),
        // hoiame tekstina, et väljal püsiks kuju "1.0"; backendile saadame numbrina
        temperature: DEFAULT_TEMPERATURE.toFixed(1),
        chatHistory: [],
      })),
      chatEntryCount: 0,
      pendingRequestCount: 0,
    }
  },
  computed: {
    isLoading() {
      return this.pendingRequestCount > 0
    },

    panelTotalsByPanelId() {
      const totalsByPanelId = {}
      this.panels.forEach((panel) => (totalsByPanelId[panel.panelId] = this.getPanelTotals(panel)))
      return totalsByPanelId
    },
  },
  methods: {
    ask() {
      if (this.question.trim() === '') {
        this.errorMessage = 'Palun sisesta küsimus'
        return
      }
      // päring tehakse ainult paneelidele, kus mudel on valitud
      const selectedPanels = this.panels.filter((panel) => panel.selectedLlmModelId !== null)
      if (selectedPanels.length === 0) {
        this.errorMessage = 'Vali vähemalt ühele aknale mudel'
        return
      }
      this.errorMessage = ''
      this.chatEntryCount++
      selectedPanels.forEach((panel) => this.askModel(panel, this.question))
      this.question = ''
      this.scrollChatsToTop()
    },

    askModel(panel, question) {
      this.normalizeTemperature(panel)
      const askRequest = {
        question: question,
        history: this.getHistoryForRequest(panel),
        llmModelId: panel.selectedLlmModelId,
        temperature: Number(panel.temperature),
        toneId: panel.selectedToneId,
        conversationId: panel.conversationId,
      }
      panel.chatHistory.push({
        id: this.chatEntryCount,
        question: question,
        answer: null,
        reasoning: null,
        isError: false,
        responseTimeMs: null,
        inputTokens: null,
        cachedTokens: null,
        outputTokens: null,
        inputCost: null,
        outputCost: null,
      })
      // otsime reaktiivse proxy, et hilisem answer-i muutmine uuendaks vaadet
      const chatEntry = panel.chatHistory[panel.chatHistory.length - 1]
      this.pendingRequestCount++
      const startTime = performance.now()
      AskService.postAsk2Request(askRequest)
        .then((response) => this.handleAskResponse(response, chatEntry))
        .catch(() => this.handleAskError(chatEntry))
        .finally(() => this.handleAskFinished(chatEntry, startTime))
    },

    // saadame backendile selle paneeli kõik vastatud küsimused/vastused LLM-i kontekstiks (piiranguta)
    getHistoryForRequest(panel) {
      return panel.chatHistory
        .filter((chatEntry) => !chatEntry.isError)
        .filter((chatEntry) => chatEntry.answer && chatEntry.answer.trim() !== '')
        .map((chatEntry) => ({
          question: chatEntry.question,
          answer: chatEntry.answer.slice(0, MAX_ANSWER_LENGTH),
        }))
    },

    getChatHistoryNewestFirst(panel) {
      return [...panel.chatHistory].reverse()
    },

    clearQuestion() {
      this.question = ''
      this.errorMessage = ''
    },

    // tokenite arvud ja kulud (EUR) arvutab backend; kuni neid vastuses pole, kuvame kriipsu
    handleAskResponse(response, chatEntry) {
      chatEntry.answer = response.data.answer
      chatEntry.reasoning = response.data.reasoning ?? null
      chatEntry.inputTokens = response.data.inputTokens ?? null
      chatEntry.cachedTokens = response.data.cachedTokens ?? null
      chatEntry.outputTokens = response.data.outputTokens ?? null
      chatEntry.inputCost = response.data.inputCost ?? null
      chatEntry.outputCost = response.data.outputCost ?? null
    },

    // ühe mudeli viga ei peata teisi - kuvame vea selle mudeli paneelis
    handleAskError(chatEntry) {
      chatEntry.answer = ERROR_ANSWER
      chatEntry.isError = true
    },

    // vastamise aeg on mõõdetud brauseris: päringu saatmisest vastuse saabumiseni
    handleAskFinished(chatEntry, startTime) {
      chatEntry.responseTimeMs = performance.now() - startTime
      this.pendingRequestCount--
    },

    formatResponseTime(responseTimeMs) {
      return 'Response time: ' + Math.round(responseTimeMs).toLocaleString('et-EE') + ' ms'
    },

    formatTokens(label, tokens, cost) {
      return label + ': ' + this.formatTokenCount(tokens) + ' (Cost: ' + this.formatCost(cost) + ')'
    },

    // cached tokenid on input tokenite osa (mitte lisaks); nende soodushind on input kulus juba arvestatud
    formatCachedTokens(cachedTokens) {
      return 'Cached tokens: ' + this.formatTokenCount(cachedTokens)
    },

    formatTokenCount(tokens) {
      if (tokens === null) {
        return NO_VALUE
      }
      return tokens.toLocaleString('et-EE')
    },

    // ühe päringu kulu on tavaliselt sendi murdosa, seega näitame vähemalt 4 komakohta
    formatCost(cost) {
      if (cost === null) {
        return NO_VALUE + ' EUR'
      }
      return (
        cost.toLocaleString('et-EE', { minimumFractionDigits: 4, maximumFractionDigits: 6 }) +
        ' EUR'
      )
    },

    // tühjendamine alustab uue vestluse - backend pärib uue vestluse esimesel küsimusel värske andmestiku
    clearPanelChat(panel) {
      panel.chatHistory = []
      panel.conversationId = crypto.randomUUID()
    },

    // paneel ootab vastust, kui mõnel tema küsimusel pole veel vastust
    // hoiab temperatuuri lubatud vahemikus 0.0-2.0 ja alati ühe komakohaga (1 -> "1.0");
    // tühja välja korral vaikeväärtus
    normalizeTemperature(panel) {
      const temperature = parseFloat(panel.temperature)
      if (Number.isNaN(temperature)) {
        panel.temperature = this.formatTemperature(DEFAULT_TEMPERATURE)
        return
      }
      const maxTemperature = this.getMaxTemperature(panel)
      const clampedTemperature = Math.min(maxTemperature, Math.max(MIN_TEMPERATURE, temperature))
      panel.temperature = this.formatTemperature(clampedTemperature)
    },

    // mõni provider lubab väiksemat ülempiiri (nt Anthropic 1.0) - see tuleb mudeli andmetest
    getMaxTemperature(panel) {
      const llmModel = this.getSelectedLlmModel(panel)
      return llmModel ? llmModel.maxTemperature : MAX_TEMPERATURE
    },

    getSelectedLlmModel(panel) {
      return this.llmModels.find((llmModel) => llmModel.llmModelId === panel.selectedLlmModelId)
    },

    getLlmModels() {
      LlmModelService.getLlmModelsRequest()
        .then((response) => (this.llmModels = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getTones() {
      ToneService.getTonesRequest()
        .then((response) => this.handleGetTonesResponse(response))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetTonesResponse(response) {
      this.tones = response.data
      const defaultTone =
        this.tones.find((tone) => tone.toneName === DEFAULT_TONE_NAME) ?? this.tones[0]
      if (defaultTone) {
        this.panels.forEach((panel) => (panel.selectedToneId = defaultTone.toneId))
      }
    },

    // uus mudel alustab puhtalt lehelt: eelmise mudeli ajalugu ei lähe kontekstiks ja summad ei segune
    handleLlmModelChange(panel) {
      this.clearPanelChat(panel)
      this.normalizeTemperature(panel)
    },

    formatTemperature(temperature) {
      return temperature.toFixed(1)
    },

    isPanelLoading(panel) {
      return panel.chatHistory.some((chatEntry) => chatEntry.answer === null)
    },

    // paneeli vestluse kokkuvõte - summeerime kõik selle mudeli vastused alates viimasest tühjendamisest
    getPanelTotals(panel) {
      const inputCost = this.sumField(panel.chatHistory, 'inputCost')
      const outputCost = this.sumField(panel.chatHistory, 'outputCost')
      return {
        inputTokens: this.sumField(panel.chatHistory, 'inputTokens'),
        cachedTokens: this.sumField(panel.chatHistory, 'cachedTokens'),
        outputTokens: this.sumField(panel.chatHistory, 'outputTokens'),
        inputCost: inputCost,
        outputCost: outputCost,
        totalCost: this.sumValues([inputCost, outputCost]),
      }
    },

    sumField(chatHistory, fieldName) {
      return this.sumValues(chatHistory.map((chatEntry) => chatEntry[fieldName]))
    },

    // tagastab null, kui ühtegi väärtust pole - siis kuvame kriipsu, mitte nulli
    sumValues(values) {
      const existingValues = values.filter((value) => value !== null)
      if (existingValues.length === 0) {
        return null
      }
      return existingValues.reduce((sum, value) => sum + value, 0)
    },

    scrollChatsToTop() {
      this.$nextTick(() => {
        const chatBoxes = this.$refs.chatBoxes
        chatBoxes.forEach((chatBox) => (chatBox.scrollTop = 0))
      })
    },
  },
}
</script>

<template>
  <div class="container-fluid px-3 text-center mt-3">
    <div class="row justify-content-center">
      <div class="col-12 col-lg-11 col-xl-10">
        <AlertDanger :error-message="errorMessage" />

        <div class="d-flex gap-2 mb-3">
          <div class="position-relative flex-grow-1">
            <input
              v-model="question"
              type="text"
              class="form-control border-secondary clearable-input"
              maxlength="500"
              placeholder="Siit saad küsida harrastatavate spordialade, spordiklubide, treenerite, treeningrühmade ja trenniaegade kohta"
              @keyup.enter="ask"
              @keyup.esc="clearQuestion"
            />
            <span v-if="question !== ''" class="esc-hint">Esc</span>
            <button
              v-if="question !== ''"
              type="button"
              class="btn-close clear-button"
              aria-label="Tühjenda"
              title="Tühjenda"
              @click="clearQuestion"
            ></button>
          </div>
          <button type="button" class="btn btn-primary" :disabled="isLoading" @click="ask">
            Küsi
          </button>
        </div>
      </div>
    </div>

    <!-- vastused alati kõrvuti; kitsal ekraanil keritakse horisontaalselt -->
    <div class="d-flex gap-3 overflow-x-auto pb-2">
      <div v-for="panel in panels" :key="panel.panelId" class="answer-panel">
        <div class="border border-secondary rounded text-start">
          <div
            class="d-flex flex-column border-bottom border-secondary bg-body-tertiary rounded-top px-3 py-2"
          >
            <!-- mudeli valik ja Tühjenda nupp ühel real, sama kõrgusega (mõlemad -sm) ja keskjoondatud;
                 temperatuur eraldi real, et kitsas paneelis ei läheks rida üle ääre -->
            <div class="d-flex align-items-center gap-2 mb-1">
              <select
                v-model="panel.selectedLlmModelId"
                class="form-select form-select-sm border-secondary fw-semibold model-select"
                aria-label="Mudel"
                :disabled="isPanelLoading(panel)"
                @change="handleLlmModelChange(panel)"
              >
                <option :value="null" disabled>Vali mudel</option>
                <option
                  v-for="llmModel in llmModels"
                  :key="llmModel.llmModelId"
                  :value="llmModel.llmModelId"
                >
                  {{ llmModel.modelName }}
                </option>
              </select>
              <button
                type="button"
                class="btn btn-sm btn-outline-secondary flex-shrink-0"
                :class="{ invisible: panel.chatHistory.length === 0 }"
                :disabled="isPanelLoading(panel)"
                @click="clearPanelChat(panel)"
              >
                Tühjenda
              </button>
            </div>
            <span class="small text-secondary text-nowrap mb-1">
              Temperature:
              <input
                v-model="panel.temperature"
                type="number"
                class="form-control form-control-sm border-secondary d-inline-block temperature-input"
                min="0"
                :max="getMaxTemperature(panel)"
                step="0.1"
                aria-label="Temperature"
                @change="normalizeTemperature(panel)"
              />
            </span>
            <span class="small text-secondary text-nowrap mb-1">
              Thinking level:
              <select
                v-model="panel.selectedThinkingLevel"
                class="form-select border-secondary d-inline-block w-auto param-select"
                aria-label="Thinking level"
                title="Ei mõjuta veel vastust - backendi tugi lisandub hiljem"
              >
                <option :value="null">DEFAULT</option>
                <option
                  v-for="thinkingLevel in thinkingLevels"
                  :key="thinkingLevel"
                  :value="thinkingLevel"
                >
                  {{ thinkingLevel }}
                </option>
              </select>
            </span>
            <span class="small text-secondary text-nowrap mb-1">
              Tone:
              <select
                v-model="panel.selectedToneId"
                class="form-select border-secondary d-inline-block w-auto param-select"
                aria-label="Tone"
              >
                <option v-for="tone in tones" :key="tone.toneId" :value="tone.toneId">
                  {{ tone.toneName }}
                </option>
              </select>
            </span>
            <!-- paneeli kokkuvõtte meetrika, sisenditest kerge joonega eraldatud -->
            <div class="d-flex flex-column border-top pt-2 mt-1 metrics-block">
              <span class="small text-secondary">
                {{
                  formatTokens(
                    'Input tokens',
                    panelTotalsByPanelId[panel.panelId].inputTokens,
                    panelTotalsByPanelId[panel.panelId].inputCost,
                  )
                }}
              </span>
              <span class="small text-secondary ps-3" :title="CACHED_TOKENS_HINT">
                {{ formatCachedTokens(panelTotalsByPanelId[panel.panelId].cachedTokens) }}
              </span>
              <span class="small text-secondary">
                {{
                  formatTokens(
                    'Output tokens',
                    panelTotalsByPanelId[panel.panelId].outputTokens,
                    panelTotalsByPanelId[panel.panelId].outputCost,
                  )
                }}
              </span>
              <span class="small fw-semibold">
                Total cost: {{ formatCost(panelTotalsByPanelId[panel.panelId].totalCost) }}
              </span>
            </div>
          </div>
          <div ref="chatBoxes" class="chat-box p-3">
            <p v-if="panel.chatHistory.length === 0" class="text-secondary text-center mb-0">
              Vestlus ilmub siia
            </p>
            <div
              v-for="chatEntry in getChatHistoryNewestFirst(panel)"
              :key="chatEntry.id"
              class="mb-3"
            >
              <div class="d-flex justify-content-end mb-2">
                <div class="chat-bubble bg-primary text-white">{{ chatEntry.question }}</div>
              </div>
              <div class="d-flex flex-column align-items-start">
                <div
                  class="chat-bubble border"
                  :title="chatEntry.reasoning ? 'Reasoning:\n' + chatEntry.reasoning : null"
                  :class="chatEntry.isError ? 'bg-danger-subtle text-danger-emphasis' : 'bg-light'"
                >
                  <span
                    v-if="chatEntry.answer === null"
                    class="spinner-border spinner-border-sm"
                    role="status"
                  ></span>
                  <template v-else>{{ chatEntry.answer }}</template>
                </div>
                <div
                  v-if="chatEntry.responseTimeMs !== null"
                  class="d-flex flex-column small text-secondary mt-1 ms-1"
                >
                  <span>{{ formatResponseTime(chatEntry.responseTimeMs) }}</span>
                  <template v-if="!chatEntry.isError">
                    <span>
                      {{ formatTokens('Input tokens', chatEntry.inputTokens, chatEntry.inputCost) }}
                    </span>
                    <span class="ps-3" :title="CACHED_TOKENS_HINT">
                      {{ formatCachedTokens(chatEntry.cachedTokens) }}
                    </span>
                    <span>
                      {{
                        formatTokens('Output tokens', chatEntry.outputTokens, chatEntry.outputCost)
                      }}
                    </span>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.answer-panel {
  flex: 1 0 18rem;
}

/* kerge eraldusjoon sisendite ja meetrika vahel (heledam kui paneeli border-secondary ääris) */
.metrics-block {
  border-color: var(--bs-border-color-translucent) !important;
}

/* väike tekstireaga samas mõõdus valik (nagu temperature-input); ületab nordic.css form-select mõõdud */
.param-select {
  min-height: 0;
  height: 1.4em;
  padding: 0 1.5rem 0 0.25rem;
  font-size: inherit;
  line-height: 1.2;
  vertical-align: baseline;
  background-position: right 0.25rem center;
  background-size: 10px 8px;
}

/* väike tekstireaga samas mõõdus väli; ületab form-control-sm min-height'i ja fondisuuruse */
/* täidab rea vaba laiuse, kuid kahaneb kitsas paneelis (pikk mudeli nimi lõigatakse).
   nordic.css annab .form-select'ile min-height 44px / 15px, .btn-sm'ile 36px / 13px -
   võtame Tühjenda nupu mõõdud, et mõlemad oleks ühel real sama suured */
.model-select {
  flex: 1 1 auto;
  min-width: 0;
  min-height: 36px;
  height: 36px;
  padding-top: 0;
  padding-bottom: 0;
  font-size: 13px;
}

.temperature-input {
  width: 3.5rem;
  min-height: 0;
  height: 1.4em;
  padding: 0 0.25rem;
  font-size: inherit;
  line-height: 1.2;
  vertical-align: baseline;
}

.chat-box {
  height: 28rem;
  overflow-y: auto;
}

.chat-bubble {
  max-width: 80%;
  padding: 0.5rem 0.75rem;
  border-radius: 0.75rem;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.clearable-input {
  padding-right: 4.5rem;
}

.esc-hint {
  position: absolute;
  top: 50%;
  right: 2.9rem;
  transform: translateY(-50%);
  font-size: 0.75rem;
  color: #adb5bd;
  pointer-events: none;
  user-select: none;
}

.clear-button {
  position: absolute;
  top: 50%;
  right: 0.75rem;
  transform: translateY(-50%);
}
</style>
