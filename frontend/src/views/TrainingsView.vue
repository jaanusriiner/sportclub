<script>
import AlertSuccess from '@/components/alert/AlertSuccess.vue'
import AreasDropDown from '@/components/dropdown/AreasDropDown.vue'
import SportsDropDown from '@/components/dropdown/SportsDropDown.vue'
import TrainingConfirmModal from '@/components/modal/TrainingConfirmModal.vue'
import TrainingInfoModal from '@/components/modal/TrainingInfoModal.vue'
import AreaService from '@/services/AreaService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import SportService from '@/services/SportService.js'
import TrainingGroupService from '@/services/TrainingGroupService.js'
import TrainingService from '@/services/TrainingService.js'
import UserService from '@/services/UserService.js'
import { PhInfo } from '@phosphor-icons/vue'

export default {
  name: 'TrainingsView',
  components: {
    AlertSuccess,
    AreasDropDown,
    SportsDropDown,
    TrainingConfirmModal,
    TrainingInfoModal,
    PhInfo,
  },
  inject: ['openLoginModal'],
  beforeMount() {
    this.getMyTrainings()
    this.getAreas()
    this.getSports()
    this.getTrainings()
  },
  data() {
    return {
      trainings: [],
      myTrainings: [],
      areas: [],
      sports: [],
      selectedAreaId: 0,
      selectedSportId: 0,
      isLoggedIn: SessionStorageService.userIsLoggedIn(),
      successMessage: '',
      modalErrorMessage: '',
      selectedTraining: null,
      infoModalIsOpen: false,
      registerModalIsOpen: false,
      joinModalIsOpen: false,
      unregisterModalIsOpen: false,
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

    getAreas() {
      AreaService.getAreasRequest()
        .then((response) => (this.areas = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getSports() {
      SportService.getSportsRequest()
        .then((response) => (this.sports = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleAreaSelected(areaId) {
      this.selectedAreaId = areaId
      this.getTrainings()
    },

    handleSportSelected(sportId) {
      this.selectedSportId = sportId
      this.getTrainings()
    },

    getTrainings() {
      TrainingService.getTrainingsRequest({
        requestUserId: SessionStorageService.getUserId(),
        areaId: this.selectedAreaId,
        sportId: this.selectedSportId,
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

    openUnregisterModal(myTraining) {
      this.selectedTraining = {
        ...myTraining,
        trainingDate: myTraining.nextTrainingDate,
        trainingTime: myTraining.nextTrainingTime,
      }
      this.modalErrorMessage = ''
      this.unregisterModalIsOpen = true
    },

    closeModals() {
      this.infoModalIsOpen = false
      this.registerModalIsOpen = false
      this.joinModalIsOpen = false
      this.unregisterModalIsOpen = false
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

    unregisterFromTraining() {
      this.successMessage = ''
      TrainingService.deleteRegisterFromTrainingRequest(
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

    // täis -> hall, vähemalt 75% täis -> kollakas, muidu roheline
    capacityClass(userCount, maxSize) {
      if (userCount >= maxSize) {
        return 'fw-bold text-secondary'
      }
      if (userCount / maxSize >= 0.75) {
        return 'fw-bold text-warning-emphasis'
      }
      return 'fw-bold text-success'
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
        <h1 class="text-center">Treeninggrupid ja treeningud</h1>
        <AlertSuccess :success-message="successMessage" />
      </div>
    </div>
    <template v-if="isLoggedIn">
      <div class="row mt-5 mb-2">
        <div class="col">
          <h2 class="h4">Minu treeningud</h2>
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
                  <td :class="capacityClass(myTraining.userCount, myTraining.maxSize)">
                    {{ myTraining.userCount }}/{{ myTraining.maxSize }}
                  </td>
                  <td>
                    <button
                      type="button"
                      class="btn btn-outline-danger btn-sm text-nowrap"
                      @click.stop="openUnregisterModal(myTraining)"
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
    <div class="row mb-2">
      <div class="col">
        <h2 class="h4">Kõik treeningud</h2>
      </div>
    </div>
    <div class="row mb-3">
      <div class="col-3">
        <AreasDropDown
          :areas="areas"
          :area-id="selectedAreaId"
          all-label="Kõik piirkonnad"
          @event-new-area-selected="handleAreaSelected"
        />
      </div>
      <div class="col-3">
        <SportsDropDown
          :sports="sports"
          :sport-id="selectedSportId"
          @event-new-sport-selected="handleSportSelected"
        />
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
                    <div :class="capacityClass(training.userCount, training.maxSize)">
                      {{ training.userCount }}/{{ training.maxSize }}
                    </div>
                    <span v-if="training.userIsRegistered" class="fw-bold text-success mt-1"
                      >Registreeritud</span
                    >
                    <span
                      v-else-if="training.userCount >= training.maxSize"
                      class="fw-bold text-secondary mt-1"
                      >Kohad on täis</span
                    >
                    <button
                      v-else
                      type="button"
                      class="btn btn-success btn-sm text-nowrap mt-1"
                      @click.stop="openRegisterModal(training)"
                    >
                      Registreeru
                    </button>
                  </template>
                  <span
                    v-else-if="training.userHasPendingJoinApplication"
                    class="fw-bold text-warning-emphasis"
                    >Taotlus edastatud</span
                  >
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
    <TrainingConfirmModal
      :confirm-modal-is-open="unregisterModalIsOpen"
      title="Koha vabastamine"
      confirm-button-text="Vabasta koht"
      :error-message="modalErrorMessage"
      :training="selectedTraining"
      :date-time="
        selectedTraining &&
        formatDateTime(selectedTraining.trainingDate, selectedTraining.trainingTime)
      "
      @event-confirm-modal-closed="closeModals"
      @event-confirmed="unregisterFromTraining"
    />
  </div>
</template>

<style scoped>
.clickable-row {
  cursor: pointer;
}
</style>
