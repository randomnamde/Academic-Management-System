<template>
  <CrudPageShell :title="t('courseArrangement.pageTitle')">
    <template #header-actions>
      <el-radio-group v-model="viewMode" size="small" class="mr-3" @change="handleViewModeChange">
        <el-radio-button value="list">列表视图</el-radio-button>
        <el-radio-button value="timetable">课表视图</el-radio-button>
      </el-radio-group>
      <AppButton v-if="!isStudent" @click="openCreate">{{ t('courseArrangement.addArrangement') }}</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('courseArrangement.college')">
          <el-select
            v-model="searchForm.collegeId"
            clearable
            filterable
            style="width: 180px"
            :disabled="collegeLocked"
            :placeholder="t('courseArrangement.selectCollege')"
            @change="handleSearchCollegeChange"
          >
            <el-option v-for="option in collegeOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('courseArrangement.courseId')">
          <el-select
            v-model="searchForm.courseId"
            clearable
            filterable
            style="width: 200px"
            :placeholder="t('courseArrangement.selectCourse')"
          >
            <el-option v-for="option in courseOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('courseArrangement.teacherId')" v-if="!isStudent && !isTeacher">
          <el-select
            v-model="searchForm.teacherId"
            clearable
            filterable
            style="width: 220px"
            :placeholder="t('courseArrangement.selectTeacher')"
          >
            <el-option v-for="option in searchTeacherOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('courseArrangement.classId')" v-if="!isStudent">
          <el-select
            v-model="searchForm.classId"
            clearable
            filterable
            style="width: 220px"
            :placeholder="t('courseArrangement.selectClass')"
          >
            <el-option v-for="option in searchClassOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('courseArrangement.semester')">
          <el-input v-model="searchForm.semester" clearable :placeholder="t('courseArrangement.semesterPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">{{ t('common.reset') }}</AppButton>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <div v-if="viewMode === 'list'">
        <el-table :data="tableData" v-loading="loading" stripe>
          <el-table-column type="index" :label="t('courseArrangement.index')" width="60" />
          <el-table-column prop="arrangementCode" :label="t('courseArrangement.arrangementCode')" width="180" />
          <el-table-column prop="courseName" :label="t('courseArrangement.course')" min-width="150" />
          <el-table-column prop="teacherName" :label="t('courseArrangement.teacher')" width="140" />
          <el-table-column prop="className" :label="t('courseArrangement.class')" width="140" />
          <el-table-column prop="semester" :label="t('courseArrangement.semester')" width="140" />
          <el-table-column prop="schedule" :label="t('courseArrangement.schedule')" min-width="170" />
          <el-table-column prop="room" :label="t('courseArrangement.room')" width="100" />
          <el-table-column :label="t('courseArrangement.people')" width="120">
            <template #default="{ row }">{{ row.enrolledCount || 0 }}/{{ row.capacity || 0 }}</template>
          </el-table-column>
          <el-table-column :label="t('courseArrangement.status')" width="90">
            <template #default="{ row }">
              <AppBadge :type="statusBadgeType(row.status)">{{ statusLabel(row.status) }}</AppBadge>
            </template>
          </el-table-column>
          <el-table-column :label="t('courseArrangement.actions')" width="180" fixed="right" v-if="!isStudent">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">{{ t('courseArrangement.edit') }}</el-button>
              <el-button link type="danger" @click="handleDelete(row)">{{ t('courseArrangement.delete') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-else-if="viewMode === 'timetable'" class="timetable-container" v-loading="loading">
        <template v-if="!canViewTimetable">
          <el-empty description="请先选择学期和班级（或教师）以查看准确的课表" />
        </template>
        <template v-else>
          <div class="timetable-grid">
            <!-- Table Header (Days) -->
            <div class="time-header-cell">时间\星期</div>
            <div v-for="day in weekDays" :key="day" class="day-header-cell">{{ day }}</div>
            
            <!-- Table Rows (Time slots) -->
            <template v-for="(slot, sIndex) in timetableTimeSlots" :key="slot">
              <!-- Row Header (Time slot) -->
              <div class="time-slot-cell">{{ slot }}</div>
              
              <!-- Content Cells -->
              <div v-for="(day, dIndex) in weekDays" :key="`${day}-${slot}`" class="timetable-cell">
                <div v-if="timetableGrid[dIndex][sIndex] && timetableGrid[dIndex][sIndex].length > 0" class="course-cards">
                  <div v-for="(course, cIndex) in timetableGrid[dIndex][sIndex]" :key="cIndex" 
                       class="course-card" :class="getColorClass(course.courseId)">
                    <div class="course-title" :title="course.courseName">{{ course.courseName }}</div>
                    <div class="course-info">{{ course.room }}</div>
                    <div class="course-info" v-if="!isTeacher">{{ course.teacherName }}</div>
                    <div class="course-info" v-if="!isStudent">{{ course.className }}</div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </template>
      </div>
    </template>

    <template #pagination v-if="viewMode === 'list'">
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('courseArrangement.dialogEditTitle') : t('courseArrangement.dialogAddTitle')" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.college')" prop="collegeId">
              <el-select
                v-model="form.collegeId"
                filterable
                style="width: 100%"
                :disabled="collegeLocked"
                :placeholder="t('courseArrangement.selectCollege')"
                @change="handleFormCollegeChange"
              >
                <el-option v-for="option in collegeOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.courseId')" prop="courseId">
              <el-select
                v-model="form.courseId"
                filterable
                style="width: 100%"
                :placeholder="t('courseArrangement.selectCourse')"
              >
                <el-option v-for="option in courseOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.teacherId')" prop="teacherId">
              <el-select
                v-model="form.teacherId"
                filterable
                style="width: 100%"
                :placeholder="t('courseArrangement.selectTeacher')"
              >
                <el-option v-for="option in formTeacherOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.classId')" prop="classId">
              <el-select
                v-model="form.classId"
                filterable
                style="width: 100%"
                :placeholder="t('courseArrangement.selectClass')"
              >
                <el-option v-for="option in formClassOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.semester')" prop="semester">
              <el-input v-model="form.semester" :placeholder="t('courseArrangement.semesterPlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.room')">
              <el-input v-model="form.room" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="t('courseArrangement.schedule')" prop="scheduleDay">
          <el-row :gutter="8" style="width: 100%">
            <el-col :span="10">
              <el-select v-model="form.scheduleDay" :placeholder="t('courseArrangement.selectScheduleDay')">
                <el-option v-for="day in weekDays" :key="day" :label="day" :value="day" />
              </el-select>
            </el-col>
            <el-col :span="14">
              <el-select v-model="form.scheduleTime" :placeholder="t('courseArrangement.selectScheduleTime')">
                <el-option v-for="time in formTimeSlotOptions" :key="time" :label="time" :value="time" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.capacity')" prop="capacity">
              <el-input-number v-model="form.capacity" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.status')">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">{{ t('courseArrangement.statusEnabled') }}</el-radio>
                <el-radio :label="0">{{ t('courseArrangement.statusDisabled') }}</el-radio>
              </el-radio-group>
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
import { computed, onActivated, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { getCollegeList } from '@/api/college'
import { getCourseList } from '@/api/course'
import {
  createCourseArrangement,
  deleteCourseArrangement,
  getCourseArrangementList,
  updateCourseArrangement
} from '@/api/courseArrangement'
import { getClassList } from '@/api/clazz'
import { getCourseTimeSlots } from '@/api/system'
import { getTeacherList } from '@/api/teacher'

const store = useStore()
const { t } = useI18n()
const userInfo = computed(() => store.state.userInfo || {})
const role = computed(() => userInfo.value?.primaryRole || userInfo.value?.role || '')
const isSchoolAdmin = computed(() => role.value === 'SCHOOL_ADMIN')
const isCollegeAdmin = computed(() => role.value === 'COLLEGE_ADMIN')
const isTeacher = computed(() => ['COURSE_TEACHER', 'HOMEROOM_TEACHER'].includes(role.value))
const isStudent = computed(() => role.value === 'STUDENT')
const collegeLocked = computed(() => !isSchoolAdmin.value)

const viewMode = ref('timetable') // default to timetable
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const defaultTimeSlots = ['08:00-09:40', '10:00-11:40', '14:00-15:40', '16:00-17:40', '19:00-20:40']
const timeSlots = ref([...defaultTimeSlots])

const canViewTimetable = computed(() => {
  if (isStudent.value) return !!searchForm.semester // Students just need semester, classId is implicit
  if (isTeacher.value) return !!searchForm.semester // Teachers just need semester
  return !!searchForm.semester && (!!searchForm.classId || !!searchForm.teacherId) // Admins need semester AND (class or teacher)
})

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const collegeOptions = ref([])
const courseOptions = ref([])
const searchTeacherOptions = ref([])
const searchClassOptions = ref([])
const formTeacherOptions = ref([])
const formClassOptions = ref([])

const searchForm = reactive({
  collegeId: null,
  courseId: null,
  teacherId: null,
  classId: null,
  semester: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  collegeId: null,
  courseId: null,
  teacherId: null,
  classId: null,
  semester: '',
  scheduleDay: '',
  scheduleTime: '',
  schedule: '',
  room: '',
  capacity: 50,
  status: 1
})

const rules = computed(() => ({
  collegeId: [{ required: true, message: t('courseArrangement.collegeRequired'), trigger: 'change' }],
  courseId: [{ required: true, message: t('courseArrangement.courseIdRequired'), trigger: 'change' }],
  teacherId: [{ required: true, message: t('courseArrangement.teacherIdRequired'), trigger: 'change' }],
  classId: [{ required: true, message: t('courseArrangement.classIdRequired'), trigger: 'change' }],
  semester: [{ required: true, message: t('courseArrangement.semesterRequired'), trigger: 'blur' }],
  scheduleDay: [{ required: true, message: t('courseArrangement.scheduleDayRequired'), trigger: 'change' }],
  scheduleTime: [{ required: true, message: t('courseArrangement.scheduleTimeRequired'), trigger: 'change' }],
  capacity: [{ required: true, message: t('courseArrangement.capacityRequired'), trigger: 'change' }]
}))

const formTimeSlotOptions = computed(() => {
  const slots = [...timeSlots.value]
  if (form.scheduleTime && !slots.includes(form.scheduleTime)) {
    slots.push(form.scheduleTime)
  }
  return slots
})

const scheduleTimeOrder = computed(() => {
  const slots = []
  tableData.value.forEach((item) => {
    const scheduleTime = extractScheduleTime(item?.schedule)
    if (scheduleTime && !slots.includes(scheduleTime)) {
      slots.push(scheduleTime)
    }
  })
  return slots.sort(compareTimeSlot)
})

const timetableTimeSlots = computed(() => {
  if (timeSlots.value.length) {
    return [...timeSlots.value]
  }
  return scheduleTimeOrder.value
})

function statusLabel(status) {
  return Number(status) === 1 ? t('courseArrangement.statusEnabled') : t('courseArrangement.statusDisabled')
}

function statusBadgeType(status) {
  return Number(status) === 1 ? 'success' : 'info'
}

function mapCollegeOption(item) {
  return {
    value: item.id,
    label: `${item.collegeName}${item.collegeCode ? ` (${item.collegeCode})` : ''}`
  }
}

function mapCourseOption(item) {
  return {
    value: item.id,
    label: `${item.courseName}${item.courseCode ? ` (${item.courseCode})` : ''}`
  }
}

function mapTeacherOption(item) {
  return {
    value: item.id,
    label: `${item.name}${item.teacherNo ? ` (${item.teacherNo})` : ''}`
  }
}

function mapClassOption(item) {
  return {
    value: item.id,
    label: `${item.className}${item.classCode ? ` (${item.classCode})` : ''}`
  }
}

function getLockedCollegeOption() {
  if (!userInfo.value?.collegeId) return null
  return {
    value: userInfo.value.collegeId,
    label: userInfo.value.collegeName || `${t('courseArrangement.college')} #${userInfo.value.collegeId}`
  }
}

function getDefaultCollegeId() {
  if (collegeLocked.value) {
    return userInfo.value?.collegeId || collegeOptions.value[0]?.value || null
  }
  return null
}

function resetForm() {
  Object.assign(form, {
    id: null,
    collegeId: getDefaultCollegeId(),
    courseId: null,
    teacherId: null,
    classId: null,
    semester: searchForm.semester,
    scheduleDay: '',
    scheduleTime: '',
    schedule: '',
    room: '',
    capacity: 50,
    status: 1
  })
}

async function ensureUserScopeInfo() {
  if (!collegeLocked.value || userInfo.value?.collegeId) return
  await store.dispatch('getUserInfo').catch(() => null)
}

async function loadCollegeOptions() {
  if (isSchoolAdmin.value || isCollegeAdmin.value) {
    const res = await getCollegeList({ page: 1, size: 200 })
    collegeOptions.value = (res.data?.records || []).map(mapCollegeOption)
    return
  }
  const currentCollegeOption = getLockedCollegeOption()
  collegeOptions.value = currentCollegeOption ? [currentCollegeOption] : []
}

async function loadCourseOptions() {
  const res = await getCourseList({ page: 1, size: 500 })
  courseOptions.value = (res.data?.records || []).map(mapCourseOption)
}

async function loadTeacherOptions(collegeId, targetRef) {
  if (!collegeId) {
    targetRef.value = []
    return
  }
  const res = await getTeacherList({ page: 1, size: 500, collegeId })
  targetRef.value = (res.data?.records || []).map(mapTeacherOption)
}

async function loadClassOptions(collegeId, targetRef) {
  if (!collegeId) {
    targetRef.value = []
    return
  }
  const res = await getClassList({ page: 1, size: 500, collegeId })
  targetRef.value = (res.data?.records || []).map(mapClassOption)
}

async function syncSearchScopedOptions() {
  await Promise.all([
    loadTeacherOptions(searchForm.collegeId, searchTeacherOptions),
    loadClassOptions(searchForm.collegeId, searchClassOptions)
  ])
}

async function syncFormScopedOptions() {
  await Promise.all([
    loadTeacherOptions(form.collegeId, formTeacherOptions),
    loadClassOptions(form.collegeId, formClassOptions)
  ])
}

function extractScheduleTime(schedule) {
  if (!schedule) return ''
  const parts = String(schedule).trim().split(/\s+/)
  return parts.length >= 2 ? parts.slice(1).join(' ') : ''
}

function parseSlotStartMinutes(slot) {
  const [start] = String(slot || '').split('-')
  const [hour, minute] = String(start || '').split(':').map((value) => Number(value))
  if (!Number.isFinite(hour) || !Number.isFinite(minute)) return Number.MAX_SAFE_INTEGER
  return hour * 60 + minute
}

function compareTimeSlot(left, right) {
  return parseSlotStartMinutes(left) - parseSlotStartMinutes(right)
}

function resolveTimetableTimeIndex(scheduleTime) {
  if (!scheduleTime) return -1

  const directIndex = timetableTimeSlots.value.indexOf(scheduleTime)
  if (directIndex >= 0) {
    return directIndex
  }

  const orderedIndex = scheduleTimeOrder.value.indexOf(scheduleTime)
  if (orderedIndex >= 0 && orderedIndex < timetableTimeSlots.value.length) {
    return orderedIndex
  }

  return -1
}

async function loadCourseTimeSlotOptions() {
  try {
    const res = await getCourseTimeSlots()
    const slots = Array.isArray(res.data?.timeSlots) ? res.data.timeSlots.filter(Boolean) : []
    timeSlots.value = slots.length ? slots : [...defaultTimeSlots]
  } catch {
    timeSlots.value = [...defaultTimeSlots]
  }
}

const timetableGrid = computed(() => {
  const grid = Array(7).fill(null).map(() => Array(timetableTimeSlots.value.length).fill(null).map(() => []))
  
  if (!tableData.value || !tableData.value.length) return grid;
  
  tableData.value.forEach(item => {
    if (!item.schedule) return;
    const parts = String(item.schedule).split(' ');
    if (parts.length >= 2) {
      const day = parts[0];
      const time = parts.slice(1).join(' ');
      
      const dayIndex = weekDays.indexOf(day);
      const timeIndex = resolveTimetableTimeIndex(time);
      
      if (dayIndex >= 0 && timeIndex >= 0) {
        grid[dayIndex][timeIndex].push(item);
      }
    }
  });
  
  return grid;
});

const colorClasses = ['bg-blue', 'bg-green', 'bg-purple', 'bg-orange', 'bg-pink', 'bg-cyan'];
function getColorClass(id) {
  if (!id) return colorClasses[0];
  const hash = Number(id) % colorClasses.length;
  return colorClasses[Math.abs(hash)];
}

async function fetchList() {
  if (viewMode.value === 'timetable' && !canViewTimetable.value) {
    tableData.value = [];
    total.value = 0;
    return;
  }

  loading.value = true
  try {
    const res = await getCourseArrangementList({
      page: viewMode.value === 'list' ? page.value : 1, // Timetable needs all data
      size: viewMode.value === 'list' ? size.value : 500,
      collegeId: searchForm.collegeId || undefined,
      courseId: searchForm.courseId || undefined,
      teacherId: searchForm.teacherId || undefined,
      classId: searchForm.classId || undefined,
      semester: searchForm.semester || undefined
    })
    tableData.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

function handleViewModeChange() {
  if (viewMode.value === 'timetable') {
    if (!searchForm.semester) {
      // Default to current semester if not set
      searchForm.semester = '2024-2025-1';
    }
  }
  fetchList();
}

function handleSearch() {
  page.value = 1
  fetchList()
}

async function handleSearchCollegeChange() {
  searchForm.teacherId = null
  searchForm.classId = null
  await syncSearchScopedOptions()
}

async function handleFormCollegeChange() {
  form.teacherId = null
  form.classId = null
  await syncFormScopedOptions()
}

async function handleReset() {
  searchForm.collegeId = getDefaultCollegeId()
  searchForm.courseId = null
  searchForm.teacherId = null
  searchForm.classId = null
  searchForm.semester = ''
  await syncSearchScopedOptions()
  handleSearch()
}

async function openCreate() {
  isEdit.value = false
  resetForm()
  await syncFormScopedOptions()
  dialogVisible.value = true
}

async function openEdit(row) {
  isEdit.value = true
  resetForm()
  
  let scheduleDay = '';
  let scheduleTime = '';
  if (row.schedule) {
    const parts = String(row.schedule).split(' ');
    if (parts.length >= 2) {
      scheduleDay = parts[0];
      scheduleTime = parts.slice(1).join(' ');
    }
  }
  
  Object.assign(form, {
    id: row.id,
    collegeId: row.collegeId || getDefaultCollegeId(),
    courseId: row.courseId,
    teacherId: row.teacherId,
    classId: row.classId,
    semester: row.semester || '',
    scheduleDay: scheduleDay,
    scheduleTime: scheduleTime,
    schedule: row.schedule || '',
    room: row.room || '',
    capacity: row.capacity || 50,
    status: typeof row.status === 'number' ? row.status : Number(row.status || 1)
  })
  await syncFormScopedOptions()
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  form.schedule = `${form.scheduleDay} ${form.scheduleTime}`

  const payload = {
    collegeId: form.collegeId,
    courseId: form.courseId,
    teacherId: form.teacherId,
    classId: form.classId,
    semester: form.semester,
    schedule: form.schedule,
    room: form.room,
    capacity: form.capacity,
    status: form.status
  }

  if (isEdit.value) {
    await updateCourseArrangement(form.id, payload)
    ElMessage.success(t('courseArrangement.updateSuccess'))
  } else {
    await createCourseArrangement(payload)
    ElMessage.success(t('courseArrangement.createSuccess'))
  }

  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(t('courseArrangement.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await deleteCourseArrangement(row.id)
  ElMessage.success(t('courseArrangement.deleteSuccess'))
  fetchList()
}

async function initializePage() {
  await ensureUserScopeInfo()
  await Promise.all([loadCollegeOptions(), loadCourseOptions(), loadCourseTimeSlotOptions()])

  const defaultCollegeId = getDefaultCollegeId()
  if (defaultCollegeId) {
    searchForm.collegeId = defaultCollegeId
  }
  if (!searchForm.semester) {
    searchForm.semester = '2024-2025-1' // 默认学期
  }

  await syncSearchScopedOptions()
  resetForm()
  await syncFormScopedOptions()
  await fetchList()
}

onMounted(initializePage)
onActivated(loadCourseTimeSlotOptions)
</script>

<style scoped>
.timetable-container {
  padding: 10px 0;
  overflow-x: auto;
}

.timetable-grid {
  display: grid;
  grid-template-columns: 80px repeat(7, minmax(140px, 1fr));
  gap: 1px;
  background-color: var(--el-border-color-lighter);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
}

.time-header-cell, .day-header-cell {
  background-color: var(--el-fill-color-light);
  padding: 12px;
  text-align: center;
  font-weight: 600;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.time-slot-cell {
  background-color: var(--el-fill-color-extra-light);
  padding: 8px 4px;
  text-align: center;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.timetable-cell {
  background-color: var(--el-bg-color);
  padding: 6px;
  min-height: 100px;
}

.course-cards {
  display: flex;
  flex-direction: column;
  gap: 6px;
  height: 100%;
}

.course-card {
  padding: 8px 10px;
  border-radius: 6px;
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  line-height: 1.2;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  transition: transform 0.2s;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.course-title {
  font-weight: bold;
  font-size: 13px;
  margin-bottom: 2px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-info {
  opacity: 0.9;
}

/* Course Colors */
.bg-blue { background: linear-gradient(135deg, #409EFF, #53a8ff); }
.bg-green { background: linear-gradient(135deg, #67C23A, #85ce61); }
.bg-purple { background: linear-gradient(135deg, #9b59b6, #a569bd); }
.bg-orange { background: linear-gradient(135deg, #E6A23C, #ebb563); }
.bg-pink { background: linear-gradient(135deg, #ff4d4f, #ff7875); }
.bg-cyan { background: linear-gradient(135deg, #13c2c2, #36cfc9); }
</style>
