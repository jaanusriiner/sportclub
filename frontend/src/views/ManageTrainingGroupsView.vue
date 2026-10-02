<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AlertSuccess from '@/components/alert/AlertSuccess.vue'
import BaseModal from '@/components/modal/BaseModal.vue'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import TrainingGroupService from '@/services/TrainingGroupService.js'
import { PhTrash } from '@phosphor-icons/vue'

export default {
  name: 'ManageTrainingGroupsView',
  components: { AlertDanger, AlertSuccess, BaseModal, PhTrash },
  beforeMount() {
    this.getTrainingGroups()
  },
  data() {
    return {
      isAdmin: SessionStorageService.userIsAdmin(),
      trainingGroupsAreLoading: true,
      trainingGroups: [],
      searchText: '',
      errorMessage: '',
      successMessage: '',
      deleteModalIsOpen: false,
      selectedTrainingGroup: null,
      isDeleting: false,
    }
  },
  computed: {
    filteredTrainingGroups() {
      const searchText = this.searchText.trim().toLowerCase()
      return this.trainingGroups.filter(
        (trainingGroup) =>
          searchText === '' ||
          [trainingGroup.trainingGroupName, trainingGroup.sportclubName, trainingGroup.trainerName]
            .join(' ')
            .toLowerCase()
            .includes(searchText),
      )
    },
  },
  methods: {
    getTrainingGroups() {
      TrainingGroupService.getTrainingGroupsRequest(SessionStorageService.getUserId())
        .then((response) => (this.trainingGroups = response.data))
        .catch(() => NavigationService.navigateToErrorView())
        .finally(() => (this.trainingGroupsAreLoading = false))
    },

    openDeleteModal(trainingGroup) {
      this.errorMessage = ''
      this.selectedTrainingGroup = trainingGroup
      this.deleteModalIsOpen = true
    },

    closeDeleteModal() {
      this.deleteModalIsOpen = false
      this.selectedTrainingGroup = null
    },

    deleteTrainingGroup() {
      this.isDeleting = true
      const trainingGroupName = this.selectedTrainingGroup.trainingGroupName
      TrainingGroupService.deleteTrainingGroupRequest(
        this.selectedTrainingGroup.trainingGroupId,
        SessionStorageService.getUserId(),
      )
        .then(() => this.handleTrainingGroupDeleted(trainingGroupName))
        .catch((error) => this.handleDeleteError(error))
        .finally(() => (this.isDeleting = false))
    },

    handleTrainingGroupDeleted(trainingGroupName) {
      this.closeDeleteModal()
      this.successMessage = `Treeninggrupp "${trainingGroupName}" on kustutatud`
      this.getTrainingGroups()
    },

    handleDeleteError(error) {
      this.closeDeleteModal()
      if (error.response && [403, 404].includes(error.response.status)) {
        this.errorMessage = error.response.data.message
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    goToCreateTrainingGroup() {
      NavigationService.navigateToCreateTrainingGroupView()
    },
  },
}
</script>

<template>
  <div class="container">
    <div class="row align-items-end mb-4">
      <div class="col">
        <p class="eyebrow">{{ isAdmin ? 'Admini vaade' : 'Treeneri vaade' }}</p>
        <h1>Halda treeninggruppe</h1>
      </div>
      <div class="col-auto">
        <button type="button" class="btn btn-primary" @click="goToCreateTrainingGroup">
          Lisa treeninggrupp
        </button>
      </div>
    </div>
    <AlertDanger :error-message="errorMessage" />
    <AlertSuccess :success-message="successMessage" />

    <div class="row g-2 mb-3">
      <div class="col-12 col-md-auto">
        <input
          v-model="searchText"
          type="search"
          class="form-control"
          placeholder="Otsi grupi, klubi või treeneri järgi..."
          aria-label="Otsi treeninggruppi"
        />
      </div>
    </div>

    <div class="table-responsive rounded shadow-sm border">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-dark">
          <tr>
            <th>Treeninggrupp</th>
            <th>Spordiklubi</th>
            <th>Spordiala / tase</th>
            <th v-if="isAdmin">Treener</th>
            <th class="text-center">Liikmeid</th>
            <th class="text-center">Treeninguid</th>
            <th class="text-end">Toimingud</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="trainingGroup in filteredTrainingGroups" :key="trainingGroup.trainingGroupId">
            <td class="sc-strong">{{ trainingGroup.trainingGroupName }}</td>
            <td>{{ trainingGroup.sportclubName }}</td>
            <td>{{ trainingGroup.sportName }} - {{ trainingGroup.skillLevelName }}</td>
            <td v-if="isAdmin">{{ trainingGroup.trainerName }}</td>
            <td class="text-center">{{ trainingGroup.memberCount }}</td>
            <td class="text-center">{{ trainingGroup.trainingCount }}</td>
            <td class="text-end">
              <button
                type="button"
                class="icon-btn icon-btn-danger"
                title="Kustuta treeninggrupp"
                aria-label="Kustuta treeninggrupp"
                @click="openDeleteModal(trainingGroup)"
              >
                <PhTrash :size="18" />
              </button>
            </td>
          </tr>
          <tr v-if="!trainingGroupsAreLoading && filteredTrainingGroups.length === 0">
            <td :colspan="isAdmin ? 7 : 6" class="text-center sc-sub py-4">
              Treeninggruppe ei leitud
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <BaseModal :is-open="deleteModalIsOpen" @event-modal-closed="closeDeleteModal">
      <template #title>Kustuta treeninggrupp</template>
      <template #body>
        <div v-if="selectedTrainingGroup">
          <p>
            Kas oled kindel, et soovid kustutada treeninggrupi
            <strong>{{ selectedTrainingGroup.trainingGroupName }}</strong>
            ({{ selectedTrainingGroup.sportclubName }})?
          </p>
          <p class="mb-1">Koos grupiga kustutatakse jäädavalt:</p>
          <ul>
            <li>
              {{ selectedTrainingGroup.trainingCount }} treeningut koos kõigi treeningkordadega
            </li>
            <li>kõik registreerumised nendele treeningutele</li>
            <li>{{ selectedTrainingGroup.memberCount }} liikme liikmesus grupis</li>
            <li>grupi liitumistaotlused</li>
          </ul>
          <p class="sc-sub mb-0">Kasutajaid ja treenereid ei kustutata.</p>
        </div>
      </template>
      <template #buttons>
        <button
          type="button"
          class="btn btn-danger me-2"
          :disabled="isDeleting"
          @click="deleteTrainingGroup"
        >
          Kustuta treeninggrupp
        </button>
      </template>
    </BaseModal>
  </div>
</template>
