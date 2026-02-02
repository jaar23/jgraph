import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAnalysisStore = defineStore('analysis', () => {
  // State
  const analysisData = ref(null)
  const searchKeyword = ref('')
  const selectedNode = ref(null)
  const filteredData = ref(null)

  // Computed
  const hasData = computed(() => analysisData.value !== null)
  
  const endpoints = computed(() => analysisData.value?.endpoints || [])
  const services = computed(() => analysisData.value?.services || [])
  const repositories = computed(() => analysisData.value?.repositories || [])
  const callGraph = computed(() => analysisData.value?.callGraph || { nodes: [], edges: [] })
  const statistics = computed(() => analysisData.value?.statistics || {})

  // Actions
  function loadAnalysisData(data) {
    analysisData.value = data
    filteredData.value = data
  }

  function clearData() {
    analysisData.value = null
    filteredData.value = null
    searchKeyword.value = ''
    selectedNode.value = null
  }

  function setSearchKeyword(keyword) {
    searchKeyword.value = keyword.toLowerCase()
    filterData()
  }

  function setSelectedNode(node) {
    selectedNode.value = node
  }

  function filterData() {
    if (!analysisData.value || !searchKeyword.value) {
      filteredData.value = analysisData.value
      return
    }

    const keyword = searchKeyword.value

    // Filter endpoints
    const filteredEndpoints = endpoints.value.filter(endpoint => 
      matchesKeyword(endpoint, keyword)
    )

    // Filter services
    const filteredServices = services.value.filter(service => 
      matchesKeyword(service, keyword)
    )

    // Filter repositories
    const filteredRepositories = repositories.value.filter(repo => 
      matchesKeyword(repo, keyword)
    )

    // Build filtered node IDs
    const filteredNodeIds = new Set([
      ...filteredEndpoints.map(e => e.id),
      ...filteredServices.map(s => s.id),
      ...filteredRepositories.map(r => r.id)
    ])

    // Include connected nodes (expand search to call chains)
    filteredEndpoints.forEach(endpoint => {
      endpoint.callChain.forEach(call => {
        // Find matching service/repo
        services.value.forEach(service => {
          if (call.includes(service.methodName) || call.includes(service.className)) {
            filteredNodeIds.add(service.id)
          }
        })
      })
    })

    filteredServices.forEach(service => {
      service.calls.forEach(call => {
        repositories.value.forEach(repo => {
          if (call.includes(repo.methodName) || call.includes(repo.className)) {
            filteredNodeIds.add(repo.id)
          }
        })
      })
    })

    // Filter call graph
    const filteredNodes = callGraph.value.nodes.filter(node => 
      filteredNodeIds.has(node.id)
    )

    const filteredEdges = callGraph.value.edges.filter(edge => 
      filteredNodeIds.has(edge.from) && filteredNodeIds.has(edge.to)
    )

    filteredData.value = {
      ...analysisData.value,
      endpoints: filteredEndpoints,
      services: filteredServices,
      repositories: filteredRepositories,
      callGraph: {
        nodes: filteredNodes,
        edges: filteredEdges
      }
    }
  }

  function matchesKeyword(item, keyword) {
    const searchableFields = [
      item.methodName,
      item.className,
      item.controllerClass,
      item.path,
      item.httpMethod,
      item.javadoc,
      item.returnType,
      ...(item.parameters || []).map(p => p.name + ' ' + p.type),
      ...(item.annotations || []),
      ...(item.callChain || []),
      ...(item.calls || [])
    ]

    return searchableFields.some(field => 
      field && field.toString().toLowerCase().includes(keyword)
    )
  }

  return {
    // State
    analysisData,
    searchKeyword,
    selectedNode,
    filteredData,
    // Computed
    hasData,
    endpoints,
    services,
    repositories,
    callGraph,
    statistics,
    // Actions
    loadAnalysisData,
    clearData,
    setSearchKeyword,
    setSelectedNode
  }
})
