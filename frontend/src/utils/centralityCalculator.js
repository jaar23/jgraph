/**
 * Calculate centrality scores for components using PageRank-style algorithm
 */

/**
 * Edge type weights - higher weight = more important connection
 */
const EDGE_WEIGHTS = {
  'endpoint_to_service': 3.0,      // Endpoints calling services are critical
  'service_to_repository': 2.5,   // Service to repository flow
  'method_to_database': 2.5,       // Database access is important
  'method_to_external': 2.0,       // External API calls
  'method_to_log': 2.0,            // Logging provides visibility
  'method_to_dataflow': 2.5,       // Data flow tracking
  'method_to_exception': 1.5,      // Exception handling
  'default': 1.0                   // Default weight
}

/**
 * Calculate PageRank-style centrality for components
 */
export function calculateCentrality(components, callGraph, options = {}) {
  const {
    dampingFactor = 0.85,
    iterations = 20,
    tolerance = 0.0001
  } = options
  
  if (!components || components.length === 0) {
    return new Map()
  }
  
  // Build adjacency lists
  const nodeIds = new Set(components.map(c => c.id))
  const inLinks = new Map()
  const outLinks = new Map()
  const edgeWeights = new Map()
  
  // Initialize
  nodeIds.forEach(id => {
    inLinks.set(id, [])
    outLinks.set(id, [])
  })
  
  // Build graph with weighted edges
  if (callGraph && callGraph.edges) {
    callGraph.edges.forEach(edge => {
      if (nodeIds.has(edge.from) && nodeIds.has(edge.to)) {
        inLinks.get(edge.to).push(edge.from)
        outLinks.get(edge.from).push(edge.to)
        
        // Determine weight based on edge type
        const weight = getEdgeWeight(edge, components)
        const edgeKey = `${edge.from}->${edge.to}`
        edgeWeights.set(edgeKey, weight)
      }
    })
  }
  
  // Initialize scores
  const initialScore = 1.0 / nodeIds.size
  let scores = new Map()
  nodeIds.forEach(id => scores.set(id, initialScore))
  
  // PageRank iterations
  for (let iter = 0; iter < iterations; iter++) {
    const newScores = new Map()
    let totalChange = 0
    
    nodeIds.forEach(nodeId => {
      let score = (1 - dampingFactor) / nodeIds.size
      
      // Sum contributions from incoming links
      const incoming = inLinks.get(nodeId) || []
      incoming.forEach(sourceId => {
        const sourceOutLinks = outLinks.get(sourceId) || []
        if (sourceOutLinks.length > 0) {
          const sourceScore = scores.get(sourceId) || 0
          const edgeKey = `${sourceId}->${nodeId}`
          const weight = edgeWeights.get(edgeKey) || 1.0
          
          // Weighted contribution
          const contribution = (sourceScore / sourceOutLinks.length) * weight
          score += dampingFactor * contribution
        }
      })
      
      newScores.set(nodeId, score)
      totalChange += Math.abs(score - (scores.get(nodeId) || 0))
    })
    
    scores = newScores
    
    // Check for convergence
    if (totalChange < tolerance) {
      break
    }
  }
  
  // Normalize scores to 0-100 range
  const maxScore = Math.max(...Array.from(scores.values()))
  const minScore = Math.min(...Array.from(scores.values()))
  const range = maxScore - minScore || 1
  
  const normalizedScores = new Map()
  scores.forEach((score, nodeId) => {
    const normalized = ((score - minScore) / range) * 100
    normalizedScores.set(nodeId, Math.round(normalized))
  })
  
  return normalizedScores
}

/**
 * Determine edge weight based on component types and connections
 */
function getEdgeWeight(edge, components) {
  const fromComponent = components.find(c => c.id === edge.from)
  const toComponent = components.find(c => c.id === edge.to)
  
  if (!fromComponent || !toComponent) {
    return EDGE_WEIGHTS.default
  }
  
  const fromType = fromComponent.type
  const toType = toComponent.type
  
  // Determine edge type and return weight
  if (fromType === 'endpoint' && toType === 'service') {
    return EDGE_WEIGHTS.endpoint_to_service
  }
  if (fromType === 'service' && toType === 'repository') {
    return EDGE_WEIGHTS.service_to_repository
  }
  if (toType === 'database') {
    return EDGE_WEIGHTS.method_to_database
  }
  if (toType === 'external') {
    return EDGE_WEIGHTS.method_to_external
  }
  if (toType === 'log') {
    return EDGE_WEIGHTS.method_to_log
  }
  if (toType === 'dataflow') {
    return EDGE_WEIGHTS.method_to_dataflow
  }
  if (toType === 'exception') {
    return EDGE_WEIGHTS.method_to_exception
  }
  
  return EDGE_WEIGHTS.default
}

/**
 * Calculate connection count for a component
 */
export function calculateConnectionCount(component) {
  if (!component || !component.connections) {
    return 0
  }
  
  const conn = component.connections
  return (
    (conn.logs?.length || 0) +
    (conn.dataFlows?.length || 0) +
    (conn.databaseOps?.length || 0) +
    (conn.externalCalls?.length || 0) +
    (conn.exceptions?.length || 0) +
    (conn.connectedMethods?.length || 0)
  )
}

/**
 * Rank components by centrality and connection count
 */
export function rankComponents(components, centralityScores) {
  return components.map(comp => {
    const centralityScore = centralityScores.get(comp.id) || 0
    const connectionCount = calculateConnectionCount(comp)
    
    // Combined score: 70% centrality + 30% connection count
    const connectionScore = Math.min(100, connectionCount * 5) // Scale connections
    const combinedScore = (centralityScore * 0.7) + (connectionScore * 0.3)
    
    return {
      ...comp,
      centralityScore,
      connectionCount,
      combinedScore: Math.round(combinedScore)
    }
  }).sort((a, b) => b.combinedScore - a.combinedScore)
}

/**
 * Calculate confidence level based on match score
 */
export function calculateConfidence(score) {
  if (score >= 80) return { level: 'HIGH', percentage: score }
  if (score >= 50) return { level: 'MEDIUM', percentage: score }
  return { level: 'LOW', percentage: score }
}

/**
 * Build summary statistics for search results
 */
export function buildSearchSummary(results) {
  const summary = {
    totalComponents: results.components?.length || 0,
    totalLogs: results.logs?.length || 0,
    totalDataFlows: results.dataFlows?.length || 0,
    totalDatabaseOps: 0,
    totalExternalCalls: 0,
    totalExceptions: 0,
    averageCentrality: 0,
    highConfidenceMatches: 0,
    matchTypes: new Set()
  }
  
  // Count totals from components
  if (results.components) {
    results.components.forEach(comp => {
      if (comp.connections) {
        summary.totalDatabaseOps += comp.connections.databaseOps?.length || 0
        summary.totalExternalCalls += comp.connections.externalCalls?.length || 0
        summary.totalExceptions += comp.connections.exceptions?.length || 0
      }
      
      if (comp.combinedScore >= 80) {
        summary.highConfidenceMatches++
      }
      
      summary.matchTypes.add(comp.type)
    })
    
    // Calculate average centrality
    const totalCentrality = results.components.reduce((sum, c) => 
      sum + (c.centralityScore || 0), 0
    )
    summary.averageCentrality = results.components.length > 0 
      ? Math.round(totalCentrality / results.components.length)
      : 0
  }
  
  summary.matchTypes = Array.from(summary.matchTypes)
  
  return summary
}
