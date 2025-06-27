<template>
  <view class="chat-container">
    <TUIKit
      :config="simpleConfig"
      :userID="userID"
      :userSig="userSig"
      @onMessageReceived="onMessageReceived"
      @onConversationListUpdated="onConversationListUpdated"
      @onError="onError"
    />
  </view>
</template>

<script>
import { genTestUserSig } from '../../debug/GenerateTestUserSig.js';

import TUIKit from '../ai-desk-customer-uniapp/components/CustomerServiceChat/index-uniapp.vue'; // Corrected import path

import TUICustomerServer from '../ai-desk-customer-uniapp/index';

export default {
  components: {
    TUIKit // Register TUIKit component
  },
  data() {
    return {
      userID: 'customer_' + Math.floor(Math.random() * 1000000), // 生成随机用户ID
      // 简化的 config
      simpleConfig: {
        sdkAppID: 1600093683, // 使用您的SDKAppID
        groupID: '61430', // 添加客服群组ID
        // 暂时移除其他配置
      }
    };
  },
  computed: {
    userSig() {
      return genTestUserSig(this.userID).userSig;
    }
  },
  methods: {
    // 收到新消息时的回调
    onMessageReceived(message) {
      console.log('收到新消息:', message);
      // 震动提醒
      uni.vibrateShort();

      // 播放提示音
      const innerAudioContext = uni.createInnerAudioContext();
      innerAudioContext.src = '/static/notification.mp3';
      innerAudioContext.play();
    },

    // 会话列表更新时的回调
    onConversationListUpdated(conversationList) {
      console.log('会话列表更新:', conversationList);
    },

    // 错误处理
    onError(error) {
      console.error('TUIKit错误:', error);
      uni.showToast({
        title: '连接异常，请稍后重试',
        icon: 'none',
        duration: 2000
      });
    }
  },
  onLoad() {
    // 页面加载时的初始化逻辑
    console.log('聊天页面已加载');

    // Initialize and login IM SDK
    TUICustomerServer.initWithProfile({
      SDKAppID: this.simpleConfig.sdkAppID,
      userID: this.userID,
      userSig: this.userSig,
    });

    // 检查网络状态
    uni.getNetworkType({
      success: (res) => {
        if (res.networkType === 'none') {
          uni.showToast({
            title: '网络连接已断开',
            icon: 'none',
            duration: 2000
          });
        }
      }
    });
  },
  onUnload() {
    // 页面卸载时的清理工作
    console.log('聊天页面已卸载');
  }
};
</script>

<style>
.chat-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

/* 自定义样式 */
:deep(.tui-chat-input) {
  background-color: #ffffff;
  border-top: 1px solid #e5e5e5;
}

:deep(.tui-message-list) {
  background-color: #f5f5f5;
  padding: 10rpx;
}

:deep(.tui-message-item) {
  margin: 10rpx 0;
}

:deep(.tui-message-content) {
  max-width: 70%;
  border-radius: 8rpx;
}

:deep(.tui-message-sender) {
  font-size: 24rpx;
  color: #999;
}

:deep(.tui-message-time) {
  font-size: 24rpx;
  color: #999;
}

:deep(.tui-message-status) {
  font-size: 24rpx;
  color: #999;
}

/* 客服相关样式 */
:deep(.tui-customer-service) {
  background-color: #ffffff;
  border-radius: 8rpx;
  padding: 20rpx;
  margin: 10rpx;
}

:deep(.tui-customer-service-title) {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

:deep(.tui-customer-service-content) {
  font-size: 26rpx;
  color: #666;
  margin-top: 10rpx;
}
</style> 