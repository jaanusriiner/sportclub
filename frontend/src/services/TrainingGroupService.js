import axios from 'axios'

export default {
  postJoinApplicationRequest(trainingGroupId, userId) {
    return axios.post(`/api/training-groups/${trainingGroupId}/join-applications`, { userId })
  },

  postTrainingGroupRequest(trainingGroupRequest) {
    return axios.post('/api/training-groups', trainingGroupRequest)
  },
}
