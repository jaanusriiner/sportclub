import axios from 'axios'

export default {
  getSportsRequest() {
    return axios.get('/api/sports')
  },
}
