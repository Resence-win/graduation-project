<template>
  <div class="rich-text-preview">
    <div v-if="bodyHtml" class="rich-text-preview-body" v-html="bodyHtml"></div>
    <div v-if="materials.length" class="application-material-list">
      <a
        v-for="(material, index) in materials"
        :key="`${material.url}-${index}`"
        class="application-material-card"
        :class="`application-material-card--${material.type}`"
        :href="material.url"
        target="_blank"
        rel="noopener noreferrer"
      >
        <img v-if="material.type === 'image'" :src="material.url" :alt="material.name" />
        <span v-else class="application-material-ext">{{ getFileExtension(material.name) }}</span>
        <span class="application-material-name">{{ material.name }}</span>
      </a>
    </div>
    <el-empty v-if="!bodyHtml && !materials.length" description="未填写说明" :image-size="80" />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  html: {
    type: String,
    default: ''
  }
})

const parsedContent = computed(() => parseReasonHtml(props.html))
const bodyHtml = computed(() => parsedContent.value.bodyHtml)
const materials = computed(() => parsedContent.value.materials)

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
    bodyHtml: wrapper.innerHTML.trim(),
    materials: parsedMaterials
  }
}

const normalizeUploadUrl = (url) => {
  if (url?.startsWith('/upload/')) {
    return `/api${url}`
  }
  return url || ''
}

const getFileExtension = (name) => {
  const extension = String(name || '').split('.').pop()
  return extension && extension !== name ? extension.slice(0, 5).toUpperCase() : 'FILE'
}
</script>

<style scoped>
.rich-text-preview {
  max-height: 60vh;
  overflow: auto;
  line-height: 1.7;
  text-align: left;
  word-break: break-word;
}

.rich-text-preview-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

.rich-text-preview-body :deep(a) {
  color: #409eff;
}

.application-material-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.application-material-card {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  color: #303133;
  background: #fff;
  text-decoration: none;
}

.application-material-card:hover {
  border-color: #409eff;
  color: #409eff;
}

.application-material-card img {
  width: 54px;
  height: 54px;
  flex: 0 0 auto;
  object-fit: cover;
  border-radius: 4px;
  background: #f5f7fa;
}

.application-material-ext {
  width: 54px;
  height: 54px;
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

.application-material-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
