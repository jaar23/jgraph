<template>
  <div class="search-results">
    <div v-if="!results" class="no-results">
      <p>No search performed yet</p>
    </div>

    <div v-else-if="results.components.length === 0" class="no-results">
      <p>No components found matching your search</p>
    </div>

    <div v-else class="results-container">
      <!-- Summary -->
      <div class="results-summary">
        <div class="summary-header">
          <h3>🎯 Trace Results</h3>
          <div class="summary-actions">
            <button @click="showDebugInfo = !showDebugInfo" class="debug-btn" title="Show/hide debug information">
              {{ showDebugInfo ? '🔍 Hide Debug' : '🔍 Show Debug' }}
            </button>
            <button @click="exportResults" class="export-btn" title="Export search results as JSON">
              💾 Export
            </button>
          </div>
        </div>
        <div class="summary-stats">
          <div class="stat">
            <span class="stat-value">{{ results.summary.totalComponents }}</span>
            <span class="stat-label">Components</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ results.summary.totalLogs }}</span>
            <span class="stat-label">Logs</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ results.summary.totalDataFlows }}</span>
            <span class="stat-label">DataFlows</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ results.confidence.level }}</span>
            <span class="stat-label">Confidence</span>
          </div>
        </div>
        
        <!-- Debug Info -->
        <div v-if="showDebugInfo" class="debug-info">
          <h4>🔍 Debug Information</h4>
          <div class="debug-section">
            <div class="debug-item">
              <strong>Input Type:</strong> {{ results.inputType }}
            </div>
            <div class="debug-item">
              <strong>Raw Logs Found:</strong> {{ results.logs?.length || 0 }}
              <span v-if="results.logs && results.logs.length > 0" class="debug-detail">
                (Top match: {{ results.logs[0].score }}% confidence - {{ truncate(results.logs[0].log.message, 50) }})
              </span>
            </div>
            <div class="debug-item">
              <strong>Raw DataFlows Found:</strong> {{ results.dataFlows?.length || 0 }}
              <span v-if="results.dataFlows && results.dataFlows.length > 0" class="debug-detail">
                (Top match: {{ results.dataFlows[0].score }}% confidence)
              </span>
            </div>
            <div class="debug-item">
              <strong>Components After Filtering:</strong> {{ results.components?.length || 0 }}
            </div>
            <div v-if="results.components.length === 0 && (results.logs?.length > 0 || results.dataFlows?.length > 0)" class="debug-warning">
              ⚠️ Found logs/dataflows but couldn't link them to components. This may indicate orphaned logs or disconnected dataflows.
            </div>
          </div>
        </div>
      </div>

      <!-- Components List -->
      <div class="components-list">
        <h4>🔥 Most Connected Components</h4>
        
        <div 
          v-for="component in results.components" 
          :key="component.id"
          class="component-card"
          :class="component.type"
        >
          <div class="card-header">
            <div class="card-title">
              <span class="type-icon">{{ getTypeIcon(component.type) }}</span>
              <span class="component-name">
                {{ getComponentName(component) }}
              </span>
            </div>
            <div class="centrality-score">
              <span class="score-value">{{ component.combinedScore }}</span>
              <span class="score-label">Score</span>
            </div>
          </div>

          <div class="card-content">
            <div v-if="component.type === 'endpoint'" class="endpoint-info">
              <span class="http-method" :class="component.data.httpMethod">
                {{ component.data.httpMethod }}
              </span>
              <span class="endpoint-path">{{ component.data.path }}</span>
            </div>

            <div class="component-meta">
              <span v-if="component.connections.logs.length > 0" class="meta-item">
                📝 {{ component.connections.logs.length }} Logs
              </span>
              <span v-if="component.connections.dataFlows.length > 0" class="meta-item">
                📊 {{ component.connections.dataFlows.length }} DataFlows
              </span>
              <span v-if="component.connections.databaseOps.length > 0" class="meta-item">
                🗄️ {{ component.connections.databaseOps.length }} DB Ops
              </span>
              <span v-if="component.connections.externalCalls.length > 0" class="meta-item">
                🌍 {{ component.connections.externalCalls.length }} External
              </span>
              <span v-if="component.connectionCount > 0" class="meta-item">
                🔗 {{ component.connectionCount }} Connections
              </span>
            </div>
          </div>

          <div class="card-actions">
            <button 
              @click="$emit('show-in-graph', component)" 
              class="action-btn primary"
              title="Highlight this component in the graph visualization"
            >
              🔍 Show in Graph
            </button>
            <button 
              @click="$emit('expand-boundary', component, 'both')" 
              class="action-btn secondary"
              title="Trace all upstream and downstream components"
            >
              ⬍⬍ Expand Boundary
            </button>
          </div>

          <!-- Expandable Details -->
          <div v-if="component.connections" class="expandable-details">
            <button 
              @click="toggleDetails(component.id)" 
              class="expand-toggle"
            >
              {{ expandedComponents.has(component.id) ? '▼ Hide Details' : '▶ Show Details' }}
            </button>

            <div v-if="expandedComponents.has(component.id)" class="details-content">
              <!-- Logs -->
              <div v-if="component.connections.logs.length > 0" class="detail-section">
                <div class="detail-header">📝 Logs ({{ component.connections.logs.length }})</div>
                <div 
                  v-for="(log, idx) in component.connections.logs.slice(0, 3)" 
                  :key="idx"
                  class="detail-item log-item"
                >
                  <span class="log-level" :class="log.level">{{ log.level }}</span>
                  <span class="log-message">{{ truncate(log.message, 60) }}</span>
                </div>
                <div v-if="component.connections.logs.length > 3" class="more-items">
                  +{{ component.connections.logs.length - 3 }} more
                </div>
              </div>

              <!-- Database Operations -->
              <div v-if="component.connections.databaseOps.length > 0" class="detail-section">
                <div class="detail-header">🗄️ Database ({{ component.connections.databaseOps.length }})</div>
                <div 
                  v-for="(op, idx) in component.connections.databaseOps.slice(0, 2)" 
                  :key="idx"
                  class="detail-item db-item"
                >
                  <span class="db-type">{{ op.operationType }}</span>
                  <span v-if="op.query" class="db-query">{{ truncate(op.query, 50) }}</span>
                </div>
                <div v-if="component.connections.databaseOps.length > 2" class="more-items">
                  +{{ component.connections.databaseOps.length - 2 }} more
                </div>
              </div>

              <!-- DataFlows -->
              <div v-if="component.connections.dataFlows.length > 0" class="detail-section">
                <div class="detail-header">📊 DataFlows ({{ component.connections.dataFlows.length }})</div>
                <div 
                  v-for="(flow, idx) in component.connections.dataFlows" 
                  :key="idx"
                  class="detail-item flow-item"
                >
                  <div class="flow-summary">
                    <span>{{ flow.parameters?.length || 0 }} params → </span>
                    <span>{{ flow.variables?.length || 0 }} transforms → </span>
                    <span>{{ flow.returns?.length || 0 }} returns</span>
                  </div>
                  <button 
                    @click="showDataFlowTimeline(flow)" 
                    class="timeline-btn-small"
                    title="View transformation timeline"
                  >
                    🔄 Timeline
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Transformation Timeline Modal -->
    <TransformationTimeline 
      v-if="selectedDataFlow"
      :visible="timelineVisible"
      :dataflow="selectedDataFlow"
      @close="timelineVisible = false"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TransformationTimeline from '../dataflow/TransformationTimeline.vue'

const props = defineProps({
  results: {
    type: Object,
    default: null
  }
})

defineEmits(['show-in-graph', 'expand-boundary'])

const expandedComponents = ref(new Set())
const timelineVisible = ref(false)
const selectedDataFlow = ref(null)
const showDebugInfo = ref(false)

function getTypeIcon(type) {
  const icons = {
    'endpoint': '🌐',
    'service': '⚙️',
    'repository': '💾',
    'log': '📝',
    'dataflow': '📊',
    'database': '🗄️',
    'external': '🌍',
    'exception': '⚠️'
  }
  return icons[type] || '📦'
}

function getComponentName(component) {
  if (component.type === 'endpoint') {
    return component.data.controllerClass + '.' + component.data.methodName
  }
  return (component.data.className || '') + '.' + (component.data.methodName || '')
}

function toggleDetails(componentId) {
  if (expandedComponents.value.has(componentId)) {
    expandedComponents.value.delete(componentId)
  } else {
    expandedComponents.value.add(componentId)
  }
}

function truncate(str, maxLength) {
  if (!str || str.length <= maxLength) return str
  return str.substring(0, maxLength) + '...'
}

function showDataFlowTimeline(dataFlow) {
  selectedDataFlow.value = dataFlow
  timelineVisible.value = true
}

function exportResults() {
  if (!props.results) return
  
  const exportData = {
    timestamp: new Date().toISOString(),
    summary: props.results.summary,
    confidence: props.results.confidence,
    inputType: props.results.inputType,
    components: props.results.components.map(c => ({
      id: c.id,
      type: c.type,
      name: getComponentName(c),
      centralityScore: c.centralityScore,
      combinedScore: c.combinedScore,
      connectionCount: c.connectionCount,
      connections: {
        logs: c.connections.logs.length,
        dataFlows: c.connections.dataFlows.length,
        databaseOps: c.connections.databaseOps.length,
        externalCalls: c.connections.externalCalls.length
      }
    }))
  }
  
  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `jgraph-trace-results-${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.search-results {
  margin-top: 1rem;
}

.no-results {
  padding: 2rem;
  text-align: center;
  color: #9ca3af;
  font-size: 0.9rem;
}

.results-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.results-summary {
  padding: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.results-summary h3 {
  margin: 0;
  font-size: 1.1rem;
}

.summary-actions {
  display: flex;
  gap: 0.5rem;
}

.debug-btn,
.export-btn {
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 6px;
  color: white;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.debug-btn:hover,
.export-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.debug-info {
  margin-top: 1rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.debug-info h4 {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  opacity: 0.95;
}

.debug-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.debug-item {
  font-size: 0.85rem;
  opacity: 0.9;
}

.debug-item strong {
  opacity: 1;
  margin-right: 0.5rem;
}

.debug-detail {
  font-size: 0.8rem;
  opacity: 0.8;
  font-style: italic;
}

.debug-warning {
  margin-top: 0.5rem;
  padding: 0.5rem;
  background: rgba(255, 193, 7, 0.2);
  border-radius: 4px;
  border-left: 3px solid rgba(255, 193, 7, 0.8);
  font-size: 0.85rem;
}

.summary-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
}

.stat-label {
  font-size: 0.75rem;
  opacity: 0.9;
}

.components-list h4 {
  margin: 0 0 1rem 0;
  font-size: 1rem;
  color: #374151;
}

.component-card {
  background: white;
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  margin-bottom: 1rem;
  overflow: hidden;
  transition: all 0.2s;
}

.component-card:hover {
  border-color: #3498db;
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.1);
}

.component-card.endpoint {
  border-left: 4px solid #22c55e;
}

.component-card.service {
  border-left: 4px solid #3b82f6;
}

.component-card.repository {
  border-left: 4px solid #ef4444;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
}

.type-icon {
  font-size: 1.2rem;
}

.component-name {
  font-weight: 600;
  color: #374151;
  font-size: 0.95rem;
}

.centrality-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.25rem 0.75rem;
  background-color: #3498db;
  color: white;
  border-radius: 6px;
}

.score-value {
  font-size: 1.25rem;
  font-weight: 700;
  line-height: 1;
}

.score-label {
  font-size: 0.7rem;
  opacity: 0.9;
}

.card-content {
  padding: 1rem;
}

.endpoint-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.http-method {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 700;
  color: white;
}

.http-method.GET { background-color: #22c55e; }
.http-method.POST { background-color: #3b82f6; }
.http-method.PUT { background-color: #f59e0b; }
.http-method.DELETE { background-color: #ef4444; }

.endpoint-path {
  font-family: 'Courier New', monospace;
  color: #065f46;
  font-weight: 500;
}

.component-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.meta-item {
  font-size: 0.875rem;
  color: #6b7280;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  padding: 1rem;
  border-top: 1px solid #e5e7eb;
}

.action-btn {
  flex: 1;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  border: none;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.primary {
  background-color: #3498db;
  color: white;
}

.action-btn.primary:hover {
  background-color: #2980b9;
}

.action-btn.secondary {
  background-color: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.action-btn.secondary:hover {
  background-color: #e5e7eb;
}

.expandable-details {
  border-top: 1px solid #e5e7eb;
}

.expand-toggle {
  width: 100%;
  padding: 0.75rem 1rem;
  background-color: #f9fafb;
  border: none;
  text-align: left;
  font-size: 0.875rem;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  transition: background-color 0.2s;
}

.expand-toggle:hover {
  background-color: #f3f4f6;
}

.details-content {
  padding: 1rem;
  background-color: #fafbfc;
}

.detail-section {
  margin-bottom: 1rem;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-header {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.detail-item {
  padding: 0.5rem;
  background-color: white;
  border-radius: 4px;
  margin-bottom: 0.25rem;
  font-size: 0.875rem;
}

.log-level {
  padding: 0.125rem 0.5rem;
  border-radius: 3px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-right: 0.5rem;
}

.log-level.ERROR { background-color: #fee2e2; color: #991b1b; }
.log-level.WARN { background-color: #fef3c7; color: #92400e; }
.log-level.INFO { background-color: #dbeafe; color: #1e40af; }
.log-level.DEBUG { background-color: #e0e7ff; color: #3730a3; }

.log-message {
  color: #6b7280;
  font-family: 'Courier New', monospace;
  font-size: 0.8rem;
}

.db-type {
  background-color: #ddd6fe;
  color: #5b21b6;
  padding: 0.125rem 0.5rem;
  border-radius: 3px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-right: 0.5rem;
}

.db-query {
  font-family: 'Courier New', monospace;
  font-size: 0.8rem;
  color: #6b7280;
}

.flow-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.8rem;
}

.flow-summary {
  display: flex;
  gap: 0.5rem;
}

.timeline-btn-small {
  padding: 0.25rem 0.5rem;
  background: linear-gradient(135deg, #fef3c7 0%, #fed7aa 100%);
  border: 1px solid #f59e0b;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 600;
  color: #92400e;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.timeline-btn-small:hover {
  background: linear-gradient(135deg, #fed7aa 0%, #fbbf24 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(245, 158, 11, 0.3);
}

.more-items {
  padding: 0.25rem 0.5rem;
  color: #9ca3af;
  font-size: 0.75rem;
  font-style: italic;
}
</style>
