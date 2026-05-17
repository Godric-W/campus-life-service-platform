<template>
  <div class="detail-container" v-if="activity">
    <div class="detail-card">
      <div class="detail-header">
        <div class="cover-image">
          <img :src="activity.coverImage || '/images/default-activity.png'" alt="活动封面" />
          <span class="status-badge" :class="getStatusClass(activity.status)">
            {{ getStatusText(activity.status) }}
          </span>
        </div>
        <div class="header-info">
          <span class="club-name">{{ activity.clubName }}</span>
          <h1>{{ activity.title }}</h1>
        </div>
      </div>

      <div class="detail-body">
        <div class="info-section">
          <div class="info-item">
            <span class="icon">
              <Location />
            </span>
            <span class="label">活动地点</span>
            <span class="value">{{ activity.location }}</span>
          </div>
          <div class="info-item">
            <span class="icon">
              <Calendar />
            </span>
            <span class="label">活动时间</span>
            <span class="value">{{ formatDateRange(activity.startTime, activity.endTime) }}</span>
          </div>
          <div class="info-item">
            <span class="icon">
              <Clock />
            </span>
            <span class="label">报名截止</span>
            <span class="value">{{ formatDateTime(activity.signupDeadline) }}</span>
          </div>
          <div class="info-item">
            <span class="icon">
              <User />
            </span>
            <span class="label">报名人数</span>
            <span class="value">{{ activity.currentParticipants }}/{{ activity.maxParticipants }} 人</span>
          </div>
        </div>

        <div class="description-section">
          <h3>活动描述</h3>
          <p>{{ activity.description }}</p>
        </div>

        <div class="publisher-section">
          <h3>发布者</h3>
          <div class="publisher-card">
            <el-avatar :size="48">
              {{ activity.publisherName?.charAt(0) }}
            </el-avatar>
            <div>
              <p class="name">{{ activity.publisherName }}</p>
              <p class="date">发布于 {{ formatDate(activity.createTime) }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="detail-footer">
        <el-button 
          v-if="canRegister" 
          type="primary" 
          @click="handleRegister"
          :loading="loading"
        >
          报名参加
        </el-button>
        <el-button 
          v-if="activity.isRegistered" 
          type="warning" 
          @click="handleCancelRegistration"
          :loading="loading"
        >
          取消报名
        </el-button>
        <el-button 
          v-if="canCancelActivity" 
          type="danger" 
          @click="handleCancelActivity"
          :loading="loading"
        >
          取消活动
        </el-button>
        <el-button v-if="!canRegister && !activity.isRegistered && !canCancelActivity" type="text" @click="$router.back()">
          返回
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { activityApi } from '@/api'
import type { Activity } from '@/types'
import { Location, Calendar, Clock, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const activity = ref<Activity | null>(null)
const loading = ref(false)

const canRegister = computed(() => {
  if (!activity.value) return false
  return activity.value.status === 'PUBLISHED' && !activity.value.isRegistered && activity.value.currentParticipants < activity.value.maxParticipants
})

const canCancelActivity = computed(() => {
  if (!activity.value || !userStore.userInfo?.userId) return false
  return activity.value.status === 'PUBLISHED' && Number(activity.value.publisherId) === Number(userStore.userInfo.userId)
})

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
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function formatDateTime(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

function formatDateRange(startStr: string, endStr: string) {
  return `${formatDateTime(startStr)} - ${formatDateTime(endStr)}`
}

async function loadActivity() {
  const id = Number(route.params.id)
  const response = await activityApi.getActivityById(id)
  activity.value = response.data
}

async function handleRegister() {
  if (!activity.value) return
  loading.value = true
  try {
    await activityApi.registerActivity(activity.value.id)
    ElMessage.success('报名成功')
    await loadActivity()
  } catch {
    ElMessage.error('报名失败')
  } finally {
    loading.value = false
  }
}

async function handleCancelRegistration() {
  if (!activity.value) return
  loading.value = true
  try {
    await activityApi.cancelRegistration(activity.value.id)
    ElMessage.success('已取消报名')
    await loadActivity()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

async function handleCancelActivity() {
  if (!activity.value) return
  loading.value = true
  try {
    await activityApi.cancelActivity(activity.value.id)
    ElMessage.success('活动已取消')
    activity.value.status = 'CANCELLED'
  } catch {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadActivity()
})
</script>

<style scoped>
.detail-container {
  padding: 20px 0;
}

.detail-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.detail-header {
  position: relative;
}

.cover-image {
  position: relative;
  width: 100%;
  height: 250px;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
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

.header-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: #fff;
}

.header-info .club-name {
  font-size: 14px;
  opacity: 0.8;
}

.header-info h1 {
  margin: 8px 0 0 0;
  font-size: 24px;
}

.detail-body {
  padding: 24px;
}

.info-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.info-item .icon {
  width: 36px;
  height: 36px;
  background: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
}

.info-item .label {
  font-size: 12px;
  color: #909399;
  width: 60px;
}

.info-item .value {
  font-size: 14px;
  color: #303133;
}

.description-section {
  margin-bottom: 24px;
}

.description-section h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #909399;
}

.description-section p {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
}

.publisher-section {
  margin-bottom: 24px;
}

.publisher-section h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #909399;
}

.publisher-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.publisher-card .name {
  margin: 0 0 4px 0;
  font-weight: 500;
}

.publisher-card .date {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #f0f0f0;
}

@media (max-width: 768px) {
  .info-section {
    grid-template-columns: 1fr;
  }
}
</style>
