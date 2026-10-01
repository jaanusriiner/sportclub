import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import TestView from '@/views/TestView.vue'
import RegisterView from '@/views/RegisterView.vue'
import ErrorView from '@/views/ErrorView.vue'
import InfoView from '@/views/InfoView.vue'
import ManageTrainingsView from '@/views/ManageTrainingsView.vue'
import CreateTrainingGroupView from '@/views/CreateTrainingGroupView.vue'
import CreateTrainingView from '@/views/CreateTrainingView.vue'
import TrainingsView from '@/views/TrainingsView.vue'
import CreateFacilityView from '@/views/CreateFacilityView.vue'
import FacilitiesView from '@/views/FacilitiesView.vue'
import ManageTrainingGroupsView from '@/views/ManageTrainingGroupsView.vue'
import ManageUsersView from '@/views/ManageUsersView.vue'
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
      meta: { requiresAdminOrTrainer: true },
    },
    {
      path: '/create-training',
      name: 'createTrainingRoute',
      component: CreateTrainingView,
      meta: { requiresAdminOrTrainer: true },
    },
    {
      path: '/trainings',
      name: 'trainingsRoute',
      component: TrainingsView,
    },
    {
      path: '/manage-training-groups',
      name: 'manageTrainingGroupsRoute',
      component: ManageTrainingGroupsView,
      meta: { requiresAdminOrTrainer: true },
    },
    {
      path: '/manage-users',
      name: 'manageUsersRoute',
      component: ManageUsersView,
      meta: { requiresAdmin: true },
    },
    {
      path: '/facilities',
      name: 'facilitiesRoute',
      component: FacilitiesView,
    },
    {
      path: '/create-facility',
      name: 'createFacilityRoute',
      component: CreateFacilityView,
      meta: { requiresAdmin: true },
    },
  ],
})

// lubab admin/trainer-only marsruudile ligi vaid sisse logitud admin või trainer rolliga kasutajal
router.beforeEach((to) => {
  // admin-only marsruudile pääseb ligi vaid sisse logitud admin
  if (to.meta.requiresAdmin) {
    const isAdmin = SessionStorageService.userIsLoggedIn() && SessionStorageService.userIsAdmin()
    return isAdmin ? true : { name: 'homeRoute' }
  }
  if (!to.meta.requiresAdminOrTrainer) {
    return true
  }
  const isAllowed =
    SessionStorageService.userIsLoggedIn() &&
    (SessionStorageService.userIsAdmin() || SessionStorageService.userIsTrainer())
  return isAllowed ? true : { name: 'homeRoute' }
})

export default router
