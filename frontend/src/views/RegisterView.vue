<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AlertSuccess from '@/components/alert/AlertSuccess.vue'
import AreaService from '@/services/AreaService.js'
import NavigationService from '@/services/NavigationService.js'
import SportService from '@/services/SportService.js'
import AreasDropDown from '@/components/dropdown/AreasDropDown.vue'
import SportsMultiSelect from '@/components/multiselect/SportsMultiSelect.vue'
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
      passwordRepeat: '',
      registrationNotComplete: true,
      termsAccepted: false,

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
        phoneNumber: '',
        email: '',
        password: '',
        areaId: 0,
        sportIds: [],
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

    handleSportsSelected(sportIds) {
      this.registerRequest.sportIds = sportIds
    },

    registerUser() {
      this.resetSuccessMessage()
      this.resetErrorMessage()
      this.checkFormForErrors()

      if (this.errorMessageIsEmpty()) {
        UserService.postRegisterRequest(this.registerRequest)
          .then(() => this.handleRegisterResponse())
          .catch(() => NavigationService.navigateToErrorView())
        // NavigationService.navigateToHomeView()
      }
    },
    goBack() {
      this.$router.back()
    },
    errorMessageIsEmpty() {
      return this.errorMessage === ''
    },
    resetSuccessMessage() {
      this.successMessage = ''
    },

    resetErrorMessage() {
      this.errorMessage = ''
    },
    checkFormForErrors() {
      if (this.registerRequest.firstName === '') {
        this.errorMessage = 'Sisesta eesnimi'
      } else if (this.registerRequest.lastName === '') {
        this.errorMessage = 'Sisesta perenimi'
      } else if (this.registerRequest.phoneNumber < 10000) {
        this.errorMessage = 'Sisesta kontaktnumber (peab olema vähemalt 5-kohaline number'
      } else if (this.registerRequest.areaId === 0) {
        this.errorMessage = 'Vali piirkond'
      } else if (this.registerRequest.sportIds.length < 1) {
        this.errorMessage = 'Vali vähemalt 1 spordiala eelistus'
      } else if (this.registerRequest.email === '') {
        this.errorMessage = 'Sisesta email'
      } else if (this.registerRequest.password !== this.passwordRepeat) {
        this.errorMessage = 'Salasõna ei kattu'
      }
    },
    handleRegisterResponse() {
      this.successMessage = 'Uus kasutaja edukalt registreeritud'
      this.registrationNotComplete = false
      // this.resetAllFields()
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
        <div v-if="registrationNotComplete">
          <h1>Registreeru kasutajaks</h1>
        </div>
      </div>
    </div>
    <div v-if="registrationNotComplete" class="row justify-content-center mb-5">
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
            <SportsMultiSelect :sports="sports" @event-new-sports-selected="handleSportsSelected" />
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
              <input
                v-model="registerRequest.password"
                type="password"
                class="form-control border border-dark"
                placeholder=""
              />
              <label>Salasõna</label>
            </div>
            <div class="form-floating">
              <input
                v-model="passwordRepeat"
                type="password"
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
                v-model="termsAccepted"
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
    <div v-if="registrationNotComplete" class="row justify-content-center">
      <div class="col">
        <button @click="goBack" class="btn btn-secondary me-3" type="button">Tagasi</button>
        <button
          :disabled="!termsAccepted"
          @click="registerUser"
          class="btn btn-success"
          type="submit"
        >
          Registreeru
        </button>
      </div>
    </div>
  </div>
</template>
