<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <image src="/static/logo.png" class="logo" />
      <text class="title">助盲通</text>
      <button class="voice-btn" @click="toggleVoiceGuide">
        {{ voiceEnabled ? "语音开启" : "语音关闭" }}
      </button>
    </view>

    <!-- 核心功能入口 -->
    <view class="main-features">
      <navigator 
        v-for="(item, index) in features" 
        :key="index"
        :url="item.path"
        class="feature-card"
        :style="{ backgroundColor: item.color }"
        hover-class="hover-effect"
      >
        <image :src="item.icon" class="feature-icon" />
        <text class="feature-title">{{ item.title }}</text>
        <text class="feature-desc">{{ item.desc }}</text>
      </navigator>
    </view>

 
    <!-- 紧急求助按钮 -->
    <button 
      class="emergency-btn"
      @click="makeEmergencyCall"
      :aria-label="'紧急求助'"
    >
      <image src="/static/sos-icon.png" class="sos-icon" />
      <text class="btn-text">紧急求助</text>
    </button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      voiceEnabled: true,
      features: [
        {
          title: "视频协助",
          desc: "志愿者实时视频帮助",
          icon: "/static/video-help.png",
          path: "/pages/index/spxz/spxz",
          color: "#FFB74D"
        },
        {
          title: "出行预约",
          desc: "地铁/机场无障碍服务",
          icon: "/static/travel.png",
          path: "/pages/index/spxz/spxz",
          color: "#81C784"
        },
        {
          title: "政策查询",
          desc: "最新补贴政策解读",
          icon: "/static/policy.png",
          path: "/pages/policy/policy",
          color: "#64B5F6"
        }
      ]
    };
  },
  computed: {
    voiceIcon() {
      return this.voiceEnabled ? "/static/voice-on.png" : "/static/voice-off.png";
    }
  },
  methods: {
    toggleVoiceGuide() {
      this.voiceEnabled = !this.voiceEnabled;
      uni.vibrateShort(); // 触觉反馈
    },
    makeEmergencyCall() {
      console.log('紧急求助按钮被点击');
      uni.makePhoneCall({ phoneNumber: "110" });
    },
    handleEmergency() {
      console.log('紧急求助按钮被点击');
    },
    confirmEmergency() {
      console.log('紧急求助按钮被双击');
    },
    speak(text) {
      if (this.voiceEnabled) {
        const innerAudioContext = uni.createInnerAudioContext();
        innerAudioContext.src = `https://tts-api.example.com/speak?text=${encodeURIComponent(text)}`;
        innerAudioContext.play();
      }
    },
    
  }
};
</script>

<style scoped>
/* 全局容器样式 */
.container {
  background: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 180rpx; /* 为底部按钮留出空间 */
}

/* 顶部导航栏 */
.header {
  padding: 30rpx;
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #1a237e, #0d47a1);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.logo {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.1);
}

.title {
  font-size: 40rpx;
  margin-left: 24rpx;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
}

/* 语音控制按钮 */
.voice-btn {
  margin-left: auto;
  padding: 12rpx 24rpx;
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
  border: 2rpx solid rgba(255, 255, 255, 0.2);
  border-radius: 32rpx;
  font-size: 28rpx;
  transition: all 0.3s ease;
}

.voice-btn:active {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(0.98);
}

/* 核心功能区域 */
.main-features {
  padding: 30rpx;
  display: grid;
  grid-template-columns: 1fr;
  gap: 24rpx;
}

.feature-card {
  padding: 40rpx 30rpx;
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6rpx;
  background: rgba(255, 255, 255, 0.2);
}

.feature-card:active {
  transform: translateY(2rpx);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.feature-icon {
  width: 96rpx;
  height: 96rpx;
  margin-bottom: 20rpx;
  transition: transform 0.3s ease;
}

.feature-card:active .feature-icon {
  transform: scale(0.95);
}

.feature-title {
  font-size: 34rpx;
  font-weight: 600;
  margin: 16rpx 0;
  color: #ffffff;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.1);
}

.feature-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
  line-height: 1.5;
}

/* 紧急求助按钮 */
.emergency-btn {
  position: fixed;
  bottom: 60rpx;
  right: 40rpx;
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff5252, #d32f2f);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 82, 82, 0.3);
  transition: all 0.3s ease;
  border: none;
  padding: 0;
}

.emergency-btn:active {
  transform: scale(0.95);
  box-shadow: 0 4rpx 12rpx rgba(255, 82, 82, 0.2);
}

.sos-icon {
  width: 48rpx;
  height: 48rpx;
  margin-bottom: 8rpx;
}

.btn-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.2);
}

/* 无障碍增强 */
button::after { 
  border: none; 
}

button[disabled] { 
  opacity: 0.6;
  cursor: not-allowed;
}

/* 暗色模式适配 */
@media (prefers-color-scheme: dark) {
  .container {
    background: #121212;
  }
  
  .feature-card {
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.2);
  }
  
  .feature-desc {
    color: rgba(255, 255, 255, 0.8);
  }
}

/* 动画效果 */
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 82, 82, 0.4);
  }
  70% {
    box-shadow: 0 0 0 20rpx rgba(255, 82, 82, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 82, 82, 0);
  }
}

.emergency-btn {
  animation: pulse 2s infinite;
}
</style>