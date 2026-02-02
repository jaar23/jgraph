<template>
  <div class="endpoint-node" :class="{ 'has-annotation': data.hasAnnotation }">
    <div class="node-header">
      <span class="http-method" :class="data.httpMethod">{{ data.httpMethod }}</span>
      <span class="annotation-indicator" v-if="data.hasAnnotation" title="Has annotations">📝</span>
    </div>
    <div class="node-path">{{ data.path || data.label }}</div>
    <div class="node-meta">{{ getShortName(data.className || data.controllerClass) }}</div>
  </div>
</template>

<script setup>
const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

function getShortName(fullName) {
  if (!fullName) return ''
  const parts = fullName.split('.')
  return parts[parts.length - 1]
}
</script>

<style scoped>
.endpoint-node {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  color: white;
  padding: 0.75rem;
  border-radius: 8px;
  min-width: 280px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid transparent;
  transition: all 0.2s;
}

.endpoint-node:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

.endpoint-node.has-annotation {
  border-color: #f39c12;
}

.node-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.http-method {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 700;
  background-color: rgba(255, 255, 255, 0.3);
}

.annotation-indicator {
  font-size: 1rem;
  cursor: help;
}

.node-path {
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
  word-break: break-all;
}

.node-meta {
  font-size: 0.75rem;
  opacity: 0.9;
  font-family: monospace;
}
</style>
