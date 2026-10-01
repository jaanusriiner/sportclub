import axios from 'axios'

export default {
  getFacilitiesRequest() {
    return axios.get('/api/facilities')
  },
}
