<template>
  <div class="employment-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane :label="t('employment.records')" name="records">
        <div class="toolbar">
          <AppButton @click="openEmploymentDialog">
            <el-icon><Plus /></el-icon>
            {{ t('employment.addRecord') }}
          </AppButton>
          <el-input v-model="searchKeyword" :placeholder="t('employment.searchPlaceholder')" style="width: 240px; margin-left: 12px" clearable @change="loadEmployments">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <AppTable :columns="employmentColumns" :rows="employmentList" :loading="loading" :pagination="true"
          v-model:page="page" v-model:page-size="size" :total="total" @page-change="loadEmployments">
          <template #cell-employmentStatus="{ row }">
            <el-tag :type="getStatusType(row.employmentStatus)">{{ getStatusLabel(row.employmentStatus) }}</el-tag>
          </template>
          <template #cell-actions="{ row }">
            <button class="app-table-action" @click="openEmploymentDialog(row)">{{ t('common.edit') }}</button>
            <button class="app-table-action" @click="handleDelete(row.id)">{{ t('common.delete') }}</button>
          </template>
        </AppTable>
      </el-tab-pane>

      <el-tab-pane :label="t('employment.statistics')" name="statistics">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <span>{{ t('employment.rateByYear') }}</span>
              </template>
              <div ref="yearChartRef" style="height: 300px"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <span>{{ t('employment.distributionByIndustry') }}</span>
              </template>
              <div ref="industryChartRef" style="height: 300px"></div>
            </el-card>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <span>{{ t('employment.distributionByCompanyType') }}</span>
              </template>
              <div ref="companyTypeChartRef" style="height: 300px"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <span>{{ t('employment.employmentRate') }}</span>
              </template>
              <div ref="rateChartRef" style="height: 300px"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane :label="t('employment.alumni')" name="alumni">
        <div class="toolbar">
          <AppButton @click="openAlumniDialog">
            <el-icon><Plus /></el-icon>
            {{ t('employment.addAlumni') }}
          </AppButton>
        </div>

        <AppTable :columns="alumniColumns" :rows="alumniList" :loading="loading" :pagination="true"
          v-model:page="alumniPage" v-model:page-size="alumniSize" :total="alumniTotal" @page-change="loadAlumni">
          <template #cell-actions="{ row }">
            <button class="app-table-action" @click="openAlumniDialog(row)">{{ t('common.edit') }}</button>
            <button class="app-table-action" @click="handleDeleteAlumni(row.id)">{{ t('common.delete') }}</button>
          </template>
        </AppTable>
      </el-tab-pane>
    </el-tabs>

    <!-- Employment Dialog -->
    <el-dialog v-model="employmentDialogVisible" :title="isEdit ? t('common.edit') : t('employment.addRecord')" width="650px">
      <el-form :model="employmentForm" label-width="140px">
        <el-form-item :label="t('employment.studentNo')">
          <el-input v-model="employmentForm.studentNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="t('employment.companyName')">
          <el-input v-model="employmentForm.companyName" />
        </el-form-item>
        <el-form-item :label="t('employment.position')">
          <el-input v-model="employmentForm.position" />
        </el-form-item>
        <el-form-item :label="t('employment.industry')">
          <el-select v-model="employmentForm.industry" style="width: 100%">
            <el-option label="IT/互联网" value="IT" />
            <el-option label="金融" value="FINANCE" />
            <el-option label="教育" value="EDUCATION" />
            <el-option label="医疗" value="HEALTHCARE" />
            <el-option label="制造业" value="MANUFACTURING" />
            <el-option label="零售/商贸" value="RETAIL" />
            <el-option label="房地产" value="REAL_ESTATE" />
            <el-option label="物流/运输" value="LOGISTICS" />
            <el-option label="传媒/广告" value="MEDIA" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('employment.companyType')">
          <el-select v-model="employmentForm.companyType" style="width: 100%">
            <el-option label="国有企业" value="STATE_OWNED" />
            <el-option label="民营企业" value="PRIVATE" />
            <el-option label="外资企业" value="FOREIGN" />
            <el-option label="合资企业" value="JOINT" />
            <el-option label="上市公司" value="LISTED" />
            <el-option label="中小企业" value="SME" />
            <el-option label="政府/事业单位" value="GOVERNMENT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('employment.companyAddress')">
          <el-input v-model="employmentForm.companyAddress" />
        </el-form-item>
        <el-form-item :label="t('employment.salary')">
          <el-input v-model="employmentForm.salary" />
        </el-form-item>
        <el-form-item :label="t('employment.employmentStatus')">
          <el-select v-model="employmentForm.employmentStatus" style="width: 100%">
            <el-option label="待就业" value="UNEMPLOYED" />
            <el-option label="已就业" value="EMPLOYED" />
            <el-option label="升学" value="CONTINUING_EDUCATION" />
            <el-option label="创业" value="STARTUP" />
            <el-option label="出国" value="OVERSEAS" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('employment.employmentDate')">
          <el-date-picker v-model="employmentForm.employmentDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="t('employment.contractType')">
          <el-select v-model="employmentForm.contractType" style="width: 100%">
            <el-option label="劳动合同" value="LABOR_CONTRACT" />
            <el-option label="劳务派遣" value="LABOR_DISPATCH" />
            <el-option label="实习协议" value="INTERNSHIP" />
            <el-option label="无合同" value="NONE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('employment.remarks')">
          <el-input v-model="employmentForm.remarks" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="employmentDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <AppButton @click="handleSaveEmployment">{{ t('common.save') }}</AppButton>
      </template>
    </el-dialog>

    <!-- Alumni Dialog -->
    <el-dialog v-model="alumniDialogVisible" :title="isAlumniEdit ? t('common.edit') : t('employment.addAlumni')" width="650px">
      <el-form :model="alumniForm" label-width="140px">
        <el-form-item :label="t('employment.studentNo')">
          <el-input v-model="alumniForm.studentNo" :disabled="isAlumniEdit" />
        </el-form-item>
        <el-form-item :label="t('employment.graduationYear')">
          <el-input-number v-model="alumniForm.graduationYear" :min="2000" :max="2030" />
        </el-form-item>
        <el-form-item :label="t('employment.currentCompany')">
          <el-input v-model="alumniForm.currentCompany" />
        </el-form-item>
        <el-form-item :label="t('employment.currentPosition')">
          <el-input v-model="alumniForm.currentPosition" />
        </el-form-item>
        <el-form-item :label="t('employment.industry')">
          <el-select v-model="alumniForm.industry" style="width: 100%">
            <el-option label="IT/互联网" value="IT" />
            <el-option label="金融" value="FINANCE" />
            <el-option label="教育" value="EDUCATION" />
            <el-option label="医疗" value="HEALTHCARE" />
            <el-option label="制造业" value="MANUFACTURING" />
            <el-option label="零售/商贸" value="RETAIL" />
            <el-option label="房地产" value="REAL_ESTATE" />
            <el-option label="物流/运输" value="LOGISTICS" />
            <el-option label="传媒/广告" value="MEDIA" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('employment.jobLevel')">
          <el-select v-model="alumniForm.jobLevel" style="width: 100%">
            <el-option label="普通员工" value="STAFF" />
            <el-option label="主管" value="SUPERVISOR" />
            <el-option label="经理" value="MANAGER" />
            <el-option label="总监" value="DIRECTOR" />
            <el-option label="高管" value="EXECUTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('employment.annualSalary')">
          <el-input v-model="alumniForm.annualSalary" />
        </el-form-item>
        <el-form-item :label="t('employment.workLocation')">
          <el-input v-model="alumniForm.workLocation" />
        </el-form-item>
        <el-form-item :label="t('employment.contactPhone')">
          <el-input v-model="alumniForm.contactPhone" />
        </el-form-item>
        <el-form-item :label="t('employment.email')">
          <el-input v-model="alumniForm.email" />
        </el-form-item>
        <el-form-item :label="t('employment.linkedIn')">
          <el-input v-model="alumniForm.linkedIn" />
        </el-form-item>
        <el-form-item :label="t('employment.alumniAssociation')">
          <el-input v-model="alumniForm.alumniAssociation" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item :label="t('employment.contributions')">
          <el-input v-model="alumniForm.contributions" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="alumniDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <AppButton @click="handleSaveAlumni">{{ t('common.save') }}</AppButton>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { Plus, Search } from '@element-plus/icons-vue'
import { getEmploymentList, createEmployment, updateEmployment, deleteEmployment, getStatisticsByYear, getStatisticsByIndustry, getStatisticsByCompanyType, getEmploymentRate, getAlumniList, createAlumni, updateAlumni, deleteAlumni } from '@/api/employment'

const { t } = useI18n()

const activeTab = ref('records')
const loading = ref(false)
const searchKeyword = ref('')

// Employment
const employmentList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const employmentDialogVisible = ref(false)
const isEdit = ref(false)
const employmentForm = reactive({
  id: null,
  studentNo: '',
  companyName: '',
  position: '',
  industry: 'IT',
  companyType: 'PRIVATE',
  companyAddress: '',
  salary: '',
  employmentStatus: 'EMPLOYED',
  employmentDate: '',
  contractType: 'LABOR_CONTRACT',
  remarks: ''
})

// Alumni
const alumniList = ref([])
const alumniPage = ref(1)
const alumniSize = ref(10)
const alumniTotal = ref(0)
const alumniDialogVisible = ref(false)
const isAlumniEdit = ref(false)
const alumniForm = reactive({
  id: null,
  studentNo: '',
  graduationYear: new Date().getFullYear(),
  currentCompany: '',
  currentPosition: '',
  industry: 'IT',
  jobLevel: 'STAFF',
  annualSalary: '',
  workLocation: '',
  contactPhone: '',
  email: '',
  linkedIn: '',
  alumniAssociation: '',
  contributions: ''
})

// Charts
const yearChartRef = ref(null)
const industryChartRef = ref(null)
const companyTypeChartRef = ref(null)
const rateChartRef = ref(null)

const employmentColumns = [
  { key: 'studentNo', label: () => t('employment.studentNo'), width: 120 },
  { key: 'studentName', label: () => t('employment.studentName'), width: 100 },
  { key: 'className', label: () => t('employment.className'), width: 120 },
  { key: 'companyName', label: () => t('employment.companyName'), minWidth: 150 },
  { key: 'position', label: () => t('employment.position'), width: 120 },
  { key: 'industry', label: () => t('employment.industry'), width: 100 },
  { key: 'employmentStatus', label: () => t('employment.employmentStatus'), width: 120 },
  { key: 'employmentDate', label: () => t('employment.employmentDate'), width: 120 },
  { key: 'createTime', label: () => t('common.createTime'), width: 160 },
  { key: 'actions', label: () => t('common.actions'), width: 150, fixed: 'right' }
]

const alumniColumns = [
  { key: 'studentNo', label: () => t('employment.studentNo'), width: 120 },
  { key: 'studentName', label: () => t('employment.studentName'), width: 100 },
  { key: 'graduationYear', label: () => t('employment.graduationYear'), width: 120 },
  { key: 'currentCompany', label: () => t('employment.currentCompany'), minWidth: 150 },
  { key: 'currentPosition', label: () => t('employment.currentPosition'), width: 120 },
  { key: 'industry', label: () => t('employment.industry'), width: 100 },
  { key: 'jobLevel', label: () => t('employment.jobLevel'), width: 100 },
  { key: 'workLocation', label: () => t('employment.workLocation'), width: 120 },
  { key: 'actions', label: () => t('common.actions'), width: 150, fixed: 'right' }
]

const loadEmployments = async () => {
  loading.value = true
  try {
    const res = await getEmploymentList({ page: page.value, size: size.value, keyword: searchKeyword.value })
    employmentList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadAlumni = async () => {
  loading.value = true
  try {
    const res = await getAlumniList({ page: alumniPage.value, size: alumniSize.value })
    alumniList.value = res.data?.records || []
    alumniTotal.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const [yearRes, industryRes, companyTypeRes, rateRes] = await Promise.all([
      getStatisticsByYear({}),
      getStatisticsByIndustry({}),
      getStatisticsByCompanyType({}),
      getEmploymentRate({})
    ])

    nextTick(() => {
      if (yearChartRef.value) {
        const chart = echarts.init(yearChartRef.value)
        const yearData = yearRes.data || []
        chart.setOption({
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: yearData.map(d => d.year) },
          yAxis: { type: 'value' },
          series: [{ data: yearData.map(d => d.count), type: 'bar', itemStyle: { color: '#409eff' } }]
        })
      }

      if (industryChartRef.value) {
        const chart = echarts.init(industryChartRef.value)
        const industryData = industryRes.data || []
        chart.setOption({
          tooltip: { trigger: 'item' },
          series: [{ type: 'pie', radius: '60%', data: industryData.map(d => ({ name: d.industry, value: d.count })) }]
        })
      }

      if (companyTypeChartRef.value) {
        const chart = echarts.init(companyTypeChartRef.value)
        const companyData = companyTypeRes.data || []
        chart.setOption({
          tooltip: { trigger: 'item' },
          series: [{ type: 'pie', radius: '60%', data: companyData.map(d => ({ name: d.companyType, value: d.count })) }]
        })
      }

      if (rateChartRef.value) {
        const chart = echarts.init(rateChartRef.value)
        const rateData = rateRes.data || []
        chart.setOption({
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: rateData.map(d => d.year) },
          yAxis: { type: 'value', max: 100 },
          series: [{ data: rateData.map(d => d.rate), type: 'line', itemStyle: { color: '#67c23a' }, areaStyle: { color: 'rgba(103,194,58,0.2)' } }]
        })
      }
    })
  } catch (e) {
    console.error('Failed to load statistics', e)
  }
}

const openEmploymentDialog = (row = null) => {
  if (row) {
    isEdit.value = true
    Object.assign(employmentForm, row)
  } else {
    isEdit.value = false
    Object.assign(employmentForm, {
      id: null, studentNo: '', companyName: '', position: '', industry: 'IT', companyType: 'PRIVATE',
      companyAddress: '', salary: '', employmentStatus: 'EMPLOYED', employmentDate: '', contractType: 'LABOR_CONTRACT', remarks: ''
    })
  }
  employmentDialogVisible.value = true
}

const handleSaveEmployment = async () => {
  try {
    if (isEdit.value) {
      await updateEmployment(employmentForm.id, employmentForm)
    } else {
      await createEmployment(employmentForm)
    }
    ElMessage.success(t('common.success'))
    employmentDialogVisible.value = false
    loadEmployments()
  } catch (e) {
    ElMessage.error(t('common.operationFailed'))
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('common.confirmDelete'), t('common.warning'), { type: 'warning' })
    await deleteEmployment(id)
    ElMessage.success(t('common.success'))
    loadEmployments()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(t('common.operationFailed'))
    }
  }
}

const openAlumniDialog = (row = null) => {
  if (row) {
    isAlumniEdit.value = true
    Object.assign(alumniForm, row)
  } else {
    isAlumniEdit.value = false
    Object.assign(alumniForm, {
      id: null, studentNo: '', graduationYear: new Date().getFullYear(), currentCompany: '', currentPosition: '',
      industry: 'IT', jobLevel: 'STAFF', annualSalary: '', workLocation: '', contactPhone: '', email: '',
      linkedIn: '', alumniAssociation: '', contributions: ''
    })
  }
  alumniDialogVisible.value = true
}

const handleSaveAlumni = async () => {
  try {
    if (isAlumniEdit.value) {
      await updateAlumni(alumniForm.id, alumniForm)
    } else {
      await createAlumni(alumniForm)
    }
    ElMessage.success(t('common.success'))
    alumniDialogVisible.value = false
    loadAlumni()
  } catch (e) {
    ElMessage.error(t('common.operationFailed'))
  }
}

const handleDeleteAlumni = async (id) => {
  try {
    await ElMessageBox.confirm(t('common.confirmDelete'), t('common.warning'), { type: 'warning' })
    await deleteAlumni(id)
    ElMessage.success(t('common.success'))
    loadAlumni()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(t('common.operationFailed'))
    }
  }
}

const getStatusType = (status) => {
  const map = { EMPLOYED: 'success', UNEMPLOYED: 'warning', CONTINUING_EDUCATION: 'info', STARTUP: 'primary', OVERSEAS: 'danger' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { EMPLOYED: '已就业', UNEMPLOYED: '待就业', CONTINUING_EDUCATION: '升学', STARTUP: '创业', OVERSEAS: '出国' }
  return map[status] || status
}

onMounted(() => {
  loadEmployments()
  loadAlumni()
})
</script>

<style scoped>
.employment-page {
  padding: 16px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}
</style>
