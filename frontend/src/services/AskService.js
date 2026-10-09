import axios from 'axios'

export default {
  postAskRequest(askRequest) {
    return axios.post('/api/ask', askRequest)
  },

  postAsk2Request(askRequest) {
    return axios.post('/api/ask2', askRequest)
  },
}
