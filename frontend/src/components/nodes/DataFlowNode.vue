<template>
  <div class="dataflow-node" :class="{ 'clickable': true }">
    <div class="node-header">
      <span class="node-icon">📊</span>
      <span class="node-type">Data Flow</span>
      <span class="click-hint" title="Click to see full data flow details">🔍</span>
    </div>
    
    <div class="node-content">
      <!-- Parameters -->
      <div v-if="node.data.parameters && node.data.parameters.length > 0" class="section">
        <div class="section-title">Parameters</div>
        <div v-for="param in node.data.parameters" :key="param.name" class="param">
          <span class="param-name">{{ param.name }}</span>
          <span class="param-type">: {{ param.type }}</span>
          <span v-if="param.isInputData" class="input-badge">Input</span>
        </div>
      </div>
      
      <!-- Variables -->
      <div v-if="node.data.variables && node.data.variables.length > 0" class="section">
        <div class="section-title">Transformations</div>
        <div v-for="variable in node.data.variables.slice(0, 3)" :key="variable.variableName" 
             class="variable">
          <span class="var-name">{{ variable.variableName }}</span>
          <span class="transform-type">{{ variable.transformationType }}</span>
        </div>
      </div>
      
      <!-- Return Values -->
      <div v-if="node.data.returns && node.data.returns.length > 0" class="section">
        <div class="section-title">Returns</div>
        <div v-for="(ret, idx) in node.data.returns.slice(0, 2)" :key="idx" class="return-val">
          {{ ret.returnType }}: {{ truncate(ret.expression, 30) }}
        </div>
      </div>
      
      <div class="node-location">
        <span class="method-name">{{ node.data.methodName }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataFlowNode',
  props: {
    node: {
      type: Object,
      required: true
    }
  },
  methods: {
    truncate(str, maxLength) {
      if (!str || str.length <= maxLength) return str
      return str.substring(0, maxLength) + '...'
    }
  }
}
</script>

<style scoped>
.dataflow-node {
  background: linear-gradient(135deg, #fef3c7 0%, #fed7aa 100%);
  border: 2px solid #f59e0b;
  border-radius: 8px;
  padding: 12px;
  min-width: 260px;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.15);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  cursor: pointer;
  transition: all 0.2s;
}

.dataflow-node:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.25);
  border-color: #d97706;
}

.click-hint {
  margin-left: auto;
  font-size: 14px;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.dataflow-node:hover .click-hint {
  opacity: 1;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f59e0b;
}

.node-icon {
  font-size: 20px;
}

.node-type {
  font-weight: 600;
  color: #92400e;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.node-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.section-title {
  font-size: 10px;
  font-weight: 700;
  color: #92400e;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.param, .variable, .return-val {
  background: #fff;
  border: 1px solid #fbbf24;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 11px;
  font-family: 'Courier New', monospace;
}

.param-name, .var-name {
  color: #b45309;
  font-weight: 600;
}

.param-type {
  color: #78350f;
  font-size: 10px;
}

.input-badge {
  background: #bbf7d0;
  color: #166534;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 9px;
  font-weight: 600;
  margin-left: 6px;
}

.transform-type {
  background: #dbeafe;
  color: #1e40af;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 9px;
  font-weight: 600;
  margin-left: 6px;
}

.node-location {
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid #f59e0b;
}

.method-name {
  color: #92400e;
  font-weight: 500;
  font-size: 11px;
  font-family: 'Courier New', monospace;
}
</style>
