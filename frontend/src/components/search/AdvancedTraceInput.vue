<template>
  <div class="advanced-trace-input">
    <div class="input-header">
      <h3>
        🎯 Smart Component Trace
        <span class="help-tooltip" title="Intelligently discovers all components related to your input using fuzzy matching and centrality ranking. Supports logs, JSON, variables, and free text.">ℹ️</span>
      </h3>
      <p class="hint">Paste a log message, JSON payload, or variable name to trace components</p>
    </div>

    <textarea
      v-model="inputText"
      class="trace-textarea"
      placeholder="Examples:&#10;• Log: User not found with id: {}&#10;• JSON: {&quot;username&quot;: &quot;john&quot;, &quot;email&quot;: &quot;john@example.com&quot;}&#10;• Variable: userId"
      rows="5"
      @input="onInputChange"
    ></textarea>

    <div v-if="detectedType" class="detected-info">
      <span class="detected-label">Detected:</span>
      <span class="detected-badge" :class="detectedType">
        {{ getTypeLabel(detectedType) }}
      </span>
      <span v-if="confidence" class="confidence">
        Confidence: {{ confidence }}%
      </span>
    </div>

    <SearchFilters 
      v-model:filters="filters"
      :show-advanced="showAdvancedFilters"
    />

    <div class="action-buttons">
      <button 
        @click="toggleAdvancedFilters" 
        class="filter-toggle-btn"
        title="Show/hide advanced filters to refine search results"
      >
        <span class="icon">🎛️</span>
        {{ showAdvancedFilters ? 'Hide' : 'Show' }} Filters
      </button>
      
      <button 
        @click="performTrace" 
        class="trace-btn"
        :disabled="!inputText.trim() || isSearching"
        :title="inputText.trim() ? 'Trace all components related to this input' : 'Enter text to trace'"
      >
        <span v-if="isSearching" class="spinner">⏳</span>
        <span v-else class="icon">🔍</span>
        {{ isSearching ? 'Tracing...' : 'Trace Components' }}
      </button>
    </div>

    <div v-if="examples && !inputText" class="examples">
      <div class="examples-header">Quick Examples:</div>
      <div class="example-chips">
        <button 
          v-for="example in examples" 
          :key="example.label"
          @click="inputText = example.text"
          class="example-chip"
        >
          {{ example.label }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { detectInputType } from '../../utils/searchAlgorithm'
import SearchFilters from './SearchFilters.vue'

const emit = defineEmits(['search'])

const inputText = ref('')
const detectedType = ref(null)
const confidence = ref(null)
const isSearching = ref(false)
const showAdvancedFilters = ref(false)
const filters = ref({
  componentTypes: ['endpoint', 'service', 'repository'],
  minCentrality: 50,
  maxResults: 20,
  minConfidence: 30
})

const examples = [
  { label: 'Log Message', text: 'User not found with id: {}' },
  { label: 'Variable', text: 'userId' },
  { label: 'JSON', text: '{"username": "john", "email": "john@example.com"}' }
]

function onInputChange() {
  if (inputText.value.trim()) {
    detectedType.value = detectInputType(inputText.value)
    // Simple confidence based on input characteristics
    confidence.value = calculateConfidence(inputText.value, detectedType.value)
  } else {
    detectedType.value = null
    confidence.value = null
  }
}

function calculateConfidence(text, type) {
  if (!text || !type) return null
  
  // Simple heuristic confidence calculation
  if (type === 'LOG_MESSAGE') {
    const hasPlaceholders = /\{\}/.test(text)
    const hasLogWords = /(error|warn|info|debug|found|failed|success)/i.test(text)
    return hasPlaceholders && hasLogWords ? 95 : hasLogWords ? 80 : 60
  }
  if (type === 'JSON_PAYLOAD') {
    try {
      JSON.parse(text)
      return 100
    } catch {
      return 50
    }
  }
  if (type === 'VARIABLE_NAME') {
    return /^[a-zA-Z_$][a-zA-Z0-9_$]*$/.test(text) ? 90 : 70
  }
  return 50
}

function getTypeLabel(type) {
  const labels = {
    'LOG_MESSAGE': '📝 Log Message',
    'JSON_PAYLOAD': '📦 JSON Payload',
    'VARIABLE_NAME': '🔤 Variable',
    'FREE_TEXT': '📄 Free Text'
  }
  return labels[type] || type
}

function toggleAdvancedFilters() {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

async function performTrace() {
  if (!inputText.value.trim()) return
  
  isSearching.value = true
  
  try {
    // Small delay to show loading state
    await new Promise(resolve => setTimeout(resolve, 300))
    
    emit('search', {
      input: inputText.value,
      filters: filters.value,
      detectedType: detectedType.value
    })
  } finally {
    isSearching.value = false
  }
}
</script>

<style scoped>
.advanced-trace-input {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.input-header h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.help-tooltip {
  font-size: 0.9rem;
  color: #3498db;
  cursor: help;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.help-tooltip:hover {
  opacity: 1;
}

.hint {
  margin: 0;
  font-size: 0.875rem;
  color: #6b7280;
}

.trace-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  resize: vertical;
  transition: border-color 0.2s;
}

.trace-textarea:focus {
  outline: none;
  border-color: #3498db;
}

.detected-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  background-color: #f0f9ff;
  border-radius: 6px;
  border-left: 4px solid #3498db;
}

.detected-label {
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.detected-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

.detected-badge.LOG_MESSAGE {
  background-color: #dbeafe;
  color: #1e40af;
}

.detected-badge.JSON_PAYLOAD {
  background-color: #d1fae5;
  color: #065f46;
}

.detected-badge.VARIABLE_NAME {
  background-color: #fef3c7;
  color: #92400e;
}

.detected-badge.FREE_TEXT {
  background-color: #e5e7eb;
  color: #374151;
}

.confidence {
  margin-left: auto;
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.filter-toggle-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background-color: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-toggle-btn:hover {
  background-color: #e5e7eb;
}

.trace-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.trace-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.trace-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.icon {
  font-size: 1.1rem;
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.examples {
  padding: 1rem;
  background-color: #f9fafb;
  border-radius: 8px;
  border: 1px dashed #d1d5db;
}

.examples-header {
  font-size: 0.875rem;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 0.75rem;
}

.example-chips {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.example-chip {
  padding: 0.5rem 1rem;
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  font-size: 0.875rem;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.example-chip:hover {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
  transform: translateY(-1px);
}
</style>
