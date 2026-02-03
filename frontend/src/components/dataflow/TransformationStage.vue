<template>
  <div class="transformation-stage">
    <div class="stage-header">
      <div class="stage-title">
        <span class="stage-icon">{{ stage.icon }}</span>
        <h4>{{ stage.name }} Stage</h4>
      </div>
      <div class="stage-description">{{ stage.description }}</div>
    </div>

    <div class="stage-items">
      <!-- Parameters Stage -->
      <div v-if="stage.type === 'parameters'" class="parameters-grid">
        <div v-for="param in stage.data" :key="param.name" class="stage-card parameter-card">
          <div class="card-header">
            <span class="item-name">{{ param.name }}</span>
            <span v-if="param.isInputData" class="badge input-badge">Input Data</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="label">Type:</span>
              <span class="value code">{{ param.type }}</span>
            </div>
            <div v-if="param.annotations?.length" class="info-row">
              <span class="label">Annotations:</span>
              <div class="annotations">
                <span v-for="ann in param.annotations" :key="ann" class="annotation">{{ ann }}</span>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <span class="flow-arrow">↓ Used in method body</span>
          </div>
        </div>
      </div>

      <!-- Variables Stage -->
      <div v-if="stage.type === 'variables'" class="variables-grid">
        <div v-for="variable in stage.data" :key="variable.variableName" class="stage-card variable-card">
          <div class="card-header">
            <span class="item-name">{{ variable.variableName }}</span>
            <span class="badge transform-badge">{{ variable.transformationType }}</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="label">Type:</span>
              <span class="value code">{{ variable.variableType }}</span>
            </div>
            <div v-if="variable.sourceVariable" class="info-row">
              <span class="label">Source:</span>
              <span class="value code highlight">{{ variable.sourceVariable }}</span>
            </div>
            <div v-if="variable.assignmentExpression" class="info-row expression">
              <span class="label">Expression:</span>
              <div class="value code-block">{{ variable.assignmentExpression }}</div>
            </div>
            <div class="info-row">
              <span class="label">Location:</span>
              <span class="value location-badge">Line {{ variable.lineNumber }}</span>
            </div>
          </div>
          <div class="card-footer">
            <div class="transformation-flow">
              <span v-if="variable.sourceVariable" class="flow-step">{{ variable.sourceVariable }}</span>
              <span class="flow-arrow">→</span>
              <span class="flow-step highlight">{{ variable.variableName }}</span>
              <span v-if="isVariableUsedInTransfers(variable.variableName)" class="flow-arrow">→</span>
              <span v-if="isVariableUsedInTransfers(variable.variableName)" class="flow-step">transferred</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Data Transfers Stage -->
      <div v-if="stage.type === 'transfers'" class="transfers-grid">
        <div v-for="transfer in stage.data" :key="transfer.targetMethod" class="stage-card transfer-card">
          <div class="card-header">
            <span class="item-name">{{ transfer.targetMethod }}()</span>
            <span class="badge transfer-badge">{{ transfer.arguments?.length || 0 }} args</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="label">Location:</span>
              <span class="value location-badge">Line {{ transfer.lineNumber }}</span>
            </div>
            <div v-if="transfer.arguments?.length" class="arguments-list">
              <div class="arguments-header">Arguments:</div>
              <div v-for="arg in transfer.arguments" :key="arg.position" class="argument-item">
                <span class="arg-position">#{{ arg.position }}</span>
                <div class="arg-details">
                  <div class="arg-type code">{{ arg.type }}</div>
                  <div v-if="arg.variableName" class="arg-variable">
                    Variable: <span class="code highlight">{{ arg.variableName }}</span>
                  </div>
                  <div v-if="arg.expression" class="arg-expression code-block">
                    {{ truncate(arg.expression, 60) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <span class="flow-arrow">↗ Data sent to {{ transfer.targetMethod }}()</span>
          </div>
        </div>
      </div>

      <!-- Returns Stage -->
      <div v-if="stage.type === 'returns'" class="returns-grid">
        <div v-for="(ret, idx) in stage.data" :key="idx" class="stage-card return-card">
          <div class="card-header">
            <span class="item-name">Return Statement {{ idx + 1 }}</span>
            <span class="badge return-badge">{{ ret.returnType }}</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="label">Type:</span>
              <span class="value code">{{ ret.returnType }}</span>
            </div>
            <div class="info-row">
              <span class="label">Location:</span>
              <span class="value location-badge">Line {{ ret.lineNumber }}</span>
            </div>
            <div v-if="ret.sourceVariable" class="info-row">
              <span class="label">Source Variable:</span>
              <span class="value code highlight">{{ ret.sourceVariable }}</span>
            </div>
            <div class="info-row expression">
              <span class="label">Expression:</span>
              <div class="value code-block return-expression">{{ ret.expression }}</div>
            </div>
          </div>
          <div class="card-footer">
            <span class="flow-arrow">↑ Returned to caller</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Summary Statistics -->
    <div class="stage-summary">
      <div class="summary-stat">
        <span class="stat-label">Total Items:</span>
        <span class="stat-value">{{ stage.count }}</span>
      </div>
      <div v-if="stage.type === 'variables'" class="summary-stat">
        <span class="stat-label">Transformation Types:</span>
        <span class="stat-value">{{ uniqueTransformationTypes }}</span>
      </div>
      <div v-if="stage.type === 'transfers'" class="summary-stat">
        <span class="stat-label">Total Arguments:</span>
        <span class="stat-value">{{ totalArguments }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  stage: {
    type: Object,
    required: true
  },
  dataflow: {
    type: Object,
    required: true
  }
})

const uniqueTransformationTypes = computed(() => {
  if (props.stage.type !== 'variables') return 0
  const types = new Set(props.stage.data.map(v => v.transformationType))
  return types.size
})

const totalArguments = computed(() => {
  if (props.stage.type !== 'transfers') return 0
  return props.stage.data.reduce((sum, t) => sum + (t.arguments?.length || 0), 0)
})

function isVariableUsedInTransfers(variableName) {
  if (!props.dataflow.dataTransfers) return false
  return props.dataflow.dataTransfers.some(transfer => 
    transfer.arguments?.some(arg => arg.variableName === variableName)
  )
}

function truncate(str, maxLength) {
  if (!str || str.length <= maxLength) return str
  return str.substring(0, maxLength) + '...'
}
</script>

<style scoped>
.transformation-stage {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.stage-header {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.stage-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.stage-icon {
  font-size: 2rem;
}

.stage-title h4 {
  margin: 0;
  font-size: 1.5rem;
  color: #111827;
}

.stage-description {
  color: #6b7280;
  font-size: 0.95rem;
  line-height: 1.5;
}

/* Grid Layouts */
.parameters-grid,
.variables-grid,
.transfers-grid,
.returns-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.25rem;
}

/* Stage Cards */
.stage-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.stage-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.parameter-card {
  border-color: #bbf7d0;
}

.parameter-card:hover {
  border-color: #22c55e;
}

.variable-card {
  border-color: #fef3c7;
}

.variable-card:hover {
  border-color: #f59e0b;
}

.transfer-card {
  border-color: #dbeafe;
}

.transfer-card:hover {
  border-color: #3b82f6;
}

.return-card {
  border-color: #e0e7ff;
}

.return-card:hover {
  border-color: #6366f1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.item-name {
  font-weight: 600;
  color: #111827;
  font-family: 'Courier New', monospace;
  font-size: 0.95rem;
}

.card-body {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.card-footer {
  padding: 0.75rem 1rem;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
  font-size: 0.85rem;
  color: #6b7280;
}

.info-row {
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
}

.info-row.expression {
  flex-direction: column;
  gap: 0.25rem;
}

.label {
  font-weight: 600;
  color: #6b7280;
  font-size: 0.85rem;
  min-width: 80px;
}

.value {
  color: #111827;
  font-size: 0.85rem;
}

.code {
  font-family: 'Courier New', monospace;
  background: #f3f4f6;
  padding: 0.25rem 0.5rem;
  border-radius: 3px;
  font-size: 0.8rem;
}

.code-block {
  font-family: 'Courier New', monospace;
  background: #f3f4f6;
  padding: 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  line-height: 1.4;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.highlight {
  background: #fef3c7;
  color: #92400e;
  font-weight: 600;
}

/* Badges */
.badge {
  padding: 0.25rem 0.625rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.input-badge {
  background: #bbf7d0;
  color: #166534;
}

.transform-badge {
  background: #fef3c7;
  color: #92400e;
}

.transfer-badge {
  background: #dbeafe;
  color: #1e40af;
}

.return-badge {
  background: #e0e7ff;
  color: #4338ca;
}

.location-badge {
  background: #e5e7eb;
  color: #374151;
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 600;
}

/* Annotations */
.annotations {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.annotation {
  background: #dbeafe;
  color: #1e40af;
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: 500;
  font-family: 'Courier New', monospace;
}

/* Transformation Flow */
.transformation-flow {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.flow-step {
  background: #f3f4f6;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #4b5563;
}

.flow-step.highlight {
  background: #fef3c7;
  color: #92400e;
}

.flow-arrow {
  color: #9ca3af;
  font-weight: 600;
}

/* Arguments List */
.arguments-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.arguments-header {
  font-weight: 600;
  color: #374151;
  font-size: 0.85rem;
  margin-bottom: -0.25rem;
}

.argument-item {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
  padding: 0.75rem;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.arg-position {
  background: #3b82f6;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 700;
  min-width: 30px;
  text-align: center;
}

.arg-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.arg-type {
  color: #6b7280;
  font-size: 0.8rem;
}

.arg-variable {
  font-size: 0.8rem;
  color: #4b5563;
}

.arg-expression {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: #059669;
}

.return-expression {
  background: #f0fdf4;
  color: #059669;
  border: 1px solid #bbf7d0;
}

/* Stage Summary */
.stage-summary {
  display: flex;
  gap: 2rem;
  padding: 1.25rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 2px solid #f59e0b;
}

.summary-stat {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-label {
  font-size: 0.8rem;
  color: #6b7280;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #f59e0b;
}
</style>
