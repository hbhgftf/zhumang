<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="back-btn" @click="goBack" aria-label="返回政策列表" tabindex="0">
        <text class="back-icon">←</text>
      </view>
      <text class="title">政策详情</text>
    </view>

    <!-- 政策内容 -->
    <view class="content" v-if="policy">
      <image 
        v-if="policy.cover" 
        :src="policy.cover" 
        mode="widthFix" 
        class="cover-image"
      />
      
      <view class="policy-header">
        <text class="policy-title">{{ policy.title }}</text>
        <view class="favorite-btn" @tap="toggleFavorite" :class="{ active: isFavorite }" :aria-label="isFavorite ? '取消收藏' : '收藏该政策'" tabindex="0">
          <text v-if="isFavorite">★ 已收藏</text>
          <text v-else>☆ 收藏</text>
        </view>
        <view class="meta-info">
          <text class="publish-date">发布时间：{{ formatDate(policy.publishDate) }}</text>
          <text class="region">地区：{{ policy.policyLevel }}</text>
        </view>
        <view class="tags" v-if="policy.tags && policy.tags.length">
          <text 
            v-for="(tag, index) in policy.tags" 
            :key="index"
            class="tag"
          >{{ tag }}</text>
        </view>
      </view>

      <view class="policy-content">
        <rich-text :nodes="policy.content"></rich-text>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading">
      <image src="/static/loading.gif" mode="widthFix" class="loading-icon" />
      <text>加载中...</text>
    </view>

    <!-- 错误状态 -->
    <view v-if="error" class="error-container">
      <image src="/static/error.png" mode="widthFix" class="error-icon" />
      <text class="error-text">加载失败，请检查网络</text>
      <button class="retry-btn" @click="loadPolicyDetail">重试</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      policy: null,
      loading: true,
      error: false,
      isFavorite: false
    }
  },

  onLoad(options) {
    if (options.id) {
      this.loadPolicyDetail(options.id)
      this.fetchFavoriteStatus(options.id)
    }
  },

  methods: {
    async loadPolicyDetail(id) {
      this.loading = true
      this.error = false

      try {
        const response = await uni.request({
          url: `${this.$config.baseUrl}/policy/detail/${id}`
        })

        const { data } = response
        if (data.code === 200) {
          this.policy = data.data
          this.fetchFavoriteStatus(id)
        }
      } catch (err) {
        this.error = true
        console.error('Failed to load policy detail:', err)
      } finally {
        this.loading = false
      }
    },
    async fetchFavoriteStatus(id) {
      try {
        const token = uni.getStorageSync('token');
        if (!token) return;
        const res = await uni.request({
          url: `${this.$config.baseUrl}/policy/favorite/${id}`,
          method: 'GET',
          header: { 'Authorization': `Bearer ${token}` }
        });
        if (res.data.code === 200) {
          this.isFavorite = !!res.data.data;
        }
      } catch (e) {
        // 忽略未登录或接口异常
      }
    },
    async toggleFavorite() {
      const token = uni.getStorageSync('token');
      if (!token) {
        uni.showToast({ title: '请先登录', icon: 'none' });
        return;
      }
      try {
        if (this.isFavorite) {
          await uni.request({
            url: `${this.$config.baseUrl}/policy/favorite/${this.policy.id}`,
            method: 'DELETE',
            header: { 'Authorization': `Bearer ${token}` }
          });
          this.isFavorite = false;
          uni.showToast({ title: '已取消收藏', icon: 'none' });
        } else {
          await uni.request({
            url: `${this.$config.baseUrl}/policy/favorite/${this.policy.id}`,
            method: 'POST',
            header: { 'Authorization': `Bearer ${token}` }
          });
          this.isFavorite = true;
          uni.showToast({ title: '已收藏', icon: 'none' });
        }
      } catch (e) {
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    },
    goBack() {
      uni.navigateBack()
    },
    formatDate(timestamp) {
      const date = new Date(timestamp)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #fff;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  padding: 20rpx;
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #eee;
}

.back-btn {
  padding: 20rpx;
  margin-right: 20rpx;
}

.back-icon {
  font-size: 40rpx;
  color: #333;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.content {
  padding: 30rpx;
}

.cover-image {
  width: 100%;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
}

.policy-header {
  margin-bottom: 40rpx;
}

.policy-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
  margin-bottom: 20rpx;
}

.meta-info {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 28rpx;
  margin-bottom: 20rpx;
}

.region {
  margin-left: 20rpx;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.tag {
  background: #FFE7BA;
  color: #FA8C16;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.policy-content {
  font-size: 32rpx;
  line-height: 1.8;
  color: #333;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60rpx 0;
}

.loading-icon {
  width: 40rpx;
  height: 40rpx;
  margin-right: 10rpx;
}

.error-container {
  text-align: center;
  padding: 60rpx 0;
}

.error-icon {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 20rpx;
}

.error-text {
  color: #666;
  font-size: 28rpx;
  margin-bottom: 20rpx;
}

.retry-btn {
  background: #FA8C16;
  color: #fff;
  padding: 16rpx 40rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.favorite-btn {
  display: inline-flex;
  align-items: center;
  font-size: 32rpx;
  color: #FFD700;
  margin-left: 20rpx;
  cursor: pointer;
  user-select: none;
}
.favorite-btn.active {
  color: #FFD700;
}
</style> 