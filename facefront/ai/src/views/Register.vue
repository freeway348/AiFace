<!-- src/views/Register.vue -->
<template>
  <div>
    <h1 class="header">
      <span>用户信息注册</span>
      <span>摄像头拍照上传</span>
    </h1>

    <div class="main-layout">
      <div class="form-container">
        <div class="input-group">
          <label for="username">用户名:</label>
          <input type="text" id="username" v-model="username" />
        </div>

        <div class="input-group">
          <label for="password">密码:</label>
          <input type="password" id="password" v-model="password" />
        </div>

        <div class="input-group">
          <label for="email">邮箱:</label>
          <input type="email" id="email" v-model="email" />
        </div>

        <div class="input-group">
          <label for="veriCode">验证码:</label>
          <input type="tel" id="veriCode" v-model="veriCode" />
        </div>
      </div>

      <div class="camera-container">
        <video
            ref="video"
            class="video"
            autoplay
            playsinline
            :style="{ display: showVideo ? 'block' : 'none' }"
        ></video>
        <canvas ref="canvas" style="display:none;"></canvas>
        <img
            :src="photoSrc"
            class="photo"
            alt="拍摄的照片"
            :style="{ display: photoSrc ? 'block' : 'none' }"
        />

        <div class="camera-buttons">
          <button
              @click="startCamera"
              :disabled="isStartButtonDisabled"
          >
            打开摄像头
          </button>
          <button
              @click="capturePhoto"
              :disabled="isCaptureButtonDisabled"
          >
            拍照
          </button>
        </div>
      </div>
    </div>

    <div class="register-button-container">
      <button
          @click="sendVerificationCode"
          :disabled="isVeriButtonDisabled"
      >
        {{ veriButtonText }}
      </button>
      <button @click="registerUser">注册</button>
      <button @click="goToLogin">前往登录</button>
    </div>

    <div
        class="message"
        :class="message.type"
        :style="{ display: message.show ? 'block' : 'none' }"
    >
      {{ message.text }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data () {
    return {
      username: '',
      password: '',
      email: '',
      veriCode: '',
      photoSrc: '',
      stream: null,
      showVideo: true,
      isStartButtonDisabled: false,
      isCaptureButtonDisabled: true,
      isVeriButtonDisabled: false,
      veriButtonText: '获取验证码',
      countdownTimer: null,
      message: {
        show: false,
        text: '',
        type: ''
      }
    }
  },
  methods: {
    /* 通用提示 */
    showMessage (text, type = 'info') {
      this.message = { show: true, text, type }
      setTimeout(() => (this.message.show = false), 3000)
    },

    /* 打开摄像头 */
    async startCamera () {
      try {
        this.showMessage('正在请求摄像头权限...', 'info')
        this.stream = await navigator.mediaDevices.getUserMedia({
          video: { width: 320, height: 240 },
          audio: false
        })
        this.$refs.video.srcObject = this.stream
        this.showMessage('摄像头已打开，可以拍照了', 'success')
        this.isStartButtonDisabled = true
        this.isCaptureButtonDisabled = false
      } catch (err) {
        this.showMessage('无法访问摄像头: ' + err.message, 'error')
      }
    },

    /* 拍照 */
    capturePhoto () {
      const video = this.$refs.video
      const canvas = this.$refs.canvas
      const ctx = canvas.getContext('2d')
      canvas.width = video.videoWidth
      canvas.height = video.videoHeight
      ctx.drawImage(video, 0, 0)
      this.photoSrc = canvas.toDataURL('image/png')
      this.showMessage('照片已拍摄，可以注册了', 'success')
      this.isCaptureButtonDisabled = true
      if (this.stream) {
        this.stream.getTracks().forEach(t => t.stop())
        this.stream = null
        this.isStartButtonDisabled = false
      }
      this.showVideo = false
    },

    /* 发送验证码 */
    async sendVerificationCode () {
      if (!this.email) {
        this.showMessage('请先填写邮箱地址', 'error')
        return
      }
      this.isVeriButtonDisabled = true
      this.veriButtonText = '发送中...'
      try {
        await this.$axios({
          method: 'POST',
          url: 'http://localhost:9999/user/mail',
          data: { email: this.email },
          headers: { 'Content-Type': 'application/json' }
        })
        this.showMessage('验证码已发送至您的邮箱，请查收！', 'success')
        this.startCountdown(60)
      } catch (e) {
        this.showMessage('发送验证码失败: ' + (e?.response?.data || e.message), 'error')
        this.isVeriButtonDisabled = false
        this.veriButtonText = '获取验证码'
      }
    },

    /* 倒计时 */
    startCountdown (seconds) {
      let count = seconds
      this.veriButtonText = `${count}秒后重发`
      this.countdownTimer = setInterval(() => {
        count--
        this.veriButtonText = `${count}秒后重发`
        if (count <= 0) {
          clearInterval(this.countdownTimer)
          this.isVeriButtonDisabled = false
          this.veriButtonText = '获取验证码'
        }
      }, 1000)
    },

    /* 用户注册 */
    async registerUser () {
      if (!this.username || !this.password || !this.email || !this.veriCode) {
        this.showMessage('请填写所有必填字段', 'error')
        return
      }
      if (!this.photoSrc) {
        this.showMessage('请上传人脸照片', 'error')
        return
      }
      const imageBase64 = this.photoSrc.split(',')[1]
      try {
        await this.$axios({
          method: 'POST',
          url: 'http://localhost:9999/user/register',
          data: {
            username: this.username,
            password: this.password,
            email: this.email,
            code: this.veriCode,
            imageBase64
          },
          headers: { 'Content-Type': 'application/json' }
        })
        this.showMessage('注册成功！即将跳转登录页...', 'success')
        setTimeout(() => this.$router.push('/login'), 1500)
      } catch (e) {
        this.showMessage('注册失败: ' + (e?.response?.data || e.message), 'error')
      }
    },

    /* 前往登录 */
    goToLogin () {
      this.$router.push('/login')
    }
  },

  beforeDestroy () {
    if (this.stream) this.stream.getTracks().forEach(t => t.stop())
    if (this.countdownTimer) clearInterval(this.countdownTimer)
  }
}
</script>

<style scoped lang="less">
body {
  font-family: Arial, sans-serif;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.main-layout {
  display: flex;
  justify-content: space-between;
}
.form-container {
  width: 50%;
  padding-right: 100px;
}
.input-group {
  margin-bottom: 15px;
}
.input-group label {
  display: block;
  margin-bottom: 5px;
}
.input-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.camera-container {
  margin: 20px 0;
  max-width: 640px;
}
.video {
  width: 100%;
  max-width: 640px;
  background-color: #000;
}
.canvas {
  display: none;
}
.photo {
  max-width: 640px;
  margin-top: 10px;
}
button {
  padding: 10px 15px;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
}
button:hover {
  background-color: #45a049;
}
button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
.message {
  margin-top: 15px;
  padding: 10px;
  border-radius: 4px;
}
.message.success {
  background-color: #dff0d8;
  color: #3c763d;
}
.message.error {
  background-color: #f2dede;
  color: #a94442;
}
.message.info {
  background-color: #d9edf7;
  color: #31708f;
}
.register-button-container {
  display: flex;
  align-items: center;
  margin-top: 20px;
}
</style>