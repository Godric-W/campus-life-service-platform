<template>
  <div class="detail-container" v-if="task">
    <div class="detail-card">
      <div class="detail-header">
        <div class="title-section">
          <h1>{{ task.title }}</h1>
          <span class="reward">¥{{ task.reward.toFixed(2) }}</span>
        </div>
        <span class="status" :class="getStatusClass(task.status)">
          {{ getStatusText(task.status) }}
        </span>
      </div>

      <div class="detail-body">
        <div class="info-section">
          <div class="info-item">
            <span class="label">任务类型</span>
            <span class="value">{{ task.taskType }}</span>
          </div>
          <div class="info-item">
            <span class="label">取货地点</span>
            <span class="value">{{ task.pickupAddress }}</span>
          </div>
          <div class="info-item">
            <span class="label">送货地点</span>
            <span class="value">{{ task.deliveryAddress }}</span>
          </div>
          <div class="info-item">
            <span class="label">截止时间</span>
            <span class="value deadline">{{ formatDeadline(task.deadline) }}</span>
          </div>
          <div class="info-item">
            <span class="label">联系方式</span>
            <span class="value">{{ task.contactInfo }}</span>
          </div>
        </div>

        <div class="description-section">
          <h3>任务描述</h3>
          <p>{{ task.description }}</p>
        </div>

        <div class="user-section">
          <div class="user-card">
            <div class="user-avatar">
              <el-avatar :size="50">
                {{ task.publisherName?.charAt(0) }}
              </el-avatar>
            </div>
            <div class="user-info">
              <p class="name">{{ task.publisherName }}</p>
              <p class="role">发布者</p>
            </div>
          </div>
          <div class="user-card" v-if="task.accepterId">
            <div class="user-avatar">
              <el-avatar :size="50">
                {{ task.accepterName?.charAt(0) }}
              </el-avatar>
            </div>
            <div class="user-info">
              <p class="name">{{ task.accepterName }}</p>
              <p class="role">接单者</p>
            </div>
          </div>
        </div>
      </div>

      <div class="detail-footer">
        <el-button 
          v-if="canAccept" 
          type="primary" 
          @click="handleAccept"
          :loading="loading"
        >
          接单
        </el-button>
        <el-button 
          v-if="canComplete" 
          type="success" 
          @click="handleComplete"
          :loading="loading"
        >
          完成任务
        </el-button>
        <el-button 
          v-if="canCancel" 
          type="danger" 
          @click="handleCancel"
          :loading="loading"
        >
          取消任务
        </el-button>
        <el-button v-if="!canAccept && !canComplete && !canCancel" type="text" @click="$router.back()">
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
import { taskApi } from '@/api'
import type { HelpTask } from '@/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const task = ref<HelpTask | null>(null)
const loading = ref(false)

const canAccept = computed(() => {
  if (!task.value || !userStore.userInfo?.userId) return false
  return task.value.status === 'PUBLISHED' && Number(task.value.publisherId) !== Number(userStore.userInfo.userId)
})

const canComplete = computed(() => {
  if (!task.value || !userStore.userInfo?.userId) return false
  return task.value.status === 'ACCEPTED' && Number(task.value.accepterId) === Number(userStore.userInfo.userId)
})

const canCancel = computed(() => {
  if (!task.value || !userStore.userInfo?.userId) return false
  return task.value.status === 'PUBLISHED' && Number(task.value.publisherId) === Number(userStore.userInfo.userId)
})

function getStatusClass(status: string) {
  switch (status) {
    case 'PUBLISHED': return 'status-pending'
    case 'ACCEPTED': return 'status-accepted'
    case 'COMPLETED': return 'status-completed'
    case 'CANCELLED': return 'status-cancelled'
    default: return ''
  }
}

function getStatusText(status: string) {
  switch (status) {
    case 'PUBLISHED': return '待接单'
    case 'ACCEPTED': return '进行中'
    case 'COMPLETED': return '已完成'
    case 'CANCELLED': return '已取消'
    default: return status
  }
}

function formatDeadline(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

async function loadTask() {
  const id = Number(route.params.id)
  const response = await taskApi.getTaskById(id)
  task.value = response.data
}

async function handleAccept() {
  if (!task.value) return
  loading.value = true
  try {
    await taskApi.acceptTask(task.value.id)
    ElMessage.success('接单成功')
    task.value.status = 'ACCEPTED'
    task.value.accepterId = userStore.userInfo?.userId
    task.value.accepterName = userStore.userInfo?.username
  } catch {
    ElMessage.error('接单失败')
  } finally {
    loading.value = false
  }
}

async function handleComplete() {
  if (!task.value) return
  loading.value = true
  try {
    await taskApi.completeTask(task.value.id)
    ElMessage.success('任务已完成')
    task.value.status = 'COMPLETED'
  } catch {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

async function handleCancel() {
  if (!task.value) return
  loading.value = true
  try {
    await taskApi.cancelTask(task.value.id)
    ElMessage.success('任务已取消')
    task.value.status = 'CANCELLED'
  } catch {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadTask)
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
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}

.title-section h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
}

.title-section .reward {
  font-size: 28px;
  font-weight: 600;
}

.status {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
}

.status-pending {
  background: rgba(103, 194, 58, 0.2);
  color: #67c23a;
}

.status-accepted {
  background: rgba(64, 158, 255, 0.2);
  color: #409eff;
}

.status-completed {
  background: rgba(144, 147, 153, 0.2);
  color: #909399;
}

.status-cancelled {
  background: rgba(245, 108, 108, 0.2);
  color: #f56c6c;
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
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: #909399;
}

.info-item .value {
  font-size: 16px;
  color: #303133;
}

.info-item .deadline {
  color: #e6a23c;
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

.user-section {
  display: flex;
  gap: 24px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
}

.user-avatar {
  flex-shrink: 0;
}

.user-info .name {
  margin: 0 0 4px 0;
  font-weight: 500;
}

.user-info .role {
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
  .user-section {
    flex-direction: column;
  }
}
</style>
