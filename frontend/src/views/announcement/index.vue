<template>
  <CrudPageShell :title="t('announcement.pageTitle')">
    <template #header-actions>
      <AppButton v-if="canManageAnnouncement" @click="openCreate">{{ t('announcement.publish') }}</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('announcement.title')">
          <el-input v-model="searchForm.title" clearable />
        </el-form-item>
        <el-form-item :label="t('announcement.type')">
          <el-select v-model="searchForm.type" clearable style="width: 140px">
            <el-option :label="t('announcement.typeNotice')" value="NOTICE" />
            <el-option :label="t('announcement.typeNews')" value="NEWS" />
            <el-option :label="t('announcement.typeEvent')" value="EVENT" />
            <el-option :label="t('announcement.typeImportant')" value="IMPORTANT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('announcement.status')">
          <el-select v-model="searchForm.status" clearable style="width: 120px">
            <el-option :label="t('announcement.statusPublished')" :value="1" />
            <el-option :label="t('announcement.statusOffline')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">{{ t('common.reset') }}</AppButton>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" :label="t('announcement.index')" width="60" />
        <el-table-column prop="title" :label="t('announcement.title')" min-width="220" />
        <el-table-column prop="type" :label="t('announcement.type')" width="100" />
        <el-table-column :label="t('announcement.targetRole')" width="140">
          <template #default="{ row }">{{ targetRoleLabel(row.targetRole) }}</template>
        </el-table-column>
        <el-table-column prop="priority" :label="t('announcement.priority')" width="90" />
        <el-table-column prop="isTop" :label="t('announcement.isTop')" width="80">
          <template #default="{ row }">{{ row.isTop === 1 ? t('announcement.yes') : t('announcement.no') }}</template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('announcement.createTime')" width="180" />
        <el-table-column v-if="canManageAnnouncement" :label="t('announcement.publishSwitch')" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column v-if="canManageAnnouncement" :label="t('announcement.actions')" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">{{ t('announcement.edit') }}</el-button>
            <el-button link type="danger" @click="handleDelete(row)">{{ t('announcement.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #pagination>
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
    </template>

    <AppModal v-model="dialogVisible" :title="isEdit ? t('announcement.dialogEditTitle') : t('announcement.dialogPublishTitle')" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('announcement.title')" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item :label="t('announcement.content')" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item :label="t('announcement.type')" prop="type">
              <el-select v-model="form.type" style="width: 100%">
                <el-option :label="t('announcement.typeNotice')" value="NOTICE" />
                <el-option :label="t('announcement.typeNews')" value="NEWS" />
                <el-option :label="t('announcement.typeEvent')" value="EVENT" />
                <el-option :label="t('announcement.typeImportant')" value="IMPORTANT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('announcement.targetRole')" prop="targetRole">
              <el-select v-model="form.targetRole" style="width: 100%">
                <el-option :label="t('announcement.targetAll')" value="ALL" />
                <el-option :label="t('roles.schoolAdmin')" value="SCHOOL_ADMIN" />
                <el-option :label="t('roles.collegeAdmin')" value="COLLEGE_ADMIN" />
                <el-option :label="t('roles.homeroomTeacher')" value="HOMEROOM_TEACHER" />
                <el-option :label="t('roles.courseTeacher')" value="COURSE_TEACHER" />
                <el-option :label="t('roles.student')" value="STUDENT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('announcement.priority')">
              <el-input-number v-model="form.priority" :min="0" :max="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('announcement.isTop')">
              <el-switch v-model="isTopSwitch" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('announcement.status')">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">{{ t('announcement.publishAction') }}</el-radio>
                <el-radio :label="0">{{ t('announcement.offlineAction') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item :label="t('announcement.targetClassId')">
              <el-input-number v-model="form.targetClassId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('announcement.startTime')">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('announcement.endTime')">
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
        <AppButton variant="secondary" @click="dialogVisible = false">{{ t('common.cancel') }}</AppButton>
        <AppButton @click="submit">{{ t('common.save') }}</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppModal from '@/components/ui/AppModal.vue'
import {
  createAnnouncement,
  deleteAnnouncement,
  getAnnouncementList,
  updateAnnouncement,
  updateAnnouncementStatus
} from '@/api/announcement'
import { canAction } from '@/permission/ability'

const store = useStore()
const { t } = useI18n()
const role = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
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

const rules = computed(() => ({
  title: [{ required: true, message: t('announcement.titleRequired'), trigger: 'blur' }],
  content: [{ required: true, message: t('announcement.contentRequired'), trigger: 'blur' }],
  type: [{ required: true, message: t('announcement.typeRequired'), trigger: 'change' }],
  targetRole: [{ required: true, message: t('announcement.targetRoleRequired'), trigger: 'change' }]
}))

function resetForm() {
  Object.assign(form, {
    id: null,
    title: '',
    content: '',
    type: 'NOTICE',
    targetRole: 'ALL',
    priority: 0,
    isTop: 0,
    status: 1,
    startTime: null,
    endTime: null,
    targetClassId: null
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
    ElMessage.success(t('announcement.updateSuccess'))
  } else {
    await createAnnouncement(payload)
    ElMessage.success(t('announcement.publishSuccess'))
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(t('announcement.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await deleteAnnouncement(row.id)
  ElMessage.success(t('announcement.deleteSuccess'))
  fetchList()
}

async function handleStatusChange(row, enabled) {
  await updateAnnouncementStatus(row.id, enabled ? 1 : 0)
  ElMessage.success(t('announcement.statusUpdated'))
  fetchList()
}

function targetRoleLabel(targetRole) {
  const map = {
    ALL: t('announcement.targetAll'),
    SCHOOL_ADMIN: t('roles.schoolAdmin'),
    COLLEGE_ADMIN: t('roles.collegeAdmin'),
    HOMEROOM_TEACHER: t('roles.homeroomTeacher'),
    COURSE_TEACHER: t('roles.courseTeacher'),
    STUDENT: t('roles.student')
  }
  return map[targetRole] || targetRole || '-'
}

onMounted(fetchList)
</script>

