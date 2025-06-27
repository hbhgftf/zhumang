<template>
  <view class="faq-list-container">
    <text class="section-title">全部常见问题</text>
    <view 
      v-for="(item, index) in faqs" 
      :key="item.id || index"
      class="faq-item"
      @click="toggleAnswer(index)"
      role="button"
      :aria-label="item.question + (item.expanded ? '，已展开答案' : '，点击展开答案')"
      tabindex="0"
    >
      <view class="question-block">
        <text class="question-text">{{ item.question }}</text>
        <view class="arrow" :class="{ expanded: item.expanded }"></view>
      </view>
      <view v-if="item.expanded" class="answer-block">
        {{ item.answer }}
      </view>
    </view>
    <view class="contact-support">
      <text>无法解决您的问题？</text>
      <button class="contact-btn" @click="goToCustomerService" aria-label="联系客服" tabindex="0">
        联系客服
      </button>
    </view>
  </view>
</template>

<script>
import config from '../../config.js';
export default {
  data() {
    return {
      faqs: []
    }
  },
  onLoad() {
    this.fetchFaqs();
  },
  methods: {
    fetchFaqs() {
      uni.request({
        url: `${config.baseUrl}/faq/all`,
        method: 'GET',
        success: (res) => {
          if (res.statusCode === 200 && res.data.code === 200) {
            this.faqs = res.data.data.map(item => ({
              ...item,
              expanded: false
            }));
          }
        }
      });
    },
    toggleAnswer(index) {
      this.faqs[index].expanded = !this.faqs[index].expanded;
    },
    goToCustomerService() {
      uni.navigateTo({ url: '/pages-ai-desk/index/index' });
    }
  }
}
</script>

<style scoped>
.faq-list-container {
  background: #f8f9fa;
  min-height: 100vh;
  padding: 30rpx;
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
.contact-support {
  margin-top: 60rpx;
  text-align: center;
}
.contact-btn {
  margin-top: 20rpx;
  background: #2196F3;
  color: #fff;
  border: none;
  border-radius: 44rpx;
  padding: 20rpx 60rpx;
  font-size: 28rpx;
}
</style> 