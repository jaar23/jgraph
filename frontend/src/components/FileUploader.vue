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
            <li>Optionally upload the analysis-source-map.json for detailed source code insights</li>
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
          <div v-if="store.hasSourceMap" class="source-map-status">
            ✅ Source map loaded ({{ Object.keys(store.sourceMapData.methods).length }} methods)
          </div>
          <div v-else class="source-map-status no-source">
            ℹ️ No source map loaded
          </div>
        </div>
        <div class="compact-stats">
          <span class="stat-item">{{ store.statistics.totalEndpoints }} endpoints</span>
          <span class="stat-item">{{ store.statistics.totalServices }} services</span>
          <span class="stat-item">{{ store.statistics.totalRepositories }} repos</span>
          
          <!-- Source map upload button -->
          <input
            ref="sourceMapInput"
            type="file"
            accept=".json"
            @change="handleSourceMapUpload"
            class="file-input"
          />
          <button 
            v-if="!store.hasSourceMap" 
            @click="triggerSourceMapInput" 
            class="source-map-btn"
            title="Upload source map file for detailed code insights"
          >
            📄 Upload Source Map
          </button>
          <button 
            v-else 
            @click="removeSourceMap" 
            class="remove-source-btn"
            title="Remove source map"
          >
            ✕ Remove Source Map
          </button>
          
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
const sourceMapInput = ref(null)
const error = ref('')

function triggerFileInput() {
  fileInput.value?.click()
}

function triggerSourceMapInput() {
  sourceMapInput.value?.click()
}

function removeSourceMap() {
  store.sourceMapData = null
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
      
      // Try to load source map file (same name with -source-map suffix)
      const fileName = file.name
      const sourceMapName = fileName.replace(/\.json$/, '-source-map.json')
      
      // Create a new file input to look for source map
      const sourceMapInput = document.createElement('input')
      sourceMapInput.type = 'file'
      sourceMapInput.accept = '.json'
      
      // Try to automatically load source map from FileList if available
      const fileList = event.target.files
      for (let i = 0; i < fileList.length; i++) {
        if (fileList[i].name === sourceMapName) {
          loadSourceMapFile(fileList[i])
          break
        }
      }
      
      // If not found, try to fetch it from the same location
      tryFetchSourceMap(fileName, sourceMapName)
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

function loadSourceMapFile(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const json = JSON.parse(e.target.result)
      
      // Validate source map structure
      if (!json.methods || !json.version) {
        console.warn('Invalid source map format, skipping')
        return
      }

      store.loadSourceMapData(json)
      console.log('Source map loaded successfully')
    } catch (err) {
      console.warn('Error parsing source map file:', err)
    }
  }
  
  reader.readAsText(file)
}

function tryFetchSourceMap(analysisFileName, sourceMapName) {
  // This won't work for local files but might work if served from a server
  // Just log for now, user can manually select source map file later
  console.log('To enable source code details, upload the source map file:', sourceMapName)
}

function handleSourceMapUpload(event) {
  const file = event.target.files[0]
  if (!file) return

  loadSourceMapFile(file)
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
