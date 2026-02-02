<template>
  <div class="graph-container">
    <div ref="networkContainer" class="network"></div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { Network } from 'vis-network/standalone'
import { useAnalysisStore } from '../stores/analysisStore'

const store = useAnalysisStore()
const networkContainer = ref(null)
let network = null

// Network options
const options = {
  nodes: {
    shape: 'box',
    margin: 10,
    font: {
      size: 14,
      face: 'monospace'
    }
  },
  edges: {
    arrows: {
      to: {
        enabled: true,
        scaleFactor: 0.5
      }
    },
    smooth: {
      type: 'cubicBezier',
      forceDirection: 'vertical'
    },
    color: {
      color: '#848484',
      highlight: '#2B7CE9',
      hover: '#2B7CE9'
    }
  },
  layout: {
    hierarchical: {
      enabled: true,
      direction: 'UD',
      sortMethod: 'directed',
      levelSeparation: 150,
      nodeSpacing: 200
    }
  },
  physics: {
    enabled: false
  },
  interaction: {
    hover: true,
    navigationButtons: true,
    keyboard: true
  }
}

function initNetwork() {
  if (!networkContainer.value) return

  const data = prepareGraphData()
  network = new Network(networkContainer.value, data, options)

  // Handle node selection
  network.on('click', (params) => {
    if (params.nodes.length > 0) {
      const nodeId = params.nodes[0]
      const node = findNodeDetails(nodeId)
      store.setSelectedNode(node)
    } else {
      store.setSelectedNode(null)
    }
  })

  // Handle double click to focus
  network.on('doubleClick', (params) => {
    if (params.nodes.length > 0) {
      network.focus(params.nodes[0], {
        scale: 1.5,
        animation: true
      })
    }
  })
}

function prepareGraphData() {
  const filteredData = store.filteredData || store.analysisData
  
  if (!filteredData || !filteredData.callGraph) {
    return { nodes: [], edges: [] }
  }

  // Prepare nodes with colors based on type
  const nodes = filteredData.callGraph.nodes.map(node => ({
    id: node.id,
    label: node.label,
    color: getNodeColor(node.type),
    title: getNodeTooltip(node),
    font: {
      color: '#ffffff',
      size: 12
    }
  }))

  // Prepare edges
  const edges = filteredData.callGraph.edges.map((edge, index) => ({
    id: `edge-${index}`,
    from: edge.from,
    to: edge.to,
    label: edge.label || '',
    title: edge.type
  }))

  return { nodes, edges }
}

function getNodeColor(type) {
  switch (type) {
    case 'endpoint':
      return {
        background: '#2ecc71',
        border: '#27ae60',
        highlight: { background: '#27ae60', border: '#229954' }
      }
    case 'service':
      return {
        background: '#3498db',
        border: '#2980b9',
        highlight: { background: '#2980b9', border: '#21618c' }
      }
    case 'repository':
      return {
        background: '#e74c3c',
        border: '#c0392b',
        highlight: { background: '#c0392b', border: '#a93226' }
      }
    default:
      return {
        background: '#95a5a6',
        border: '#7f8c8d',
        highlight: { background: '#7f8c8d', border: '#626567' }
      }
  }
}

function getNodeTooltip(node) {
  return `${node.type.toUpperCase()}\n${node.className || ''}.${node.methodName || ''}`
}

function findNodeDetails(nodeId) {
  const data = store.filteredData || store.analysisData
  if (!data) return null

  // Check endpoints
  const endpoint = data.endpoints.find(e => e.id === nodeId)
  if (endpoint) return { ...endpoint, type: 'endpoint' }

  // Check services
  const service = data.services.find(s => s.id === nodeId)
  if (service) return { ...service, type: 'service' }

  // Check repositories
  const repo = data.repositories.find(r => r.id === nodeId)
  if (repo) return { ...repo, type: 'repository' }

  return null
}

function updateGraph() {
  if (network) {
    const data = prepareGraphData()
    network.setData(data)
    network.fit()
  }
}

// Watch for data changes
watch(() => store.filteredData, () => {
  updateGraph()
}, { deep: true })

watch(() => store.analysisData, () => {
  if (store.analysisData && network) {
    updateGraph()
  }
})

onMounted(() => {
  initNetwork()
})

onUnmounted(() => {
  if (network) {
    network.destroy()
    network = null
  }
})
</script>

<style scoped>
.graph-container {
  width: 100%;
  height: 100%;
  background-color: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
}

.network {
  width: 100%;
  height: 100%;
}
</style>
