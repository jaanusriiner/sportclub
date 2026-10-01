<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AskService from '@/services/AskService.js'

export default {
  name: 'InfoView',
  components: { AlertDanger },
  data() {
    return {
      errorMessage: '',
      isLoading: false,

      askRequest: {
        question: '',
      },

      chatHistory: [],
      chatEntryCount: 0,
    }
  },
  computed: {
    chatHistoryNewestFirst() {
      return [...this.chatHistory].reverse()
    },
  },
  methods: {
    ask() {
      if (this.askRequest.question.trim() === '') {
        this.errorMessage = 'Palun sisesta küsimus'
        return
      }
      this.errorMessage = ''
      this.isLoading = true
      this.chatEntryCount++
      this.chatHistory.push({
        id: this.chatEntryCount,
        question: this.askRequest.question,
        answer: null,
      })
      // otsime reaktiivse proxy, et hilisem answer-i muutmine uuendaks vaadet
      const chatEntry = this.chatHistory[this.chatHistory.length - 1]
      this.scrollChatToTop()
      AskService.postAskRequest(this.askRequest)
        .then((response) => this.handleAskResponse(response, chatEntry))
        .catch(() => this.handleAskError(chatEntry))
        .finally(() => (this.isLoading = false))
    },

    clearQuestion() {
      this.askRequest.question = ''
      this.errorMessage = ''
    },

    handleAskResponse(response, chatEntry) {
      chatEntry.answer = response.data.answer
      this.askRequest.question = ''
      this.scrollChatToTop()
    },

    clearChat() {
      this.chatHistory = []
    },

    scrollChatToTop() {
      this.$nextTick(() => {
        const chatBox = this.$refs.chatBox
        chatBox.scrollTop = 0
      })
    },

    handleAskError(chatEntry) {
      this.chatHistory.splice(this.chatHistory.indexOf(chatEntry), 1)
      this.errorMessage = 'Kahjuks ei oska ma sellele küsimusele vastata'
    },
  },
}
</script>

<template>
  <div class="container text-center mt-5">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <figure class="mt-3 mb-5">
          <blockquote class="blockquote fs-3 fst-italic">
            <p>
              “Just remember, you can’t climb the ladder of success with your hands in your
              pockets.”
            </p>
          </blockquote>
          <figcaption class="blockquote-footer">Arnold Schwarzenegger</figcaption>
        </figure>

        <p class="fs-6 text-secondary intro-text">
          Siit saad küsida harrastatavate spordialade, spordiklubide, treenerite, treeningrühmade ja
          trenniaegade kohta
        </p>

        <AlertDanger :error-message="errorMessage" />

        <div class="d-flex gap-2 mb-3">
          <div class="position-relative flex-grow-1">
            <input
              v-model="askRequest.question"
              type="text"
              class="form-control clearable-input"
              maxlength="500"
              placeholder="Esita küsimus"
              @keyup.enter="ask"
              @keyup.esc="clearQuestion"
            />
            <span v-if="askRequest.question !== ''" class="esc-hint">Esc</span>
            <button
              v-if="askRequest.question !== ''"
              type="button"
              class="btn-close clear-button"
              aria-label="Tühjenda"
              title="Tühjenda"
              @click="clearQuestion"
            ></button>
          </div>
          <button type="button" class="btn btn-primary" :disabled="isLoading" @click="ask">
            Otsi
          </button>
        </div>

        <div class="text-start mb-2">
          <button
            v-if="chatHistory.length > 0"
            type="button"
            class="btn btn-secondary"
            :disabled="isLoading"
            @click="clearChat"
          >
            Tühjenda vestlus
          </button>
        </div>

        <div ref="chatBox" class="chat-box border rounded p-3 text-start">
          <p v-if="chatHistory.length === 0" class="text-secondary text-center mb-0">
            Vestlus ilmub siia
          </p>
          <div v-for="chatEntry in chatHistoryNewestFirst" :key="chatEntry.id" class="mb-3">
            <div class="d-flex justify-content-end mb-2">
              <div class="chat-bubble bg-primary text-white">{{ chatEntry.question }}</div>
            </div>
            <div class="d-flex justify-content-start">
              <div class="chat-bubble bg-light border">
                <span
                  v-if="chatEntry.answer === null"
                  class="spinner-border spinner-border-sm"
                  role="status"
                ></span>
                <template v-else>{{ chatEntry.answer }}</template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.intro-text {
  margin-bottom: 2.5rem;
}

.chat-box {
  height: 24rem;
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
