<script>
import AlertSuccess from '@/components/alert/AlertSuccess.vue'
import TrainingConfirmModal from '@/components/modal/TrainingConfirmModal.vue'
import TrainingInfoModal from '@/components/modal/TrainingInfoModal.vue'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import TrainingGroupService from '@/services/TrainingGroupService.js'
import TrainingService from '@/services/TrainingService.js'
import UserService from '@/services/UserService.js'
import { PhInfo } from '@phosphor-icons/vue'

export default {
  name: 'TrainingsView',
  components: { AlertSuccess, TrainingConfirmModal, TrainingInfoModal, PhInfo },
  inject: ['openLoginModal'],
  beforeMount() {
    this.getMyTrainings()
    this.getTrainings()
  },
  data() {
    return {
      trainings: [],
      myTrainings: [],
      isLoggedIn: SessionStorageService.userIsLoggedIn(),
      successMessage: '',
      modalErrorMessage: '',
      selectedTraining: null,
      infoModalIsOpen: false,
      registerModalIsOpen: false,
      joinModalIsOpen: false,
    }
  },
  computed: {
    // kasutaja registreeritud treeningud on tabelis "Minu ..." ega kuvata alumises tabelis uuesti
    availableTrainings() {
      return this.trainings.filter((training) => !training.userIsRegistered)
    },
  },
  methods: {
    getMyTrainings() {
      if (this.isLoggedIn) {
        UserService.getUserTrainingsRequest(SessionStorageService.getUserId())
          .then((response) => (this.myTrainings = response.data))
          .catch(() => NavigationService.navigateToErrorView())
      }
    },

    getTrainings() {
      TrainingService.getTrainingsRequest({
        requestUserId: SessionStorageService.getUserId(),
        areaId: 0,
        sportId: 0,
        trainerId: 0,
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

    openInfoModal(training) {
      this.selectedTraining = training
      this.infoModalIsOpen = true
    },

    // MyTrainingDto kasutab nextTrainingDate/nextTrainingTime, infomodaal ootab trainingDate/trainingTime
    openMyTrainingInfoModal(myTraining) {
      this.openInfoModal({
        ...myTraining,
        trainingDate: myTraining.nextTrainingDate,
        trainingTime: myTraining.nextTrainingTime,
      })
    },

    openRegisterModal(training) {
      this.selectedTraining = training
      this.modalErrorMessage = ''
      this.registerModalIsOpen = true
    },

    openJoinModal(training) {
      if (!this.isLoggedIn) {
        this.openLoginModal()
        return
      }
      this.selectedTraining = training
      this.modalErrorMessage = ''
      this.joinModalIsOpen = true
    },

    closeModals() {
      this.infoModalIsOpen = false
      this.registerModalIsOpen = false
      this.joinModalIsOpen = false
    },

    registerToTraining() {
      this.successMessage = ''
      TrainingService.postRegisterToTrainingRequest(
        this.selectedTraining.trainingDateId,
        SessionStorageService.getUserId(),
      )
        .then((response) => this.handleActionSuccess(response.data.message))
        .catch((error) => this.handleActionError(error))
    },

    applyToJoinTrainingGroup() {
      this.successMessage = ''
      TrainingGroupService.postJoinApplicationRequest(
        this.selectedTraining.trainingGroupId,
        SessionStorageService.getUserId(),
      )
        .then((response) => this.handleActionSuccess(response.data.message))
        .catch((error) => this.handleActionError(error))
    },

    handleActionSuccess(message) {
      this.closeModals()
      this.successMessage = message
      this.getMyTrainings()
      this.getTrainings()
    },

    handleActionError(error) {
      if (error.response && error.response.status === 403) {
        this.modalErrorMessage = error.response.data.message
      } else {
        NavigationService.navigateToErrorView()
      }
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
    <div class="row justify-content-center">
      <div class="col">
        <AlertSuccess :success-message="successMessage" />
      </div>
    </div>
    <template v-if="isLoggedIn">
      <div class="row justify-content-center mb-4">
        <div class="col">
          <h2 class="text-center">Minu Treeninggrupid ja Treeningud</h2>
        </div>
      </div>
      <div class="row justify-content-center mb-5">
        <div class="col">
          <p v-if="myTrainings.length === 0" class="text-center">
            Hetkel pole ühelegi treeningule registreeritud.
          </p>
          <div v-else class="table-responsive rounded shadow-sm border">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-dark">
                <tr>
                  <th>Sport</th>
                  <th>Asukoht</th>
                  <th>Treener</th>
                  <th>Järgmine treening</th>
                  <th>Täituvus</th>
                  <th>Toimingud</th>
                  <th>Info</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="myTraining in myTrainings"
                  :key="myTraining.trainingDateId"
                  class="clickable-row"
                  @click="openMyTrainingInfoModal(myTraining)"
                >
                  <td>{{ myTraining.sportName }}</td>
                  <td>{{ myTraining.facilityName }}</td>
                  <td>{{ myTraining.trainerName }}</td>
                  <td class="text-nowrap">
                    {{ formatDateTime(myTraining.nextTrainingDate, myTraining.nextTrainingTime) }}
                  </td>
                  <td>{{ myTraining.userCount }}/{{ myTraining.maxSize }}</td>
                  <td>
                    <button
                      type="button"
                      class="btn btn-outline-danger btn-sm text-nowrap"
                      @click.stop
                    >
                      Vabasta koht
                    </button>
                  </td>
                  <td class="text-nowrap">
                    <button
                      type="button"
                      class="btn btn-link p-1"
                      title="Info"
                      @click.stop="openMyTrainingInfoModal(myTraining)"
                    >
                      <PhInfo :size="22" />
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </template>
    <div class="row justify-content-center mb-4">
      <div class="col">
        <h2 class="text-center">Treeningud</h2>
      </div>
    </div>
    <div class="row justify-content-center">
      <div class="col">
        <div class="table-responsive rounded shadow-sm border">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
              <tr>
                <th>Sport</th>
                <th>Asukoht</th>

                <th>Spordiklubi</th>
                <th>Oskustase</th>
                <th>Järgmine treening</th>
                <th>Täituvus</th>
                <th>Info</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="training in availableTrainings"
                :key="training.trainingDateId"
                class="clickable-row"
                @click="openInfoModal(training)"
              >
                <td>{{ training.sportName }}</td>
                <td>{{ training.facilityName }}</td>

                <td>{{ training.sportclubName }}</td>
                <td>{{ training.skillLevelName }}</td>
                <td class="text-nowrap">
                  {{ formatDateTime(training.trainingDate, training.trainingTime) }}
                </td>
                <td>
                  <template v-if="training.userIsTrainingGroupMember">
                    <div>{{ training.userCount }}/{{ training.maxSize }}</div>
                    <span v-if="training.userIsRegistered">Registreeritud</span>
                    <span v-else-if="training.userCount >= training.maxSize">Kohad on täis</span>
                    <button
                      v-else
                      type="button"
                      class="btn btn-success btn-sm text-nowrap mt-1"
                      @click.stop="openRegisterModal(training)"
                    >
                      Registreeru
                    </button>
                  </template>
                  <span v-else-if="training.userHasPendingJoinApplication">Taotlus edastatud</span>
                  <button
                    v-else
                    type="button"
                    class="btn btn-primary btn-sm text-nowrap"
                    @click.stop="openJoinModal(training)"
                  >
                    Taotle Liitumist
                  </button>
                </td>
                <td class="text-nowrap">
                  <button
                    type="button"
                    class="btn btn-link p-1"
                    title="Info"
                    @click.stop="openInfoModal(training)"
                  >
                    <PhInfo :size="22" />
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <TrainingInfoModal
      :info-modal-is-open="infoModalIsOpen"
      :training="selectedTraining"
      :date-time="
        selectedTraining &&
        formatDateTime(selectedTraining.trainingDate, selectedTraining.trainingTime)
      "
      @event-info-modal-closed="closeModals"
    />
    <TrainingConfirmModal
      :confirm-modal-is-open="registerModalIsOpen"
      title="Treeningule registreerimine"
      confirm-button-text="Registreeru"
      :error-message="modalErrorMessage"
      :training="selectedTraining"
      :date-time="
        selectedTraining &&
        formatDateTime(selectedTraining.trainingDate, selectedTraining.trainingTime)
      "
      @event-confirm-modal-closed="closeModals"
      @event-confirmed="registerToTraining"
    />
    <TrainingConfirmModal
      :confirm-modal-is-open="joinModalIsOpen"
      title="Treeninggrupiga liitumise taotlemine"
      confirm-button-text="Taotle"
      :error-message="modalErrorMessage"
      :training="selectedTraining"
      :date-time="
        selectedTraining &&
        formatDateTime(selectedTraining.trainingDate, selectedTraining.trainingTime)
      "
      @event-confirm-modal-closed="closeModals"
      @event-confirmed="applyToJoinTrainingGroup"
    />
  </div>
</template>

<style scoped>
.clickable-row {
  cursor: pointer;
}
</style>
