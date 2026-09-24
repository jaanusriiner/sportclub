<script>
import TomSelect from 'tom-select'

export default {
  name: 'SportsMultiSelect',

  props: {
    sportId: {
      type: Number,
      default: 0,
    },
    sports: Array,
  },
  emits: ['event-new-sports-selected'],
  mounted() {
    this.tomSelect = new TomSelect(this.$refs.sportsSelect, {
      plugins: ['remove_button'], // adds an × to remove each selected tag
      placeholder: 'Vali vähemalt üks spordiala...',
      maxItems: null, // null = unlimited selections
      create: false, // prevents users typing in new options that don't exist
      controlClass: 'ts-control form-select border border-dark', // näeb välja nagu teised valikuväljad
    })
    this.syncOptions()
  },
  beforeUnmount() {
    this.tomSelect?.destroy()
  },
  watch: {
    // sports tuleb API-st asünkroonselt peale komponendi mounted() käivitumist,
    // seega tuleb valikute nimekiri Tom Selectis API kaudu uuesti sünkroonida,
    // kui päris andmed kohale jõuavad. Tom Selecti destroy()+taasloomine ei sobi,
    // sest destroy() taastab <select>-i algse (tühja) innerHTML, kustutades
    // vahepeal lisatud valikud uuesti ära.
    sports() {
      this.syncOptions()
    },
  },
  methods: {
    syncOptions() {
      this.tomSelect.clearOptions()
      this.sports
        .filter((sport) => sport.sportName)
        .forEach((sport) => this.tomSelect.addOption({ value: sport.sportId, text: sport.sportName }))
      this.tomSelect.refreshOptions(false)
    },
  },
}
</script>

<template>
  <select id="sports" ref="sportsSelect" multiple></select>
</template>

<style scoped>
/* Tom Select kirjutab valitud spordialade puhul oma padding'u üle, mis katab muidu form-select
   noole ära - taastame ruumi noole jaoks, et väli näeks endiselt dropdown'i moodi välja. */
:deep(.ts-wrapper .ts-control.form-select) {
  padding-right: 2.25rem !important;
}
</style>
