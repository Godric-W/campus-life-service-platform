<template>
  <div class="notification-container">
    <div class="notification-header">
      <h2>消息通知</h2>
      <div class="header-actions">
        <span class="unread-count">{{ notificationStore.unreadCount }} 条未读</span>
        <el-button 
          v-if="notificationStore.unreadCount > 0" 
          type="primary" 
          size="small"
          @click="handleMarkAllRead"
        >
          全部已读
        </el-button>
      </div>
    </div>

    <div class="notification-list">
      <div 
        v-for="notification in notificationStore.notifications" 
        :key="notification.id"
        class="notification-item"
        :class="{ unread: notification.readStatus === 0 }"
        @click="handleMarkRead(notification.id)"
      >
        <div class="notification-icon" :class="getTypeClass(notification.type)">
          <component :is="getTypeIcon(notification.type)" />
        </div>
        <div class="notification-content">
          <div class="notification-header">
            <h3>{{ notification.title }}</h3>
            <span class="time">{{ formatTime(notification.createTime) }}</span>
          </div>
          <p>{{ notification.content }}</p>
          <span class="type-label">{{ getTypeText(notification.type) }}</span>
        </div>
        <div class="notification-actions">
          <el-button type="text" @click.stop="handleDelete(notification.id)">删除</el-button>
        </div>
      </div>
    </div>

    <div v-if="notificationStore.notifications.length === 0" class="empty-state">
      <Bell class="empty-icon" />
      <p>暂无通知</p>
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
import { useNotificationStore } from '@/stores/notification'
import { Bell, Tickets, Calendar, ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const notificationStore = useNotificationStore()
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

function getTypeIcon(type: string) {
  switch (type) {
    case 'SYSTEM': return Bell
    case 'TASK': return Tickets
    case 'ACTIVITY': return Calendar
    case 'MARKET': return ShoppingCart
    default: return Bell
  }
}

function getTypeClass(type: string) {
  switch (type) {
    case 'SYSTEM': return 'type-system'
    case 'TASK': return 'type-task'
    case 'ACTIVITY': return 'type-activity'
    case 'MARKET': return 'type-market'
    default: return 'type-default'
  }
}

function getTypeText(type: string) {
  switch (type) {
    case 'SYSTEM': return '系统通知'
    case 'TASK': return '任务提醒'
    case 'ACTIVITY': return '活动通知'
    case 'MARKET': return '交易通知'
    default: return '其他'
  }
}

function formatTime(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return `${date.getMonth() + 1}/${date.getDate()}`
}

async function loadNotifications() {
  const response = await notificationStore.fetchNotifications(pageNum.value, pageSize.value)
  total.value = response.data.total
}

function handlePageChange(page: number) {
  pageNum.value = page
  loadNotifications()
}

async function handleMarkRead(id: number) {
  await notificationStore.markAsRead(id)
}

async function handleMarkAllRead() {
  await notificationStore.markAllAsRead()
  ElMessage.success('已全部标记为已读')
}

async function handleDelete(id: number) {
  await notificationStore.deleteNotification(id)
  ElMessage.success('删除成功')
}

onMounted(async () => {
  await loadNotifications()
  await notificationStore.fetchUnreadCount()
})
</script>

<style scoped>
.notification-container {
  padding: 20px 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.notification-header h2 {
  margin: 0;
  font-size: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.unread-count {
  font-size: 14px;
  color: #f56c6c;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.notification-item:hover {
  background: #fafafa;
}

.notification-item.unread {
  border-left: 4px solid #667eea;
}

.notification-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.type-system {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.type-task {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.type-activity {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.type-market {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.type-default {
  background: linear-gradient(135deg, #969799 0%, #b6b7b9 100%);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-content h3 {
  margin: 0;
  font-size: 16px;
}

.notification-content .time {
  font-size: 12px;
  color: #909399;
}

.notification-content p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-label {
  font-size: 12px;
  padding: 4px 10px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #909399;
}

.notification-actions {
  flex-shrink: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
}

.empty-icon {
  width: 64px;
  height: 64px;
  color: #d9d9d9;
  margin-bottom: 16px;
}

.empty-state p {
  margin: 0;
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
