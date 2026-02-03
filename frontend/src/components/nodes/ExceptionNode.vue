<template>
  <div class="exception-node">
    <div class="node-header">
      <span class="node-icon">⚠️</span>
      <span class="node-type">{{ node.data.blockType || 'Exception' }}</span>
    </div>
    
    <div class="node-content">
      <!-- Try-Catch Block -->
      <div v-if="node.data.caughtExceptions" class="exception-details">
        <div class="caught-exceptions">
          <strong>Catches:</strong>
          <div v-for="(ex, idx) in node.data.caughtExceptions" :key="idx" class="exception-type">
            {{ formatExceptionType(ex) }}
          </div>
        </div>
        
        <div class="exception-flags">
          <span v-if="node.data.hasFinally" class="flag finally-flag">finally</span>
          <span v-if="node.data.rethrows" class="flag rethrow-flag">rethrows</span>
        </div>
      </div>
      
      <!-- Exception Handler -->
      <div v-if="node.data.handledExceptions" class="handler-details">
        <div class="handler-method">
          <strong>Handler:</strong> {{ node.data.methodName }}
        </div>
        <div class="handled-exceptions">
          <strong>Handles:</strong>
          <div v-for="(ex, idx) in node.data.handledExceptions" :key="idx" class="exception-type">
            {{ formatExceptionType(ex) }}
          </div>
        </div>
        <div v-if="node.data.isGlobal" class="global-badge">
          Global Handler
        </div>
      </div>
      
      <div class="node-location">
        <span class="method-name">{{ node.data.parentMethodName || node.data.methodName }}</span>
        <span class="line-number">:{{ node.data.lineNumber }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ExceptionNode',
  props: {
    node: {
      type: Object,
      required: true
    }
  },
  methods: {
    formatExceptionType(exceptionType) {
      // Extract simple name from fully qualified name
      if (!exceptionType) return '';
      const parts = exceptionType.split('.');
      return parts[parts.length - 1];
    }
  }
}
</script>

<style scoped>
.exception-node {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%);
  border: 2px solid #fc8181;
  border-radius: 8px;
  padding: 12px;
  min-width: 220px;
  box-shadow: 0 2px 8px rgba(252, 129, 129, 0.15);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #fc8181;
}

.node-icon {
  font-size: 20px;
}

.node-type {
  font-weight: 600;
  color: #c53030;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.node-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.exception-details,
.handler-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.caught-exceptions,
.handled-exceptions {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.caught-exceptions strong,
.handled-exceptions strong,
.handler-method strong {
  font-size: 11px;
  color: #742a2a;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.exception-type {
  background: #fff;
  border: 1px solid #fc8181;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  color: #c53030;
  font-family: 'Courier New', monospace;
}

.exception-flags {
  display: flex;
  gap: 6px;
  margin-top: 4px;
}

.flag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.finally-flag {
  background: #bee3f8;
  color: #2c5282;
}

.rethrow-flag {
  background: #feebc8;
  color: #c05621;
}

.handler-method {
  font-size: 12px;
  color: #742a2a;
}

.global-badge {
  background: #9ae6b4;
  color: #22543d;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  text-align: center;
  letter-spacing: 0.3px;
}

.node-location {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid #fc8181;
  font-size: 11px;
}

.method-name {
  color: #742a2a;
  font-weight: 500;
  font-family: 'Courier New', monospace;
}

.line-number {
  color: #e53e3e;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}
</style>
