<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="title">我的收藏</text>
    </view>

    <!-- 瀑布流内容区 -->
    <view class="waterfall-container">
      <view class="waterfall-column left-column">
        <view 
          v-for="(item, index) in leftColumn" 
          :key="item.id || index"
          class="policy-card"
          @click="navigateToDetail(item.id)"
          :style="{ opacity: item.loaded ? 1 : 0 }"
          role="button"
          :aria-label="item.title + '，点击查看政策详情'"
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
          <!-- 这里不再显示收藏标签和按钮，因为已经是收藏列表 -->
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

      <view class="waterfall-column right-column">
        <view 
          v-for="(item, index) in rightColumn" 
          :key="item.id || index"
          class="policy-card"
          @click="navigateToDetail(item.id)"
          :style="{ opacity: item.loaded ? 1 : 0 }"
          role="button"
          :aria-label="item.title + '，点击查看政策详情'"
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
      <button class="retry-btn" @click="loadFavorites">重试</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      leftColumn: [],
      rightColumn: [],
      page: 1,
      pageSize: 10,
      loading: false,
      hasMore: true,
      error: false,
    }
  },

  onLoad() {
    this.loadFavorites();
  },

  onReachBottom() {
    if (!this.loading && this.hasMore) {
      this.loadFavorites();
    }
  },

  methods: {
    goBack() {
      uni.navigateBack();
    },
    async loadFavorites(reset = false) {
      const token = uni.getStorageSync('token');
      if (!token) {
        uni.showToast({ title: '请先登录才能查看收藏', icon: 'none' });
        this.error = true;
        this.hasMore = false; // 停止加载
        return;
      }

      if (this.loading || (!this.hasMore && !reset)) return;

      if (reset) {
        this.leftColumn = [];
        this.rightColumn = [];
        this.page = 1;
        this.hasMore = true;
        this.error = false;
      }

      this.loading = true;
      this.error = false;

      try {
        const url = `${this.$config.baseUrl}/policy/favorites`;
        console.log('请求我的收藏 URL:', url);
        const res = await uni.request({
          url: url,
          method: 'GET',
          data: {
            pageNum: this.page,
            pageSize: this.pageSize,
          },
          header: { 'Authorization': `Bearer ${token}` }
        });

        if (res.data.code !== 200) throw new Error(res.data.message || '接口错误');

        const newPolicies = res.data.data.records || [];
        if (newPolicies.length === 0) {
          this.hasMore = false;
          return;
        }

        newPolicies.forEach((policy) => {
          const card = { ...policy, loaded: true };
          if ((this.leftColumn.length + this.rightColumn.length) % 2 === 0) {
            this.leftColumn.push(card);
          } else {
            this.rightColumn.push(card);
          }
        });

        this.hasMore = newPolicies.length === this.pageSize;
        this.page++;
      } catch (error) {
        this.error = true;
        console.error('加载我的收藏失败:', error);
        uni.showToast({ title: '加载收藏失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    onImageLoad(id) {
      const updateColumn = (column) => {
        const index = column.findIndex(item => item.id === id);
        if (index !== -1) column[index].loaded = true;
      };
      updateColumn(this.leftColumn);
      updateColumn(this.rightColumn);
    },
    navigateToDetail(id) {
      uni.navigateTo({ url: `/pages/policy/detail?id=${id}` });
    },
    formatDate(timestamp) {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toISOString().split('T')[0];
    },
    formatViews(views) {
      return views >= 10000 ? `${(views / 10000).toFixed(1)}万` : views;
    }
  }
};
</script>

<style scoped>
/* 基本容器和头部样式 */
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
  border-bottom: 1rpx solid #eee;
  margin: -20rpx -20rpx 20rpx; /* 抵消父容器的padding */
  border-radius: 0 0 16rpx 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
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

/* 瀑布流布局 */
.waterfall-container {
  display: flex;
  gap: 20rpx;
}

.waterfall-column {
  width: 48%; /* 稍微留点空隙 */
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
  position: relative; /* 用于内部元素的定位 */
}

.policy-card.visible {
  opacity: 1;
  transform: translateY(0);
}

.card-cover {
  width: 100%;
  height: auto;
  aspect-ratio: 3/2; /* 保持图片比例 */
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
  -webkit-line-clamp: 2; /* 限制两行 */
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

/* 加载和错误状态 */
.loading-container {
  padding: 40rpx 0;
  text-align: center;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  color: #999;
  font-size: 28rpx;
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
</style> 