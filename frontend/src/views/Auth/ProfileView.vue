<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="avatar-wrapper">
        <el-avatar :size="120" :src="userStore.userInfo?.avatar">
          {{ userStore.userInfo?.username?.charAt(0) }}
        </el-avatar>
      </div>
      <div class="user-info">
        <h2>{{ userStore.userInfo?.username }}</h2>
        <p class="student-no">{{ userStore.userInfo?.studentNo }}</p>
        <p class="role">{{ userStore.userInfo?.role === 'STUDENT' ? '学生' : '管理员' }}</p>
      </div>
    </div>

    <div class="profile-content">
      <el-card title="基本信息" class="info-card">
        <el-form ref="formRef" :model="form" label-width="120px">
          <el-form-item label="学号">
            <el-input v-model="form.studentNo" disabled />
          </el-form-item>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" />
          </el-form-item>
          <el-form-item label="学院" prop="college">
            <el-input v-model="form.college" />
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-input v-model="form.major" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleUpdate" :loading="loading">
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card title="操作" class="action-card">
        <el-button type="danger" @click="handleLogout">
          退出登录
        </el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  studentNo: '',
  username: '',
  phone: '',
  email: '',
  college: '',
  major: ''
})

watch(() => userStore.userInfo, (info) => {
  if (info) {
    form.studentNo = info.studentNo
    form.username = info.username
    form.phone = info.phone
    form.email = info.email
    form.college = info.college
    form.major = info.major
  }
}, { immediate: true, deep: true })

onMounted(async () => {
  await userStore.fetchUserInfo()
})

async function handleUpdate() {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    await userStore.updateUserInfo(form)
    ElMessage.success('修改成功')
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    loading.value = false
  }
}

async function handleLogout() {
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.profile-container {
  padding: 30px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 30px;
  color: #fff;
}

.avatar-wrapper {
  flex-shrink: 0;
}

.user-info h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.user-info p {
  margin: 5px 0;
  opacity: 0.9;
}

.profile-content {
  display: flex;
  gap: 30px;
}

.info-card {
  flex: 1;
}

.action-card {
  width: 280px;
}

@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
  }
  .action-card {
    width: 100%;
  }
}
</style>
