<template>
  <div class="my-market-container">
    <div class="page-header">
      <h2>我发布的商品</h2>
    </div>

    <div class="item-grid" v-if="items.length > 0">
      <div class="item-card" v-for="item in items" :key="item.id" @click="$router.push(`/market/${item.id}`)">
        <div class="item-image">
          <img :src="item.coverImage || '/images/default-item.png'" alt="商品图片" />
          <span class="status-badge" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </span>
        </div>
        <div class="item-info">
          <h3>{{ item.title }}</h3>
          <p class="price">¥{{ item.price.toFixed(2) }}</p>
          <p class="category">{{ item.category }}</p>
          <div class="item-footer">
            <span class="views">{{ item.viewCount || 0 }} 次浏览</span>
            <span class="date">{{ formatDate(item.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <p>暂无发布的商品</p>
      <el-button type="primary" @click="$router.push('/market/publish')">去发布</el-button>
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
import { marketApi } from '@/api'
import type { MarketItem } from '@/types'

const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const items = ref<MarketItem[]>([])
const loading = ref(false)

async function loadItems() {
  loading.value = true
  try {
    const response = await marketApi.getMyItems({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    items.value = response.data.records
    total.value = response.data.total
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pageNum.value = page
  loadItems()
}

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

onMounted(loadItems)
onActivated(loadItems)
</script>

<style scoped>
.my-market-container {
  padding: 20px 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.item-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.item-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  background: #f5f7fa;
}

.item-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
}

.status-badge.status-on {
  background: #67c23a;
}

.status-badge.status-off {
  background: #909399;
}

.status-badge.status-sold {
  background: #e6a23c;
}

.item-info {
  padding: 16px;
}

.item-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-info .price {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
}

.item-info .category {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #c0c4cc;
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

@media (max-width: 768px) {
  .item-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
