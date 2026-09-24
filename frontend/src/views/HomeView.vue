<template>
  <div class="container text-center">
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

export default {
  name: 'HomeView',
  components: {
    TestComponent,
  },
  data() {
    return {
    }
  },
}
</script>

<style scoped></style>
