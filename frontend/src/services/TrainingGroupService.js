import axios from 'axios'

export default {
  postJoinApplicationRequest(trainingGroupId, userId) {
    return axios.post(`/api/training-groups/${trainingGroupId}/join-applications`, { userId })
  },

  postTrainingGroupRequest(trainingGroupRequest) {
    return axios.post('/api/training-groups', trainingGroupRequest)
  },

  getTrainingGroupsRequest(userId) {
    return axios.get('/api/training-groups', { params: { userId } })
  },

  deleteTrainingGroupRequest(trainingGroupId, userId) {
    return axios.delete(`/api/training-groups/${trainingGroupId}`, { params: { userId } })
  },
}
