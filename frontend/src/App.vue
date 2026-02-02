<template>
  <div class="app">
    <header class="app-header">
      <h1>JGraph Analyzer</h1>
      <p>Java Web Project Architecture Visualization</p>
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
            <NodeDetails />
          </div>
        </div>
      </div>
    </main>

    <footer class="app-footer">
      <p>JGraph v1.0.0 - Analyze Spring and Jakarta EE projects</p>
    </footer>
  </div>
</template>

<script setup>
import { useAnalysisStore } from './stores/analysisStore'
import FileUploader from './components/FileUploader.vue'
import SearchBar from './components/SearchBar.vue'
import GraphView from './components/GraphView.vue'
import NodeDetails from './components/NodeDetails.vue'

const store = useAnalysisStore()
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
  padding: 1.5rem 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.app-header h1 {
  font-size: 1.75rem;
  margin-bottom: 0.25rem;
}

.app-header p {
  font-size: 0.95rem;
  opacity: 0.9;
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
  grid-template-columns: 1fr 400px;
  gap: 0;
  overflow: hidden;
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

.app-footer {
  background-color: #2c3e50;
  color: white;
  text-align: center;
  padding: 1rem;
  font-size: 0.875rem;
}

@media (max-width: 1024px) {
  .visualization-area {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr 400px;
  }

  .details-section {
    border-left: none;
    border-top: 1px solid #e0e0e0;
  }
}
</style>
