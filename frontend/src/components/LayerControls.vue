<template>
  <div class="layer-controls">
    <div class="layer-header">
      <h3>Graph Layers</h3>
      <button @click="toggleAll" class="toggle-all-btn">
        {{ allEnabled ? 'Hide All' : 'Show All' }}
      </button>
    </div>
    
    <div class="layer-options">
      <label class="layer-option">
        <input 
          type="checkbox" 
          v-model="layers.methods" 
          @change="emitChange"
        />
        <span class="layer-icon">🔗</span>
        <span class="layer-name">Method Calls</span>
        <span class="layer-count">{{ counts.methods }}</span>
      </label>
      
      <label class="layer-option">
        <input 
          type="checkbox" 
          v-model="layers.logs" 
          @change="emitChange"
        />
        <span class="layer-icon">📋</span>
        <span class="layer-name">Logging</span>
        <span class="layer-count">{{ counts.logs }}</span>
      </label>
      
      <label class="layer-option">
        <input 
          type="checkbox" 
          v-model="layers.exceptions" 
          @change="emitChange"
        />
        <span class="layer-icon">⚠️</span>
        <span class="layer-name">Exceptions</span>
        <span class="layer-count">{{ counts.exceptions }}</span>
      </label>
      
      <label class="layer-option">
        <input 
          type="checkbox" 
          v-model="layers.database" 
          @change="emitChange"
        />
        <span class="layer-icon">🗄️</span>
        <span class="layer-name">Database</span>
        <span class="layer-count">{{ counts.database }}</span>
      </label>
      
      <label class="layer-option">
        <input 
          type="checkbox" 
          v-model="layers.external" 
          @change="emitChange"
        />
        <span class="layer-icon">🌐</span>
        <span class="layer-name">External APIs</span>
        <span class="layer-count">{{ counts.external }}</span>
      </label>
      
      <label class="layer-option">
        <input 
          type="checkbox" 
          v-model="layers.dataflow" 
          @change="emitChange"
        />
        <span class="layer-icon">📊</span>
        <span class="layer-name">Data Flow</span>
        <span class="layer-count">{{ counts.dataflow }}</span>
      </label>
    </div>
    
    <!-- Log Level Filter (shown when logs are enabled) -->
    <div v-if="layers.logs" class="log-level-filter">
      <h4>Log Levels</h4>
      <div class="log-level-options">
        <label v-for="level in logLevels" :key="level" class="log-level-option">
          <input 
            type="checkbox" 
            :value="level" 
            v-model="selectedLogLevels"
            @change="emitChange"
          />
          <span class="log-level-badge" :class="`log-${level.toLowerCase()}`">
            {{ level }}
          </span>
          <span class="log-level-count">{{ logLevelCounts[level] || 0 }}</span>
        </label>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useAnalysisStore } from '../stores/analysisStore'

const store = useAnalysisStore()

const layers = ref({
  methods: true,
  logs: true,
  exceptions: true,
  database: true,
  external: true,
  dataflow: false
})

const logLevels = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL']
const selectedLogLevels = ref(['INFO', 'WARN', 'ERROR'])

const emit = defineEmits(['change'])

const counts = computed(() => {
  const data = store.analysisData
  if (!data) return { methods: 0, logs: 0, exceptions: 0, database: 0, external: 0, dataflow: 0 }
  
  return {
    methods: (data.endpoints?.length || 0) + (data.services?.length || 0) + (data.repositories?.length || 0),
    logs: data.logStatements?.length || 0,
    exceptions: (data.tryCatchBlocks?.length || 0) + (data.exceptionHandlers?.length || 0),
    database: (data.databaseOperations?.length || 0) + (data.transactions?.length || 0),
    external: data.externalCalls?.length || 0,
    dataflow: data.dataFlows?.length || 0
  }
})

const logLevelCounts = computed(() => {
  const data = store.analysisData
  if (!data || !data.statistics?.logsByLevel) return {}
  return data.statistics.logsByLevel
})

const allEnabled = computed(() => {
  return layers.value.methods && layers.value.logs && layers.value.exceptions && 
         layers.value.database && layers.value.external && layers.value.dataflow
})

function toggleAll() {
  const newState = !allEnabled.value
  layers.value.methods = newState
  layers.value.logs = newState
  layers.value.exceptions = newState
  layers.value.database = newState
  layers.value.external = newState
  layers.value.dataflow = newState
  emitChange()
}

function emitChange() {
  emit('change', {
    layers: { ...layers.value },
    selectedLogLevels: [...selectedLogLevels.value]
  })
}

// Watch for data changes to update initial state
watch(() => store.analysisData, (data) => {
  if (data) {
    // Auto-enable logs if log statements exist
    if (data.logStatements && data.logStatements.length > 0) {
      layers.value.logs = true
      emitChange()
    }
  }
}, { immediate: true })
</script>

<style scoped>
.layer-controls {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-width: 280px;
}

.layer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #e0e0e0;
}

.layer-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #2c3e50;
}

.toggle-all-btn {
  padding: 0.4rem 0.8rem;
  border: none;
  background: #3498db;
  color: white;
  border-radius: 4px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.2s;
}

.toggle-all-btn:hover {
  background: #2980b9;
}

.layer-options {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.layer-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.layer-option:hover:not(.disabled) {
  background: #f5f5f5;
}

.layer-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.layer-option input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.layer-option.disabled input[type="checkbox"] {
  cursor: not-allowed;
}

.layer-icon {
  font-size: 1.2rem;
}

.layer-name {
  flex: 1;
  font-weight: 500;
  color: #2c3e50;
}

.layer-count {
  font-size: 0.85rem;
  color: #7f8c8d;
  background: #ecf0f1;
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-weight: 600;
}

.coming-soon {
  font-size: 0.7rem;
  font-style: italic;
  background: #fff3cd;
  color: #856404;
}

.log-level-filter {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e0e0e0;
}

.log-level-filter h4 {
  margin: 0 0 0.75rem 0;
  font-size: 0.95rem;
  color: #2c3e50;
}

.log-level-options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.log-level-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.log-level-option:hover {
  background: #f5f5f5;
}

.log-level-option input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.log-level-badge {
  padding: 0.2rem 0.5rem;
  border-radius: 3px;
  font-size: 0.75rem;
  font-weight: 700;
  color: white;
  letter-spacing: 0.5px;
  min-width: 50px;
  text-align: center;
}

.log-level-badge.log-trace {
  background: #95a5a6;
}

.log-level-badge.log-debug {
  background: #3498db;
}

.log-level-badge.log-info {
  background: #1abc9c;
}

.log-level-badge.log-warn {
  background: #f39c12;
}

.log-level-badge.log-error {
  background: #e74c3c;
}

.log-level-badge.log-fatal {
  background: #8e44ad;
}

.log-level-count {
  font-size: 0.8rem;
  color: #7f8c8d;
  margin-left: auto;
}
</style>
