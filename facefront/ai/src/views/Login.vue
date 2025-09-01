<!-- src/views/Login.vue -->
<template>
  <div class="login-container">
    <div class="login-header"><h1>用户登录</h1></div>

    <!-- 标签 -->
    <div class="login-tabs">
      <button
          v-for="t in tabs"
          :key="t.key"
          :class="['tab-button', { active: tab === t.key }]"
          @click="tab = t.key"
      >
        {{ t.text }}
      </button>
    </div>

    <!-- 1. 密码 -->
    <div v-if="tab === 'password'" class="login-form active">
      <div class="form-group">
        <label>用户名:</label>
        <input v-model="form.username" placeholder="请输入用户名" />
      </div>
      <div class="form-group">
        <label>密码:</label>
        <input v-model="form.password" type="password" placeholder="请输入密码" />
      </div>
      <div class="button-group">
        <button class="login-button small" @click="handlePasswordLogin">
          登录
        </button>
        <button class="register-link" @click="$router.push('/register')">
          前往注册
        </button>
      </div>
    </div>

    <!-- 2. 邮箱验证码 -->
    <div v-if="tab === 'email'" class="login-form active">
      <div class="form-group">
        <label>邮箱:</label>
        <input v-model="form.email" type="email" placeholder="请输入邮箱" />
      </div>
      <div class="form-group verification-group">
        <input v-model="form.emailCode" placeholder="请输入验证码" />
        <button :disabled="counting" @click="handleSendCode">
          {{ counting ? `${count}s后重发` : "发送验证码" }}
        </button>
      </div>
      <div class="button-group">
        <button class="login-button small" @click="handleEmailLogin">
          登录
        </button>
        <button class="register-link" @click="$router.push('/register')">
          前往注册
        </button>
      </div>
    </div>

    <!-- 3. 人脸识别 -->
    <div v-if="tab === 'face'" class="login-form active">
      <div class="camera-container">
        <video v-show="showVideo" ref="video" autoplay playsinline></video>
        <canvas ref="canvas" style="display:none;"></canvas>
        <img v-if="photoSrc" :src="photoSrc" alt="photo" />
        <div class="camera-buttons">
          <button @click="handleOpenCamera" :disabled="cameraOpened">
            打开摄像头
          </button>
          <button
              @click="handleCaptureFace"
              :disabled="!cameraOpened || captured"
          >
            拍照
          </button>
        </div>
      </div>
      <div class="button-group">
        <button
            class="login-button small"
            :disabled="!captured"
            @click="handleFaceLogin"
        >
          人脸识别登录
        </button>
        <button class="register-link" @click="$router.push('/register')">
          前往注册
        </button>
      </div>
    </div>

    <div v-if="msg" :class="['message', msgType]">{{ msg }}</div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      tabs: [
        { key: 'password', text: '密码登录' },
        { key: 'email', text: '邮箱验证码' },
        { key: 'face', text: '人脸识别' }
      ],
      tab: 'password',
      form: {
        username: '',
        password: '',
        email: '',
        emailCode: ''
      },
      msg: '',
      msgType: 'info',
      showVideo: true,
      cameraOpened: false,
      captured: false,
      photoSrc: '',
      stream: null,
      counting: false,
      count: 60
    }
  },
  methods: {
    /* 通用提示 */
    showMsg(txt, type = 'info') {
      this.msg = txt
      this.msgType = type
    },

    /* 1. 密码登录 */
    handlePasswordLogin() {
      if (!this.form.username || !this.form.password) return
      this.$axios({
        method: 'POST',
        url: 'http://localhost:9999/user/login',
        data: {
          username: this.form.username,
          password: this.form.password
        },
        headers: { 'Content-Type': 'application/json' }
      })
          .then((res) => {
            const data = res.data
            if (!data) return alert('用户名或密码错误')
            sessionStorage.setItem(
                'user',
                JSON.stringify({
                  userInfo: {
                    id: data.id,
                    username: data.username,
                    password: data.password,
                    email: data.email
                  },
                  expiration: new Date(Date.now() + 5 * 60 * 60 * 1000).toISOString()
                })
            )
            this.$router.push('/chat')
          })
          .catch(() => alert('登录失败'))
    },

    /* 2. 发送邮箱验证码 */
    handleSendCode() {
      if (!this.form.email) return this.showMsg('请填写邮箱', 'error')
      this.counting = true
      this.$axios({
        method: 'POST',
        url: 'http://localhost:9999/user/mail',
        data: { email: this.form.email },
        headers: { 'Content-Type': 'application/json' }
      })
          .then(() => {
            this.showMsg('验证码已发送', 'success')
            const timer = setInterval(() => {
              this.count--
              if (this.count <= 0) {
                clearInterval(timer)
                this.counting = false
                this.count = 60
              }
            }, 1000)
          })
          .catch((e) => {
            this.counting = false
            this.showMsg(e?.response?.data || '发送失败', 'error')
          })
    },

    /* 3. 邮箱验证码登录 */
    handleEmailLogin() {
      if (!this.form.email || !this.form.emailCode)
        return this.showMsg('请填写完整', 'error')
      this.$axios({
        method: 'POST',
        url: 'http://localhost:9999/user/loginMail',
        data: { email: this.form.email, code: this.form.emailCode },
        headers: { 'Content-Type': 'application/json' }
      })
          .then((res) => {
            const data = res.data
            sessionStorage.setItem(
                'user',
                JSON.stringify({
                  userInfo: {
                    userId: data.id,
                    username: data.username,
                    password: data.password,
                    email: data.email
                  },
                  expiration: new Date(Date.now() + 5 * 60 * 60 * 1000).toISOString()
                })
            )
            this.$router.push('/chat')
          })
          .catch((e) => this.showMsg(e?.response?.data || '登录失败', 'error'))
    },

    /* 4. 打开摄像头 */
    handleOpenCamera() {
      navigator.mediaDevices
          .getUserMedia({ video: { width: 320, height: 240 } })
          .then((st) => {
            this.stream = st
            this.$refs.video.srcObject = st
            this.cameraOpened = true
          })
          .catch(() => this.showMsg('无法访问摄像头', 'error'))
    },

    /* 5. 拍照 */
    handleCaptureFace() {
      const c = this.$refs.canvas
      const v = this.$refs.video
      c.width = v.videoWidth
      c.height = v.videoHeight
      c.getContext('2d').drawImage(v, 0, 0)
      this.photoSrc = c.toDataURL('image/png')
      this.captured = true
      this.stream?.getTracks().forEach((t) => t.stop())
      this.stream = null
      this.showVideo = false
    },

    /* 6. 人脸识别登录 */
    handleFaceLogin() {
      const base64 = this.photoSrc.split(',')[1]
      if (!base64) return this.showMsg('请拍照', 'error')
      this.$axios({
        method: 'POST',
        url: 'http://localhost:9999/user/loginFace',
        data: { imagetoken: base64 },
        headers: { 'Content-Type': 'application/json' }
      })
          .then((res) => {
            const data = res.data
            sessionStorage.setItem(
                'user',
                JSON.stringify({
                  userInfo: {
                    userId: data.id,
                    username: data.username,
                    password: data.password,
                    email: data.email
                  },
                  expiration: new Date(Date.now() + 5 * 60 * 60 * 1000).toISOString()
                })
            )
            this.$router.push('/chat')
          })
          .catch((e) => this.showMsg(e?.response?.data || '登录失败', 'error'))
    }
  }
}
</script>

<style scoped>
body {
  font-family: Arial, sans-serif;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f5f5;
}
.login-container {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-top: 50px;
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.login-tabs {
  display: flex;
  border-bottom: 1px solid #ddd;
  margin-bottom: 30px;
}
.tab-button {
  flex: 1;
  padding: 15px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
}
.tab-button.active {
  color: #4caf50;
  border-bottom-color: #4caf50;
  font-weight: bold;
}
.login-form {
  display: none;
}
.login-form.active {
  display: block;
}
.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}
.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  box-sizing: border-box;
}
.verification-group {
  display: flex;
  gap: 10px;
}
.verification-group button {
  flex: 0 0 auto;
  padding: 12px 15px;
  background: #4caf50;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.verification-group button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.camera-container {
  text-align: center;
  margin: 20px 0;
}
video {
  width: 100%;
  max-width: 320px;
  background: #000;
  border-radius: 4px;
}
.camera-buttons {
  margin: 15px 0;
}
.camera-buttons button {
  margin: 0 5px;
  padding: 10px 15px;
  background: #4caf50;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.camera-buttons button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.login-button {
  width: 100%;
  padding: 12px;
  background: #4caf50;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 10px;
}
.login-button:hover {
  background: #45a049;
}
.login-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.message {
  margin: 15px 0;
  padding: 10px;
  border-radius: 4px;
  text-align: center;
}
.message.success {
  background: #dff0d8;
  color: #3c763d;
}
.message.error {
  background: #f2dede;
  color: #a94442;
}
.message.info {
  background: #d9edf7;
  color: #31708f;
}
.button-group {
  display: flex;
  justify-content: center;
  gap: 30px;
  align-items: center;
  margin-top: 20px;
}
.login-button.small {
  width: auto;
  flex: 0 0 auto;
  padding: 10px 20px;
}
.register-link {
  background: none;
  border: none;
  color: #4caf50;
  text-decoration: underline;
  cursor: pointer;
  font-size: 14px;
  padding: 10px 0;
}
.register-link:hover {
  color: #45a049;
}
</style>