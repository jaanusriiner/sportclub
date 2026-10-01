<template>
  <BaseModal :is-open="deleteModalIsOpen" @event-modal-closed="$emit('event-delete-modal-closed')">
    <template #title>Kinnitan kustutamise</template>
    <template #body>
      <div v-if="training" class="text-start">
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
        <div>{{ training.sportclubName }}</div>
        <div>{{ dateTime }}</div>
      </div>
    </template>

    <template #buttons>
      <button type="button" class="btn btn-danger me-2" @click="$emit('event-delete-confirmed')">
        Kinnitan treeningu kustutamise
      </button>
    </template>
  </BaseModal>
</template>

<script>
import BaseModal from '@/components/modal/BaseModal.vue'

export default {
  name: 'TrainingDeleteModal',
  components: { BaseModal },
  props: {
    deleteModalIsOpen: {
      type: Boolean,
      default: false,
    },
    training: Object,
    dateTime: String,
  },
  emits: ['event-delete-modal-closed', 'event-delete-confirmed'],
}
</script>
