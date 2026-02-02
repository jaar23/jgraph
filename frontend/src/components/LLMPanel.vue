<template>
  <div class="llm-panel">
    <div class="panel-header">
      <h3>AI Analysis</h3>
      <button @click="$emit('toggle')" class="collapse-btn">{{ collapsed ? '◀' : '▶' }}</button>
    </div>

    <div v-if="!collapsed" class="panel-content">
      <!-- Selected Context -->
      <div v-if="store.selectedNodes.length > 0" class="context-section">
        <div class="context-header">
          <span class="context-label">Selected Context:</span>
          <span class="context-count">{{ store.selectedNodes.length }} nodes</span>
        </div>
        <div class="context-items">
          <div v-for="nodeId in store.selectedNodes" :key="nodeId" class="context-item">
            {{ getNodeLabel(nodeId) }}
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="tabs">
        <button
          :class="{ active: activeTab === 'analyze' }"
          @click="activeTab = 'analyze'"
        >
          🔍 Analyze Error
        </button>
        <button
          :class="{ active: activeTab === 'chat' }"
          @click="activeTab = 'chat'"
        >
          💬 Chat
        </button>
        <button
          :class="{ active: activeTab === 'history' }"
          @click="activeTab = 'history'"
        >
          📜 History
        </button>
      </div>

      <!-- Error Analysis Tab -->
      <div v-if="activeTab === 'analyze'" class="tab-content">
        <div class="form-group">
          <label>Error Message / Stack Trace:</label>
          <textarea
            v-model="errorInput"
            placeholder="Paste your error message or stack trace here..."
            rows="6"
          ></textarea>
        </div>

        <div class="form-group">
          <label>Additional Context (optional):</label>
          <textarea
            v-model="additionalContext"
            placeholder="Any additional information about the issue..."
            rows="3"
          ></textarea>
        </div>

        <button
          @click="analyzeError"
          class="analyze-btn"
          :disabled="analyzing || !errorInput || store.selectedNodes.length === 0 || !store.llmConfig.apiKey"
        >
          {{ analyzing ? 'Analyzing...' : '🔍 Analyze with AI' }}
        </button>

        <div v-if="!store.llmConfig.apiKey" class="warning-box">
          ⚠️ Please configure your API key in settings
        </div>

        <div v-if="store.selectedNodes.length === 0" class="info-box">
          ℹ️ Select one or more nodes to analyze
        </div>

        <!-- Analysis Results -->
        <div v-if="currentAnalysis" class="analysis-results">
          <div class="result-section">
            <h4>🎯 Root Cause</h4>
            <div class="result-content" v-html="renderMarkdown(currentAnalysis.rootCause)"></div>
          </div>

          <div v-if="currentAnalysis.problematicNode" class="result-section">
            <h4>⚠️ Problematic Component</h4>
            <div class="result-content">
              <button @click="highlightNode(currentAnalysis.problematicNode)" class="highlight-btn">
                {{ getNodeLabel(currentAnalysis.problematicNode) }}
              </button>
            </div>
          </div>

          <div class="result-section">
            <h4>💡 Suggested Fix</h4>
            <div class="result-content" v-html="renderMarkdown(currentAnalysis.suggestedFix)"></div>
          </div>

          <div v-if="currentAnalysis.preventionTips" class="result-section">
            <h4>🛡️ Prevention Tips</h4>
            <div class="result-content" v-html="renderMarkdown(currentAnalysis.preventionTips)"></div>
          </div>

          <div class="result-footer">
            <span class="confidence">Confidence: {{ (currentAnalysis.confidence * 100).toFixed(0) }}%</span>
            <button @click="saveAnalysisAsAnnotation" class="save-annotation-btn">
              Save as Annotation
            </button>
          </div>
        </div>
      </div>

      <!-- Chat Tab -->
      <div v-if="activeTab === 'chat'" class="tab-content">
        <div class="chat-container">
          <div class="chat-messages" ref="chatMessages">
            <div v-for="(msg, index) in currentChatMessages" :key="index" class="chat-message" :class="msg.role">
              <div class="message-header">
                <span class="message-role">{{ msg.role === 'user' ? 'You' : 'AI' }}</span>
                <span class="message-time">{{ formatTime(msg.timestamp) }}</span>
              </div>
              <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
            </div>
          </div>

          <div class="chat-input-area">
            <textarea
              v-model="chatInput"
              placeholder="Ask a question about the selected code flow..."
              rows="3"
              @keydown.enter.ctrl="sendChat"
            ></textarea>
            <button
              @click="sendChat"
              class="send-btn"
              :disabled="!chatInput || chatting || store.selectedNodes.length === 0"
            >
              {{ chatting ? 'Sending...' : 'Send (Ctrl+Enter)' }}
            </button>
          </div>

          <div v-if="store.selectedNodes.length === 0" class="info-box">
            ℹ️ Select nodes to provide context for the chat
          </div>
        </div>
      </div>

      <!-- History Tab -->
      <div v-if="activeTab === 'history'" class="tab-content">
        <div v-if="store.llmAnalyses.length === 0" class="empty-state">
          No analysis history yet
        </div>

        <div v-else class="history-list">
          <div
            v-for="analysis in store.llmAnalyses"
            :key="analysis.id"
            class="history-item"
            @click="viewAnalysis(analysis)"
          >
            <div class="history-header">
              <span class="history-time">{{ formatDate(analysis.timestamp) }}</span>
              <span class="history-nodes">{{ analysis.selectedNodes.length }} nodes</span>
            </div>
            <div class="history-error">{{ truncate(analysis.errorMessage, 80) }}</div>
            <div class="history-cause">{{ truncate(analysis.rootCause, 100) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAnalysisStore } from '../stores/analysisStore'
import llmService from '../services/llmService'
import { marked } from 'marked'

const props = defineProps({
  collapsed: Boolean
})

const emit = defineEmits(['toggle'])

const store = useAnalysisStore()
const activeTab = ref('analyze')

// Error Analysis
const errorInput = ref('')
const additionalContext = ref('')
const analyzing = ref(false)
const currentAnalysis = ref(null)

// Chat
const chatInput = ref('')
const chatting = ref(false)
const chatMessages = ref(null)

const currentChatMessages = computed(() => {
  return store.currentChatSession?.messages || []
})

function getNodeLabel(nodeId) {
  const data = store.analysisData
  if (!data) return nodeId

  let found = data.endpoints?.find(e => e.id === nodeId)
  if (found) return `${found.httpMethod} ${found.path}`

  found = data.services?.find(s => s.id === nodeId)
  if (found) return `${found.className}.${found.methodName}`

  found = data.repositories?.find(r => r.id === nodeId)
  if (found) return `${found.className}.${found.methodName}`

  return nodeId
}

function getSelectedNodesData() {
  const data = store.analysisData
  const selected = []

  store.selectedNodes.forEach(nodeId => {
    let found = data.endpoints?.find(e => e.id === nodeId)
    if (found) {
      selected.push({ ...found, type: 'endpoint' })
      return
    }

    found = data.services?.find(s => s.id === nodeId)
    if (found) {
      selected.push({ ...found, type: 'service' })
      return
    }

    found = data.repositories?.find(r => r.id === nodeId)
    if (found) {
      selected.push({ ...found, type: 'repository' })
    }
  })

  return selected
}

async function analyzeError() {
  if (!errorInput.value || store.selectedNodes.length === 0) return

  analyzing.value = true
  currentAnalysis.value = null

  try {
    const context = {
      selectedNodes: getSelectedNodesData(),
      errorMessage: errorInput.value,
      additionalContext: additionalContext.value
    }

    const result = await llmService.analyzeError(context, store.llmConfig)

    currentAnalysis.value = result

    // Save to store
    store.addLLMAnalysis({
      selectedNodes: store.selectedNodes,
      errorMessage: errorInput.value,
      additionalContext: additionalContext.value,
      ...result
    })
  } catch (error) {
    alert('Analysis failed: ' + error.message)
  } finally {
    analyzing.value = false
  }
}

async function sendChat() {
  if (!chatInput.value || store.selectedNodes.length === 0) return

  chatting.value = true

  try {
    // Start session if not exists
    if (!store.currentChatSession) {
      store.startChatSession(store.selectedNodes)
    }

    // Add user message
    store.addChatMessage({
      role: 'user',
      content: chatInput.value
    })

    const userMessage = chatInput.value
    chatInput.value = ''

    // Get AI response
    const context = {
      selectedNodes: getSelectedNodesData()
    }

    const response = await llmService.chat(
      userMessage,
      store.currentChatSession.messages,
      context,
      store.llmConfig
    )

    // Add AI response
    store.addChatMessage({
      role: 'assistant',
      content: response.content
    })

    // Scroll to bottom
    setTimeout(() => {
      if (chatMessages.value) {
        chatMessages.value.scrollTop = chatMessages.value.scrollHeight
      }
    }, 100)
  } catch (error) {
    alert('Chat failed: ' + error.message)
  } finally {
    chatting.value = false
  }
}

function highlightNode(nodeId) {
  const data = store.analysisData
  let found = data.endpoints?.find(e => e.id === nodeId)
  if (found) found = { ...found, type: 'endpoint' }
  
  if (!found) {
    found = data.services?.find(s => s.id === nodeId)
    if (found) found = { ...found, type: 'service' }
  }
  
  if (!found) {
    found = data.repositories?.find(r => r.id === nodeId)
    if (found) found = { ...found, type: 'repository' }
  }

  if (found) {
    store.setSelectedNode(found)
    store.setSelectedNodes([nodeId])
  }
}

function saveAnalysisAsAnnotation() {
  if (!currentAnalysis.value) return

  const noteText = `## AI Analysis Result

**Root Cause:**
${currentAnalysis.value.rootCause}

**Suggested Fix:**
${currentAnalysis.value.suggestedFix}

**Prevention:**
${currentAnalysis.value.preventionTips || 'N/A'}`

  const targetNodeId = currentAnalysis.value.problematicNode || store.selectedNodes[0]

  store.addNodeAnnotation(targetNodeId, {
    note: noteText,
    severity: 'warning',
    tags: ['ai-analysis', 'error-investigation']
  })

  alert('Analysis saved as annotation!')
}

function viewAnalysis(analysis) {
  currentAnalysis.value = analysis
  activeTab.value = 'analyze'
}

function renderMarkdown(text) {
  if (!text) return ''
  try {
    return marked.parse(text)
  } catch (e) {
    return text
  }
}

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleString()
}

function formatTime(dateString) {
  const date = new Date(dateString)
  return date.toLocaleTimeString()
}

function truncate(text, length) {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}
</script>

<style scoped>
.llm-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: white;
  border-left: 1px solid #e0e0e0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
  background-color: #f8f9fa;
}

.panel-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #2c3e50;
}

.collapse-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.25rem;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.context-section {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.context-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.context-label {
  font-weight: 600;
  color: #666;
}

.context-count {
  color: #3498db;
  font-weight: 600;
}

.context-items {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.context-item {
  font-size: 0.85rem;
  color: #555;
  font-family: monospace;
  padding: 0.25rem 0.5rem;
  background-color: white;
  border-radius: 4px;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  border-bottom: 2px solid #e0e0e0;
}

.tabs button {
  background: none;
  border: none;
  padding: 0.75rem 1rem;
  cursor: pointer;
  font-size: 0.9rem;
  color: #666;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
}

.tabs button:hover {
  color: #2c3e50;
}

.tabs button.active {
  color: #3498db;
  border-bottom-color: #3498db;
  font-weight: 600;
}

.tab-content {
  animation: fadeIn 0.2s;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  color: #2c3e50;
}

.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  font-family: 'Courier New', monospace;
  resize: vertical;
}

.analyze-btn {
  width: 100%;
  background-color: #3498db;
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
}

.analyze-btn:hover:not(:disabled) {
  background-color: #2980b9;
}

.analyze-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.warning-box,
.info-box {
  padding: 0.75rem;
  border-radius: 6px;
  margin-top: 0.75rem;
  font-size: 0.875rem;
}

.warning-box {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.info-box {
  background-color: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.analysis-results {
  margin-top: 1.5rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.result-section {
  margin-bottom: 1.5rem;
}

.result-section h4 {
  margin: 0 0 0.75rem 0;
  color: #2c3e50;
  font-size: 1rem;
}

.result-content {
  background-color: white;
  padding: 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  line-height: 1.6;
}

.result-content :deep(code) {
  background-color: #f0f0f0;
  padding: 0.2rem 0.4rem;
  border-radius: 3px;
  font-size: 0.85rem;
}

.result-content :deep(pre) {
  background-color: #2c3e50;
  color: #ecf0f1;
  padding: 1rem;
  border-radius: 4px;
  overflow-x: auto;
}

.highlight-btn {
  background-color: #e8f5e9;
  color: #27ae60;
  border: 1px solid #27ae60;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-family: monospace;
}

.highlight-btn:hover {
  background-color: #d5f4e6;
}

.result-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #e0e0e0;
}

.confidence {
  color: #666;
  font-size: 0.875rem;
}

.save-annotation-btn {
  background-color: #2ecc71;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.save-annotation-btn:hover {
  background-color: #27ae60;
}

/* Chat Styles */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.chat-message {
  margin-bottom: 1rem;
  padding: 1rem;
  border-radius: 8px;
}

.chat-message.user {
  background-color: #e3f2fd;
  border-left: 3px solid #2196f3;
}

.chat-message.assistant {
  background-color: #f3e5f5;
  border-left: 3px solid #9c27b0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
}

.message-role {
  font-weight: 600;
  color: #2c3e50;
}

.message-time {
  color: #999;
}

.message-content {
  font-size: 0.9rem;
  line-height: 1.6;
}

.chat-input-area {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.chat-input-area textarea {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  resize: vertical;
}

.send-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
}

.send-btn:hover:not(:disabled) {
  background-color: #2980b9;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* History Styles */
.empty-state {
  text-align: center;
  padding: 3rem;
  color: #999;
  font-style: italic;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.history-item:hover {
  background-color: #e8e8e8;
  transform: translateX(4px);
}

.history-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: #666;
}

.history-nodes {
  color: #3498db;
  font-weight: 600;
}

.history-error {
  font-weight: 600;
  color: #e74c3c;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.history-cause {
  font-size: 0.875rem;
  color: #555;
  line-height: 1.4;
}
</style>
