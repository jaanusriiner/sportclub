<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AlertSuccess from '@/components/alert/AlertSuccess.vue'
import BaseModal from '@/components/modal/BaseModal.vue'
import AreaService from '@/services/AreaService.js'
import FacilityService from '@/services/FacilityService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'

export default {
  name: 'ManageFacilitiesView',
  components: { AlertDanger, AlertSuccess, BaseModal },
  beforeMount() {
    this.getAreas()
    this.getFacilities()
  },
  data() {
    return {
      adminId: SessionStorageService.getUserId(),
      facilitiesAreLoading: true,
      areas: [],
      facilities: [],
      searchText: '',
      errorMessage: '',
      successMessage: '',
      deleteModalIsOpen: false,
      selectedFacility: null,
      isDeleting: false,
    }
  },
  computed: {
    filteredFacilities() {
      const searchText = this.searchText.trim().toLowerCase()
      return this.facilities.filter(
        (facility) =>
          searchText === '' ||
          facility.facilityName.toLowerCase().includes(searchText) ||
          facility.facilityAddress.toLowerCase().includes(searchText),
      )
    },
  },
  methods: {
    getAreas() {
      AreaService.getAreasRequest()
        .then((response) => (this.areas = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getFacilities() {
      FacilityService.getFacilitiesRequest()
        .then((response) => (this.facilities = response.data))
        .catch(() => NavigationService.navigateToErrorView())
        .finally(() => (this.facilitiesAreLoading = false))
    },

    getAreaName(areaId) {
      const area = this.areas.find((area) => area.areaId === areaId)
      return area ? area.areaName : ''
    },

    goToCreateFacility() {
      NavigationService.navigateToCreateFacilityView()
    },

    goToEditFacility(facility) {
      NavigationService.navigateToEditFacilityView(facility.facilityId)
    },

    openDeleteModal(facility) {
      this.resetMessages()
      this.selectedFacility = facility
      this.deleteModalIsOpen = true
    },

    closeDeleteModal() {
      this.deleteModalIsOpen = false
      this.selectedFacility = null
    },

    deleteFacility() {
      this.isDeleting = true
      const facilityName = this.selectedFacility.facilityName
      FacilityService.deleteFacilityRequest(this.selectedFacility.facilityId, this.adminId)
        .then(() => this.handleFacilityDeleted(facilityName))
        .catch((error) => this.handleDeleteError(error))
        .finally(() => {
          this.isDeleting = false
          this.closeDeleteModal()
        })
    },

    handleFacilityDeleted(facilityName) {
      this.successMessage = `Asukoht "${facilityName}" on kustutatud`
      this.getFacilities()
    },

    handleDeleteError(error) {
      if (error.response && [400, 403, 404].includes(error.response.status)) {
        this.errorMessage = error.response.data.message
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    resetMessages() {
      this.errorMessage = ''
      this.successMessage = ''
    },
  },
}
</script>

<template>
  <div class="container">
    <div class="row align-items-end mb-4">
      <div class="col">
        <p class="eyebrow">Admini vaade</p>
        <h1>Halda asukohti</h1>
      </div>
      <div class="col-auto">
        <button type="button" class="btn btn-primary" @click="goToCreateFacility">
          Lisa asukoht
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
          placeholder="Otsi nime või aadressi järgi..."
          aria-label="Otsi asukohta"
        />
      </div>
    </div>

    <div class="table-responsive rounded shadow-sm border">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-dark">
          <tr>
            <th>Nimi</th>
            <th>Aadress</th>
            <th>Maakond</th>
            <th class="text-end">Toimingud</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="facility in filteredFacilities" :key="facility.facilityId">
            <td class="sc-strong">{{ facility.facilityName }}</td>
            <td>{{ facility.facilityAddress }}</td>
            <td>{{ getAreaName(facility.areaId) }}</td>
            <td class="text-end text-nowrap">
              <button
                type="button"
                class="btn btn-sm btn-outline-primary me-2"
                @click="goToEditFacility(facility)"
              >
                Muuda
              </button>
              <button
                type="button"
                class="btn btn-sm btn-outline-danger"
                @click="openDeleteModal(facility)"
              >
                Kustuta
              </button>
            </td>
          </tr>
          <tr v-if="!facilitiesAreLoading && filteredFacilities.length === 0">
            <td colspan="4" class="text-center sc-sub py-4">Asukohti ei leitud</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-if="!facilitiesAreLoading" class="sc-sub mt-2">
      Kuvatud {{ filteredFacilities.length }} / {{ facilities.length }} asukohta
    </p>

    <BaseModal :is-open="deleteModalIsOpen" @event-modal-closed="closeDeleteModal">
      <template #title>Kinnitan kustutamise</template>
      <template #body>
        <p v-if="selectedFacility">
          Kas soovid kustutada asukoha <strong>{{ selectedFacility.facilityName }}</strong
          >? Treeningutes kasutatavat asukohta kustutada ei saa.
        </p>
      </template>
      <template #buttons>
        <button
          type="button"
          class="btn btn-danger me-2"
          :disabled="isDeleting"
          @click="deleteFacility"
        >
          Kustuta
        </button>
      </template>
    </BaseModal>
  </div>
</template>
