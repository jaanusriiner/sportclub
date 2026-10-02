<script>
import AreasDropDown from '@/components/dropdown/AreasDropDown.vue'
import FacilityInfoModal from '@/components/modal/FacilityInfoModal.vue'
import AreaService from '@/services/AreaService.js'
import FacilityService from '@/services/FacilityService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'

export default {
  name: 'FacilitiesView',
  components: { AreasDropDown, FacilityInfoModal },
  beforeMount() {
    this.getAreas()
    this.getFacilities()
  },
  data() {
    return {
      isAdmin: SessionStorageService.userIsAdmin(),
      facilitiesAreLoading: true,
      areas: [],
      facilities: [],
      selectedAreaId: 0,
      searchText: '',
      infoModalIsOpen: false,
      selectedFacility: null,
      selectedFacilityImageData: '',
      imageIsLoading: false,
    }
  },
  computed: {
    filteredFacilities() {
      const searchText = this.searchText.trim().toLowerCase()
      return this.facilities
        .filter((facility) => this.selectedAreaId === 0 || facility.areaId === this.selectedAreaId)
        .filter(
          (facility) =>
            searchText === '' ||
            facility.facilityName.toLowerCase().includes(searchText) ||
            facility.facilityAddress.toLowerCase().includes(searchText),
        )
    },

    hasActiveFilters() {
      return this.selectedAreaId !== 0 || this.searchText !== ''
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

    handleAreaSelected(areaId) {
      this.selectedAreaId = areaId
    },

    clearFilters() {
      this.selectedAreaId = 0
      this.searchText = ''
    },

    openFacilityInfoModal(facility) {
      this.selectedFacility = facility
      this.selectedFacilityImageData = ''
      this.imageIsLoading = true
      this.infoModalIsOpen = true
      FacilityService.getFacilityImageRequest(facility.facilityId)
        .then((response) => this.handleFacilityImageResponse(facility, response.data.imageData))
        .catch(() => NavigationService.navigateToErrorView())
        .finally(() => (this.imageIsLoading = false))
    },

    handleFacilityImageResponse(facility, imageData) {
      // kasutaja võis vahepeal teise asukoha avada - vana vastus ei tohi uut pilti üle kirjutada
      if (this.selectedFacility === facility) {
        this.selectedFacilityImageData = imageData
      }
    },

    closeFacilityInfoModal() {
      this.infoModalIsOpen = false
      this.selectedFacility = null
    },

    goToCreateFacility() {
      NavigationService.navigateToCreateFacilityView()
    },
  },
}
</script>

<template>
  <div class="container">
    <div class="row align-items-end mb-4">
      <div class="col">
        <h1>Asukohad</h1>
        <p class="sc-sub mb-0">Kõik treeningute toimumiskohad</p>
      </div>
      <div v-if="isAdmin" class="col-auto">
        <button type="button" class="btn btn-primary" @click="goToCreateFacility">
          Lisa asukoht
        </button>
      </div>
    </div>

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
      <div class="col-12 col-md-auto">
        <AreasDropDown
          :area-id="selectedAreaId"
          :areas="areas"
          all-label="Kõik maakonnad"
          show-separator
          @event-new-area-selected="handleAreaSelected"
        />
      </div>
      <div class="col-auto">
        <button
          type="button"
          class="btn btn-outline-secondary"
          :disabled="!hasActiveFilters"
          @click="clearFilters"
        >
          Kustuta filtrid
        </button>
      </div>
    </div>

    <div class="row">
      <div class="col">
        <div class="table-responsive rounded shadow-sm border">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
              <tr>
                <th>Nimi</th>
                <th>Aadress</th>
                <th>Maakond</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="facility in filteredFacilities"
                :key="facility.facilityId"
                class="facility-row"
                title="Vaata asukoha infot"
                @click="openFacilityInfoModal(facility)"
              >
                <td class="sc-strong">{{ facility.facilityName }}</td>
                <td>{{ facility.facilityAddress }}</td>
                <td>{{ getAreaName(facility.areaId) }}</td>
              </tr>
              <tr v-if="!facilitiesAreLoading && filteredFacilities.length === 0">
                <td colspan="3" class="text-center sc-sub py-4">
                  {{
                    hasActiveFilters
                      ? 'Valitud filtritele vastavaid asukohti ei leitud'
                      : 'Asukohti pole veel lisatud'
                  }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-if="!facilitiesAreLoading" class="sc-sub mt-2">
          Kuvatud {{ filteredFacilities.length }} / {{ facilities.length }} asukohta
        </p>
      </div>
    </div>

    <FacilityInfoModal
      :info-modal-is-open="infoModalIsOpen"
      :facility="selectedFacility"
      :area-name="selectedFacility ? getAreaName(selectedFacility.areaId) : ''"
      :image-data="selectedFacilityImageData"
      :image-is-loading="imageIsLoading"
      @event-info-modal-closed="closeFacilityInfoModal"
    />
  </div>
</template>

<style scoped>
.facility-row {
  cursor: pointer;
}
</style>
