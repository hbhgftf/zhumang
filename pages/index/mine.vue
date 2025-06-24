<template>
  <view class="container">
    <!-- 顶部背景 -->
    <view class="top-bg"></view>
    
    <!-- 头像及登录状态 -->
    <view class="box">
      <view class="head-box">
        <view class="avatar-container" @tap="chooseAvatar">
          <image 
            class="head-img" 
            :src="login.avatar" 
            mode="aspectFill"
          />
        </view>
        <view class="tip">{{ login.show ? '点击头像可更换' : '当前未登录，请登录！' }}</view>
        <!-- 昵称输入框 -->
        <view class="nickname-container" v-if="login.show">
          <input 
            type="nickname" 
            class="nickname-input" 
            placeholder="请输入昵称"
            :value="login.nickname"
            @change="handleNicknameChange"
          />
        </view>
      </view>
      
      <!-- 登录/注册按钮 -->
      <view class="login-section" v-if="!login.show">
        <!-- 登录方式切换条 -->
        <view class="login-tabs">
          <view :class="['tab-item', loginTab === 0 ? 'active' : '']" @tap="loginTab = 0">验证码登录</view>
          <view :class="['tab-item', loginTab === 1 ? 'active' : '']" @tap="loginTab = 1">密码登录</view>
        </view>
        
        <!-- 验证码登录表单 -->
        <view v-if="loginTab === 0" class="email-login">
          <input 
            type="text" 
            class="email-input" 
            placeholder="请输入邮箱"
            v-model="emailForCode"
          />
          <view class="code-input-group">
            <input 
              type="text" 
              class="code-input" 
              placeholder="请输入验证码"
              v-model="code"
            />
            <button 
              class="send-code-btn" 
              :disabled="isSendingCode"
              @tap="handleSendCode"
            >
              {{ isSendingCode ? `${countdown}s后重试` : '发送验证码' }}
            </button>
          </view>
          <button @tap="handleEmailLogin" class="email-login-btn">
            <text class="login-text">邮箱验证码登录</text>
          </button>
        </view>
        
        <!-- 密码登录表单 -->
        <view v-if="loginTab === 1" class="password-login">
          <input 
            type="text" 
            class="email-input" 
            placeholder="请输入邮箱"
            v-model="email"
          />
          <input 
            type="password" 
            class="email-input" 
            placeholder="请输入密码"
            v-model="password"
          />
          <button @tap="handlePasswordLogin" class="email-login-btn">
            <text class="login-text">密码登录</text>
          </button>
        </view>
        
        <!-- 分割线 -->
        <view class="divider">
          <view class="divider-line"></view>
          <text class="divider-text">或</text>
          <view class="divider-line"></view>
        </view>
        
        <!-- 微信登录按钮 -->
        <button @tap="handleAuth" class="login-btn">
          <text class="login-text">微信快捷登录</text>
        </button>
      </view>
      
      <!-- 功能列表 -->
      <view class="function-list" v-if="login.show">
        <!-- 管理员功能入口 -->
        <button class="row" v-if="isAdmin" @tap="navigateTo('/pages/admin/index')">
          <view class="left">
            <icon class="icon-small" type="success" size="16" color="green" />
            <text class="text">管理后台</text>
          </view>
          <view class="right">》</view>
        </button>
        <!-- 其他功能入口 -->
        <button class="row" @tap="navigateTo('/pages/policy/favorites')">
          <view class="left">
            <icon class="icon-small" type="success" size="16" color="green" />
            <text class="text">我的收藏</text>
          </view>
          <view class="right">》</view>
        </button>
        <button class="row" @tap="navigateTo('/pages/basicInfo/basicInfo')">
          <view class="left">
            <icon class="icon-small" type="success" size="16" color="green" />
            <text class="text">基本信息</text>
          </view>
          <view class="right">》</view>
        </button>
        <button class="row" @tap="navigateTo('feedback')">
          <view class="left">
            <icon class="icon-small" type="success" size="16" color="green" />
            <text class="text">我要反馈</text>
          </view>
          <view class="right">》</view>
        </button>
        <button class="row" style="width: 100%;" @tap="navigateTo('about')">
          <view class="left">
            <icon class="icon-small" type="success" size="16" color="green" />
            <text class="text">关于我们</text>
          </view>
          <view class="right">》</view>
        </button>
        <button class="row" style="width: 100%;" open-type="contact" @tap="navigateToCustomerService">
          <view class="left">
            <icon class="icon-small" type="success" size="16" color="green" />
            <text class="text">在线客服</text>
          </view>
          <view class="right">》</view>
        </button>
      </view>
      
      <!-- 退出登录按钮 -->
      <view class="logout-section" v-if="login.show">
        <button @tap="handleLogout" class="logout-btn">
          <text class="logout-text">退出登录</text>
        </button>
      </view>
    </view>
    
    <!-- 页脚 -->
    <view class="footer">
      <text>© 2025 公益助盲个人中心</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      login: {
        show: false,        // 是否已登录
        avatar: '/static/default_avatar.png', // 默认头像
        nickname: ''        // 存储昵称
      },
      userId: '',           // 用户ID
      tempLocalPath: '',    // 本地临时文件路径（下载后的头像路径）
      loading: false,       // 加载状态
      email: '',           // 密码登录邮箱
      password: '',        // 密码
      emailForCode: '',    // 验证码登录邮箱
      code: '',            // 验证码
      isSendingCode: false, // 是否正在发送验证码
      countdown: 60,        // 验证码倒计时
      isAdmin: false,      // 是否为管理员
      loginTab: 0          // 0: 验证码登录, 1: 密码登录
    };
  },

  onLoad() {
    // 恢复登录状态
    const userInfo = uni.getStorageSync('userInfo');
    const token = uni.getStorageSync('token');
    console.log('页面加载：从Storage读取UserInfo:', userInfo);
    console.log('页面加载：从Storage读取Token:', token);

    if (userInfo && userInfo.id) {
      this.login.show = true;
      this.userId = userInfo.id;
      this.login.avatar = userInfo.avatarUrl || this.login.avatar;
      this.login.nickname = userInfo.nickname || '';
      this.isAdmin = userInfo.role === 'admin' || userInfo.role === '管理员';
    }
    // 添加日志，检查onLoad处理后的组件状态
    console.log('onLoad处理后：login.show =', this.login.show);
    console.log('onLoad处理后：isAdmin =', this.isAdmin);
  },

  methods: {
    // 密码登录
    async handlePasswordLogin() {
      if (!this.email || !this.password) {
        uni.showToast({ title: '请输入邮箱和密码', icon: 'none' });
        return;
      }
      
      if (this.loading) return;
      this.loading = true;
      uni.showLoading({ title: '登录中...' });
      
      try {
        const loginResponse = await uni.request({
          url: `${this.$config.baseUrl}/user/login/password`,
          method: 'POST',
          data: {
            email: this.email,
            password: this.password
          },
          header: {
            'Content-Type': 'application/json'
          }
        });
        
        // 根据后端实际返回的成功状态码进行判断
        if (loginResponse.data.code === 200) {
          const token = loginResponse.data.data.userInfo.token; // 修正token路径
          const userInfoToSave = loginResponse.data.data.userInfo; // 获取要保存的实际用户信息对象
          console.log('登录返回的原始数据:', loginResponse.data.data); // 打印原始 data 对象
          console.log('即将保存的Token:', token);
          console.log('即将保存的UserInfo对象:', userInfoToSave);
          
          // 保存用户信息和token
          uni.setStorageSync('token', token);
          uni.setStorageSync('userInfo', userInfoToSave);
          console.log('密码登录成功：已保存Token和用户信息到Storage。');
          console.log('Storage中的Token:', uni.getStorageSync('token'));
          console.log('Storage中的UserInfo:', uni.getStorageSync('userInfo'));
          
          // 更新组件状态
          this.login.show = true;
          this.login.avatar = userInfoToSave.avatarUrl || '/static/default_avatar.png';
          this.login.nickname = userInfoToSave.nickname || '默认昵称';
          this.isAdmin = userInfoToSave.role === 'admin' || userInfoToSave.role === '管理员';
          
          // 如果是管理员，显示提示
          if (this.isAdmin) {
            uni.showToast({ 
              title: '管理员登录成功',
              icon: 'success'
            });
          } else {
            uni.showToast({
              title: '登录成功',
              icon: 'success'
            });
          }
          // 导航到个人中心页面，或者刷新当前页面状态
          // 暂时不做跳转，让用户看到状态变化
        } else {
          uni.showToast({
            title: loginResponse.data.message || '登录失败，请检查邮箱或密码', // 更改提示信息，更具体
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('密码登录请求失败:', error);
        uni.showToast({
          title: '网络或服务器错误，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.loading = false;
        uni.hideLoading();
      }
    },
    
    // 邮箱验证码登录
    async handleEmailLogin() {
      if (!this.emailForCode || !this.code) {
        uni.showToast({ title: '请输入邮箱和验证码', icon: 'none' });
        return;
      }
      
      if (this.loading) return;
      this.loading = true;
      uni.showLoading({ title: '登录中...' });
      
      try {
        const loginResponse = await uni.request({
          url: `${this.$config.baseUrl}/user/login/email`,
          method: 'POST',
          data: {
            email: this.emailForCode,
            code: this.code
          },
          header: {
            'Content-Type': 'application/json'
          }
        });
        
        if (loginResponse.data.code === 200 && loginResponse.data.success) {
          const token = loginResponse.data.data.userInfo.token; // 修正token路径
          const userInfoToSave = loginResponse.data.data.userInfo; // 获取要保存的实际用户信息对象
          console.log('邮箱登录返回的原始数据:', loginResponse.data.data); // 打印原始 data 对象
          console.log('即将保存的Token:', token);
          console.log('即将保存的UserInfo对象:', userInfoToSave);

          // 保存用户信息和token
          uni.setStorageSync('token', token);
          uni.setStorageSync('userInfo', userInfoToSave);
          console.log('邮箱验证码登录成功：已保存Token和用户信息到Storage。');
          console.log('Storage中的Token:', uni.getStorageSync('token'));
          console.log('Storage中的UserInfo:', uni.getStorageSync('userInfo'));
          
          // 更新组件状态
          this.login.show = true;
          this.login.avatar = userInfoToSave.avatarUrl || '/static/default_avatar.png';
          this.login.nickname = userInfoToSave.nickname || '默认昵称';
          this.isAdmin = userInfoToSave.role === 'admin' || userInfoToSave.role === '管理员';
          
          // 清空输入
          this.emailForCode = '';
          this.code = '';
          
          uni.showToast({ title: '登录成功', icon: 'none' });
        } else {
          uni.showToast({ 
            title: loginResponse.data.msg || '登录失败', 
            icon: 'none' 
          });
        }
      } catch (error) {
        console.error('登录失败:', error);
        uni.showToast({ title: '登录请求失败，请重试', icon: 'none' });
      } finally {
        this.loading = false;
        uni.hideLoading();
      }
    },
    
    // 发送验证码
    async handleSendCode() {
      if (!this.emailForCode) {
        uni.showToast({ title: '请输入邮箱', icon: 'none' });
        return;
      }
      
      // 验证邮箱格式
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.emailForCode)) {
        uni.showToast({ title: '请输入正确的邮箱格式', icon: 'none' });
        return;
      }
      
      if (this.isSendingCode) return;
      
      this.isSendingCode = true;
      this.countdown = 60;
      
      try {
        const response = await uni.request({
          url: `${this.$config.baseUrl}/user/sendEmail/${this.emailForCode}`,
          method: 'GET'
        });
        
        if (response.data === "验证码发送成功！") {
          uni.showToast({ title: '验证码已发送', icon: 'none' });
          this.startCountdown();
        } else {
          uni.showToast({ title: response.data, icon: 'none' });
          this.isSendingCode = false;
        }
      } catch (error) {
        console.error('发送验证码失败:', error);
        uni.showToast({ title: '发送验证码失败', icon: 'none' });
        this.isSendingCode = false;
      }
    },
    
    // 开始倒计时
    startCountdown() {
      const timer = setInterval(() => {
        if (this.countdown > 0) {
          this.countdown--;
        } else {
          clearInterval(timer);
          this.isSendingCode = false;
        }
      }, 1000);
    },
    
    // 微信登录
    async handleAuth() {
      if (this.loading) return;
      this.loading = true;
      uni.showLoading({ title: '登录中...' });
    
      try {
        const { code } = await uni.login({ provider: 'weixin' });
        const loginRes = await uni.request({
          url: `${this.$config.baseUrl}/user/login`,
          method: 'POST',
          data: { code },
          header: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
    
        if (loginRes.data.code === 200) {
          const userInfo = loginRes.data.data.userInfo;
    
          // 直接存储后端返回的用户信息
          const userInfoToSave = userInfo; // 微信登录直接使用userInfo变量
          uni.setStorageSync('userInfo', userInfoToSave);
          uni.setStorageSync('token', loginRes.data.data.token); // 确保微信登录也保存token
          console.log('微信登录成功：已保存Token和用户信息到Storage。');
          console.log('Storage中的Token:', uni.getStorageSync('token'));
          console.log('Storage中的UserInfo:', uni.getStorageSync('userInfo'));
    
          // 更新组件状态
          this.login.show = true;
          this.userId = userInfoToSave.id;
          this.login.avatar = userInfoToSave.avatarUrl || '/static/default_avatar.png';
          this.login.nickname = userInfoToSave.nickname || '默认昵称';
          
          uni.showToast({ title: '登录成功', icon: 'none' });
        } else {
          uni.showToast({ 
            title: `登录失败：${loginRes.data.msg || '未知错误'}`, 
            icon: 'none' 
          });
        }
      } catch (error) {
        console.error('登录失败:', error);
        uni.showToast({ title: '登录请求失败，请重试', icon: 'none' });
      } finally {
        this.loading = false;
        uni.hideLoading();
      }
    },
    
    // 选择头像（本地相册选择）
    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          if (res.tempFilePaths && res.tempFilePaths.length > 0) {
            const tempFilePath = res.tempFilePaths[0];
            this.login.avatar = tempFilePath; // 先本地预览

            // 自动上传到服务器
            uni.showLoading({ title: '上传中...' });
            try {
              const token = uni.getStorageSync('token');
              if (!token) throw new Error('未找到登录凭证');
              // 1. 上传到 minio
              const uploadRes = await uni.uploadFile({
                url: `${this.$config.baseUrl}/minio/upload`,
                filePath: tempFilePath,
                name: 'file',
                header: { 'Authorization': `Bearer ${token}` }
              });
              if (uploadRes.statusCode === 200) {
                const result = JSON.parse(uploadRes.data);
                if (result.code === 200) {
                  const avatarUrl = result.data.url;
                  // 2. 保存头像URL到用户信息
                  const userInfo = uni.getStorageSync('userInfo');
                  await this.updateUserInfo({
                    id: userInfo.id,
                    avatarUrl
                  });
                } else {
                  throw new Error(result.message || '上传失败');
                }
              } else {
                throw new Error('上传失败，状态码:' + uploadRes.statusCode);
              }
            } catch (err) {
              uni.showToast({ title: err.message || '头像上传失败', icon: 'none' });
            } finally {
              uni.hideLoading();
            }
          }
        },
        fail: () => {
          uni.showToast({ title: '未选择图片', icon: 'none' });
        }
      });
    },

    // 处理昵称变化
    async handleNicknameChange(e) {
      if (!this.login.show) {
        uni.showToast({ title: '请先登录', icon: 'none' });
        return;
      }
      
      const nickname = e.detail.value;
      if (nickname && nickname.trim() !== '') {
        const userInfo = uni.getStorageSync('userInfo');
        await this.updateUserInfo({ 
          id: userInfo.id,
          nickname: nickname.trim(),
          avatarUrl: userInfo.avatarUrl || this.login.avatar
        });
      }
    },

    // 通用更新方法
    async updateUserInfo(params) {
      try {
        const userInfo = uni.getStorageSync('userInfo');
        if (!userInfo || !userInfo.id) {
          throw new Error('用户未登录或ID缺失');
        }

        const token = uni.getStorageSync('token');
        if (!token) {
          throw new Error('未找到登录凭证');
        }

        // 确保必填参数存在
        const requestData = {
          id: params.id || userInfo.id,
          avatarUrl: params.avatarUrl || userInfo.avatarUrl || this.login.avatar,
          ...params
        };

        const updateRes = await uni.request({
          url: `${this.$config.baseUrl}/user/updateUserInfo`,
          method: 'POST',
          data: requestData,
          header: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (updateRes.data.code === 200) {
          // 更新本地存储和组件状态
          const updatedUserInfo = {
            ...userInfo,
            ...params
          };
          uni.setStorageSync('userInfo', updatedUserInfo);
          
          if (params.nickname) {
            this.login.nickname = params.nickname;
          }
          if (params.avatarUrl) {
            this.login.avatar = params.avatarUrl;
          }
          uni.showToast({ title: '更新成功', icon: 'none' });
        } else {
          throw new Error(updateRes.data.message || '更新失败');
        }
      } catch (error) {
        console.error('更新失败:', error);
        uni.showToast({ title: error.message, icon: 'none' });
      }
    },

    // 导航功能
    navigateTo(page) {
      if(page === 'about') {
        uni.navigateTo({ url: '/pages/index/about' });
      } else if(page === 'feedback') {
        uni.navigateTo({ url: '/pages/index/feedback' });
      } else if(page === 'basicInfo') {
        uni.navigateTo({ url: '/pages/basicInfo/basicInfo' });
      } else {
        uni.navigateTo({ url: page });
      }
    },

    // 退出登录
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            // 清除所有登录相关信息
            uni.removeStorageSync('userInfo');
            uni.removeStorageSync('token');
            
            // 重置组件状态
            this.login.show = false;
            this.login.avatar = '/static/default_avatar.png';
            this.login.nickname = '';
            this.userId = '';
            this.email = '';
            this.password = '';
            this.emailForCode = '';
            this.code = '';
            this.isAdmin = false;
            
            uni.showToast({ title: '已退出登录', icon: 'none' });
          }
        }
      });
    },

    navigateToCustomerService() {
      uni.navigateTo({ url: '/pages-ai-desk/index/index' });
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

/* 顶部背景 */
.top-bg {
  height: 300rpx;
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

/* 主要内容区域 */
.box {
  margin: -120rpx 30rpx 0;
  padding: 40rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1;
}

/* 头像区域 */
.head-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 0 40rpx;
}

.avatar-container {
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto 20rpx auto;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.head-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.tip {
  font-size: 26rpx;
  color: #666;
  margin: 16rpx 0;
}

/* 昵称输入框 */
.nickname-container {
  width: 100%;
  max-width: 400rpx;
  margin-top: 16rpx;
}

.nickname-input {
  width: 100%;
  height: 72rpx;
  background: #f8f9fa;
  border: 2rpx solid #e0e0e0;
  border-radius: 36rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
  color: #333;
  text-align: center;
  transition: all 0.3s ease;
}

.nickname-input:focus {
  border-color: #1a237e;
  background: #fff;
  box-shadow: 0 0 0 4rpx rgba(26, 35, 126, 0.1);
}

/* 登录区域 */
.login-section {
  padding: 30rpx 0;
}

.login-tabs {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 30rpx;
  background: #f5f5f5;
  border-radius: 44rpx;
  overflow: hidden;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 30rpx;
  color: #666;
  background: transparent;
  transition: background 0.2s, color 0.2s;
  cursor: pointer;
}
.tab-item.active {
  background: linear-gradient(135deg, #1a237e 0%, #0d47a1 100%);
  color: #fff;
}

.password-login {
  margin-bottom: 30rpx;
}

.email-input,
.code-input {
  width: 100%;
  height: 88rpx;
  background: #f8f9fa;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 20rpx;
  transition: all 0.3s ease;
}

.code-input-group {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  width: 240rpx;
  height: 88rpx;
  background: #1a237e;
  color: #fff;
  font-size: 26rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.send-code-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.send-code-btn[disabled] {
  background: #ccc;
  opacity: 0.7;
}

/* 分割线 */
.divider {
  display: flex;
  align-items: center;
  margin: 40rpx 0;
}

.divider-line {
  flex: 1;
  height: 2rpx;
  background: #e0e0e0;
}

.divider-text {
  padding: 0 30rpx;
  color: #999;
  font-size: 26rpx;
}

/* 登录按钮 */
.login-btn,
.email-login-btn {
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
  margin-bottom: 20rpx;
}

.login-btn:active,
.email-login-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

/* 功能列表 */
.function-list {
  margin-top: 40rpx;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 0;
  background: none;
  border: none;
  border-bottom: 2rpx solid #f0f0f0;
  transition: all 0.3s ease;
}

.row:active {
  background: #f8f9fa;
}

.left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.icon-small {
  width: 40rpx;
  height: 40rpx;
  color: #1a237e;
}

.text {
  font-size: 30rpx;
  color: #333;
}

.right {
  color: #999;
  font-size: 28rpx;
}

/* 退出登录按钮 */
.logout-section {
  margin-top: 60rpx;
  padding: 0 30rpx;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  background: #f5f5f5;
  color: #f44336;
  font-size: 32rpx;
  font-weight: 500;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  border: none;
}

.logout-btn:active {
  transform: scale(0.98);
  background: #ffebee;
}

/* 页脚 */
.footer {
  text-align: center;
  padding: 40rpx 0;
  color: #999;
  font-size: 24rpx;
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

.function-list .row {
  animation: slideUp 0.3s ease forwards;
  opacity: 0;
}

.function-list .row:nth-child(1) { animation-delay: 0.1s; }
.function-list .row:nth-child(2) { animation-delay: 0.2s; }
.function-list .row:nth-child(3) { animation-delay: 0.3s; }
.function-list .row:nth-child(4) { animation-delay: 0.4s; }
.function-list .row:nth-child(5) { animation-delay: 0.5s; }

/* 暗色模式适配 */
@media (prefers-color-scheme: dark) {
  .container {
    background-color: #121212;
  }
  
  .box {
    background-color: #1e1e1e;
  }
  
  .tip {
    color: #999;
  }
  
  .nickname-input,
  .email-input,
  .code-input {
    background: #2d2d2d;
    border-color: #404040;
    color: #e0e0e0;
  }
  
  .nickname-input:focus,
  .email-input:focus,
  .code-input:focus {
    border-color: #7986cb;
    background: #2d2d2d;
    box-shadow: 0 0 0 4rpx rgba(121, 134, 203, 0.2);
  }
  
  .row {
    border-bottom-color: #2d2d2d;
  }
  
  .row:active {
    background: #2d2d2d;
  }
  
  .text {
    color: #e0e0e0;
  }
  
  .right {
    color: #666;
  }
  
  .logout-btn {
    background: #2d2d2d;
  }
  
  .logout-btn:active {
    background: #3d2d2d;
  }
}

/* 无障碍增强 */
button:focus {
  outline: 4rpx solid #7986cb;
  outline-offset: 2rpx;
}

input:focus {
  outline: none;
}

.input, .nickname-input, .form-input, input, textarea {
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
}
</style>