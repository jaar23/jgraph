<template>
  <div v-if="visible" class="timeline-modal-overlay" @click.self="close">
    <div class="timeline-modal">
      <div class="modal-header">
        <h3>🔄 Data Transformation Timeline</h3>
        <button @click="close" class="close-btn">×</button>
      </div>

      <div class="modal-subheader">
        <div class="method-info">
          <span class="code">{{ dataflow.className }}.{{ dataflow.methodName }}()</span>
        </div>
        <div class="stage-indicator">
          Stage {{ currentStageIndex + 1 }} of {{ stages.length }}
        </div>
      </div>

      <!-- Timeline Navigation -->
      <div class="timeline-nav">
        <button 
          v-for="(stage, index) in stages" 
          :key="index"
          @click="currentStageIndex = index"
          :class="['stage-tab', { 
            'active': currentStageIndex === index,
            'completed': index < currentStageIndex 
          }]"
        >
          <span class="stage-icon">{{ stage.icon }}</span>
          <span class="stage-name">{{ stage.name }}</span>
          <span class="stage-count">{{ stage.count }}</span>
        </button>
      </div>

      <!-- Stage Content -->
      <div class="stage-content">
        <TransformationStage 
          :stage="stages[currentStageIndex]"
          :dataflow="dataflow"
        />
      </div>

      <!-- Navigation Controls -->
      <div class="modal-footer">
        <button 
          @click="previousStage" 
          :disabled="currentStageIndex === 0"
          class="nav-btn"
        >
          ← Previous
        </button>
        
        <div class="stage-dots">
          <span 
            v-for="(stage, index) in stages" 
            :key="index"
            :class="['dot', { 'active': currentStageIndex === index }]"
          />
        </div>
        
        <button 
          @click="nextStage" 
          :disabled="currentStageIndex === stages.length - 1"
          class="nav-btn"
        >
          Next →
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import TransformationStage from './TransformationStage.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  },
  dataflow: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close'])

const currentStageIndex = ref(0)

// Build stages from dataflow data
const stages = computed(() => {
  const result = []

  // Stage 1: Input Parameters
  if (props.dataflow.parameters?.length > 0) {
    result.push({
      icon: '📥',
      name: 'Input',
      count: props.dataflow.parameters.length,
      type: 'parameters',
      data: props.dataflow.parameters,
      description: 'Method input parameters and their types'
    })
  }

  // Stage 2: Variable Transformations
  if (props.dataflow.variables?.length > 0) {
    result.push({
      icon: '🔄',
      name: 'Transform',
      count: props.dataflow.variables.length,
      type: 'variables',
      data: props.dataflow.variables,
      description: 'Variable assignments and transformations within the method'
    })
  }

  // Stage 3: Data Transfers (Method Calls)
  if (props.dataflow.dataTransfers?.length > 0) {
    result.push({
      icon: '📤',
      name: 'Transfer',
      count: props.dataflow.dataTransfers.length,
      type: 'transfers',
      data: props.dataflow.dataTransfers,
      description: 'Data passed to other methods via method calls'
    })
  }

  // Stage 4: Return Values
  if (props.dataflow.returns?.length > 0) {
    result.push({
      icon: '↩️',
      name: 'Return',
      count: props.dataflow.returns.length,
      type: 'returns',
      data: props.dataflow.returns,
      description: 'Values returned from the method'
    })
  }

  return result
})

// Reset stage when modal opens
watch(() => props.visible, (newVal) => {
  if (newVal) {
    currentStageIndex.value = 0
  }
})

// Keyboard navigation
watch(() => props.visible, (newVal) => {
  if (newVal) {
    document.addEventListener('keydown', handleKeydown)
  } else {
    document.removeEventListener('keydown', handleKeydown)
  }
})

function handleKeydown(e) {
  if (e.key === 'ArrowLeft') {
    previousStage()
  } else if (e.key === 'ArrowRight') {
    nextStage()
  } else if (e.key === 'Escape') {
    close()
  }
}

function previousStage() {
  if (currentStageIndex.value > 0) {
    currentStageIndex.value--
  }
}

function nextStage() {
  if (currentStageIndex.value < stages.value.length - 1) {
    currentStageIndex.value++
  }
}

function close() {
  emit('close')
}
</script>

<style scoped>
.timeline-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 2rem;
}

.timeline-modal {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 1000px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  background: linear-gradient(135deg, #fef3c7 0%, #fed7aa 100%);
  border-bottom: 2px solid #f59e0b;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  color: #92400e;
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #92400e;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #78350f;
}

.modal-subheader {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: #fffbeb;
  border-bottom: 1px solid #fde68a;
}

.method-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.code {
  font-family: 'Courier New', monospace;
  background: #fef3c7;
  padding: 0.375rem 0.75rem;
  border-radius: 4px;
  font-size: 0.9rem;
  color: #78350f;
  font-weight: 500;
}

.stage-indicator {
  font-size: 0.85rem;
  color: #92400e;
  font-weight: 600;
}

/* Timeline Navigation */
.timeline-nav {
  display: flex;
  background: #f9fafb;
  border-bottom: 2px solid #e5e7eb;
  overflow-x: auto;
}

.stage-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.375rem;
  padding: 1rem;
  background: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.stage-tab:hover {
  background: #f3f4f6;
}

.stage-tab.active {
  background: white;
  border-bottom-color: #f59e0b;
}

.stage-tab.completed::after {
  content: '✓';
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  color: #22c55e;
  font-weight: bold;
  font-size: 0.9rem;
}

.stage-icon {
  font-size: 1.75rem;
}

.stage-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: #374151;
}

.stage-tab.active .stage-name {
  color: #f59e0b;
}

.stage-count {
  font-size: 0.75rem;
  color: #9ca3af;
  background: #e5e7eb;
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-weight: 600;
}

.stage-tab.active .stage-count {
  background: #fef3c7;
  color: #92400e;
}

/* Stage Content */
.stage-content {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
  background: #fafafa;
}

/* Modal Footer */
.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: white;
  border-top: 1px solid #e5e7eb;
}

.nav-btn {
  padding: 0.625rem 1.25rem;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  color: #374151;
}

.nav-btn:hover:not(:disabled) {
  background: #f59e0b;
  border-color: #f59e0b;
  color: white;
}

.nav-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.stage-dots {
  display: flex;
  gap: 0.5rem;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d1d5db;
  transition: all 0.2s;
}

.dot.active {
  background: #f59e0b;
  transform: scale(1.5);
}
</style>
