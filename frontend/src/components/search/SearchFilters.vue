<template>
  <div v-if="showAdvanced" class="search-filters">
    <div class="filters-header">
      <h4>🎛️ Advanced Filters</h4>
    </div>

    <div class="filter-section">
      <label class="filter-label">Component Types:</label>
      <div class="filter-chips">
        <label 
          v-for="type in componentTypes" 
          :key="type.value"
          class="filter-chip"
          :class="{ active: isTypeSelected(type.value) }"
        >
          <input 
            type="checkbox" 
            :value="type.value"
            v-model="localFilters.componentTypes"
            @change="emitUpdate"
          />
          <span class="chip-icon">{{ type.icon }}</span>
          <span class="chip-label">{{ type.label }}</span>
        </label>
      </div>
    </div>

    <div class="filter-section">
      <label class="filter-label">
        Min Centrality Score: 
        <span class="filter-value">{{ localFilters.minCentrality }}</span>
        <span class="info-tooltip" title="Filters components by their importance score (0-100). Higher scores mean more connected/important components.">ℹ️</span>
      </label>
      <input 
        type="range" 
        v-model.number="localFilters.minCentrality"
        min="0" 
        max="100" 
        step="5"
        class="filter-slider"
        @input="emitUpdate"
        title="Drag to adjust minimum centrality score"
      />
      <div class="slider-labels">
        <span>0 (All)</span>
        <span>50</span>
        <span>100 (Best)</span>
      </div>
    </div>

    <div class="filter-section">
      <label class="filter-label">
        Max Results: 
        <span class="filter-value">{{ localFilters.maxResults }}</span>
      </label>
      <input 
        type="range" 
        v-model.number="localFilters.maxResults"
        min="5" 
        max="100" 
        step="5"
        class="filter-slider"
        @input="emitUpdate"
      />
      <div class="slider-labels">
        <span>5</span>
        <span>50</span>
        <span>100</span>
      </div>
    </div>

    <div class="filter-section">
      <label class="filter-label">
        Min Confidence: 
        <span class="filter-value">{{ localFilters.minConfidence }}%</span>
        <span class="info-tooltip" title="Filters matches by confidence level (0-100%). Higher values show only strong matches.">ℹ️</span>
      </label>
      <input 
        type="range" 
        v-model.number="localFilters.minConfidence"
        min="0" 
        max="100" 
        step="10"
        title="Drag to adjust minimum confidence percentage"
        class="filter-slider"
        @input="emitUpdate"
      />
      <div class="slider-labels">
        <span>Low</span>
        <span>Medium</span>
        <span>High</span>
      </div>
    </div>

    <div class="filter-actions">
      <button @click="resetFilters" class="reset-btn">
        Reset Filters
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  filters: {
    type: Object,
    required: true
  },
  showAdvanced: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:filters'])

const localFilters = ref({ ...props.filters })

const componentTypes = [
  { value: 'endpoint', label: 'Endpoints', icon: '🌐' },
  { value: 'service', label: 'Services', icon: '⚙️' },
  { value: 'repository', label: 'Repositories', icon: '💾' },
  { value: 'log', label: 'Logs', icon: '📝' },
  { value: 'dataflow', label: 'DataFlows', icon: '📊' },
  { value: 'database', label: 'Database', icon: '🗄️' },
  { value: 'external', label: 'External', icon: '🌍' },
  { value: 'exception', label: 'Exceptions', icon: '⚠️' }
]

function isTypeSelected(type) {
  return localFilters.value.componentTypes.includes(type)
}

function emitUpdate() {
  emit('update:filters', { ...localFilters.value })
}

function resetFilters() {
  localFilters.value = {
    componentTypes: ['endpoint', 'service', 'repository'],
    minCentrality: 50,
    maxResults: 20,
    minConfidence: 30
  }
  emitUpdate()
}

watch(() => props.filters, (newFilters) => {
  localFilters.value = { ...newFilters }
}, { deep: true })
</script>

<style scoped>
.search-filters {
  padding: 1rem;
  background-color: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.filters-header h4 {
  margin: 0 0 1rem 0;
  font-size: 1rem;
  color: #374151;
}

.filter-section {
  margin-bottom: 1.5rem;
}

.filter-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 0.5rem;
}

.filter-value {
  color: #3498db;
  font-weight: 700;
}

.filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.filter-chip {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.5rem 0.75rem;
  background-color: white;
  border: 2px solid #e5e7eb;
  border-radius: 20px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.filter-chip input[type="checkbox"] {
  display: none;
}

.filter-chip:hover {
  border-color: #3498db;
}

.filter-chip.active {
  background-color: #3498db;
  border-color: #3498db;
  color: white;
}

.chip-icon {
  font-size: 1rem;
}

.chip-label {
  font-weight: 500;
}

.filter-slider {
  width: 100%;
  height: 6px;
  background: linear-gradient(to right, #e5e7eb 0%, #3498db 100%);
  border-radius: 3px;
  outline: none;
  -webkit-appearance: none;
}

.filter-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  background: #3498db;
  border: 2px solid white;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.filter-slider::-moz-range-thumb {
  width: 18px;
  height: 18px;
  background: #3498db;
  border: 2px solid white;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: #9ca3af;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

.reset-btn {
  padding: 0.5rem 1rem;
  background-color: #6b7280;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.reset-btn:hover {
  background-color: #4b5563;
}

.info-tooltip {
  font-size: 0.875rem;
  color: #3498db;
  cursor: help;
  opacity: 0.7;
  transition: opacity 0.2s;
  margin-left: 0.375rem;
}

.info-tooltip:hover {
  opacity: 1;
}
</style>
