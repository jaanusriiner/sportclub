import axios from 'axios'

export default {
  getTrainingsRequest(params) {
    return axios.get('/api/trainings', { params })
  },

  putTrainingDateRequest(trainingDateId, updateRequest) {
    return axios.put(`/api/training-dates/${trainingDateId}`, updateRequest)
  },

  deleteTrainingDateRequest(trainingDateId) {
    return axios.delete(`/api/training-dates/${trainingDateId}`)
  },
}
