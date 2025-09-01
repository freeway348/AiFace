<template>
  <div class="wrap">
    <!--侧边栏-->
    <aside class="sidebar">
      <h2>智言Chat</h2>

      <button @click="createConversation">新建对话</button>
      <button @click="logout">退出登录</button>

      <div>
        <div
            v-for="c in conversations"
            :key="c.id"
            :class="['conversation-card', { active: c.id === activeId }]"
            @click="selectConversation(c.id)"
        >
          {{ c.title }}
        </div>
      </div>

      <!--当前用户信息及修改按钮-->
      <div v-if="user" style="margin-top: auto; padding: 10px 20px;">
        <p>当前用户：{{ user.username }}</p>
        <button @click="openUserModal">修改个人信息</button>
      </div>

      <!--修改用户信息模态框-->
      <div
          v-if="userModal.show"
          class="modal-mask"
          @click.self="closeUserModal"
      >
        <div class="modal">
          <h3>修改用户信息</h3>
          <label>用户名</label>
          <input v-model="userModal.form.username" />
          <label>密码</label>
          <input v-model="userModal.form.password" type="password" />
          <label>邮箱（可选）</label>
          <input v-model="userModal.form.email" />
          <div class="modal-actions">
            <button @click="saveUserInfo">保存</button>
            <button @click="closeUserModal">取消</button>
          </div>
        </div>
      </div>
    </aside>

    <!--聊天区-->
    <main class="chat-container">
      <header class="chat-header">
        <h1>{{ chatTitle }}</h1>
      </header>

      <div class="chat-messages" ref="chatMessages">
        <div
            v-for="(m, i) in messages"
            :key="i"
            :class="['message', m.sender === 'user' ? 'user-message' : 'ai-message']"
            v-html="m.html"
        ></div>
        <div v-show="typing" class="typing-indicator">AI正在思考中...</div>
      </div>

      <footer class="chat-input">
        <input
            v-model="question"
            @keyup.enter="send"
            placeholder="请输入您的问题..."
        />
        <button @click="send" :disabled="!question.trim()">发送</button>
      </footer>
    </main>
  </div>
</template>

<script>
export default {
  name: 'Chat',
  data () {
    return {
      user: null,
      activeId: '',
      conversations: [],
      messages: [],
      chatTitle: 'AI聊天助手',
      question: '',
      typing: false,
      userModal: {
        show: false,
        form: { username: '', email: '' }
      }
    }
  },
  computed: {
    userNs () {
      return this.user ? `user_${this.user.userId}_` : ''
    }
  },
  mounted () {
    this.initUser()
    setInterval(this.saveConversation, 3000)
  },
  methods: {
    /* ===== 工具 ===== */
    genId () {
      return `conv_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    },
    store (k, v) {
      localStorage.setItem(this.userNs + k, JSON.stringify(v))
    },
    load (k) {
      const str = localStorage.getItem(this.userNs + k)
      return str ? JSON.parse(str) : null
    },

    /* ===== 用户 ===== */
    initUser () {
      const raw = sessionStorage.getItem('user')
      if (!raw) return this.$router.push('/login')
      const { userInfo } = JSON.parse(raw)
      this.user = userInfo
      this.loadConversations()
    },
    logout () {
      sessionStorage.removeItem('user')
      this.$router.push('/login')
    },

    /* ===== 会话 ===== */
    createConversation () {
      const id = this.genId()
      const title = `会话 ${new Date().toLocaleString()}`
      this.conversations.unshift({ id, title, lastActive: Date.now() })
      this.store('conversations', this.conversations)
      this.selectConversation(id)
    },
    selectConversation (id) {
      this.activeId = id
      const data = this.load(id) || { messages: [] }
      this.messages = (data.messages || []).map(m => ({
        ...m,
        html: this.render(m.text)
      }))
      this.chatTitle = data.title || '新会话'
    },
    saveConversation () {
      if (!this.activeId) return
      this.store(this.activeId, {
        title: this.chatTitle,
        messages: this.messages.map(m => ({ text: m.text, sender: m.sender })),
        lastActive: Date.now()
      })
    },
    loadConversations () {
      this.conversations = this.load('conversations') || []
      if (this.conversations.length) {
        this.selectConversation(this.conversations[0].id)
      } else {
        this.createConversation()
      }
    },

    /* ===== 消息 ===== */
    render (raw) {
      if (!raw) return ''
      let html = raw
          .replace(/<think>[\s\S]*?<\/think>/gi, '')
          .replace(/\n{2,}/g, '\n')
          .replace(/^\s+|\s+$/g, '')

      html = html
          .replace(/&/g, '&amp;')
          .replace(/</g, '&lt;')
          .replace(/>/g, '&gt;')

      html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>')
      html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>')
      html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>')

      html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
      html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')

      html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      html = html.replace(/`([^`]+)`/g, '<code>$1</code>')
      html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
      html = html.replace(/\n/g, '<br>')
      return html
    },
    scroll () {
      this.$nextTick(() => {
        const el = this.$refs.chatMessages
        el.scrollTop = el.scrollHeight
      })
    },
    send () {
      const q = this.question.trim()
      if (!q) return
      this.messages.push({ text: q, sender: 'user', html: this.render(q) })
      this.question = ''
      this.scroll()
      this.typing = true

      this.$axios({
        method: 'POST',
        url: 'http://localhost:9999/chat',
        data: { question: q },
        headers: { 'Content-Type': 'application/json' }
      }).then(res => {
        const ans = res.data.message?.content || '抱歉，未能获取回答内容。'
        this.messages.push({ text: ans, sender: 'ai', html: this.render(ans) })
        this.scroll()
      }).catch(() => {
        this.messages.push({ text: '网络错误', sender: 'ai', html: '网络错误' })
      }).finally(() => {
        this.typing = false
        this.scroll()
        this.saveConversation()
      })
    },

    /* ===== 模态框 ===== */
    openUserModal () {
      const { userInfo } = JSON.parse(sessionStorage.getItem('user') || '{}')
      Object.assign(this.userModal.form, {
        username: userInfo.username || '',
        email: userInfo.email || '',
        password: userInfo.password || ''
      })
      this.userModal.show = true
    },
    closeUserModal () {
      this.userModal.show = false
    },
    saveUserInfo () {
      const { userInfo } = JSON.parse(sessionStorage.getItem('user'))
      this.$axios({
        method: 'PUT',
        url: 'http://localhost:9999/user/update',
        data: {
          userId: userInfo.userId,
          username: this.userModal.form.username,
          email: this.userModal.form.email,
          password: this.userModal.form.password || undefined
        },
        headers: { 'Content-Type': 'application/json' }
      }).then(res => {
        this.user = res.data
        const stored = JSON.parse(sessionStorage.getItem('user'))
        stored.userInfo = res.data
        sessionStorage.setItem('user', JSON.stringify(stored))
        this.closeUserModal()
      }).catch(err => {
        alert('更新失败: ' + (err?.response?.data || err.message))
      })
    }
  }
}
</script>

<style scoped>
html, body, #app {
  height: 100%;
  margin: 0;
}
.wrap {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #ddd;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  padding: 20px 0;
}
.sidebar h2 {
  margin: 0 20px 20px;
}
.sidebar button {
  width: calc(100% - 40px);
  margin: 0 20px 10px;
  padding: 12px 20px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 5px;
  cursor: pointer;
  text-align: left;
}
.sidebar button:hover {
  background: #e9ecef;
}
.conversation-card {
  margin: 5px 20px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 5px;
  cursor: pointer;
}
.conversation-card.active {
  background: #dfebf8;
}
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.chat-header {
  padding: 15px 20px;
  text-align: center;
  border-bottom: 1px solid #eee;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}
.message {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 18px;
  word-wrap: break-word;
}
.user-message {
  align-self: flex-end;
  background: #4e73df;
  color: #fff;
}
.ai-message {
  align-self: flex-start;
  background: #f0f0f0;
  color: #333;
}
.chat-input {
  display: flex;
  padding: 12px 20px;
  border-top: 1px solid #eee;
}
.chat-input input {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
}
.chat-input button {
  margin-left: 10px;
  padding: 12px 25px;
  background: #4e73df;
  color: #fff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}
.typing-indicator {
  align-self: flex-start;
  background: #f0f0f0;
  padding: 12px 16px;
  border-radius: 18px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  width: 320px;
  max-width: 90vw;
}
.modal label {
  display: block;
  margin: 8px 0 4px;
}
.modal input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.modal-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>