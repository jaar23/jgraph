<template>
  <div class="service-node" :class="{ 'has-annotation': data.hasAnnotation }">
    <div class="node-header">
      <span class="node-type">SERVICE</span>
      <span class="annotation-indicator" v-if="data.hasAnnotation" title="Has annotations">📝</span>
    </div>
    <div class="node-class">{{ getShortName(data.className) }}</div>
    <div class="node-method">{{ data.methodName }}()</div>
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
.service-node {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  color: white;
  padding: 0.75rem;
  border-radius: 8px;
  min-width: 240px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 2px solid transparent;
  transition: all 0.2s;
}

.service-node:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

.service-node.has-annotation {
  border-color: #f39c12;
}

.node-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.node-type {
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.5px;
  opacity: 0.8;
}

.annotation-indicator {
  font-size: 1rem;
  cursor: help;
}

.node-class {
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.node-method {
  font-size: 0.85rem;
  opacity: 0.9;
  font-family: monospace;
}
</style>
