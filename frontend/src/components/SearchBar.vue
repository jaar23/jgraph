<template>
  <div class="search-bar">
    <!-- Tab Navigation -->
    <div class="tab-nav">
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'quick' }"
        @click="activeTab = 'quick'"
      >
        🔍 Quick Search
      </button>
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'advanced' }"
        @click="activeTab = 'advanced'"
      >
        🎯 Advanced Trace
      </button>
    </div>

    <!-- Quick Search Tab (existing functionality) -->
    <div v-show="activeTab === 'quick'" class="search-container">
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
          <div class="dropdown-header">
            <span>Endpoints ({{ filteredEndpoints.length }})</span>
            <button 
              v-if="filteredEndpoints.length > ITEMS_PER_SECTION"
              @click.stop="toggleExpandSection('endpoints')"
              class="expand-btn"
            >
              {{ expandedSections.endpoints ? 'Show Less' : 'Show All' }}
            </button>
          </div>
          <div
            v-for="(endpoint, index) in getVisibleItems(filteredEndpoints, 'endpoints')"
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
          <div 
            v-if="filteredEndpoints.length > ITEMS_PER_SECTION && !expandedSections.endpoints" 
            class="dropdown-more-clickable"
            @click="toggleExpandSection('endpoints')"
          >
            +{{ filteredEndpoints.length - ITEMS_PER_SECTION }} more endpoints... (click to load)
          </div>
        </div>

        <div class="dropdown-section" v-if="filteredServices.length > 0">
          <div class="dropdown-header">
            <span>Services ({{ filteredServices.length }})</span>
            <button 
              v-if="filteredServices.length > ITEMS_PER_SECTION"
              @click.stop="toggleExpandSection('services')"
              class="expand-btn"
            >
              {{ expandedSections.services ? 'Show Less' : 'Show All' }}
            </button>
          </div>
          <div
            v-for="(service, index) in getVisibleItems(filteredServices, 'services')"
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
          <div 
            v-if="filteredServices.length > ITEMS_PER_SECTION && !expandedSections.services" 
            class="dropdown-more-clickable"
            @click="toggleExpandSection('services')"
          >
            +{{ filteredServices.length - ITEMS_PER_SECTION }} more services... (click to load)
          </div>
        </div>

        <div class="dropdown-section" v-if="filteredRepositories.length > 0">
          <div class="dropdown-header">
            <span>Repositories ({{ filteredRepositories.length }})</span>
            <button 
              v-if="filteredRepositories.length > ITEMS_PER_SECTION"
              @click.stop="toggleExpandSection('repositories')"
              class="expand-btn"
            >
              {{ expandedSections.repositories ? 'Show Less' : 'Show All' }}
            </button>
          </div>
          <div
            v-for="(repo, index) in getVisibleItems(filteredRepositories, 'repositories')"
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
          <div 
            v-if="filteredRepositories.length > ITEMS_PER_SECTION && !expandedSections.repositories" 
            class="dropdown-more-clickable"
            @click="toggleExpandSection('repositories')"
          >
            +{{ filteredRepositories.length - ITEMS_PER_SECTION }} more repositories... (click to load)
          </div>
        </div>

        <div class="dropdown-section" v-if="filteredDataFlows.length > 0">
          <div class="dropdown-header">
            <span>Data Flows ({{ filteredDataFlows.length }})</span>
            <button 
              v-if="filteredDataFlows.length > ITEMS_PER_SECTION"
              @click.stop="toggleExpandSection('dataflows')"
              class="expand-btn"
            >
              {{ expandedSections.dataflows ? 'Show Less' : 'Show All' }}
            </button>
          </div>
          <div
            v-for="(dataflow, index) in getVisibleItems(filteredDataFlows, 'dataflows')"
            :key="dataflow.methodId"
            class="dropdown-item dataflow-item"
            :class="{ 
              selected: isSelected(dataflow.methodId),
              highlighted: highlightedIndex === getItemIndex('dataflow', index)
            }"
            @click="toggleItem(dataflow, 'dataflow')"
            @mouseenter="highlightedIndex = getItemIndex('dataflow', index)"
          >
            <div class="item-main">
              <span class="dataflow-icon">📊</span>
              <span>{{ dataflow.className }}.{{ dataflow.methodName }}</span>
            </div>
            <div class="item-meta">
              {{ dataflow.parameters?.length || 0 }} params, 
              {{ dataflow.variables?.length || 0 }} transforms, 
              {{ dataflow.returns?.length || 0 }} returns
            </div>
          </div>
          <div 
            v-if="filteredDataFlows.length > ITEMS_PER_SECTION && !expandedSections.dataflows" 
            class="dropdown-more-clickable"
            @click="toggleExpandSection('dataflows')"
          >
            +{{ filteredDataFlows.length - ITEMS_PER_SECTION }} more data flows... (click to load)
          </div>
        </div>

        <div v-if="filteredOptions.length === 0" class="dropdown-empty">
          No results found
        </div>
      </div>

      <div v-if="selectedItems.length > 0" class="search-info">
        Selected {{ selectedItems.length }} item{{ selectedItems.length > 1 ? 's' : '' }} - 
        Showing {{ resultCount }} nodes in graph
      </div>
    </div>

    <!-- Advanced Trace Tab (new functionality) -->
    <div v-show="activeTab === 'advanced'" class="advanced-trace-container">
      <AdvancedTraceInput @search="handleAdvancedSearch" />
      
      <!-- Search Notification -->
      <div v-if="searchNotification" class="search-notification" :class="searchNotification.type">
        <span class="notification-icon">{{ searchNotification.icon }}</span>
        <span class="notification-message">{{ searchNotification.message }}</span>
        <button @click="searchNotification = null" class="notification-close">×</button>
      </div>
      
      <SearchResults 
        v-if="advancedSearchResults" 
        :results="advancedSearchResults"
        @show-in-graph="handleShowInGraph"
        @expand-boundary="handleExpandBoundary"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAnalysisStore } from '../stores/analysisStore'
import AdvancedTraceInput from './search/AdvancedTraceInput.vue'
import SearchResults from './search/SearchResults.vue'

const store = useAnalysisStore()
const searchInput = ref('')
const showDropdown = ref(false)
const searchWrapper = ref(null)
const selectedItems = ref([])
const highlightedIndex = ref(0)
const activeTab = ref('quick')
const advancedSearchResults = ref(null)
const searchNotification = ref(null)

// Track expanded sections
const expandedSections = ref({
  endpoints: false,
  services: false,
  repositories: false,
  dataflows: false
})

// Items to show per section when collapsed
const ITEMS_PER_SECTION = 10

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

const filteredDataFlows = computed(() => {
  if (!store.analysisData) return []
  const query = searchInput.value.toLowerCase()
  if (!query) return store.dataFlows
  
  return store.dataFlows.filter(dataflow => 
    matchesDataFlowQuery(dataflow, query)
  )
})

const filteredOptions = computed(() => {
  return [
    ...filteredEndpoints.value,
    ...filteredServices.value,
    ...filteredRepositories.value,
    ...filteredDataFlows.value
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

function matchesDataFlowQuery(dataflow, query) {
  const searchableFields = [
    dataflow.methodName,
    dataflow.className,
    dataflow.methodId,
    ...(dataflow.parameters || []).map(p => p.name + ' ' + p.type),
    ...(dataflow.variables || []).map(v => v.variableName + ' ' + v.transformationType),
    ...(dataflow.returns || []).map(r => r.returnType + ' ' + r.expression),
    ...(dataflow.dataTransfers || []).map(t => t.targetMethod)
  ]

  return searchableFields.some(field => 
    field && field.toString().toLowerCase().includes(query)
  )
}

function onSearch() {
  showDropdown.value = true
  highlightedIndex.value = 0
  // Reset expanded sections when search changes
  expandedSections.value = {
    endpoints: false,
    services: false,
    repositories: false,
    dataflows: false
  }
}

function toggleItem(item, type) {
  const itemId = type === 'dataflow' ? item.methodId : item.id
  const existingIndex = selectedItems.value.findIndex(i => i.id === itemId)
  
  if (existingIndex >= 0) {
    selectedItems.value.splice(existingIndex, 1)
  } else {
    let label = ''
    if (type === 'endpoint') {
      label = `${item.httpMethod} ${item.path}`
    } else if (type === 'dataflow') {
      label = `📊 ${item.className}.${item.methodName}`
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
    // Clear selection and show all nodes
    store.setSelectedNodes([])
    store.filteredData = null
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
    if (endpoint.callChain) {
      endpoint.callChain.forEach(call => {
        store.services.forEach(service => {
          if (call.includes(service.methodName) || call.includes(service.className)) {
            allNodeIds.add(service.id)
            if (!filteredServices.find(s => s.id === service.id)) {
              filteredServices.push(service)
            }
          }
        })
      })
    }
  })
  
  // Add connected repositories from services
  filteredServices.forEach(service => {
    if (service.calls) {
      service.calls.forEach(call => {
        store.repositories.forEach(repo => {
          if (call.includes(repo.methodName) || call.includes(repo.className)) {
            allNodeIds.add(repo.id)
            if (!filteredRepositories.find(r => r.id === repo.id)) {
              filteredRepositories.push(repo)
            }
          }
        })
      })
    }
  })
  
  // Filter call graph
  const filteredNodes = store.callGraph.nodes.filter(node => allNodeIds.has(node.id))
  const filteredEdges = store.callGraph.edges.filter(edge => 
    allNodeIds.has(edge.from) && allNodeIds.has(edge.to)
  )
  
  // Set filtered data for the graph to render
  store.filteredData = {
    ...store.analysisData,
    endpoints: filteredEndpoints,
    services: filteredServices,
    repositories: filteredRepositories,
    callGraph: {
      nodes: filteredNodes,
      edges: filteredEdges
    }
  }
  
  // Also update selected nodes for highlighting in Vue Flow
  store.setSelectedNodes(Array.from(allNodeIds))
}

function getItemIndex(type, index) {
  let offset = 0
  if (type === 'service') {
    offset = expandedSections.value.endpoints 
      ? filteredEndpoints.value.length 
      : Math.min(filteredEndpoints.value.length, ITEMS_PER_SECTION)
  } else if (type === 'repository') {
    const endpointCount = expandedSections.value.endpoints 
      ? filteredEndpoints.value.length 
      : Math.min(filteredEndpoints.value.length, ITEMS_PER_SECTION)
    const serviceCount = expandedSections.value.services
      ? filteredServices.value.length
      : Math.min(filteredServices.value.length, ITEMS_PER_SECTION)
    offset = endpointCount + serviceCount
  } else if (type === 'dataflow') {
    const endpointCount = expandedSections.value.endpoints 
      ? filteredEndpoints.value.length 
      : Math.min(filteredEndpoints.value.length, ITEMS_PER_SECTION)
    const serviceCount = expandedSections.value.services
      ? filteredServices.value.length
      : Math.min(filteredServices.value.length, ITEMS_PER_SECTION)
    const repoCount = expandedSections.value.repositories
      ? filteredRepositories.value.length
      : Math.min(filteredRepositories.value.length, ITEMS_PER_SECTION)
    offset = endpointCount + serviceCount + repoCount
  }
  return offset + index
}

function navigateDown() {
  const endpointCount = expandedSections.value.endpoints 
    ? filteredEndpoints.value.length 
    : Math.min(filteredEndpoints.value.length, ITEMS_PER_SECTION)
  const serviceCount = expandedSections.value.services
    ? filteredServices.value.length
    : Math.min(filteredServices.value.length, ITEMS_PER_SECTION)
  const repoCount = expandedSections.value.repositories
    ? filteredRepositories.value.length
    : Math.min(filteredRepositories.value.length, ITEMS_PER_SECTION)
  const dataflowCount = expandedSections.value.dataflows
    ? filteredDataFlows.value.length
    : Math.min(filteredDataFlows.value.length, ITEMS_PER_SECTION)
  
  const maxIndex = endpointCount + serviceCount + repoCount + dataflowCount - 1
  
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
  
  const endpointLimit = expandedSections.value.endpoints 
    ? filteredEndpoints.value.length 
    : Math.min(filteredEndpoints.value.length, ITEMS_PER_SECTION)
  if (highlightedIndex.value < endpointLimit) {
    toggleItem(filteredEndpoints.value[highlightedIndex.value], 'endpoint')
    return
  }
  currentIndex += endpointLimit
  
  const serviceLimit = expandedSections.value.services
    ? filteredServices.value.length
    : Math.min(filteredServices.value.length, ITEMS_PER_SECTION)
  if (highlightedIndex.value < currentIndex + serviceLimit) {
    toggleItem(filteredServices.value[highlightedIndex.value - currentIndex], 'service')
    return
  }
  currentIndex += serviceLimit
  
  const repoLimit = expandedSections.value.repositories
    ? filteredRepositories.value.length
    : Math.min(filteredRepositories.value.length, ITEMS_PER_SECTION)
  if (highlightedIndex.value < currentIndex + repoLimit) {
    toggleItem(filteredRepositories.value[highlightedIndex.value - currentIndex], 'repository')
    return
  }
  currentIndex += repoLimit
  
  const dataflowLimit = expandedSections.value.dataflows
    ? filteredDataFlows.value.length
    : Math.min(filteredDataFlows.value.length, ITEMS_PER_SECTION)
  if (highlightedIndex.value < currentIndex + dataflowLimit) {
    toggleItem(filteredDataFlows.value[highlightedIndex.value - currentIndex], 'dataflow')
    return
  }
}

function toggleExpandSection(section) {
  expandedSections.value[section] = !expandedSections.value[section]
}

function getVisibleItems(items, section) {
  if (expandedSections.value[section]) {
    return items
  }
  return items.slice(0, ITEMS_PER_SECTION)
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

function handleAdvancedSearch(searchData) {
  try {
    // Clear previous notification
    searchNotification.value = null
    
    // Perform search
    const results = store.performAdvancedSearch(searchData.input, searchData.filters)
    
    console.log('[SearchBar] Search results:', results)
    
    if (!results || !results.components || results.components.length === 0) {
      // Check if we found logs or dataflows but no components
      const hasLogs = results && results.logs && results.logs.length > 0
      const hasDataFlows = results && results.dataFlows && results.dataFlows.length > 0
      
      let message = 'No components found matching your search.'
      if (hasLogs || hasDataFlows) {
        message += ` Found ${results.logs?.length || 0} log(s) and ${results.dataFlows?.length || 0} dataflow(s), but they may not be connected to any components. Try a different search term or check console for details.`
      } else {
        message += ' Try using keywords from log messages, variable names, or adjust your search input. Check browser console for debugging info.'
      }
      
      searchNotification.value = {
        type: 'warning',
        icon: '⚠️',
        message
      }
      advancedSearchResults.value = results
    } else {
      // Success
      searchNotification.value = {
        type: 'success',
        icon: '✅',
        message: `Found ${results.components.length} component(s) with ${results.summary.totalLogs} log(s) and ${results.summary.totalDataFlows} dataflow(s)`
      }
      advancedSearchResults.value = results
      
      // Auto-dismiss success notification after 5 seconds
      setTimeout(() => {
        if (searchNotification.value?.type === 'success') {
          searchNotification.value = null
        }
      }, 5000)
    }
  } catch (error) {
    console.error('Advanced search error:', error)
    searchNotification.value = {
      type: 'error',
      icon: '❌',
      message: `Search failed: ${error.message || 'Unknown error occurred'}. Check console for details.`
    }
    advancedSearchResults.value = null
  }
}

function handleShowInGraph(component) {
  // Expand and show this component in graph
  const expandedNodes = store.expandScopeForNode(component.id)
  store.setSelectedNodes(expandedNodes)
  
  // Also select this node specifically
  store.setSelectedNode(component.data)
}

function handleExpandBoundary(component, direction) {
  const boundary = store.expandBoundaryUnlimited(component.id, direction)
  store.setSelectedNodes(boundary.nodes)
}
</script>

<style scoped>
.search-bar {
  width: 100%;
  background-color: white;
  border-bottom: 1px solid #e0e0e0;
}

.tab-nav {
  display: flex;
  gap: 0;
  border-bottom: 2px solid #e0e0e0;
  background-color: #f8f9fa;
}

.tab-btn {
  flex: 1;
  padding: 0.75rem 1.5rem;
  border: none;
  background: transparent;
  font-size: 0.95rem;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 3px solid transparent;
}

.tab-btn:hover {
  background-color: #e5e7eb;
  color: #374151;
}

.tab-btn.active {
  color: #3498db;
  background-color: white;
  border-bottom-color: #3498db;
}

.search-container {
  padding: 1rem;
}

.advanced-trace-container {
  padding: 1rem;
  min-height: 200px;
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

.selected-tag.dataflow {
  background-color: #fef3c7;
  color: #f59e0b;
}

.dataflow-icon {
  font-size: 14px;
  margin-right: 4px;
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
  max-height: 500px;
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  color: #666;
  background-color: #f8f9fa;
}

.expand-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.expand-btn:hover {
  background-color: #2980b9;
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

.dropdown-more-clickable {
  padding: 0.75rem 1rem;
  font-size: 0.875rem;
  color: #3498db;
  font-weight: 500;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  background-color: #f8f9fa;
  border-top: 1px solid #e0e0e0;
}

.dropdown-more-clickable:hover {
  background-color: #e8f4f8;
  color: #2980b9;
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

/* Search Notification */
.search-notification {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-notification.success {
  background-color: #d1fae5;
  border: 2px solid #10b981;
  color: #065f46;
}

.search-notification.warning {
  background-color: #fef3c7;
  border: 2px solid #f59e0b;
  color: #92400e;
}

.search-notification.error {
  background-color: #fee2e2;
  border: 2px solid #ef4444;
  color: #991b1b;
}

.notification-icon {
  font-size: 1.5rem;
}

.notification-message {
  flex: 1;
  font-weight: 500;
  font-size: 0.9rem;
}

.notification-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: currentColor;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.notification-close:hover {
  opacity: 1;
}
</style>
