<script>
export default {
  name: 'AreasDropDown',
  props: {
    areaId: {
      type: Number,
      default: 0,
    },
    areas: Array,
    // kui true, lisatakse eraldusjoon üldvalikute ja üksikvalikute vahele
    showSeparator: {
      type: Boolean,
      default: false,
    },
    allLabel: {
      type: String,
      default: 'Vali piirkond...',
    },
  },
  emits: ['event-new-area-selected'],
}
</script>

<template>
  <select
    :value="areaId"
    @change="$emit('event-new-area-selected', Number($event.target.value))"
    class="form-select"
    aria-label="Default select example"
  >
    <option :value="0">{{ allLabel }}</option>
    <option v-if="showSeparator" disabled>──────────</option>
    <option v-for="area in areas" :key="area.areaId" :value="area.areaId">
      {{ area.areaName }}
    </option>
  </select>
</template>
