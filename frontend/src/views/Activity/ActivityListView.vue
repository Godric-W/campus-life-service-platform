<template>
  <div class="activity-container">
    <div class="search-bar">
      <el-input 
        v-model="keyword" 
        placeholder="搜索活动" 
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <Search />
          </el-button>
        </template>
      </el-input>
      <el-radio-group v-model="status" @change="handleSearch">
        <el-radio :value="'ALL'">全部</el-radio>
        <el-radio :value="'PUBLISHED'">未开始</el-radio>
        <el-radio :value="'FINISHED'">已结束</el-radio>
      </el-radio-group>
    </div>

    <div class="activity-grid">
      <div class="activity-card" v-for="activity in activities" :key="activity.id" @click="$router.push(`/activities/${activity.id}`)">
        <div class="activity-image">
          <img :src="activity.coverImage || '/images/default-activity.png'" alt="活动封面" />
          <span class="status-badge" :class="getStatusClass(activity.status)">
            {{ getStatusText(activity.status) }}
          </span>
        </div>
        <div class="activity-info">
          <h3>{{ activity.title }}</h3>
          <p class="club-name">{{ activity.clubName }}</p>
          <div class="activity-meta">
            <span class="location">
              <Location />
              {{ activity.location }}
            </span>
            <span class="time">
              <Clock />
              {{ formatDate(activity.startTime) }}
            </span>
          </div>
          <div class="activity-footer">
            <span class="participants">{{ activity.currentParticipants }}/{{ activity.maxParticipants }} 人报名</span>
            <span class="publisher">{{ activity.publisherName }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="handlePageChange"
        layout="prev, pager, next, jumper, ->, total"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated } from 'vue'
import { Search, Location, Clock } from '@element-plus/icons-vue'
import { activityApi } from '@/api'
import type { Activity } from '@/types'

const keyword = ref('')
const status = ref('ALL')
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const activities = ref<Activity[]>([])

async function loadActivities() {
  const params: Record<string, unknown> = {
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }
  if (keyword.value) params.keyword = keyword.value
  if (status.value !== 'ALL') params.status = status.value

  const response = await activityApi.getActivities(params)
  activities.value = response.data.records
  total.value = response.data.total
}

function handleSearch() {
  pageNum.value = 1
  loadActivities()
}

function handlePageChange(page: number) {
  pageNum.value = page
  loadActivities()
}

function getStatusClass(status: string) {
  switch (status) {
    case 'PUBLISHED': return 'status-pending'
    case 'FINISHED': return 'status-ended'
    case 'CANCELLED': return 'status-cancelled'
    default: return ''
  }
}

function getStatusText(status: string) {
  switch (status) {
    case 'PUBLISHED': return '未开始'
    case 'FINISHED': return '已结束'
    case 'CANCELLED': return '已取消'
    default: return status
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(loadActivities)
onActivated(loadActivities)
</script>

<style scoped>
.activity-container {
  padding: 20px 0;
}

.search-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.activity-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.activity-image {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  background: #f5f7fa;
}

.activity-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
}

.status-pending {
  background: #67c23a;
}

.status-ongoing {
  background: #409eff;
}

.status-ended {
  background: #909399;
}

.status-cancelled {
  background: #f56c6c;
}

.activity-info {
  padding: 16px;
}

.activity-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.activity-info .club-name {
  margin: 0 0 12px 0;
  font-size: 12px;
  color: #909399;
}

.activity-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.activity-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-footer .participants {
  font-size: 12px;
  color: #67c23a;
}

.activity-footer .publisher {
  font-size: 12px;
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
  }
  .search-input {
    max-width: none;
  }
  .activity-grid {
    grid-template-columns: 1fr;
  }
}
</style>
