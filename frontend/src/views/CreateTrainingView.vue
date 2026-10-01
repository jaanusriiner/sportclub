<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import FacilitiesDropDown from '@/components/dropdown/FacilitiesDropDown.vue'
import FacilityService from '@/services/FacilityService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import TrainerService from '@/services/TrainerService.js'

const WEEKDAYS = [
  { code: 'E', name: 'Esmaspäev' },
  { code: 'T', name: 'Teisipäev' },
  { code: 'K', name: 'Kolmapäev' },
  { code: 'N', name: 'Neljapäev' },
  { code: 'R', name: 'Reede' },
  { code: 'L', name: 'Laupäev' },
  { code: 'P', name: 'Pühapäev' },
]

export default {
  name: 'CreateTrainingView',
  components: { AlertDanger, FacilitiesDropDown },
  beforeMount() {
    this.getTrainerSportclubs()
    this.getTrainerTrainingGroups()
    this.getFacilities()
  },
  data() {
    return {
      errorMessage: '',
      weekdays: WEEKDAYS,
      sportclubs: [],
      trainerTrainingGroups: [],
      facilities: [],

      trainingRequest: {
        sportclubId: 0,
        trainingGroupId: 0,
        facilityId: 0,
        weekdays: [],
        startTime: '',
        maxSize: null,
        description: '',
      },
    }
  },
  computed: {
    trainingGroups() {
      return this.trainerTrainingGroups.filter(
        (trainingGroup) => trainingGroup.sportclubId === this.trainingRequest.sportclubId,
      )
    },
  },
  methods: {
    getTrainerSportclubs() {
      TrainerService.getTrainerSportclubsRequest(SessionStorageService.getUserId())
        .then((response) => (this.sportclubs = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getTrainerTrainingGroups() {
      TrainerService.getTrainerTrainingGroupsRequest(SessionStorageService.getUserId())
        .then((response) => (this.trainerTrainingGroups = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getFacilities() {
      FacilityService.getFacilitiesRequest()
        .then((response) => (this.facilities = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleSportclubChanged() {
      this.trainingRequest.trainingGroupId = 0
    },

    createTraining() {
      this.errorMessage = ''
      this.checkFormForErrors()
      if (this.errorMessage === '') {
        // TODO: saada päring, kui backendis valmib treeningu loomise endpoint
      }
    },

    checkFormForErrors() {
      if (this.trainingRequest.sportclubId === 0) {
        this.errorMessage = 'Vali spordiklubi'
      } else if (this.trainingRequest.trainingGroupId === 0) {
        this.errorMessage = 'Vali treeninggrupp'
      } else if (this.trainingRequest.facilityId === 0) {
        this.errorMessage = 'Vali asukoht'
      } else if (this.trainingRequest.weekdays.length === 0) {
        this.errorMessage = 'Vali vähemalt üks nädalapäev'
      } else if (this.trainingRequest.startTime === '') {
        this.errorMessage = 'Sisesta treeningu algusaeg'
      } else if (!this.trainingRequest.maxSize || this.trainingRequest.maxSize < 1) {
        this.errorMessage = 'Sisesta maksimaalne osalejate arv'
      }
    },

    goBack() {
      NavigationService.navigateToManageTrainingsView()
    },
  },
}
</script>

<template>
  <div class="container">
    <div class="row justify-content-center mb-4">
      <div class="col col-5">
        <h1>Loo uus treening</h1>
        <AlertDanger :error-message="errorMessage" />
      </div>
    </div>
    <div class="row justify-content-center mb-5">
      <div class="col col-5">
        <div class="mb-3">
          <label class="form-label">Spordiklubi</label>
          <select
            v-model="trainingRequest.sportclubId"
            class="form-select border border-dark"
            @change="handleSportclubChanged"
          >
            <option :value="0">Vali spordiklubi...</option>
            <option
              v-for="sportclub in sportclubs"
              :key="sportclub.sportclubId"
              :value="sportclub.sportclubId"
            >
              {{ sportclub.sportclubName }}
            </option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label">Treeninggrupi nimi</label>
          <select
            v-model="trainingRequest.trainingGroupId"
            class="form-select border border-dark"
            :disabled="trainingRequest.sportclubId === 0"
          >
            <option :value="0">Vali treeninggrupp...</option>
            <option
              v-for="trainingGroup in trainingGroups"
              :key="trainingGroup.trainingGroupId"
              :value="trainingGroup.trainingGroupId"
            >
              {{ trainingGroup.trainingGroupName }}
            </option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label">Asukoht</label>
          <FacilitiesDropDown
            :facilities="facilities"
            :facility-id="trainingRequest.facilityId"
            @event-new-facility-selected="(facilityId) => (trainingRequest.facilityId = facilityId)"
          />
        </div>

        <div class="mb-3">
          <label class="form-label">Treeningu toimumise aeg</label>
          <div class="mb-2">
            <div
              v-for="weekday in weekdays"
              :key="weekday.code"
              class="form-check form-check-inline"
            >
              <input
                :id="'weekday-' + weekday.code"
                v-model="trainingRequest.weekdays"
                :value="weekday.code"
                type="checkbox"
                class="form-check-input border-dark"
              />
              <label
                class="form-check-label"
                :for="'weekday-' + weekday.code"
                :title="weekday.name"
              >
                {{ weekday.code }}
              </label>
            </div>
          </div>
          <input
            v-model="trainingRequest.startTime"
            type="time"
            class="form-control border border-dark w-auto"
          />
        </div>

        <div class="mb-3">
          <label class="form-label">Maksimaalne osalejate arv</label>
          <input
            v-model.number="trainingRequest.maxSize"
            type="number"
            min="1"
            class="form-control border border-dark w-auto"
          />
        </div>

        <div class="mb-3">
          <label class="form-label">Kirjeldus</label>
          <textarea
            v-model="trainingRequest.description"
            class="form-control border border-dark"
            rows="4"
            maxlength="255"
          ></textarea>
        </div>
      </div>
    </div>
    <div class="row justify-content-center">
      <div class="col col-5 text-center">
        <button @click="goBack" class="btn btn-secondary me-3" type="button">Sulge</button>
        <button @click="createTraining" class="btn btn-success" type="submit">Loo</button>
      </div>
    </div>
  </div>
</template>
