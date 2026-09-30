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

      askResponse: {
        answer: '',
      },
    }
  },
  methods: {
    ask() {
      if (this.askRequest.question.trim() === '') {
        this.errorMessage = 'Palun sisesta küsimus'
        return
      }
      this.errorMessage = ''
      this.askResponse.answer = ''
      this.isLoading = true
      AskService.postAskRequest(this.askRequest)
        .then((response) => this.handleAskResponse(response))
        .catch(() => this.handleAskError())
        .finally(() => (this.isLoading = false))
    },

    clearQuestion() {
      this.askRequest.question = ''
      this.askResponse.answer = ''
      this.errorMessage = ''
    },

    handleAskResponse(response) {
      this.askResponse = response.data
    },

    handleAskError() {
      this.errorMessage = 'Kahjuks ei oska ma sellele küsimusele vastata'
    },
  },
}
</script>

<template>
  <div class="container text-center mt-5">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <p class="fs-5 intro-text">
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
            />
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

        <div v-if="isLoading" class="spinner-border" role="status"></div>
        <textarea
          v-else
          v-model="askResponse.answer"
          class="form-control"
          rows="8"
          readonly
          placeholder="Vastus ilmub siia"
        ></textarea>
      </div>
    </div>
  </div>
</template>

<style scoped>
.intro-text {
  margin-bottom: 5rem;
}

.clearable-input {
  padding-right: 2.5rem;
}

.clear-button {
  position: absolute;
  top: 50%;
  right: 0.75rem;
  transform: translateY(-50%);
}
</style>
