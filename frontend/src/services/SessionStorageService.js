export default {
  userIsLoggedIn() {
    return sessionStorage.getItem('userId') !== null
  },

  getUserId() {
    return Number(sessionStorage.getItem('userId'))
  },

  getUserFullName() {
    return sessionStorage.getItem('userFullName') || ''
  },

  userIsAdmin() {
    return sessionStorage.getItem('roleName') === 'admin'
  },

  userIsTrainer() {
    return sessionStorage.getItem('roleName') === 'trainer'
  },
}
