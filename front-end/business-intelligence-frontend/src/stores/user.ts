import { defineStore } from 'pinia'
import axios from 'axios'

interface UserState {
  userId: number | null
  userInterests: any[]
  loading: boolean
  error: string | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    userId: null,
    userInterests: [],
    loading: false,
    error: null
  }),

  actions: {
    async fetchUserInterests(userId: number) {
      this.loading = true
      try {
        const response = await axios.get(`/api/analysis/user/${userId}/interests`)
        this.userInterests = response.data
        this.userId = userId
      } catch (error) {
        this.error = '获取用户兴趣数据失败'
        console.error(error)
      } finally {
        this.loading = false
      }
    }
  }
}) 