<script>
const HIDE_AFTER_MS = 10000

export default {
  name: 'AlertSuccess',
  props: {
    successMessage: String,
  },
  data() {
    return {
      isVisible: false,
      hideTimeoutId: null,
    }
  },
  watch: {
    // iga uus mitte-tühi teade kuvatakse 10 sekundit, seejärel peidetakse
    successMessage: {
      immediate: true,
      handler(message) {
        clearTimeout(this.hideTimeoutId)
        this.isVisible = !!message
        if (this.isVisible) {
          this.hideTimeoutId = setTimeout(() => (this.isVisible = false), HIDE_AFTER_MS)
        }
      },
    },
  },
  beforeUnmount() {
    clearTimeout(this.hideTimeoutId)
  },
}
</script>

<template>
  <div>
    <div v-if="isVisible" class="alert alert-success text-center" role="alert">
      {{ successMessage }}
    </div>
  </div>
</template>
