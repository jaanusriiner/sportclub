import axios from 'axios'

export default {
  postAskRequest(askRequest) {
    return axios.post('/api/ask', askRequest)
  },

  postAsk2Request(askRequest) {
    return axios.post('/api/ask2', askRequest)
  },

  // Benchmarki SQL-režiim: LLM koostab kataloogi põhjal ise SELECT-i
  postAsk3Request(askRequest) {
    return axios.post('/api/ask3', askRequest)
  },
}
