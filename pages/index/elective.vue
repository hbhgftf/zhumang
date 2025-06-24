<!-- 模板部分 -->
<template>
  <view class="volunteer-container">
    <!-- 非志愿者显示注册按钮 -->
    <view v-if="!isVolunteer" class="register-section">
      <view class="register-header">
        <text class="register-title">成为志愿者</text>
        <text class="register-subtitle">加入我们，一起帮助他人</text>
      </view>
      <button class="register-btn" @tap="goToRegister">成为志愿者</button>
    </view>

    <!-- 志愿者显示服务内容 -->
    <block v-else>
    <!-- 状态控制栏 -->
    <view class="status-bar">
      <text class="status-title">服务状态</text>
        <!-- 真实的uni-switch 组件 -->
        <switch 
          :checked="isOnline" 
          @change="toggleOnlineStatus" 
          color="#1AAD19"
          class="status-switch"
        />
        <text class="status-text">{{ isOnline ? '在线' : '离线' }}</text>
    </view>

    <!-- 服务范围选择 -->
    <view class="service-scope">
      <text class="section-title">您提供的帮助类型</text>
      <view class="tag-container">
        <view class="tag mock-tag active" @tap="goToVideoAssist" role="button" aria-label="视频协助，点击进入视频协助页面" tabindex="0">视频协助</view>
        <view class="tag mock-tag active" @tap="goToTravelAppointment" role="button" aria-label="出行预约，点击进入出行预约页面" tabindex="0">出行预约</view>
        <view class="tag mock-tag active" role="button" aria-label="政策咨询，点击进入政策咨询页面" tabindex="0">政策咨询</view>
      </view>
    </view>

    <!-- 通话请求弹窗 -->
    <view v-if="showCallRequest" class="call-request-modal">
      <view class="call-request-content">
        <view class="call-request-header">
          <text class="call-request-title">收到通话请求</text>
          <text class="call-request-subtitle">用户 {{callRequestInfo.callerId}} 请求与您通话</text>
        </view>
        <view class="call-request-buttons">
          <button class="call-btn reject" @tap="handleRejectCall" aria-label="拒绝通话" tabindex="0">拒绝</button>
          <button class="call-btn accept" @tap="handleAcceptCall" aria-label="接受通话" tabindex="0">接受</button>
        </view>
      </view>
    </view>

   
    <!-- 任务列表 -->
    <view class="task-list">
      <text class="section-title">感谢您提供帮助，如有人需要您的帮助，我们会弹窗通知您	        
	  若您接到视频协助，请耐心回复，
	  若您接到出行预约，请您问清楚出行站点，出行时间，个人信息，
	  然后在地铁/机场官网联系工作人员预约</text>

			

    </view>
    </block>
  </view>
</template>

<script>
export default {
  data() {
    return {
      isVolunteer: false,
      isOnline: false,
      // 新增通话相关数据
      wsConnection: null,
      showCallRequest: false,
      isInCall: false,
      callRequestInfo: {
        roomId: '',
        callerId: '',
        calleeId: ''
      },
      heartbeatTimer: null
    }
  },
  onLoad() {
    this.checkVolunteerStatus()
    // 如果是志愿者，初始化WebSocket连接
    if (this.isVolunteer) {
      this.initWebSocket()
    }
  },
  onShow() {
    // 每次页面显示时重新检查状态
    this.checkVolunteerStatus()
  },
  onUnload() {
    // 清理资源
    if (this.wsConnection) {
      this.wsConnection.close()
    }
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
    }
  },
  methods: {
    // 检查志愿者状态并获取在线状态
    async checkVolunteerStatus() {
      const userInfo = uni.getStorageSync('userInfo')
      
      if (!userInfo || typeof userInfo === 'string') {
         // 如果userInfo不存在或不是对象，则不是志愿者且无法获取openid
        this.isVolunteer = false
        this.isOnline = false // 未登录或信息异常，设置为离线
        return
      }

      // 检查角色
      if (userInfo.role === '志愿者') {
        this.isVolunteer = true
        // 如果是志愿者，则获取在线状态
        try {
          const res = await uni.request({
            url: `${this.$config.baseUrl}/api/volunteer/status/${userInfo.id}`,
            method: 'GET',
            header: {'Authorization': `Bearer ${userInfo.token}`}
          });

          if (res.data.code === 200) {
            this.isOnline = res.data.data; // 后端返回的是布尔值
          } else {
            console.error('获取志愿者在线状态失败:', res.data.msg);
            // 获取失败，可以给用户提示或将状态设为默认离线
            this.isOnline = false;
             uni.showToast({
              title: res.data.msg || '获取状态失败',
              icon: 'none'
            });
          }
        } catch (error) {
          console.error('请求获取志愿者在线状态失败:', error);
          this.isOnline = false;
           uni.showToast({
            title: '请求获取状态失败',
            icon: 'none'
          });
        }

      } else {
        this.isVolunteer = false
        this.isOnline = false // 不是志愿者，设置为离线
      }
    },

    // 切换志愿者在线状态
    async toggleOnlineStatus(event) {
       const userInfo = uni.getStorageSync('userInfo')
       if (!userInfo || userInfo.role !== '志愿者') {
         uni.showToast({ title: '您不是志愿者', icon: 'none' });
         // 回滚开关状态
         this.$nextTick(() => { this.isOnline = !event.detail.value; });
         return;
       }

      const newStatus = event.detail.value; // 开关的新状态 (true/false)
      const userId = userInfo.id; // 获取userId

      uni.showLoading({ title: '更新状态中...' });

      // 修改为将userId和isOnline作为URL参数发送
      const url = `${this.$config.baseUrl}/api/volunteer/status?userId=${userId}&isOnline=${newStatus}`;
      
      console.log('请求URL:', url); // 打印完整的请求URL
      console.log('请求数据(作为URL参数):', { userId, isOnline: newStatus }); // 打印参数数据

      try {
        const res = await uni.request({
          url: url,
          method: 'POST', // 保持POST方法，但数据通过URL参数发送
          // data: {}, // 无需发送请求体
           header: {
               'Authorization': `Bearer ${userInfo.token}`
               // 对于URL参数，Content-Type通常不是application/json
            }
        });

        if (res.data.code === 200) {
          this.isOnline = newStatus; // 成功后更新前端状态
          uni.showToast({ title: '状态更新成功', icon: 'success' });
        } else {
          console.error('更新志愿者在线状态失败:', res.data.msg);
          // 更新失败，回滚开关状态
          this.$nextTick(() => { this.isOnline = !newStatus; });
           uni.showToast({ 
             title: res.data.msg || '更新状态失败', 
             icon: 'none' 
           });
        }
      } catch (error) {
        console.error('请求更新志愿者在线状态失败:', error);
        // 请求失败，回滚开关状态
        this.$nextTick(() => { this.isOnline = !newStatus; });
         uni.showToast({ 
           title: '请求更新状态失败', 
           icon: 'none' 
         });
      } finally {
        uni.hideLoading();
      }
    },

    goToRegister() {
      // 检查是否登录
      const userInfo = uni.getStorageSync('userInfo')
      if (!userInfo) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        setTimeout(() => {
          uni.navigateTo({
            url: '/pages/mine/'
          })
        }, 1500)
        return
      }
      
      // 已登录，跳转到志愿者注册页面
      uni.navigateTo({
        url: '/pages/index/volunteer-register'
      })
    },
    
    // 跳转到视频协助页面
    goToVideoAssist() {
      uni.navigateTo({
        url: '/pages/index/spxz/spxz'
      });
    },

    // 跳转到出行预约页面
    goToTravelAppointment() {
      uni.navigateTo({
        url: '/pages/index/travel-appointment/travel-appointment'
      });
    },

    // 初始化WebSocket连接
    initWebSocket() {
      const userInfo = uni.getStorageSync('userInfo')
      if (!userInfo || !userInfo.token) return

      this.wsConnection = uni.connectSocket({
        url: `${this.$config.baseUrl}/ws?token=${userInfo.token}`,
        success: () => {
          console.log('WebSocket连接成功')
          this.startHeartbeat()
        }
      })

      // 监听WebSocket消息
      this.wsConnection.onMessage((res) => {
        const data = JSON.parse(res.data)
        this.handleWebSocketMessage(data)
      })

      // 监听连接关闭
      this.wsConnection.onClose(() => {
        console.log('WebSocket连接已关闭')
        // 可以在这里添加重连逻辑
      })
    },

    // 启动心跳
    startHeartbeat() {
      this.heartbeatTimer = setInterval(() => {
        if (this.wsConnection) {
          this.wsConnection.send({
            data: JSON.stringify({
              type: 'HEARTBEAT'
            })
          })
        }
      }, 15000)
    },

    // 处理WebSocket消息
    handleWebSocketMessage(message) {
      switch(message.type) {
        case 'CALL_REQUEST':
          // 收到通话请求
          this.showCallRequest = true
          this.callRequestInfo = {
            roomId: message.roomId,
            callerId: message.callerId,
            calleeId: message.calleeId
          }
          // 播放提示音
          this.playCallRingtone()
          break
        case 'CALL_END':
          // 通话结束
          this.handleCallEnded(message)
          break
        case 'ERROR':
          uni.showToast({
            title: message.content || '发生错误',
            icon: 'none'
          })
          break
      }
    },

    // 播放提示音
    playCallRingtone() {
      const innerAudioContext = uni.createInnerAudioContext()
      innerAudioContext.src = '/static/audio/call-ringtone.mp3' // 需要添加提示音文件
      innerAudioContext.loop = true
      innerAudioContext.play()
      // 保存引用以便后续停止
      this.ringtone = innerAudioContext
    },

    // 停止提示音
    stopCallRingtone() {
      if (this.ringtone) {
        this.ringtone.stop()
        this.ringtone = null
      }
    },

    // 处理接受通话
    async handleAcceptCall() {
      const userInfo = uni.getStorageSync('userInfo')
      if (!userInfo) return

      this.stopCallRingtone()
      this.showCallRequest = false
      
      try {
        const res = await uni.request({
          url: `${this.$config.baseUrl}/api/call/accept`,
          method: 'POST',
          header: {
            'Authorization': `Bearer ${userInfo.token}`
          },
          data: {
            roomId: this.callRequestInfo.roomId
          }
        })

        if (res.data.code === 200) {
          this.isInCall = true
          // 使用TUICallKit接听通话
          this.answerCall(this.callRequestInfo.roomId, this.callRequestInfo.callerId)
        } else {
          uni.showToast({
            title: res.data.message || '接受通话失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('接受通话请求失败:', error)
        uni.showToast({
          title: '接受通话失败',
          icon: 'none'
        })
      }
    },

    // 处理拒绝通话
    async handleRejectCall() {
      const userInfo = uni.getStorageSync('userInfo')
      if (!userInfo) return

      this.stopCallRingtone()
      this.showCallRequest = false
      
      try {
        const res = await uni.request({
          url: `${this.$config.baseUrl}/api/call/reject`,
          method: 'POST',
          header: {
            'Authorization': `Bearer ${userInfo.token}`
          },
          data: {
            roomId: this.callRequestInfo.roomId
          }
        })

        if (res.data.code === 200) {
          uni.showToast({
            title: '已拒绝通话请求',
            icon: 'none'
          })
        } else {
          uni.showToast({
            title: res.data.message || '拒绝通话失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('拒绝通话请求失败:', error)
        uni.showToast({
          title: '拒绝通话失败',
          icon: 'none'
        })
      }
    },

    // 处理通话结束
    async handleCallEnded(message) {
      this.isInCall = false
      this.stopCallRingtone()
      // 结束TUICallKit通话
      uni.$TUICallKit.hangup()
      uni.showToast({
        title: '通话已结束',
        icon: 'none'
      })
    },

    // 主动结束通话
    async handleEndCall() {
      const userInfo = uni.getStorageSync('userInfo')
      if (!userInfo) return

      try {
        const res = await uni.request({
          url: `${this.$config.baseUrl}/api/call/end`,
          method: 'POST',
          header: {
            'Authorization': `Bearer ${userInfo.token}`
          },
          data: {
            roomId: this.callRequestInfo.roomId
          }
        })

        if (res.data.code === 200) {
          this.handleCallEnded()
        } else {
          uni.showToast({
            title: res.data.message || '结束通话失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('结束通话失败:', error)
        uni.showToast({
          title: '结束通话失败',
          icon: 'none'
        })
      }
    },

    // 使用TUICallKit接听通话
    answerCall(roomId, callerId) {
      try {
        const callParams = {
          userIDList: [callerId],
          callMediaType: 2,   // 视频通话
          callParams: { 
            roomID: parseInt(roomId), 
            strRoomID: roomId, 
            timeout: 30 
          },
        }
        
        uni.$TUICallKit.calls(callParams, res => {
          console.log('[TUICallKit] 通话参数:', JSON.stringify(res))
          if (res.code !== 0) {
            this.isInCall = false
            uni.showToast({
              title: '接听通话失败',
              icon: 'none'
            })
          }
        })
      } catch (error) {
        console.error('[TUICallKit] 通话错误:', error)
        this.isInCall = false
        uni.showToast({
          title: '接听通话失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
/* ========== 核心视觉样式 ========== */
/* 容器样式 */
.volunteer-container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

/* 注册部分样式 */
.register-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
  text-align: center;
  margin-top: 100rpx;
}

.register-header {
  margin-bottom: 40rpx;
}

.register-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.register-subtitle {
  font-size: 28rpx;
  color: #666;
}

.register-btn {
  background: #1AAD19;
  color: white;
  border-radius: 40rpx;
  font-size: 32rpx;
  padding: 20rpx 0;
  width: 100%;
  text-align: center;
  box-shadow: 0 4rpx 12rpx rgba(26, 173, 25, 0.2);
}

/* 状态栏 */
.status-bar {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.08);
}

.status-title {
  font-size: 36rpx;
  color: #333;
  margin-right: 30rpx;
}

/* 替换 .mock-switch 的样式 */
.status-switch {
  /* uni-switch 默认样式已经很好，这里可以只调整边距或尺寸 */
  transform: scale(0.9); /* 微调大小 */
  margin-right: 10rpx; /* 调整与文本的间距 */
}

.status-text {
  font-size: 28rpx;
  color: #666;
}

/* 服务标签 */
.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-top: 20rpx;
}

.mock-tag {
  padding: 16rpx 32rpx;
  border: 2rpx solid #eee;
  border-radius: 40rpx;
  font-size: 28rpx;
}

.mock-tag.active {
  background: #e6f7ff;
  border-color: #1890ff;
  color: #1890ff;
}

/* 任务列表 */
.mock-task {
  padding: 30rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.mock-task.urgent {
  border-left: 8rpx solid #ff4d4f;
}

.mock-btn {
  background: #1AAD19;
  color: white;
  border-radius: 40rpx;
  padding: 16rpx 40rpx;
  text-align: center;
}

/* 通话请求弹窗样式 */
.call-request-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.call-request-content {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  width: 80%;
  max-width: 600rpx;
}

.call-request-header {
  text-align: center;
  margin-bottom: 40rpx;
}

.call-request-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.call-request-subtitle {
  font-size: 28rpx;
  color: #666;
}

.call-request-buttons {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.call-btn {
  flex: 1;
  padding: 20rpx 0;
  border-radius: 40rpx;
  font-size: 32rpx;
  text-align: center;
}

.call-btn.reject {
  background: #ff4d4f;
  color: #fff;
}

.call-btn.accept {
  background: #1AAD19;
  color: #fff;
}

/* 通话状态栏样式 */
.call-status-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: #1AAD19;
  color: #fff;
  padding: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 998;
}

.call-status-text {
  font-size: 28rpx;
}

.end-call-btn {
  background: #ff4d4f;
  color: #fff;
  font-size: 24rpx;
  padding: 10rpx 30rpx;
  border-radius: 30rpx;
}
</style>