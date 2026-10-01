<template>
  <BaseModal :is-open="infoModalIsOpen" @event-modal-closed="$emit('event-info-modal-closed')">
    <template #title>Treeningu info</template>
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
        <div>Täituvus: {{ training.userCount }}/{{ training.maxSize }}</div>
      </div>
    </template>
  </BaseModal>
</template>

<script>
import BaseModal from '@/components/modal/BaseModal.vue'

export default {
  name: 'TrainingInfoModal',
  components: { BaseModal },
  props: {
    infoModalIsOpen: {
      type: Boolean,
      default: false,
    },
    training: Object,
    dateTime: String,
  },
  emits: ['event-info-modal-closed'],
}
</script>
