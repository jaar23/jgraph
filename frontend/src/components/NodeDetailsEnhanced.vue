<template>
  <div v-if="node" class="node-details">
    <div class="details-header">
      <h3>{{ getTypeLabel(node.type) }}</h3>
      <button @click="close" class="close-btn">×</button>
    </div>

    <div class="details-content">
      <!-- Node Information (existing details) -->
      <div class="detail-section">
        <div v-if="node.type === 'endpoint'">
          <div class="detail-row">
            <span class="label">HTTP Method:</span>
            <span class="value method-badge" :class="node.httpMethod">{{ node.httpMethod }}</span>
          </div>
          <div class="detail-row">
            <span class="label">Path:</span>
            <span class="value code">{{ node.path }}</span>
          </div>
          <div class="detail-row">
            <span class="label">Controller:</span>
            <span class="value code">{{ node.controllerClass }}</span>
          </div>
        </div>

        <div class="detail-row">
          <span class="label">{{ node.type === 'endpoint' ? 'Method' : 'Class' }}:</span>
          <span class="value code">{{ node.type === 'endpoint' ? node.methodName : node.className }}</span>
        </div>

        <div v-if="node.type !== 'endpoint'" class="detail-row">
          <span class="label">Method:</span>
          <span class="value code">{{ node.methodName }}</span>
        </div>

        <div class="detail-row">
          <span class="label">Return Type:</span>
          <span class="value code">{{ node.returnType }}</span>
        </div>

        <!-- File Location -->
        <div v-if="node.filePath" class="detail-row">
          <span class="label">Location:</span>
          <span class="value code small">{{ node.filePath }}:{{ node.lineNumber }}</span>
        </div>
      </div>

      <!-- Annotations Section -->
      <div class="annotations-section">
        <div class="section-header">
          <h4>Annotations</h4>
          <button @click="showAddAnnotation = !showAddAnnotation" class="add-btn">
            {{ showAddAnnotation ? '✕' : '+ Add' }}
          </button>
        </div>

        <!-- Add Annotation Form -->
        <div v-if="showAddAnnotation" class="annotation-form">
          <div class="form-group">
            <label>Note (supports Markdown):</label>
            <textarea
              v-model="newAnnotation.note"
              placeholder="Add your notes here... You can use **bold**, *italic*, `code`, etc."
              rows="4"
            ></textarea>
          </div>

          <div class="form-group">
            <label>Severity:</label>
            <select v-model="newAnnotation.severity">
              <option value="info">ℹ️ Info</option>
              <option value="warning">⚠️ Warning</option>
              <option value="error">🔴 Error</option>
              <option value="success">✅ Success</option>
            </select>
          </div>

          <div class="form-group">
            <label>Tags (comma-separated):</label>
            <input
              v-model="newAnnotation.tagsInput"
              type="text"
              placeholder="bug, performance, refactor"
            />
          </div>

          <div class="form-actions">
            <button @click="saveAnnotation" class="save-btn">Save</button>
            <button @click="cancelAnnotation" class="cancel-btn">Cancel</button>
          </div>
        </div>

        <!-- Display Existing Annotations -->
        <div v-if="annotations.notes.length > 0" class="annotations-list">
          <div
            v-for="note in annotations.notes"
            :key="note.id"
            class="annotation-item"
            :class="note.severity"
          >
            <div class="annotation-header">
              <span class="severity-badge">{{ getSeverityIcon(note.severity) }}</span>
              <span class="annotation-date">{{ formatDate(note.createdAt) }}</span>
              <button @click="deleteAnnotation(note.id)" class="delete-btn">🗑️</button>
            </div>
            <div class="annotation-content" v-html="renderMarkdown(note.text)"></div>
          </div>
        </div>

        <!-- Tags -->
        <div v-if="annotations.tags.length > 0" class="tags-container">
          <span
            v-for="tag in annotations.tags"
            :key="tag"
            class="tag"
          >
            {{ tag }}
            <button @click="removeTag(tag)" class="tag-remove">×</button>
          </span>
        </div>

        <div v-if="annotations.notes.length === 0 && annotations.tags.length === 0" class="no-annotations">
          No annotations yet. Click "+ Add" to create one.
        </div>
      </div>

      <!-- Javadoc if available -->
      <div v-if="node.javadoc" class="detail-section">
        <h4>Documentation</h4>
        <div class="javadoc">{{ node.javadoc }}</div>
      </div>
    </div>
  </div>

  <div v-else class="no-selection">
    <p>Click on a node to view details</p>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useAnalysisStore } from '../stores/analysisStore'
import { marked } from 'marked'

const store = useAnalysisStore()
const node = computed(() => store.selectedNode)

const showAddAnnotation = ref(false)
const newAnnotation = ref({
  note: '',
  severity: 'info',
  tagsInput: ''
})

const annotations = computed(() => {
  if (!node.value) return { notes: [], tags: [], customFields: {} }
  return store.getNodeAnnotation(node.value.id)
})

function getTypeLabel(type) {
  const labels = {
    endpoint: 'Endpoint',
    service: 'Service',
    repository: 'Repository'
  }
  return labels[type] || type
}

function getSeverityIcon(severity) {
  const icons = {
    info: 'ℹ️',
    warning: '⚠️',
    error: '🔴',
    success: '✅'
  }
  return icons[severity] || 'ℹ️'
}

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleString()
}

function renderMarkdown(text) {
  try {
    return marked.parse(text)
  } catch (e) {
    return text
  }
}

function saveAnnotation() {
  if (!node.value || !newAnnotation.value.note) return

  const tags = newAnnotation.value.tagsInput
    .split(',')
    .map(t => t.trim())
    .filter(t => t.length > 0)

  store.addNodeAnnotation(node.value.id, {
    note: newAnnotation.value.note,
    severity: newAnnotation.value.severity,
    tags
  })

  // Reset form
  newAnnotation.value = {
    note: '',
    severity: 'info',
    tagsInput: ''
  }
  showAddAnnotation.value = false
}

function cancelAnnotation() {
  newAnnotation.value = {
    note: '',
    severity: 'info',
    tagsInput: ''
  }
  showAddAnnotation.value = false
}

function deleteAnnotation(noteId) {
  if (node.value && confirm('Delete this annotation?')) {
    store.removeNodeAnnotation(node.value.id, noteId)
  }
}

function removeTag(tag) {
  if (node.value) {
    const ann = store.getNodeAnnotation(node.value.id)
    ann.tags = ann.tags.filter(t => t !== tag)
    store.nodeAnnotations.set(node.value.id, ann)
  }
}

function close() {
  store.setSelectedNode(null)
}

// Reset form when node changes
watch(node, () => {
  showAddAnnotation.value = false
})
</script>

<style scoped>
.node-details {
  height: 100%;
  overflow-y: auto;
  background-color: white;
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
  background-color: #f8f9fa;
  position: sticky;
  top: 0;
  z-index: 10;
}

.details-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.details-content {
  padding: 1rem;
}

.detail-section {
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row {
  margin-bottom: 1rem;
}

.label {
  display: block;
  font-weight: 600;
  color: #666;
  margin-bottom: 0.25rem;
  font-size: 0.875rem;
  text-transform: uppercase;
}

.value {
  display: block;
  color: #2c3e50;
}

.code {
  font-family: 'Courier New', monospace;
  background-color: #f5f5f5;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  display: inline-block;
  word-break: break-all;
}

.code.small {
  font-size: 0.75rem;
}

.method-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.875rem;
  color: white;
}

.method-badge.GET { background-color: #2ecc71; }
.method-badge.POST { background-color: #3498db; }
.method-badge.PUT { background-color: #f39c12; }
.method-badge.DELETE { background-color: #e74c3c; }
.method-badge.PATCH { background-color: #9b59b6; }

/* Annotations Section */
.annotations-section {
  margin-bottom: 1.5rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.add-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.add-btn:hover {
  background-color: #2980b9;
}

.annotation-form {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  color: #555;
}

.form-group textarea,
.form-group input,
.form-group select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
  font-family: inherit;
}

.form-group textarea {
  resize: vertical;
  font-family: 'Courier New', monospace;
}

.form-actions {
  display: flex;
  gap: 0.5rem;
}

.save-btn, .cancel-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.save-btn {
  background-color: #2ecc71;
  color: white;
}

.save-btn:hover {
  background-color: #27ae60;
}

.cancel-btn {
  background-color: #95a5a6;
  color: white;
}

.cancel-btn:hover {
  background-color: #7f8c8d;
}

.annotations-list {
  margin-bottom: 1rem;
}

.annotation-item {
  background-color: #f8f9fa;
  border-left: 4px solid #3498db;
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 4px;
}

.annotation-item.warning {
  border-left-color: #f39c12;
}

.annotation-item.error {
  border-left-color: #e74c3c;
}

.annotation-item.success {
  border-left-color: #2ecc71;
}

.annotation-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  color: #666;
}

.severity-badge {
  font-size: 1rem;
}

.annotation-date {
  flex: 1;
}

.delete-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  opacity: 0.6;
  padding: 0;
}

.delete-btn:hover {
  opacity: 1;
}

.annotation-content {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #2c3e50;
}

.annotation-content :deep(p) {
  margin: 0.5rem 0;
}

.annotation-content :deep(code) {
  background-color: #e8e8e8;
  padding: 0.2rem 0.4rem;
  border-radius: 3px;
  font-size: 0.85rem;
}

.annotation-content :deep(pre) {
  background-color: #2c3e50;
  color: #ecf0f1;
  padding: 1rem;
  border-radius: 4px;
  overflow-x: auto;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  background-color: #e8e8e8;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  color: #555;
}

.tag-remove {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
  padding: 0;
  color: #999;
}

.tag-remove:hover {
  color: #333;
}

.no-annotations {
  text-align: center;
  color: #999;
  padding: 2rem;
  font-style: italic;
}

.javadoc {
  background-color: #f8f9fa;
  padding: 0.75rem;
  border-radius: 4px;
  white-space: pre-wrap;
  font-size: 0.875rem;
  line-height: 1.6;
  color: #555;
}

.no-selection {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  font-style: italic;
}
</style>
