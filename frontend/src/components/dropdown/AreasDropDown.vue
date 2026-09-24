<script>
export default {
  name: 'AreasDropDown',
  props: {
    areaId: {
      type: Number,
      default: 0,
    },
    areas: Array,
  },
  emits: ['event-new-area-selected'],
}
</script>

<template>
  <select
    :value="areaId"
    @change="$emit('event-new-area-selected', Number($event.target.value))"
    class="form-select border border-dark"
    aria-label="Default select example"
  >
    <option :value="0">Vali piirkond...</option>
    <option v-for="area in areas" :key="area.areaId" :value="area.areaId">
      {{ area.areaName }}
    </option>
  </select>
</template>
