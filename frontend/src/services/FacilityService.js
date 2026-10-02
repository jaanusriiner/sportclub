import axios from 'axios'

export default {
  getFacilitiesRequest() {
    return axios.get('/api/facilities')
  },

  getFacilityRequest(facilityId) {
    return axios.get(`/api/facilities/${facilityId}`)
  },

  getFacilityImageRequest(facilityId) {
    return axios.get(`/api/facilities/${facilityId}/image`)
  },

  postFacilityRequest(facilityRequest) {
    return axios.post('/api/facilities', facilityRequest)
  },

  putFacilityRequest(facilityId, facilityRequest) {
    return axios.put(`/api/facilities/${facilityId}`, facilityRequest)
  },

  deleteFacilityRequest(facilityId, adminId) {
    return axios.delete(`/api/facilities/${facilityId}`, { params: { adminId } })
  },
}
