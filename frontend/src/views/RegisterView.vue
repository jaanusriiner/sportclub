<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AlertSuccess from '@/components/alert/AlertSuccess.vue'
import AreaService from '@/services/AreaService.js'
import NavigationService from '@/services/NavigationService.js'
import SportService from '@/services/SportService.js'
import AreasDropDown from '@/components/dropdown/AreasDropDown.vue'
import SportsMultiSelect from '@/views/SportsMultiSelect.vue'
import UserService from '@/services/UserService.js'

export default {
  name: 'RegisterView',
  components: { SportsMultiSelect, AreasDropDown, AlertSuccess, AlertDanger },
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

      registerRequest: {
        firstName: '',
        lastName: '',
        phoneNumber: 0,
        email: '',
        password: '',
        areaId: 0,
        sportIds: [1,2],
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

    handleAreaSelected(id) {
      this.registerRequest.areaId = id
    },

    handleSportsSelected([]) {},

    //todo Jaanus, puudu sisendite validatsioonid ja spordialade valik on hetkel hardcode'itud, samuti veaolukorrad
    registerUser() {
      UserService.postRegisterRequest(this.registerRequest)
      NavigationService.navigateToHomeView()
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
        <div class="row justify-content-center mb-3">
          <div class="col">
            <div class="form-floating">
              <input
                v-model="registerRequest.firstName"
                type="text"
                class="form-control border border-dark"
                placeholder=""
              />
              <label>Eesnimi</label>
            </div>
          </div>
          <div class="col">
            <div class="form-floating">
              <input
                v-model="registerRequest.lastName"
                type="text"
                class="form-control border border-dark"
                placeholder=""
              />
              <label>Perenimi</label>
            </div>
          </div>
        </div>
        <div class="row justify-content-center mb-3">
          <div class="col">
            <div class="form-floating">
              <input
                v-model="registerRequest.phoneNumber"
                type="number"
                class="form-control border border-dark"
                placeholder=""
              />
              <label>Kontakttelefon</label>
            </div>
          </div>
          <div class="col"></div>
        </div>

        <div class="row justify-content-center mb-3">
          <div class="col">
            <AreasDropDown :areas="areas" @event-new-area-selected="handleAreaSelected" />
          </div>
          <div class="col"></div>
        </div>
        <div class="row justify-content-center mb-5">
          <div class="col">
            <SportsMultiSelect :sports="sports" />
          </div>
        </div>

        <div class="row justify-content-center mb-3">
          <div class="col">
            <div class="form-floating">
              <input
                v-model="registerRequest.email"
                type="email"
                class="form-control border border-dark"
                placeholder=""
              />
              <label>Email</label>
            </div>
          </div>
          <div class="col">
            <div class="form-floating mb-3">
              <input type="text" class="form-control border border-dark" placeholder="" />
              <label>Salasõna</label>
            </div>
            <div class="form-floating">
              <input
                v-model="registerRequest.password"
                type="text"
                class="form-control border border-dark"
                placeholder=""
              />
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
    <div class="row justify-content-center">
      <div class="col">
        <button class="btn btn-secondary me-3" type="submit">Tagasi</button>
        <button @click="registerUser" class="btn btn-success" type="submit">Registreeru</button>
      </div>
    </div>
  </div>
</template>
