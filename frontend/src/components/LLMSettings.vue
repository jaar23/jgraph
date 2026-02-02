<template>
  <div v-if="show" class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>LLM Configuration</h2>
        <button @click="closeModal" class="close-btn">×</button>
      </div>

      <div class="modal-body">
        <div class="form-group">
          <label>API Endpoint</label>
          <input
            v-model="config.endpoint"
            type="text"
            placeholder="https://api.openai.com/v1/chat/completions"
          />
          <small>OpenAI-compatible endpoint (OpenAI, LocalAI, Ollama, etc.)</small>
        </div>

        <div class="form-group">
          <label>Model Name</label>
          <input
            v-model="config.model"
            type="text"
            placeholder="gpt-4-turbo-preview"
          />
          <small>Model to use for analysis</small>
        </div>

        <div class="form-group">
          <label>API Key</label>
          <input
            v-model="config.apiKey"
            type="password"
            placeholder="sk-..."
          />
          <small>Your API key (stored locally, never sent to server)</small>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Temperature</label>
            <input
              v-model.number="config.temperature"
              type="number"
              min="0"
              max="2"
              step="0.1"
            />
          </div>

          <div class="form-group">
            <label>Max Tokens</label>
            <input
              v-model.number="config.maxTokens"
              type="number"
              min="100"
              max="4000"
              step="100"
            />
          </div>
        </div>

        <div class="form-group">
          <label>System Prompt</label>
          <textarea
            v-model="config.systemPrompt"
            rows="6"
            placeholder="You are an expert Java developer..."
          ></textarea>
        </div>

        <div class="form-group">
          <label>User Prompt Template</label>
          <textarea
            v-model="config.userPromptTemplate"
            rows="8"
            placeholder="Use {errorMessage}, {callPath}, {componentDetails} as variables"
          ></textarea>
          <small>Available variables: {errorMessage}, {callPath}, {componentDetails}</small>
        </div>

        <div class="test-section">
          <button @click="testConnection" class="test-btn" :disabled="testing">
            {{ testing ? 'Testing...' : 'Test Connection' }}
          </button>
          <span v-if="testResult" class="test-result" :class="testResult.success ? 'success' : 'error'">
            {{ testResult.message }}
          </span>
        </div>
      </div>

      <div class="modal-footer">
        <button @click="reset" class="reset-btn">Reset to Defaults</button>
        <div class="footer-actions">
          <button @click="closeModal" class="cancel-btn">Cancel</button>
          <button @click="save" class="save-btn">Save</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useAnalysisStore } from '../stores/analysisStore'
import llmService from '../services/llmService'

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['close'])

const store = useAnalysisStore()
const config = ref({ ...store.llmConfig })
const testing = ref(false)
const testResult = ref(null)

watch(() => props.show, (show) => {
  if (show) {
    config.value = { ...store.llmConfig }
    testResult.value = null
  }
})

function closeModal() {
  emit('close')
}

function save() {
  store.updateLLMConfig(config.value)
  emit('close')
}

function reset() {
  config.value = {
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
}

async function testConnection() {
  testing.value = true
  testResult.value = null

  try {
    const response = await llmService.callLLM(
      [{ role: 'user', content: 'Test connection. Reply with OK.' }],
      config.value
    )

    testResult.value = {
      success: true,
      message: `✓ Connected! Model: ${config.value.model}`
    }
  } catch (error) {
    testResult.value = {
      success: false,
      message: `✗ Failed: ${error.message}`
    }
  } finally {
    testing.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 2rem;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #999;
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
  font-family: inherit;
}

.form-group textarea {
  font-family: 'Courier New', monospace;
  resize: vertical;
}

.form-group small {
  display: block;
  margin-top: 0.25rem;
  color: #666;
  font-size: 0.85rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.test-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.test-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
}

.test-btn:hover:not(:disabled) {
  background-color: #2980b9;
}

.test-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.test-result {
  font-size: 0.9rem;
  padding: 0.5rem 1rem;
  border-radius: 4px;
}

.test-result.success {
  background-color: #d5f4e6;
  color: #27ae60;
}

.test-result.error {
  background-color: #fadbd8;
  color: #e74c3c;
}

.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-top: 1px solid #e0e0e0;
}

.footer-actions {
  display: flex;
  gap: 0.75rem;
}

.reset-btn,
.cancel-btn,
.save-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
}

.reset-btn {
  background-color: #f8f9fa;
  color: #666;
}

.reset-btn:hover {
  background-color: #e8e8e8;
}

.cancel-btn {
  background-color: #95a5a6;
  color: white;
}

.cancel-btn:hover {
  background-color: #7f8c8d;
}

.save-btn {
  background-color: #2ecc71;
  color: white;
}

.save-btn:hover {
  background-color: #27ae60;
}
</style>
