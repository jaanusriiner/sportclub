<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AlertSuccess from '@/components/alert/AlertSuccess.vue'
import AreaService from '@/services/AreaService.js'
import NavigationService from '@/services/NavigationService.js'
import SportService from '@/services/SportService.js'
import AreasDropDown from '@/components/dropdown/AreasDropDown.vue'
import TomSelect from 'tom-select'

export default {
  name: 'RegisterView',
  components: { AreasDropDown, AlertSuccess, AlertDanger },
  beforeMount() {
    this.getAreas()
    this.getSports()
  },
  mounted() {
    this.sportsSelect = new TomSelect('#sports', {
      plugins: ['remove_button'], // adds an × to remove each selected tag
      placeholder: 'Vali spordialad...',
      maxItems: null, // null = unlimited selections
      create: false, // prevents users typing in new options that don't exist
      controlClass: 'ts-control form-select border border-dark', // näeb välja nagu teised valikuväljad
    })
  },
  beforeUnmount() {
    this.sportsSelect?.destroy()
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

    alertInfo(id) {
      alert(id)
    }

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
            <AreasDropDown :areas="areas" @event-new-area-selected="alertInfo"/>
          </div>
          <div class="col">

            <div class="mb-3">
              <label for="sports" class="form-label"></label>
              <select id="sports" multiple>
                <option value="jousaal">Jõusaal</option>
                <option value="jooga">Jooga</option>
                <option value="crossfit">Crossfit</option>
                <option value="pilates">Pilates</option>
                <option value="boks">Poks</option>
              </select>
            </div>




<!--            <div class="dropdown">-->
<!--              <button class="btn btn-outline-primary dropdown-toggle border border-dark" type="button" data-bs-toggle="dropdown" data-bs-auto-close="outside">-->
<!--                Spordialad-->
<!--              </button>-->
<!--              <ul class="dropdown-menu p-2" style="min-width: 220px;">-->
<!--                <li><div class="form-check">-->
<!--                  <input class="form-check-input" type="checkbox" value="1" id="opt1">-->
<!--                  <label class="form-check-label" for="opt1">Jõusaal</label>-->
<!--                </div></li>-->
<!--                <li><div class="form-check">-->
<!--                  <input class="form-check-input" type="checkbox" value="2" id="opt2">-->
<!--                  <label class="form-check-label" for="opt2">Jooga</label>-->
<!--                </div></li>-->
<!--                <li><div class="form-check">-->
<!--                  <input class="form-check-input" type="checkbox" value="3" id="opt3">-->
<!--                  <label class="form-check-label" for="opt3">Crossfit</label>-->
<!--                </div></li>-->
<!--              </ul>-->
<!--            </div>-->
          </div>
        </div>
        <div class="row justify-content-center mb-5">
          <div class="col">
            <div class="mb-3">
              <label for="sports" class="form-label">Vali spordialad</label>
              <select id="sports" multiple>
                <option value="jousaal">Jõusaal</option>
                <option value="jooga">Jooga</option>
                <option value="crossfit">Crossfit</option>
                <option value="pilates">Pilates</option>
                <option value="boks">Poks</option>
              </select>
            </div>
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

<style scoped>
/* Tom Select kirjutab valitud spordialade puhul oma padding'u üle, mis katab muidu form-select
   noole ära - taastame ruumi noole jaoks, et väli näeks endiselt dropdown'i moodi välja. */
:deep(.ts-wrapper .ts-control.form-select) {
  padding-right: 2.25rem !important;
}
</style>
