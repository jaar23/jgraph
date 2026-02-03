<template>
  <div class="log-node" :class="[`log-${logLevel.toLowerCase()}`, { 'has-exception': data.hasException }]">
    <div class="node-header">
      <span class="log-icon">📋</span>
      <span class="log-level">{{ logLevel }}</span>
      <span class="exception-indicator" v-if="data.hasException" title="Logs exception">⚠️</span>
    </div>
    <div class="log-message">{{ truncateMessage(data.logMessage || data.message) }}</div>
    <div class="log-meta" v-if="data.variables && data.variables.length > 0">
      <span class="log-vars">{{ formatVariables(data.variables) }}</span>
    </div>
    <div class="log-framework" v-if="data.loggerFramework">
      {{ data.loggerFramework }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const logLevel = computed(() => {
  return props.data.logLevel || props.data.level || 'INFO'
})

function truncateMessage(message) {
  if (!message) return 'Log statement'
  if (message.length <= 50) return message
  return message.substring(0, 50) + '...'
}

function formatVariables(variables) {
  if (!variables || variables.length === 0) return ''
  if (variables.length <= 2) return variables.join(', ')
  return `${variables.slice(0, 2).join(', ')} +${variables.length - 2}`
}
</script>

<style scoped>
.log-node {
  color: white;
  padding: 0.6rem;
  border-radius: 6px;
  min-width: 220px;
  max-width: 300px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  border: 2px solid transparent;
  transition: all 0.2s;
  font-size: 0.85rem;
}

.log-node:hover {
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.25);
  transform: translateY(-1px);
}

/* Log level colors */
.log-node.log-trace {
  background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%);
}

.log-node.log-debug {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
}

.log-node.log-info {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
}

.log-node.log-warn {
  background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
}

.log-node.log-error {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
}

.log-node.log-fatal {
  background: linear-gradient(135deg, #8e44ad 0%, #71368a 100%);
}

.log-node.has-exception {
  border-color: #e74c3c;
  box-shadow: 0 0 0 2px rgba(231, 76, 60, 0.3);
}

.node-header {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.4rem;
}

.log-icon {
  font-size: 1rem;
}

.log-level {
  display: inline-block;
  padding: 0.15rem 0.4rem;
  border-radius: 3px;
  font-size: 0.7rem;
  font-weight: 700;
  background-color: rgba(255, 255, 255, 0.3);
  letter-spacing: 0.5px;
}

.exception-indicator {
  font-size: 0.9rem;
  cursor: help;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.log-message {
  font-weight: 500;
  font-size: 0.85rem;
  margin-bottom: 0.3rem;
  line-height: 1.3;
  word-break: break-word;
}

.log-meta {
  font-size: 0.7rem;
  opacity: 0.9;
  margin-bottom: 0.2rem;
}

.log-vars {
  font-family: monospace;
  background-color: rgba(0, 0, 0, 0.2);
  padding: 0.1rem 0.3rem;
  border-radius: 3px;
}

.log-framework {
  font-size: 0.65rem;
  opacity: 0.7;
  text-align: right;
  font-style: italic;
}
</style>
