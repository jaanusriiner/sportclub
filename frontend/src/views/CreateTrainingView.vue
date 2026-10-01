<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import FacilitiesDropDown from '@/components/dropdown/FacilitiesDropDown.vue'
import FacilityService from '@/services/FacilityService.js'
import TrainingService from '@/services/TrainingService.js'
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
      todayDate: new Date().toLocaleDateString('sv-SE'),
      sportclubs: [],
      trainerTrainingGroups: [],
      facilities: [],

      trainingRequest: {
        sportclubId: 0,
        trainingGroupId: 0,
        facilityId: 0,
        weekdays: [],
        startTime: '',
        duration: 90,
        startDate: new Date().toLocaleDateString('sv-SE'),
        endDate: '',
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
        TrainingService.postTrainingRequest(this.createRequestBody())
          .then(() => NavigationService.navigateToManageTrainingsView())
          .catch((error) => this.handleCreateError(error))
      }
    },

    createRequestBody() {
      // backend ootab nädalapäevi komadega eraldatud stringina, nt "E,N"
      const orderedWeekdays = this.weekdays
        .map((weekday) => weekday.code)
        .filter((code) => this.trainingRequest.weekdays.includes(code))
      return {
        trainerId: SessionStorageService.getUserId(),
        trainingGroupId: this.trainingRequest.trainingGroupId,
        facilityId: this.trainingRequest.facilityId,
        weekdays: orderedWeekdays.join(','),
        startTime: this.trainingRequest.startTime,
        duration: this.trainingRequest.duration,
        startDate: this.trainingRequest.startDate,
        endDate: this.trainingRequest.endDate,
        maxSize: this.trainingRequest.maxSize,
        description: this.trainingRequest.description,
      }
    },

    handleCreateError(error) {
      if (error.response && [400, 403, 404].includes(error.response.status)) {
        this.errorMessage = error.response.data.message
      } else {
        NavigationService.navigateToErrorView()
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
      } else if (!this.trainingRequest.duration || this.trainingRequest.duration < 1) {
        this.errorMessage = 'Sisesta treeningu kestus minutites'
      } else if (!this.trainingRequest.maxSize || this.trainingRequest.maxSize < 1) {
        this.errorMessage = 'Sisesta maksimaalne osalejate arv'
      } else if (this.trainingRequest.startDate === '') {
        this.errorMessage = 'Sisesta alguskuupäev'
      } else if (this.trainingRequest.endDate === '') {
        this.errorMessage = 'Sisesta lõppkuupäev'
      } else if (this.trainingRequest.endDate < this.trainingRequest.startDate) {
        this.errorMessage = 'Lõpu kuupäev ei tohi olla enne alguse kuupäeva'
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
      <div class="col-12 col-md-8 col-lg-6">
        <h1>Loo uus treening</h1>
        <AlertDanger :error-message="errorMessage" />
      </div>
    </div>
    <div class="row justify-content-center mb-4">
      <div class="col-12 col-md-8 col-lg-6 card p-4">
        <div class="mb-3">
          <label class="form-label">Spordiklubi</label>
          <select
            v-model="trainingRequest.sportclubId"
            class="form-select"
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
            class="form-select"
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
                class="form-check-input"
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
          <input v-model="trainingRequest.startTime" type="time" class="form-control w-auto" />
        </div>

        <div class="mb-3">
          <label class="form-label">Treeningu kestus (minutites)</label>
          <input
            v-model.number="trainingRequest.duration"
            type="number"
            min="1"
            class="form-control w-auto"
          />
        </div>

        <div class="mb-3">
          <label class="form-label">Treeningute periood</label>
          <div class="d-flex align-items-center gap-2">
            <input
              v-model="trainingRequest.startDate"
              type="date"
              :min="todayDate"
              class="form-control w-auto"
              aria-label="Alguskuupäev"
            />
            <span>–</span>
            <input
              v-model="trainingRequest.endDate"
              type="date"
              :min="trainingRequest.startDate || todayDate"
              class="form-control w-auto"
              aria-label="Lõppkuupäev"
            />
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label">Maksimaalne osalejate arv</label>
          <input
            v-model.number="trainingRequest.maxSize"
            type="number"
            min="1"
            class="form-control w-auto"
          />
        </div>

        <div class="mb-3">
          <label class="form-label">Kirjeldus</label>
          <textarea
            v-model="trainingRequest.description"
            class="form-control"
            rows="4"
            maxlength="255"
          ></textarea>
        </div>
      </div>
    </div>
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-6 text-center">
        <button @click="goBack" class="btn btn-secondary me-3" type="button">Tagasi</button>
        <button @click="createTraining" class="btn btn-primary" type="submit">Salvesta</button>
      </div>
    </div>
  </div>
</template>
