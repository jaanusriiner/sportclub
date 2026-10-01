<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import SportService from '@/services/SportService.js'
import TrainerService from '@/services/TrainerService.js'
import TrainingGroupService from '@/services/TrainingGroupService.js'

export default {
  name: 'CreateTrainingGroupView',
  components: { AlertDanger },
  beforeMount() {
    this.getTrainerSportclubs()
    this.getSports()
  },
  data() {
    return {
      errorMessage: '',
      sportclubs: [],
      sports: [],
      skillLevels: [],

      trainingGroupRequest: {
        trainerId: SessionStorageService.getUserId(),
        sportclubId: 0,
        sportId: 0,
        skillLevelId: 0,
        trainingGroupName: '',
        description: '',
      },
    }
  },
  methods: {
    getTrainerSportclubs() {
      TrainerService.getTrainerSportclubsRequest(this.trainingGroupRequest.trainerId)
        .then((response) => (this.sportclubs = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getSports() {
      SportService.getSportsRequest()
        .then((response) => (this.sports = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleSportChanged() {
      this.trainingGroupRequest.skillLevelId = 0
      this.skillLevels = []
      if (this.trainingGroupRequest.sportId !== 0) {
        this.getSkillLevels()
      }
    },

    getSkillLevels() {
      SportService.getSkillLevelsRequest(this.trainingGroupRequest.sportId)
        .then((response) => (this.skillLevels = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    createTrainingGroup() {
      this.errorMessage = ''
      this.checkFormForErrors()
      if (this.errorMessage === '') {
        TrainingGroupService.postTrainingGroupRequest(this.trainingGroupRequest)
          .then(() => NavigationService.navigateToManageTrainingsView())
          .catch((error) => this.handleCreateError(error))
      }
    },

    checkFormForErrors() {
      if (this.trainingGroupRequest.sportclubId === 0) {
        this.errorMessage = 'Vali spordiklubi'
      } else if (this.trainingGroupRequest.sportId === 0) {
        this.errorMessage = 'Vali spordiala'
      } else if (this.trainingGroupRequest.skillLevelId === 0) {
        this.errorMessage = 'Vali oskustase'
      } else if (this.trainingGroupRequest.trainingGroupName.trim() === '') {
        this.errorMessage = 'Sisesta treeninggrupi nimi'
      }
    },

    handleCreateError(error) {
      if (error.response && [400, 403].includes(error.response.status)) {
        this.errorMessage = error.response.data.message
      } else {
        NavigationService.navigateToErrorView()
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
        <h1>Loo uus treeninggrupp</h1>
        <AlertDanger :error-message="errorMessage" />
      </div>
    </div>
    <div class="row justify-content-center mb-5">
      <div class="col col-5">
        <div class="mb-3">
          <label class="form-label">Spordiklubi</label>
          <select v-model="trainingGroupRequest.sportclubId" class="form-select border border-dark">
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
          <label class="form-label">Spordiala</label>
          <select
            v-model="trainingGroupRequest.sportId"
            class="form-select border border-dark"
            @change="handleSportChanged"
          >
            <option :value="0">Vali spordiala...</option>
            <option v-for="sport in sports" :key="sport.sportId" :value="sport.sportId">
              {{ sport.sportName }}
            </option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label">Skill-level</label>
          <select
            v-model="trainingGroupRequest.skillLevelId"
            class="form-select border border-dark"
            :disabled="trainingGroupRequest.sportId === 0"
          >
            <option :value="0">Vali oskustase...</option>
            <option
              v-for="skillLevel in skillLevels"
              :key="skillLevel.skillLevelId"
              :value="skillLevel.skillLevelId"
            >
              {{ skillLevel.skillLevelName }}
            </option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label">Treeninggrupi nimi</label>
          <input
            v-model="trainingGroupRequest.trainingGroupName"
            type="text"
            class="form-control border border-dark"
            maxlength="100"
          />
        </div>

        <div class="mb-3">
          <label class="form-label">Kirjeldus</label>
          <textarea
            v-model="trainingGroupRequest.description"
            class="form-control border border-dark"
            rows="4"
            maxlength="255"
          ></textarea>
        </div>
      </div>
    </div>
    <div class="row justify-content-center">
      <div class="col col-5 text-center">
        <button @click="goBack" class="btn btn-secondary me-3" type="button">Tagasi</button>
        <button @click="createTrainingGroup" class="btn btn-success" type="submit">Salvesta</button>
      </div>
    </div>
  </div>
</template>
