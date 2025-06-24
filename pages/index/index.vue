<template>
  
  <class class="container">
    <!-- 语音控制条 -->
    <view class="voice-control">
      <text class="section-title" @click="playIntroduction" aria-label="点击播放使用说明" tabindex="0" role="button">语音说明</text>
      <view class="voice-btns">
        <button 
          class="voice-btn"
          @click="playIntroduction"
          aria-label="播放使用说明"
          tabindex="0"
          role="button"
        >
          <image src="/static/play.png" class="btn-icon" alt="播放指南" />
          播放指南
        </button>
        <button 
          class="voice-btn"
          @click="repeatLastMessage"
          aria-label="重复提示"
          tabindex="0"
          role="button"
        >
          <image src="/static/repeat.png" class="btn-icon" alt="重复提示" />
          重复提示
        </button>
      </view>
    </view>

    <!-- 常见问题 -->
    <view class="faq-section">
      <view style="display: flex; justify-content: space-between; align-items: center;">
        <text class="section-title">常见问题</text>
        <text class="more-btn" @click="goToFaqList" aria-label="查看更多常见问题" tabindex="0" role="button">更多</text>
      </view>
      <view 
        v-for="(item, index) in faqs.slice(0, 2)" 
        :key="index"
        class="faq-item"
        @click="toggleAnswer(index)"
        role="button"
        :aria-label="item.question + (item.expanded ? '，已展开答案' : '，点击展开答案')"
        tabindex="0"
      >
        <!-- 问题区块 -->
        <view class="question-block" aria-label="问题：{{item.question}}" tabindex="0">
          <text class="question-text">{{ item.question }}</text>
          <view class="arrow" :class="{ expanded: item.expanded }"></view>
        </view>
        
        <!-- 答案区块 -->
        <view v-if="item.expanded" class="answer-block" aria-label="答案：{{item.answer}}" tabindex="0">
          {{ item.answer }}
        </view>
      </view>
    </view>

    <!-- 联系客服 -->
    <view class="contact-section">
      <text class="section-title">需要人工帮助？</text>
      <button 
        class="contact-btn"
        @click="makeCall"
        aria-label="拨打求助热线"
        tabindex="0"
        role="button"
      >
        <image src="/static/phone.png" class="btn-icon" alt="拨打求助热线" />
        拨打求助热线
      </button>
      <button 
        class="contact-btn"
        @click="openChat"
        aria-label="在线即时沟通"
        tabindex="0"
        role="button"
      >
        <image src="/static/chat.png" class="btn-icon" alt="在线即时沟通" />
        在线即时沟通
      </button>
    </view>
  </class>
</template>

<script>
export default {
  data() {
    return {
      faqs: [
        {
          question: "如何申请志愿者帮助？",
          answer: "在首页点击'视频协助'按钮，系统将自动匹配在线志愿者...",
          expanded: false
        },
        {
          question: "地铁预约需要提前多久？",
          answer: "建议至少提前2小时预约，紧急情况可致电客服...",
          expanded: false
        }
      ]
    };
  },
  methods: {
    toggleAnswer(index) {
      this.faqs[index].expanded = !this.faqs[index].expanded;
      uni.vibrateShort(); // 触觉反馈
    },
    goToFaqList() {
      uni.navigateTo({
        url: '/pages/index/faq-list'
      });
    },
    playIntroduction() {
      const audio = uni.createInnerAudioContext();
      audio.src = '/static/1750342160925.mp3';
      audio.play();
      audio.onError((res) => {
        uni.showToast({ title: '音频播放失败', icon: 'none' });
        console.error('音频播放错误:', res);
      });
    },
    repeatLastMessage() {
      // 重复最后一条系统消息
    },
    makeCall() {
      console.log('拨打求助热线按钮被点击');
      uni.makePhoneCall({ phoneNumber: "110" });
    },
    openChat() {
      uni.navigateTo({ url: "/pages-ai-desk/index/index" });
    }
  }
};
</script>

<style scoped>
/* 全局容器样式 */
.container {
  background: #f8f9fa;
  min-height: 100vh;
  padding: 30rpx;
}

/* 语音控制区域 */
.voice-control {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.voice-btns {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.voice-btn {
  flex: 1;
  height: 88rpx;
  background: #4CAF50;
  color: white;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  transition: all 0.3s ease;
  border: none;
}

.voice-btn:active {
  transform: scale(0.98);
  background: #43A047;
}

/* 常见问题区域 */
.faq-section {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 36rpx;
  color: #333333;
  font-weight: 600;
  display: block;
  margin-bottom: 30rpx;
  position: relative;
  padding-left: 24rpx;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 8rpx;
  height: 32rpx;
  background: #4CAF50;
  border-radius: 4rpx;
}

.faq-item {
  background: #ffffff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  border: 2rpx solid #e0e0e0;
  transition: all 0.3s ease;
}

.faq-item:active {
  transform: scale(0.99);
}

.question-block {
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-text {
  font-size: 30rpx;
  color: #333333;
  font-weight: 500;
  flex: 1;
  margin-right: 20rpx;
}

.arrow {
  width: 20rpx;
  height: 20rpx;
  border-right: 3rpx solid #666;
  border-bottom: 3rpx solid #666;
  transform: rotate(-45deg);
  transition: transform 0.3s ease;
}

.arrow.expanded {
  transform: rotate(135deg);
}

.answer-block {
  padding: 30rpx;
  background: #f5f5f5;
  color: #666666;
  font-size: 28rpx;
  line-height: 1.6;
  border-top: 2rpx solid #e0e0e0;
  animation: slideDown 0.3s ease;
}

/* 联系客服区域 */
.contact-section {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  margin-top: 30rpx; /* Adjusted margin */
}

.contact-btn {
  width: 100%;
  height: 88rpx;
  margin: 20rpx 0; /* Adjusted margin */
  background: #2196F3;
  color: white;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  transition: all 0.3s ease;
  border: none;
}

.contact-btn:active {
  transform: scale(0.98);
  background: #1976D2;
}

.btn-icon {
  width: 40rpx;
  height: 40rpx;
  margin-right: 10rpx;
}

/* 动画效果 */
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 无障碍焦点状态 */
button:focus, navigator:focus {
  outline: 4rpx solid #4CAF50;
  outline-offset: 2rpx;
}

/* 暗色模式适配 */
@media (prefers-color-scheme: dark) {
  .container,
  .voice-control,
  .faq-section,
  .contact-section {
    background-color: #1e1e1e;
    color: #e0e0e0;
  }

  .section-title {
    color: #e0e0e0;
  }

  .faq-item {
    border-color: #333333;
  }

  .question-text {
    color: #e0e0e0;
  }

  .arrow {
    border-right-color: #999;
    border-bottom-color: #999;
  }

  .answer-block {
    background: #2d2d2d;
    color: #bbbbbb;
    border-top-color: #333333;
  }

  .voice-btn {
    background: #388E3C;
  }

  .voice-btn:active {
    background: #2E7D32;
  }

  .contact-btn {
    background: #1565C0;
  }

  .contact-btn:active {
    background: #0D47A1;
  }
}

.more-btn {
  font-size: 26rpx;
  color: #2196F3;
  padding: 0 10rpx;
  cursor: pointer;
}
</style>