<script>
import { RouterLink, RouterView } from 'vue-router'
import LoginModal from '@/components/modal/LoginModal.vue'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'

export default {
  name: 'App',
  components: { RouterLink, RouterView, LoginModal },
  // Lubab vaadetel (nt TrainingsView) login modaali avada
  provide() {
    return { openLoginModal: this.openModal }
  },
  data() {
    return {
      routerViewKey: 0,
      loginModalIsOpen: false,
      isLoggedIn: SessionStorageService.userIsLoggedIn(),
      isAdmin: SessionStorageService.userIsAdmin(),
      isTrainer: SessionStorageService.userIsTrainer(),
      userFullName: SessionStorageService.getUserFullName(),
    }
  },
  computed: {
    userInitials() {
      return this.userFullName
        .split(' ')
        .filter(Boolean)
        .slice(0, 2)
        .map((namePart) => namePart[0].toUpperCase())
        .join('')
    },
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
      this.isTrainer = false
      this.userFullName = ''
      NavigationService.navigateToHomeView()
    },
    updateNavMenu() {
      this.isLoggedIn = true
      // laeb aktiivse vaate uuesti, et see näeks uut sisselogimise olekut
      this.routerViewKey++
      this.isAdmin = SessionStorageService.userIsAdmin()
      this.isTrainer = SessionStorageService.userIsTrainer()
      this.userFullName = SessionStorageService.getUserFullName()
    },
  },
}
</script>

<template>
  <nav class="navbar navbar-expand-lg sc-nav">
    <RouterLink class="navbar-brand" to="/">
      <svg
        width="22"
        height="22"
        viewBox="0 0 24 24"
        fill="none"
        stroke="#2F5148"
        stroke-width="1.8"
        aria-hidden="true"
      >
        <circle cx="12" cy="12" r="9"></circle>
        <path d="M3 12h18"></path>
        <path d="M12 3c2.5 2.6 3.8 5.6 3.8 9s-1.3 6.4-3.8 9"></path>
      </svg>
      <span>SportClub</span>
    </RouterLink>
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
        <!--        <RouterLink class="nav-link" to="/">Kodu</RouterLink>-->
        <RouterLink class="nav-link" to="/info">Küsi</RouterLink>
        <RouterLink class="nav-link" to="/trainings">Treeningud</RouterLink>
        <RouterLink class="nav-link" to="/facilities">Asukohad</RouterLink>
        <RouterLink v-if="isAdmin" class="nav-link" to="/create-facility">Lisa asukoht</RouterLink>
      </div>
      <div class="navbar-nav">
        <div v-if="isLoggedIn" class="nav-item dropdown">
          <button
            type="button"
            class="nav-link btn btn-link dropdown-toggle d-flex align-items-center gap-2"
            data-bs-toggle="dropdown"
            aria-expanded="false"
          >
            <span v-if="userFullName">{{ userFullName }}</span>
            <span class="sc-avatar">{{ userInitials }}</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <template v-if="isTrainer || isAdmin">
              <li>
                <RouterLink class="dropdown-item" to="/manage-trainings">
                  Halda treeninguid
                </RouterLink>
              </li>
              <li>
                <RouterLink class="dropdown-item" to="/manage-training-groups">
                  Halda treeninggruppe
                </RouterLink>
              </li>
              <li v-if="isAdmin">
                <RouterLink class="dropdown-item" to="/manage-users">Halda kasutajaid</RouterLink>
              </li>
              <li v-if="isAdmin">
                <RouterLink class="dropdown-item" to="/manage-facilities"
                  >Halda asukohti</RouterLink
                >
              </li>
              <li><hr class="dropdown-divider" /></li>
            </template>
            <li>
              <button type="button" class="dropdown-item" @click="executeLogOut">Logi välja</button>
            </li>
          </ul>
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

  <RouterView :key="routerViewKey" />
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
