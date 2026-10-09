import axios from 'axios'

export default {
  getTonesRequest() {
    return axios.get('/api/tones')
  },
}
