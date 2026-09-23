<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AlertSuccess from '@/components/alert/AlertSuccess.vue'
import AreaService from '@/services/AreaService.js'
import NavigationService from '@/services/NavigationService.js'
import SportService from "@/services/SportService.js";

export default {
  name: 'RegisterView',
  components: { AlertSuccess, AlertDanger },
  beforeMount() {
    this.getAreas()
    this.getSports()
  },
  data() {
    return {
      errorMessage: '',
      successMessage: '',

      areas: [
        {
          areaId: 0,
          areaName: '',
        },
      ],

      sports: [
        {
          sportId: 0,
          sportName: '',
        },
      ],

      loginRequest: {
        firstName: '',
        lastName: '',
        phoneNumber: 0,
        email: '',
        password: '',
        areaId: 0,
        sportIds: [0],
      },

      loginResponse: {
        userId: 0,
        roleName: '',
      },
    }
  },
  methods: {
    getAreas() {
      AreaService.getAreasRequest()
        .then((response) => this.handleGetAreasResponse(response))
        .catch(() => NavigationService.navigateToErrorView())
        .finally()
    },

    handleGetAreasResponse(response) {
      this.areas = response.data
    },

    getSports() {
      SportService.getSportsRequest()
        .then((response) => this.handleGetSportsResponse(response))
        .catch(() => NavigationService.navigateToErrorView())
        .finally()
    },

    handleGetSportsResponse(response) {
      this.sports = response.data
    },
  },
}
</script>

<template>
  <div class="container text-center">
    <div class="row justify-content-center mb-3">
      <div class="col col-5">
        <AlertSuccess :success-message="successMessage" />
        <AlertDanger :error-message="errorMessage" />
        <h1>Registreeru kasutajaks</h1>
      </div>
    </div>
    <div class="row justify-content-center mb-5">
      <div class="col col-5">
        <div class="row justify-content-center">
          <div class="col">
            <div class="form-floating mb-3">
              <input type="text" class="form-control border border-dark" placeholder="" />
              <label>Eesnimi</label>
            </div>
          </div>
          <div class="col">
            <div class="form-floating mb-3">
              <input type="text" class="form-control border border-dark" placeholder="" />
              <label>Perenimi</label>
            </div>
          </div>
        </div>
        <div class="row justify-content-center">
          <div class="col">
            <div class="form-floating mb-3">
              <input type="number" class="form-control border border-dark" placeholder="" />
              <label>Kontakttelefon</label>
            </div>
          </div>
          <div class="col"></div>
        </div>

        <div class="row justify-content-center mb-5">
          <div class="col">
            <select class="form-select border border-dark" aria-label="Default select example">
              <option selected>Piirkond</option>
              <option value="1">Harjumaa</option>
              <option value="2">Läänemaa</option>
              <option value="3">Pärnumaa</option>
            </select>
          </div>
          <div class="col">
            <select class="form-select border border-dark" aria-label="Default select example">
              <option selected>Spordiala</option>
              <option value="1">Tennis</option>
              <option value="2">Jalgpall</option>
              <option value="3">Golf</option>
            </select>
          </div>
        </div>

        <div class="row justify-content-center">
          <div class="col">
            <div class="form-floating mb-3">
              <input type="email" class="form-control border border-dark" placeholder="" />
              <label>Email</label>
            </div>
          </div>
          <div class="col">
            <div class="form-floating mb-3">
              <input type="text" class="form-control border border-dark" placeholder="" />
              <label>Salasõna</label>
            </div>
            <div class="form-floating mb-3">
              <input type="text" class="form-control border border-dark" placeholder="" />
              <label>Korda Salasõna</label>
            </div>
          </div>
        </div>
        <div class="row justify-content-center">
          <div class="col">
            <div class="form-check">
              <input
                class="form-check-input border border-dark"
                type="checkbox"
                value=""
                id="checkDefault"
              />
              <label class="form-check-label" for="checkDefault">
                Nõustun
                <a
                  href="https://www.apolloklubi.ee/terms-and-conditions"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="text-dark"
                  >Kasutustingimustega</a
                >
              </label>
            </div>
          </div>
          <div class="col"></div>
        </div>
      </div>
    </div>

    <!--          <div class="form-floating mb-3">-->
    <!--            <input type="number" class="form-control" placeholder="" />-->
    <!--            <label>Kontakttelefon</label>-->
    <!--          </div>-->

    <!--      </div>-->
    <!--      <div class="col col-3">-->
    <!--        <div class="form-floating mb-3">-->
    <!--          <input type="text" class="form-control" placeholder="" />-->
    <!--          <label>Perenimi</label>-->
    <!--        </div>-->

    <!--      </div>-->
    <!--      </div>-->
    <!--      </div>-->
    <div class="row justify-content-center">
      <div class="col">
        <button class="btn btn-secondary me-3" type="submit">Tagasi</button>
        <button class="btn btn-success" type="submit">Registreeru</button>
      </div>
    </div>
  </div>
</template>
