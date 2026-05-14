<template>
  <div class="publish-container">
    <div class="publish-card">
      <div class="publish-header">
        <h2>{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题" />
        </el-form-item>
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="数码产品" value="数码产品" />
            <el-option label="书籍教材" value="书籍教材" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="交通工具" value="交通工具" />
            <el-option label="体育用品" value="体育用品" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number 
            v-model="form.price" 
            :min="0" 
            :step="0.01" 
            placeholder="请输入价格"
          />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="封面图片" prop="coverImage">
          <el-input v-model="form.coverImage" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="图片列表">
          <el-input v-model="form.images" placeholder="多张图片URL用逗号分隔" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="form.contactInfo" placeholder="如：微信、QQ、手机号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ isEdit ? '保存修改' : '发布商品' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marketApi } from '@/api'
import type { PublishItemRequest } from '@/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const isEdit = ref(false)

const form = reactive<PublishItemRequest>({
  title: '',
  description: '',
  price: 0,
  category: '',
  coverImage: '',
  images: '',
  contactInfo: ''
})

const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { min: 0, message: '价格不能为负数', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ]
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    if (isEdit.value) {
      const id = Number(route.params.id)
      await marketApi.updateMarketItem(id, form)
      ElMessage.success('修改成功')
    } else {
      await marketApi.createMarketItem(form)
      ElMessage.success('发布成功')
    }
    router.push('/market')
  } catch (error) {
    ElMessage.error(isEdit.value ? '修改失败' : '发布失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const id = route.params.id
  if (id) {
    isEdit.value = true
    const response = await marketApi.getMarketItemById(Number(id))
    const item = response.data
    form.title = item.title
    form.description = item.description
    form.price = item.price
    form.category = item.category
    form.coverImage = item.coverImage || ''
    form.images = item.images || ''
    form.contactInfo = item.contactInfo
  }
})
</script>

<style scoped>
.publish-container {
  padding: 20px 0;
  max-width: 600px;
  margin: 0 auto;
}

.publish-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.publish-header {
  margin-bottom: 30px;
}

.publish-header h2 {
  margin: 0;
  font-size: 20px;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>
