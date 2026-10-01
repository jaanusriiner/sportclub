<template>
  <BaseModal :is-open="editModalIsOpen" @event-modal-closed="closeModal">
    <template #title>Muuda treeningut</template>
    <template #body>
      <AlertDanger :error-message="errorMessage" />
      <div v-if="training" class="text-start">
        <div class="mb-3">
          <div>{{ training.sportName }}</div>
          <div>
            <strong>{{ training.facilityName }}</strong>
          </div>
          <div>
            <strong>Treener: {{ training.trainerName }}</strong>
          </div>
          <div>
            <strong>{{ training.skillLevelName }}</strong>
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label">Maksimaalne osalejate arv</label>
          <input
            v-model.number="updateRequest.maxSize"
            type="number"
            min="1"
            class="form-control"
          />
        </div>

        <div class="mb-3">
          <label class="form-label">Kuupäev</label>
          <input v-model="updateRequest.trainingDate" type="date" class="form-control" />
        </div>

        <div class="mb-3">
          <label class="form-label">Kellaaeg</label>
          <input v-model="updateRequest.trainingTime" type="time" class="form-control" />
        </div>
      </div>
    </template>

    <template #buttons>
      <button type="button" class="btn btn-primary me-2" @click="updateTraining">Kinnitan</button>
    </template>
  </BaseModal>
</template>

<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import BaseModal from '@/components/modal/BaseModal.vue'
import TrainingService from '@/services/TrainingService.js'
import NavigationService from '@/services/NavigationService.js'

export default {
  name: 'TrainingEditModal',
  components: { BaseModal, AlertDanger },
  props: {
    editModalIsOpen: {
      type: Boolean,
      default: false,
    },
    training: Object,
  },
  emits: ['event-edit-modal-closed', 'event-training-updated'],
  data() {
    return {
      errorMessage: '',
      // description jääb saatmata (null), et backend jätaks olemasoleva kirjelduse muutmata
      updateRequest: {
        description: null,
        maxSize: 1,
        trainingDate: '',
        trainingTime: '',
      },
    }
  },
  watch: {
    editModalIsOpen(isOpen) {
      if (isOpen) {
        this.fillFormFromTraining()
      }
    },
  },
  methods: {
    fillFormFromTraining() {
      this.errorMessage = ''
      this.updateRequest.maxSize = this.training.maxSize
      this.updateRequest.trainingDate = this.training.trainingDate
      this.updateRequest.trainingTime = this.training.trainingTime.substring(0, 5)
    },

    updateTraining() {
      this.errorMessage = ''
      this.checkFormForErrors()
      if (this.errorMessage === '') {
        TrainingService.putTrainingDateRequest(this.training.trainingDateId, this.updateRequest)
          .then(() => this.handleUpdateResponse())
          .catch((error) => this.handleUpdateError(error))
      }
    },

    checkFormForErrors() {
      if (!this.updateRequest.maxSize || this.updateRequest.maxSize < 1) {
        this.errorMessage = 'Sisesta maksimaalne osalejate arv (vähemalt 1)'
      } else if (this.updateRequest.maxSize < this.training.userCount) {
        this.errorMessage = `Maksimaalne osalejate arv ei tohi olla väiksem juba registreerunud kasutajate arvust (${this.training.userCount})`
      } else if (!this.updateRequest.trainingDate) {
        this.errorMessage = 'Sisesta kuupäev'
      } else if (!this.updateRequest.trainingTime) {
        this.errorMessage = 'Sisesta kellaaeg'
      }
    },

    handleUpdateResponse() {
      this.$emit('event-training-updated')
    },

    handleUpdateError(error) {
      if (error.response && error.response.status === 403) {
        this.errorMessage = error.response.data.message
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    closeModal() {
      this.errorMessage = ''
      this.$emit('event-edit-modal-closed')
    },
  },
}
</script>
