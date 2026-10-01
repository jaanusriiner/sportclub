import axios from 'axios'

export default {
  postTrainingGroupRequest(trainingGroupRequest) {
    return axios.post('/api/training-groups', trainingGroupRequest)
  },
}
