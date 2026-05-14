<template>
  <div class="task-container">
    <div class="search-bar">
      <el-input 
        v-model="keyword" 
        placeholder="搜索任务" 
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <Search />
          </el-button>
        </template>
      </el-input>
      <el-select v-model="taskType" placeholder="任务类型">
        <el-option label="全部" value="" />
        <el-option label="取快递" value="取快递" />
        <el-option label="代买" value="代买" />
        <el-option label="代送" value="代送" />
        <el-option label="其他" value="其他" />
      </el-select>
      <el-button type="primary" @click="$router.push('/tasks/publish')">
        <Plus />
        发布任务
      </el-button>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="status" @change="handleSearch">
        <el-radio :value="'PENDING'">待接单</el-radio>
        <el-radio :value="'ACCEPTED'">进行中</el-radio>
        <el-radio :value="'COMPLETED'">已完成</el-radio>
        <el-radio :value="'CANCELLED'">已取消</el-radio>
        <el-radio :value="'ALL'">全部</el-radio>
      </el-radio-group>
    </div>

    <div class="task-list">
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
            <span class="publisher">{{ task.publisherName }}</span>
            <span class="status" :class="getStatusClass(task.status)">
              {{ getStatusText(task.status) }}
            </span>
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
import { ref, onMounted } from 'vue'
import { Search, Plus, Tickets } from '@element-plus/icons-vue'
import { taskApi } from '@/api'
import type { HelpTask } from '@/types'

const keyword = ref('')
const taskType = ref('')
const status = ref('ALL')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tasks = ref<HelpTask[]>([])

async function loadTasks() {
  const params: Record<string, unknown> = {
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }
  if (keyword.value) params.keyword = keyword.value
  if (taskType.value) params.taskType = taskType.value
  if (status.value !== 'ALL') params.status = status.value

  const response = await taskApi.getTasks(params)
  tasks.value = response.data.records
  total.value = response.data.total
}

function handleSearch() {
  pageNum.value = 1
  loadTasks()
}

function handlePageChange(page: number) {
  pageNum.value = page
  loadTasks()
}

function getStatusClass(status: string) {
  switch (status) {
    case 'PENDING': return 'status-pending'
    case 'ACCEPTED': return 'status-accepted'
    case 'COMPLETED': return 'status-completed'
    case 'CANCELLED': return 'status-cancelled'
    default: return ''
  }
}

function getStatusText(status: string) {
  switch (status) {
    case 'PENDING': return '待接单'
    case 'ACCEPTED': return '进行中'
    case 'COMPLETED': return '已完成'
    case 'CANCELLED': return '已取消'
    default: return status
  }
}

function formatDeadline(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(loadTasks)
</script>

<style scoped>
.task-container {
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

.filter-bar {
  margin-bottom: 20px;
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
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 14px;
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
}

.task-header .reward {
  font-size: 20px;
  font-weight: 600;
  color: #67c23a;
}

.task-desc {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.task-type, .location, .deadline {
  font-size: 12px;
  padding: 4px 10px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
}

.deadline {
  background: #fff3e0;
  color: #e6a23c;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.publisher {
  font-size: 12px;
  color: #909399;
}

.status {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 4px;
}

.status-pending {
  background: #e8f5e9;
  color: #67c23a;
}

.status-accepted {
  background: #e3f2fd;
  color: #409eff;
}

.status-completed {
  background: #f5f7fa;
  color: #909399;
}

.status-cancelled {
  background: #fef0f0;
  color: #f56c6c;
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
}
</style>
