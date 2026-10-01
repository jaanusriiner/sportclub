import axios from 'axios'

export default {
  getUserTrainingsRequest(userId) {
    return axios.get(`/api/users/${userId}/trainings`)
  },

  postRegisterRequest(registerRequest) {
    return axios.post('/api/register', registerRequest)
  },

  getUsersRequest(adminId) {
    return axios.get('/api/users', { params: { adminId } })
  },

  putUserRequest(userId, updateUserRequest) {
    return axios.put(`/api/users/${userId}`, updateUserRequest)
  },

  putUserSportclubsRequest(userId, updateUserSportclubsRequest) {
    return axios.put(`/api/users/${userId}/sportclubs`, updateUserSportclubsRequest)
  },
}
