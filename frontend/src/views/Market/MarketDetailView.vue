<template>
  <div class="detail-container" v-if="item">
    <div class="detail-header">
      <div class="images-section">
        <div class="main-image">
          <img :src="item.coverImage || '/images/default-item.png'" alt="商品图片" />
        </div>
        <div class="thumbnails" v-if="imageList.length > 1">
          <div 
            v-for="(img, index) in imageList" 
            :key="index" 
            class="thumbnail"
            :class="{ active: currentImageIndex === index }"
            @click="currentImageIndex = index"
          >
            <img :src="img" alt="" />
          </div>
        </div>
      </div>
      <div class="info-section">
        <h1>{{ item.title }}</h1>
        <div class="price">¥{{ item.price.toFixed(2) }}</div>
        <div class="meta-info">
          <span class="category">{{ item.category }}</span>
          <span class="status" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </span>
        </div>
        <div class="description">
          <h3>商品描述</h3>
          <p>{{ item.description }}</p>
        </div>
        <div class="contact-info">
          <h3>联系方式</h3>
          <p>{{ item.contactInfo }}</p>
        </div>
        <div class="publisher-info">
          <el-avatar :size="40" :src="userStore.userInfo?.avatar">
            {{ item.publisherName?.charAt(0) }}
          </el-avatar>
          <div>
            <p class="name">{{ item.publisherName }}</p>
            <p class="date">发布于 {{ formatDate(item.createTime) }}</p>
          </div>
        </div>
        <div class="actions" v-if="canEdit">
          <el-button type="primary" @click="$router.push(`/market/${item.id}/edit`)">
            编辑商品
          </el-button>
          <el-button 
            v-if="item.status === 'ON_SALE'" 
            type="warning" 
            @click="handleOffShelf"
          >
            下架商品
          </el-button>
          <el-button 
            v-if="item.status === 'ON_SALE'" 
            type="success" 
            @click="handleMarkSold"
          >
            标记已售出
          </el-button>
          <el-button 
            v-if="item.status === 'OFF_SHELF'" 
            type="primary" 
            @click="handleOnShelf"
          >
            重新上架
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { marketApi } from '@/api'
import type { MarketItem } from '@/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const item = ref<MarketItem | null>(null)
const currentImageIndex = ref(0)

const imageList = computed(() => {
  if (!item.value) return []
  const images = item.value.images?.split(',') || []
  return [item.value.coverImage, ...images].filter(Boolean)
})

const canEdit = computed(() => {
  if (!item.value || !userStore.userInfo?.userId) return false
  return Number(userStore.userInfo.userId) === Number(item.value.publisherId)
})

function getStatusClass(status: string) {
  switch (status) {
    case 'ON_SALE': return 'status-on'
    case 'OFF_SHELF': return 'status-off'
    case 'SOLD': return 'status-sold'
    default: return ''
  }
}

function getStatusText(status: string) {
  switch (status) {
    case 'ON_SALE': return '上架中'
    case 'OFF_SHELF': return '已下架'
    case 'SOLD': return '已售出'
    default: return status
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

async function loadItem() {
  const id = Number(route.params.id)
  const response = await marketApi.getMarketItemById(id)
  item.value = response.data
}

async function handleOffShelf() {
  if (!item.value) return
  await marketApi.offShelfItem(item.value.id)
  ElMessage.success('商品已下架')
  item.value.status = 'OFF_SHELF'
}

async function handleMarkSold() {
  if (!item.value) return
  await marketApi.markAsSold(item.value.id)
  ElMessage.success('商品已标记为已售出')
  item.value.status = 'SOLD'
}

async function handleOnShelf() {
  if (!item.value) return
  await marketApi.onShelfItem(item.value.id)
  ElMessage.success('商品已重新上架')
  item.value.status = 'ON_SALE'
}

onMounted(loadItem)
</script>

<style scoped>
.detail-container {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  gap: 40px;
}

.images-section {
  width: 500px;
  flex-shrink: 0;
}

.main-image {
  width: 100%;
  aspect-ratio: 1;
  background: #f5f7fa;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnails {
  display: flex;
  gap: 12px;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  opacity: 0.6;
  transition: all 0.3s;
}

.thumbnail.active {
  opacity: 1;
  border: 2px solid #667eea;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-section {
  flex: 1;
}

.info-section h1 {
  margin: 0 0 16px 0;
  font-size: 24px;
}

.price {
  font-size: 32px;
  font-weight: 600;
  color: #f56c6c;
  margin-bottom: 16px;
}

.meta-info {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.category {
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
}

.status {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.status-on {
  background: #e8f5e9;
  color: #67c23a;
}

.status-off {
  background: #fafafa;
  color: #909399;
}

.status-sold {
  background: #fff3e0;
  color: #e6a23c;
}

.description, .contact-info {
  margin-bottom: 24px;
}

.description h3, .contact-info h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #909399;
}

.description p, .contact-info p {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 24px;
}

.publisher-info .name {
  margin: 0 0 4px 0;
  font-weight: 500;
}

.publisher-info .date {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
  }
  .images-section {
    width: 100%;
  }
}
</style>
