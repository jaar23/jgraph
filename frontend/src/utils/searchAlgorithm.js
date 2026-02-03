/**
 * Smart search algorithm for tracing components from logs, variables, or JSON
 */

/**
 * Detect the type of input the user provided
 */
export function detectInputType(text) {
  if (!text || !text.trim()) return null
  
  const trimmed = text.trim()
  
  // Check if it's valid JSON
  if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
    try {
      JSON.parse(trimmed)
      return 'JSON_PAYLOAD'
    } catch (e) {
      // Not valid JSON, continue
    }
  }
  
  // Check if it looks like a log message
  const logPatterns = [
    /\{\}/g,  // {} placeholders
    /(error|warn|info|debug|trace)[:|\s]/i,  // Log level keywords
    /(exception|failed|success|completed|started)/i,  // Common log words
  ]
  
  const hasLogPattern = logPatterns.some(pattern => pattern.test(trimmed))
  if (hasLogPattern || trimmed.split('\n').length === 1) {
    return 'LOG_MESSAGE'
  }
  
  // Check if it's a simple variable name (single word, camelCase or snake_case)
  if (/^[a-zA-Z_$][a-zA-Z0-9_$]*$/.test(trimmed)) {
    return 'VARIABLE_NAME'
  }
  
  return 'FREE_TEXT'
}

/**
 * Parse log message and extract keywords and variables
 */
export function parseLogMessage(text) {
  const trimmed = text.trim()
  
  // Extract placeholders like {}, {0}, {1}, etc.
  const placeholders = (trimmed.match(/\{[^}]*\}/g) || [])
  
  // Remove placeholders and split into words
  let cleaned = trimmed.replace(/\{[^}]*\}/g, ' ')
  
  // Remove special characters but keep spaces
  cleaned = cleaned.replace(/[^\w\s]/g, ' ')
  
  // Extract keywords (words longer than 2 chars, excluding common words)
  const stopWords = ['the', 'and', 'with', 'for', 'from', 'this', 'that', 'are', 'was']
  const words = cleaned.toLowerCase().split(/\s+/)
    .filter(word => word.length > 2 && !stopWords.includes(word))
  
  // Remove duplicates
  const keywords = [...new Set(words)]
  
  return {
    original: text,
    keywords,
    placeholders,
    wordCount: words.length
  }
}

/**
 * Parse JSON payload and extract field paths and values
 */
export function parseJSON(text) {
  try {
    const obj = JSON.parse(text)
    const fields = []
    const values = []
    
    function traverse(obj, path = '') {
      if (obj === null || obj === undefined) return
      
      if (typeof obj === 'object' && !Array.isArray(obj)) {
        Object.keys(obj).forEach(key => {
          const newPath = path ? `${path}.${key}` : key
          fields.push(newPath)
          
          const value = obj[key]
          if (typeof value !== 'object') {
            values.push({ field: newPath, value })
          }
          
          traverse(value, newPath)
        })
      } else if (Array.isArray(obj)) {
        obj.forEach((item, idx) => {
          traverse(item, `${path}[${idx}]`)
        })
      }
    }
    
    traverse(obj)
    
    return {
      original: text,
      fields: [...new Set(fields)],
      values,
      topLevelFields: Object.keys(obj)
    }
  } catch (e) {
    return null
  }
}

/**
 * Calculate similarity between two strings using Levenshtein distance
 */
function levenshteinDistance(str1, str2) {
  const matrix = []
  
  for (let i = 0; i <= str2.length; i++) {
    matrix[i] = [i]
  }
  
  for (let j = 0; j <= str1.length; j++) {
    matrix[0][j] = j
  }
  
  for (let i = 1; i <= str2.length; i++) {
    for (let j = 1; j <= str1.length; j++) {
      if (str2.charAt(i - 1) === str1.charAt(j - 1)) {
        matrix[i][j] = matrix[i - 1][j - 1]
      } else {
        matrix[i][j] = Math.min(
          matrix[i - 1][j - 1] + 1,
          matrix[i][j - 1] + 1,
          matrix[i - 1][j] + 1
        )
      }
    }
  }
  
  return matrix[str2.length][str1.length]
}

/**
 * Calculate similarity score (0-100) between two strings
 */
function calculateSimilarity(str1, str2) {
  const maxLen = Math.max(str1.length, str2.length)
  if (maxLen === 0) return 100
  
  const distance = levenshteinDistance(str1.toLowerCase(), str2.toLowerCase())
  return Math.round(((maxLen - distance) / maxLen) * 100)
}

/**
 * Fuzzy match logs based on keywords
 */
export function fuzzyMatchLogs(parsedInput, logs) {
  if (!parsedInput || !logs) return []
  
  const matches = []
  
  logs.forEach(log => {
    let score = 0
    const reasons = []
    
    // Exact message match
    if (log.message && parsedInput.original) {
      const similarity = calculateSimilarity(log.message, parsedInput.original)
      if (similarity >= 80) {
        score += similarity
        reasons.push(`Message similarity: ${similarity}%`)
      }
    }
    
    // Keyword matching
    if (parsedInput.keywords && parsedInput.keywords.length > 0) {
      const logText = (log.message || '').toLowerCase()
      const matchedKeywords = parsedInput.keywords.filter(kw => 
        logText.includes(kw.toLowerCase())
      )
      
      if (matchedKeywords.length > 0) {
        const keywordScore = (matchedKeywords.length / parsedInput.keywords.length) * 80
        score += keywordScore
        reasons.push(`Matched ${matchedKeywords.length}/${parsedInput.keywords.length} keywords`)
      }
    }
    
    // Variable matching
    if (log.variables && log.variables.length > 0) {
      const varMatches = log.variables.some(v => 
        parsedInput.keywords?.some(kw => 
          v.toLowerCase().includes(kw.toLowerCase())
        )
      )
      if (varMatches) {
        score += 20
        reasons.push('Variable match')
      }
    }
    
    if (score > 0) {
      matches.push({
        log,
        score: Math.min(100, score),
        reasons,
        confidence: score >= 80 ? 'HIGH' : score >= 50 ? 'MEDIUM' : 'LOW'
      })
    }
  })
  
  // Sort by score descending
  return matches.sort((a, b) => b.score - a.score)
}

/**
 * Find variable usage in dataflows
 */
export function findVariableInDataFlows(varName, dataFlows) {
  if (!varName || !dataFlows) return []
  
  const matches = []
  const lowerVarName = varName.toLowerCase()
  
  dataFlows.forEach(dataFlow => {
    let score = 0
    const locations = []
    
    // Check parameters
    if (dataFlow.parameters) {
      dataFlow.parameters.forEach(param => {
        if (param.name && param.name.toLowerCase() === lowerVarName) {
          score += 30
          locations.push({type: 'parameter', name: param.name, dataType: param.type})
        } else if (param.name && param.name.toLowerCase().includes(lowerVarName)) {
          score += 15
          locations.push({type: 'parameter', name: param.name, dataType: param.type})
        }
      })
    }
    
    // Check variables/transformations
    if (dataFlow.variables) {
      dataFlow.variables.forEach(variable => {
        if (variable.variableName && variable.variableName.toLowerCase() === lowerVarName) {
          score += 30
          locations.push({type: 'variable', name: variable.variableName, transformType: variable.transformationType})
        } else if (variable.variableName && variable.variableName.toLowerCase().includes(lowerVarName)) {
          score += 15
          locations.push({type: 'variable', name: variable.variableName, transformType: variable.transformationType})
        }
        
        if (variable.sourceVariable && variable.sourceVariable.toLowerCase().includes(lowerVarName)) {
          score += 10
          locations.push({type: 'source', name: variable.sourceVariable})
        }
      })
    }
    
    // Check returns
    if (dataFlow.returns) {
      dataFlow.returns.forEach(ret => {
        if (ret.sourceVariable && ret.sourceVariable.toLowerCase().includes(lowerVarName)) {
          score += 20
          locations.push({type: 'return', name: ret.sourceVariable, returnType: ret.returnType})
        }
      })
    }
    
    // Check data transfers
    if (dataFlow.dataTransfers) {
      dataFlow.dataTransfers.forEach(transfer => {
        if (transfer.arguments) {
          transfer.arguments.forEach(arg => {
            if (arg.variableName && arg.variableName.toLowerCase().includes(lowerVarName)) {
              score += 15
              locations.push({type: 'argument', name: arg.variableName, method: transfer.targetMethod})
            }
          })
        }
      })
    }
    
    if (score > 0) {
      matches.push({
        dataFlow,
        score: Math.min(100, score),
        locations,
        confidence: score >= 60 ? 'HIGH' : score >= 30 ? 'MEDIUM' : 'LOW'
      })
    }
  })
  
  return matches.sort((a, b) => b.score - a.score)
}

/**
 * Find JSON fields in parameters, variables, and models
 */
export function findJSONFieldsInComponents(parsedJSON, analysisData) {
  if (!parsedJSON || !analysisData) return []
  
  const matches = {
    endpoints: [],
    services: [],
    repositories: [],
    dataFlows: []
  }
  
  const fields = parsedJSON.fields || []
  
  // Search in endpoints
  if (analysisData.endpoints) {
    analysisData.endpoints.forEach(endpoint => {
      let score = 0
      const matchedFields = []
      
      if (endpoint.parameters) {
        endpoint.parameters.forEach(param => {
          fields.forEach(field => {
            if (param.type && param.type.toLowerCase().includes(field.toLowerCase())) {
              score += 20
              matchedFields.push(field)
            }
            if (param.name && param.name.toLowerCase() === field.toLowerCase()) {
              score += 30
              matchedFields.push(field)
            }
          })
        })
      }
      
      if (score > 0) {
        matches.endpoints.push({ endpoint, score, matchedFields })
      }
    })
  }
  
  // Search in dataflows
  if (analysisData.dataFlows) {
    analysisData.dataFlows.forEach(dataFlow => {
      let score = 0
      const matchedFields = []
      
      if (dataFlow.parameters) {
        dataFlow.parameters.forEach(param => {
          fields.forEach(field => {
            if (param.name && param.name.toLowerCase() === field.toLowerCase()) {
              score += 25
              matchedFields.push(field)
            }
          })
        })
      }
      
      if (dataFlow.variables) {
        dataFlow.variables.forEach(variable => {
          fields.forEach(field => {
            if (variable.variableName && variable.variableName.toLowerCase().includes(field.toLowerCase())) {
              score += 15
              matchedFields.push(field)
            }
          })
        })
      }
      
      if (score > 0) {
        matches.dataFlows.push({ dataFlow, score, matchedFields })
      }
    })
  }
  
  return matches
}

/**
 * Build a comprehensive component graph showing all relationships
 */
export function buildComponentGraph(matches, analysisData) {
  const componentMap = new Map()
  
  // Helper to add component
  function addComponent(id, type, data) {
    if (!componentMap.has(id)) {
      componentMap.set(id, {
        id,
        type,
        data,
        connections: {
          logs: [],
          dataFlows: [],
          databaseOps: [],
          externalCalls: [],
          exceptions: [],
          connectedMethods: []
        }
      })
    }
    return componentMap.get(id)
  }
  
  // Add matched logs and their parent methods
  if (matches.logs) {
    matches.logs.forEach(match => {
      const log = match.log
      const methodId = log.methodId
      
      // Find parent method
      const endpoint = analysisData.endpoints?.find(e => e.id === methodId)
      const service = analysisData.services?.find(s => s.id === methodId)
      const repository = analysisData.repositories?.find(r => r.id === methodId)
      
      if (endpoint) {
        const comp = addComponent(endpoint.id, 'endpoint', endpoint)
        comp.connections.logs.push(log)
      }
      if (service) {
        const comp = addComponent(service.id, 'service', service)
        comp.connections.logs.push(log)
      }
      if (repository) {
        const comp = addComponent(repository.id, 'repository', repository)
        comp.connections.logs.push(log)
      }
    })
  }
  
  // Add matched dataflows and their parent methods
  if (matches.dataFlows) {
    matches.dataFlows.forEach(match => {
      const dataFlow = match.dataFlow
      const methodId = dataFlow.methodId
      
      const endpoint = analysisData.endpoints?.find(e => e.id === methodId)
      const service = analysisData.services?.find(s => s.id === methodId)
      const repository = analysisData.repositories?.find(r => r.id === methodId)
      
      if (endpoint) {
        const comp = addComponent(endpoint.id, 'endpoint', endpoint)
        comp.connections.dataFlows.push(dataFlow)
      }
      if (service) {
        const comp = addComponent(service.id, 'service', service)
        comp.connections.dataFlows.push(dataFlow)
      }
      if (repository) {
        const comp = addComponent(repository.id, 'repository', repository)
        comp.connections.dataFlows.push(dataFlow)
      }
    })
  }
  
  // Enhance each component with related database ops, external calls, etc.
  componentMap.forEach(comp => {
    const className = comp.data.className || comp.data.controllerClass
    const methodName = comp.data.methodName
    
    // Add database operations
    if (analysisData.databaseOperations) {
      const dbOps = analysisData.databaseOperations.filter(op => 
        op.className === className && op.methodName === methodName
      )
      comp.connections.databaseOps.push(...dbOps)
    }
    
    // Add external calls
    if (analysisData.externalCalls) {
      const extCalls = analysisData.externalCalls.filter(call =>
        call.className === className && call.callingMethod === methodName
      )
      comp.connections.externalCalls.push(...extCalls)
    }
    
    // Add exceptions
    if (analysisData.tryCatchBlocks) {
      const exceptions = analysisData.tryCatchBlocks.filter(ex =>
        ex.className === className && ex.methodName === methodName
      )
      comp.connections.exceptions.push(...exceptions)
    }
  })
  
  // Build call relationships using callGraph
  if (analysisData.callGraph) {
    componentMap.forEach(comp => {
      const edges = analysisData.callGraph.edges.filter(e => 
        e.from === comp.id || e.to === comp.id
      )
      
      edges.forEach(edge => {
        if (edge.from === comp.id) {
          comp.connections.connectedMethods.push({
            direction: 'downstream',
            methodId: edge.to,
            edgeType: edge.type
          })
        } else {
          comp.connections.connectedMethods.push({
            direction: 'upstream',
            methodId: edge.from,
            edgeType: edge.type
          })
        }
      })
    })
  }
  
  return Array.from(componentMap.values())
}
