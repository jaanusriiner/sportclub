<template>
  <BaseModal
    :is-open="confirmModalIsOpen"
    @event-modal-closed="$emit('event-confirm-modal-closed')"
  >
    <template #title>{{ title }}</template>
    <template #body>
      <AlertDanger :error-message="errorMessage" />
      <div v-if="training" class="text-start">
        <div>{{ training.sportName }}</div>
        <div>
          <strong>{{ training.sportclubName }}</strong>
        </div>
        <div>
          <strong>Treener: {{ training.trainerName }}</strong>
        </div>
        <div>
          <strong>{{ training.skillLevelName }}</strong>
        </div>
        <div>{{ training.facilityName }}</div>
        <div>{{ dateTime }}</div>
      </div>
    </template>

    <template #buttons>
      <button type="button" class="btn btn-primary me-2" @click="$emit('event-confirmed')">
        {{ confirmButtonText }}
      </button>
    </template>
  </BaseModal>
</template>

<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import BaseModal from '@/components/modal/BaseModal.vue'

export default {
  name: 'TrainingConfirmModal',
  components: { BaseModal, AlertDanger },
  props: {
    confirmModalIsOpen: {
      type: Boolean,
      default: false,
    },
    title: String,
    confirmButtonText: String,
    errorMessage: {
      type: String,
      default: '',
    },
    training: Object,
    dateTime: String,
  },
  emits: ['event-confirm-modal-closed', 'event-confirmed'],
}
</script>
