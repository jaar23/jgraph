<template>
  <div class="service-node" :class="{ 'has-annotation': data.hasAnnotation, 'has-source': data.hasSource }">
    <div class="node-header">
      <span class="node-type">SERVICE</span>
      <div class="node-indicators">
        <span class="source-indicator" v-if="data.hasSource" title="Source code details available">📄</span>
        <span class="annotation-indicator" v-if="data.hasAnnotation" title="Has annotations">📝</span>
      </div>
    </div>
    <div class="node-class">{{ getShortName(data.className) }}</div>
    <div class="node-method">{{ data.methodName }}()</div>
    <div v-if="data.summary" class="node-summary" :title="data.summary">
      {{ truncateSummary(data.summary) }}
    </div>
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

function truncateSummary(summary) {
  if (!summary) return ''
  const maxLength = 50
  if (summary.length <= maxLength) return summary
  return summary.substring(0, maxLength) + '...'
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

.service-node.has-source {
  border-left: 4px solid #3498db;
}

.node-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.node-indicators {
  display: flex;
  gap: 0.25rem;
  align-items: center;
}

.node-type {
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.5px;
  opacity: 0.8;
}

.source-indicator {
  font-size: 0.9rem;
  cursor: help;
  opacity: 0.9;
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

.node-summary {
  margin-top: 0.5rem;
  padding-top: 0.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 0.75rem;
  opacity: 0.95;
  font-style: italic;
  line-height: 1.3;
}
</style>
