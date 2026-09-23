<template>
  <div class="container text-center">
    <LoginModal
      :login-modal-is-open="isLoginModalOpen"
      @event-login-modal-closed="isLoginModalOpen = false"
    />
    <div class="row justify-content-end my-3">
      <div class="col col-2">
        <button class="btn btn-outline-primary" @click="isLoginModalOpen = true">
          Ava sisselogimine
        </button>
      </div>
    </div>

    <div class="row justify-content-center">
      <div class="col">
        <div class="alert alert-primary" role="alert">See on koduvaade, see asub rajal (/)</div>
        <TestComponent />
      </div>
    </div>
  </div>
</template>

<script>
import TestComponent from '@/components/TestComponent.vue'
import LoginModal from '@/components/modal/LoginModal.vue'

export default {
  name: 'HomeView',
  components: { TestComponent, LoginModal },
  data() {
    return {
      isLoginModalOpen: false,
      email: '',
      password: '',
      errorMessage: '',
    }
  },
}
</script>
