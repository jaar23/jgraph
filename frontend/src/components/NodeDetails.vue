<template>
  <div v-if="node" class="node-details">
    <div class="details-header">
      <h3>{{ getTypeLabel(node.type) }}</h3>
      <button @click="close" class="close-btn">×</button>
    </div>

    <div class="details-content">
      <!-- Endpoint Details -->
      <div v-if="node.type === 'endpoint'" class="detail-section">
        <div class="detail-row">
          <span class="label">HTTP Method:</span>
          <span class="value method-badge" :class="node.httpMethod">{{ node.httpMethod }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Path:</span>
          <span class="value code">{{ node.path }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Controller:</span>
          <span class="value code">{{ node.controllerClass }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Method:</span>
          <span class="value code">{{ node.methodName }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Return Type:</span>
          <span class="value code">{{ node.returnType }}</span>
        </div>
        
        <!-- Parameters -->
        <div v-if="node.parameters && node.parameters.length > 0" class="detail-row">
          <span class="label">Parameters:</span>
          <div class="params-list">
            <div v-for="(param, index) in node.parameters" :key="index" class="param-item">
              <span class="param-annotation">{{ param.annotation }}</span>
              <span class="param-type">{{ param.type }}</span>
              <span class="param-name">{{ param.name }}</span>
            </div>
          </div>
        </div>

        <!-- Call Chain -->
        <div v-if="node.callChain && node.callChain.length > 0" class="detail-row">
          <span class="label">Calls:</span>
          <div class="calls-list">
            <div v-for="(call, index) in node.callChain" :key="index" class="call-item">
              {{ call }}
            </div>
          </div>
        </div>
      </div>

      <!-- Service Details -->
      <div v-if="node.type === 'service'" class="detail-section">
        <div class="detail-row">
          <span class="label">Class:</span>
          <span class="value code">{{ node.className }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Method:</span>
          <span class="value code">{{ node.methodName }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Return Type:</span>
          <span class="value code">{{ node.returnType }}</span>
        </div>

        <!-- Parameters -->
        <div v-if="node.parameters && node.parameters.length > 0" class="detail-row">
          <span class="label">Parameters:</span>
          <div class="params-list">
            <div v-for="(param, index) in node.parameters" :key="index" class="param-item code">
              {{ param }}
            </div>
          </div>
        </div>

        <!-- Calls -->
        <div v-if="node.calls && node.calls.length > 0" class="detail-row">
          <span class="label">Calls:</span>
          <div class="calls-list">
            <div v-for="(call, index) in node.calls" :key="index" class="call-item">
              {{ call }}
            </div>
          </div>
        </div>

        <!-- Called By -->
        <div v-if="node.calledBy && node.calledBy.length > 0" class="detail-row">
          <span class="label">Called By:</span>
          <div class="calls-list">
            <div v-for="(caller, index) in node.calledBy" :key="index" class="call-item">
              {{ caller }}
            </div>
          </div>
        </div>
      </div>

      <!-- Repository Details -->
      <div v-if="node.type === 'repository'" class="detail-section">
        <div class="detail-row">
          <span class="label">Class:</span>
          <span class="value code">{{ node.className }}</span>
        </div>
        <div v-if="node.interfaceName" class="detail-row">
          <span class="label">Interface:</span>
          <span class="value code">{{ node.interfaceName }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Method:</span>
          <span class="value code">{{ node.methodName }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Return Type:</span>
          <span class="value code">{{ node.returnType }}</span>
        </div>

        <!-- Parameters -->
        <div v-if="node.parameters && node.parameters.length > 0" class="detail-row">
          <span class="label">Parameters:</span>
          <div class="params-list">
            <div v-for="(param, index) in node.parameters" :key="index" class="param-item code">
              {{ param }}
            </div>
          </div>
        </div>

        <!-- Called By -->
        <div v-if="node.calledBy && node.calledBy.length > 0" class="detail-row">
          <span class="label">Called By:</span>
          <div class="calls-list">
            <div v-for="(caller, index) in node.calledBy" :key="index" class="call-item">
              {{ caller }}
            </div>
          </div>
        </div>
      </div>

      <!-- Javadoc -->
      <div v-if="node.javadoc" class="detail-row">
        <span class="label">Documentation:</span>
        <div class="javadoc">{{ node.javadoc }}</div>
      </div>

      <!-- Annotations -->
      <div v-if="node.annotations && node.annotations.length > 0" class="detail-row">
        <span class="label">Annotations:</span>
        <div class="annotations-list">
          <span v-for="(ann, index) in node.annotations" :key="index" class="annotation-badge">
            {{ ann }}
          </span>
        </div>
      </div>

      <!-- File Location -->
      <div v-if="node.filePath" class="detail-row">
        <span class="label">Location:</span>
        <span class="value code small">{{ node.filePath }}:{{ node.lineNumber }}</span>
      </div>
    </div>
  </div>

  <div v-else class="no-selection">
    <p>Click on a node to view details</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAnalysisStore } from '../stores/analysisStore'

const store = useAnalysisStore()
const node = computed(() => store.selectedNode)

function getTypeLabel(type) {
  const labels = {
    endpoint: 'Endpoint',
    service: 'Service',
    repository: 'Repository'
  }
  return labels[type] || type
}

function close() {
  store.setSelectedNode(null)
}
</script>

<style scoped>
.node-details {
  height: 100%;
  overflow-y: auto;
  background-color: white;
  border-left: 1px solid #e0e0e0;
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
  background-color: #f8f9fa;
}

.details-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.details-content {
  padding: 1rem;
}

.detail-section {
  margin-bottom: 1rem;
}

.detail-row {
  margin-bottom: 1rem;
}

.label {
  display: block;
  font-weight: 600;
  color: #666;
  margin-bottom: 0.25rem;
  font-size: 0.875rem;
  text-transform: uppercase;
}

.value {
  display: block;
  color: #2c3e50;
}

.code {
  font-family: 'Courier New', monospace;
  background-color: #f5f5f5;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  display: inline-block;
}

.code.small {
  font-size: 0.75rem;
}

.method-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.875rem;
  color: white;
}

.method-badge.GET { background-color: #2ecc71; }
.method-badge.POST { background-color: #3498db; }
.method-badge.PUT { background-color: #f39c12; }
.method-badge.DELETE { background-color: #e74c3c; }
.method-badge.PATCH { background-color: #9b59b6; }

.params-list,
.calls-list {
  margin-top: 0.5rem;
}

.param-item,
.call-item {
  padding: 0.5rem;
  background-color: #f8f9fa;
  border-left: 3px solid #3498db;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.param-annotation {
  color: #9b59b6;
  margin-right: 0.5rem;
  font-weight: 600;
}

.param-type {
  color: #e67e22;
  margin-right: 0.5rem;
}

.param-name {
  color: #2c3e50;
  font-weight: 600;
}

.javadoc {
  background-color: #f8f9fa;
  padding: 0.75rem;
  border-radius: 4px;
  white-space: pre-wrap;
  font-size: 0.875rem;
  line-height: 1.6;
  color: #555;
}

.annotations-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.annotation-badge {
  background-color: #9b59b6;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.875rem;
  font-family: 'Courier New', monospace;
}

.no-selection {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  font-style: italic;
}
</style>
