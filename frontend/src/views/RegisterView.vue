<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AlertSuccess from '@/components/alert/AlertSuccess.vue'

export default {
  name: 'RegisterView',
  components: { AlertSuccess, AlertDanger},
  beforeMount() {},
  data() {
    return {
      errorMessage: 'Test',
      successMessage: 'Test2',
    }
  },
  methods: {

  }
}
</script>

<template>
  <div class="container text-center">
    <div class="row justify-content-center">
      <div class="col col-5">
        <AlertSuccess :success-message="successMessage" />
        <AlertDanger :error-message="errorMessage" />
        <h1>Registreeru kasutajaks</h1>
      </div>
    </div>
    <div class="row justify-content-center">
      <div class="col col-3">
        <div class="form-floating mb-3">
          <input type="text" class="form-control" placeholder="" />
          <label>Eesnimi</label>
        </div>

      </div>
      <div class="col col-3">
        <div class="form-floating mb-3">
          <input type="text" class="form-control" placeholder="" />
          <label>Perenimi</label>
        </div>

      </div>
    </div>
    <div class="row justify-content-center">
      <div class="col">
        <button class="btn btn-secondary me-3" type="submit">Tagasi</button>
        <button @click="" class="btn btn-success" type="submit">Registreeru</button>
      </div>
    </div>
  </div>
</template>
