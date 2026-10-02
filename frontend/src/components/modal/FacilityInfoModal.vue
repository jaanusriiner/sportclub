<template>
  <BaseModal :is-open="infoModalIsOpen" @event-modal-closed="$emit('event-info-modal-closed')">
    <template #title>{{ facility ? facility.facilityName : '' }}</template>
    <template #body>
      <div v-if="facility" class="text-start">
        <div class="mb-3 text-center">
          <div v-if="imageIsLoading" class="sc-sub py-4">Pildi laadimine...</div>
          <img
            v-else-if="imageData"
            :src="imageData"
            :alt="`Asukoha ${facility.facilityName} pilt`"
            class="facility-image"
          />
          <div v-else class="sc-sub py-4">Asukohal pole pilti</div>
        </div>
        <div><strong>Aadress:</strong> {{ facility.facilityAddress }}</div>
        <div v-if="areaName"><strong>Maakond:</strong> {{ areaName }}</div>
        <div v-if="facility.facilityDescription" class="mt-2">
          {{ facility.facilityDescription }}
        </div>
      </div>
    </template>
  </BaseModal>
</template>

<script>
import BaseModal from '@/components/modal/BaseModal.vue'

export default {
  name: 'FacilityInfoModal',
  components: { BaseModal },
  props: {
    infoModalIsOpen: {
      type: Boolean,
      default: false,
    },
    facility: Object,
    areaName: String,
    imageData: String,
    imageIsLoading: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-info-modal-closed'],
}
</script>

<style scoped>
.facility-image {
  max-width: 100%;
  max-height: 320px;
  border-radius: 0.5rem;
}
</style>
