<template>
  <div class="dashboard">
    <el-row :gutter="20" class="statistics-row">
      <el-col :xs="24" :sm="12" :lg="6" class="stat-col">
        <el-card class="stat-card glow-card" @click="openStatDetail('student')">
          <div class="stat-icon student-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">学生总数</div>
            <div class="stat-value">{{ statistics.studentCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6" class="stat-col">
        <el-card class="stat-card glow-card" @click="openStatDetail('teacher')">
          <div class="stat-icon teacher-icon">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">教师总数</div>
            <div class="stat-value">{{ statistics.teacherCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6" class="stat-col">
        <el-card class="stat-card glow-card" @click="openStatDetail('course')">
          <div class="stat-icon course-icon">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">课程总数</div>
            <div class="stat-value">{{ statistics.courseCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6" class="stat-col">
        <el-card class="stat-card glow-card" @click="openStatDetail('class')">
          <div class="stat-icon class-icon">
            <el-icon><School /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">班级总数</div>
            <div class="stat-value">{{ statistics.classCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12" class="chart-col">
        <el-card class="panel-card glow-card">
          <template #header>
            <span>学生性别分布</span>
          </template>
          <div ref="genderChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12" class="chart-col">
        <el-card class="panel-card glow-card">
          <template #header>
            <span>课程类型分布</span>
          </template>
          <div ref="courseChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="info-row">
      <el-col :xs="24" :lg="12" class="info-col">
        <el-card class="panel-card glow-card">
          <template #header>
            <div class="card-header">
              <span>最新公告</span>
              <el-link type="primary" @click="$router.push('/announcement')">查看更多</el-link>
            </div>
          </template>
          <el-empty v-if="announcementLoading || !announcements.length" :description="announcementLoading ? '加载中...' : '暂无公告'" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="(item, index) in announcements"
              :key="item.id || index"
              :timestamp="item.createTime"
              :type="index === 0 ? 'primary' : ''"
            >
              <el-link type="primary" :underline="false" @click="openAnnouncementDetail(item)">
                {{ item.title }}
              </el-link>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12" class="info-col">
        <el-card class="panel-card glow-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
            </div>
          </template>
          <el-empty v-if="!todos.length" description="暂无待办事项" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="(item, index) in todos"
              :key="index"
              :timestamp="item.time"
              type="warning"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="detailDialogVisible" title="公告详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题" :span="2">{{ detail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.type || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标角色">{{ detail.targetRole || '-' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ detail.priority ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ detail.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <div class="detail-content">{{ detail.content || '暂无内容' }}</div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statDialogVisible" :title="currentStatTitle" width="900px">
      <el-table :data="statRecords" v-loading="statLoading" stripe>
        <template v-if="statType === 'student'">
          <el-table-column prop="studentNo" label="学号" width="140" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column label="性别" width="90">
            <template #default="{ row }">{{ getGenderText(row.gender) }}</template>
          </el-table-column>
          <el-table-column prop="className" label="班级" min-width="140" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ getStudentStatusText(row.status) }}</template>
          </el-table-column>
        </template>

        <template v-if="statType === 'teacher'">
          <el-table-column prop="teacherNo" label="教师编号" width="150" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column label="性别" width="90">
            <template #default="{ row }">{{ getGenderText(row.gender) }}</template>
          </el-table-column>
          <el-table-column prop="title" label="职称" width="170" />
          <el-table-column prop="department" label="院系" min-width="150" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ row.status === 1 ? '在职' : '停用' }}</template>
          </el-table-column>
        </template>

        <template v-if="statType === 'course'">
          <el-table-column prop="courseCode" label="课程代码" width="140" />
          <el-table-column prop="courseName" label="课程名称" min-width="180" />
          <el-table-column prop="credit" label="学分" width="90" />
          <el-table-column prop="hours" label="学时" width="90" />
          <el-table-column label="类型" width="110">
            <template #default="{ row }">{{ getCourseCategoryText(row.category) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
          </el-table-column>
        </template>

        <template v-if="statType === 'class'">
          <el-table-column prop="classCode" label="班级代码" width="140" />
          <el-table-column prop="className" label="班级名称" min-width="180" />
          <el-table-column prop="grade" label="年级" width="100" />
          <el-table-column prop="major" label="专业" min-width="160" />
          <el-table-column prop="studentCount" label="人数" width="90" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ row.status === 1 ? '在读' : '停用' }}</template>
          </el-table-column>
        </template>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="statPage"
        v-model:page-size="statSize"
        :total="statTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleStatSizeChange"
        @current-change="handleStatPageChange"
      />

      <template #footer>
        <el-button @click="statDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToModule">进入对应模块</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { User, UserFilled, Reading, School } from '@element-plus/icons-vue'
import { getAnnouncementDetail, getAnnouncementList } from '@/api/announcement'
import { getStudentGenderStatistics, getStudentList } from '@/api/student'
import { getTeacherList } from '@/api/teacher'
import { getCourseList } from '@/api/course'
import { getClassList } from '@/api/clazz'

const router = useRouter()
const genderChartRef = ref(null)
const courseChartRef = ref(null)
let genderChartInstance = null
let courseChartInstance = null

const genderStatistics = ref({
  male: 0,
  female: 0
})

const statistics = ref({
  studentCount: 0,
  teacherCount: 0,
  courseCount: 0,
  classCount: 0
})

const announcements = ref([])
const announcementLoading = ref(false)
const detailDialogVisible = ref(false)
const detail = ref({
  id: null,
  title: '',
  content: '',
  type: '',
  targetRole: '',
  priority: 0,
  createTime: ''
})

const statDialogVisible = ref(false)
const statType = ref('student')
const statLoading = ref(false)
const statRecords = ref([])
const statTotal = ref(0)
const statPage = ref(1)
const statSize = ref(10)

const statMeta = {
  student: { title: '学生详情', route: '/student', fetch: getStudentList },
  teacher: { title: '教师详情', route: '/teacher', fetch: getTeacherList },
  course: { title: '课程详情', route: '/course', fetch: getCourseList },
  class: { title: '班级详情', route: '/class', fetch: getClassList }
}

const currentStatTitle = computed(() => statMeta[statType.value]?.title || '详情')

const todos = ref([
  { content: '审核请假申请', time: '2024-01-20' },
  { content: '录入期末成绩', time: '2024-01-18' }
])

const renderGenderChart = () => {
  if (!genderChartInstance) return
  genderChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%', left: 'center' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
        data: [
          { value: genderStatistics.value.male, name: '男生', itemStyle: { color: '#8d79ff' } },
          { value: genderStatistics.value.female, name: '女生', itemStyle: { color: '#c3a2ff' } }
        ]
      }
    ]
  })
}

const renderCourseChart = () => {
  if (!courseChartInstance) return
  courseChartInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'category', data: ['必修课', '选修课', '实践课'] },
    yAxis: { type: 'value' },
    series: [
      {
        data: [45, 30, 15],
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#c7b5ff' },
            { offset: 0.5, color: '#9a80ff' },
            { offset: 1, color: '#7b63f4' }
          ])
        }
      }
    ]
  })
}

const handleChartResize = () => {
  genderChartInstance?.resize()
  courseChartInstance?.resize()
}

const initCharts = () => {
  nextTick(() => {
    if (genderChartRef.value && !genderChartInstance) {
      genderChartInstance = echarts.init(genderChartRef.value)
    }
    if (courseChartRef.value && !courseChartInstance) {
      courseChartInstance = echarts.init(courseChartRef.value)
    }

    renderGenderChart()
    renderCourseChart()

    window.removeEventListener('resize', handleChartResize)
    window.addEventListener('resize', handleChartResize)
  })
}

const fetchStatistics = async () => {
  try {
    const [studentRes, teacherRes, courseRes, classRes] = await Promise.all([
      getStudentList({ page: 1, size: 1 }),
      getTeacherList({ page: 1, size: 1 }),
      getCourseList({ page: 1, size: 1 }),
      getClassList({ page: 1, size: 1 })
    ])

    statistics.value = {
      studentCount: Number(studentRes.data?.total || 0),
      teacherCount: Number(teacherRes.data?.total || 0),
      courseCount: Number(courseRes.data?.total || 0),
      classCount: Number(classRes.data?.total || 0)
    }
  } catch (_e) {
    ElMessage.error('获取统计数据失败')
  }
}

const fetchGenderStatistics = async () => {
  try {
    const res = await getStudentGenderStatistics()
    genderStatistics.value = {
      male: Number(res.data?.male ?? res.data?.MALE ?? 0),
      female: Number(res.data?.female ?? res.data?.FEMALE ?? 0)
    }
    renderGenderChart()
  } catch (_e) {
    ElMessage.error('获取性别分布失败')
  }
}

const fetchLatestAnnouncements = async () => {
  announcementLoading.value = true
  try {
    const res = await getAnnouncementList({
      page: 1,
      size: 8,
      status: 1
    })
    announcements.value = res.data?.records || []
  } finally {
    announcementLoading.value = false
  }
}

const openAnnouncementDetail = async (item) => {
  try {
    const res = await getAnnouncementDetail(item.id)
    detail.value = res.data || {}
    detailDialogVisible.value = true
  } catch (_e) {
    ElMessage.error('获取公告详情失败')
  }
}

const fetchStatDetail = async () => {
  const meta = statMeta[statType.value]
  if (!meta) return

  statLoading.value = true
  try {
    const res = await meta.fetch({
      page: statPage.value,
      size: statSize.value
    })
    statRecords.value = res.data?.records || []
    statTotal.value = Number(res.data?.total || 0)
  } catch (_e) {
    ElMessage.error('获取详情数据失败')
  } finally {
    statLoading.value = false
  }
}

const openStatDetail = async (type) => {
  statType.value = type
  statPage.value = 1
  statSize.value = 10
  statDialogVisible.value = true
  await fetchStatDetail()
}

const handleStatSizeChange = (val) => {
  statSize.value = val
  fetchStatDetail()
}

const handleStatPageChange = (val) => {
  statPage.value = val
  fetchStatDetail()
}

const goToModule = () => {
  const route = statMeta[statType.value]?.route
  statDialogVisible.value = false
  if (route) {
    router.push(route)
  }
}

const getGenderText = (gender) => (gender === 'MALE' ? '男' : '女')

const getStudentStatusText = (status) => {
  const map = {
    ENROLLED: '在读',
    SUSPENDED: '休学',
    GRADUATED: '毕业',
    DROPPED: '退学'
  }
  return map[status] || status || '-'
}

const getCourseCategoryText = (category) => {
  const map = {
    REQUIRED: '必修',
    ELECTIVE: '选修',
    PRACTICAL: '实践'
  }
  return map[category] || category || '-'
}

onMounted(() => {
  fetchStatistics()
  fetchGenderStatistics()
  fetchLatestAnnouncements()
  initCharts()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleChartResize)
  genderChartInstance?.dispose()
  courseChartInstance?.dispose()
  genderChartInstance = null
  courseChartInstance = null
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 20px;
  position: relative;
}

.statistics-row {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 10px;
  padding: 20px 12px;
  min-height: 150px;
  cursor: pointer;
}

.stat-card::before,
.panel-card::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(120deg, rgba(255, 255, 255, 0.16), transparent 45%);
}

.glow-card {
  transition: transform 0.28s ease, box-shadow 0.28s ease;
}

.glow-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 36px rgba(65, 48, 126, 0.24);
}

.stat-col,
.chart-col,
.info-col {
  opacity: 0;
  animation: revealUp 0.68s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

.stat-col:nth-child(1) { animation-delay: 0.04s; }
.stat-col:nth-child(2) { animation-delay: 0.12s; }
.stat-col:nth-child(3) { animation-delay: 0.2s; }
.stat-col:nth-child(4) { animation-delay: 0.28s; }
.chart-col:nth-child(1) { animation-delay: 0.26s; }
.chart-col:nth-child(2) { animation-delay: 0.34s; }
.info-col:nth-child(1) { animation-delay: 0.36s; }
.info-col:nth-child(2) { animation-delay: 0.44s; }

.panel-card {
  position: relative;
  overflow: hidden;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 0;
  font-size: 30px;
  color: #fff;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.2), 0 10px 24px rgba(116, 92, 199, 0.18);
  backdrop-filter: blur(2px);
}

.student-icon {
  background:
    radial-gradient(circle at 24% 24%, rgba(255, 255, 255, 0.38) 0%, rgba(255, 255, 255, 0.08) 30%, rgba(255, 255, 255, 0) 56%),
    radial-gradient(circle at 70% 74%, rgba(126, 104, 248, 0.42) 0%, rgba(126, 104, 248, 0.16) 58%, rgba(126, 104, 248, 0) 100%);
  border: 1px solid rgba(126, 104, 248, 0.22);
  box-shadow: inset 0 0 18px rgba(255, 255, 255, 0.18), 0 8px 22px rgba(118, 95, 212, 0.16);
  color: rgba(255, 255, 255, 0.85);
}

.student-icon :deep(svg) {
  opacity: 0.82;
  transform: translateY(1px);
  filter: drop-shadow(0 2px 6px rgba(109, 87, 198, 0.22));
}

.teacher-icon { background: linear-gradient(135deg, #b89dff 0%, #9377ff 100%); }
.course-icon { background: linear-gradient(135deg, #c5adff 0%, #9b7dff 100%); }
.class-icon { background: linear-gradient(135deg, #aa90ff 0%, #856dff 100%); }

.stat-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-title {
  color: #7e739f;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #2b2150;
}

.chart-row {
  margin-bottom: 20px;
}

.chart {
  height: 300px;
  animation: chartGlow 5.4s ease-in-out infinite;
}

:deep(.panel-card .el-card__header) {
  border-bottom: 1px solid rgba(175, 157, 255, 0.3);
}

:deep(.el-link--primary) {
  color: #7e67f6;
  text-shadow: 0 0 12px rgba(149, 126, 237, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #3a315d;
  min-height: 80px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

@keyframes revealUp {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.985);
    filter: blur(3px);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
    filter: blur(0);
  }
}

@keyframes chartGlow {
  0%,
  100% {
    filter: drop-shadow(0 0 0 rgba(154, 128, 245, 0));
  }
  50% {
    filter: drop-shadow(0 0 14px rgba(154, 128, 245, 0.24));
  }
}

@media (max-width: 992px) {
  .stat-col,
  .chart-col,
  .info-col {
    margin-bottom: 20px;
  }

  .chart-row,
  .info-row {
    margin-bottom: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .stat-col,
  .chart-col,
  .info-col,
  .chart {
    animation: none;
    opacity: 1;
  }

  .glow-card,
  .glow-card:hover {
    transform: none;
  }
}
</style>
