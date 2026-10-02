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
  },
  data() {
    return {
      errorMessage: '',
      successMessage: '',
      areas: [],
      sports: [],
      // muutmine sunnib vormi (sh Tom Selecti spordialade valiku) uuesti looma
      formKey: 0,

      facilityRequest: this.createEmptyFacilityRequest(),
    }
  },
  methods: {
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

    createFacility() {
      this.errorMessage = ''
      this.successMessage = ''
      this.checkFormForErrors()
      if (this.errorMessage === '') {
        FacilityService.postFacilityRequest(this.facilityRequest)
          .then(() => this.handleCreateFacilityResponse())
          .catch((error) => this.handleCreateError(error))
      }
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
        <h1>Lisa uus asukoht</h1>
        <AlertDanger :error-message="errorMessage" />
        <AlertSuccess :success-message="successMessage" />
      </div>
    </div>
    <div class="row justify-content-center mb-4">
      <div :key="formKey" class="col-12 col-md-8 col-lg-6 card p-4">
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
          <SportsMultiSelect :sports="sports" @event-new-sports-selected="handleSportsSelected" />
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
          <label class="form-label" for="image">Pilt (JPEG või PNG, kuni 10 MB)</label>
          <input
            id="image"
            ref="imageInput"
            type="file"
            class="form-control image-input"
            accept="image/jpeg,image/png"
            @change="handleImageSelected"
          />
          <div v-if="facilityRequest.imageData" class="mt-3 text-center">
            <img
              :src="facilityRequest.imageData"
              alt="Asukoha pildi eelvaade"
              class="img-preview"
            />
            <div>
              <button type="button" class="btn btn-link btn-sm" @click="removeImage">
                Eemalda pilt
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-6 text-center">
        <button @click="goBack" class="btn btn-secondary action-button me-3" type="button">
          Tagasi
        </button>
        <button @click="createFacility" class="btn btn-primary action-button" type="submit">
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
