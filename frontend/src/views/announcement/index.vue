<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>通知公告</span>
          <el-button v-if="canManageAnnouncement" type="primary" @click="openCreate">发布公告</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" clearable style="width: 140px">
            <el-option label="通知" value="NOTICE" />
            <el-option label="新闻" value="NEWS" />
            <el-option label="活动" value="EVENT" />
            <el-option label="重要" value="IMPORTANT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable style="width: 120px">
            <el-option label="已发布" :value="1" />
            <el-option label="已下线" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="targetRole" label="目标角色" width="110" />
        <el-table-column prop="priority" label="优先级" width="90" />
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">{{ row.isTop === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column v-if="canManageAnnouncement" label="发布" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column v-if="canManageAnnouncement" label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" style="width: 100%">
                <el-option label="通知" value="NOTICE" />
                <el-option label="新闻" value="NEWS" />
                <el-option label="活动" value="EVENT" />
                <el-option label="重要" value="IMPORTANT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目标角色" prop="targetRole">
              <el-select v-model="form.targetRole" style="width: 100%">
                <el-option label="全部" value="ALL" />
                <el-option label="学生" value="STUDENT" />
                <el-option label="教师" value="TEACHER" />
                <el-option label="管理员" value="ADMIN" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级">
              <el-input-number v-model="form.priority" :min="0" :max="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="置顶">
              <el-switch v-model="isTopSwitch" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">发布</el-radio>
                <el-radio :label="0">下线</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="目标班级ID">
              <el-input-number v-model="form.targetClassId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="生效时间">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="失效时间">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAnnouncement,
  deleteAnnouncement,
  getAnnouncementList,
  updateAnnouncement,
  updateAnnouncementStatus
} from '@/api/announcement'
import { canAction } from '@/permission/ability'

const store = useStore()
const role = computed(() => store.state.userInfo?.role || '')
const permissions = computed(() => store.state.userInfo?.permissions || [])
const canManageAnnouncement = computed(() =>
  canAction(role.value, 'announcement:create', permissions.value) ||
  canAction(role.value, 'announcement:publish', permissions.value)
)

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  title: '',
  type: '',
  status: undefined
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  title: '',
  content: '',
  type: 'NOTICE',
  targetRole: 'ALL',
  priority: 0,
  isTop: 0,
  status: 1
})

const isTopSwitch = computed({
  get: () => form.isTop === 1,
  set: (val) => {
    form.isTop = val ? 1 : 0
  }
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  targetRole: [{ required: true, message: '请选择目标角色', trigger: 'change' }]
}

function resetForm() {
  Object.assign(form, {
    id: null,
    title: '',
    content: '',
    type: 'NOTICE',
    targetRole: 'ALL',
    priority: 0,
    isTop: 0,
    status: 1
  })
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getAnnouncementList({
      page: page.value,
      size: size.value,
      title: searchForm.title || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function handleReset() {
  searchForm.title = ''
  searchForm.type = ''
  searchForm.status = undefined
  handleSearch()
}

function openCreate() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = {
    title: form.title,
    content: form.content,
    type: form.type,
    targetRole: form.targetRole,
    priority: form.priority,
    isTop: form.isTop,
    status: form.status,
    startTime: form.startTime || null,
    endTime: form.endTime || null,
    targetClassId: form.targetClassId || null
  }

  if (isEdit.value) {
    await updateAnnouncement(form.id, payload)
    ElMessage.success('修改成功')
  } else {
    await createAnnouncement(payload)
    ElMessage.success('发布成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该公告吗？', '提示', { type: 'warning' })
  await deleteAnnouncement(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleStatusChange(row, enabled) {
  await updateAnnouncementStatus(row.id, enabled ? 1 : 0)
  ElMessage.success('状态已更新')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
