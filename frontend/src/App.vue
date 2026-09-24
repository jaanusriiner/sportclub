<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark px-3 mb-3">
    <RouterLink class="navbar-brand" to="/">SportClub</RouterLink>
    <button
      class="navbar-toggler"
      type="button"
      data-bs-toggle="collapse"
      data-bs-target="#navMenu"
    >
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse justify-content-center" id="navMenu">
      <div class="navbar-nav">
        <RouterLink class="nav-link" to="/">Kodu</RouterLink>
        <RouterLink class="nav-link" to="/">Tutvustus</RouterLink>
        <RouterLink class="nav-link" to="/">Treeningud</RouterLink>
        <button type="button" class="nav-link btn btn-link text-start" @click="openModal">
          Logi sisse
        </button>
        <RouterLink class="nav-link" to="/register">Registreeru</RouterLink>
      </div>
    </div>
  </nav>

  <RouterView />
  <LoginModal :login-modal-is-open="loginModalIsOpen" @event-login-modal-closed="closeModal" />
</template>

<script>
import { RouterLink, RouterView } from 'vue-router'
import LoginModal from '@/components/modal/LoginModal.vue'

export default {
  name: 'App',
  components: { RouterLink, RouterView, LoginModal },
  data() {
    return {
      loginModalIsOpen: false,
    }
  },
  methods: {
    openModal() {
      this.loginModalIsOpen = true
    },
    closeModal() {
      this.loginModalIsOpen = false
    },
  },
}
</script>

<style scoped>
.btn-link {
  border: none;
  background: none;
  text-decoration: none;
}
</style>
