<script>
export default {
  name: 'SportclubsDropDown',
  props: {
    sportclubId: {
      type: Number,
      default: 0,
    },
    sportclubs: Array,
  },
  emits: ['event-new-sportclub-selected'],
}
</script>

<template>
  <select
    :value="sportclubId"
    @change="$emit('event-new-sportclub-selected', Number($event.target.value))"
    class="form-select border border-dark"
    aria-label="Vali sportklubi"
  >
    <option :value="0">Kõik sportklubid</option>
    <option
      v-for="sportclub in sportclubs"
      :key="sportclub.sportclubId"
      :value="sportclub.sportclubId"
    >
      {{ sportclub.sportclubName }}
    </option>
  </select>
</template>
