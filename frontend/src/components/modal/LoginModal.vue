<template>
  <BaseModal :is-open="loginModalIsOpen" @event-modal-closed="closeModal">
    <template #title> Logi sisse </template>
    <template #body>
      <div v-if="errorMessage" class="alert alert-danger" role="alert">
        {{ errorMessage }}
      </div>

      <form>
        <div class="mb-3 text-start">
          <label for="emailInput" class="form-label">E-post</label>
          <input
            id="emailInput"
            v-model="email"
            type="email"
            class="form-control"
            placeholder="Nimi@eesnimi.ee"
          />
        </div>

        <div class="mb-3 text-start">
          <label for="passwordInput" class="form-label">Salasõna</label>
          <input
            id="passwordInput"
            v-model="password"
            type="password"
            class="form-control"
            placeholder="********"
          />
        </div>
      </form>
    </template>

    <template #buttons>
      <button type="button" class="btn btn-primary me-2" @click="handleLogin">Logi Sisse</button>
    </template>
  </BaseModal>
</template>

<script>
import BaseModal from '@/components/modal/BaseModal.vue'

export default {
  name: 'LoginModal',
  components: {
    BaseModal,
  },
  props: {
    loginModalIsOpen: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      email: '',
      password: '',
      errorMessage: '',
    }
  },
  methods: {

    closeModal() {
      this.email = ''
      this.password = ''
      this.errorMessage = ''
      this.$emit('event-login-modal-closed')
    },


    handleLogin() {
      this.errorMessage = ''
      console.log('Saadan andmed:', this.email, this.password)


      if (this.email === 'admin@admin.ee' && this.password === '123') {
        sessionStorage.setItem('userId', '1')
        sessionStorage.setItem('roleName', 'admin')
        alert('Login successful!')
        this.closeModal()
      } else {
        this.errorMessage = 'Vale e-post või parool'
      }
    },
  },
}
</script>

<style scoped></style>
