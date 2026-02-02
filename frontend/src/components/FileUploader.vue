<template>
  <div class="file-uploader">
    <div v-if="!store.hasData" class="upload-area">
      <div class="upload-box">
        <h2>JGraph Analyzer Viewer</h2>
        <p>Upload your analysis JSON file to visualize your Java project architecture</p>
        
        <div class="upload-controls">
          <input
            ref="fileInput"
            type="file"
            accept=".json"
            @change="handleFileUpload"
            class="file-input"
          />
          <button @click="triggerFileInput" class="upload-btn">
            Choose JSON File
          </button>
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <div class="instructions">
          <h3>How to use:</h3>
          <ol>
            <li>Run the JGraph analyzer on your Java project</li>
            <li>Upload the generated analysis.json file</li>
            <li>Explore your project's architecture visually</li>
          </ol>
        </div>
      </div>
    </div>

    <div v-else class="loaded-info-compact">
      <div class="compact-header">
        <div class="project-title">
          <strong>{{ store.analysisData.project.name }}</strong>
          <span class="project-path">{{ store.analysisData.project.path }}</span>
        </div>
        <div class="compact-stats">
          <span class="stat-item">{{ store.statistics.totalEndpoints }} endpoints</span>
          <span class="stat-item">{{ store.statistics.totalServices }} services</span>
          <span class="stat-item">{{ store.statistics.totalRepositories }} repos</span>
          <button @click="store.clearData()" class="reload-btn-compact">
            Change File
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAnalysisStore } from '../stores/analysisStore'

const store = useAnalysisStore()
const fileInput = ref(null)
const error = ref('')

function triggerFileInput() {
  fileInput.value?.click()
}

function handleFileUpload(event) {
  const file = event.target.files[0]
  if (!file) return

  error.value = ''

  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const json = JSON.parse(e.target.result)
      
      // Validate JSON structure
      if (!json.project || !json.callGraph) {
        throw new Error('Invalid analysis JSON format')
      }

      store.loadAnalysisData(json)
    } catch (err) {
      error.value = 'Error parsing JSON file: ' + err.message
      console.error('Parse error:', err)
    }
  }

  reader.onerror = () => {
    error.value = 'Error reading file'
  }

  reader.readAsText(file)
}
</script>

<style scoped>
.file-uploader {
  padding: 1rem;
  background-color: white;
}

.upload-area {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.upload-box {
  max-width: 600px;
  text-align: center;
  padding: 2rem;
}

.upload-box h2 {
  color: #2c3e50;
  margin-bottom: 1rem;
  font-size: 2rem;
}

.upload-box p {
  color: #666;
  margin-bottom: 2rem;
}

.upload-controls {
  margin-bottom: 1rem;
}

.file-input {
  display: none;
}

.upload-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 1rem 2rem;
  font-size: 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.upload-btn:hover {
  background-color: #2980b9;
}

.error-message {
  color: #e74c3c;
  background-color: #fadbd8;
  padding: 1rem;
  border-radius: 4px;
  margin-top: 1rem;
}

.instructions {
  margin-top: 3rem;
  text-align: left;
  background-color: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
}

.instructions h3 {
  color: #2c3e50;
  margin-bottom: 1rem;
}

.instructions ol {
  margin-left: 1.5rem;
  color: #666;
  line-height: 2;
}

.loaded-info-compact {
  padding: 0.75rem 1.5rem;
  background-color: #f8f9fa;
  border-bottom: 2px solid #e0e0e0;
}

.compact-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.project-title {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.project-title strong {
  color: #2c3e50;
  font-size: 1rem;
}

.project-path {
  color: #666;
  font-size: 0.75rem;
  font-family: monospace;
}

.compact-stats {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-item {
  color: #666;
  font-size: 0.875rem;
  white-space: nowrap;
}

.reload-btn-compact {
  background-color: #95a5a6;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  white-space: nowrap;
}

.reload-btn-compact:hover {
  background-color: #7f8c8d;
}
</style>
