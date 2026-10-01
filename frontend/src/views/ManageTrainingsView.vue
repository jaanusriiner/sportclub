<script>
import TrainingGroupsDropDown from '@/components/dropdown/TrainingGroupsDropDown.vue'
import TrainingDeleteModal from '@/components/modal/TrainingDeleteModal.vue'
import TrainingEditModal from '@/components/modal/TrainingEditModal.vue'
import TrainingInfoModal from '@/components/modal/TrainingInfoModal.vue'
import TrainerService from '@/services/TrainerService.js'
import TrainingService from '@/services/TrainingService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import { PhCheck, PhInfo, PhPencilSimple, PhTrash, PhX } from '@phosphor-icons/vue'
import JoinApplicationService from '@/services/JoinApplicationService.js'
import SportclubsDropDown from '@/components/dropdown/SportclubsDropDown.vue'
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AlertSuccess from '@/components/alert/AlertSuccess.vue'

export default {
  name: 'ManageTrainingsView',
  components: {
    SportclubsDropDown,
    TrainingGroupsDropDown,
    TrainingDeleteModal,
    TrainingEditModal,
    TrainingInfoModal,
    PhCheck,
    PhInfo,
    PhPencilSimple,
    PhTrash,
    PhX,
    AlertDanger,
    AlertSuccess,
  },
  beforeMount() {
    this.getTrainerJoinApplications()
    this.getTrainerTrainingGroups()
    this.getTrainerTrainings()
  },
  data() {
    return {
      trainings: [],
      trainerTrainingGroups: [],
      joinApplications: [],
      selectedSportclubId: 0,
      selectedTrainingGroupId: 0,
      infoModalIsOpen: false,
      deleteModalIsOpen: false,
      editModalIsOpen: false,
      selectedTraining: null,
      joinApplicationErrorMessage: '',
      successMessage: '',
      isProcessingJoinApplication: false,
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
    getTrainerJoinApplications() {
      TrainerService.getTrainerJoinApplicationsRequest(SessionStorageService.getUserId())
        .then((response) => (this.joinApplications = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getTrainerTrainingGroups() {
      TrainerService.getTrainerTrainingGroupsRequest(SessionStorageService.getUserId())
        .then((response) => (this.trainerTrainingGroups = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    openInfoModal(training) {
      this.selectedTraining = training
      this.infoModalIsOpen = true
    },

    openEditModal(training) {
      this.successMessage = ''
      this.selectedTraining = training
      this.editModalIsOpen = true
    },

    closeEditModal() {
      this.editModalIsOpen = false
    },

    handleTrainingUpdated() {
      this.closeEditModal()
      this.successMessage = 'Treeningu muudatused on salvestatud'
      this.getTrainerTrainings()
    },

    openDeleteModal(training) {
      this.successMessage = ''
      this.selectedTraining = training
      this.deleteModalIsOpen = true
    },

    closeDeleteModal() {
      this.deleteModalIsOpen = false
    },

    deleteTraining() {
      TrainingService.deleteTrainingDateRequest(this.selectedTraining.trainingDateId)
        .then(() => this.handleDeleteTrainingResponse())
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleDeleteTrainingResponse() {
      this.closeDeleteModal()
      this.successMessage = 'Treening on kustutatud'
      this.getTrainerTrainings()
    },

    goToCreateTrainingGroup() {
      NavigationService.navigateToCreateTrainingGroupView()
    },

    closeInfoModal() {
      this.infoModalIsOpen = false
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

    confirmJoinApplication(joinApplicationId) {
      this.joinApplicationErrorMessage = ''
      this.isProcessingJoinApplication = true

      JoinApplicationService.putConfirmRequest(joinApplicationId)
        .then(() => this.handleJoinApplicationProcessed())
        .catch((error) => this.handleJoinApplicationError(error))
    },

    rejectJoinApplication(joinApplicationId) {
      this.joinApplicationErrorMessage = ''
      this.isProcessingJoinApplication = true

      JoinApplicationService.putRejectRequest(joinApplicationId)
        .then(() => this.handleJoinApplicationProcessed())
        .catch((error) => this.handleJoinApplicationError(error))
    },

    handleJoinApplicationProcessed() {
      this.isProcessingJoinApplication = false
      this.getTrainerJoinApplications()
    },

    handleJoinApplicationError(error) {
      this.isProcessingJoinApplication = false
      this.getTrainerJoinApplications()

      const response = error.response
      if (response && (response.status === 403 || response.status === 404)) {
        this.joinApplicationErrorMessage = response.data.message || 'Midagi läks valesti.'
      } else {
        NavigationService.navigateToErrorView()
      }
    },
  },
}
</script>

<template>
  <div class="container">
    <div class="row justify-content-center mb-4">
      <div class="col">
        <h1 class="text-center">Halda treeninggruppe ja treeninguid</h1>
        <AlertSuccess :success-message="successMessage" />
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
              <th>Toimingud</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="training in filteredTrainings" :key="training.trainingDateId">
              <td>{{ training.facilityName }}</td>
              <td>{{ training.sportclubName }}</td>
              <td>{{ training.sportName }} - {{ training.skillLevelName }}</td>
              <td>{{ formatDateTime(training.trainingDate, training.trainingTime) }}</td>
              <td>{{ training.userCount }}/{{ training.maxSize }}</td>
              <td class="text-nowrap">
                <button
                  type="button"
                  class="btn btn-link p-1"
                  title="Info"
                  @click="openInfoModal(training)"
                >
                  <PhInfo :size="22" />
                </button>
                <button
                  type="button"
                  class="btn btn-link p-1"
                  title="Muuda"
                  @click="openEditModal(training)"
                >
                  <PhPencilSimple :size="22" />
                </button>
                <button
                  type="button"
                  class="btn btn-link p-1"
                  title="Kustuta"
                  @click="openDeleteModal(training)"
                >
                  <PhTrash :size="22" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <div class="row mb-4">
      <div class="col">
        <button type="button" class="btn btn-primary" @click="goToCreateTrainingGroup">
          Loo uus Treeninggrupp
        </button>
      </div>
    </div>
    <div class="row mt-5 mb-2">
      <div class="col">
        <h2 class="h4">Treeninggruppide liitumistaotlused</h2>
      </div>
    </div>
    <div class="row justify-content-center mb-3">
      <div class="col">
        <AlertDanger :error-message="joinApplicationErrorMessage" />
      </div>
    </div>
    <div class="row justify-content-center mb-5">
      <div class="col">
        <table class="table table-bordered align-middle">
          <thead>
            <tr>
              <th>Nimi</th>
              <th>Spordiklubi</th>
              <th>Treeninggrupp</th>
              <th>Grupi hetketäituvus</th>
              <th>Kinnitus</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="joinApplication in joinApplications"
              :key="joinApplication.joinApplicationId"
            >
              <td>{{ joinApplication.userFullName }}</td>
              <td>{{ joinApplication.sportclubName }}</td>
              <td>{{ joinApplication.trainingGroupName }}</td>
              <td>{{ joinApplication.trainingGroupMemberCount }}</td>
              <td class="text-nowrap">
                <button
                  type="button"
                  class="btn btn-link p-1"
                  title="Kinnita"
                  :disabled="isProcessingJoinApplication"
                  @click="confirmJoinApplication(joinApplication.joinApplicationId)"
                >
                  <PhCheck :size="22" />
                </button>
                <button
                  type="button"
                  class="btn btn-link p-1"
                  title="Lükka tagasi"
                  :disabled="isProcessingJoinApplication"
                  @click="rejectJoinApplication(joinApplication.joinApplicationId)"
                >
                  <PhX :size="22" />
                </button>
              </td>
            </tr>
            <tr v-if="joinApplications.length === 0">
              <td colspan="5" class="text-center">Ootel liitumistaotlusi ei ole</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <TrainingEditModal
      :edit-modal-is-open="editModalIsOpen"
      :training="selectedTraining"
      @event-edit-modal-closed="closeEditModal"
      @event-training-updated="handleTrainingUpdated"
    />
    <TrainingDeleteModal
      :delete-modal-is-open="deleteModalIsOpen"
      :training="selectedTraining"
      :date-time="
        selectedTraining &&
        formatDateTime(selectedTraining.trainingDate, selectedTraining.trainingTime)
      "
      @event-delete-modal-closed="closeDeleteModal"
      @event-delete-confirmed="deleteTraining"
    />
    <TrainingInfoModal
      :info-modal-is-open="infoModalIsOpen"
      :training="selectedTraining"
      :date-time="
        selectedTraining &&
        formatDateTime(selectedTraining.trainingDate, selectedTraining.trainingTime)
      "
      @event-info-modal-closed="closeInfoModal"
    />
  </div>
</template>
