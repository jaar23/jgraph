<template>
  <div class="database-node" :class="{ 'transactional': node.data.isTransactional }">
    <div class="node-header">
      <span class="node-icon">🗄️</span>
      <span class="node-type">{{ node.data.operationType || 'Query' }}</span>
      <span v-if="node.data.isNativeQuery" class="native-badge">Native</span>
    </div>
    
    <div class="node-content">
      <!-- Database Operation -->
      <div v-if="node.data.query" class="query-details">
        <div class="query-text">
          {{ formatQuery(node.data.query) }}
        </div>
        <div class="query-meta">
          <span class="framework-badge">{{ node.data.type || 'JPA' }}</span>
        </div>
      </div>
      
      <!-- Transaction Info -->
      <div v-if="node.data.propagation" class="transaction-details">
        <div class="transaction-header">
          <strong>Transaction</strong>
          <span v-if="node.data.readOnly" class="readonly-badge">Read-Only</span>
        </div>
        <div class="transaction-props">
          <div class="prop">
            <span class="prop-label">Propagation:</span>
            <span class="prop-value">{{ node.data.propagation }}</span>
          </div>
          <div v-if="node.data.isolation" class="prop">
            <span class="prop-label">Isolation:</span>
            <span class="prop-value">{{ node.data.isolation }}</span>
          </div>
          <div v-if="node.data.timeout && node.data.timeout !== -1" class="prop">
            <span class="prop-label">Timeout:</span>
            <span class="prop-value">{{ node.data.timeout }}s</span>
          </div>
        </div>
      </div>
      
      <div class="node-location">
        <span class="method-name">{{ node.data.methodName }}</span>
        <span class="line-number">:{{ node.data.lineNumber }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DatabaseNode',
  props: {
    node: {
      type: Object,
      required: true
    }
  },
  methods: {
    formatQuery(query) {
      if (!query) return '';
      // Truncate long queries
      const maxLength = 60;
      if (query.length <= maxLength) return query;
      return query.substring(0, maxLength) + '...';
    }
  }
}
</script>

<style scoped>
.database-node {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 2px solid #3b82f6;
  border-radius: 8px;
  padding: 12px;
  min-width: 240px;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.database-node.transactional {
  border-color: #8b5cf6;
  background: linear-gradient(135deg, #faf5ff 0%, #f3e8ff 100%);
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #3b82f6;
}

.database-node.transactional .node-header {
  border-bottom-color: #8b5cf6;
}

.node-icon {
  font-size: 20px;
}

.node-type {
  font-weight: 600;
  color: #1e40af;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.database-node.transactional .node-type {
  color: #6b21a8;
}

.native-badge {
  background: #fbbf24;
  color: #78350f;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 9px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.node-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.query-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.query-text {
  background: #fff;
  border: 1px solid #3b82f6;
  border-radius: 4px;
  padding: 8px;
  font-size: 11px;
  color: #1e40af;
  font-family: 'Courier New', monospace;
  line-height: 1.5;
  word-break: break-word;
}

.database-node.transactional .query-text {
  border-color: #8b5cf6;
  color: #6b21a8;
}

.query-meta {
  display: flex;
  gap: 6px;
}

.framework-badge {
  background: #dbeafe;
  color: #1e40af;
  padding: 3px 8px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.database-node.transactional .framework-badge {
  background: #e9d5ff;
  color: #6b21a8;
}

.transaction-details {
  background: #f3e8ff;
  border: 1px solid #8b5cf6;
  border-radius: 6px;
  padding: 8px;
}

.transaction-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.transaction-header strong {
  font-size: 11px;
  color: #6b21a8;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.readonly-badge {
  background: #bfdbfe;
  color: #1e3a8a;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 9px;
  font-weight: 600;
  text-transform: uppercase;
}

.transaction-props {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.prop {
  display: flex;
  gap: 6px;
  font-size: 11px;
}

.prop-label {
  color: #7c3aed;
  font-weight: 600;
}

.prop-value {
  color: #5b21b6;
  font-family: 'Courier New', monospace;
}

.node-location {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid #3b82f6;
  font-size: 11px;
}

.database-node.transactional .node-location {
  border-top-color: #8b5cf6;
}

.method-name {
  color: #1e40af;
  font-weight: 500;
  font-family: 'Courier New', monospace;
}

.database-node.transactional .method-name {
  color: #6b21a8;
}

.line-number {
  color: #2563eb;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

.database-node.transactional .line-number {
  color: #7c3aed;
}
</style>
