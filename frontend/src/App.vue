<script>
import { RouterLink, RouterView } from 'vue-router'
import LoginModal from '@/components/modal/LoginModal.vue'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'

export default {
  name: 'App',
  components: { RouterLink, RouterView, LoginModal },
  data() {
    return {
      loginModalIsOpen: false,
      isLoggedIn: SessionStorageService.userIsLoggedIn(),
      isAdmin: SessionStorageService.userIsAdmin(),
    }
  },
  methods: {
    openModal() {
      this.loginModalIsOpen = true
    },
    closeModal() {
      this.loginModalIsOpen = false
    },
    executeLogOut() {
      sessionStorage.clear()
      this.isLoggedIn = false
      this.isAdmin = false
      NavigationService.navigateToHomeView()
    },
    updateNavMenu() {
      this.isLoggedIn = true
      this.isAdmin = SessionStorageService.userIsAdmin()
    },
  },
}
</script>

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
    <div class="collapse navbar-collapse" id="navMenu">
      <div class="navbar-nav me-auto">
        <RouterLink class="nav-link" to="/">Kodu</RouterLink>
        <RouterLink class="nav-link" to="/info">Info</RouterLink>
        <RouterLink class="nav-link" to="/">Treeningud</RouterLink>
      </div>
      <div class="navbar-nav">
        <div v-if="isLoggedIn">
          <button type="button" class="nav-link btn btn-link text-start" @click="executeLogOut">
            Logi välja
          </button>
        </div>
        <template v-else>
          <RouterLink class="nav-link" to="/register">Registreeru</RouterLink>
          <button type="button" class="nav-link btn btn-link text-start" @click="openModal">
            Logi sisse
          </button>
        </template>
      </div>
    </div>
  </nav>

  <RouterView />
  <LoginModal
    :login-modal-is-open="loginModalIsOpen"
    @event-login-modal-closed="closeModal"
    @event-login-successful="updateNavMenu"
  />
</template>

<style scoped>
.btn-link {
  border: none;
  background: none;
  text-decoration: none;
}
</style>
