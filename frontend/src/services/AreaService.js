import axios from 'axios'

export default {
  getAreasRequest() {
    return axios.get('/api/areas')
  },
}
