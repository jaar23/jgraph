<template>
  <div class="graph-container">
    <VueFlow
      v-model:nodes="nodes"
      v-model:edges="edges"
      :default-zoom="1"
      :min-zoom="0.1"
      :max-zoom="4"
      @node-click="onNodeClick"
      @node-context-menu="onNodeContextMenu"
      @pane-click="onPaneClick"
      class="vue-flow-container"
    >
      <Background />
      <Controls />
      <MiniMap />
      
      <template #node-endpoint="{ data }">
        <EndpointNode :data="data" />
      </template>
      
      <template #node-service="{ data }">
        <ServiceNode :data="data" />
      </template>
      
      <template #node-repository="{ data }">
        <RepositoryNode :data="data" />
      </template>
    </VueFlow>
    
    <!-- Context menu -->
    <div
      v-if="contextMenu.show"
      class="context-menu"
      :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }"
    >
      <div class="context-menu-item" @click="addAnnotation">
        📝 Add Annotation
      </div>
      <div class="context-menu-item" @click="generateAnnotation">
        ✨ Generate Annotation (AI)
      </div>
      <div class="context-menu-item" @click="selectConnected">
        🔗 Select Connected Nodes
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import { useAnalysisStore } from '../stores/analysisStore'
import dagre from 'dagre'
import EndpointNode from './nodes/EndpointNode.vue'
import ServiceNode from './nodes/ServiceNode.vue'
import RepositoryNode from './nodes/RepositoryNode.vue'

const store = useAnalysisStore()
const { fitView } = useVueFlow()

const nodes = ref([])
const edges = ref([])
const contextMenu = ref({ show: false, x: 0, y: 0, nodeId: null })

// Watch for data changes
watch(() => store.filteredData || store.analysisData, (data) => {
  if (data) {
    updateGraph(data)
  }
}, { deep: true, immediate: true })

// Watch for selected nodes changes
watch(() => store.selectedNodes, (selected) => {
  // Update node selection state
  nodes.value.forEach(node => {
    node.selected = selected.includes(node.id)
  })
}, { deep: true })

function updateGraph(data) {
  if (!data || !data.callGraph) return
  
  const graphNodes = data.callGraph.nodes.map(node => ({
    id: node.id,
    type: node.type,
    position: { x: 0, y: 0 }, // Will be set by layout
    data: {
      ...node,
      label: node.label,
      hasAnnotation: store.hasNodeAnnotation(node.id),
      annotation: store.getNodeAnnotation(node.id)
    },
    selected: store.selectedNodes.includes(node.id)
  }))
  
  const graphEdges = data.callGraph.edges.map((edge, index) => ({
    id: edge.id || `edge-${index}`,
    source: edge.from,
    target: edge.to,
    label: edge.label || '',
    type: 'smoothstep',
    animated: store.selectedNodes.includes(edge.from) && store.selectedNodes.includes(edge.to),
    style: {
      stroke: getEdgeColor(edge.type),
      strokeWidth: 2
    },
    markerEnd: {
      type: 'arrowclosed',
      color: getEdgeColor(edge.type)
    }
  }))
  
  // Apply dagre layout
  const layouted = getLayoutedElements(graphNodes, graphEdges)
  
  nodes.value = layouted.nodes
  edges.value = layouted.edges
  
  // Fit view after layout
  setTimeout(() => {
    fitView({ padding: 0.2, duration: 400 })
  }, 100)
}

function getLayoutedElements(nodes, edges) {
  const g = new dagre.graphlib.Graph()
  g.setDefaultEdgeLabel(() => ({}))
  g.setGraph({ 
    rankdir: 'TB',
    ranksep: 100,
    nodesep: 80,
    edgesep: 40
  })
  
  // Add nodes
  nodes.forEach(node => {
    const width = node.type === 'endpoint' ? 280 : 240
    const height = 80
    g.setNode(node.id, { width, height })
  })
  
  // Add edges
  edges.forEach(edge => {
    g.setEdge(edge.source, edge.target)
  })
  
  // Calculate layout
  dagre.layout(g)
  
  // Apply positions
  const layoutedNodes = nodes.map(node => {
    const position = g.node(node.id)
    return {
      ...node,
      position: {
        x: position.x - position.width / 2,
        y: position.y - position.height / 2
      }
    }
  })
  
  return { nodes: layoutedNodes, edges }
}

function getEdgeColor(type) {
  switch (type) {
    case 'method_call': return '#3498db'
    case 'dependency_injection': return '#9b59b6'
    default: return '#95a5a6'
  }
}

function onNodeClick(event) {
  const nodeId = event.node.id
  
  // Handle multi-select with Shift key
  if (event.event.shiftKey) {
    store.toggleNodeSelection(nodeId)
  } else {
    // Single select
    store.setSelectedNodes([nodeId])
  }
  
  // Find full node details
  const allData = store.analysisData || store.filteredData
  const node = findNodeById(nodeId, allData)
  if (node) {
    store.setSelectedNode(node)
  }
}

function onNodeContextMenu(event) {
  event.event.preventDefault()
  contextMenu.value = {
    show: true,
    x: event.event.clientX,
    y: event.event.clientY,
    nodeId: event.node.id
  }
}

function onPaneClick() {
  contextMenu.value.show = false
  if (!event.shiftKey) {
    store.setSelectedNodes([])
    store.setSelectedNode(null)
  }
}

function findNodeById(nodeId, data) {
  if (!data) return null
  
  let found = data.endpoints?.find(e => e.id === nodeId)
  if (found) return { ...found, type: 'endpoint' }
  
  found = data.services?.find(s => s.id === nodeId)
  if (found) return { ...found, type: 'service' }
  
  found = data.repositories?.find(r => r.id === nodeId)
  if (found) return { ...found, type: 'repository' }
  
  return null
}

function addAnnotation() {
  // Emit event to show annotation dialog
  const node = findNodeById(contextMenu.value.nodeId, store.analysisData)
  if (node) {
    store.setSelectedNode(node)
    // The NodeDetails component will handle showing the annotation form
  }
  contextMenu.value.show = false
}

async function generateAnnotation() {
  const node = findNodeById(contextMenu.value.nodeId, store.analysisData)
  if (node) {
    // This will be implemented when LLM panel is created
    console.log('Generate annotation for', node)
  }
  contextMenu.value.show = false
}

function selectConnected() {
  const nodeId = contextMenu.value.nodeId
  const connected = new Set([nodeId])
  
  // Find all connected nodes
  edges.value.forEach(edge => {
    if (edge.source === nodeId) {
      connected.add(edge.target)
    }
    if (edge.target === nodeId) {
      connected.add(edge.source)
    }
  })
  
  store.setSelectedNodes(Array.from(connected))
  contextMenu.value.show = false
}

// Close context menu on click outside
document.addEventListener('click', () => {
  if (contextMenu.value.show) {
    contextMenu.value.show = false
  }
})
</script>

<style>
@import '@vue-flow/core/dist/style.css';
@import '@vue-flow/core/dist/theme-default.css';
@import '@vue-flow/controls/dist/style.css';
@import '@vue-flow/minimap/dist/style.css';
</style>

<style scoped>
.graph-container {
  width: 100%;
  height: 100%;
  position: relative;
  background-color: #f8f9fa;
}

.vue-flow-container {
  width: 100%;
  height: 100%;
}

.context-menu {
  position: fixed;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  min-width: 200px;
}

.context-menu-item {
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background-color 0.15s;
  font-size: 0.9rem;
}

.context-menu-item:hover {
  background-color: #f0f0f0;
}

.context-menu-item:first-child {
  border-radius: 6px 6px 0 0;
}

.context-menu-item:last-child {
  border-radius: 0 0 6px 6px;
}

:deep(.vue-flow__node) {
  cursor: pointer;
}

:deep(.vue-flow__node.selected) {
  box-shadow: 0 0 0 3px #3498db;
}

:deep(.vue-flow__edge.selected) {
  z-index: 1000;
}

:deep(.vue-flow__edge.animated path) {
  stroke-dasharray: 5;
  animation: dashdraw 0.5s linear infinite;
}

@keyframes dashdraw {
  to {
    stroke-dashoffset: -10;
  }
}
</style>
