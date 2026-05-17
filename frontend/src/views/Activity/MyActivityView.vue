<template>
  <div class="my-activity-container">
    <div class="page-header">
      <h2>我的活动</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="我报名的" name="registered" />
      <el-tab-pane label="我发布的" name="published" />
    </el-tabs>

    <div class="activity-grid" v-if="activities.length > 0">
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

    <div v-else-if="!loading" class="empty-state">
      <p>{{ activeTab === 'registered' ? '暂未报名任何活动' : '暂未发布任何活动' }}</p>
      <el-button v-if="activeTab === 'published'" type="primary" @click="$router.push('/activities')">去发布</el-button>
    </div>

    <div v-if="total > pageSize" class="pagination-wrapper">
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
import { Location, Clock } from '@element-plus/icons-vue'
import { activityApi } from '@/api'
import type { Activity } from '@/types'

const activeTab = ref('registered')
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const activities = ref<Activity[]>([])
const loading = ref(false)

async function loadActivities() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    const response = activeTab.value === 'registered'
      ? await activityApi.getMyRegisteredActivities(params)
      : await activityApi.getMyPublishedActivities(params)
    activities.value = response.data.records
    total.value = response.data.total
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  pageNum.value = 1
  loadActivities()
}

function handlePageChange(page: number) {
  pageNum.value = page
  loadActivities()
}

function getStatusText(status: string) {
  switch (status) {
    case 'PUBLISHED': return '未开始'
    case 'ACTIVE': return '进行中'
    case 'FINISHED': return '已结束'
    case 'CANCELLED': return '已取消'
    default: return status
  }
}

function getStatusClass(status: string) {
  switch (status) {
    case 'PUBLISHED': return 'status-pending'
    case 'ACTIVE': return 'status-active'
    case 'FINISHED': return 'status-finished'
    case 'CANCELLED': return 'status-cancelled'
    default: return ''
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
.my-activity-container {
  padding: 20px 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
  padding-top: 56%;
  background: linear-gradient(135deg, #667eea, #764ba2);
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
  right: 8px;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
}

.status-badge.status-pending {
  background: #409eff;
}

.status-badge.status-active {
  background: #67c23a;
}

.status-badge.status-finished {
  background: #909399;
}

.status-badge.status-cancelled {
  background: #f56c6c;
}

.activity-info {
  padding: 16px;
}

.activity-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.club-name {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #667eea;
}

.activity-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}

.activity-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.participants {
  color: #f56c6c;
}

.publisher {
  color: #c0c4cc;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #909399;
}

.empty-state p {
  margin-bottom: 16px;
  font-size: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .activity-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
