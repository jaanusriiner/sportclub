import axios from 'axios'

export default {
  postAskRequest(askRequest) {
    return axios.post('/api/ask', askRequest)
  },
}
