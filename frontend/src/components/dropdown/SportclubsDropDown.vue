<script>
export default {
  name: 'SportclubsDropDown',
  props: {
    sportclubId: {
      type: Number,
      default: 0,
    },
    sportclubs: Array,
    // kui true, lisatakse eraldusjoon üldvalikute ja üksikvalikute vahele
    showSeparator: {
      type: Boolean,
      default: false,
    },
    allLabel: {
      type: String,
      default: 'Kõik sportklubid',
    },
    // kui true, lisatakse valik "Minu ..." väärtusega -1
    showMyOption: {
      type: Boolean,
      default: false,
    },
    myLabel: {
      type: String,
      default: 'Minu sportklubid',
    },
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
    <option :value="0">{{ allLabel }}</option>
    <option v-if="showMyOption" :value="-1">{{ myLabel }}</option>
    <option v-if="showSeparator" disabled>──────────</option>
    <option
      v-for="sportclub in sportclubs"
      :key="sportclub.sportclubId"
      :value="sportclub.sportclubId"
    >
      {{ sportclub.sportclubName }}
    </option>
  </select>
</template>
