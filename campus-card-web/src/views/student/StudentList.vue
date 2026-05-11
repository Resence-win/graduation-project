<template>
  <div class="student-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">新增学生</el-button>
            <el-button type="success" @click="handleExport">导出学生</el-button>
            <el-button type="warning" @click="dialogImportVisible = true">导入学生</el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="学号">
          <el-input v-model="searchForm.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column label="ID" width="80">
          <template #default="{ $index }">
            {{ $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="college" label="学院" width="150" />
        <el-table-column prop="major" label="专业" width="150" />
        <el-table-column prop="className" label="班级" width="100" />
        <el-table-column prop="teacherName" label="负责老师" width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="考勤模式" width="110">
          <template #default="{ row }">
            <el-tag :type="row.attendanceMode === 'INTERNSHIP' ? 'warning' : 'success'">
              {{ row.attendanceMode === 'INTERNSHIP' ? '校外实习' : '在校考勤' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="考勤状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.attendanceStatus === 'LEAVE' ? 'warning' : row.attendanceStatus === 'INTERNSHIP' ? 'primary' : 'success'">
              {{ getAttendanceStatusLabel(row.attendanceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
    
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院" prop="college">
          <el-select
            v-model="form.college"
            placeholder="请选择学院"
            filterable
            style="width: 100%"
            @change="handleCollegeChange"
          >
            <el-option
              v-for="college in collegeOptions"
              :key="college"
              :label="college"
              :value="college"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-select
            v-model="form.major"
            placeholder="请选择专业"
            filterable
            style="width: 100%"
            :disabled="!form.college"
          >
            <el-option
              v-for="major in majorOptions"
              :key="major"
              :label="major"
              :value="major"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input v-model="form.className" placeholder="请输入班级" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="负责老师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="请选择负责老师" filterable clearable>
            <el-option
              v-for="teacher in teacherOptions"
              :key="teacher.id"
              :label="`${teacher.name}（${teacher.teacherNo}）`"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考勤模式" prop="attendanceMode">
          <el-select v-model="form.attendanceMode" placeholder="请选择考勤模式">
            <el-option label="在校考勤" value="CAMPUS" />
            <el-option label="校外实习" value="INTERNSHIP" />
          </el-select>
        </el-form-item>
        <el-form-item label="考勤状态" prop="attendanceStatus">
          <el-select v-model="form.attendanceStatus" placeholder="请选择考勤状态">
            <el-option label="在校" value="ON_CAMPUS" />
            <el-option label="外出实习" value="INTERNSHIP" />
            <el-option label="已请假" value="LEAVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="实习单位" prop="internshipCompany" v-if="form.attendanceMode === 'INTERNSHIP'">
          <el-input v-model="form.internshipCompany" placeholder="请输入实习单位" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 导入学生对话框 -->
    <el-dialog
      v-model="dialogImportVisible"
      title="导入学生"
      width="500px"
    >
      <div class="mb-4">
        <el-button type="info" @click="handleDownloadTemplate">
          <el-icon><download /></el-icon>
          下载导入模板
        </el-button>
        <div class="text-xs text-gray-500 mt-2">
          模板包含导入字段：学号、姓名、学院、专业、班级；手机号和性别可后续补充，学生端业务操作前必须完整
        </div>
      </div>
      <el-form label-width="80px">
        <el-form-item label="导入文件">
          <el-upload
            class="upload-demo"
            ref="uploadRef"
            action=""
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            :limit="1"
            accept=".xlsx, .xls"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传Excel文件，使用模板文件格式
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogImportVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentList, addStudent, updateStudent, deleteStudent, exportStudents, importStudents, downloadImportTemplate } from '@/api/student'
import { getTeacherList } from '@/api/teacher'
import { resetPassword } from '@/api/admin'
import { getCollegeMajorOptions } from '@/api/collegeMajor'

const formRef = ref(null)
const dialogVisible = ref(false)
const dialogTitle = ref('新增学生')
const tableData = ref([])
const teacherOptions = ref([])
const collegeMajorOptions = ref({})
const dialogImportVisible = ref(false)
const uploadRef = ref(null)
const fileList = ref([])
let selectedFile = null

const searchForm = reactive({
  studentNo: '',
  name: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  studentNo: '',
  name: '',
  gender: '',
  college: '',
  major: '',
  className: '',
  phone: '',
  teacherId: null,
  attendanceMode: 'CAMPUS',
  attendanceStatus: 'ON_CAMPUS',
  internshipCompany: '',
  password: ''
})

const rules = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  college: [
    { required: true, message: '请选择学院', trigger: 'change' }
  ],
  major: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ],
  className: [
    { required: true, message: '请输入班级', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择负责老师', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const collegeOptions = computed(() => Object.keys(collegeMajorOptions.value))
const majorOptions = computed(() => collegeMajorOptions.value[form.college] || [])

const loadCollegeMajorOptions = async () => {
  try {
    const res = await getCollegeMajorOptions()
    if (res.code === 0) {
      collegeMajorOptions.value = res.data || {}
    }
  } catch (error) {
    console.error('加载学院专业失败:', error)
  }
}

const handleCollegeChange = () => {
  form.major = ''
}

const loadTeachers = async () => {
  try {
    const res = await getTeacherList({
      page: 1,
      size: 1000
    })
    if (res.code === 0) {
      teacherOptions.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载教师列表失败:', error)
  }
}

const loadData = async () => {
  try {
    const res = await getStudentList({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    })
    if (res.code === 0) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.studentNo = ''
  searchForm.name = ''
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增学生'
  dialogVisible.value = true
  Object.assign(form, {
    id: null,
    studentNo: '',
    name: '',
    gender: '',
    college: '',
    major: '',
    className: '',
    phone: '',
    teacherId: null,
    attendanceMode: 'CAMPUS',
    attendanceStatus: 'ON_CAMPUS',
    internshipCompany: '',
    password: ''
  })
}

const getAttendanceStatusLabel = (status) => {
  const map = {
    ON_CAMPUS: '在校',
    INTERNSHIP: '外出实习',
    LEAVE: '已请假'
  }
  return map[status] || '在校'
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑学生'
  dialogVisible.value = true
  Object.assign(form, row)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该学生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteStudent(row.id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm('确定要将该学生的密码重置为123456吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await resetPassword({
      username: row.studentNo,
      newPassword: '123456'
    })
    if (res.code === 0) {
      ElMessage.success('密码重置成功')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    let res
    if (form.id) {
      res = await updateStudent(form)
    } else {
      res = await addStudent(form)
    }
    
    if (res.code === 0) {
      ElMessage.success(form.id ? '更新成功' : '新增成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
    }
  }
}

const handleSizeChange = (val) => {
  pagination.size = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  loadData()
}

const handleFileChange = (file) => {
  selectedFile = file.raw
  fileList.value = [file]
}

const handleExport = async () => {
  try {
    const res = await exportStudents()
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `学生信息_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const handleImport = async () => {
  if (!selectedFile) {
    ElMessage.warning('请选择文件')
    return
  }
  
  try {
    const res = await importStudents(selectedFile)
    if (res.code === 0) {
      const result = res.data || {}
      const failureReasons = result.failureReasons || []
      const summary = [
        `总数：${result.totalCount || 0}`,
        `成功数：${result.successCount || 0}`,
        `跳过数：${result.skippedCount || 0}`,
        `失败数：${result.failedCount || 0}`
      ].join('<br>')
      const reasonHtml = failureReasons.length
        ? `<br><br><strong>失败原因：</strong><br>${failureReasons.map(escapeHtml).join('<br>')}`
        : '<br><br><strong>失败原因：</strong>无'

      ElMessage.success('导入完成')
      await ElMessageBox.alert(`${summary}${reasonHtml}`, '导入结果', {
        confirmButtonText: '知道了',
        dangerouslyUseHTMLString: true
      })
      dialogImportVisible.value = false
      fileList.value = []
      selectedFile = null
      loadData()
    }
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败')
  }
}

const escapeHtml = (value) => {
  return String(value || '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

const handleDownloadTemplate = async () => {
  try {
    const res = await downloadImportTemplate()
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `学生导入模板.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败')
  }
}

onMounted(() => {
  loadCollegeMajorOptions()
  loadTeachers()
  loadData()
})
</script>

<style scoped>
.student-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-pagination {
  display: flex;
}
</style>
