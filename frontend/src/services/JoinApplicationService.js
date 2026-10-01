import axios from 'axios'

export default {
  putConfirmRequest(joinApplicationId) {
    return axios.put(`/api/join-applications/${joinApplicationId}/confirm`)
  },

  putRejectRequest(joinApplicationId) {
    return axios.put(`/api/join-applications/${joinApplicationId}/reject`)
  },
}
