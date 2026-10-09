import axios from 'axios'

export default {
  getLlmModelsRequest() {
    return axios.get('/api/llm-models')
  },
}
