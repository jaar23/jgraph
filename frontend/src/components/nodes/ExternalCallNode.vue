<template>
  <div class="external-call-node" :class="callTypeClass">
    <div class="node-header">
      <span class="node-icon">{{ getIcon() }}</span>
      <span class="node-type">{{ node.data.type || 'External Call' }}</span>
      <span v-if="node.data.isAsync" class="async-badge">Async</span>
    </div>
    
    <div class="node-content">
      <!-- REST Call -->
      <div v-if="node.data.type === 'REST'" class="call-details">
        <div class="http-method" :class="methodClass">
          {{ node.data.method }}
        </div>
        <div class="endpoint">
          {{ formatEndpoint(node.data.endpoint) }}
        </div>
        <div class="client-badge">
          {{ node.data.clientType || 'RestTemplate' }}
        </div>
      </div>
      
      <!-- Messaging Call -->
      <div v-if="node.data.type === 'MESSAGING'" class="call-details">
        <div class="topic-name">
          📮 {{ node.data.endpoint || 'Topic/Queue' }}
        </div>
        <div class="client-badge">
          {{ node.data.clientType || 'Kafka' }}
        </div>
      </div>
      
      <!-- Cache Call -->
      <div v-if="node.data.type === 'CACHE'" class="call-details">
        <div class="cache-operation">
          <span class="operation-label">{{ node.data.method || 'get' }}</span>
          <span class="cache-key">{{ formatEndpoint(node.data.endpoint) }}</span>
        </div>
        <div class="client-badge">
          {{ node.data.clientType || 'Redis' }}
        </div>
      </div>
      
      <div class="node-location">
        <span class="method-name">{{ node.data.callingMethod }}</span>
        <span class="line-number">:{{ node.data.lineNumber }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ExternalCallNode',
  props: {
    node: {
      type: Object,
      required: true
    }
  },
  computed: {
    callTypeClass() {
      const type = this.node.data.type?.toLowerCase();
      return `call-type-${type}`;
    },
    methodClass() {
      const method = this.node.data.method?.toLowerCase();
      return `http-${method}`;
    }
  },
  methods: {
    getIcon() {
      switch (this.node.data.type) {
        case 'REST': return '🌐';
        case 'MESSAGING': return '📨';
        case 'CACHE': return '💾';
        default: return '🔗';
      }
    },
    formatEndpoint(endpoint) {
      if (!endpoint) return '';
      const maxLength = 50;
      if (endpoint.length <= maxLength) return endpoint;
      return endpoint.substring(0, maxLength) + '...';
    }
  }
}
</script>

<style scoped>
.external-call-node {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border: 2px solid #10b981;
  border-radius: 8px;
  padding: 12px;
  min-width: 240px;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.15);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.call-type-messaging {
  background: linear-gradient(135deg, #fefce8 0%, #fef3c7 100%);
  border-color: #f59e0b;
}

.call-type-cache {
  background: linear-gradient(135deg, #fdf4ff 0%, #fae8ff 100%);
  border-color: #d946ef;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #10b981;
}

.call-type-messaging .node-header {
  border-bottom-color: #f59e0b;
}

.call-type-cache .node-header {
  border-bottom-color: #d946ef;
}

.node-icon {
  font-size: 20px;
}

.node-type {
  font-weight: 600;
  color: #047857;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.call-type-messaging .node-type {
  color: #b45309;
}

.call-type-cache .node-type {
  color: #a21caf;
}

.async-badge {
  background: #93c5fd;
  color: #1e3a8a;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 9px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  margin-left: auto;
}

.node-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.call-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.http-method {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 700;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  width: fit-content;
}

.http-get {
  background: #bfdbfe;
  color: #1e40af;
}

.http-post {
  background: #bef264;
  color: #365314;
}

.http-put {
  background: #fde68a;
  color: #78350f;
}

.http-delete {
  background: #fecaca;
  color: #991b1b;
}

.http-patch {
  background: #e9d5ff;
  color: #6b21a8;
}

.endpoint {
  background: #fff;
  border: 1px solid #10b981;
  border-radius: 4px;
  padding: 8px;
  font-size: 11px;
  color: #047857;
  font-family: 'Courier New', monospace;
  word-break: break-word;
  line-height: 1.5;
}

.call-type-messaging .endpoint {
  border-color: #f59e0b;
  color: #b45309;
}

.call-type-cache .endpoint {
  border-color: #d946ef;
  color: #a21caf;
}

.topic-name {
  background: #fff;
  border: 1px solid #f59e0b;
  border-radius: 4px;
  padding: 8px;
  font-size: 12px;
  color: #b45309;
  font-family: 'Courier New', monospace;
  font-weight: 600;
}

.cache-operation {
  background: #fff;
  border: 1px solid #d946ef;
  border-radius: 4px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.operation-label {
  font-size: 10px;
  color: #a21caf;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.cache-key {
  font-size: 11px;
  color: #c026d3;
  font-family: 'Courier New', monospace;
}

.client-badge {
  background: #d1fae5;
  color: #065f46;
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  width: fit-content;
}

.call-type-messaging .client-badge {
  background: #fef3c7;
  color: #78350f;
}

.call-type-cache .client-badge {
  background: #fae8ff;
  color: #86198f;
}

.node-location {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid #10b981;
  font-size: 11px;
}

.call-type-messaging .node-location {
  border-top-color: #f59e0b;
}

.call-type-cache .node-location {
  border-top-color: #d946ef;
}

.method-name {
  color: #047857;
  font-weight: 500;
  font-family: 'Courier New', monospace;
}

.call-type-messaging .method-name {
  color: #b45309;
}

.call-type-cache .method-name {
  color: #a21caf;
}

.line-number {
  color: #059669;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

.call-type-messaging .line-number {
  color: #d97706;
}

.call-type-cache .line-number {
  color: #c026d3;
}
</style>
