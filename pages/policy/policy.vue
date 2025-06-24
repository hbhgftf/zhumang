<template>
  <view class="container">
    <!-- 地区选择器 -->
    <view class="header">
      <picker 
        mode="selector" 
        :range="regions" 
        @change="onRegionChange" 
        class="region-picker"
      >
        <view class="picker-content">
          <text class="selected-region">{{ currentRegion }}</text>
          <text class="picker-arrow">▼</text>
        </view>
      </picker>
      <text class="my-favorites-btn" @click="navigateToFavorites">
        我的收藏
      </text>
    </view>

    <!-- 单列内容区 -->
    <view class="policy-list">
      <view 
        v-for="item in policyList" 
        :key="item.id"
        class="policy-card"
        @click="navigateToDetail(item.id)"
        :style="{ opacity: item.loaded ? 1 : 0 }"
        role="button"
        :aria-label="item.title + '，点击查看详情'"
        tabindex="0"
      >
        <image 
          v-if="item.cover" 
          :src="item.cover" 
          mode="widthFix"
          class="card-cover"
          loading="lazy"
          @load="onImageLoad(item.id)"
        />
        <view class="card-tag" v-if="item.tags && item.tags.length">
          {{ item.tags[0] }}
        </view>
        <view class="card-content">
          <text class="card-title">{{ item.title }}</text>
          <text class="card-summary">{{ item.summary }}</text>
          <view class="card-footer">
            <text class="publish-date">{{ formatDate(item.publishDate) }}</text>
            <text class="views">{{ formatViews(item.views) }}阅读</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading || !hasMore">
      <view v-if="loading" class="loading">
        <image src="/static/loading.gif" mode="widthFix" class="loading-icon" />
        <text>加载中...</text>
      </view>
      <text v-else-if="!hasMore" class="no-more">没有更多数据了</text>
    </view>

    <!-- 错误状态 -->
    <view v-if="error" class="error-container">
      <image src="/static/error.png" mode="widthFix" class="error-icon" />
      <text class="error-text">加载失败，请检查网络</text>
      <button class="retry-btn" @click="loadPolicies">重试</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      regions: ['全国', '北京', '上海', '广州', '深圳'],
      currentRegion: '全国',
      policyList: [],
      page: 1,
      pageSize: 10,
      loading: false,
      hasMore: true,
      error: false,
      searchTitle: '',
      favoriteMap: {},
    }
  },

  onLoad() {
    this.loadPolicies();
  },

  onReachBottom() {
    if (!this.loading && this.hasMore) {
      this.loadPolicies();
    }
  },

  methods: {
    async loadPolicies(reset = false) {
      if (this.loading || (!this.hasMore && !reset)) return;
      if (reset) {
        this.policyList = [];
        this.page = 1;
        this.hasMore = true;
        this.error = false;
      }
      this.loading = true;
      this.error = false;
      try {
        // 构造查询参数
        const params = {
          pageNum: this.page,
          pageSize: this.pageSize,
        };
        if (this.currentRegion && this.currentRegion !== '全国') {
          params.policyLevel = this.currentRegion;
        }
        if (this.searchTitle) {
          params.title = this.searchTitle;
        }
        const url = `${this.$config.baseUrl}/policy/page`;
        const res = await uni.request({
          url: url,
          method: 'GET',
          data: params,
        });
        if (res.data.code !== 200) throw new Error(res.data.message || '接口错误');
        const newPolicies = res.data.data.records || [];
        if (newPolicies.length === 0) {
          this.hasMore = false;
          return;
        }
        await Promise.all(newPolicies.map(item => this.fetchFavoriteStatus(item.id)));
        newPolicies.forEach((policy) => {
          const card = { ...policy, loaded: true };
          this.policyList.push(card);
        });
        this.hasMore = newPolicies.length === this.pageSize;
        this.page++;
      } catch (error) {
        this.error = true;
      } finally {
        this.loading = false;
      }
    },

    async fetchFavoriteStatus(policyId) {
      try {
        const token = uni.getStorageSync('token');
        if (!token) return;
        const url = `${this.$config.baseUrl}/policy/${policyId}/favorite`;
        console.log('请求收藏状态 URL:', url);
        const res = await uni.request({
          url: url,
          method: 'GET',
          header: { 'Authorization': `Bearer ${token}` }
        });
        if (res.data.code === 200) {
          this.$set(this.favoriteMap, policyId, !!res.data.data);
        }
      } catch (e) {
        // 忽略未登录或接口异常
      }
    },

    onRegionChange(e) {
      this.currentRegion = this.regions[e.detail.value];
      this.policyList = [];
      this.page = 1;
      this.hasMore = true;
      this.error = false;
      this.loadPolicies(true);
    },

    onImageLoad(id) {
      const index = this.policyList.findIndex(item => item.id === id);
      if (index !== -1) this.policyList[index].loaded = true;
    },

    navigateToDetail(id) {
      uni.navigateTo({ url: `/pages/policy/detail?id=${id}` });
    },

    formatDate(timestamp) {
      const date = new Date(timestamp);
      return date.toISOString().split('T')[0];
    },

    formatViews(views) {
      return views >= 10000 ? `${(views / 10000).toFixed(1)}万` : views;
    },

    navigateToFavorites() {
      uni.navigateTo({ url: '/pages/policy/favorites' });
    },
  }
};
</script>

<style scoped>
.container {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  padding: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid #eee;
  margin: -20rpx -20rpx 20rpx;
  border-radius: 0 0 16rpx 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.region-picker {
  width: 100%;
  max-width: 300rpx;
}

.picker-content {
  display: flex;
  align-items: center;
}

.selected-region {
  font-size: 32rpx;
  font-weight: 500;
  color: #333333;
}

.picker-arrow {
  font-size: 28rpx;
  color: #999999;
}

.policy-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.policy-card {
  background: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(20rpx);
  will-change: opacity, transform;
}

.policy-card.visible {
  opacity: 1;
  transform: translateY(0);
}

.card-cover {
  width: 100%;
  height: auto;
  aspect-ratio: 3/2;
  object-fit: cover;
}

.card-tag {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  padding: 4rpx 12rpx;
  background: rgba(255, 215, 0, 0.1);
  color: #FFD700;
  font-size: 24rpx;
  border-radius: 20rpx;
}

.card-content {
  padding: 20rpx;
}

.card-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1A1A1A;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-summary {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #999999;
}

.loading-container {
  padding: 40rpx 0;
  text-align: center;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
}

.loading-icon {
  width: 40rpx;
  height: 40rpx;
  animation: spin 1s linear infinite;
}

.no-more {
  font-size: 28rpx;
  color: #999999;
}

.error-container {
  padding: 80rpx 20rpx;
  text-align: center;
}

.error-icon {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 30rpx;
}

.error-text {
  font-size: 32rpx;
  color: #666666;
  margin-bottom: 20rpx;
}

.retry-btn {
  padding: 18rpx 60rpx;
  background: #FF5252;
  color: #ffffff;
  border-radius: 28rpx;
  font-size: 30rpx;
  font-weight: 500;
  transition: opacity 0.3s;
}

.retry-btn:active {
  opacity: 0.8;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.my-favorites-btn {
  font-size: 28rpx;
  color: #007aff;
  padding: 10rpx 20rpx;
  border-radius: 30rpx;
  background-color: #e6f2ff;
  margin-left: auto;
}
</style>