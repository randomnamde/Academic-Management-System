<template>
  <CrudPageShell :title="t('courseArrangement.pageTitle')">
    <template #header-actions>
      <div class="arrangement-header-actions">
        <div class="view-switch" role="tablist" :aria-label="t('courseArrangement.pageTitle')">
          <button
            type="button"
            class="view-switch-option"
            :class="{ 'is-active': viewMode === 'list' }"
            @click="setViewMode('list')"
          >
            {{ t('courseArrangement.listView') }}
          </button>
          <button
            type="button"
            class="view-switch-option"
            :class="{ 'is-active': viewMode === 'timetable' }"
            @click="setViewMode('timetable')"
          >
            {{ t('courseArrangement.timetableView') }}
          </button>
        </div>
        <AppButton v-if="!isStudent" @click="openCreate">{{ t('courseArrangement.addArrangement') }}</AppButton>
      </div>
    </template>

    <template #filters>
      <div class="arrangement-filter-grid">
        <label class="filter-field">
          <span class="filter-label">{{ t('courseArrangement.college') }}</span>
          <el-select
            v-model="searchForm.collegeCode"
            clearable
            filterable
            :disabled="collegeLocked"
            :placeholder="t('courseArrangement.selectCollege')"
            @change="handleSearchCollegeChange"
          >
            <el-option v-for="option in collegeOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </label>
        <label class="filter-field">
          <span class="filter-label">{{ t('courseArrangement.courseCode') }}</span>
          <el-select
            v-model="searchForm.courseCode"
            clearable
            filterable
            :placeholder="t('courseArrangement.selectCourse')"
          >
            <el-option v-for="option in courseOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </label>
        <label v-if="!isStudent && !isTeacher" class="filter-field">
          <span class="filter-label">{{ t('courseArrangement.teacherNo') }}</span>
          <el-select
            v-model="searchForm.teacherNo"
            clearable
            filterable
            :placeholder="t('courseArrangement.selectTeacher')"
          >
            <el-option v-for="option in searchTeacherOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </label>
        <label v-if="!isStudent" class="filter-field">
          <span class="filter-label">{{ t('courseArrangement.classId') }}</span>
          <el-select
            v-model="searchForm.classId"
            clearable
            filterable
            :placeholder="t('courseArrangement.selectClass')"
          >
            <el-option v-for="option in searchClassOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </label>
        <label class="filter-field">
          <span class="filter-label">{{ t('courseArrangement.semester') }}</span>
          <el-input v-model="searchForm.semester" clearable :placeholder="t('courseArrangement.semesterPlaceholder')" />
        </label>
        <div class="filter-actions">
          <div class="app-filter-action-bar">
            <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
            <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          </div>
        </div>
      </div>
    </template>

    <template #table>
      <div class="arrangement-workspace">
        <div class="arrangement-stats">
          <article v-for="metric in arrangementMetrics" :key="metric.label" class="arrangement-stat-card">
            <span class="arrangement-stat-label">{{ metric.label }}</span>
            <strong class="arrangement-stat-value">{{ metric.value }}</strong>
            <span class="arrangement-stat-caption">{{ metric.caption }}</span>
          </article>
        </div>

        <section class="arrangement-panel app-panel app-surface-base">
          <header class="arrangement-panel-header">
            <div>
              <p class="arrangement-panel-kicker">{{ currentViewKicker }}</p>
              <h2 class="arrangement-panel-title">{{ currentViewTitle }}</h2>
            </div>
            <p class="arrangement-panel-description">{{ currentViewDescription }}</p>
          </header>

          <div class="arrangement-panel-body">
            <div v-if="viewMode === 'list'" class="list-view-shell">
              <AppTable :columns="tableColumns" :rows="listRows" :loading="loading" :density="tableDensity">
                <template #cell-schedule="{ row }">
                  <span class="schedule-pill">{{ row.schedule || '--' }}</span>
                </template>
                <template #cell-people="{ row }">
                  <span class="capacity-pill">{{ row.people }}</span>
                </template>
                <template #cell-status="{ row }">
                  <AppBadge :type="statusBadgeType(row.status)">{{ statusLabel(row.status) }}</AppBadge>
                </template>
                <template #cell-actions="{ row }">
                  <div class="app-table-actions arrangement-row-actions">
                    <button class="app-table-action" @click="openEdit(row)">{{ t('courseArrangement.edit') }}</button>
                    <button class="app-table-action app-table-action--danger" @click="handleDelete(row)">{{ t('courseArrangement.delete') }}</button>
                  </div>
                </template>
              </AppTable>
            </div>

            <div v-else class="timetable-shell" v-loading="loading">
              <template v-if="!canViewTimetable">
                <div class="arrangement-empty-state">
                  <el-empty :description="t('courseArrangement.timetableHint')" />
                </div>
              </template>
              <template v-else>
                <div class="timetable-container">
                  <div class="timetable-grid">
                    <div class="time-header-cell">{{ t('courseArrangement.timeAxis') }}</div>
                    <div v-for="day in weekDays" :key="day" class="day-header-cell">{{ day }}</div>

                    <template v-for="(slot, sIndex) in timetableTimeSlots" :key="slot">
                      <div class="time-slot-cell">{{ slot }}</div>

                      <div v-for="(day, dIndex) in weekDays" :key="`${day}-${slot}`" class="timetable-cell">
                        <div v-if="timetableGrid[dIndex][sIndex] && timetableGrid[dIndex][sIndex].length > 0" class="course-cards">
                          <div
                            v-for="(course, cIndex) in timetableGrid[dIndex][sIndex]"
                            :key="cIndex"
                            class="course-card"
                            :style="getCourseCardStyle(course)"
                          >
                            <div class="course-title" :title="course.courseName">{{ course.courseName }}</div>
                            <div class="course-info">{{ course.room }}</div>
                            <div class="course-info" v-if="!isTeacher">{{ course.teacherName }}</div>
                            <div class="course-info" v-if="!isStudent">{{ course.className }}</div>
                          </div>
                        </div>
                      </div>
                    </template>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </section>
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
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.college')" prop="collegeCode">
              <el-select
                v-model="form.collegeCode"
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
            <el-form-item :label="t('courseArrangement.courseCode')" prop="courseCode">
              <el-select
                v-model="form.courseCode"
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
            <el-form-item :label="t('courseArrangement.teacherNo')" prop="teacherNo">
              <el-select
                v-model="form.teacherNo"
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
import AppTable from '@/components/ui/AppTable.vue'
import { getCollegeList } from '@/api/college'
import { getCourseList } from '@/api/course'
import {
  createCourseArrangement,
  deleteCourseArrangement,
  getCourseArrangementList,
  updateCourseArrangement
} from '@/api/courseArrangement'
import { getClassList } from '@/api/clazz'
import { getCourseTimeSlots, getCurrentSemester } from '@/api/system'
import { getTeacherList } from '@/api/teacher'

const store = useStore()
const { t } = useI18n()
const tableDensity = computed(() => store.getters.tableDensity)
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
const currentSemester = ref('')

const canViewTimetable = computed(() => {
  if (isStudent.value) return !!searchForm.semester // Students just need semester, classId is implicit
  if (isTeacher.value) return !!searchForm.semester // Teachers just need semester
  return !!searchForm.semester && (!!searchForm.classId || !!searchForm.teacherNo) // Admins need semester AND (class or teacher)
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
  collegeCode: null,
  courseCode: null,
  teacherNo: null,
  classId: null,
  semester: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  collegeCode: null,
  courseCode: null,
  teacherNo: null,
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
  collegeCode: [{ required: true, message: t('courseArrangement.collegeRequired'), trigger: 'change' }],
  courseCode: [{ required: true, message: t('courseArrangement.courseCodeRequired'), trigger: 'change' }],
  teacherNo: [{ required: true, message: t('courseArrangement.teacherNoRequired'), trigger: 'change' }],
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

const listRows = computed(() =>
  tableData.value.map((item, index) => ({
    ...item,
    rowNumber: (page.value - 1) * size.value + index + 1,
    people: `${item.enrolledCount || 0}/${item.capacity || 0}`
  }))
)

const tableColumns = computed(() => {
  const columns = [
    { key: 'rowNumber', title: t('courseArrangement.index'), width: 72, align: 'left' },
    { key: 'arrangementCode', title: t('courseArrangement.arrangementCode'), width: 168 },
    { key: 'courseName', title: t('courseArrangement.course'), width: 168 },
    { key: 'teacherName', title: t('courseArrangement.teacher'), width: 132 },
    { key: 'className', title: t('courseArrangement.class'), width: 148 },
    { key: 'semester', title: t('courseArrangement.semester'), width: 132 },
    { key: 'schedule', title: t('courseArrangement.schedule'), width: 170 },
    { key: 'room', title: t('courseArrangement.room'), width: 110 },
    { key: 'people', title: t('courseArrangement.people'), width: 110, align: 'left' },
    { key: 'status', title: t('courseArrangement.status'), width: 96, align: 'left' }
  ]

  if (!isStudent.value) {
    columns.push({ key: 'actions', title: t('courseArrangement.actions'), width: 172, align: 'left' })
  }

  return columns
})

const activeArrangementCount = computed(() => tableData.value.filter((item) => Number(item.status) === 1).length)
const courseCoverageCount = computed(() => new Set(tableData.value.map((item) => item.courseCode || item.courseName).filter(Boolean)).size)
const occupiedSlotCount = computed(() => new Set(tableData.value.map((item) => item.schedule).filter(Boolean)).size)

const arrangementMetrics = computed(() => [
  {
    label: t('courseArrangement.metricArrangements'),
    value: viewMode.value === 'list' ? total.value : tableData.value.length,
    caption: t('courseArrangement.metricArrangementsCaption')
  },
  {
    label: t('courseArrangement.metricActive'),
    value: activeArrangementCount.value,
    caption: t('courseArrangement.metricActiveCaption')
  },
  {
    label: t('courseArrangement.metricCourses'),
    value: courseCoverageCount.value,
    caption: t('courseArrangement.metricCoursesCaption')
  },
  {
    label: t('courseArrangement.metricSlots'),
    value: occupiedSlotCount.value,
    caption: t('courseArrangement.metricSlotsCaption')
  }
])

const currentViewKicker = computed(() =>
  viewMode.value === 'list' ? t('courseArrangement.listView') : t('courseArrangement.timetableView')
)

const currentViewTitle = computed(() =>
  viewMode.value === 'list' ? t('courseArrangement.listViewTitle') : t('courseArrangement.timetableViewTitle')
)

const currentViewDescription = computed(() =>
  viewMode.value === 'list'
    ? t('courseArrangement.listViewDescription')
    : t('courseArrangement.timetableViewDescription')
)

function statusLabel(status) {
  return Number(status) === 1 ? t('courseArrangement.statusEnabled') : t('courseArrangement.statusDisabled')
}

function statusBadgeType(status) {
  return Number(status) === 1 ? 'success' : 'info'
}

function mapCollegeOption(item) {
  return {
    value: item.collegeCode,
    label: `${item.collegeName}${item.collegeCode ? ` (${item.collegeCode})` : ''}`
  }
}

function mapCourseOption(item) {
  return {
    value: item.collegeCode,
    label: `${item.courseName}${item.courseCode ? ` (${item.courseCode})` : ''}`
  }
}

function mapTeacherOption(item) {
  return {
    value: item.teacherNo,
    label: `${item.name}${item.teacherNo ? ` (${item.teacherNo})` : ''}`
  }
}

function mapClassOption(item) {
  return {
    value: item.classCode,
    label: `${item.className}${item.classCode ? ` (${item.classCode})` : ''}`
  }
}

function getLockedCollegeOption() {
  if (!userInfo.value?.collegeCode) return null
  return {
    value: userInfo.value.collegeCode,
    label: userInfo.value.collegeName || `${t('courseArrangement.college')} #${userInfo.value.collegeCode}`
  }
}

function getDefaultCollegeCode() {
  if (collegeLocked.value) {
    return userInfo.value?.collegeCode || collegeOptions.value[0]?.value || null
  }
  return null
}

function resetForm() {
  Object.assign(form, {
    id: null,
    collegeCode: getDefaultCollegeCode(),
    courseCode: null,
    teacherNo: null,
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
  if (!collegeLocked.value && !isStudent.value) return
  if (userInfo.value?.collegeCode && (!isStudent.value || userInfo.value?.classId)) return
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

async function loadTeacherOptions(collegeCode, targetRef) {
  if (!collegeCode) {
    targetRef.value = []
    return
  }
  const res = await getTeacherList({ page: 1, size: 500, collegeCode })
  targetRef.value = (res.data?.records || []).map(mapTeacherOption)
}

async function loadClassOptions(collegeCode, targetRef) {
  if (!collegeCode) {
    targetRef.value = []
    return
  }
  const res = await getClassList({ page: 1, size: 500, collegeCode })
  targetRef.value = (res.data?.records || []).map(mapClassOption)
}

async function syncSearchScopedOptions() {
  await Promise.all([
    loadTeacherOptions(searchForm.collegeCode, searchTeacherOptions),
    loadClassOptions(searchForm.collegeCode, searchClassOptions)
  ])
}

async function syncFormScopedOptions() {
  await Promise.all([
    loadTeacherOptions(form.collegeCode, formTeacherOptions),
    loadClassOptions(form.collegeCode, formClassOptions)
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

async function loadCurrentSemesterOption() {
  try {
    const res = await getCurrentSemester()
    currentSemester.value = String(res.data?.currentSemester || '').trim()
  } catch {
    currentSemester.value = ''
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

const courseCardPalettes = [
  { background: '#4f8cff', border: '#77a6ff', text: '#f7fbff' },
  { background: '#5ebc77', border: '#86d49a', text: '#f6fff8' },
  { background: '#9a6bff', border: '#bc95ff', text: '#fbf8ff' },
  { background: '#ff8b3d', border: '#ffad71', text: '#fff9f5' },
  { background: '#e4678c', border: '#ef8dab', text: '#fff8fb' },
  { background: '#2fb8c9', border: '#71d8e4', text: '#f4feff' },
  { background: '#d0a132', border: '#e2bc63', text: '#fffbee' },
  { background: '#6076e8', border: '#8d9cf1', text: '#f7f8ff' }
]

function hashCourseKey(value) {
  const normalized = String(value || '')
  let hash = 0
  for (let index = 0; index < normalized.length; index += 1) {
    hash = (hash * 31 + normalized.charCodeAt(index)) >>> 0
  }
  return hash
}

function getCourseCardStyle(course) {
  const paletteSeed = course?.courseCode || course?.courseName || course?.id || 'course'
  const palette = courseCardPalettes[hashCourseKey(paletteSeed) % courseCardPalettes.length]
  return {
    '--course-card-bg': palette.background,
    '--course-card-border': palette.border,
    '--course-card-text': palette.text
  }
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
      collegeCode: searchForm.collegeCode || undefined,
      courseCode: searchForm.courseCode || undefined,
      teacherNo: searchForm.teacherNo || undefined,
      classId: searchForm.classId || undefined,
      semester: searchForm.semester || undefined
    })
    tableData.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

function setViewMode(mode) {
  if (viewMode.value === mode) return
  viewMode.value = mode
  handleViewModeChange()
}

function handleViewModeChange() {
  if (viewMode.value === 'timetable') {
    if (!searchForm.semester) {
      searchForm.semester = currentSemester.value || '2024-2025-1'
    }
  }
  fetchList();
}

function handleSearch() {
  page.value = 1
  fetchList()
}

async function handleSearchCollegeChange() {
  searchForm.teacherNo = null
  searchForm.classId = null
  await syncSearchScopedOptions()
}

async function handleFormCollegeChange() {
  form.teacherNo = null
  form.classId = null
  await syncFormScopedOptions()
}

async function handleReset() {
  searchForm.collegeCode = getDefaultCollegeCode()
  searchForm.courseCode = null
  searchForm.teacherNo = null
  searchForm.classId = isStudent.value ? (userInfo.value?.classId || null) : null
  searchForm.semester = currentSemester.value || ''
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
    collegeCode: row.collegeCode || getDefaultCollegeCode(),
    courseCode: row.courseCode,
    teacherNo: row.teacherNo,
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
    collegeCode: form.collegeCode,
    courseCode: form.courseCode,
    teacherNo: form.teacherNo,
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
  await Promise.all([loadCollegeOptions(), loadCourseOptions(), loadCourseTimeSlotOptions(), loadCurrentSemesterOption()])

  const defaultCollegeCode = getDefaultCollegeCode()
  if (defaultCollegeCode) {
    searchForm.collegeCode = defaultCollegeCode
  }
  if (isStudent.value && userInfo.value?.classId) {
    searchForm.classId = userInfo.value.classId
  }
  if (!searchForm.semester) {
    searchForm.semester = currentSemester.value || '2024-2025-1'
  }

  await syncSearchScopedOptions()
  resetForm()
  await syncFormScopedOptions()
  await fetchList()
}

onMounted(initializePage)
onActivated(async () => {
  await Promise.all([loadCourseTimeSlotOptions(), loadCurrentSemesterOption()])
  if (isStudent.value && userInfo.value?.classId && !searchForm.classId) {
    searchForm.classId = userInfo.value.classId
  }
  if (!searchForm.semester) {
    searchForm.semester = currentSemester.value || '2024-2025-1'
  }
})
</script>

<style scoped>
.arrangement-header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.view-switch {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 76%, transparent);
  border-radius: 999px;
  background: color-mix(in srgb, var(--surface-base) 76%, transparent);
  box-shadow: inset 0 1px 0 color-mix(in srgb, var(--color-white) 20%, transparent);
}

.view-switch-option {
  border: 0;
  background: transparent;
  color: var(--text-secondary);
  min-width: 92px;
  padding: 7px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition:
    background-color var(--motion-standard),
    color var(--motion-standard),
    box-shadow var(--motion-standard),
    transform var(--motion-standard);
}

.view-switch-option:hover {
  color: var(--text-primary);
}

.view-switch-option.is-active {
  background: color-mix(in srgb, var(--accent-500) 16%, var(--surface-elevated));
  color: var(--text-primary);
  box-shadow: 0 10px 20px color-mix(in srgb, var(--accent-500) 16%, transparent);
}

.arrangement-filter-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 12px;
  align-items: end;
}

.filter-field {
  display: grid;
  gap: 6px;
  grid-column: span 12;
}

.filter-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.filter-actions {
  grid-column: span 12;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.arrangement-row-actions {
  gap: 6px;
}

.arrangement-row-actions .app-table-action {
  min-width: 52px;
  justify-content: center;
}

.arrangement-workspace {
  display: grid;
  gap: 14px;
}

.arrangement-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.arrangement-stat-card {
  display: grid;
  gap: 4px;
  padding: 14px 16px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  border-radius: 14px;
  background:
    linear-gradient(
      135deg,
      color-mix(in srgb, var(--accent-500) 9%, transparent),
      transparent 55%
    ),
    color-mix(in srgb, var(--surface-base) 86%, transparent);
  box-shadow: var(--shadow-soft);
}

.arrangement-stat-label {
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.arrangement-stat-value {
  font-size: 24px;
  line-height: 1;
  color: var(--text-primary);
}

.arrangement-stat-caption {
  font-size: 12px;
  color: var(--text-secondary);
}

.arrangement-panel {
  overflow: hidden;
}

.arrangement-panel-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px 14px;
  border-bottom: 1px solid color-mix(in srgb, var(--panel-border) 76%, transparent);
}

.arrangement-panel-kicker {
  margin: 0 0 6px;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.arrangement-panel-title {
  margin: 0;
  font-size: 19px;
  line-height: 1.2;
  color: var(--text-primary);
}

.arrangement-panel-description {
  margin: 0;
  max-width: 360px;
  text-align: right;
  font-size: 12px;
  line-height: 1.5;
  color: var(--text-secondary);
}

.arrangement-panel-body {
  padding: 16px 18px 18px;
}

.list-view-shell,
.timetable-shell {
  min-height: 320px;
}

.schedule-pill,
.capacity-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 76%, transparent);
  background: color-mix(in srgb, var(--surface-elevated) 74%, transparent);
  color: var(--text-primary);
  font-size: 12px;
  font-weight: 600;
}

.schedule-pill {
  font-family: 'IBM Plex Sans', 'Noto Sans SC', 'PingFang SC', sans-serif;
}

.arrangement-empty-state {
  display: grid;
  place-items: center;
  min-height: 360px;
  border: 1px dashed color-mix(in srgb, var(--panel-border) 72%, transparent);
  border-radius: 16px;
  background: color-mix(in srgb, var(--surface-base) 78%, transparent);
}

.timetable-container {
  --timetable-shell-border: color-mix(in srgb, var(--panel-border) 78%, transparent);
  --timetable-header-bg: color-mix(in srgb, var(--surface-elevated) 78%, var(--surface-base));
  --timetable-header-text: var(--text-primary);
  --timetable-time-bg: color-mix(in srgb, var(--surface-base) 86%, var(--surface-elevated));
  --timetable-time-text: var(--text-primary);
  --timetable-time-muted: var(--text-secondary);
  --timetable-cell-bg: color-mix(in srgb, var(--surface-base) 94%, transparent);
  padding: 2px 0;
  overflow-x: auto;
}

:global(:root[data-theme='dark']) .timetable-container {
  --timetable-shell-border: color-mix(in srgb, var(--panel-border) 74%, transparent);
  --timetable-header-bg: color-mix(in srgb, var(--surface-base) 90%, var(--surface-elevated));
  --timetable-time-bg: color-mix(in srgb, var(--surface-base) 92%, var(--surface-elevated));
  --timetable-cell-bg: color-mix(in srgb, var(--surface-base) 95%, var(--surface-elevated) 5%);
}

.timetable-grid {
  display: grid;
  grid-template-columns: 80px repeat(7, minmax(140px, 1fr));
  gap: 1px;
  background-color: var(--timetable-shell-border);
  border: 1px solid var(--timetable-shell-border);
  border-radius: 14px;
  overflow: hidden;
}

.time-header-cell, .day-header-cell {
  background: var(--timetable-header-bg);
  padding: 12px;
  text-align: center;
  font-weight: 500;
  color: var(--timetable-header-text);
  font-size: 14px;
  letter-spacing: 0.02em;
  box-shadow: inset 0 -1px 0 color-mix(in srgb, var(--timetable-shell-border) 72%, transparent);
}

.time-header-cell {
  line-height: 1.35;
}

.time-slot-cell {
  background: var(--timetable-time-bg);
  padding: 10px 6px;
  text-align: center;
  font-size: 13px;
  color: var(--timetable-time-text);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  line-height: 1.45;
  box-shadow: inset -1px 0 0 color-mix(in srgb, var(--timetable-shell-border) 68%, transparent);
}

.timetable-cell {
  background-color: var(--timetable-cell-bg);
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
  --course-card-bg: #4f8cff;
  --course-card-border: #77a6ff;
  --course-card-text: #f7fbff;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid color-mix(in srgb, var(--course-card-border) 78%, transparent);
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--course-card-bg) 92%, white 8%) 0%,
    var(--course-card-bg) 100%
  );
  color: var(--course-card-text);
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  line-height: 1.2;
  box-shadow: 0 10px 18px color-mix(in srgb, var(--course-card-bg) 24%, transparent);
  transition: transform 0.2s;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 24px color-mix(in srgb, var(--course-card-bg) 28%, transparent);
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
  opacity: 0.92;
}

@media (min-width: 768px) {
  .filter-field {
    grid-column: span 2;
  }

  .filter-actions {
    grid-column: span 2;
    justify-content: flex-start;
  }
}

@media (max-width: 900px) {
  .arrangement-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .arrangement-panel-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .arrangement-panel-description {
    max-width: none;
    text-align: left;
  }
}

@media (max-width: 640px) {
  .arrangement-header-actions {
    width: 100%;
    justify-content: stretch;
  }

  .arrangement-header-actions :deep(.app-button) {
    flex: 1;
  }

  .view-switch {
    width: 100%;
  }

  .view-switch-option {
    flex: 1;
  }

  .arrangement-stats {
    grid-template-columns: 1fr;
  }

  .filter-actions {
    justify-content: flex-start;
  }

  .arrangement-panel-body {
    padding: 14px;
  }

  .timetable-grid {
    grid-template-columns: 72px repeat(7, minmax(136px, 1fr));
  }
}
</style>
