<template>
  <div class="layout-container">
    <header class="layout-header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon size="32" color="#667eea">
            <OfficeBuilding />
          </el-icon>
          <span>校园生活平台</span>
        </div>
        <nav class="nav-menu">
          <el-button 
            v-for="item in menuItems" 
            :key="item.path"
            type="text"
            :class="{ active: $route.path === item.path }"
            @click="$router.push(item.path)"
          >
            <component :is="item.icon" class="menu-icon" />
            <span>{{ item.label }}</span>
          </el-button>
        </nav>
        <div class="header-right">
          <el-badge :value="notificationStore.unreadCount" :hidden="notificationStore.unreadCount === 0">
            <el-button type="text" @click="$router.push('/notifications')">
              <Bell class="icon" />
            </el-button>
          </el-badge>
          <el-dropdown>
            <el-button type="text" class="user-btn">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.username?.charAt(0) }}
              </el-avatar>
              <span>{{ userStore.userInfo?.username }}</span>
              <ArrowDown />
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    <main class="layout-main">
      <router-view />
    </main>
    <footer class="layout-footer">
      <p>© 2024 校园综合生活服务平台</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { OfficeBuilding, Bell, ArrowDown, HomeFilled, ShoppingCart, Tickets, Calendar, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const notificationStore = useNotificationStore()

const menuItems = [
  { path: '/', label: '首页', icon: HomeFilled },
  { path: '/market', label: '二手交易', icon: ShoppingCart },
  { path: '/tasks', label: '跑腿任务', icon: Tickets },
  { path: '/clubs', label: '社团', icon: OfficeBuilding },
  { path: '/activities', label: '活动', icon: Calendar },
  { path: '/notifications', label: '通知', icon: ChatDotRound }
]

onMounted(async () => {
  await userStore.fetchUserInfo()
  await notificationStore.fetchUnreadCount()
})

async function handleLogout() {
  await userStore.logout()
  ElMessage.success('已退出登录')
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
  cursor: pointer;
}

.nav-menu {
  display: flex;
  gap: 20px;
}

.nav-menu button {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s;
}

.nav-menu button:hover,
.nav-menu button.active {
  background: #f0f0f0;
  color: #667eea;
}

.menu-icon {
  width: 18px;
  height: 18px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon {
  font-size: 20px;
  color: #606266;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
}

.layout-main {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

.layout-footer {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
  border-top: 1px solid #f0f0f0;
}

@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
}
</style>
