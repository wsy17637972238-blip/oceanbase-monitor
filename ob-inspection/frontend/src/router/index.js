import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import InspectionView from '../views/InspectionView.vue'
import AlertView from '../views/AlertView.vue'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: DashboardView },
  { path: '/inspection', component: InspectionView },
  { path: '/alerts', component: AlertView }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
