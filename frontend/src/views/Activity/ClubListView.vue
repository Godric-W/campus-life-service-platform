<template>
  <div class="club-container">
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索社团"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <Search />
          </el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="showCreateModal = true">
        <Plus />
        创建社团
      </el-button>
    </div>

    <div class="club-grid">
      <div
        class="club-card"
        v-for="club in filteredClubs"
        :key="club.id"
        @click="$router.push(`/clubs/${club.id}`)"
      >
        <div class="club-logo">
          <img :src="club.logo || '/images/default-club.png'" alt="社团Logo" />
        </div>
        <div class="club-info">
          <h3>{{ club.name }}</h3>
          <p class="description">{{ club.description }}</p>
          <div class="club-meta">
            <span class="manager">{{ club.adminName }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="filteredClubs.length === 0" class="empty-state">
      <p>暂无社团</p>
    </div>

    <el-dialog title="创建社团" v-model="showCreateModal" width="500px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="社团名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入社团名称" />
        </el-form-item>
        <el-form-item label="社团简介" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入社团简介"
          />
        </el-form-item>
        <el-form-item label="Logo地址">
          <el-input v-model="createForm.logo" placeholder="请输入Logo URL" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="createForm.contactInfo" placeholder="如：QQ群号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateModal = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { activityApi } from '@/api'
import type { Club } from '@/types'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const clubs = ref<Club[]>([])
const showCreateModal = ref(false)
const createFormRef = ref()
const creating = ref(false)

const createForm = reactive({
  name: '',
  description: '',
  logo: '',
  contactInfo: ''
})

const createRules = {
  name: [
    { required: true, message: '请输入社团名称', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入社团简介', trigger: 'blur' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ]
}

const filteredClubs = computed(() => {
  if (!keyword.value) return clubs.value
  const kw = keyword.value.toLowerCase()
  return clubs.value.filter(club =>
    club.name.toLowerCase().includes(kw) ||
    club.description.toLowerCase().includes(kw)
  )
})

async function loadClubs() {
  const response = await activityApi.getClubs()
  clubs.value = response.data
}

function handleSearch() {
  // Client-side filtering handled by computed property
}

async function handleCreate() {
  if (!createFormRef.value) return
  const valid = await createFormRef.value.validate()
  if (!valid) return

  creating.value = true
  try {
    await activityApi.createClub(createForm)
    ElMessage.success('创建成功')
    showCreateModal.value = false
    createForm.name = ''
    createForm.description = ''
    createForm.logo = ''
    createForm.contactInfo = ''
    loadClubs()
  } catch {
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

onMounted(loadClubs)
</script>

<style scoped>
.club-container {
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

.club-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.club-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.club-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.club-logo {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f7fa;
}

.club-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.club-info {
  flex: 1;
  min-width: 0;
}

.club-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.club-info .description {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.club-meta {
  display: flex;
  gap: 12px;
}

.club-meta span {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
}

.empty-state p {
  color: #909399;
}

@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
  }
  .search-input {
    max-width: none;
  }
  .club-grid {
    grid-template-columns: 1fr;
  }
}
</style>
