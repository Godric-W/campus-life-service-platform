<template>
  <div class="market-container">
    <div class="search-bar">
      <el-input 
        v-model="keyword" 
        placeholder="搜索商品" 
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <Search />
          </el-button>
        </template>
      </el-input>
      <el-select 
        v-model="category" 
        placeholder="选择分类"
        clearable
        @change="handleCategoryChange"
      >
        <el-option label="全部" value="" />
        <el-option label="数码产品" value="数码产品" />
        <el-option label="书籍教材" value="书籍教材" />
        <el-option label="生活用品" value="生活用品" />
        <el-option label="交通工具" value="交通工具" />
        <el-option label="体育用品" value="体育用品" />
        <el-option label="其他" value="其他" />
        <el-option label="自定义..." value="__CUSTOM__" />
      </el-select>
      <el-input
        v-if="category === '__CUSTOM__'"
        v-model="customKeyword"
        placeholder="输入商品关键字"
        class="custom-category-input"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" @click="$router.push('/market/publish')">
        <Plus />
        发布商品
      </el-button>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="status" @change="handleSearch">
        <el-radio :value="'ON_SALE'">上架中</el-radio>
        <el-radio :value="'OFF_SHELF'">已下架</el-radio>
        <el-radio :value="'SOLD'">已售出</el-radio>
        <el-radio :value="'ALL'">全部</el-radio>
      </el-radio-group>
    </div>

    <div class="item-grid">
      <div class="item-card" v-for="item in items" :key="item.id" @click="$router.push(`/market/${item.id}`)">
        <div class="item-image">
          <img :src="item.coverImage || '/images/default-item.png'" alt="商品图片" />
          <div v-if="item.status === 'SOLD'" class="status-badge sold">已售出</div>
          <div v-else-if="item.status === 'OFF_SHELF'" class="status-badge off">已下架</div>
        </div>
        <div class="item-info">
          <h3>{{ item.title }}</h3>
          <p class="price">¥{{ item.price.toFixed(2) }}</p>
          <p class="category">{{ item.category }}</p>
          <p class="publisher">{{ item.publisherName }}</p>
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
import { Search, Plus } from '@element-plus/icons-vue'
import { marketApi } from '@/api'
import type { MarketItem } from '@/types'

const keyword = ref('')
const category = ref('')
const customKeyword = ref('')
const status = ref('ALL')
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const items = ref<MarketItem[]>([])

async function loadItems() {
  const params: Record<string, unknown> = {
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }
  if (category.value === '__CUSTOM__') {
    if (customKeyword.value) params.keyword = customKeyword.value
  } else {
    if (keyword.value) params.keyword = keyword.value
    if (category.value) params.category = category.value
  }
  if (status.value !== 'ALL') params.status = status.value

  const response = await marketApi.getMarketItems(params)
  items.value = response.data.records
  total.value = response.data.total
}

function handleCategoryChange() {
  if (category.value !== '__CUSTOM__') {
    customKeyword.value = ''
  }
  handleSearch()
}

function handleSearch() {
  pageNum.value = 1
  loadItems()
}

function handlePageChange(page: number) {
  pageNum.value = page
  loadItems()
}

onMounted(loadItems)
</script>

<style scoped>
.market-container {
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

.custom-category-input {
  width: 200px;
}

.filter-bar {
  margin-bottom: 20px;
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
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
}

.status-badge.sold {
  background: #67c23a;
}

.status-badge.off {
  background: #909399;
}

.item-info {
  padding: 16px;
}

.item-info h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-info .price {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
}

.item-info .category {
  margin: 0 0 4px 0;
  font-size: 12px;
  color: #909399;
}

.item-info .publisher {
  margin: 0;
  font-size: 12px;
  color: #606266;
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
  .item-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
