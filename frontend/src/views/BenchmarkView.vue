<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AskService from '@/services/AskService.js'

const MAX_HISTORY_ENTRIES = 5
const MAX_ANSWER_LENGTH = 2000 // sama piir mis backendi AskHistoryEntry.answer
const ERROR_ANSWER = 'Kahjuks ei oska ma sellele küsimusele vastata'
const NO_VALUE = '–'
const MIN_TEMPERATURE = 0
const MAX_TEMPERATURE = 2
const DEFAULT_TEMPERATURE = 1

// võrreldavad mudelid - model väärtus saadetakse backendile, label kuvatakse paneeli päises
const MODELS = [
  { model: 'model-1', label: 'Mudel 1' },
  { model: 'model-2', label: 'Mudel 2' },
  { model: 'model-3', label: 'Mudel 3' },
  { model: 'model-4', label: 'Mudel 4' },
]

export default {
  name: 'BenchmarkView',
  components: { AlertDanger },
  data() {
    return {
      errorMessage: '',
      question: '',

      // iga mudeli kohta eraldi vestlus, sest LLM-i kontekstiks läheb vaid selle mudeli ajalugu
      panels: MODELS.map((modelInfo) => ({
        model: modelInfo.model,
        label: modelInfo.label,
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

    panelTotalsByModel() {
      const totalsByModel = {}
      this.panels.forEach((panel) => (totalsByModel[panel.model] = this.getPanelTotals(panel)))
      return totalsByModel
    },
  },
  methods: {
    ask() {
      if (this.question.trim() === '') {
        this.errorMessage = 'Palun sisesta küsimus'
        return
      }
      this.errorMessage = ''
      this.chatEntryCount++
      this.panels.forEach((panel) => this.askModel(panel, this.question))
      this.question = ''
      this.scrollChatsToTop()
    },

    askModel(panel, question) {
      this.normalizeTemperature(panel)
      const askRequest = {
        question: question,
        history: this.getHistoryForRequest(panel),
        model: panel.model,
        temperature: Number(panel.temperature),
      }
      panel.chatHistory.push({
        id: this.chatEntryCount,
        question: question,
        answer: null,
        isError: false,
        responseTimeMs: null,
        inputTokens: null,
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

    // saadame backendile selle mudeli viimased vastatud küsimused/vastused LLM-i kontekstiks
    getHistoryForRequest(panel) {
      return panel.chatHistory
        .filter((chatEntry) => !chatEntry.isError)
        .filter((chatEntry) => chatEntry.answer && chatEntry.answer.trim() !== '')
        .slice(-MAX_HISTORY_ENTRIES)
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
      chatEntry.inputTokens = response.data.inputTokens ?? null
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

    clearPanelChat(panel) {
      panel.chatHistory = []
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
      const clampedTemperature = Math.min(MAX_TEMPERATURE, Math.max(MIN_TEMPERATURE, temperature))
      panel.temperature = this.formatTemperature(clampedTemperature)
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
      <div v-for="panel in panels" :key="panel.model" class="answer-panel">
        <div class="border border-secondary rounded text-start">
          <div
            class="d-flex justify-content-between align-items-start border-bottom border-secondary bg-body-tertiary rounded-top px-3 py-2"
          >
            <div class="d-flex flex-column">
              <span>
                <span class="fw-semibold">{{ panel.label }}</span>
                <span class="small text-secondary ms-1">
                  (temperature:
                  <input
                    v-model="panel.temperature"
                    type="number"
                    class="form-control form-control-sm border-secondary d-inline-block temperature-input"
                    min="0"
                    max="2"
                    step="0.1"
                    aria-label="Temperature"
                    @change="normalizeTemperature(panel)"
                  />)
                </span>
              </span>
              <span class="small text-secondary">
                {{
                  formatTokens(
                    'Input tokens',
                    panelTotalsByModel[panel.model].inputTokens,
                    panelTotalsByModel[panel.model].inputCost,
                  )
                }}
              </span>
              <span class="small text-secondary">
                {{
                  formatTokens(
                    'Output tokens',
                    panelTotalsByModel[panel.model].outputTokens,
                    panelTotalsByModel[panel.model].outputCost,
                  )
                }}
              </span>
              <span class="small fw-semibold">
                Total cost: {{ formatCost(panelTotalsByModel[panel.model].totalCost) }}
              </span>
            </div>
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary"
              :class="{ invisible: panel.chatHistory.length === 0 }"
              :disabled="isPanelLoading(panel)"
              @click="clearPanelChat(panel)"
            >
              Tühjenda
            </button>
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

/* väike tekstireaga samas mõõdus väli; ületab form-control-sm min-height'i ja fondisuuruse */
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
