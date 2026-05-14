<template>
  <div class="detail-container" v-if="club">
    <div class="detail-card">
      <div class="detail-header">
        <div class="logo-section">
          <div class="club-logo">
            <img :src="club.logo || '/images/default-club.png'" alt="社团Logo" />
          </div>
        </div>
        <div class="info-section">
          <h1>{{ club.name }}</h1>
          <p class="contact">{{ club.contactInfo }}</p>
        </div>
      </div>

      <div class="detail-body">
        <div class="description-section">
          <h3>社团简介</h3>
          <p>{{ club.description }}</p>
        </div>

        <div class="manager-section">
          <h3>社团负责人</h3>
          <div class="manager-card">
            <el-avatar :size="48">
              {{ club.adminName?.charAt(0) }}
            </el-avatar>
            <div>
              <p class="name">{{ club.adminName }}</p>
              <p class="role">负责人</p>
            </div>
          </div>
        </div>

        <div class="activities-section">
          <div class="section-header">
            <h3>社团活动</h3>
            <el-button 
              v-if="canManage" 
              type="primary" 
              size="small"
              @click="showPublishModal = true"
            >
              <Plus />
              发布活动
            </el-button>
          </div>
          <div class="activity-list">
            <div 
              v-for="activity in activities" 
              :key="activity.id" 
              class="activity-item"
              @click="$router.push(`/activities/${activity.id}`)"
            >
              <h4>{{ activity.title }}</h4>
              <p>{{ activity.location }} · {{ formatDate(activity.startTime) }}</p>
              <span class="status" :class="getActivityStatusClass(activity.status)">
                {{ getActivityStatusText(activity.status) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog title="发布活动" v-model="showPublishModal" width="600px">
      <el-form ref="publishFormRef" :model="publishForm" :rules="publishRules" label-width="120px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入活动标题" />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="publishForm.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="活动时间" prop="startTime">
          <el-date-picker 
            v-model="publishForm.startTime" 
            type="datetime" 
            placeholder="开始时间"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker 
            v-model="publishForm.endTime" 
            type="datetime" 
            placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="报名截止" prop="signupDeadline">
          <el-date-picker 
            v-model="publishForm.signupDeadline" 
            type="datetime" 
            placeholder="报名截止时间"
          />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number v-model="publishForm.maxParticipants" :min="1" />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input 
            v-model="publishForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入活动描述"
          />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-input v-model="publishForm.coverImage" placeholder="请输入图片URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishModal = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { activityApi } from '@/api'
import type { Club, Activity } from '@/types'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const club = ref<Club | null>(null)
const activities = ref<Activity[]>([])
const showPublishModal = ref(false)
const publishFormRef = ref()
const publishing = ref(false)

const publishForm = reactive({
  title: '',
  location: '',
  startTime: '',
  endTime: '',
  signupDeadline: '',
  maxParticipants: 50,
  description: '',
  coverImage: ''
})

const publishRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  signupDeadline: [{ required: true, message: '请选择报名截止时间', trigger: 'change' }],
  maxParticipants: [{ required: true, message: '请输入最大人数', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }]
}

const canManage = computed(() => {
  return club.value && userStore.userInfo?.userId === club.value.adminId
})

function getActivityStatusClass(status: string) {
  switch (status) {
    case 'PENDING': return 'status-pending'
    case 'ONGOING': return 'status-ongoing'
    case 'ENDED': return 'status-ended'
    case 'CANCELLED': return 'status-cancelled'
    default: return ''
  }
}

function getActivityStatusText(status: string) {
  switch (status) {
    case 'PENDING': return '未开始'
    case 'ONGOING': return '进行中'
    case 'ENDED': return '已结束'
    case 'CANCELLED': return '已取消'
    default: return status
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

async function loadClub() {
  const id = Number(route.params.id)
  const response = await activityApi.getClubById(id)
  club.value = response.data
}

async function loadActivities() {
  if (!club.value) return
  const response = await activityApi.getActivities({
    pageNum: 1,
    pageSize: 10
  })
  activities.value = response.data.records.filter(a => a.clubId === club.value?.id)
}

async function handlePublish() {
  if (!publishFormRef.value || !club.value) return
  const valid = await publishFormRef.value.validate()
  if (!valid) return

  publishing.value = true
  try {
    await activityApi.createActivity({
      clubId: club.value.id,
      ...publishForm
    })
    ElMessage.success('发布成功')
    showPublishModal.value = false
    Object.assign(publishForm, {
      title: '',
      location: '',
      startTime: '',
      endTime: '',
      signupDeadline: '',
      maxParticipants: 50,
      description: '',
      coverImage: ''
    })
    await loadActivities()
  } catch {
    ElMessage.error('发布失败')
  } finally {
    publishing.value = false
  }
}

onMounted(async () => {
  await loadClub()
  await loadActivities()
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
  display: flex;
  gap: 30px;
  padding: 30px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.club-logo {
  width: 120px;
  height: 120px;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.2);
}

.club-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-section h1 {
  margin: 0 0 12px 0;
  font-size: 28px;
}

.info-section .member-count {
  margin: 0 0 8px 0;
  font-size: 16px;
  opacity: 0.9;
}

.info-section .contact {
  margin: 0;
  font-size: 14px;
  opacity: 0.8;
}

.detail-body {
  padding: 30px;
}

.description-section, .manager-section {
  margin-bottom: 30px;
}

.description-section h3, .manager-section h3 {
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

.manager-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.manager-card .name {
  margin: 0 0 4px 0;
  font-weight: 500;
}

.manager-card .role {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.activity-item:hover {
  background: #f0f0f0;
}

.activity-item h4 {
  margin: 0 0 4px 0;
}

.activity-item p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.activity-item .status {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 4px;
}

.status-pending {
  background: #e8f5e9;
  color: #67c23a;
}

.status-ongoing {
  background: #e3f2fd;
  color: #409eff;
}

.status-ended {
  background: #f5f7fa;
  color: #909399;
}

.status-cancelled {
  background: #fef0f0;
  color: #f56c6c;
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}
</style>
