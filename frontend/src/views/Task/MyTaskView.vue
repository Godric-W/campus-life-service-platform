<template>
  <div class="my-task-container">
    <div class="page-header">
      <h2>我的任务</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="我发布的" name="published" />
      <el-tab-pane label="我接单的" name="accepted" />
    </el-tabs>

    <div class="task-list" v-if="tasks.length > 0">
      <div class="task-card" v-for="task in tasks" :key="task.id" @click="$router.push(`/tasks/${task.id}`)">
        <div class="task-icon-wrapper">
          <div class="task-icon">
            <Tickets />
          </div>
        </div>
        <div class="task-content">
          <div class="task-header">
            <h3>{{ task.title }}</h3>
            <span class="reward">¥{{ task.reward.toFixed(2) }}</span>
          </div>
          <p class="task-desc">{{ task.description }}</p>
          <div class="task-info">
            <span class="task-type">{{ task.taskType }}</span>
            <span class="location">{{ task.pickupAddress }} → {{ task.deliveryAddress }}</span>
            <span class="deadline">截止: {{ formatDeadline(task.deadline) }}</span>
          </div>
          <div class="task-footer">
            <span class="publisher" v-if="activeTab === 'published'">
              <template v-if="task.accepterName">接单者: {{ task.accepterName }}</template>
              <template v-else>暂无人接单</template>
            </span>
            <span class="publisher" v-else>发布者: {{ task.publisherName }}</span>
            <span class="status" :class="getStatusClass(task.status)">
              {{ getStatusText(task.status) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <p>{{ activeTab === 'published' ? '暂未发布任何任务' : '暂未接单任何任务' }}</p>
      <el-button v-if="activeTab === 'published'" type="primary" @click="$router.push('/tasks/publish')">去发布</el-button>
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
import { Tickets } from '@element-plus/icons-vue'
import { taskApi } from '@/api'
import type { HelpTask } from '@/types'

const activeTab = ref('published')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tasks = ref<HelpTask[]>([])
const loading = ref(false)

async function loadTasks() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    const response = activeTab.value === 'published'
      ? await taskApi.getMyPublishedTasks(params)
      : await taskApi.getMyAcceptedTasks(params)
    tasks.value = response.data.records
    total.value = response.data.total
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  pageNum.value = 1
  loadTasks()
}

function handlePageChange(page: number) {
  pageNum.value = page
  loadTasks()
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

function getStatusClass(status: string) {
  switch (status) {
    case 'PUBLISHED': return 'status-pending'
    case 'ACCEPTED': return 'status-accepted'
    case 'COMPLETED': return 'status-completed'
    case 'CANCELLED': return 'status-cancelled'
    default: return ''
  }
}

function formatDeadline(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(loadTasks)
onActivated(loadTasks)
</script>

<style scoped>
.my-task-container {
  padding: 20px 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.task-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.task-icon-wrapper {
  flex-shrink: 0;
}

.task-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.reward {
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
  flex-shrink: 0;
  margin-left: 16px;
}

.task-desc {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-info {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}

.task-type {
  padding: 2px 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.publisher {
  font-size: 13px;
  color: #909399;
}

.status {
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.status-pending {
  background: #fef0f0;
  color: #f56c6c;
}

.status-accepted {
  background: #e8f4ff;
  color: #409eff;
}

.status-completed {
  background: #e8f5e9;
  color: #67c23a;
}

.status-cancelled {
  background: #fafafa;
  color: #909399;
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
</style>
