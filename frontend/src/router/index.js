import Vue from 'vue'
import Router from 'vue-router'
import Books from '@/views/Books'
import Users from '@/views/Users'
import Borrow from '@/views/Borrow'
import Statistics from '@/views/Statistics'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/books'
    },
    {
      path: '/books',
      name: 'Books',
      component: Books
    },
    {
      path: '/users',
      name: 'Users',
      component: Users
    },
    {
      path: '/borrow',
      name: 'Borrow',
      component: Borrow
    },
    {
      path: '/statistics',
      name: 'Statistics',
      component: Statistics
    }
  ]
})
