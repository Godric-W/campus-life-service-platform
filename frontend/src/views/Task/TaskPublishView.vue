<template>
  <div class="publish-container">
    <div class="publish-card">
      <div class="publish-header">
        <h2>发布跑腿任务</h2>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="form.taskType" placeholder="请选择任务类型">
            <el-option label="取快递" value="取快递" />
            <el-option label="代买" value="代买" />
            <el-option label="代送" value="代送" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="报酬金额" prop="reward">
          <el-input-number 
            v-model="form.reward" 
            :min="0" 
            :step="0.01" 
            placeholder="请输入报酬金额"
          />
        </el-form-item>
        <el-form-item label="取货地点" prop="pickupAddress">
          <el-input v-model="form.pickupAddress" placeholder="请输入取货地点" />
        </el-form-item>
        <el-form-item label="送货地点" prop="deliveryAddress">
          <el-input v-model="form.deliveryAddress" placeholder="请输入送货地点" />
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker 
            v-model="form.deadline" 
            type="datetime" 
            placeholder="请选择截止时间"
          />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入任务描述"
          />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="form.contactInfo" placeholder="如：微信、QQ、手机号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            发布任务
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { taskApi } from '@/api'
import type { PublishTaskRequest } from '@/types'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive<PublishTaskRequest>({
  title: '',
  description: '',
  taskType: '',
  reward: 0,
  pickupAddress: '',
  deliveryAddress: '',
  deadline: '',
  contactInfo: ''
})

const rules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' }
  ],
  taskType: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ],
  reward: [
    { required: true, message: '请输入报酬金额', trigger: 'blur' },
    { min: 0, message: '金额不能为负数', trigger: 'blur' }
  ],
  pickupAddress: [
    { required: true, message: '请输入取货地点', trigger: 'blur' }
  ],
  deliveryAddress: [
    { required: true, message: '请输入送货地点', trigger: 'blur' }
  ],
  deadline: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入任务描述', trigger: 'blur' }
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
    await taskApi.createTask(form)
    ElMessage.success('发布成功')
    router.push('/tasks')
  } catch (error) {
    ElMessage.error('发布失败')
  } finally {
    loading.value = false
  }
}
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
