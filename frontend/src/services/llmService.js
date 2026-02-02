/**
 * LLM Service for code analysis
 * Supports OpenAI-compatible endpoints
 */

export class LLMService {
  /**
   * Analyze error with LLM
   */
  async analyzeError(context, config) {
    const prompt = this.buildErrorAnalysisPrompt(context, config)
    
    try {
      const response = await this.callLLM(prompt, config)
      return this.parseAnalysisResponse(response, context)
    } catch (error) {
      console.error('LLM analysis error:', error)
      throw new Error(`Failed to analyze: ${error.message}`)
    }
  }

  /**
   * Chat with LLM about selected code
   */
  async chat(message, history, context, config) {
    const messages = this.buildChatMessages(message, history, context, config)
    
    try {
      const response = await this.callLLM(messages, config, true)
      return response
    } catch (error) {
      console.error('LLM chat error:', error)
      throw new Error(`Chat failed: ${error.message}`)
    }
  }

  /**
   * Generate annotation for a node
   */
  async generateAnnotation(node, config) {
    const prompt = this.buildAnnotationPrompt(node, config)
    
    try {
      const response = await this.callLLM(prompt, config)
      return response.content
    } catch (error) {
      console.error('Annotation generation error:', error)
      throw new Error(`Failed to generate annotation: ${error.message}`)
    }
  }

  /**
   * Build error analysis prompt
   */
  buildErrorAnalysisPrompt(context, config) {
    const { selectedNodes, errorMessage, additionalContext } = context
    
    // Build call path
    const callPath = this.buildCallPath(selectedNodes)
    
    // Build component details
    const componentDetails = this.buildComponentDetails(selectedNodes)
    
    // Replace template variables
    let userPrompt = config.userPromptTemplate
      .replace('{errorMessage}', errorMessage || 'Not provided')
      .replace('{callPath}', callPath)
      .replace('{componentDetails}', componentDetails)
    
    if (additionalContext) {
      userPrompt += `\n\nAdditional Context:\n${additionalContext}`
    }
    
    return [
      { role: 'system', content: config.systemPrompt },
      { role: 'user', content: userPrompt }
    ]
  }

  /**
   * Build chat messages array
   */
  buildChatMessages(message, history, context, config) {
    const messages = [
      { role: 'system', content: config.systemPrompt }
    ]
    
    // Add context as initial message if new session
    if (history.length === 0 && context.selectedNodes) {
      const contextMsg = `I'm analyzing this code flow:\n\n${this.buildCallPath(context.selectedNodes)}\n\nComponents:\n${this.buildComponentDetails(context.selectedNodes)}`
      messages.push({ role: 'system', content: contextMsg })
    }
    
    // Add history
    history.forEach(msg => {
      messages.push({ role: msg.role, content: msg.content })
    })
    
    // Add new message
    messages.push({ role: 'user', content: message })
    
    return messages
  }

  /**
   * Build annotation prompt
   */
  buildAnnotationPrompt(node, config) {
    let description = ''
    
    if (node.type === 'endpoint') {
      description = `Endpoint: ${node.httpMethod} ${node.path}\nController: ${node.controllerClass}\nMethod: ${node.methodName}\nReturn: ${node.returnType}`
    } else if (node.type === 'service') {
      description = `Service: ${node.className}\nMethod: ${node.methodName}\nReturn: ${node.returnType}`
    } else {
      description = `Repository: ${node.className}\nMethod: ${node.methodName}\nReturn: ${node.returnType}`
    }
    
    return [
      { role: 'system', content: 'You are a code documentation assistant. Provide brief, clear descriptions.' },
      { role: 'user', content: `Generate a brief summary annotation for this component:\n\n${description}` }
    ]
  }

  /**
   * Build call path string
   */
  buildCallPath(selectedNodes) {
    const endpoints = selectedNodes.filter(n => n.type === 'endpoint')
    const services = selectedNodes.filter(n => n.type === 'service')
    const repos = selectedNodes.filter(n => n.type === 'repository')
    
    const parts = []
    
    if (endpoints.length > 0) {
      parts.push(endpoints.map(e => `${e.httpMethod} ${e.path}`).join(', '))
    }
    
    if (services.length > 0) {
      parts.push(services.map(s => `${s.className}.${s.methodName}()`).join(', '))
    }
    
    if (repos.length > 0) {
      parts.push(repos.map(r => `${r.className}.${r.methodName}()`).join(', '))
    }
    
    return parts.join(' → ')
  }

  /**
   * Build detailed component information
   */
  buildComponentDetails(selectedNodes) {
    return selectedNodes.map(node => {
      let detail = `### ${node.type.toUpperCase()}: ${node.id}\n`
      
      if (node.type === 'endpoint') {
        detail += `- HTTP: ${node.httpMethod} ${node.path}\n`
        detail += `- Controller: ${node.controllerClass}\n`
        detail += `- Method: ${node.methodName}\n`
        detail += `- Returns: ${node.returnType}\n`
        
        if (node.parameters && node.parameters.length > 0) {
          detail += `- Parameters:\n`
          node.parameters.forEach(p => {
            detail += `  - ${p.annotation || ''} ${p.type} ${p.name}\n`
          })
        }
        
        if (node.callChain && node.callChain.length > 0) {
          detail += `- Calls: ${node.callChain.join(', ')}\n`
        }
      } else if (node.type === 'service') {
        detail += `- Class: ${node.className}\n`
        detail += `- Method: ${node.methodName}\n`
        detail += `- Returns: ${node.returnType}\n`
        
        if (node.parameters && node.parameters.length > 0) {
          detail += `- Parameters: ${node.parameters.join(', ')}\n`
        }
        
        if (node.calls && node.calls.length > 0) {
          detail += `- Calls: ${node.calls.join(', ')}\n`
        }
      } else {
        detail += `- Class: ${node.className}\n`
        detail += `- Method: ${node.methodName}\n`
        detail += `- Returns: ${node.returnType}\n`
      }
      
      if (node.javadoc) {
        detail += `- Documentation: ${node.javadoc}\n`
      }
      
      return detail
    }).join('\n')
  }

  /**
   * Call LLM API
   */
  async callLLM(messages, config, isChat = false) {
    if (!config.apiKey) {
      throw new Error('API key not configured')
    }
    
    const requestBody = {
      model: config.model,
      messages: Array.isArray(messages) ? messages : [messages],
      temperature: config.temperature,
      max_tokens: config.maxTokens
    }
    
    const response = await fetch(config.endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${config.apiKey}`
      },
      body: JSON.stringify(requestBody)
    })
    
    if (!response.ok) {
      const error = await response.json().catch(() => ({ error: 'Unknown error' }))
      throw new Error(error.error?.message || `HTTP ${response.status}`)
    }
    
    const data = await response.json()
    
    if (!data.choices || !data.choices[0]) {
      throw new Error('Invalid response format')
    }
    
    return {
      content: data.choices[0].message.content,
      usage: data.usage
    }
  }

  /**
   * Parse analysis response into structured format
   */
  parseAnalysisResponse(response, context) {
    const content = response.content
    
    // Try to extract structured information
    const result = {
      rawResponse: content,
      rootCause: this.extractSection(content, ['root cause', 'cause analysis', '1.']),
      problematicNode: this.extractProblematicNode(content, context.selectedNodes),
      suggestedFix: this.extractSection(content, ['suggested fix', 'recommended fix', 'fix', '3.']),
      preventionTips: this.extractSection(content, ['prevention', 'recommendations', '4.']),
      confidence: this.estimateConfidence(content),
      usage: response.usage
    }
    
    return result
  }

  /**
   * Extract section from text
   */
  extractSection(text, keywords) {
    for (const keyword of keywords) {
      const regex = new RegExp(`(?:${keyword})[:\\-]?\\s*([\\s\\S]*?)(?:\\n\\n|###|$)`, 'i')
      const match = text.match(regex)
      if (match) {
        return match[1].trim()
      }
    }
    return null
  }

  /**
   * Try to identify problematic node from response
   */
  extractProblematicNode(text, selectedNodes) {
    for (const node of selectedNodes) {
      if (node.className && text.includes(node.className)) {
        return node.id
      }
      if (node.methodName && text.includes(node.methodName)) {
        return node.id
      }
    }
    return null
  }

  /**
   * Estimate confidence from response
   */
  estimateConfidence(text) {
    const certaintyWords = ['definitely', 'certainly', 'clearly', 'obviously']
    const uncertaintyWords = ['might', 'possibly', 'perhaps', 'maybe', 'could be']
    
    const lowerText = text.toLowerCase()
    const certainCount = certaintyWords.filter(w => lowerText.includes(w)).length
    const uncertainCount = uncertaintyWords.filter(w => lowerText.includes(w)).length
    
    if (certainCount > uncertainCount) return 0.8
    if (uncertainCount > certainCount) return 0.5
    return 0.7
  }
}

export default new LLMService()
