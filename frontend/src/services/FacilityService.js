import axios from 'axios'

export default {
  getFacilitiesRequest() {
    return axios.get('/api/facilities')
  },

  postFacilityRequest(facilityRequest) {
    return axios.post('/api/facilities', facilityRequest)
  },
}
