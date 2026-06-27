<template>
  <div class="ai-chat-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>智能问答</h2>
      <p>基于AI的智能销售数据分析和问答系统</p>
    </div>
    
    <!-- 聊天区域 -->
    <div class="chat-area">
      <!-- 聊天消息列表 -->
      <div class="chat-messages" ref="chatMessagesRef">
        <div v-for="(message, index) in messages" :key="index" :class="['message', message.type]">
          <div class="message-avatar">
            <el-avatar :size="40" v-if="message.type === 'user'">
              {{ userInfo.name?.charAt(0) || 'U' }}
            </el-avatar>
            <el-avatar :size="40" v-else style="background-color: #409eff;">
              <el-icon><ChatDotRound /></el-icon>
            </el-avatar>
          </div>
          
          <div class="message-content">
            <div class="message-header">
              <span class="message-sender">{{ message.type === 'user' ? userInfo.name || '用户' : 'AI助手' }}</span>
              <span class="message-time">{{ message.time }}</span>
            </div>
            
            <div class="message-body" v-html="formatMessage(message.content)"></div>
            
          <!-- 消息操作 -->
          <div class="message-actions" v-if="message.type === 'ai' && !message.isStreaming">
            <el-button size="small" link @click="copyMessage(message.content)">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
          </div>
          
          <!-- 流式输出光标 -->
          <span v-if="message.isStreaming" class="streaming-cursor">|</span>
        </div>
      </div>
      
      <!-- 加载中状态（仅在初始等待时显示） -->
      <div v-if="loading && !streamingActive && messages.length === 0" class="message ai">
        <div class="message-avatar">
          <el-avatar :size="40" style="background-color: #409eff;">
            <el-icon><ChatDotRound /></el-icon>
          </el-avatar>
        </div>
        <div class="message-content">
          <div class="message-header">
            <span class="message-sender">AI助手</span>
          </div>
          <div class="message-body">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="chat-input">
        <div class="input-toolbar">
          <el-tooltip content="数据库查询分析">
            <el-button 
              :type="chatMode === 'sql' ? 'primary' : 'default'" 
              size="small" 
              @click="chatMode = 'sql'"
            >
              <el-icon><DataAnalysis /></el-icon>
              数据分析
            </el-button>
          </el-tooltip>
          
          <el-tooltip content="自动意图识别">
            <el-button 
              :type="chatMode === 'auto' ? 'primary' : 'default'" 
              size="small" 
              @click="chatMode = 'auto'"
            >
              <el-icon><MagicStick /></el-icon>
              智能问答
            </el-button>
          </el-tooltip>
          
          <el-button size="small" @click="clearChat" style="margin-left: auto;">
            <el-icon><Delete /></el-icon>
            清空对话
          </el-button>
        </div>
        
        <div class="input-box">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题，例如：本月销售额最高的销售是谁？哪些商品库存不足？销售趋势如何？"
            @keydown.enter.ctrl="sendMessage"
            @keydown.enter.meta="sendMessage"
          />
          
          <div class="input-actions">
            <div class="input-tip">
              按 Ctrl+Enter 发送消息
            </div>
            <el-button type="primary" @click="sendMessage" :loading="loading" :disabled="!inputMessage.trim()">
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 历史对话侧边栏 -->
    <div class="history-sidebar" :class="{ 'is-open': showHistory }">
      <div class="sidebar-header">
        <h3>历史对话</h3>
        <el-button link @click="showHistory = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="sidebar-content">
        <div 
          v-for="(conversation, index) in conversations" 
          :key="index" 
          class="conversation-item"
          :class="{ active: currentConversationId === conversation.id }"
          @click="loadConversation(conversation.id)"
        >
          <div class="conversation-title">{{ conversation.title }}</div>
          <div class="conversation-time">{{ conversation.lastMessageTime }}</div>
        </div>
        
        <div v-if="conversations.length === 0" class="empty-history">
          暂无历史对话
        </div>
      </div>
    </div>
    
    <!-- 历史对话按钮 -->
    <el-button 
      class="history-button" 
      @click="showHistory = !showHistory"
      :type="showHistory ? 'primary' : 'default'"
    >
      <el-icon><Clock /></el-icon>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiApi } from '@/api'

// 用户信息
const userInfo = reactive({
  name: '',
  role: ''
})

// 聊天相关
const inputMessage = ref('')
const chatMode = ref<'sql' | 'auto'>('auto')
const loading = ref(false)
const streamingActive = ref(false)
const chatMessagesRef = ref()
const showHistory = ref(false)

// 消息列表
const messages = ref<Array<{
  type: 'user' | 'ai'
  content: string
  time: string
  isStreaming?: boolean
}>>([])

// 对话历史
const conversations = ref<Array<{
  id: string
  title: string
  lastMessageTime: string
}>>([])

const currentConversationId = ref('')

// 格式化消息（支持Markdown）
const formatMessage = (content: string) => {
  if (!content) return ''
  
  // 1. 先提取代码块，避免被解析
  const codeBlocks: string[] = []
  let formatted = content.replace(/```(\w*)\n([\s\S]*?)```/g, (_match, lang, code) => {
    const index = codeBlocks.length
    const languageClass = lang ? ` class="language-${lang}"` : ''
    codeBlocks.push(`<pre><code${languageClass}>${escapeHtml(code.trim())}</code></pre>`)
    return `\n__CODE_BLOCK_${index}__\n`
  })
  
  // 2. 提取行内代码
  const inlineCodes: string[] = []
  formatted = formatted.replace(/`([^`]+)`/g, (_match, code) => {
    const index = inlineCodes.length
    inlineCodes.push(`<code>${escapeHtml(code)}</code>`)
    return `__INLINE_CODE_${index}__`
  })
  
  // 3. 逐行处理Markdown
  const lines = formatted.split('\n')
  const outputLines: string[] = []
  let inOrderedList = false
  let inUnorderedList = false
  
  for (let i = 0; i < lines.length; i++) {
    let line = lines[i]
    
    // 检查是否是代码块占位符
    if (line.trim().match(/^__CODE_BLOCK_\d+__$/)) {
      if (inOrderedList) { outputLines.push('</ol>'); inOrderedList = false }
      if (inUnorderedList) { outputLines.push('</ul>'); inUnorderedList = false }
      outputLines.push(line.trim())
      continue
    }
    
    // 转义HTML
    line = escapeHtml(line)
    
    // 标题
    const headingMatch = line.match(/^(#{1,6})\s+(.+)$/)
    if (headingMatch) {
      if (inOrderedList) { outputLines.push('</ol>'); inOrderedList = false }
      if (inUnorderedList) { outputLines.push('</ul>'); inUnorderedList = false }
      const level = headingMatch[1].length
      outputLines.push(`<h${level}>${headingMatch[2]}</h${level}>`)
      continue
    }
    
    // 无序列表
    const ulMatch = line.match(/^\s*[-*]\s+(.+)$/)
    if (ulMatch) {
      if (inOrderedList) { outputLines.push('</ol>'); inOrderedList = false }
      if (!inUnorderedList) { outputLines.push('<ul>'); inUnorderedList = true }
      outputLines.push(`<li>${applyInlineFormatting(ulMatch[1])}</li>`)
      continue
    }
    
    // 有序列表
    const olMatch = line.match(/^\s*\d+\.\s+(.+)$/)
    if (olMatch) {
      if (inUnorderedList) { outputLines.push('</ul>'); inUnorderedList = false }
      if (!inOrderedList) { outputLines.push('<ol>'); inOrderedList = true }
      outputLines.push(`<li>${applyInlineFormatting(olMatch[1])}</li>`)
      continue
    }
    
    // 引用块
    const blockquoteMatch = line.match(/^&gt;\s*(.+)$/)
    if (blockquoteMatch) {
      if (inOrderedList) { outputLines.push('</ol>'); inOrderedList = false }
      if (inUnorderedList) { outputLines.push('</ul>'); inUnorderedList = false }
      outputLines.push(`<blockquote>${applyInlineFormatting(blockquoteMatch[1])}</blockquote>`)
      continue
    }
    
    // 水平线
    if (line.match(/^---+$/)) {
      if (inOrderedList) { outputLines.push('</ol>'); inOrderedList = false }
      if (inUnorderedList) { outputLines.push('</ul>'); inUnorderedList = false }
      outputLines.push('<hr>')
      continue
    }
    
    // 关闭列表
    if (line.trim() === '') {
      if (inOrderedList) { outputLines.push('</ol>'); inOrderedList = false }
      if (inUnorderedList) { outputLines.push('</ul>'); inUnorderedList = false }
      continue
    }
    
    // 普通段落
    if (inOrderedList) { outputLines.push('</ol>'); inOrderedList = false }
    if (inUnorderedList) { outputLines.push('</ul>'); inUnorderedList = false }
    outputLines.push(applyInlineFormatting(line))
  }
  
  // 关闭未关闭的列表
  if (inOrderedList) outputLines.push('</ol>')
  if (inUnorderedList) outputLines.push('</ul>')
  
  // 4. 用<br>连接行，还原代码块和行内代码
  let result = outputLines.join('<br>')
  
  // 还原代码块和行内代码
  result = result.replace(/__CODE_BLOCK_(\d+)__/g, (_match, index) => codeBlocks[parseInt(index)])
  result = result.replace(/__INLINE_CODE_(\d+)__/g, (_match, index) => inlineCodes[parseInt(index)])
  
  // 5. 清理多余的<br>标签
  result = result.replace(/<br><br>/g, '<br>')
  result = result.replace(/<br><\/(h[1-6]|blockquote|pre|ul|ol|li|hr)>/g, '</$1>')
  result = result.replace(/<(h[1-6]|blockquote|pre|ul|ol|li|hr)><br>/g, '<$1>')
  
  return result
}

// 应用行内Markdown格式（粗体、斜体、链接等）
const applyInlineFormatting = (text: string) => {
  let result = text
  // 粗体
  result = result.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  // 斜体
  result = result.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  // 链接
  result = result.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
  // 图片
  result = result.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" style="max-width: 100%;">')
  return result
}

// HTML转义函数
const escapeHtml = (text: string) => {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

// 复制消息
const copyMessage = async (content: string) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = content
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    ElMessage.success('已复制到剪贴板')
  }
}

// 发送消息（支持SSE流式输出）
const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  const userMessage = inputMessage.value.trim()
  
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: userMessage,
    time: new Date().toLocaleTimeString()
  })
  
  inputMessage.value = ''
  loading.value = true
  streamingActive.value = true
  
  // 添加AI消息占位符（用于流式更新）
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    time: new Date().toLocaleTimeString(),
    isStreaming: true
  })
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  // 构建请求数据
  const requestData = {
    question: userMessage,
    conversationId: currentConversationId.value || undefined
  }
  
  // 使用SSE流式输出
  aiApi.chatStream(
    requestData,
    // onToken - 每收到一个token
    (token: string) => {
      messages.value[aiMessageIndex].content += token
      nextTick(() => scrollToBottom())
    },
    // onComplete - 完成
    (response: any) => {
      messages.value[aiMessageIndex].isStreaming = false
      loading.value = false
      streamingActive.value = false
      
      // 更新对话ID
      if (response.conversationId) {
        currentConversationId.value = response.conversationId
      }
      
      // 更新对话历史
      loadConversations()
      
      nextTick(() => scrollToBottom())
    },
    // onError - 错误
    (error: string) => {
      console.error('流式聊天失败:', error)
      
      // 如果还没有内容，显示错误信息
      if (!messages.value[aiMessageIndex].content) {
        messages.value[aiMessageIndex].content = '抱歉，处理您的问题时出现错误，请稍后重试。'
      }
      
      messages.value[aiMessageIndex].isStreaming = false
      loading.value = false
      streamingActive.value = false
      
      ElMessage.error('获取回复失败: ' + error)
      nextTick(() => scrollToBottom())
    }
  )
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

// 清空对话
const clearChat = async () => {
  try {
    await ElMessageBox.confirm('确定要清空当前对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 如果有对话ID，清除后端对话历史
    if (currentConversationId.value) {
      try {
        await aiApi.clearConversationHistory(currentConversationId.value)
      } catch (error) {
        console.error('清除对话历史失败:', error)
      }
    }
    
    messages.value = []
    currentConversationId.value = ''
    ElMessage.success('对话已清空')
    
    // 刷新对话历史列表
    loadConversations()
  } catch (error) {
    // 取消操作
  }
}

// 加载对话历史
const loadConversations = async () => {
  try {
    const response = await aiApi.getConversations()
    if (response.code === 200) {
      conversations.value = response.data
    }
  } catch (error: any) {
    console.error('加载对话历史失败:', error)
  }
}

// 加载特定对话
const loadConversation = async (conversationId: string) => {
  try {
    const response = await aiApi.getConversationHistory(conversationId)
    if (response.code === 200) {
      messages.value = response.data.map((msg: any) => ({
        type: msg.role === 'user' ? 'user' : 'ai',
        content: msg.content,
        time: new Date(msg.timestamp).toLocaleTimeString()
      }))
      currentConversationId.value = conversationId
      showHistory.value = false
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
    }
  } catch (error: any) {
    console.error('加载对话失败:', error)
    ElMessage.error('加载对话失败')
  }
}

// 获取用户信息
const getUserInfo = () => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      const info = JSON.parse(userInfoStr)
      userInfo.name = info.name
      userInfo.role = info.role
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 欢迎消息
const addWelcomeMessage = () => {
  messages.value.push({
    type: 'ai',
    content: `您好！我是企业销售管理系统的AI助手，可以为您提供以下帮助：

1. **销售数据分析** - 查询销售额、销售排行、本月业绩等
2. **库存查询** - 查看商品库存、库存预警等
3. **订单管理** - 查询订单状态、订单统计等
4. **员工绩效** - 查询员工业绩、团队数据等
5. **趋势分析** - 分析销售趋势、数据对比等

请问有什么可以帮助您的？`,
    time: new Date().toLocaleTimeString()
  })
}

onMounted(() => {
  getUserInfo()
  addWelcomeMessage()
  loadConversations()
})
</script>

<style scoped>
.ai-chat-container {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  position: relative;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  min-width: 100px;
}

.message.user .message-content {
  text-align: right;
}

.message-header {
  margin-bottom: 8px;
}

.message-sender {
  font-weight: bold;
  color: #333;
  margin-right: 10px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-body {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  line-height: 1.6;
  word-break: break-word;
}

.message.user .message-body {
  background: #ecf5ff;
  color: #333;
}

.message-actions {
  margin-top: 10px;
  opacity: 0;
  transition: opacity 0.3s;
}

.message:hover .message-actions {
  opacity: 1;
}

.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 10px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.6;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.streaming-cursor {
  display: inline-block;
  color: #409eff;
  font-weight: bold;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.chat-input {
  border-top: 1px solid #eee;
  padding: 15px;
}

.input-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #eee;
}

.input-box {
  display: flex;
  flex-direction: column;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.input-tip {
  font-size: 12px;
  color: #999;
}

.history-sidebar {
  position: fixed;
  right: -350px;
  top: 0;
  width: 350px;
  height: 100vh;
  background: white;
  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.1);
  transition: right 0.3s;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.history-sidebar.is-open {
  right: 0;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.sidebar-header h3 {
  margin: 0;
  color: #333;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.conversation-item {
  padding: 15px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 10px;
  background: #f5f7fa;
  transition: background-color 0.3s;
}

.conversation-item:hover {
  background: #ecf5ff;
}

.conversation-item.active {
  background: #ecf5ff;
  border: 1px solid #409eff;
}

.conversation-title {
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-time {
  font-size: 12px;
  color: #999;
}

.empty-history {
  text-align: center;
  color: #999;
  padding: 40px 20px;
}

.history-button {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 1001;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 代码样式 */
.message-body :deep(pre) {
  background: #282c34;
  color: #abb2bf;
  padding: 15px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 10px 0;
}

.message-body :deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace;
}

.message-body :deep(pre code) {
  background: none;
  padding: 0;
}

.message-body :deep(strong) {
  font-weight: bold;
  color: #333;
}

.message-body :deep(em) {
  font-style: italic;
  color: #666;
}

/* 标题样式 */
.message-body :deep(h1),
.message-body :deep(h2),
.message-body :deep(h3),
.message-body :deep(h4),
.message-body :deep(h5),
.message-body :deep(h6) {
  margin: 15px 0 10px 0;
  color: #2c3e50;
  line-height: 1.4;
}

.message-body :deep(h1) {
  font-size: 1.6em;
  border-bottom: 2px solid #eee;
  padding-bottom: 8px;
}

.message-body :deep(h2) {
  font-size: 1.4em;
  border-bottom: 1px solid #eee;
  padding-bottom: 6px;
}

.message-body :deep(h3) {
  font-size: 1.2em;
}

.message-body :deep(h4) {
  font-size: 1.1em;
}

.message-body :deep(h5) {
  font-size: 1em;
}

.message-body :deep(h6) {
  font-size: 0.9em;
  color: #666;
}

/* 列表样式 */
.message-body :deep(ul),
.message-body :deep(ol) {
  margin: 10px 0;
  padding-left: 25px;
}

.message-body :deep(li) {
  margin: 5px 0;
  line-height: 1.6;
}

/* 链接样式 */
.message-body :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.message-body :deep(a:hover) {
  text-decoration: underline;
}

/* 引用块样式 */
.message-body :deep(blockquote) {
  margin: 10px 0;
  padding: 10px 15px;
  border-left: 4px solid #409eff;
  background: #f8f9fa;
  color: #666;
}

/* 水平线样式 */
.message-body :deep(hr) {
  margin: 15px 0;
  border: none;
  border-top: 1px solid #eee;
}

/* 图片样式 */
.message-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 10px 0;
}

/* 表格样式 */
.message-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 10px 0;
}

.message-body :deep(th),
.message-body :deep(td) {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: left;
}

.message-body :deep(th) {
  background: #f5f7fa;
  font-weight: bold;
}

.message-body :deep(tr:nth-child(even)) {
  background: #f9f9f9;
}
</style>