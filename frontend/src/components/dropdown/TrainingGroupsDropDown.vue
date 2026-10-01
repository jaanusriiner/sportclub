<script>
export default {
  name: 'TrainingGroupsDropDown',
  props: {
    trainingGroupId: {
      type: Number,
      default: 0,
    },
    trainingGroups: Array,
    // kui true, lisatakse eraldusjoon üldvaliku ja üksikvalikute vahele
    showSeparator: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-new-training-group-selected'],
}
</script>

<template>
  <select
    :value="trainingGroupId"
    @change="$emit('event-new-training-group-selected', Number($event.target.value))"
    class="form-select border border-dark"
    aria-label="Vali treeninggrupp"
  >
    <option :value="0">Kõik treeninggrupid</option>
    <option v-if="showSeparator" disabled>──────────</option>
    <option
      v-for="trainingGroup in trainingGroups"
      :key="trainingGroup.trainingGroupId"
      :value="trainingGroup.trainingGroupId"
    >
      {{ trainingGroup.trainingGroupName }}
    </option>
  </select>
</template>
