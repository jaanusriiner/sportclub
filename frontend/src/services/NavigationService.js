import router from '@/router/index.js'

export default {
  navigateToHomeView() {
    router.push({ name: 'homeRoute' })
  },

  navigateToRegisterView() {
    router.push({ name: 'registerRoute' })
  },

  navigateToErrorView() {
    router.push({ name: 'errorRoute' })
  },

  navigateToInfoView() {
    router.push({ name: 'infoRoute' })
  },

  navigateToManageTrainingsView() {
    router.push({ name: 'manageTrainingsRoute' })
  },

  navigateToCreateTrainingGroupView() {
    router.push({ name: 'createTrainingGroupRoute' })
  },
}
