<template>
  <div class="code-block" :class="blockClass" :style="{ marginLeft: `${depth * 1}rem` }">
    <div class="block-header">
      <span class="block-type-badge" :class="`type-${block.type.toLowerCase()}`">
        {{ formatBlockType(block.type) }}
      </span>
      <span v-if="block.lineNumber" class="line-number">Line {{ block.lineNumber }}</span>
    </div>
    
    <div class="block-code">
      <code>{{ block.code }}</code>
    </div>
    
    <div v-if="block.description" class="block-description">
      {{ block.description }}
    </div>

    <!-- Conditional blocks -->
    <div v-if="block.thenBlocks && block.thenBlocks.length > 0" class="nested-blocks">
      <div class="nested-label">Then:</div>
      <CodeBlock
        v-for="(thenBlock, index) in block.thenBlocks"
        :key="`then-${index}`"
        :block="thenBlock"
        :depth="depth + 1"
      />
    </div>

    <div v-if="block.elseBlocks && block.elseBlocks.length > 0" class="nested-blocks">
      <div class="nested-label">Else:</div>
      <CodeBlock
        v-for="(elseBlock, index) in block.elseBlocks"
        :key="`else-${index}`"
        :block="elseBlock"
        :depth="depth + 1"
      />
    </div>

    <!-- Loop body -->
    <div v-if="block.loopBody && block.loopBody.length > 0" class="nested-blocks">
      <div class="nested-label">Loop Body:</div>
      <CodeBlock
        v-for="(bodyBlock, index) in block.loopBody"
        :key="`body-${index}`"
        :block="bodyBlock"
        :depth="depth + 1"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  block: {
    type: Object,
    required: true
  },
  depth: {
    type: Number,
    default: 0
  }
})

const blockClass = computed(() => {
  return `block-depth-${Math.min(props.depth, 3)}`
})

function formatBlockType(type) {
  const typeMap = {
    'CONDITIONAL': '🔀 If',
    'LOOP': '🔄 Loop',
    'METHOD_CALL': '📞 Call',
    'RETURN': '↩️ Return',
    'VARIABLE_DECLARATION': '📦 Var',
    'ASSIGNMENT': '➡️ Assign',
    'TRY_CATCH': '⚠️ Try-Catch',
    'LAMBDA': 'λ Lambda',
    'STREAM': '🌊 Stream'
  }
  return typeMap[type] || type
}
</script>

<style scoped>
.code-block {
  background: white;
  border-radius: 4px;
  padding: 0.5rem;
  border-left: 3px solid #e0e0e0;
  font-size: 0.85rem;
}

.block-depth-0 {
  border-left-color: #3498db;
}

.block-depth-1 {
  border-left-color: #9b59b6;
}

.block-depth-2 {
  border-left-color: #e74c3c;
}

.block-depth-3 {
  border-left-color: #f39c12;
}

.block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.block-type-badge {
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: 600;
}

.type-conditional {
  background: #e3f2fd;
  color: #1976d2;
}

.type-loop {
  background: #f3e5f5;
  color: #7b1fa2;
}

.type-method_call {
  background: #e8f5e9;
  color: #388e3c;
}

.type-return {
  background: #fff3e0;
  color: #f57c00;
}

.type-variable_declaration,
.type-assignment {
  background: #fce4ec;
  color: #c2185b;
}

.type-try_catch {
  background: #ffebee;
  color: #d32f2f;
}

.type-lambda,
.type-stream {
  background: #e0f2f1;
  color: #00796b;
}

.line-number {
  font-size: 0.7rem;
  color: #999;
  font-family: 'Courier New', monospace;
}

.block-code {
  background: #f5f5f5;
  padding: 0.5rem;
  border-radius: 3px;
  overflow-x: auto;
}

.block-code code {
  font-family: 'Courier New', monospace;
  font-size: 0.8rem;
  color: #2c3e50;
  white-space: pre-wrap;
  word-break: break-word;
}

.block-description {
  margin-top: 0.5rem;
  color: #666;
  font-size: 0.8rem;
  font-style: italic;
}

.nested-blocks {
  margin-top: 0.5rem;
  padding-left: 0.5rem;
  border-left: 2px dashed #ddd;
}

.nested-label {
  font-size: 0.75rem;
  color: #666;
  font-weight: 600;
  margin-bottom: 0.25rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
</style>
