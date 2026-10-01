import axios from 'axios'

export default {
  getSportclubsRequest() {
    return axios.get('/api/sportclubs')
  },

  getSportclubTrainersRequest(sportclubId) {
    return axios.get(`/api/sportclubs/${sportclubId}/trainers`)
  },
}
