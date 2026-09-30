export default {
  userIsLoggedIn() {
    return sessionStorage.getItem('userId') !== null
  },

  getUserId() {
    return Number(sessionStorage.getItem('userId'))
  },

  userIsAdmin() {
    return sessionStorage.getItem('roleName') === 'admin'
  },

  userIsTrainer() {
    return sessionStorage.getItem('roleName') === 'trainer'
  },
}
