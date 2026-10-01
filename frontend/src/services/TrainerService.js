import axios from 'axios'

export default {
  getTrainerJoinApplicationsRequest(trainerId) {
    return axios.get(`/api/trainers/${trainerId}/join-applications`)
  },

  getTrainerSportclubsRequest(trainerId) {
    return axios.get(`/api/trainers/${trainerId}/sportclubs`)
  },

  getTrainerTrainingGroupsRequest(trainerId) {
    return axios.get(`/api/trainers/${trainerId}/training-groups`)
  },
}
