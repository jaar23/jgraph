import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as searchAlgorithm from '../utils/searchAlgorithm'
import * as centralityCalculator from '../utils/centralityCalculator'

export const useAnalysisStore = defineStore('analysis', () => {
  // Original analysis data
  const analysisData = ref(null)
  const searchKeyword = ref('')
  const selectedNode = ref(null)
  const filteredData = ref(null)
  
  // User annotations
  const nodeAnnotations = ref(new Map())
  const edgeAnnotations = ref(new Map())
  
  // Default LLM configuration
  const defaultLLMConfig = {
    endpoint: 'https://api.openai.com/v1/chat/completions',
    model: 'gpt-4-turbo-preview',
    apiKey: '',
    temperature: 0.3,
    maxTokens: 2000,
    systemPrompt: `You are an expert Java software architect specializing in Spring Boot and Jakarta EE applications. 
You analyze code flows, identify issues, and provide actionable solutions.

Your responses should:
1. Be concise and specific
2. Reference exact classes and methods
3. Include code snippets when suggesting fixes
4. Consider Spring/Jakarta EE best practices
5. Explain the reasoning behind your analysis`,
    userPromptTemplate: `Analyze this Java code flow issue:

Error Message:
{errorMessage}

Call Path:
{callPath}

Components Involved:
{componentDetails}

Please provide:
1. Root Cause Analysis
2. Problematic Component (specify which node)
3. Recommended Fix
4. Prevention Tips`
  }

  // Load LLM config from localStorage or use defaults
  function loadLLMConfigFromStorage() {
    try {
      const stored = localStorage.getItem('jgraph-llm-config')
      if (stored) {
        return { ...defaultLLMConfig, ...JSON.parse(stored) }
      }
    } catch (error) {
      console.error('Failed to load LLM config from localStorage:', error)
    }
    return defaultLLMConfig
  }

  // LLM features
  const llmConfig = ref(loadLLMConfigFromStorage())
  
  const llmAnalyses = ref([])
  const chatHistory = ref([])
  const currentChatSession = ref(null)
  
  // UI state
  const selectedNodes = ref([])
  const unsavedChanges = ref(false)
  const lastExportTimestamp = ref(null)

  // Computed
  const hasData = computed(() => analysisData.value !== null)
  
  const endpoints = computed(() => analysisData.value?.endpoints || [])
  const services = computed(() => analysisData.value?.services || [])
  const repositories = computed(() => analysisData.value?.repositories || [])
  const logStatements = computed(() => analysisData.value?.logStatements || [])
  const tryCatchBlocks = computed(() => analysisData.value?.tryCatchBlocks || [])
  const exceptionHandlers = computed(() => analysisData.value?.exceptionHandlers || [])
  const databaseOperations = computed(() => analysisData.value?.databaseOperations || [])
  const transactions = computed(() => analysisData.value?.transactions || [])
  const externalCalls = computed(() => analysisData.value?.externalCalls || [])
  const dataFlows = computed(() => analysisData.value?.dataFlows || [])
  const callGraph = computed(() => analysisData.value?.callGraph || { nodes: [], edges: [] })
  const statistics = computed(() => analysisData.value?.statistics || {})

  const hasUnsavedChanges = computed(() => unsavedChanges.value)

  // Actions
  function loadAnalysisData(data) {
    analysisData.value = data
    filteredData.value = data
    
    // Load user annotations if present in data
    if (data.userAnnotations) {
      loadUserAnnotations(data.userAnnotations)
    }
  }

  function clearData() {
    analysisData.value = null
    filteredData.value = null
    searchKeyword.value = ''
    selectedNode.value = null
    nodeAnnotations.value.clear()
    edgeAnnotations.value.clear()
    llmAnalyses.value = []
    chatHistory.value = []
    selectedNodes.value = []
    unsavedChanges.value = false
  }

  function setSearchKeyword(keyword) {
    searchKeyword.value = keyword.toLowerCase()
    filterData()
  }

  function setSelectedNode(node) {
    selectedNode.value = node
  }
  
  function setSelectedNodes(nodes) {
    selectedNodes.value = nodes
  }
  
  function toggleNodeSelection(nodeId) {
    const index = selectedNodes.value.indexOf(nodeId)
    if (index >= 0) {
      selectedNodes.value.splice(index, 1)
    } else {
      selectedNodes.value.push(nodeId)
    }
  }
  
  function expandScopeForNode(nodeId) {
    if (!analysisData.value || !analysisData.value.callGraph) return []
    
    const expandedNodes = new Set([nodeId])
    const edges = analysisData.value.callGraph.edges
    
    // Find all directly connected nodes
    edges.forEach(edge => {
      if (edge.from === nodeId) expandedNodes.add(edge.to)
      if (edge.to === nodeId) expandedNodes.add(edge.from)
    })
    
    // Find parent method for special nodes (logs, exceptions, db, external)
    const node = analysisData.value.callGraph.nodes.find(n => n.id === nodeId)
    if (node) {
      if (['log', 'exception', 'database', 'external', 'dataflow'].includes(node.type)) {
        // Find parent method by checking edges
        edges.forEach(edge => {
          if (edge.to === nodeId) {
            const parentNode = analysisData.value.callGraph.nodes.find(n => n.id === edge.from)
            if (parentNode && ['endpoint', 'service', 'repository'].includes(parentNode.type)) {
              expandedNodes.add(edge.from)
            }
          }
        })
      }
    }
    
    return Array.from(expandedNodes)
  }
  
  function expandDataFlow(dataflow) {
    if (!analysisData.value || !analysisData.value.callGraph) return []
    
    const expandedNodes = new Set()
    const methodId = dataflow.methodId
    
    // Add the dataflow node itself
    const dataflowNode = analysisData.value.callGraph.nodes.find(n => n.id === methodId && n.type === 'dataflow')
    if (dataflowNode) {
      expandedNodes.add(dataflowNode.id)
    }
    
    // Find and add the parent method (endpoint, service, or repository)
    const parentMethod = endpoints.value.find(e => e.id === methodId) ||
                        services.value.find(s => s.id === methodId) ||
                        repositories.value.find(r => r.id === methodId)
    if (parentMethod) {
      expandedNodes.add(parentMethod.id)
    }
    
    // Add all related database operations
    databaseOperations.value.forEach(op => {
      if (op.className === dataflow.className && op.methodName === dataflow.methodName) {
        const dbNode = analysisData.value.callGraph.nodes.find(n => 
          n.data?.className === op.className && 
          n.data?.methodName === op.methodName &&
          n.type === 'database'
        )
        if (dbNode) expandedNodes.add(dbNode.id)
      }
    })
    
    // Add all related logs
    logStatements.value.forEach(log => {
      if (log.className === dataflow.className && log.methodName === dataflow.methodName) {
        const logNode = analysisData.value.callGraph.nodes.find(n => 
          n.data?.className === log.className && 
          n.data?.methodName === log.methodName &&
          n.type === 'log'
        )
        if (logNode) expandedNodes.add(logNode.id)
      }
    })
    
    // Add all related exceptions
    tryCatchBlocks.value.forEach(ex => {
      if (ex.className === dataflow.className && ex.methodName === dataflow.methodName) {
        const exNode = analysisData.value.callGraph.nodes.find(n => 
          n.data?.className === ex.className && 
          n.data?.methodName === ex.methodName &&
          n.type === 'exception'
        )
        if (exNode) expandedNodes.add(exNode.id)
      }
    })
    
    // Add all related external calls
    externalCalls.value.forEach(call => {
      if (call.className === dataflow.className && call.callingMethod === dataflow.methodName) {
        const extNode = analysisData.value.callGraph.nodes.find(n => 
          n.data?.className === call.className && 
          n.data?.callingMethod === call.callingMethod &&
          n.type === 'external'
        )
        if (extNode) expandedNodes.add(extNode.id)
      }
    })
    
    // Add connected methods from data transfers
    if (dataflow.dataTransfers) {
      analysisData.value.callGraph.edges.forEach(edge => {
        if (expandedNodes.has(edge.from)) {
          expandedNodes.add(edge.to)
        }
        if (expandedNodes.has(edge.to)) {
          expandedNodes.add(edge.from)
        }
      })
    }
    
    return Array.from(expandedNodes)
  }
  
  /**
   * Advanced search using smart algorithm
   */
  function performAdvancedSearch(inputText, filters = {}) {
    if (!inputText || !analysisData.value) {
      return null
    }
    
    // Detect input type
    const inputType = searchAlgorithm.detectInputType(inputText)
    
    let matches = {
      logs: [],
      dataFlows: [],
      components: []
    }
    
    // Parse and search based on input type
    if (inputType === 'LOG_MESSAGE') {
      const parsed = searchAlgorithm.parseLogMessage(inputText)
      matches.logs = searchAlgorithm.fuzzyMatchLogs(parsed, logStatements.value)
    } else if (inputType === 'VARIABLE_NAME') {
      matches.dataFlows = searchAlgorithm.findVariableInDataFlows(inputText, dataFlows.value)
      
      // Also search in logs for variable
      const parsed = searchAlgorithm.parseLogMessage(inputText)
      matches.logs = searchAlgorithm.fuzzyMatchLogs(parsed, logStatements.value)
    } else if (inputType === 'JSON_PAYLOAD') {
      const parsed = searchAlgorithm.parseJSON(inputText)
      const jsonMatches = searchAlgorithm.findJSONFieldsInComponents(parsed, analysisData.value)
      
      // Combine JSON matches into components
      matches.components = [
        ...jsonMatches.endpoints.map(m => ({ ...m, type: 'endpoint', data: m.endpoint })),
        ...jsonMatches.dataFlows.map(m => ({ ...m, type: 'dataflow', data: m.dataFlow }))
      ]
    } else {
      // FREE_TEXT - search everywhere
      const parsed = searchAlgorithm.parseLogMessage(inputText)
      matches.logs = searchAlgorithm.fuzzyMatchLogs(parsed, logStatements.value)
      matches.dataFlows = searchAlgorithm.findVariableInDataFlows(inputText, dataFlows.value)
    }
    
    // Build component graph from matches
    const componentGraph = searchAlgorithm.buildComponentGraph(matches, analysisData.value)
    
    // Calculate centrality scores
    const centralityScores = centralityCalculator.calculateCentrality(
      componentGraph, 
      analysisData.value.callGraph
    )
    
    // Rank components
    const rankedComponents = centralityCalculator.rankComponents(componentGraph, centralityScores)
    
    // Apply filters
    let filteredComponents = rankedComponents
    
    if (filters.minCentrality) {
      filteredComponents = filteredComponents.filter(c => 
        c.combinedScore >= filters.minCentrality
      )
    }
    
    if (filters.componentTypes && filters.componentTypes.length > 0) {
      filteredComponents = filteredComponents.filter(c => 
        filters.componentTypes.includes(c.type)
      )
    }
    
    if (filters.maxResults) {
      filteredComponents = filteredComponents.slice(0, filters.maxResults)
    }
    
    // Build summary
    const summary = centralityCalculator.buildSearchSummary({
      components: filteredComponents,
      logs: matches.logs,
      dataFlows: matches.dataFlows
    })
    
    return {
      inputType,
      summary,
      components: filteredComponents,
      logs: matches.logs,
      dataFlows: matches.dataFlows,
      confidence: centralityCalculator.calculateConfidence(
        summary.averageCentrality
      )
    }
  }
  
  /**
   * Expand boundary with unlimited depth
   */
  function expandBoundaryUnlimited(nodeId, direction = 'both') {
    if (!analysisData.value || !analysisData.value.callGraph) return { nodes: [], edges: [], maxDepth: 0 }
    
    const visited = new Set()
    const result = { nodes: new Set(), edges: [], maxDepth: 0 }
    const queue = [{ id: nodeId, depth: 0 }]
    
    // Safety limit
    let iterations = 0
    const MAX_ITERATIONS = 1000
    
    while (queue.length > 0 && iterations < MAX_ITERATIONS) {
      const { id, depth } = queue.shift()
      iterations++
      
      if (visited.has(id)) continue
      visited.add(id)
      result.nodes.add(id)
      result.maxDepth = Math.max(result.maxDepth, depth)
      
      // Find connected nodes based on direction
      if (direction === 'upstream' || direction === 'both') {
        // Find callers
        callGraph.value.edges
          .filter(e => e.to === id && !visited.has(e.from))
          .forEach(e => {
            queue.push({ id: e.from, depth: depth + 1 })
            result.edges.push(e)
          })
      }
      
      if (direction === 'downstream' || direction === 'both') {
        // Find callees
        callGraph.value.edges
          .filter(e => e.from === id && !visited.has(e.to))
          .forEach(e => {
            queue.push({ id: e.to, depth: depth + 1 })
            result.edges.push(e)
          })
      }
    }
    
    return {
      nodes: Array.from(result.nodes),
      edges: result.edges,
      maxDepth: result.maxDepth,
      totalNodes: result.nodes.size
    }
  }
  
  /**
   * Trace upstream (find all callers)
   */
  function traceUpstream(nodeId, maxIterations = 100) {
    return expandBoundaryUnlimited(nodeId, 'upstream')
  }
  
  /**
   * Trace downstream (find all callees)
   */
  function traceDownstream(nodeId, maxIterations = 100) {
    return expandBoundaryUnlimited(nodeId, 'downstream')
  }
  
  /**
   * Calculate complete boundary path
   */
  function calculateBoundaryPath(nodeId) {
    const upstream = traceUpstream(nodeId)
    const downstream = traceDownstream(nodeId)
    
    // Find entry points (endpoints with no incoming edges)
    const entryPoints = upstream.nodes.filter(nId => {
      const node = callGraph.value.nodes.find(n => n.id === nId)
      return node && node.type === 'endpoint'
    })
    
    // Find exit points (database/external with no outgoing edges)
    const exitPoints = downstream.nodes.filter(nId => {
      const node = callGraph.value.nodes.find(n => n.id === nId)
      return node && (node.type === 'database' || node.type === 'external')
    })
    
    return {
      upstream: upstream.nodes,
      downstream: downstream.nodes,
      fullPath: [...upstream.nodes, ...downstream.nodes],
      entryPoints,
      exitPoints,
      depth: {
        upstream: upstream.maxDepth,
        downstream: downstream.maxDepth,
        total: upstream.maxDepth + downstream.maxDepth
      }
    }
  }

  function filterData() {
    if (!analysisData.value || !searchKeyword.value) {
      filteredData.value = analysisData.value
      return
    }

    const keyword = searchKeyword.value

    const filteredEndpoints = endpoints.value.filter(endpoint => 
      matchesKeyword(endpoint, keyword)
    )

    const filteredServices = services.value.filter(service => 
      matchesKeyword(service, keyword)
    )

    const filteredRepositories = repositories.value.filter(repo => 
      matchesKeyword(repo, keyword)
    )

    const filteredLogs = logStatements.value.filter(log => 
      matchesLogKeyword(log, keyword)
    )

    const filteredNodeIds = new Set([
      ...filteredEndpoints.map(e => e.id),
      ...filteredServices.map(s => s.id),
      ...filteredRepositories.map(r => r.id),
      ...filteredLogs.map(l => l.id)
    ])

    filteredEndpoints.forEach(endpoint => {
      endpoint.callChain.forEach(call => {
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
      logStatements: filteredLogs,
      callGraph: {
        nodes: filteredNodes,
        edges: filteredEdges
      }
    }
  }

  function matchesLogKeyword(log, keyword) {
    const searchableFields = [
      log.message,
      log.level,
      log.loggerFramework,
      log.filePath,
      log.exceptionVariable,
      ...(log.variables || []),
      ...(log.mdcKeys || [])
    ]

    return searchableFields.some(field => 
      field && field.toString().toLowerCase().includes(keyword)
    )
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

  // Annotation management
  function addNodeAnnotation(nodeId, annotation) {
    const existing = nodeAnnotations.value.get(nodeId) || { notes: [], tags: [], customFields: {} }
    
    if (annotation.note) {
      existing.notes.push({
        id: `note-${Date.now()}`,
        text: annotation.note,
        createdAt: new Date().toISOString(),
        severity: annotation.severity || 'info'
      })
    }
    
    if (annotation.tags) {
      existing.tags = [...new Set([...existing.tags, ...annotation.tags])]
    }
    
    if (annotation.customFields) {
      existing.customFields = { ...existing.customFields, ...annotation.customFields }
    }
    
    nodeAnnotations.value.set(nodeId, existing)
    unsavedChanges.value = true
  }
  
  function removeNodeAnnotation(nodeId, noteId) {
    const existing = nodeAnnotations.value.get(nodeId)
    if (existing) {
      existing.notes = existing.notes.filter(n => n.id !== noteId)
      if (existing.notes.length === 0 && existing.tags.length === 0) {
        nodeAnnotations.value.delete(nodeId)
      }
      unsavedChanges.value = true
    }
  }
  
  function updateNodeAnnotation(nodeId, noteId, updates) {
    const existing = nodeAnnotations.value.get(nodeId)
    if (existing) {
      const note = existing.notes.find(n => n.id === noteId)
      if (note) {
        Object.assign(note, updates)
        unsavedChanges.value = true
      }
    }
  }
  
  function getNodeAnnotation(nodeId) {
    return nodeAnnotations.value.get(nodeId) || { notes: [], tags: [], customFields: {} }
  }
  
  function hasNodeAnnotation(nodeId) {
    const ann = nodeAnnotations.value.get(nodeId)
    return ann && (ann.notes.length > 0 || ann.tags.length > 0)
  }

  // LLM management
  function updateLLMConfig(config) {
    llmConfig.value = { ...llmConfig.value, ...config }
    
    // Save to localStorage
    try {
      localStorage.setItem('jgraph-llm-config', JSON.stringify(llmConfig.value))
    } catch (error) {
      console.error('Failed to save LLM config to localStorage:', error)
    }
    
    // Don't mark as unsaved for config changes
  }
  
  function resetLLMConfig() {
    llmConfig.value = { ...defaultLLMConfig }
    
    // Remove from localStorage
    try {
      localStorage.removeItem('jgraph-llm-config')
    } catch (error) {
      console.error('Failed to remove LLM config from localStorage:', error)
    }
  }
  
  function addLLMAnalysis(analysis) {
    llmAnalyses.value.push({
      id: `analysis-${Date.now()}`,
      timestamp: new Date().toISOString(),
      ...analysis
    })
    unsavedChanges.value = true
  }
  
  function startChatSession(context) {
    currentChatSession.value = {
      id: `chat-${Date.now()}`,
      startedAt: new Date().toISOString(),
      context: context || selectedNodes.value,
      messages: []
    }
  }
  
  function addChatMessage(message) {
    if (currentChatSession.value) {
      currentChatSession.value.messages.push({
        ...message,
        timestamp: new Date().toISOString()
      })
      unsavedChanges.value = true
    }
  }
  
  function endChatSession() {
    if (currentChatSession.value) {
      chatHistory.value.push(currentChatSession.value)
      currentChatSession.value = null
    }
  }

  // Export functionality
  function getExportData(options = {}) {
    const {
      includeAnnotations = true,
      includeLLMInsights = true,
      includeChatHistory = false,
      selectedNodesOnly = false
    } = options

    let exportData = {
      ...analysisData.value
    }

    // Build user annotations section
    const userAnnotations = {
      version: '1.0',
      createdAt: analysisData.value?.project?.analyzedAt || new Date().toISOString(),
      lastModified: new Date().toISOString()
    }

    if (includeAnnotations) {
      const nodeAnnotationsObj = {}
      nodeAnnotations.value.forEach((value, key) => {
        if (!selectedNodesOnly || selectedNodes.value.includes(key)) {
          nodeAnnotationsObj[key] = value
        }
      })
      userAnnotations.nodeAnnotations = nodeAnnotationsObj
      
      const edgeAnnotationsObj = {}
      edgeAnnotations.value.forEach((value, key) => {
        edgeAnnotationsObj[key] = value
      })
      userAnnotations.edgeAnnotations = edgeAnnotationsObj
    }

    if (includeLLMInsights) {
      userAnnotations.llmInsights = {
        analyses: llmAnalyses.value.filter(a => 
          !selectedNodesOnly || a.selectedNodes.some(n => selectedNodes.value.includes(n))
        )
      }
      
      if (includeChatHistory) {
        userAnnotations.llmInsights.chatHistory = chatHistory.value
      }
    }

    userAnnotations.metadata = {
      appVersion: '1.0.0',
      exportedAt: new Date().toISOString()
    }

    exportData.userAnnotations = userAnnotations

    // Filter by selected nodes if requested
    if (selectedNodesOnly && selectedNodes.value.length > 0) {
      const selectedSet = new Set(selectedNodes.value)
      exportData.endpoints = exportData.endpoints.filter(e => selectedSet.has(e.id))
      exportData.services = exportData.services.filter(s => selectedSet.has(s.id))
      exportData.repositories = exportData.repositories.filter(r => selectedSet.has(r.id))
      exportData.callGraph.nodes = exportData.callGraph.nodes.filter(n => selectedSet.has(n.id))
      exportData.callGraph.edges = exportData.callGraph.edges.filter(e => 
        selectedSet.has(e.from) && selectedSet.has(e.to)
      )
    }

    return exportData
  }

  function exportToJSON(options = {}) {
    const data = getExportData(options)
    const jsonString = JSON.stringify(data, null, 2)
    const blob = new Blob([jsonString], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    
    const projectName = data.project?.name || 'analysis'
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-').substring(0, 19)
    const filename = `${projectName}-enhanced-${timestamp}.json`
    
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    
    URL.revokeObjectURL(url)
    
    lastExportTimestamp.value = new Date().toISOString()
    unsavedChanges.value = false
    
    return filename
  }

  function loadUserAnnotations(userAnnotations) {
    // Load node annotations
    if (userAnnotations.nodeAnnotations) {
      nodeAnnotations.value.clear()
      Object.entries(userAnnotations.nodeAnnotations).forEach(([key, value]) => {
        nodeAnnotations.value.set(key, value)
      })
    }
    
    // Load edge annotations
    if (userAnnotations.edgeAnnotations) {
      edgeAnnotations.value.clear()
      Object.entries(userAnnotations.edgeAnnotations).forEach(([key, value]) => {
        edgeAnnotations.value.set(key, value)
      })
    }
    
    // Load LLM insights
    if (userAnnotations.llmInsights) {
      llmAnalyses.value = userAnnotations.llmInsights.analyses || []
      chatHistory.value = userAnnotations.llmInsights.chatHistory || []
    }
    
    unsavedChanges.value = false
  }

  return {
    // State
    analysisData,
    searchKeyword,
    selectedNode,
    filteredData,
    selectedNodes,
    nodeAnnotations,
    edgeAnnotations,
    llmConfig,
    llmAnalyses,
    chatHistory,
    currentChatSession,
    unsavedChanges,
    lastExportTimestamp,
    
    // Computed
    hasData,
    endpoints,
    services,
    repositories,
    logStatements,
    tryCatchBlocks,
    exceptionHandlers,
    databaseOperations,
    transactions,
    externalCalls,
    dataFlows,
    callGraph,
    statistics,
    hasUnsavedChanges,
    
    // Actions
    loadAnalysisData,
    clearData,
    setSearchKeyword,
    setSelectedNode,
    setSelectedNodes,
    toggleNodeSelection,
    expandScopeForNode,
    expandDataFlow,
    performAdvancedSearch,
    expandBoundaryUnlimited,
    traceUpstream,
    traceDownstream,
    calculateBoundaryPath,
    
    // Annotations
    addNodeAnnotation,
    removeNodeAnnotation,
    updateNodeAnnotation,
    getNodeAnnotation,
    hasNodeAnnotation,
    
    // LLM
    updateLLMConfig,
    resetLLMConfig,
    addLLMAnalysis,
    startChatSession,
    addChatMessage,
    endChatSession,
    
    // Export
    getExportData,
    exportToJSON
  }
})
