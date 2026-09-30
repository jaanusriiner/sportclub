import axios from 'axios'

export default {
  getTrainingsRequest(params) {
    return axios.get('/api/trainings', { params })
  },
}
