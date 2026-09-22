<script setup>
import BaseModal from '@/components/modal/BaseModal.vue'
</script>

<template>

<BaseModal
  :is-open="LoginModalIsOpen"
  @event-modal-closed="$emit('event-login-modal-closed')"
  >

<template #title>
{{Sisselogimine}}
</template>

</BaseModal>
</template>

<style scoped></style>
