<script>
export default {
  name: 'SportsDropDown',
  props: {
    sportId: {
      type: Number,
      default: 0,
    },
    sports: Array,
  },
  emits: ['event-new-sport-selected'],
}
</script>

<template>
  <select
    :value="sportId"
    @change="$emit('event-new-sport-selected', Number($event.target.value))"
    class="form-select border border-dark"
    aria-label="Vali spordiala"
  >
    <option :value="0">Kõik spordialad</option>
    <option v-for="sport in sports" :key="sport.sportId" :value="sport.sportId">
      {{ sport.sportName }}
    </option>
  </select>
</template>
