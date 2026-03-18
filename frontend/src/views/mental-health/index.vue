<template>
  <div class="mental-health-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane :label="t('mentalHealth.records')" name="records">
        <div class="toolbar">
          <AppButton @click="openRecordDialog">
            <el-icon><Plus /></el-icon>
            {{ t('mentalHealth.addRecord') }}
          </AppButton>
        </div>

        <AppTable :columns="recordColumns" :rows="recordList" :loading="loading" :pagination="true"
          v-model:page="page" v-model:page-size="size" :total="total" @page-change="loadRecords">
          <template #cell-riskLevel="{ row }">
            <el-tag :type="getRiskType(row.riskLevel)">{{ getRiskLabel(row.riskLevel) }}</el-tag>
          </template>
          <template #cell-actions="{ row }">
            <button class="app-table-action" @click="openRecordDialog(row)">{{ t('common.edit') }}</button>
          </template>
        </AppTable>
      </el-tab-pane>

      <el-tab-pane :label="t('mentalHealth.interviews')" name="interviews">
        <div class="toolbar">
          <AppButton @click="openInterviewDialog">
            <el-icon><Plus /></el-icon>
            {{ t('mentalHealth.addInterview') }}
          </AppButton>
        </div>

        <AppTable :columns="interviewColumns" :rows="interviewList" :loading="loading" :pagination="true"
          v-model:page="interviewPage" v-model:page-size="interviewSize" :total="interviewTotal" @page-change="loadInterviews">
          <template #cell-interviewType="{ row }">
            {{ getInterviewTypeLabel(row.interviewType) }}
          </template>
        </AppTable>
      </el-tab-pane>

      <el-tab-pane :label="t('mentalHealth.crises')" name="crises">
        <div class="toolbar">
          <AppButton @click="openCrisisDialog">
            <el-icon><Plus /></el-icon>
            {{ t('mentalHealth.reportCrisis') }}
          </AppButton>
        </div>

        <AppTable :columns="crisisColumns" :rows="crisisList" :loading="loading" :pagination="true"
          v-model:page="crisisPage" v-model:page-size="crisisSize" :total="crisisTotal" @page-change="loadCrises">
          <template #cell-riskLevel="{ row }">
            <el-tag :type="getCrisisRiskType(row.riskLevel)">{{ getCrisisRiskLabel(row.riskLevel) }}</el-tag>
          </template>
          <template #cell-outcome="{ row }">
            <el-tag :type="getOutcomeType(row.outcome)">{{ getOutcomeLabel(row.outcome) }}</el-tag>
          </template>
        </AppTable>
      </el-tab-pane>

      <el-tab-pane :label="t('mentalHealth.riskWarning')" name="warning">
        <AppTable :columns="riskColumns" :rows="riskList" :loading="loading">
          <template #cell-riskLevel="{ row }">
            <el-tag :type="getRiskType(row.riskLevel)">{{ getRiskLabel(row.riskLevel) }}</el-tag>
          </template>
        </AppTable>
      </el-tab-pane>
    </el-tabs>

    <!-- Record Dialog -->
    <el-dialog v-model="recordDialogVisible" :title="isEdit ? t('common.edit') : t('mentalHealth.addRecord')" width="600px">
      <el-form :model="recordForm" label-width="120px">
        <el-form-item :label="t('mentalHealth.studentNo')">
          <el-input v-model="recordForm.studentNo" />
        </el-form-item>
        <el-form-item :label="t('mentalHealth.assessmentType')">
          <el-select v-model="recordForm.assessmentType">
            <el-option label="SCL-90" value="SCL90" />
            <el-option label="SDS抑郁自评" value="SDS" />
            <el-option label="SAS焦虑自评" value="SAS" />
            <el-option label="GHQ一般健康问卷" value="GHQ" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('mentalHealth.totalScore')">
          <el-input-number v-model="recordForm.totalScore" :min="0" :max="200" />
        </el-form-item>
        <el-form-item :label="t('mentalHealth.counselorNote')">
          <el-input v-model="recordForm.counselorNote" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <AppButton @click="handleSaveRecord">{{ t('common.save') }}</AppButton>
      </template>
    </el-dialog>

    <!-- Interview Dialog -->
    <el-dialog v-model="interviewDialogVisible" :title="t('mentalHealth.addInterview')" width="600px">
      <el-form :model="interviewForm" label-width="120px">
        <el-form-item :label="t('mentalHealth.studentNo')">
          <el-input v-model="interviewForm.studentNo" />
        </el-form-item>
        <el-form-item :label="t('mentalHealth.interviewType')">
          <el-select v-model="interviewForm.interviewType">
            <el-option label="初次访谈" value="INITIAL" />
            <el-option label="跟进访谈" value="FOLLOW" />
            <el-option label="危机访谈" value="CRISIS" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('mentalHealth.topic')">
          <el-input v-model="interviewForm.topic" />
        </el-form-item>
        <el-form-item :label="t('mentalHealth.content')">
          <el-input v-model="interviewForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item :label="t('mentalHealth.suggestion')">
          <el-input v-model="interviewForm.suggestion" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="interviewDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <AppButton @click="handleSaveInterview">{{ t('common.save') }}</AppButton>
      </template>
    </el-dialog>

    <!-- Crisis Dialog -->
    <el-dialog v-model="crisisDialogVisible" :title="t('mentalHealth.reportCrisis')" width="600px">
      <el-form :model="crisisForm" label-width="120px">
        <el-form-item :label="t('mentalHealth.studentNo')">
          <el-input v-model="crisisForm.studentNo" />
        </el-form-item>
        <el-form-item :label="t('mentalHealth.crisisType')">
          <el-select v-model="crisisForm.crisisType">
            <el-option label="自杀倾向" value="SUICIDE" />
            <el-option label="自伤行为" value="SELF_HARM" />
            <el-option label="虐待" value="ABUSE" />
            <el-option label="校园欺凌" value="BULLYING" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('mentalHealth.riskLevel')">
          <el-select v-model="crisisForm.riskLevel">
            <el-option label="低风险" value="LOW" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="高风险" value="HIGH" />
            <el-option label="极高风险" value="EXTREME" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('mentalHealth.description')">
          <el-input v-model="crisisForm.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item :label="t('mentalHealth.interventionMeasures')">
          <el-input v-model="crisisForm.interventionMeasures" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="crisisDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <AppButton @click="handleSaveCrisis">{{ t('common.save') }}</AppButton>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getRecordList, addRecord, updateRecord, getInterviewList, addInterview, getCrisisList, addCrisis, getRiskStudents } from '@/api/mental-health'

const { t } = useI18n()

const activeTab = ref('records')
const loading = ref(false)

// Records
const recordList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const recordDialogVisible = ref(false)
const isEdit = ref(false)
const recordForm = reactive({
  id: null,
  studentNo: '',
  assessmentType: 'SCL90',
  totalScore: 0,
  counselorNote: ''
})

// Interviews
const interviewList = ref([])
const interviewPage = ref(1)
const interviewSize = ref(10)
const interviewTotal = ref(0)
const interviewDialogVisible = ref(false)
const interviewForm = reactive({
  studentNo: '',
  interviewType: 'INITIAL',
  topic: '',
  content: '',
  suggestion: ''
})

// Crises
const crisisList = ref([])
const crisisPage = ref(1)
const crisisSize = ref(10)
const crisisTotal = ref(0)
const crisisDialogVisible = ref(false)
const crisisForm = reactive({
  studentNo: '',
  crisisType: 'OTHER',
  riskLevel: 'LOW',
  description: '',
  interventionMeasures: ''
})

// Risk students
const riskList = ref([])

const recordColumns = [
  { key: 'studentNo', label: () => t('mentalHealth.studentNo'), width: 120 },
  { key: 'recordDate', label: () => t('mentalHealth.recordDate'), width: 120 },
  { key: 'assessmentType', label: () => t('mentalHealth.assessmentType'), width: 120 },
  { key: 'totalScore', label: () => t('mentalHealth.totalScore'), width: 100 },
  { key: 'riskLevel', label: () => t('mentalHealth.riskLevel'), width: 100 },
  { key: 'followUpStatus', label: () => t('mentalHealth.followUpStatus'), width: 100 },
  { key: 'createTime', label: () => t('common.createTime'), width: 160 }
]

const interviewColumns = [
  { key: 'studentNo', label: () => t('mentalHealth.studentNo'), width: 120 },
  { key: 'interviewDate', label: () => t('mentalHealth.interviewDate'), width: 160 },
  { key: 'interviewType', label: () => t('mentalHealth.interviewType'), width: 120 },
  { key: 'topic', label: () => t('mentalHealth.topic'), minWidth: 150 },
  { key: 'interviewerNo', label: () => t('mentalHealth.interviewer'), width: 120 }
]

const crisisColumns = [
  { key: 'studentNo', label: () => t('mentalHealth.studentNo'), width: 120 },
  { key: 'crisisDate', label: () => t('mentalHealth.crisisDate'), width: 160 },
  { key: 'crisisType', label: () => t('mentalHealth.crisisType'), width: 120 },
  { key: 'riskLevel', label: () => t('mentalHealth.riskLevel'), width: 100 },
  { key: 'outcome', label: () => t('mentalHealth.outcome'), width: 100 }
]

const riskColumns = [
  { key: 'studentNo', label: () => t('mentalHealth.studentNo'), width: 120 },
  { key: 'recordDate', label: () => t('mentalHealth.recordDate'), width: 120 },
  { key: 'assessmentType', label: () => t('mentalHealth.assessmentType'), width: 120 },
  { key: 'totalScore', label: () => t('mentalHealth.totalScore'), width: 100 },
  { key: 'riskLevel', label: () => t('mentalHealth.riskLevel'), width: 100 }
]

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getRecordList({ page: page.value, size: size.value })
    recordList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadInterviews = async () => {
  loading.value = true
  try {
    const res = await getInterviewList({ page: interviewPage.value, size: interviewSize.value })
    interviewList.value = res.data?.records || []
    interviewTotal.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadCrises = async () => {
  loading.value = true
  try {
    const res = await getCrisisList({ page: crisisPage.value, size: crisisSize.value })
    crisisList.value = res.data?.records || []
    crisisTotal.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadRiskStudents = async () => {
  try {
    const res = await getRiskStudents({ riskLevel: 'WARNING', limit: 50 })
    riskList.value = res.data || []
  } catch (e) {
    // ignore
  }
}

const openRecordDialog = (row = null) => {
  if (row) {
    isEdit.value = true
    Object.assign(recordForm, row)
  } else {
    isEdit.value = false
    Object.assign(recordForm, { id: null, studentNo: '', assessmentType: 'SCL90', totalScore: 0, counselorNote: '' })
  }
  recordDialogVisible.value = true
}

const handleSaveRecord = async () => {
  try {
    if (isEdit.value) {
      await updateRecord(recordForm.id, recordForm)
    } else {
      await addRecord(recordForm)
    }
    ElMessage.success(t('common.success'))
    recordDialogVisible.value = false
    loadRecords()
  } catch (e) {
    ElMessage.error(t('common.operationFailed'))
  }
}

const openInterviewDialog = () => {
  Object.assign(interviewForm, { studentNo: '', interviewType: 'INITIAL', topic: '', content: '', suggestion: '' })
  interviewDialogVisible.value = true
}

const handleSaveInterview = async () => {
  try {
    await addInterview(interviewForm)
    ElMessage.success(t('common.success'))
    interviewDialogVisible.value = false
    loadInterviews()
  } catch (e) {
    ElMessage.error(t('common.operationFailed'))
  }
}

const openCrisisDialog = () => {
  Object.assign(crisisForm, { studentNo: '', crisisType: 'OTHER', riskLevel: 'LOW', description: '', interventionMeasures: '' })
  crisisDialogVisible.value = true
}

const handleSaveCrisis = async () => {
  try {
    await addCrisis(crisisForm)
    ElMessage.success(t('common.success'))
    crisisDialogVisible.value = false
    loadCrises()
  } catch (e) {
    ElMessage.error(t('common.operationFailed'))
  }
}

const getRiskType = (level) => {
  const map = { NORMAL: 'success', WARNING: 'warning', DANGER: 'danger' }
  return map[level] || 'info'
}

const getRiskLabel = (level) => {
  const map = { NORMAL: '正常', WARNING: '预警', DANGER: '危险' }
  return map[level] || level
}

const getCrisisRiskType = (level) => {
  const map = { LOW: 'info', MEDIUM: 'warning', HIGH: 'danger', EXTREME: 'danger' }
  return map[level] || 'info'
}

const getCrisisRiskLabel = (level) => {
  const map = { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', EXTREME: '极高风险' }
  return map[level] || level
}

const getOutcomeType = (outcome) => {
  const map = { RESOLVED: 'success', MONITORING: 'warning', ESCALATED: 'danger' }
  return map[outcome] || 'info'
}

const getOutcomeLabel = (outcome) => {
  const map = { RESOLVED: '已化解', MONITORING: '持续关注', ESCALED: '已上报' }
  return map[outcome] || outcome
}

const getInterviewTypeLabel = (type) => {
  const map = { INITIAL: '初次访谈', FOLLOW: '跟进访谈', CRISIS: '危机访谈' }
  return map[type] || type
}

onMounted(() => {
  loadRecords()
  loadInterviews()
  loadCrises()
  loadRiskStudents()
})
</script>

<style scoped>
.mental-health-page {
  padding: 16px;
}
.toolbar {
  margin-bottom: 16px;
}
</style>
