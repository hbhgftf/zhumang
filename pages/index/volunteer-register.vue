<!-- 志愿者注册页面 -->
<template>
  <view class="register-container">
    <view class="form-header">
      <text class="header-title">成为志愿者</text>
      <text class="header-subtitle">让我们一起帮助他人</text>
    </view>

    <view class="form-content">
      <view class="form-item">
        <text class="label">姓名</text>
        <input type="text" v-model="formData.name" placeholder="请输入您的真实姓名" />
      </view>

      <view class="form-item">
        <text class="label">手机号码</text>
        <input 
          type="number" 
          v-model="formData.phone" 
          placeholder="请输入手机号码" 
          maxlength="11"
        />
      </view>

      <view class="form-item">
        <text class="label">身份证号</text>
        <input type="idcard" v-model="formData.idCard" placeholder="请输入身份证号码" />
      </view>

      <button class="submit-btn" @tap="submitForm">提交申请</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      formData: {
        name: '',
        phone: '',
        idCard: '',
        openid: ''
      }
    }
  },
  onLoad() {
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo) {
      this.formData.openid = userInfo.openid
      console.log(this.formData.openid)
    } else {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/mine/'
        })
      }, 1500)
    }
  },
  methods: {
    async submitForm() {
      // 表单验证
      if (!this.formData.name || !this.formData.phone || !this.formData.idCard) {
        uni.showToast({
          title: '请填写完整信息',
          icon: 'none'
        })
        return
      }

      // 打印表单数据到控制台
      console.log('志愿者注册表单数据：', {
        姓名: this.formData.name,
        手机号码: this.formData.phone,
        身份证号: this.formData.idCard,
        openid: this.formData.openid
      })

      try {
        // 调用注册接口
        console.log('开始请求注册接口...')
        const res = await uni.request({
          url: `${this.$config.baseUrl}/user/user/volunteer/register`,
          method: 'POST',
          data: this.formData,
          header: {
            'content-type': 'application/json'
          }
        })
        
        console.log('接口返回数据：', res)
        
        if (res.data.success) {
          uni.showToast({
            title: '申请提交成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          console.error('注册失败，错误信息：', res.data)
          uni.showToast({
            title: res.data.message || '提交失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('请求发生错误：', error)
        console.error('错误详情：', {
          错误信息: error.message,
          错误堆栈: error.stack,
          请求数据: this.formData
        })
        uni.showToast({
          title: '提交失败，请重试',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
.register-container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.form-header {
  text-align: center;
  margin-bottom: 40rpx;
}

.header-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}

.header-subtitle {
  font-size: 28rpx;
  color: #666;
}

.form-content {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
  display: block;
}

input {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #eee;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  background: #fff;
}

.submit-btn {
  background: #1AAD19;
  color: white;
  border-radius: 40rpx;
  margin-top: 40rpx;
  font-size: 32rpx;
}
</style> 