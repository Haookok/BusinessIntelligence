<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <h1>商务智能新闻分析系统</h1>
          <p>欢迎使用新闻数据分析平台</p>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-4">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>新闻统计</span>
            </div>
          </template>
          <div class="card-content">
            <!-- 这里可以添加新闻统计图表 -->
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户分析</span>
            </div>
          </template>
          <div class="card-content">
            <!-- 这里可以添加用户分析图表 -->
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>热门新闻</span>
            </div>
          </template>
          <div class="card-content">
            <!-- 这里可以添加热门新闻列表 -->
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useNewsStore } from '@/stores/news'

const newsStore = useNewsStore()

onMounted(() => {
  newsStore.fetchNewsList()
})
</script>

<style scoped>
.home {
  padding: 20px;
}

.welcome-card {
  text-align: center;
  padding: 40px;
}

.mt-4 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  min-height: 200px;
}
</style> 