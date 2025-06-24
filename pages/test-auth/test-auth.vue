<template>
  <view class="container">
    <button @click="handleAuth" aria-label="测试授权" tabindex="0">测试授权</button>
  </view>
</template>

<script>
export default {
  methods: {
    async handleAuth() {
      try {
        const { userInfo } = await uni.getUserProfile({
          desc: "测试授权",
          lang: "zh_CN"
        });
        console.log('测试用户信息:', userInfo);
      } catch (error) {
        console.error('测试授权错误:', error);
      }
    }
  }
};
</script>

<style>
.container {
  padding: 20px;
}
</style>