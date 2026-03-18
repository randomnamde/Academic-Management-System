<template>
  <div class="workload-page">
    <!-- 我的工作量 -->
    <el-card v-if="!isAdmin" class="workload-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('teacher.myWorkload') }}</span>
          <el-select v-model="semester" :placeholder="t('exam.selectSemester')" @change="loadMyWorkload" class="semester-select">
            <el-option v-for="item in semesterList" :key="item.semester" :label="item.semesterName" :value="item.semester" />
          </el-select>
        </div>
      </template>

      <div class="workload-grid">
        <div class="workload-item">
          <div class="workload-value">{{ workloadData.courseCount || 0 }}</div>
          <div class="workload-label">{{ t('teacher.courseCount') }}</div>
        </div>
        <div class="workload-item">
          <div class="workload-value">{{ workloadData.totalHours || 0 }}</div>
          <div class="workload-label">{{ t('teacher.totalHours') }}</div>
        </div>
        <div class="workload-item">
          <div class="workload-value">{{ workloadData.studentCount || 0 }}</div>
          <div class="workload-label">{{ t('teacher.studentCount') }}</div>
        </div>
        <div class="workload-item">
          <div class="workload-value">{{ workloadData.examCount || 0 }}</div>
          <div class="workload-label">{{ t('teacher.examCount') }}</div>
        </div>
        <div class="workload-item">
          <div class="workload-value">{{ workloadData.invigilateHours || 0 }}</div>
          <div class="workload-label">{{ t('teacher.invigilateHours') }}</div>
        </div>
        <div class="workload-item workload-item--highlight">
          <div class="workload-value">{{ workloadData.workloadScore || 0 }}</div>
          <div class="workload-label">{{ t('teacher.workloadScore') }}</div>
        </div>
      </div>
    </el-card>

    <!-- 管理后台 - 查看所有教师工作量 -->
    <el-card v-if="isAdmin" class="statistics-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('teacher.teachingStatistics') }}</span>
          <div class="header-actions">
            <el-select v-model="collegeCode" clearable :placeholder="t('college.selectCollege')" @change="loadStatistics" class="college-select">
              <el-option v-for="item in collegeList" :key="item.collegeCode" :label="item.collegeName" :value="item.collegeCode" />
            </el-select>
            <el-select v-model="semester" :placeholder="t('exam.selectSemester')" @change="loadStatistics" class="semester-select">
              <el-option v-for="item in semesterList" :key="item.semester" :label="item.semesterName" :value="item.semester" />
            </el-select>
          </div>
        </div>
      </template>

      <AppTable :columns="columns" :rows="tableData" :loading="loading" :pagination="true" v-model:page="page" v-model:page-size="size" :total="total" @page-change="loadStatistics">
        <template #cell-teacherName="{ row }">
          <div class="teacher-info">
            <div class="teacher-name">{{ row.teacherName }}</div>
            <div class="teacher-no">{{ row.teacherNo }}</div>
          </div>
        </template>
        <template #cell-courseCount="{ row }">
          <el-tag type="primary">{{ row.courseCount || 0 }}</el-tag>
        </template>
        <template #cell-totalHours="{ row }">
          <el-tag type="success">{{ row.totalHours || 0 }}</el-tag>
        </template>
        <template #cell-studentCount="{ row }">
          <el-tag type="warning">{{ row.studentCount || 0 }}</el-tag>
        </template>
      </AppTable>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getMyWorkload, getTeachingStatistics } from '@/api/teacher'
import { getCollegeList } from '@/api/college'
import { getSemesterList } from '@/api/semester'
import store from '@/store'

const { t } = useI18n()
const userInfo = computed(() => store.state.userInfo)
const roles = computed(() => store.getters.userRoles)

const isAdmin = computed(() => ['SCHOOL_ADMIN', 'COLLEGE_ADMIN'].includes(userInfo.value?.role))
const isStudent = computed(() => userInfo.value?.role === 'STUDENT')

const semester = ref('')
const collegeCode = ref('')
const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const collegeList = ref([])
const semesterList = ref([])
const workloadData = ref({})

const columns = [
  { key: 'teacherName', label: () => t('teacher.name'), minWidth: 150 },
  { key: 'courseCount', label: () => t('teacher.courseCount'), width: 100 },
  { key: 'totalHours', label: () => t('teacher.totalHours'), width: 100 },
  { key: 'studentCount', label: () => t('teacher.studentCount'), width: 100 }
]

const loadMyWorkload = async () => {
  try {
    const res = await getMyWorkload({ semester: semester.value })
    workloadData.value = res.data || {}
  } catch (error) {
    console.error('Failed to load workload:', error)
  }
}

const loadStatistics = async () => {
  loading.value = true
  try {
    const res = await getTeachingStatistics({
      collegeCode: collegeCode.value || undefined,
      semester: semester.value || undefined,
      page: page.value,
      size: size.value
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to load statistics:', error)
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  try {
    const [collegeRes, semesterRes] = await Promise.all([
      getCollegeList(),
      getSemesterList()
    ])
    collegeList.value = collegeRes.data || []
    semesterList.value = semesterRes.data || []
    if (semesterList.value.length > 0) {
      semester.value = semesterList.value[0].semester
    }
  } catch (error) {
    console.error('Failed to load options:', error)
  }
}

onMounted(() => {
  loadOptions()
  if (isAdmin.value) {
    loadStatistics()
  } else {
    loadMyWorkload()
  }
})
</script>

<style scoped>
.workload-page {
  padding: 16px;
}

.workload-card, .statistics-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.semester-select, .college-select {
  width: 180px;
}

.workload-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.workload-item {
  padding: 20px;
  text-align: center;
  background: #f5f7fa;
  border-radius: 8px;
}

.workload-item--highlight {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.workload-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
}

.workload-label {
  font-size: 14px;
  color: #606266;
}

.workload-item--highlight .workload-label {
  color: rgba(255, 255, 255, 0.9);
}

.teacher-info {
  display: flex;
  flex-direction: column;
}

.teacher-name {
  font-weight: 500;
}

.teacher-no {
  font-size: 12px;
  color: #909399;
}
</style>
