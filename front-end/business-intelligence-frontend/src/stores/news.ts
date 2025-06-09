import { defineStore } from 'pinia'
import axios from 'axios'

interface NewsState {
  newsList: any[]
  currentNews: any
  loading: boolean
  error: string | null
}

export const useNewsStore = defineStore('news', {
  state: (): NewsState => ({
    newsList: [],
    currentNews: null,
    loading: false,
    error: null
  }),

  actions: {
    async fetchNewsList() {
      this.loading = true
      try {
        const response = await axios.get('/api/analysis/news/search')
        this.newsList = response.data
      } catch (error) {
        this.error = '获取新闻列表失败'
        console.error(error)
      } finally {
        this.loading = false
      }
    },

    async fetchNewsById(id: number) {
      this.loading = true
      try {
        const response = await axios.get(`/api/analysis/news/${id}/lifecycle`)
        this.currentNews = response.data
      } catch (error) {
        this.error = '获取新闻详情失败'
        console.error(error)
      } finally {
        this.loading = false
      }
    }
  }
}) 