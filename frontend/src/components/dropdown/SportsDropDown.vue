<script>
export default {
  name: 'SportsDropDown',
  props: {
    sportId: {
      type: Number,
      default: 0,
    },
    sports: Array,
    // kui true, lisatakse eraldusjoon üldvalikute ja üksikvalikute vahele
    showSeparator: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-new-sport-selected'],
}
</script>

<template>
  <select
    :value="sportId"
    @change="$emit('event-new-sport-selected', Number($event.target.value))"
    class="form-select"
    aria-label="Vali spordiala"
  >
    <option :value="0">Kõik spordialad</option>
    <option v-if="showSeparator" disabled>──────────</option>
    <option v-for="sport in sports" :key="sport.sportId" :value="sport.sportId">
      {{ sport.sportName }}
    </option>
  </select>
</template>
