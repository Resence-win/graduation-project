<template>
  <div class="rich-text-editor">
    <Toolbar
      class="rich-text-toolbar"
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      mode="default"
    />
    <Editor
      class="rich-text-content"
      :model-value="editorHtml"
      :defaultConfig="editorConfig"
      mode="default"
      @onCreated="handleCreated"
      @update:modelValue="handleChange"
    />
    <div class="rich-text-actions">
      <el-upload
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleAttachmentChange"
        accept=".jpg,.jpeg,.png,.gif,.webp,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar"
      >
        <el-button size="small" :loading="uploading">上传附件</el-button>
      </el-upload>
      <span class="rich-text-tip">可输入文字，或上传图片、PDF、Word、Excel 等资料</span>
    </div>
    <div v-if="materials.length" class="material-list">
      <div
        v-for="(material, index) in materials"
        :key="`${material.url}-${index}`"
        class="material-card"
        :class="{ 'is-image': material.type === 'image' }"
      >
        <img v-if="material.type === 'image'" :src="material.url" :alt="material.name" />
        <div v-else class="material-file-icon">{{ getFileExtension(material.name) }}</div>
        <div class="material-info">
          <a :href="material.url" target="_blank" rel="noopener noreferrer">{{ material.name }}</a>
          <span>{{ material.type === 'image' ? '图片资料' : '附件资料' }}</span>
        </div>
        <el-button size="small" link type="danger" @click="removeMaterial(index)">移除</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import '@wangeditor/editor/dist/css/style.css'
import { computed, onBeforeUnmount, shallowRef, ref, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { ElMessage } from 'element-plus'
import { uploadAttendanceApplicationMaterial } from '@/api/attendance'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容'
  }
})

const emit = defineEmits(['update:modelValue'])
const editorRef = shallowRef()
const uploading = ref(false)
const editorHtml = ref('')
const materials = ref([])
const lastEmittedValue = ref('')

const toolbarConfig = {
  excludeKeys: ['group-video', 'insertVideo', 'uploadVideo', 'fullScreen']
}

const editorConfig = computed(() => ({
  placeholder: props.placeholder,
  MENU_CONF: {
    uploadImage: {
      async customUpload(file) {
        const uploaded = await uploadMaterial(file)
        appendMaterial(uploaded)
        ElMessage.success('资料上传成功')
      }
    }
  }
}))

watch(
  () => props.modelValue,
  (value) => {
    if (value === lastEmittedValue.value) {
      return
    }
    const parsed = parseReasonHtml(value)
    editorHtml.value = parsed.bodyHtml
    materials.value = parsed.materials
  },
  { immediate: true }
)

const handleCreated = (editor) => {
  editorRef.value = editor
}

const handleChange = (value) => {
  editorHtml.value = value
  emitComposedValue()
}

const handleAttachmentChange = async (uploadFile) => {
  if (!uploadFile?.raw) {
    return
  }
  try {
    uploading.value = true
    const uploaded = await uploadMaterial(uploadFile.raw)
    appendMaterial(uploaded)
    ElMessage.success('资料上传成功')
  } catch (error) {
    console.error('资料上传失败:', error)
    ElMessage.error(error.response?.data?.msg || error.message || '资料上传失败')
  } finally {
    uploading.value = false
  }
}

const uploadMaterial = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  const res = await uploadAttendanceApplicationMaterial(formData)
  return res.data
}

const appendMaterial = (uploaded) => {
  materials.value.push({
    name: uploaded.name || uploaded.url || '申报资料',
    url: normalizeUploadUrl(uploaded.url),
    type: uploaded.type === 'image' ? 'image' : 'file'
  })
  emitComposedValue()
}

const removeMaterial = (index) => {
  materials.value.splice(index, 1)
  emitComposedValue()
}

const emitComposedValue = () => {
  const composed = composeReasonHtml(editorHtml.value, materials.value)
  lastEmittedValue.value = composed
  emit('update:modelValue', composed)
}

const parseReasonHtml = (html) => {
  if (!html) {
    return { bodyHtml: '', materials: [] }
  }
  const wrapper = document.createElement('div')
  wrapper.innerHTML = html
  const parsedMaterials = Array.from(wrapper.querySelectorAll('[data-material-card="true"]')).map((node) => {
    const link = node.tagName.toLowerCase() === 'a' ? node : node.querySelector('a')
    const image = node.querySelector('img')
    return {
      name: node.dataset.materialName || link?.textContent?.trim() || image?.alt || '申报资料',
      url: normalizeUploadUrl(node.dataset.materialUrl || link?.getAttribute('href') || image?.getAttribute('src') || ''),
      type: node.dataset.materialType === 'image' || image ? 'image' : 'file'
    }
  }).filter(material => material.url)

  wrapper.querySelectorAll('[data-material-list="true"]').forEach(node => node.remove())
  return {
    bodyHtml: wrapper.innerHTML,
    materials: parsedMaterials
  }
}

const composeReasonHtml = (bodyHtml, materialList) => {
  const content = bodyHtml || ''
  if (!materialList.length) {
    return content
  }
  const materialHtml = materialList.map(renderMaterialHtml).join('')
  return `${content}<div class="application-material-list" data-material-list="true">${materialHtml}</div>`
}

const renderMaterialHtml = (material) => {
  const name = escapeHtml(material.name || '申报资料')
  const url = escapeHtml(material.url || '')
  const type = material.type === 'image' ? 'image' : 'file'
  const preview = type === 'image'
    ? `<img src="${url}" alt="${name}" />`
    : `<span class="application-material-ext">${escapeHtml(getFileExtension(material.name))}</span>`
  return `<a class="application-material-card application-material-card--${type}" data-material-card="true" data-material-type="${type}" data-material-name="${name}" data-material-url="${url}" href="${url}" target="_blank" rel="noopener noreferrer">${preview}<span class="application-material-name">${name}</span></a>`
}

const normalizeUploadUrl = (url) => {
  if (url?.startsWith('/upload/')) {
    return `/api${url}`
  }
  return url || ''
}

const escapeHtml = (value) => {
  return String(value || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

const getFileExtension = (name) => {
  const extension = String(name || '').split('.').pop()
  return extension && extension !== name ? extension.slice(0, 5).toUpperCase() : 'FILE'
}

onBeforeUnmount(() => {
  editorRef.value?.destroy()
})
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
}

.rich-text-toolbar {
  border-bottom: 1px solid #e4e7ed;
}

.rich-text-content {
  min-height: 180px;
  text-align: left;
}

.rich-text-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-top: 1px solid #e4e7ed;
  background: #fafafa;
}

.rich-text-tip {
  color: #909399;
  font-size: 12px;
}

.material-list {
  display: grid;
  gap: 8px;
  padding: 10px 12px;
  border-top: 1px solid #e4e7ed;
}

.material-card {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}

.material-card img {
  width: 52px;
  height: 52px;
  border-radius: 4px;
  object-fit: cover;
  background: #f5f7fa;
}

.material-file-icon {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  border-radius: 4px;
  background: #eef5ff;
  color: #409eff;
  font-size: 12px;
  font-weight: 700;
}

.material-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  text-align: left;
}

.material-info a {
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.material-info span {
  color: #909399;
  font-size: 12px;
}
</style>
