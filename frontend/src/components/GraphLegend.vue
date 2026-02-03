<template>
  <div class="graph-legend" :class="{ 'collapsed': isCollapsed }">
    <!-- Toggle Button -->
    <button 
      class="legend-toggle" 
      @click="isCollapsed = !isCollapsed"
      :title="isCollapsed ? 'Show Legend' : 'Hide Legend'"
    >
      {{ isCollapsed ? '📖 Show Help' : '✕' }}
    </button>

    <!-- Legend Content -->
    <div v-if="!isCollapsed" class="legend-content">
      <div class="legend-header">
        <h3>📖 Graph Controls</h3>
        <p class="legend-subtitle">Keyboard shortcuts and operations</p>
      </div>

      <!-- Navigation Section -->
      <div class="legend-section">
        <div class="section-title">🧭 Navigation</div>
        <div class="legend-items">
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Click + Drag</kbd>
            </span>
            <span class="description">Pan the canvas</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Mouse Wheel</kbd>
            </span>
            <span class="description">Zoom in/out</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Ctrl</kbd> + <kbd>Wheel</kbd>
            </span>
            <span class="description">Fine zoom control</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Double Click</kbd>
            </span>
            <span class="description">Reset view to fit all</span>
          </div>
        </div>
      </div>

      <!-- Node Selection Section -->
      <div class="legend-section">
        <div class="section-title">🎯 Node Selection</div>
        <div class="legend-items">
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Click</kbd>
            </span>
            <span class="description">Select single node</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Ctrl</kbd> + <kbd>Click</kbd>
            </span>
            <span class="description">Multi-select nodes</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Shift</kbd> + <kbd>Drag</kbd>
            </span>
            <span class="description">Box select multiple</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Esc</kbd>
            </span>
            <span class="description">Clear selection</span>
          </div>
        </div>
      </div>

      <!-- Node Operations Section -->
      <div class="legend-section">
        <div class="section-title">⚙️ Node Operations</div>
        <div class="legend-items">
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Right Click</kbd>
            </span>
            <span class="description">Node context menu</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Drag Node</kbd>
            </span>
            <span class="description">Move node position</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Del</kbd>
            </span>
            <span class="description">Hide selected nodes</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Ctrl</kbd> + <kbd>A</kbd>
            </span>
            <span class="description">Select all visible</span>
          </div>
        </div>
      </div>

      <!-- View Controls Section -->
      <div class="legend-section">
        <div class="section-title">👁️ View Controls</div>
        <div class="legend-items">
          <div class="legend-item">
            <span class="key-combo">
              <kbd>F</kbd>
            </span>
            <span class="description">Fit selected to view</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Ctrl</kbd> + <kbd>F</kbd>
            </span>
            <span class="description">Fit all to view</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>+</kbd> / <kbd>-</kbd>
            </span>
            <span class="description">Zoom in/out</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>0</kbd>
            </span>
            <span class="description">Reset zoom to 100%</span>
          </div>
        </div>
      </div>

      <!-- Search & Filter Section -->
      <div class="legend-section">
        <div class="section-title">🔍 Search & Filter</div>
        <div class="legend-items">
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Ctrl</kbd> + <kbd>K</kbd>
            </span>
            <span class="description">Open quick search</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>Ctrl</kbd> + <kbd>H</kbd>
            </span>
            <span class="description">Toggle layer filters</span>
          </div>
          <div class="legend-item">
            <span class="key-combo">
              <kbd>/</kbd>
            </span>
            <span class="description">Focus search box</span>
          </div>
        </div>
      </div>

      <!-- Node Types Legend -->
      <div class="legend-section">
        <div class="section-title">🏷️ Node Types</div>
        <div class="legend-items node-types">
          <div class="legend-item">
            <span class="node-icon endpoint">🌐</span>
            <span class="description">Endpoint/Controller</span>
          </div>
          <div class="legend-item">
            <span class="node-icon service">⚙️</span>
            <span class="description">Service Layer</span>
          </div>
          <div class="legend-item">
            <span class="node-icon repository">💾</span>
            <span class="description">Repository/DAO</span>
          </div>
          <div class="legend-item">
            <span class="node-icon dataflow">📊</span>
            <span class="description">Data Flow</span>
          </div>
          <div class="legend-item">
            <span class="node-icon database">🗄️</span>
            <span class="description">Database Operation</span>
          </div>
          <div class="legend-item">
            <span class="node-icon log">📝</span>
            <span class="description">Log Statement</span>
          </div>
          <div class="legend-item">
            <span class="node-icon external">🌍</span>
            <span class="description">External Call</span>
          </div>
          <div class="legend-item">
            <span class="node-icon error">⚠️</span>
            <span class="description">Error/Exception</span>
          </div>
        </div>
      </div>

      <!-- Tips Section -->
      <div class="legend-section tips-section">
        <div class="section-title">💡 Pro Tips</div>
        <div class="tips-list">
          <div class="tip-item">
            <span class="tip-icon">💡</span>
            <span class="tip-text">Use <strong>Advanced Trace</strong> to find components by log messages</span>
          </div>
          <div class="tip-item">
            <span class="tip-icon">💡</span>
            <span class="tip-text">Click <strong>DataFlow</strong> nodes to see transformation timeline</span>
          </div>
          <div class="tip-item">
            <span class="tip-icon">💡</span>
            <span class="tip-text">Use <strong>Layer Controls</strong> to hide/show node types</span>
          </div>
          <div class="tip-item">
            <span class="tip-icon">💡</span>
            <span class="tip-text">Right-click nodes for quick actions and navigation</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const isCollapsed = ref(false)

// Auto-collapse after 10 seconds on first load
onMounted(() => {
  const hasSeenLegend = localStorage.getItem('jgraph-legend-seen')
  
  if (!hasSeenLegend) {
    // First time: show legend, auto-collapse after 10 seconds
    setTimeout(() => {
      isCollapsed.value = true
      localStorage.setItem('jgraph-legend-seen', 'true')
    }, 10000)
  } else {
    // Subsequent times: start collapsed
    isCollapsed.value = true
  }
})

// Global keyboard shortcuts
function handleGlobalKeyPress(e) {
  // Ctrl+H to toggle legend
  if (e.ctrlKey && e.key === 'h') {
    e.preventDefault()
    isCollapsed.value = !isCollapsed.value
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleGlobalKeyPress)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleGlobalKeyPress)
})
</script>

<style scoped>
.graph-legend {
  position: fixed;
  bottom: 20px;
  left: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 100;
  max-width: 400px;
  max-height: calc(100vh - 100px);
  transition: all 0.3s ease;
}

.graph-legend.collapsed {
  max-width: 150px;
  max-height: 50px;
}

.legend-toggle {
  position: absolute;
  top: 0;
  right: 0;
  padding: 0.75rem 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 0 12px 0 0;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 101;
}

.graph-legend.collapsed .legend-toggle {
  border-radius: 12px;
  position: static;
  width: 100%;
  padding: 0.875rem 1.25rem;
}

.legend-toggle:hover {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.legend-content {
  padding: 1rem;
  padding-top: 3rem;
  overflow-y: auto;
  max-height: calc(100vh - 140px);
}

.legend-content::-webkit-scrollbar {
  width: 6px;
}

.legend-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.legend-content::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.legend-content::-webkit-scrollbar-thumb:hover {
  background: #555;
}

.legend-header {
  margin-bottom: 1.25rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e5e7eb;
}

.legend-header h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  color: #111827;
}

.legend-subtitle {
  margin: 0;
  font-size: 0.875rem;
  color: #6b7280;
}

.legend-section {
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 0.875rem;
  font-weight: 700;
  color: #374151;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.75rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.875rem;
}

.key-combo {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  min-width: 120px;
}

kbd {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  background: linear-gradient(to bottom, #f9fafb 0%, #e5e7eb 100%);
  border: 1px solid #d1d5db;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  font-family: 'Courier New', monospace;
  font-size: 0.75rem;
  font-weight: 600;
  color: #374151;
  line-height: 1;
}

.description {
  flex: 1;
  color: #6b7280;
  line-height: 1.4;
}

/* Node Types */
.node-types {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.625rem;
}

.node-icon {
  font-size: 1.25rem;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: #f3f4f6;
}

.node-icon.endpoint {
  background: #d1fae5;
}

.node-icon.service {
  background: #dbeafe;
}

.node-icon.repository {
  background: #fecaca;
}

.node-icon.dataflow {
  background: #fef3c7;
}

.node-icon.database {
  background: #e9d5ff;
}

.node-icon.log {
  background: #cffafe;
}

.node-icon.external {
  background: #fed7aa;
}

.node-icon.error {
  background: #fef9c3;
}

/* Tips Section */
.tips-section {
  background: linear-gradient(135deg, #fef3c7 0%, #fef9e7 100%);
  padding: 1rem;
  border-radius: 8px;
  border: 2px solid #fbbf24;
  margin-bottom: 0;
}

.tips-section .section-title {
  border-bottom-color: #fbbf24;
  color: #92400e;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.tip-item {
  display: flex;
  gap: 0.625rem;
  align-items: flex-start;
}

.tip-icon {
  font-size: 1rem;
  flex-shrink: 0;
}

.tip-text {
  font-size: 0.8rem;
  color: #78350f;
  line-height: 1.4;
}

.tip-text strong {
  color: #92400e;
  font-weight: 600;
}

/* Responsive */
@media (max-width: 768px) {
  .graph-legend {
    bottom: 10px;
    left: 10px;
    max-width: calc(100vw - 20px);
  }
  
  .node-types {
    grid-template-columns: 1fr;
  }
  
  .key-combo {
    min-width: 100px;
  }
}

/* Animation */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.legend-content {
  animation: slideIn 0.3s ease-out;
}
</style>
