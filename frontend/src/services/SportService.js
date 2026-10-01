import axios from 'axios'

export default {
  getSportsRequest() {
    return axios.get('/api/sports')
  },

  getSkillLevelsRequest(sportId) {
    return axios.get(`/api/sports/${sportId}/skill-levels`)
  },
}
