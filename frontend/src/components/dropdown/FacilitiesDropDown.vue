<script>
// <option> sees ei saa teksti osi eraldi stiliseerida, seepärast on siin kohandatud rippmenüü
export default {
  name: 'FacilitiesDropDown',
  props: {
    facilityId: {
      type: Number,
      default: 0,
    },
    facilities: Array,
  },
  emits: ['event-new-facility-selected'],
  data() {
    return {
      isOpen: false,
    }
  },
  computed: {
    selectedFacility() {
      return (this.facilities || []).find((facility) => facility.facilityId === this.facilityId)
    },
  },
  mounted() {
    document.addEventListener('click', this.closeOnOutsideClick)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.closeOnOutsideClick)
  },
  methods: {
    selectFacility(facilityId) {
      this.$emit('event-new-facility-selected', facilityId)
      this.isOpen = false
    },

    closeOnOutsideClick(event) {
      if (!this.$refs.dropdown.contains(event.target)) {
        this.isOpen = false
      }
    },
  },
}
</script>

<template>
  <div ref="dropdown" class="dropdown">
    <button
      type="button"
      class="form-select text-start"
      aria-label="Vali asukoht"
      aria-haspopup="listbox"
      :aria-expanded="isOpen"
      @click="isOpen = !isOpen"
    >
      <template v-if="selectedFacility">
        {{ selectedFacility.facilityName }}
        <small class="text-secondary">({{ selectedFacility.facilityAddress }})</small>
      </template>
      <template v-else>Vali asukoht...</template>
    </button>
    <ul v-if="isOpen" class="dropdown-menu show w-100 facility-menu" role="listbox">
      <li>
        <button type="button" class="dropdown-item" @click="selectFacility(0)">
          Vali asukoht...
        </button>
      </li>
      <li v-for="facility in facilities" :key="facility.facilityId">
        <button
          type="button"
          class="dropdown-item"
          :class="{ active: facility.facilityId === facilityId }"
          @click="selectFacility(facility.facilityId)"
        >
          {{ facility.facilityName }}
          <small :class="facility.facilityId === facilityId ? '' : 'text-secondary'">
            ({{ facility.facilityAddress }})
          </small>
        </button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.facility-menu {
  max-height: 16rem;
  overflow-y: auto;
}
</style>
