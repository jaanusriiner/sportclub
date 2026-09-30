import { createRouter, createWebHistory } from 'vue-router'
import HomeView from "@/views/HomeView.vue";
import TestView from "@/views/TestView.vue";
import RegisterView from '@/views/RegisterView.vue'
import ErrorView from '@/views/ErrorView.vue'
import InfoView from '@/views/InfoView.vue'
import ManageTrainingsView from '@/views/ManageTrainingsView.vue'


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
    },
    {
      path: '/training',
      name: 'trainingRoute',
      component: HomeView,
    },
  ],
})

export default router
