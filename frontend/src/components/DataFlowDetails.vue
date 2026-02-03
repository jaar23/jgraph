<template>
  <div v-if="dataflow" class="dataflow-details">
    <div class="details-header">
      <h3>📊 Data Flow Analysis</h3>
      <button @click="$emit('close')" class="close-btn">×</button>
    </div>

    <div class="details-content">
      <!-- Method Information -->
      <div class="detail-section primary-info">
        <h4>Method</h4>
        <div class="method-info">
          <div class="detail-row">
            <span class="label">Class:</span>
            <span class="value code">{{ dataflow.className }}</span>
          </div>
          <div class="detail-row">
            <span class="label">Method:</span>
            <span class="value code">{{ dataflow.methodName }}()</span>
          </div>
          <div class="detail-row">
            <span class="label">Method ID:</span>
            <span class="value code small">{{ dataflow.methodId }}</span>
          </div>
        </div>
      </div>

      <!-- Related Components -->
      <div v-if="relatedComponents" class="detail-section related-components">
        <h4>🔗 Related Components</h4>
        
        <div v-if="relatedComponents.endpoint" class="component-card endpoint-card">
          <div class="card-header">
            <span class="icon">🌐</span>
            <span class="type">Endpoint</span>
          </div>
          <div class="card-content">
            <div class="http-method" :class="relatedComponents.endpoint.httpMethod">
              {{ relatedComponents.endpoint.httpMethod }}
            </div>
            <div class="path">{{ relatedComponents.endpoint.path }}</div>
            <div class="class-name">{{ relatedComponents.endpoint.controllerClass }}</div>
          </div>
        </div>

        <div v-if="relatedComponents.service" class="component-card service-card">
          <div class="card-header">
            <span class="icon">⚙️</span>
            <span class="type">Service</span>
          </div>
          <div class="card-content">
            <div class="class-name">{{ relatedComponents.service.className }}</div>
            <div class="method-name">{{ relatedComponents.service.methodName }}()</div>
          </div>
        </div>

        <div v-if="relatedComponents.repository" class="component-card repo-card">
          <div class="card-header">
            <span class="icon">💾</span>
            <span class="type">Repository</span>
          </div>
          <div class="card-content">
            <div class="class-name">{{ relatedComponents.repository.className }}</div>
            <div class="method-name">{{ relatedComponents.repository.methodName }}()</div>
          </div>
        </div>

        <div v-if="relatedComponents.databaseOps?.length > 0" class="component-card db-card">
          <div class="card-header">
            <span class="icon">🗄️</span>
            <span class="type">Database Operations ({{ relatedComponents.databaseOps.length }})</span>
          </div>
          <div class="card-content">
            <div v-for="(op, idx) in relatedComponents.databaseOps.slice(0, 3)" :key="idx" class="db-op">
              <span class="op-type">{{ op.operationType }}</span>
              <span class="op-query" v-if="op.query">{{ truncate(op.query, 50) }}</span>
            </div>
            <div v-if="relatedComponents.databaseOps.length > 3" class="more-items">
              +{{ relatedComponents.databaseOps.length - 3 }} more operations
            </div>
          </div>
        </div>

        <div v-if="relatedComponents.logs?.length > 0" class="component-card log-card">
          <div class="card-header">
            <span class="icon">📝</span>
            <span class="type">Log Statements ({{ relatedComponents.logs.length }})</span>
          </div>
          <div class="card-content">
            <div v-for="(log, idx) in relatedComponents.logs.slice(0, 3)" :key="idx" class="log-item">
              <span class="log-level" :class="log.level">{{ log.level }}</span>
              <span class="log-msg">{{ truncate(log.message, 60) }}</span>
            </div>
            <div v-if="relatedComponents.logs.length > 3" class="more-items">
              +{{ relatedComponents.logs.length - 3 }} more logs
            </div>
          </div>
        </div>

        <div v-if="relatedComponents.exceptions?.length > 0" class="component-card exception-card">
          <div class="card-header">
            <span class="icon">⚠️</span>
            <span class="type">Exception Handling ({{ relatedComponents.exceptions.length }})</span>
          </div>
          <div class="card-content">
            <div v-for="(ex, idx) in relatedComponents.exceptions.slice(0, 2)" :key="idx" class="exception-item">
              <span class="exception-types">{{ ex.exceptionTypes?.join(', ') || 'Exception' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Parameters -->
      <div v-if="dataflow.parameters?.length > 0" class="detail-section">
        <h4>📥 Input Parameters ({{ dataflow.parameters.length }})</h4>
        <div class="parameters-list">
          <div v-for="param in dataflow.parameters" :key="param.name" class="parameter-item">
            <div class="param-header">
              <span class="param-name">{{ param.name }}</span>
              <span class="param-type">{{ param.type }}</span>
              <span v-if="param.isInputData" class="badge input-badge">Input</span>
            </div>
            <div v-if="param.annotations?.length" class="param-annotations">
              <span v-for="ann in param.annotations" :key="ann" class="annotation">{{ ann }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Variable Transformations -->
      <div v-if="dataflow.variables?.length > 0" class="detail-section">
        <h4>🔄 Data Transformations ({{ dataflow.variables.length }})</h4>
        <div class="variables-list">
          <div v-for="variable in dataflow.variables" :key="variable.variableName" class="variable-item">
            <div class="var-header">
              <span class="var-name">{{ variable.variableName }}</span>
              <span class="var-type">{{ variable.variableType }}</span>
              <span class="transform-badge">{{ variable.transformationType }}</span>
            </div>
            <div v-if="variable.sourceVariable" class="var-source">
              ← from: <span class="code">{{ variable.sourceVariable }}</span>
            </div>
            <div v-if="variable.assignmentExpression" class="var-expression">
              <span class="code">{{ variable.assignmentExpression }}</span>
            </div>
            <div class="var-location">
              Line {{ variable.lineNumber }}
            </div>
          </div>
        </div>
      </div>

      <!-- Data Transfers (Method Calls) -->
      <div v-if="dataflow.dataTransfers?.length > 0" class="detail-section">
        <h4>📤 Data Transfers ({{ dataflow.dataTransfers.length }})</h4>
        <div class="transfers-list">
          <div v-for="transfer in dataflow.dataTransfers" :key="transfer.targetMethod" class="transfer-item">
            <div class="transfer-header">
              <span class="transfer-target">{{ transfer.targetMethod }}()</span>
              <span class="transfer-line">Line {{ transfer.lineNumber }}</span>
            </div>
            <div v-if="transfer.arguments?.length" class="transfer-args">
              <div v-for="arg in transfer.arguments" :key="arg.position" class="arg-item">
                <span class="arg-pos">#{{ arg.position }}</span>
                <span class="arg-type">{{ arg.type }}</span>
                <span v-if="arg.variableName" class="arg-var">{{ arg.variableName }}</span>
                <span v-if="arg.expression" class="arg-expr">= {{ truncate(arg.expression, 40) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Return Values -->
      <div v-if="dataflow.returns?.length > 0" class="detail-section">
        <h4>↩️ Return Values ({{ dataflow.returns.length }})</h4>
        <div class="returns-list">
          <div v-for="(ret, idx) in dataflow.returns" :key="idx" class="return-item">
            <div class="return-header">
              <span class="return-type">{{ ret.returnType }}</span>
              <span class="return-line">Line {{ ret.lineNumber }}</span>
            </div>
            <div class="return-expression">
              <span class="code">{{ ret.expression }}</span>
            </div>
            <div v-if="ret.sourceVariable" class="return-source">
              Variable: <span class="code">{{ ret.sourceVariable }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="detail-section actions">
        <button @click="showTimeline" class="action-btn timeline-btn">
          🔄 View Transformation Timeline
        </button>
        <button @click="expandDataFlow" class="action-btn primary">
          🔍 Show Full Flow in Graph
        </button>
        <button @click="copyToClipboard" class="action-btn secondary">
          📋 Copy Flow Details
        </button>
      </div>
    </div>

    <!-- Transformation Timeline Modal -->
    <TransformationTimeline 
      :visible="timelineVisible"
      :dataflow="dataflow"
      @close="timelineVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAnalysisStore } from '../stores/analysisStore'
import TransformationTimeline from './dataflow/TransformationTimeline.vue'

const props = defineProps({
  dataflow: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'expand'])

const store = useAnalysisStore()
const timelineVisible = ref(false)

const relatedComponents = computed(() => {
  if (!props.dataflow) return null

  const methodId = props.dataflow.methodId
  
  // Find the parent endpoint
  const endpoint = store.endpoints.find(e => e.id === methodId)
  
  // Find the parent service
  const service = store.services.find(s => s.id === methodId)
  
  // Find the parent repository
  const repository = store.repositories.find(r => r.id === methodId)
  
  // Find related database operations (by class and method name)
  const databaseOps = store.databaseOperations.filter(op => 
    op.className === props.dataflow.className && 
    op.methodName === props.dataflow.methodName
  )
  
  // Find related logs
  const logs = store.logStatements.filter(log => 
    log.className === props.dataflow.className && 
    log.methodName === props.dataflow.methodName
  )
  
  // Find related exceptions
  const exceptions = store.tryCatchBlocks.filter(ex => 
    ex.className === props.dataflow.className && 
    ex.methodName === props.dataflow.methodName
  )
  
  return {
    endpoint,
    service,
    repository,
    databaseOps,
    logs,
    exceptions
  }
})

function truncate(str, maxLength) {
  if (!str || str.length <= maxLength) return str
  return str.substring(0, maxLength) + '...'
}

function showTimeline() {
  timelineVisible.value = true
}

function expandDataFlow() {
  emit('expand', props.dataflow)
}

function copyToClipboard() {
  const text = `
Data Flow: ${props.dataflow.className}.${props.dataflow.methodName}

Parameters (${props.dataflow.parameters?.length || 0}):
${props.dataflow.parameters?.map(p => `  - ${p.name}: ${p.type}`).join('\n') || '  None'}

Transformations (${props.dataflow.variables?.length || 0}):
${props.dataflow.variables?.map(v => `  - ${v.variableName} (${v.transformationType})`).join('\n') || '  None'}

Returns (${props.dataflow.returns?.length || 0}):
${props.dataflow.returns?.map(r => `  - ${r.returnType}: ${r.expression}`).join('\n') || '  None'}

Data Transfers (${props.dataflow.dataTransfers?.length || 0}):
${props.dataflow.dataTransfers?.map(t => `  - ${t.targetMethod}() with ${t.arguments?.length || 0} args`).join('\n') || '  None'}
  `.trim()
  
  navigator.clipboard.writeText(text)
}
</script>

<style scoped>
.dataflow-details {
  position: fixed;
  top: 0;
  right: 0;
  width: 500px;
  height: 100vh;
  background: white;
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  background: linear-gradient(135deg, #fef3c7 0%, #fed7aa 100%);
  border-bottom: 2px solid #f59e0b;
}

.details-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #92400e;
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #92400e;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
}

.close-btn:hover {
  color: #78350f;
}

.details-content {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
}

.detail-section {
  margin-bottom: 2rem;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.detail-section h4 {
  margin: 0 0 1rem 0;
  font-size: 1rem;
  color: #374151;
  font-weight: 600;
}

.primary-info {
  background: linear-gradient(135deg, #fef3c7 0%, #fef9e7 100%);
  border-color: #f59e0b;
}

.detail-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.label {
  font-weight: 600;
  color: #6b7280;
  min-width: 100px;
}

.value {
  color: #111827;
}

.code {
  font-family: 'Courier New', monospace;
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.9rem;
}

.small {
  font-size: 0.8rem;
}

/* Related Components */
.related-components {
  background: white;
  padding: 1.5rem;
}

.component-card {
  margin-bottom: 1rem;
  padding: 1rem;
  border-radius: 6px;
  border: 2px solid;
}

.endpoint-card {
  background: #f0fdf4;
  border-color: #22c55e;
}

.service-card {
  background: #eff6ff;
  border-color: #3b82f6;
}

.repo-card {
  background: #fef2f2;
  border-color: #ef4444;
}

.db-card {
  background: #f5f3ff;
  border-color: #8b5cf6;
}

.log-card {
  background: #ecfeff;
  border-color: #06b6d4;
}

.exception-card {
  background: #fff7ed;
  border-color: #f97316;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-weight: 600;
  font-size: 0.9rem;
}

.card-header .icon {
  font-size: 1.2rem;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.85rem;
}

.http-method {
  display: inline-block;
  padding: 0.125rem 0.5rem;
  border-radius: 3px;
  font-size: 0.75rem;
  font-weight: 700;
  color: white;
}

.http-method.GET { background-color: #22c55e; }
.http-method.POST { background-color: #3b82f6; }
.http-method.PUT { background-color: #f59e0b; }
.http-method.DELETE { background-color: #ef4444; }

.path {
  font-family: 'Courier New', monospace;
  color: #065f46;
  font-weight: 500;
}

.class-name {
  color: #6b7280;
  font-size: 0.8rem;
  font-family: 'Courier New', monospace;
}

.method-name {
  font-family: 'Courier New', monospace;
  color: #4b5563;
}

.db-op, .log-item, .exception-item {
  padding: 0.25rem 0.5rem;
  background: white;
  border-radius: 3px;
  margin-bottom: 0.25rem;
}

.op-type, .log-level {
  font-weight: 600;
  padding: 0.125rem 0.375rem;
  border-radius: 3px;
  font-size: 0.7rem;
  margin-right: 0.5rem;
}

.op-type { background: #ddd6fe; color: #5b21b6; }
.log-level.ERROR { background: #fee2e2; color: #991b1b; }
.log-level.WARN { background: #fef3c7; color: #92400e; }
.log-level.INFO { background: #dbeafe; color: #1e40af; }
.log-level.DEBUG { background: #e0e7ff; color: #3730a3; }

.more-items {
  color: #9ca3af;
  font-style: italic;
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

/* Parameters */
.parameters-list, .variables-list, .transfers-list, .returns-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.parameter-item, .variable-item, .transfer-item, .return-item {
  padding: 0.75rem;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.param-header, .var-header, .transfer-header, .return-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.param-name, .var-name {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #b45309;
}

.param-type, .var-type, .return-type {
  font-family: 'Courier New', monospace;
  color: #6b7280;
  font-size: 0.85rem;
}

.badge {
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: 600;
}

.input-badge {
  background: #bbf7d0;
  color: #166534;
}

.transform-badge {
  background: #dbeafe;
  color: #1e40af;
}

.var-source, .var-expression, .var-location, .return-source {
  font-size: 0.8rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

.transfer-target {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #2563eb;
}

.transfer-line, .return-line {
  color: #9ca3af;
  font-size: 0.75rem;
}

.transfer-args {
  margin-top: 0.5rem;
  padding-left: 1rem;
  border-left: 2px solid #e5e7eb;
}

.arg-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  margin-bottom: 0.25rem;
}

.arg-pos {
  background: #f3f4f6;
  padding: 0.125rem 0.375rem;
  border-radius: 3px;
  font-weight: 600;
  color: #4b5563;
  font-size: 0.7rem;
}

.arg-type {
  color: #6b7280;
  font-family: 'Courier New', monospace;
}

.arg-var {
  color: #b45309;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

.arg-expr {
  color: #059669;
  font-family: 'Courier New', monospace;
}

.return-expression {
  font-family: 'Courier New', monospace;
  color: #059669;
  background: #f0fdf4;
  padding: 0.5rem;
  border-radius: 4px;
  margin-top: 0.5rem;
}

/* Actions */
.actions {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  background: white;
  border: none;
  position: sticky;
  bottom: 0;
  flex-wrap: wrap;
}

.action-btn {
  flex: 1;
  min-width: 150px;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.action-btn.timeline-btn {
  background: linear-gradient(135deg, #fef3c7 0%, #fed7aa 100%);
  color: #92400e;
  border: 2px solid #f59e0b;
  flex: 1 1 100%;
}

.action-btn.timeline-btn:hover {
  background: linear-gradient(135deg, #fed7aa 0%, #fbbf24 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.action-btn.primary {
  background: #f59e0b;
  color: white;
}

.action-btn.primary:hover {
  background: #d97706;
}

.action-btn.secondary {
  background: #e5e7eb;
  color: #374151;
}

.action-btn.secondary:hover {
  background: #d1d5db;
}
</style>
