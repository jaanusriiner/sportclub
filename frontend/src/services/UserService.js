import axios from 'axios'

export default {
  postRegisterRequest(registerRequest) {
    return axios.post('/api/register', registerRequest)
  },
}
