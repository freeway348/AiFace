<template>
  <div>
    <div class="login-container">
      <h1 class="page-title">用户信息注册系统</h1>

      <div class="content-wrapper">
        <!--左侧表单-->
        <div class="form-panel">
          <label class="form-label">用户名</label>
          <input v-model="username" placeholder="请输入用户名" class="form-input" />

          <label class="form-label">密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" class="form-input" />

          <label class="form-label">邮箱</label>
          <input v-model="email" type="email" placeholder="请输入邮箱" class="form-input" />

          <label class="form-label">验证码</label>
          <input v-model="veriCode" placeholder="请输入验证码" class="form-input" />

          <!--表单按钮-->
          <div class="form-actions">
            <button @click="sendVerificationCode" :disabled="isVeriButtonDisabled">
              {{ veriButtonText }}
            </button>
            <button @click="registerUser">注册</button>

            <button @click="goToLogin" class="text-btn">前往登录</button>
          </div>
        </div>

        <!--摄像头-->
        <div class="camera-panel">
          <video
              v-show="showVideo"
              ref="video"
              autoplay
              playsinline
              class="video"
          ></video>
          <canvas ref="canvas" style="display: none"></canvas>
          <img v-if="photoSrc" :src="photoSrc" class="photo" alt="photo" />

          <div class="camera-buttons">
            <button @click="startCamera" :disabled="isStartButtonDisabled">
              打开摄像头
            </button>
            <button @click="capturePhoto" :disabled="isCaptureButtonDisabled">
              拍照
            </button>
          </div>
        </div>
      </div>

      <!--消息提示-->
      <div
          v-show="message.show"
          class="message"
          :style="{ display: message.show ? 'block' : 'none' }"
      >
        {{ message.text }}
      </div>
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
    showMessage (text, type = 'info') {
      this.message = { show: true, text, type }
      setTimeout(() => (this.message.show = false), 3000)
    },

    // 打开摄像头
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

    // 拍照
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


    sendVerificationCode () {
      if (!this.email) {
        this.showMessage('请先填写邮箱地址', 'error')
        return
      }
      this.isVeriButtonDisabled = true
      this.veriButtonText = '发送中...'

      this.$axios({
        method: 'POST',
        url: 'http://localhost:9999/user/mail',
        data: { email: this.email },
        headers: { 'Content-Type': 'application/json' }
      }).then(() => {
        this.showMessage('验证码已发送至您的邮箱，请查收！', 'success')
        this.startCountdown(60)
      }).catch(e => {
        this.showMessage('发送验证码失败: ' + (e?.response?.data || e.message), 'error')
        this.isVeriButtonDisabled = false
        this.veriButtonText = '获取验证码'
      })
    },

    // 倒计时
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

    registerUser() {
      if (!this.username || !this.password || !this.email || !this.veriCode) {
        this.showMessage('请填写所有必填字段', 'error')
        return
      }
      if (!this.photoSrc) {
        this.showMessage('请上传人脸照片', 'error')
        return
      }
      const imageBase64 = this.photoSrc.split(',')[1]

      this.$axios({
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
      }).then(res => {
        console.log(res);
        // 如果后端传来的res为"fail"，则输出注册失败
        if (res.data === "fail") {
          this.showMessage('注册失败: ' + (e?.response?.data || e.message), 'error')
        }else{
          this.showMessage('注册成功！即将跳转登录页...', 'success')
          setTimeout(() => this.$router.push('/login'), 1500)
        }
      }).catch(e => {
        this.showMessage('注册失败: ' + (e?.response?.data || e.message), 'error')
      })
    },


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
  background: #f5f5f5;
}

.login-container {
  max-width: 800px;
  margin: 50px auto;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

// 标题居中
.page-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 26px;
  color: #333;
}

// 左右两栏
.content-wrapper {
  display: flex;
  gap: 40px;
}

.form-panel {
  flex: 1 1 320px;
}

.form-label {
  display: block;
  margin-bottom: 5px;
  font-weight: normal;
}

.form-input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  margin-bottom: 20px;
}

.form-input:last-child {
  margin-bottom: 0;
}

// 表单底部按钮区
.form-actions {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.camera-panel {
  flex: 1 1 320px;
  text-align: center;
}

.video,
.photo {
  width: 100%;
  max-width: 320px;
  background: #000;
  border-radius: 4px;
}

.camera-buttons {
  margin-top: 15px;
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
</style>