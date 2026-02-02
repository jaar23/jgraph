<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Export / Import Analysis</h2>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>
      
      <div class="modal-body">
        <div class="section">
          <h3>Export</h3>
          <p class="description">
            Export your analysis data including annotations, LLM insights, and chat history.
          </p>
          
          <div class="export-options">
            <label class="option">
              <input type="radio" v-model="exportType" value="full" />
              <span>Full Export (Everything)</span>
            </label>
            
            <label class="option">
              <input type="radio" v-model="exportType" value="annotations" />
              <span>Annotations Only</span>
            </label>
            
            <label class="option">
              <input type="radio" v-model="exportType" value="selected" />
              <span>Selected Nodes Only</span>
            </label>
          </div>
          
          <div class="stats" v-if="stats">
            <div class="stat-item">
              <span class="label">Nodes:</span>
              <span class="value">{{ stats.totalNodes }}</span>
            </div>
            <div class="stat-item">
              <span class="label">Annotations:</span>
              <span class="value">{{ stats.annotatedNodes }}</span>
            </div>
            <div class="stat-item">
              <span class="label">LLM Analyses:</span>
              <span class="value">{{ stats.llmAnalyses }}</span>
            </div>
          </div>
          
          <button @click="handleExport" class="btn btn-primary">
            <span class="icon">💾</span>
            Export to JSON
          </button>
        </div>
        
        <div class="divider"></div>
        
        <div class="section">
          <h3>Import</h3>
          <p class="description">
            Import a previously exported analysis file. This will merge annotations and LLM data.
          </p>
          
          <div class="import-options">
            <label class="option">
              <input type="radio" v-model="importMode" value="merge" />
              <span>Merge (Keep existing + add new)</span>
            </label>
            
            <label class="option">
              <input type="radio" v-model="importMode" value="replace" />
              <span>Replace (Overwrite all user data)</span>
            </label>
          </div>
          
          <div class="file-input-area">
            <input 
              ref="fileInput"
              type="file" 
              accept=".json"
              @change="handleFileSelect"
              id="import-file"
              style="display: none;"
            />
            <label for="import-file" class="btn btn-secondary">
              <span class="icon">📁</span>
              Choose File
            </label>
            <span v-if="selectedFile" class="file-name">{{ selectedFile.name }}</span>
          </div>
          
          <button 
            v-if="selectedFile"
            @click="handleImport" 
            class="btn btn-primary"
            :disabled="importing"
          >
            <span class="icon">📥</span>
            {{ importing ? 'Importing...' : 'Import Data' }}
          </button>
          
          <div v-if="importError" class="error-message">
            {{ importError }}
          </div>
          
          <div v-if="importSuccess" class="success-message">
            Successfully imported! {{ importStats }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAnalysisStore } from '../stores/analysisStore'

const emit = defineEmits(['close'])
const store = useAnalysisStore()

const exportType = ref('full')
const importMode = ref('merge')
const fileInput = ref(null)
const selectedFile = ref(null)
const importing = ref(false)
const importError = ref('')
const importSuccess = ref(false)
const importStats = ref('')

const stats = computed(() => {
  if (!store.analysisData) return null
  
  const totalNodes = [
    ...(store.analysisData.endpoints || []),
    ...(store.analysisData.services || []),
    ...(store.analysisData.repositories || [])
  ].length
  
  const annotatedNodes = store.nodeAnnotations.size
  const llmAnalyses = store.llmAnalyses?.length || 0
  
  return {
    totalNodes,
    annotatedNodes,
    llmAnalyses
  }
})

function handleExport() {
  try {
    let data
    
    if (exportType.value === 'full') {
      // Full export - everything
      data = store.getExportData()
    } else if (exportType.value === 'annotations') {
      // Annotations only
      data = {
        exportVersion: '1.0',
        exportDate: new Date().toISOString(),
        userAnnotations: Object.fromEntries(store.nodeAnnotations),
        llmAnalyses: store.llmAnalyses,
        llmChatHistory: store.llmChatHistory
      }
    } else {
      // Selected nodes only
      const selectedNodes = store.selectedNodes || []
      if (selectedNodes.length === 0) {
        alert('No nodes selected. Please select nodes in the graph first.')
        return
      }
      
      const selectedAnnotations = {}
      selectedNodes.forEach(nodeId => {
        const annotation = store.nodeAnnotations.get(nodeId)
        if (annotation) {
          selectedAnnotations[nodeId] = annotation
        }
      })
      
      data = {
        exportVersion: '1.0',
        exportDate: new Date().toISOString(),
        selectedNodes: selectedNodes,
        userAnnotations: selectedAnnotations
      }
    }
    
    const json = JSON.stringify(data, null, 2)
    const blob = new Blob([json], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `jgraph-analysis-${new Date().toISOString().split('T')[0]}.json`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    
    emit('close')
  } catch (error) {
    console.error('Export error:', error)
    alert('Failed to export: ' + error.message)
  }
}

function handleFileSelect(event) {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
    importError.value = ''
    importSuccess.value = false
  }
}

async function handleImport() {
  if (!selectedFile.value) return
  
  importing.value = true
  importError.value = ''
  importSuccess.value = false
  
  try {
    const text = await selectedFile.value.text()
    const data = JSON.parse(text)
    
    // Validate the data structure
    if (!data.exportVersion && !data.projectInfo) {
      throw new Error('Invalid export file format')
    }
    
    let importedAnnotations = 0
    let importedAnalyses = 0
    let importedChats = 0
    
    // Import annotations
    if (data.userAnnotations) {
      const annotations = data.userAnnotations
      
      if (importMode.value === 'replace') {
        // Clear existing annotations
        store.nodeAnnotations.clear()
      }
      
      // Add/merge annotations
      Object.entries(annotations).forEach(([nodeId, annotation]) => {
        store.nodeAnnotations.set(nodeId, annotation)
        importedAnnotations++
      })
    }
    
    // Import LLM analyses
    if (data.llmAnalyses) {
      if (importMode.value === 'replace') {
        store.llmAnalyses = data.llmAnalyses
        importedAnalyses = data.llmAnalyses.length
      } else {
        // Merge - append new analyses
        store.llmAnalyses = [...store.llmAnalyses, ...data.llmAnalyses]
        importedAnalyses = data.llmAnalyses.length
      }
    }
    
    // Import chat history
    if (data.llmChatHistory) {
      if (importMode.value === 'replace') {
        store.llmChatHistory = data.llmChatHistory
        importedChats = data.llmChatHistory.length
      } else {
        store.llmChatHistory = [...store.llmChatHistory, ...data.llmChatHistory]
        importedChats = data.llmChatHistory.length
      }
    }
    
    // If this is a full export with analysis data, load that too
    if (data.projectInfo && importMode.value === 'replace') {
      store.loadAnalysisData(data)
    }
    
    importSuccess.value = true
    importStats.value = `${importedAnnotations} annotations, ${importedAnalyses} analyses, ${importedChats} chats`
    
    // Auto-close after 2 seconds
    setTimeout(() => {
      emit('close')
    }, 2000)
    
  } catch (error) {
    console.error('Import error:', error)
    importError.value = 'Failed to import: ' + error.message
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 2rem;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #e0e0e0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.modal-header h2 {
  font-size: 1.5rem;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 2rem;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.modal-body {
  padding: 2rem;
  overflow-y: auto;
}

.section {
  margin-bottom: 2rem;
}

.section:last-child {
  margin-bottom: 0;
}

.section h3 {
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.description {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
  line-height: 1.5;
}

.export-options,
.import-options {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.option:hover {
  background: #f8f9fa;
  border-color: #667eea;
}

.option input[type="radio"] {
  cursor: pointer;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 6px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-item .label {
  font-size: 0.85rem;
  color: #666;
}

.stat-item .value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #667eea;
}

.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e0e0e0, transparent);
  margin: 2rem 0;
}

.file-input-area {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.file-name {
  color: #666;
  font-size: 0.9rem;
  font-style: italic;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-secondary:hover {
  background: #667eea;
  color: white;
}

.icon {
  font-size: 1.1rem;
}

.error-message {
  margin-top: 1rem;
  padding: 0.75rem;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 6px;
  color: #c33;
  font-size: 0.9rem;
}

.success-message {
  margin-top: 1rem;
  padding: 0.75rem;
  background: #efe;
  border: 1px solid #cfc;
  border-radius: 6px;
  color: #3c3;
  font-size: 0.9rem;
}
</style>
