import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import TestView from '@/views/TestView.vue'
import RegisterView from '@/views/RegisterView.vue'
import ErrorView from '@/views/ErrorView.vue'
import InfoView from '@/views/InfoView.vue'
import ManageTrainingsView from '@/views/ManageTrainingsView.vue'
import CreateTrainingGroupView from '@/views/CreateTrainingGroupView.vue'
import TrainingsView from '@/views/TrainingsView.vue'
import SessionStorageService from '@/services/SessionStorageService.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'homeRoute',
      component: HomeView,
    },
    {
      path: '/test',
      name: 'testRoute',
      component: TestView,
    },
    {
      path: '/register',
      name: 'registerRoute',
      component: RegisterView,
    },
    {
      path: '/error',
      name: 'errorRoute',
      component: ErrorView,
    },
    {
      path: '/info',
      name: 'infoRoute',
      component: InfoView,
    },
    {
      path: '/manage-trainings',
      name: 'manageTrainingsRoute',
      component: ManageTrainingsView,
      meta: { requiresAdminOrTrainer: true },
    },
    {
      path: '/create-training-group',
      name: 'createTrainingGroupRoute',
      component: CreateTrainingGroupView,
    },
    {
      path: '/trainings',
      name: 'trainingsRoute',
      component: TrainingsView,
    },
  ],
})

// lubab admin/trainer-only marsruudile ligi vaid sisse logitud admin või trainer rolliga kasutajal
router.beforeEach((to) => {
  if (!to.meta.requiresAdminOrTrainer) {
    return true
  }
  const isAllowed =
    SessionStorageService.userIsLoggedIn() &&
    (SessionStorageService.userIsAdmin() || SessionStorageService.userIsTrainer())
  return isAllowed ? true : { name: 'homeRoute' }
})

export default router
