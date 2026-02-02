<template>
  <div class="app">
    <header class="app-header">
      <div class="header-left">
        <h1>JGraph Analyzer</h1>
        <span class="version">v1.0.0</span>
      </div>
      
      <div class="header-actions">
        <button 
          v-if="store.hasData"
          @click="showExportImport = true" 
          class="header-btn export-btn"
          title="Export/Import Analysis"
        >
          <span class="icon">💾</span>
          Export/Import
        </button>
        
        <button 
          @click="showLLMSettings = true" 
          class="header-btn settings-btn"
          title="LLM Settings"
        >
          <span class="icon">⚙️</span>
          AI Settings
        </button>
      </div>
    </header>

    <main class="app-main">
      <FileUploader />

      <div v-if="store.hasData" class="content-area">
        <SearchBar />
        
        <div class="visualization-area">
          <div class="graph-section">
            <GraphView />
          </div>
          
          <div class="details-section">
            <NodeDetailsEnhanced />
          </div>
          
          <div class="llm-section" :class="{ 'collapsed': isLLMPanelCollapsed }">
            <button 
              class="collapse-btn" 
              @click="isLLMPanelCollapsed = !isLLMPanelCollapsed"
              :title="isLLMPanelCollapsed ? 'Expand AI Panel' : 'Collapse AI Panel'"
            >
              {{ isLLMPanelCollapsed ? '◀' : '▶' }}
            </button>
            <LLMPanel v-if="!isLLMPanelCollapsed" />
          </div>
        </div>
      </div>
    </main>

    <footer class="app-footer">
      <p>JGraph - Analyze Spring and Jakarta EE projects with AI-powered insights</p>
    </footer>
    
    <!-- Modals -->
    <LLMSettings v-if="showLLMSettings" @close="showLLMSettings = false" />
    <ExportImportPanel v-if="showExportImport" @close="showExportImport = false" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAnalysisStore } from './stores/analysisStore'
import FileUploader from './components/FileUploader.vue'
import SearchBar from './components/SearchBar.vue'
import GraphView from './components/GraphView.vue'
import NodeDetailsEnhanced from './components/NodeDetailsEnhanced.vue'
import LLMPanel from './components/LLMPanel.vue'
import LLMSettings from './components/LLMSettings.vue'
import ExportImportPanel from './components/ExportImportPanel.vue'

const store = useAnalysisStore()
const showLLMSettings = ref(false)
const showExportImport = ref(false)
const isLLMPanelCollapsed = ref(false)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
}

#app {
  width: 100%;
  height: 100vh;
}
</style>

<style scoped>
.app {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 1rem;
}

.app-header h1 {
  font-size: 1.5rem;
  margin: 0;
}

.version {
  font-size: 0.85rem;
  opacity: 0.8;
  font-weight: normal;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.header-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  color: white;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.header-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.header-btn .icon {
  font-size: 1rem;
}

.app-main {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background-color: white;
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: white;
}

.visualization-area {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 350px 400px;
  gap: 0;
  overflow: hidden;
  transition: grid-template-columns 0.3s ease;
}

.visualization-area:has(.llm-section.collapsed) {
  grid-template-columns: 1fr 350px 40px;
}

.graph-section {
  overflow: hidden;
  background-color: white;
}

.details-section {
  overflow-y: auto;
  background-color: white;
  border-left: 1px solid #e0e0e0;
}

.llm-section {
  position: relative;
  overflow-y: auto;
  background-color: #f8f9fa;
  border-left: 1px solid #e0e0e0;
  transition: all 0.3s ease;
}

.llm-section.collapsed {
  overflow: hidden;
}

.collapse-btn {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 10;
  width: 24px;
  height: 24px;
  padding: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.2s;
}

.collapse-btn:hover {
  background: #f0f0f0;
  transform: scale(1.1);
}

.app-footer {
  background-color: #2c3e50;
  color: white;
  text-align: center;
  padding: 1rem;
  font-size: 0.875rem;
}

@media (max-width: 1440px) {
  .visualization-area {
    grid-template-columns: 1fr 300px 350px;
  }
  
  .visualization-area:has(.llm-section.collapsed) {
    grid-template-columns: 1fr 300px 40px;
  }
}

@media (max-width: 1024px) {
  .visualization-area {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr 350px 400px;
  }

  .details-section,
  .llm-section {
    border-left: none;
    border-top: 1px solid #e0e0e0;
  }
  
  .visualization-area:has(.llm-section.collapsed) {
    grid-template-rows: 1fr 350px 40px;
  }
}
</style>
