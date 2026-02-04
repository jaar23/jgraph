<template>
  <div v-if="methodSource" class="method-source-panel">
    <!-- Summary Section -->
    <div class="source-section summary-section">
      <h4>📝 Summary</h4>
      <p class="summary-text">{{ methodSource.summary || 'No summary available' }}</p>
    </div>

    <!-- Complexity Metrics -->
    <div class="source-section complexity-section">
      <h4>📊 Complexity Metrics</h4>
      <div class="metrics-grid">
        <div class="metric-item">
          <span class="metric-label">Cyclomatic:</span>
          <span class="metric-value" :class="getComplexityClass(methodSource.complexity.cyclomaticComplexity, 10, 20)">
            {{ methodSource.complexity.cyclomaticComplexity }}
          </span>
        </div>
        <div class="metric-item">
          <span class="metric-label">Cognitive:</span>
          <span class="metric-value" :class="getComplexityClass(methodSource.complexity.cognitiveComplexity, 15, 30)">
            {{ methodSource.complexity.cognitiveComplexity }}
          </span>
        </div>
        <div class="metric-item">
          <span class="metric-label">LOC:</span>
          <span class="metric-value">{{ methodSource.complexity.linesOfCode }}</span>
        </div>
        <div class="metric-item">
          <span class="metric-label">Nesting Depth:</span>
          <span class="metric-value" :class="getComplexityClass(methodSource.complexity.nestingDepth, 3, 5)">
            {{ methodSource.complexity.nestingDepth }}
          </span>
        </div>
        <div class="metric-item">
          <span class="metric-label">Maintainability:</span>
          <span class="metric-value" :class="getMaintainabilityClass(methodSource.complexity.maintainabilityIndex)">
            {{ methodSource.complexity.maintainabilityIndex.toFixed(1) }}
          </span>
        </div>
        <div class="metric-item">
          <span class="metric-label">Statements:</span>
          <span class="metric-value">{{ methodSource.complexity.statementCount }}</span>
        </div>
      </div>
    </div>

    <!-- Operations Summary -->
    <div class="source-section operations-section">
      <h4>⚙️ Operations</h4>
      <div class="operations-grid">
        <div v-if="methodSource.operations.conditionals > 0" class="operation-item">
          <span class="operation-icon">🔀</span>
          <span class="operation-count">{{ methodSource.operations.conditionals }}</span>
          <span class="operation-label">Conditionals</span>
        </div>
        <div v-if="methodSource.operations.loops > 0" class="operation-item">
          <span class="operation-icon">🔄</span>
          <span class="operation-count">{{ methodSource.operations.loops }}</span>
          <span class="operation-label">Loops</span>
        </div>
        <div v-if="methodSource.operations.methodCalls > 0" class="operation-item">
          <span class="operation-icon">📞</span>
          <span class="operation-count">{{ methodSource.operations.methodCalls }}</span>
          <span class="operation-label">Method Calls</span>
        </div>
        <div v-if="methodSource.operations.exceptionHandling > 0" class="operation-item">
          <span class="operation-icon">⚠️</span>
          <span class="operation-count">{{ methodSource.operations.exceptionHandling }}</span>
          <span class="operation-label">Exception Handling</span>
        </div>
        <div v-if="methodSource.operations.lambdas > 0" class="operation-item">
          <span class="operation-icon">λ</span>
          <span class="operation-count">{{ methodSource.operations.lambdas }}</span>
          <span class="operation-label">Lambdas</span>
        </div>
        <div v-if="methodSource.operations.streams > 0" class="operation-item">
          <span class="operation-icon">🌊</span>
          <span class="operation-count">{{ methodSource.operations.streams }}</span>
          <span class="operation-label">Streams</span>
        </div>
      </div>
    </div>

    <!-- Logic Flow -->
    <div v-if="methodSource.logicFlow" class="source-section logic-flow-section">
      <h4>🔄 Logic Flow</h4>
      <div class="logic-steps">
        <div
          v-for="step in methodSource.logicFlow.steps"
          :key="step.step"
          class="logic-step"
        >
          <div class="step-number">{{ step.step }}</div>
          <div class="step-content">
            <div class="step-operation" :class="getOperationClass(step.operation)">
              {{ formatOperation(step.operation) }}
            </div>
            <div class="step-description">{{ step.description }}</div>
            <div v-if="step.methods && step.methods.length > 0" class="step-methods">
              <span class="step-methods-label">Methods:</span>
              <code>{{ step.methods.join(', ') }}</code>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Code Blocks (Collapsible) -->
    <div v-if="methodSource.codeBlocks && methodSource.codeBlocks.length > 0" class="source-section code-blocks-section">
      <div class="section-header" @click="showCodeBlocks = !showCodeBlocks">
        <h4>💻 Code Structure</h4>
        <span class="toggle-icon">{{ showCodeBlocks ? '▼' : '▶' }}</span>
      </div>
      <div v-if="showCodeBlocks" class="code-blocks">
        <CodeBlock
          v-for="(block, index) in methodSource.codeBlocks"
          :key="index"
          :block="block"
          :depth="0"
        />
      </div>
    </div>

    <!-- Variables (if detailed level) -->
    <div v-if="methodSource.variables && Object.keys(methodSource.variables).length > 0" class="source-section variables-section">
      <div class="section-header" @click="showVariables = !showVariables">
        <h4>📦 Variable Lifecycles</h4>
        <span class="toggle-icon">{{ showVariables ? '▼' : '▶' }}</span>
      </div>
      <div v-if="showVariables" class="variables-list">
        <div
          v-for="(lifecycle, varName) in methodSource.variables"
          :key="varName"
          class="variable-item"
        >
          <div class="variable-header">
            <code class="variable-name">{{ varName }}</code>
            <span class="variable-type">{{ lifecycle.type }}</span>
          </div>
          <div class="variable-lifecycle">
            <div class="lifecycle-item">
              <span class="lifecycle-label">Declared:</span>
              <span class="lifecycle-value">Line {{ lifecycle.declaredAt }}</span>
            </div>
            <div class="lifecycle-item">
              <span class="lifecycle-label">Usages:</span>
              <span class="lifecycle-value">{{ lifecycle.usages.length }}</span>
            </div>
            <div class="lifecycle-item">
              <span class="lifecycle-label">Last used:</span>
              <span class="lifecycle-value">Line {{ lifecycle.lastUsedAt }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="no-source-message">
    <p>No source code details available for this method.</p>
    <p class="hint">Upload the analysis-source-map.json file to see detailed source information.</p>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import CodeBlock from './source/CodeBlock.vue'

const props = defineProps({
  methodSource: {
    type: Object,
    default: null
  }
})

const showCodeBlocks = ref(false)
const showVariables = ref(false)

function getComplexityClass(value, warningThreshold, errorThreshold) {
  if (value >= errorThreshold) return 'complexity-high'
  if (value >= warningThreshold) return 'complexity-medium'
  return 'complexity-low'
}

function getMaintainabilityClass(value) {
  if (value >= 80) return 'complexity-low'
  if (value >= 60) return 'complexity-medium'
  return 'complexity-high'
}

function getOperationClass(operation) {
  const classMap = {
    'QUERY_DATABASE': 'op-database',
    'UPDATE_DATABASE': 'op-database',
    'CALL_EXTERNAL_SERVICE': 'op-external',
    'AUTHENTICATE': 'op-auth',
    'VALIDATE_AUTH': 'op-auth',
    'VALIDATE': 'op-validation',
    'CHECK_PERMISSION': 'op-permission',
    'HANDLE_ERROR': 'op-error',
    'TRANSFORM_DATA': 'op-transform',
    'PROCESS_STREAM': 'op-stream'
  }
  return classMap[operation] || 'op-default'
}

function formatOperation(operation) {
  return operation.replace(/_/g, ' ').toLowerCase()
    .replace(/\b\w/g, l => l.toUpperCase())
}
</script>

<style scoped>
.method-source-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0;
}

.source-section {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 1rem;
}

.source-section h4 {
  margin: 0 0 0.75rem 0;
  color: #2c3e50;
  font-size: 0.9rem;
  font-weight: 600;
}

.summary-section {
  background: #e3f2fd;
  border-left: 4px solid #2196f3;
}

.summary-text {
  margin: 0;
  color: #1976d2;
  font-size: 0.95rem;
  line-height: 1.5;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 0.75rem;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.metric-label {
  font-size: 0.75rem;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.metric-value {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
}

.complexity-low {
  color: #27ae60;
}

.complexity-medium {
  color: #f39c12;
}

.complexity-high {
  color: #e74c3c;
}

.operations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 0.5rem;
}

.operation-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  background: white;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.operation-icon {
  font-size: 1.25rem;
}

.operation-count {
  font-weight: 600;
  color: #2c3e50;
  min-width: 20px;
}

.operation-label {
  font-size: 0.8rem;
  color: #666;
}

.logic-steps {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.logic-step {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #3498db;
}

.step-number {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3498db;
  color: white;
  border-radius: 50%;
  font-weight: 600;
  font-size: 0.85rem;
}

.step-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.step-operation {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 2px 6px;
  border-radius: 3px;
  display: inline-block;
  width: fit-content;
}

.op-database { background: #e8f5e9; color: #2e7d32; }
.op-external { background: #fff3e0; color: #e65100; }
.op-auth { background: #f3e5f5; color: #6a1b9a; }
.op-validation { background: #e1f5fe; color: #01579b; }
.op-permission { background: #fce4ec; color: #c2185b; }
.op-error { background: #ffebee; color: #c62828; }
.op-transform { background: #fff9c4; color: #f57f17; }
.op-stream { background: #e0f2f1; color: #00695c; }
.op-default { background: #f5f5f5; color: #616161; }

.step-description {
  color: #555;
  font-size: 0.9rem;
}

.step-methods {
  font-size: 0.8rem;
  color: #666;
  margin-top: 0.25rem;
}

.step-methods-label {
  font-weight: 600;
  margin-right: 0.25rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.toggle-icon {
  color: #666;
  font-size: 0.9rem;
}

.code-blocks {
  margin-top: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.variables-list {
  margin-top: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.variable-item {
  background: white;
  padding: 0.75rem;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.variable-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.variable-name {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #2c3e50;
}

.variable-type {
  font-size: 0.8rem;
  color: #666;
  font-family: 'Courier New', monospace;
}

.variable-lifecycle {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.lifecycle-item {
  display: flex;
  gap: 0.5rem;
  font-size: 0.85rem;
}

.lifecycle-label {
  color: #666;
  font-weight: 500;
}

.lifecycle-value {
  color: #2c3e50;
  font-family: 'Courier New', monospace;
}

.no-source-message {
  padding: 2rem;
  text-align: center;
  color: #666;
}

.no-source-message .hint {
  font-size: 0.9rem;
  color: #999;
  margin-top: 0.5rem;
}
</style>
