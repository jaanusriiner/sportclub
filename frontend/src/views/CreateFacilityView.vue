<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AlertSuccess from '@/components/alert/AlertSuccess.vue'
import AreasDropDown from '@/components/dropdown/AreasDropDown.vue'
import SportsMultiSelect from '@/components/multiselect/SportsMultiSelect.vue'
import AreaService from '@/services/AreaService.js'
import FacilityService from '@/services/FacilityService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import SportService from '@/services/SportService.js'

const MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png']

export default {
  name: 'CreateFacilityView',
  components: { AlertDanger, AlertSuccess, AreasDropDown, SportsMultiSelect },
  beforeMount() {
    this.getAreas()
    this.getSports()
    this.initializeForm()
  },
  watch: {
    // "Lisa asukoht" ja "Muuda asukohta" kasutavad sama komponenti - marsruudi vahetusel
    // komponenti uuesti ei looda, seega tuleb vorm ise lähtestada
    facilityId() {
      this.initializeForm()
    },
  },
  data() {
    return {
      errorMessage: '',
      successMessage: '',
      areas: [],
      sports: [],
      // muutmine sunnib vormi (sh Tom Selecti spordialade valiku) uuesti looma
      formKey: 0,
      facilityIsLoaded: false,
      currentImageData: '',

      facilityRequest: this.createEmptyFacilityRequest(),
    }
  },
  computed: {
    facilityId() {
      const facilityId = this.$route.params.facilityId
      return facilityId ? Number(facilityId) : null
    },

    isEditMode() {
      return this.facilityId !== null
    },

    previewImageData() {
      return this.facilityRequest.imageData || this.currentImageData
    },
  },
  methods: {
    initializeForm() {
      this.errorMessage = ''
      this.successMessage = ''
      this.facilityRequest = this.createEmptyFacilityRequest()
      this.currentImageData = ''
      this.facilityIsLoaded = false
      this.formKey++
      if (this.isEditMode) {
        this.getFacility()
        this.getFacilityImage()
      }
    },

    getFacility() {
      FacilityService.getFacilityRequest(this.facilityId)
        .then((response) => this.handleGetFacilityResponse(response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    handleGetFacilityResponse(facilityDetail) {
      this.facilityRequest.areaId = facilityDetail.areaId
      this.facilityRequest.facilityName = facilityDetail.facilityName
      this.facilityRequest.address = facilityDetail.address
      this.facilityRequest.description = facilityDetail.description || ''
      this.facilityRequest.sportIds = facilityDetail.sportIds
      this.facilityIsLoaded = true
    },

    getFacilityImage() {
      FacilityService.getFacilityImageRequest(this.facilityId)
        .then((response) => (this.currentImageData = response.data.imageData))
        .catch(() => NavigationService.navigateToErrorView())
    },

    createEmptyFacilityRequest() {
      return {
        adminId: SessionStorageService.getUserId(),
        areaId: 0,
        facilityName: '',
        address: '',
        description: '',
        sportIds: [],
        imageData: '',
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
      this.facilityRequest.areaId = areaId
    },

    handleSportsSelected(sportIds) {
      this.facilityRequest.sportIds = sportIds
    },

    handleImageSelected(event) {
      this.errorMessage = ''
      this.facilityRequest.imageData = ''
      const file = event.target.files[0]
      if (!file) {
        return
      }
      if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
        this.errorMessage = 'Lubatud on ainult JPEG ja PNG pildid'
        event.target.value = ''
      } else if (file.size > MAX_IMAGE_SIZE_BYTES) {
        this.errorMessage = 'Pildi maksimaalne suurus on 10 MB'
        event.target.value = ''
      } else {
        const fileReader = new FileReader()
        fileReader.onload = () => (this.facilityRequest.imageData = fileReader.result)
        fileReader.readAsDataURL(file)
      }
    },

    removeImage() {
      this.facilityRequest.imageData = ''
      this.$refs.imageInput.value = ''
    },

    saveFacility() {
      this.errorMessage = ''
      this.successMessage = ''
      this.checkFormForErrors()
      if (this.errorMessage === '') {
        if (this.isEditMode) {
          this.updateFacility()
        } else {
          this.createFacility()
        }
      }
    },

    createFacility() {
      FacilityService.postFacilityRequest(this.facilityRequest)
        .then(() => this.handleCreateFacilityResponse())
        .catch((error) => this.handleCreateError(error))
    },

    updateFacility() {
      FacilityService.putFacilityRequest(this.facilityId, this.facilityRequest)
        .then(() => this.handleUpdateFacilityResponse())
        .catch((error) => this.handleCreateError(error))
    },

    checkFormForErrors() {
      if (this.facilityRequest.facilityName.trim() === '') {
        this.errorMessage = 'Sisesta asukoha nimi'
      } else if (this.facilityRequest.address.trim() === '') {
        this.errorMessage = 'Sisesta aadress'
      } else if (this.facilityRequest.areaId === 0) {
        this.errorMessage = 'Vali maakond'
      } else if (this.facilityRequest.sportIds.length < 1) {
        this.errorMessage = 'Vali vähemalt üks spordiala'
      }
    },

    handleCreateFacilityResponse() {
      this.successMessage = `Asukoht "${this.facilityRequest.facilityName}" on lisatud`
      this.facilityRequest = this.createEmptyFacilityRequest()
      this.formKey++
    },

    handleUpdateFacilityResponse() {
      this.successMessage = `Asukoha "${this.facilityRequest.facilityName}" muudatused on salvestatud`
      if (this.facilityRequest.imageData) {
        this.currentImageData = this.facilityRequest.imageData
        this.removeImage()
      }
    },

    handleCreateError(error) {
      if (error.response && [400, 403, 404].includes(error.response.status)) {
        this.errorMessage = error.response.data.message
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    goBack() {
      this.$router.back()
    },
  },
}
</script>

<template>
  <div class="container">
    <div class="row justify-content-center mb-4">
      <div class="col-12 col-md-8 col-lg-6">
        <p v-if="isEditMode" class="eyebrow">Admini vaade</p>
        <h1>{{ isEditMode ? 'Muuda asukohta' : 'Lisa uus asukoht' }}</h1>
        <AlertDanger :error-message="errorMessage" />
        <AlertSuccess :success-message="successMessage" />
      </div>
    </div>
    <div class="row justify-content-center mb-4">
      <div
        v-if="isEditMode && !facilityIsLoaded"
        class="col-12 col-md-8 col-lg-6 text-center sc-sub"
      >
        Asukoha andmete laadimine...
      </div>
      <div v-else :key="formKey" class="col-12 col-md-8 col-lg-6 card p-4">
        <div class="mb-3">
          <label class="form-label" for="facilityName">Asukoha nimi</label>
          <input
            id="facilityName"
            v-model="facilityRequest.facilityName"
            type="text"
            class="form-control"
            maxlength="60"
          />
        </div>

        <div class="mb-3">
          <label class="form-label" for="address">Aadress</label>
          <input
            id="address"
            v-model="facilityRequest.address"
            type="text"
            class="form-control"
            maxlength="90"
          />
        </div>

        <div class="mb-3">
          <label class="form-label">Maakond</label>
          <AreasDropDown
            :area-id="facilityRequest.areaId"
            :areas="areas"
            all-label="Vali maakond..."
            @event-new-area-selected="handleAreaSelected"
          />
        </div>

        <div class="mb-3">
          <label class="form-label" for="sports">Spordialad</label>
          <SportsMultiSelect
            :sports="sports"
            :selected-sport-ids="facilityRequest.sportIds"
            @event-new-sports-selected="handleSportsSelected"
          />
        </div>

        <div class="mb-3">
          <label class="form-label" for="description">Kirjeldus</label>
          <textarea
            id="description"
            v-model="facilityRequest.description"
            class="form-control"
            rows="3"
            maxlength="255"
          ></textarea>
        </div>

        <div class="mb-3">
          <label class="form-label" for="image">
            {{ isEditMode ? 'Uus pilt' : 'Pilt' }} (JPEG või PNG, kuni 10 MB)
          </label>
          <input
            id="image"
            ref="imageInput"
            type="file"
            class="form-control image-input"
            accept="image/jpeg,image/png"
            @change="handleImageSelected"
          />
          <div v-if="previewImageData" class="mt-3 text-center">
            <img :src="previewImageData" alt="Asukoha pildi eelvaade" class="img-preview" />
            <div v-if="facilityRequest.imageData">
              <button type="button" class="btn btn-link btn-sm" @click="removeImage">
                {{ isEditMode ? 'Loobu uuest pildist' : 'Eemalda pilt' }}
              </button>
            </div>
            <div v-else class="sc-sub small mt-1">Praegune pilt</div>
          </div>
        </div>
      </div>
    </div>
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-6 text-center">
        <button @click="goBack" class="btn btn-secondary action-button me-3" type="button">
          Tagasi
        </button>
        <button @click="saveFacility" class="btn btn-primary action-button" type="submit">
          Salvesta
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Tagasi ja Salvesta nupud ühelaiused */
.action-button {
  min-width: 140px;
}

/* Bootstrapi failivalija nupp on joondatud vaikimisi kõrgusele ja polsterdusele, aga nordic.css
   teeb väljad kõrgemaks (44px) - joondame "Choose File" nupu ja failinime välja keskele */
.image-input {
  padding-top: 0;
  padding-bottom: 0;
  line-height: 42px;
}
.image-input::file-selector-button {
  height: 42px;
  padding: 0 14px;
  margin: 0 14px 0 -14px;
}

.img-preview {
  max-width: 100%;
  max-height: 240px;
  border-radius: 0.5rem;
}
</style>
