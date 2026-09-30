<script>
import SportclubsDropDown from '@/components/dropdown/SportclubsDropDown.vue'
import TrainingGroupsDropDown from '@/components/dropdown/TrainingGroupsDropDown.vue'
import TrainerService from '@/services/TrainerService.js'
import TrainingService from '@/services/TrainingService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'

export default {
  name: 'ManageTrainingsView',
  components: { SportclubsDropDown, TrainingGroupsDropDown },
  beforeMount() {
    this.getTrainerTrainingGroups()
    this.getTrainerTrainings()
  },
  data() {
    return {
      trainings: [],
      trainerTrainingGroups: [],
      selectedSportclubId: 0,
      selectedTrainingGroupId: 0,
    }
  },
  computed: {
    sportclubs() {
      const sportclubs = new Map()
      this.trainerTrainingGroups.forEach((trainingGroup) =>
        sportclubs.set(trainingGroup.sportclubId, {
          sportclubId: trainingGroup.sportclubId,
          sportclubName: trainingGroup.sportclubName,
        }),
      )
      return [...sportclubs.values()]
    },

    trainingGroups() {
      return this.trainerTrainingGroups.filter(
        (trainingGroup) =>
          this.selectedSportclubId === 0 || trainingGroup.sportclubId === this.selectedSportclubId,
      )
    },

    // /api/trainings ei toeta sportclubId ja trainingGroupId filtreid, seepärast filtreerime siin
    filteredTrainings() {
      return this.trainings.filter(
        (training) =>
          (this.selectedSportclubId === 0 || training.sportclubId === this.selectedSportclubId) &&
          (this.selectedTrainingGroupId === 0 ||
            training.trainingGroupId === this.selectedTrainingGroupId),
      )
    },
  },
  methods: {
    getTrainerTrainingGroups() {
      TrainerService.getTrainerTrainingGroupsRequest(SessionStorageService.getUserId())
        .then((response) => (this.trainerTrainingGroups = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleSportclubSelected(sportclubId) {
      this.selectedSportclubId = sportclubId
      this.selectedTrainingGroupId = 0
    },

    handleTrainingGroupSelected(trainingGroupId) {
      this.selectedTrainingGroupId = trainingGroupId
    },

    getTrainerTrainings() {
      const trainerId = SessionStorageService.getUserId()
      TrainingService.getTrainingsRequest({
        requestUserId: trainerId,
        areaId: 0,
        sportId: 0,
        trainerId: trainerId,
        dateFrom: new Date().toLocaleDateString('sv-SE'),
        timeFrom: '00:00',
        page: 1,
        size: 100,
      })
        .then((response) => this.handleGetTrainingsResponse(response))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetTrainingsResponse(response) {
      this.trainings = response.data.trainings
    },

    formatDateTime(trainingDate, trainingTime) {
      const [year, month, day] = trainingDate.split('-')
      return `${day}.${month}.${year} ${trainingTime.substring(0, 5)}`
    },
  },
}
</script>

<template>
  <div class="container">
    <div class="row justify-content-center mb-4">
      <div class="col">
        <h1 class="text-center">Halda treeninggruppe ja treeninguid</h1>
      </div>
    </div>
    <div class="row mb-3">
      <div class="col-3">
        <SportclubsDropDown
          :sportclubs="sportclubs"
          :sportclub-id="selectedSportclubId"
          @event-new-sportclub-selected="handleSportclubSelected"
        />
      </div>
      <div class="col-3">
        <TrainingGroupsDropDown
          :training-groups="trainingGroups"
          :training-group-id="selectedTrainingGroupId"
          @event-new-training-group-selected="handleTrainingGroupSelected"
        />
      </div>
    </div>
    <div class="row justify-content-center">
      <div class="col">
        <table class="table table-bordered align-middle">
          <thead>
            <tr>
              <th>Asukoht</th>
              <th>Spordiklubi</th>
              <th>Treeninggrupp</th>
              <th>Kuupäev/Aeg</th>
              <th>Täituvus</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="training in filteredTrainings" :key="training.trainingDateId">
              <td>{{ training.facilityName }}</td>
              <td>{{ training.sportclubName }}</td>
              <td>{{ training.sportName }} - {{ training.skillLevelName }}</td>
              <td>{{ formatDateTime(training.trainingDate, training.trainingTime) }}</td>
              <td>{{ training.userCount }}/{{ training.maxSize }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
