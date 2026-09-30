import axios from 'axios'

export default {
  getTrainerTrainingGroupsRequest(trainerId) {
    return axios.get(`/api/trainers/${trainerId}/training-groups`)
  },
}
