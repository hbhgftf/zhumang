<template>
  <view class="feedback-container">
    <text class="feedback-title">我要反馈</text>
    <view class="feedback-form">
      <textarea v-model="content" class="feedback-textarea" placeholder="请输入您的反馈内容" maxlength="500" aria-label="请输入您的反馈内容" tabindex="0" />
      <input v-model="contact" class="feedback-input" placeholder="联系方式（可选）" aria-label="联系方式，可选" tabindex="0" />
      <button class="submit-btn" @click="submitFeedback" :disabled="submitting">{{ submitting ? '提交中...' : '提交反馈' }}</button>
    </view>
  </view>
</template>

<script>
import config from '../../config.js';
export default {
  data() {
    return {
      content: '',
      contact: '',
      submitting: false
    }
  },
  methods: {
    submitFeedback() {
      if (!this.content.trim()) {
        uni.showToast({ title: '请填写反馈内容', icon: 'none' });
        return;
      }
      this.submitting = true;
      uni.request({
        url: `${config.baseUrl}/user/manage/feedback`,
        method: 'POST',
        header: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${uni.getStorageSync('token')}`
        },
        data: {
          content: this.content,
          contact: this.contact
        },
        success: (res) => {
          if (res.statusCode === 200 && res.data.code === 200) {
            uni.showToast({ title: '反馈提交成功', icon: 'success' });
            this.content = '';
            this.contact = '';
          } else {
            uni.showToast({ title: res.data.msg || '提交失败', icon: 'none' });
          }
        },
        fail: () => {
          uni.showToast({ title: '网络错误', icon: 'none' });
        },
        complete: () => {
          this.submitting = false;
        }
      });
    }
  }
}
</script>

<style scoped>
.feedback-container {
  background: #f8f9fa;
  min-height: 100vh;
  padding: 40rpx;
}
.feedback-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #2196F3;
  margin-bottom: 30rpx;
  display: block;
}
.feedback-form {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05);
}
.feedback-textarea {
  width: 100%;
  min-height: 160rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
  background: #f5f5f5;
}
.feedback-input {
  width: 100%;
  height: 80rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  margin-bottom: 30rpx;
  background: #f5f5f5;
}
.submit-btn {
  width: 100%;
  height: 88rpx;
  background: #2196F3;
  color: #fff;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
}
.submit-btn:disabled {
  background: #90caf9;
}
</style> 