<template>
  <view class="container">
    <!-- 顶部背景 -->
    <view class="top-bg"></view>
    
    <!-- 基本信息表单 -->
    <view class="form-container">
      <view class="form-group">
        <text class="label">昵称</text>
        <input type="nickname" class="input" v-model="userInfo.nickname" placeholder="请输入昵称" aria-label="昵称" tabindex="0" />
      </view>
      
      <view class="form-group">
        <text class="label">姓名</text>
        <input type="text" class="input" v-model="userInfo.username" placeholder="请输入用户名" aria-label="姓名" tabindex="0" />
      </view>
      
      <view class="form-group">
        <text class="label">手机号</text>
        <input type="number" class="input" v-model="userInfo.phone" placeholder="请输入手机号" aria-label="手机号" tabindex="0" />
      </view>
      
      <view class="form-group">
        <text class="label">常用地址</text>
        <input type="text" class="input" v-model="userInfo.usualAddress" placeholder="请输入常用地址" aria-label="常用地址" tabindex="0" />
      </view>
      
      
      <!-- 志愿者专属信息 -->
      <block v-if="isVolunteer">
        <view class="form-group">
          <text class="label">语音设置</text>
          <input type="text" class="input" v-model="userInfo.voiceSettings" placeholder="请输入语音设置" aria-label="语音设置" tabindex="0" />
        </view>
        
        <view class="form-group">
          <text class="label">服务时长</text>
          <input type="number" class="input" v-model="userInfo.serviceHours" placeholder="请输入服务时长" aria-label="服务时长" tabindex="0" />
        </view>
        
        <view class="form-group">
          <text class="label">服务评分</text>
          <input type="number" class="input" v-model="userInfo.serviceRating" placeholder="请输入服务评分" aria-label="服务评分" tabindex="0" />
        </view>
        
        <view class="form-group">
          <text class="label">所属组织</text>
          <input type="text" class="input" v-model="userInfo.organization" placeholder="请输入所属组织" aria-label="所属组织" tabindex="0" />
        </view>
        
        <view class="form-group">
          <text class="label">认证时间</text>
          <input type="text" class="input" v-model="userInfo.certificationTime" placeholder="请输入认证时间" aria-label="认证时间" tabindex="0" />
        </view>
      </block>
     
    </view>
    
    <!-- 保存按钮 -->
    <view class="button-container">
      <button class="save-btn" @tap="handleSave" aria-label="保存基本信息" tabindex="0">保存修改</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      isVolunteer: false, // 新增：是否为志愿者
      userInfo: {
        id: '',
        nickname: '',
        name: '',
        phone: '',
        usualAddress: '',
        voiceSettings: '',
        serviceHours: '',
        serviceRating: '',
        organization: '',
        certificationTime: '',
        username: ''
      }
    };
  },
  
  onLoad() {
    this.checkVolunteerStatus(); // 检查志愿者状态
    this.loadUserInfo();
  },
  
  onShow() {
    // 每次页面显示时重新检查状态
    this.checkVolunteerStatus()
  },
  
  methods: {
    // 新增：检查志愿者状态
    checkVolunteerStatus() {
      const userInfo = uni.getStorageSync('userInfo');
      if (userInfo && userInfo.role === '志愿者') {
        this.isVolunteer = true;
      } else {
        this.isVolunteer = false;
      }
    },
    
    // 加载用户信息
    async loadUserInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo');
        console.log('存储的用户信息:', userInfo); // 添加日志
        
        if (!userInfo) {
          throw new Error('未找到用户信息，请先登录');
        }
        
        if (!userInfo.id) {
          console.error('用户信息中缺少ID:', userInfo);
          throw new Error('用户信息不完整，请重新登录');
        }
        
        const token = uni.getStorageSync('token');
        console.log('存储的token:', token); // 添加日志
        
        if (!token) {
          throw new Error('未找到登录凭证，请重新登录');
        }
        
        const res = await uni.request({
          url: `${this.$config.baseUrl}/user/getUserInfo?id=${userInfo.id}`,
          method: 'GET',
          header: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        console.log('获取用户信息响应:', res); // 添加日志
        
        if (res.data.code === 200) {
          // 只更新存在的字段，避免覆盖可能未返回的role等
          this.userInfo = { ...this.userInfo, ...res.data.data.userInfo };
          console.log('更新后的用户信息:', this.userInfo); // 添加日志
        } else {
          throw new Error(res.data.message || '获取用户信息失败');
        }
      } catch (error) {
        console.error('加载用户信息失败:', error);
        // 如果是未登录错误，跳转到登录页面
        if (error.message.includes('未找到用户信息') || error.message.includes('未找到登录凭证')) {
          uni.showModal({
            title: '提示',
            content: '请先登录',
            showCancel: false,
            success: () => {
              uni.switchTab({
                url: '/pages/index/mine'
              });
            }
          });
        } else {
          uni.showToast({ 
            title: error.message || '加载失败，请重试', 
            icon: 'none' 
          });
        }
      }
    },
    
    // 保存修改
    async handleSave() {
      try {
        const userInfo = uni.getStorageSync('userInfo');
        if (!userInfo || !userInfo.id) {
          throw new Error('用户未登录');
        }
        
        const token = uni.getStorageSync('token');
        if (!token) {
          throw new Error('未找到登录凭证');
        }
        
        // 确保id存在
        this.userInfo.id = userInfo.id;
        
        const res = await uni.request({
          url: `${this.$config.baseUrl}/user/updateUserInfo`,
          method: 'POST',
          data: this.userInfo,
          header: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (res.data.code === 200) {
          uni.showToast({ 
            title: '保存成功', 
            icon: 'success' 
          });
          // 更新本地存储中可能改变的字段
          uni.setStorageSync('userInfo', {
            ...userInfo,
            ...this.userInfo
          });
        } else {
          throw new Error(res.data.message || '保存失败');
        }
      } catch (error) {
        console.error('保存失败:', error);
        uni.showToast({ 
          title: error.message || '保存失败，请重试', 
          icon: 'none' 
        });
      }
    }
  }
};
</script>

<style>
.container {
  min-height: 100vh;
  background-color: #f8f9fa;
  position: relative;
}

.top-bg {
  height: 240rpx;
  background: linear-gradient(135deg, #1a237e 0%, #0d47a1 100%);
  position: relative;
  overflow: hidden;
}

.top-bg::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background: linear-gradient(to bottom, transparent, rgba(255, 255, 255, 0.1));
}

.form-container {
  margin: -120rpx 30rpx 0;
  padding: 40rpx 30rpx;
  background-color: #fff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1;
}

.form-group {
  margin-bottom: 36rpx;
  position: relative;
}

.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
  font-weight: 500;
  transition: all 0.3s ease;
}

.input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  background: #f8f9fa;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 30rpx;
  color: #333;
  transition: all 0.3s ease;
}

.input:focus {
  border-color: #1a237e;
  background: #fff;
  box-shadow: 0 0 0 4rpx rgba(26, 35, 126, 0.1);
}

.input::placeholder {
  color: #999;
}

/* 志愿者专属信息样式 */
.form-group.volunteer {
  background: #f5f7ff;
  padding: 24rpx;
  border-radius: 12rpx;
  margin-top: 40rpx;
}

.form-group.volunteer .label {
  color: #1a237e;
}

.form-group.volunteer .input {
  background: #fff;
}

/* 保存按钮容器 */
.button-container {
  padding: 40rpx 30rpx;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  z-index: 2;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #1a237e 0%, #0d47a1 100%);
  color: #fff;
  font-size: 32rpx;
  font-weight: 500;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  border: none;
}

.save-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

/* 动画效果 */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-group {
  animation: slideUp 0.3s ease forwards;
  opacity: 0;
}

.form-group:nth-child(1) { animation-delay: 0.1s; }
.form-group:nth-child(2) { animation-delay: 0.2s; }
.form-group:nth-child(3) { animation-delay: 0.3s; }
.form-group:nth-child(4) { animation-delay: 0.4s; }
.form-group:nth-child(5) { animation-delay: 0.5s; }
.form-group:nth-child(6) { animation-delay: 0.6s; }

/* 暗色模式适配 */
@media (prefers-color-scheme: dark) {
  .container {
    background-color: #121212;
  }
  
  .form-container {
    background-color: #1e1e1e;
  }
  
  .label {
    color: #e0e0e0;
  }
  
  .input {
    background: #2d2d2d;
    border-color: #404040;
    color: #e0e0e0;
  }
  
  .input:focus {
    border-color: #7986cb;
    background: #2d2d2d;
    box-shadow: 0 0 0 4rpx rgba(121, 134, 203, 0.2);
  }
  
  .input::placeholder {
    color: #666;
  }
  
  .form-group.volunteer {
    background: #1a1a1a;
  }
  
  .form-group.volunteer .label {
    color: #7986cb;
  }
  
  .button-container {
    background: #1e1e1e;
  }
}

/* 无障碍增强 */
.input:focus {
  outline: none;
}

.save-btn:focus {
  outline: 4rpx solid #7986cb;
  outline-offset: 2rpx;
}

/* 错误状态 */
.input.error {
  border-color: #f44336;
  background: #fff5f5;
}

.error-message {
  font-size: 24rpx;
  color: #f44336;
  margin-top: 8rpx;
}
</style> 