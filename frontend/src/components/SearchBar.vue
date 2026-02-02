<template>
  <div class="search-bar">
    <div class="search-container">
      <div class="search-input-wrapper" ref="searchWrapper">
        <div class="selected-items">
          <span 
            v-for="item in selectedItems" 
            :key="item.id" 
            class="selected-tag"
            :class="item.type"
          >
            {{ item.label }}
            <button @click="removeItem(item)" class="remove-btn">×</button>
          </span>
        </div>
        
        <input
          v-model="searchInput"
          type="text"
          placeholder="Search endpoints, services, methods, or select from dropdown..."
          class="search-input"
          @input="onSearch"
          @focus="showDropdown = true"
          @keydown.escape="showDropdown = false"
          @keydown.down.prevent="navigateDown"
          @keydown.up.prevent="navigateUp"
          @keydown.enter.prevent="selectHighlighted"
        />
        
        <button v-if="selectedItems.length > 0" @click="clearAll" class="clear-all-btn">
          Clear All
        </button>
      </div>

      <div v-if="showDropdown && filteredOptions.length > 0" class="dropdown">
        <div class="dropdown-section" v-if="filteredEndpoints.length > 0">
          <div class="dropdown-header">Endpoints ({{ filteredEndpoints.length }})</div>
          <div
            v-for="(endpoint, index) in filteredEndpoints.slice(0, 10)"
            :key="endpoint.id"
            class="dropdown-item endpoint-item"
            :class="{ 
              selected: isSelected(endpoint.id),
              highlighted: highlightedIndex === getItemIndex('endpoint', index)
            }"
            @click="toggleItem(endpoint, 'endpoint')"
            @mouseenter="highlightedIndex = getItemIndex('endpoint', index)"
          >
            <div class="item-main">
              <span class="http-method" :class="endpoint.httpMethod">{{ endpoint.httpMethod }}</span>
              <span class="item-path">{{ endpoint.path }}</span>
            </div>
            <div class="item-meta">{{ endpoint.controllerClass }}.{{ endpoint.methodName }}</div>
          </div>
          <div v-if="filteredEndpoints.length > 10" class="dropdown-more">
            +{{ filteredEndpoints.length - 10 }} more endpoints...
          </div>
        </div>

        <div class="dropdown-section" v-if="filteredServices.length > 0">
          <div class="dropdown-header">Services ({{ filteredServices.length }})</div>
          <div
            v-for="(service, index) in filteredServices.slice(0, 10)"
            :key="service.id"
            class="dropdown-item service-item"
            :class="{ 
              selected: isSelected(service.id),
              highlighted: highlightedIndex === getItemIndex('service', index)
            }"
            @click="toggleItem(service, 'service')"
            @mouseenter="highlightedIndex = getItemIndex('service', index)"
          >
            <div class="item-main">{{ service.className }}</div>
            <div class="item-meta">{{ service.methodName }}()</div>
          </div>
          <div v-if="filteredServices.length > 10" class="dropdown-more">
            +{{ filteredServices.length - 10 }} more services...
          </div>
        </div>

        <div class="dropdown-section" v-if="filteredRepositories.length > 0">
          <div class="dropdown-header">Repositories ({{ filteredRepositories.length }})</div>
          <div
            v-for="(repo, index) in filteredRepositories.slice(0, 10)"
            :key="repo.id"
            class="dropdown-item repo-item"
            :class="{ 
              selected: isSelected(repo.id),
              highlighted: highlightedIndex === getItemIndex('repository', index)
            }"
            @click="toggleItem(repo, 'repository')"
            @mouseenter="highlightedIndex = getItemIndex('repository', index)"
          >
            <div class="item-main">{{ repo.className }}</div>
            <div class="item-meta">{{ repo.methodName }}()</div>
          </div>
          <div v-if="filteredRepositories.length > 10" class="dropdown-more">
            +{{ filteredRepositories.length - 10 }} more repositories...
          </div>
        </div>

        <div v-if="filteredOptions.length === 0" class="dropdown-empty">
          No results found
        </div>
      </div>
    </div>

    <div v-if="selectedItems.length > 0" class="search-info">
      Selected {{ selectedItems.length }} item{{ selectedItems.length > 1 ? 's' : '' }} - 
      Showing {{ resultCount }} nodes in graph
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAnalysisStore } from '../stores/analysisStore'

const store = useAnalysisStore()
const searchInput = ref('')
const showDropdown = ref(false)
const searchWrapper = ref(null)
const selectedItems = ref([])
const highlightedIndex = ref(0)

const resultCount = computed(() => {
  if (!store.filteredData) return 0
  return (
    (store.filteredData.endpoints?.length || 0) +
    (store.filteredData.services?.length || 0) +
    (store.filteredData.repositories?.length || 0)
  )
})

const filteredEndpoints = computed(() => {
  if (!store.analysisData) return []
  const query = searchInput.value.toLowerCase()
  if (!query) return store.endpoints
  
  return store.endpoints.filter(endpoint => 
    matchesQuery(endpoint, query)
  )
})

const filteredServices = computed(() => {
  if (!store.analysisData) return []
  const query = searchInput.value.toLowerCase()
  if (!query) return store.services
  
  return store.services.filter(service => 
    matchesQuery(service, query)
  )
})

const filteredRepositories = computed(() => {
  if (!store.analysisData) return []
  const query = searchInput.value.toLowerCase()
  if (!query) return store.repositories
  
  return store.repositories.filter(repo => 
    matchesQuery(repo, query)
  )
})

const filteredOptions = computed(() => {
  return [
    ...filteredEndpoints.value,
    ...filteredServices.value,
    ...filteredRepositories.value
  ]
})

function matchesQuery(item, query) {
  const searchableFields = [
    item.methodName,
    item.className,
    item.controllerClass,
    item.path,
    item.httpMethod,
    item.javadoc,
    item.returnType,
    ...(item.parameters || []).map(p => p.name + ' ' + p.type),
    ...(item.annotations || [])
  ]

  return searchableFields.some(field => 
    field && field.toString().toLowerCase().includes(query)
  )
}

function onSearch() {
  showDropdown.value = true
  highlightedIndex.value = 0
}

function toggleItem(item, type) {
  const itemId = item.id
  const existingIndex = selectedItems.value.findIndex(i => i.id === itemId)
  
  if (existingIndex >= 0) {
    selectedItems.value.splice(existingIndex, 1)
  } else {
    let label = ''
    if (type === 'endpoint') {
      label = `${item.httpMethod} ${item.path}`
    } else if (type === 'service') {
      label = `${item.className}.${item.methodName}`
    } else {
      label = `${item.className}.${item.methodName}`
    }
    
    selectedItems.value.push({
      id: itemId,
      type,
      label,
      data: item
    })
  }
  
  updateFilter()
}

function removeItem(item) {
  const index = selectedItems.value.findIndex(i => i.id === item.id)
  if (index >= 0) {
    selectedItems.value.splice(index, 1)
    updateFilter()
  }
}

function clearAll() {
  selectedItems.value = []
  searchInput.value = ''
  updateFilter()
}

function isSelected(id) {
  return selectedItems.value.some(item => item.id === id)
}

function updateFilter() {
  if (selectedItems.value.length === 0) {
    store.setSearchKeyword('')
    return
  }
  
  // Build a filter based on selected items
  const selectedIds = new Set(selectedItems.value.map(i => i.id))
  
  // Filter the data
  const filteredEndpoints = store.endpoints.filter(e => selectedIds.has(e.id))
  const filteredServices = store.services.filter(s => selectedIds.has(s.id))
  const filteredRepositories = store.repositories.filter(r => selectedIds.has(r.id))
  
  // Include connected nodes
  const allNodeIds = new Set(selectedIds)
  
  // Add connected services from endpoints
  filteredEndpoints.forEach(endpoint => {
    endpoint.callChain.forEach(call => {
      store.services.forEach(service => {
        if (call.includes(service.methodName) || call.includes(service.className)) {
          allNodeIds.add(service.id)
          filteredServices.push(service)
        }
      })
    })
  })
  
  // Add connected repositories from services
  filteredServices.forEach(service => {
    service.calls.forEach(call => {
      store.repositories.forEach(repo => {
        if (call.includes(repo.methodName) || call.includes(repo.className)) {
          allNodeIds.add(repo.id)
          filteredRepositories.push(repo)
        }
      })
    })
  })
  
  // Filter call graph
  const filteredNodes = store.callGraph.nodes.filter(node => allNodeIds.has(node.id))
  const filteredEdges = store.callGraph.edges.filter(edge => 
    allNodeIds.has(edge.from) && allNodeIds.has(edge.to)
  )
  
  store.filteredData = {
    ...store.analysisData,
    endpoints: [...new Set(filteredEndpoints)],
    services: [...new Set(filteredServices)],
    repositories: [...new Set(filteredRepositories)],
    callGraph: {
      nodes: filteredNodes,
      edges: filteredEdges
    }
  }
}

function getItemIndex(type, index) {
  let offset = 0
  if (type === 'service') {
    offset = Math.min(filteredEndpoints.value.length, 10)
  } else if (type === 'repository') {
    offset = Math.min(filteredEndpoints.value.length, 10) + Math.min(filteredServices.value.length, 10)
  }
  return offset + index
}

function navigateDown() {
  const maxIndex = Math.min(
    filteredEndpoints.value.length, 10
  ) + Math.min(
    filteredServices.value.length, 10
  ) + Math.min(
    filteredRepositories.value.length, 10
  ) - 1
  
  if (highlightedIndex.value < maxIndex) {
    highlightedIndex.value++
  }
}

function navigateUp() {
  if (highlightedIndex.value > 0) {
    highlightedIndex.value--
  }
}

function selectHighlighted() {
  // Find the highlighted item and toggle it
  let currentIndex = 0
  
  const endpointLimit = Math.min(filteredEndpoints.value.length, 10)
  if (highlightedIndex.value < endpointLimit) {
    toggleItem(filteredEndpoints.value[highlightedIndex.value], 'endpoint')
    return
  }
  currentIndex += endpointLimit
  
  const serviceLimit = Math.min(filteredServices.value.length, 10)
  if (highlightedIndex.value < currentIndex + serviceLimit) {
    toggleItem(filteredServices.value[highlightedIndex.value - currentIndex], 'service')
    return
  }
  currentIndex += serviceLimit
  
  const repoLimit = Math.min(filteredRepositories.value.length, 10)
  if (highlightedIndex.value < currentIndex + repoLimit) {
    toggleItem(filteredRepositories.value[highlightedIndex.value - currentIndex], 'repository')
    return
  }
}

function handleClickOutside(event) {
  if (searchWrapper.value && !searchWrapper.value.contains(event.target)) {
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.search-bar {
  width: 100%;
  padding: 1rem;
  background-color: white;
  border-bottom: 1px solid #e0e0e0;
}

.search-container {
  position: relative;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  background-color: white;
  min-height: 48px;
  transition: border-color 0.2s;
}

.search-input-wrapper:focus-within {
  border-color: #3498db;
}

.selected-items {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.selected-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
}

.selected-tag.endpoint {
  background-color: #d5f4e6;
  color: #27ae60;
}

.selected-tag.service {
  background-color: #d6eaf8;
  color: #2980b9;
}

.selected-tag.repository {
  background-color: #fadbd8;
  color: #c0392b;
}

.remove-btn {
  background: none;
  border: none;
  color: inherit;
  font-size: 1.25rem;
  line-height: 1;
  cursor: pointer;
  padding: 0;
  opacity: 0.7;
}

.remove-btn:hover {
  opacity: 1;
}

.search-input {
  flex: 1;
  min-width: 200px;
  border: none;
  outline: none;
  font-size: 1rem;
  padding: 0.25rem;
}

.clear-all-btn {
  background-color: #95a5a6;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  font-size: 0.875rem;
  cursor: pointer;
  white-space: nowrap;
}

.clear-all-btn:hover {
  background-color: #7f8c8d;
}

.dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 0.5rem;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-height: 400px;
  overflow-y: auto;
  z-index: 1000;
}

.dropdown-section {
  padding: 0.5rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.dropdown-section:last-child {
  border-bottom: none;
}

.dropdown-header {
  padding: 0.5rem 1rem;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  color: #666;
  background-color: #f8f9fa;
}

.dropdown-item {
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background-color 0.15s;
}

.dropdown-item:hover,
.dropdown-item.highlighted {
  background-color: #f8f9fa;
}

.dropdown-item.selected {
  background-color: #e8f5e9;
}

.item-main {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.item-meta {
  font-size: 0.875rem;
  color: #666;
  font-family: monospace;
}

.http-method {
  display: inline-block;
  padding: 0.125rem 0.5rem;
  border-radius: 3px;
  font-size: 0.75rem;
  font-weight: 700;
  color: white;
}

.http-method.GET { background-color: #2ecc71; }
.http-method.POST { background-color: #3498db; }
.http-method.PUT { background-color: #f39c12; }
.http-method.DELETE { background-color: #e74c3c; }
.http-method.PATCH { background-color: #9b59b6; }

.item-path {
  font-family: monospace;
  font-size: 0.9rem;
}

.dropdown-more {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  color: #999;
  font-style: italic;
}

.dropdown-empty {
  padding: 2rem;
  text-align: center;
  color: #999;
}

.search-info {
  margin-top: 0.75rem;
  font-size: 0.875rem;
  color: #666;
}
</style>
