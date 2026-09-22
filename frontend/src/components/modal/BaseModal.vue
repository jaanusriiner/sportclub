<script>
export default {
  name: 'BaseModal',
  props: {
    isOpen: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-modal-closed'],
  methods: {
    close() {
      this.$emit('event-modal-closed')
    },
  },
}
</script>

<template>

  <div v-if="Open">

  </div>

</template>

<style scoped></style>
