<template>
  <div class="home-container">
    <div class="hero-section">
      <div class="hero-content">
        <h1>校园综合生活服务平台</h1>
        <p>一站式校园生活服务，让校园生活更精彩</p>
        <div class="hero-buttons">
          <el-button type="primary" size="large" @click="$router.push('/market')">
            去逛逛
          </el-button>
        </div>
      </div>
    </div>

    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon market">
          <ShoppingCart />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ marketCount }}</div>
          <div class="stat-label">二手商品</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon task">
          <Tickets />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ taskCount }}</div>
          <div class="stat-label">跑腿任务</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon club">
          <OfficeBuilding />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ clubCount }}</div>
          <div class="stat-label">社团组织</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon activity">
          <Calendar />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ activityCount }}</div>
          <div class="stat-label">精彩活动</div>
        </div>
      </div>
    </div>

    <div class="quick-links">
      <h2>快速入口</h2>
      <div class="link-grid">
        <div class="link-card" @click="$router.push('/market')">
          <div class="link-icon">
            <ShoppingCart />
          </div>
          <span>二手交易</span>
        </div>
        <div class="link-card" @click="$router.push('/tasks')">
          <div class="link-icon">
            <Tickets />
          </div>
          <span>跑腿任务</span>
        </div>
        <div class="link-card" @click="$router.push('/clubs')">
          <div class="link-icon">
            <OfficeBuilding />
          </div>
          <span>社团中心</span>
        </div>
        <div class="link-card" @click="$router.push('/activities')">
          <div class="link-icon">
            <Calendar />
          </div>
          <span>活动报名</span>
        </div>
        <div class="link-card" @click="$router.push('/market/publish')">
          <div class="link-icon">
            <Plus />
          </div>
          <span>发布商品</span>
        </div>
        <div class="link-card" @click="$router.push('/tasks/publish')">
          <div class="link-icon">
            <Edit />
          </div>
          <span>发布任务</span>
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h2>热门商品</h2>
        <el-button type="text" @click="$router.push('/market')">查看更多</el-button>
      </div>
      <div class="item-grid">
        <div class="item-card" v-for="item in hotItems" :key="item.id" @click="$router.push(`/market/${item.id}`)">
          <div class="item-image">
            <img :src="item.coverImage || '/images/default-item.png'" alt="商品图片" />
          </div>
          <div class="item-info">
            <h3>{{ item.title }}</h3>
            <p class="price">¥{{ item.price.toFixed(2) }}</p>
            <p class="publisher">{{ item.publisherName }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h2>最新任务</h2>
        <el-button type="text" @click="$router.push('/tasks')">查看更多</el-button>
      </div>
      <div class="task-list">
        <div class="task-card" v-for="task in latestTasks" :key="task.id" @click="$router.push(`/tasks/${task.id}`)">
          <div class="task-icon">
            <Tickets />
          </div>
          <div class="task-info">
            <h3>{{ task.title }}</h3>
            <p class="task-type">{{ task.taskType }} · {{ task.publisherName }}</p>
          </div>
          <div class="task-reward">
            ¥{{ task.reward.toFixed(2) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ShoppingCart, Tickets, OfficeBuilding, Calendar, Plus, Edit } from '@element-plus/icons-vue'
import { marketApi, taskApi, activityApi } from '@/api'
import type { MarketItem, HelpTask } from '@/types'

const marketCount = ref(0)
const taskCount = ref(0)
const clubCount = ref(0)
const activityCount = ref(0)
const hotItems = ref<MarketItem[]>([])
const latestTasks = ref<HelpTask[]>([])

onMounted(async () => {
  try {
    const marketRes = await marketApi.getMarketItems({ pageNum: 1, pageSize: 6 })
    hotItems.value = marketRes.data.records
    marketCount.value = marketRes.data.total

    const taskRes = await taskApi.getTasks({ pageNum: 1, pageSize: 4 })
    latestTasks.value = taskRes.data.records
    taskCount.value = taskRes.data.total

    const clubRes = await activityApi.getClubs()
    clubCount.value = clubRes.data.length

    const activityRes = await activityApi.getActivities({ pageNum: 1, pageSize: 1 })
    activityCount.value = activityRes.data.total
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.home-container {
  padding: 20px 0;
}

.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 60px 40px;
  text-align: center;
  color: #fff;
  margin-bottom: 30px;
}

.hero-content h1 {
  margin: 0 0 16px 0;
  font-size: 36px;
}

.hero-content p {
  margin: 0 0 24px 0;
  opacity: 0.9;
  font-size: 16px;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.stat-icon.market {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.task {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.club {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.activity {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.quick-links {
  margin-bottom: 30px;
}

.quick-links h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
}

.link-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.link-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.link-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.link-icon {
  width: 48px;
  height: 48px;
  background: #f5f7fa;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #667eea;
}

.link-card span {
  font-size: 14px;
  color: #606266;
}

.section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.item-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.item-image {
  width: 100%;
  padding-top: 100%;
  position: relative;
  background: #f5f7fa;
}

.item-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  padding: 12px;
}

.item-info h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-info .price {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.item-info .publisher {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.task-card:hover {
  background: #fafafa;
}

.task-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.task-info {
  flex: 1;
}

.task-info h3 {
  margin: 0 0 4px 0;
  font-size: 14px;
}

.task-info .task-type {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.task-reward {
  font-size: 16px;
  font-weight: 600;
  color: #67c23a;
}

@media (max-width: 768px) {
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
  .link-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .item-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
