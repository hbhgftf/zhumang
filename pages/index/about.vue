<template>
  <view class="about-container">
    <text class="about-title">关于我们</text>
    <view class="about-content">
      <text>
        本软件是一款专为视障及有特殊需求用户打造的无障碍服务平台。
        我们致力于为用户提供便捷的志愿者视频协助、出行预约、政策查询、常见问题解答和人工客服等功能。
        软件支持语音提示、触觉反馈，并对所有操作区块进行了无障碍优化，确保TalkBack、旁白等读屏工具可顺畅使用。
        欢迎提出宝贵意见，让我们一起让世界更有温度！
      </text>
      <view class="about-section">
        <text class="about-subtitle">主要功能：</text>
        <text>1. 视频协助：一键呼叫志愿者，获得实时帮助。</text>
        <text>2. 出行预约：提前预约志愿者协助出行。</text>
        <text>3. 政策查询：查阅最新补贴政策和相关信息。</text>
        <text>4. 常见问题：快速获取常见问题解答。</text>
        <text>5. 人工客服：遇到难题可随时联系客服。</text>
        <text>6. 全面无障碍：支持读屏、语音、触觉反馈。</text>
      </view>
      <view class="about-section">
        <text class="about-subtitle">联系我们：</text>
        <text>如有建议或合作意向，请通过客服入口与我们联系。</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'AboutPage'
}
</script>

<style scoped>
.about-container {
  background: #f8f9fa;
  min-height: 100vh;
  padding: 40rpx;
}
.about-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #2196F3;
  margin-bottom: 30rpx;
  display: block;
}
.about-content {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05);
}
.about-section {
  margin-top: 30rpx;
}
.about-subtitle {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 10rpx;
  display: block;
}
.about-content text {
  display: block;
  margin-bottom: 10rpx;
  color: #444;
  font-size: 28rpx;
}
</style> 