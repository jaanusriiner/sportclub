<template>
  <BaseModal :is-open="loginModalIsOpen" @event-modal-closed="closeModal">
    <template #title> Logi sisse </template>
    <template #body>
      <AlertDanger v-if="errorMessage" :error-message="errorMessage" />
      <form @submit.prevent="handleLogin">
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
import AlertDanger from '@/components/alert/AlertDanger.vue'
import LoginService from '@/services/LoginService.js'

export default {
  name: 'LoginModal',
  components: { BaseModal, AlertDanger },
  props: {
    loginModalIsOpen: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['event-login-successful', 'event-login-modal-closed'],

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
      LoginService.sendLoginRequest(this.email, this.password)
        .then((response) => {
          const data = response.data
          sessionStorage.setItem('userId', data.userId)
          sessionStorage.setItem('roleName', data.roleName)
          this.$emit('event-login-successful')
          this.closeModal()
          this.$router.push('/trainings')
        })
        .catch((error) => {
          if (error.response && error.response.status === 403) {
            this.errorMessage = error.response.data.message || 'Vale e-post või parool'
          } else {
            this.errorMessage = 'Süsteemne viga. Palun proovi hiljem uuesti.'
          }
        })
    },
  },
}
</script>

<style scoped></style>
