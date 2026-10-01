import axios from 'axios'

export default {
  getUserTrainingsRequest(userId) {
    return axios.get(`/api/users/${userId}/trainings`)
  },

  postRegisterRequest(registerRequest) {
    return axios.post('/api/register', registerRequest)
  },
}
