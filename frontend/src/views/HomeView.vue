<template>
  <div class="container text-center mt-4">
    <nav class="d-flex justify-content-end mb-4 p-2 bg-light rounded">
      <button class="btn btn-outline-primary" @click="openLoginModal">Logi sisse</button>
    </nav>
    <div class="alert alert-primary" role="alert">See on koduvaade, see asub rajal (/)</div>
    <TestComponent />
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
    }
  },
  methods: {
    openLoginModal() {
      this.isLoginModalOpen = true
    },
    colseLoginModal() {
      this.closeLoginModalOpen = false
    },
  },
}
</script>
