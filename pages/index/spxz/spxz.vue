<template>
	<view class="container">
		<text v-if="!isLogin" >正在登录视频通话系统...</text>
		<text v-else >已登录志愿者协助系统，用户ID: {{ userID }}</text>
		<view v-if="isLogin" class="call-section">
			<button @click="handleCall" :disabled="isRequesting" aria-label="点击开始寻找帮助" tabindex="0">
				{{ isRequesting ? '正在寻找在线志愿者...' : '点击开始寻找帮助' }}
			</button>
			<text v-if="isRequesting" class="hint-text" aria-label="请稍候，系统正在为您匹配在线志愿者">请稍候，系统正在为您匹配在线志愿者...</text>
		</view>
	</view>
</template>

<script>
	const TUICallKit = uni.requireNativePlugin('TencentCloud-TUICallKit');
	uni.$TUICallKit = TUICallKit;
	import config from '../../../config.js';
	// import { genTestUserSig } from '../../../debug/GenerateTestUserSig.js'
	export default {
		data() {
			return {
				isLogin: false,
				userID: '',
				isRequesting: false,
				wsConnection: null,
				roomId: null,
				heartbeatTimer: null
			}
		},
		onLoad() {
			// 获取登录用户信息并自动登录TUICallKit
			const userInfo = uni.getStorageSync('userInfo');
			if (!userInfo || !userInfo.id) {
				uni.showToast({
					title: '请先登录',
					icon: 'none'
				});
				setTimeout(() => {
					uni.navigateBack();
				}, 1500);
				return;
			}
			
			this.userID = userInfo.id.toString();
			this.loginToTUICallKit();
			this.initWebSocket();
		},
		onUnload() {
			// 页面卸载时清理资源
			if (this.wsConnection) {
				this.wsConnection.close();
			}
			if (this.heartbeatTimer) {
				clearInterval(this.heartbeatTimer);
			}
		},
		methods: {
			initWebSocket() {
				const token = uni.getStorageSync('token');
				// 建立WebSocket连接，带上token
				this.wsConnection = uni.connectSocket({
					url: `${config.baseUrl.replace('http', 'ws')}/ws?token=${token}`,
					success: () => {
						console.log('WebSocket连接成功');
						// 启动心跳
						this.startHeartbeat();
					}
				});
				
				// 监听WebSocket消息
				this.wsConnection.onMessage((res) => {
					const data = JSON.parse(res.data);
					this.handleWebSocketMessage(data);
				});
				
				// 监听连接关闭
				this.wsConnection.onClose(() => {
					console.log('WebSocket连接已关闭');
					// 重连逻辑可以在这里实现
				});
			},
			
			startHeartbeat() {
				// 每15秒发送一次心跳
				this.heartbeatTimer = setInterval(() => {
					if (this.wsConnection) {
						this.wsConnection.send({
							data: JSON.stringify({
								type: 'HEARTBEAT'
							})
						});
					}
				}, 15000);
			},
			
			handleWebSocketMessage(message) {
				switch(message.type) {
					case 'CALL_REQUEST':
						// 作为志愿者收到通话请求
						this.handleIncomingCall(message);
						break;
					case 'CALL_ACCEPT':
						// 作为主叫收到接受通知
						this.handleCallAccepted(message);
						break;
					case 'CALL_REJECT':
						// 作为主叫收到拒绝通知
						this.handleCallRejected(message);
						break;
					case 'CALL_END':
						// 收到通话结束通知
						this.handleCallEnded(message);
						break;
					case 'CALL_TIMEOUT':
						// 收到通话超时通知
						this.handleCallTimeout(message);
						break;
					case 'ERROR':
						// 处理错误消息
						this.handleErrorMessage(message);
						break;
				}
			},
			
			loginToTUICallKit() {
				uni.request({
					url: `${config.baseUrl}/user/userSig`,
					method: 'GET',
					data: {
						userID: this.userID
					},
					success: (res) => {
						console.log('[TUICallKit] 获取UserSig响应数据:', JSON.stringify(res.data, null, 2));
						if (res.statusCode === 200 && res.data.code === 200) {
							const { userSig } = res.data.data;
							const SDKAppID = 1600089018; 
							console.log('[TUICallKit] 解析后的UserSig信息:', {
								userSig,
								SDKAppID,
								userID: this.userID
							});
							const loginParams = { SDKAppID, userID: this.userID, userSig };
							
							uni.$TUICallKit.login(loginParams, res => {
								if (res.code === 0) {
									this.isLogin = true;
									console.log('[TUICallKit] login success');
								} else {
									console.error('[TUICallKit] TUICallKit login failed:', res.msg);
									uni.showToast({
										title: '视频通话系统登录失败',
										icon: 'none'
									});
								}
							});
						} else {
							console.error('[TUICallKit] Failed to get userSig. Status Code:', res.statusCode);
							console.error('[TUICallKit] Failed to get userSig. Response Data:', res.data);
							console.error('[TUICallKit] Failed to get userSig. Message:', res.data.message || res.data.msg || '未知错误');
							uni.showToast({
								title: res.data.message || res.data.msg || '获取视频通话凭证失败',
								icon: 'none'
							});
						}
					},
					fail: (err) => {
						console.error('[TUICallKit] Network error:', err);
						uni.showToast({
							title: '网络请求失败',
							icon: 'none'
						});
					}
				});
			},
			
			handleCall() {
				if (this.isRequesting) return;
				
				this.isRequesting = true;
				
				// 发起通话请求
				uni.request({
					url: `${config.baseUrl}/api/call/request`,
					method: 'POST',
					header: {
						'Authorization': `Bearer ${uni.getStorageSync('token')}`
					},
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 200) {
							console.log('通话请求已发送，等待志愿者接听');
						} else {
							this.isRequesting = false;
							uni.showToast({
								title: res.data.message || '发起通话请求失败',
								icon: 'none'
							});
						}
					},
					fail: (err) => {
						this.isRequesting = false;
						console.error('请求失败:', err);
						uni.showToast({
							title: '网络请求失败',
							icon: 'none'
						});
					}
				});
			},
			
			handleIncomingCall(message) {
				console.log('[TUICallKit] handleIncomingCall triggered with message:', message);
				// 作为志愿者收到通话请求
				uni.showModal({
					title: '收到通话请求',
					content: `用户 ${message.callerId} 请求与您通话`,
					confirmText: '接受',
					cancelText: '拒绝',
					success: (res) => {
						if (res.confirm) {
							// 接受通话
							this.acceptCall(message.roomId);
						} else {
							// 拒绝通话
							this.rejectCall(message.roomId);
						}
					}
				});
			},
			
			acceptCall(roomId) {
				uni.request({
					url: `${config.baseUrl}/api/call/accept`,
					method: 'POST',
					header: {
						'Authorization': `Bearer ${uni.getStorageSync('token')}`,
						'Content-Type': 'application/json' 
					},
					data: {
						roomId: roomId
					},
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 200) {
							console.log('已接受通话请求');
						} else {
							uni.showToast({
								title: res.data.message || '接受通话失败',
								icon: 'none'
							});
						}
					}
				});
			},
			
			rejectCall(roomId) {
				uni.request({
					url: `${config.baseUrl}/api/call/reject`,
					method: 'POST',
					header: {
						'Authorization': `Bearer ${uni.getStorageSync('token')}`,
						'Content-Type': 'application/json' 
					},
					data: {
						roomId: roomId
					},
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 200) {
							console.log('已拒绝通话请求');
						} else {
							uni.showToast({
								title: res.data.message || '拒绝通话失败',
								icon: 'none'
							});
						}
					}
				});
			},
			
			handleCallAccepted(message) {
				// 作为主叫收到接受通知，开始通话
				this.startCall(message.roomId, message.calleeId);
			},
			
			handleCallRejected(message) {
				this.isRequesting = false;
				uni.showToast({
					title: '志愿者拒绝了通话请求',
					icon: 'none'
				});
			},
			
			handleCallEnded(message) {
				this.isRequesting = false;
				// 结束TUICallKit通话
				uni.$TUICallKit.hangup();
				uni.showToast({
					title: '通话已结束',
					icon: 'none'
				});
			},
			
			handleCallTimeout(message) {
				this.isRequesting = false;
				uni.showToast({
					title: message.content || '通话请求超时',
					icon: 'none'
				});
			},
			
			handleErrorMessage(message) {
				this.isRequesting = false;
				uni.showToast({
					title: message.content || '发生错误',
					icon: 'none'
				});
			},
			
			startCall(roomId, calleeId) {
				try {
					const callParams = {
						userIDList: [calleeId],
						callMediaType: 2,   // 视频通话
						callParams: { 
							roomID: parseInt(roomId), 
							strRoomID: roomId, 
							timeout: 30 
						},
					};
					
					uni.$TUICallKit.calls(callParams, res => {
						console.log('[TUICallKit] 通话参数:', JSON.stringify(res));
						if (res.code !== 0) {
							this.isRequesting = false;
							uni.showToast({
								title: '发起通话失败',
								icon: 'none'
							});
						}
					});
				} catch (error) {
					console.error('[TUICallKit] 通话错误:', error);
					this.isRequesting = false;
					uni.showToast({
						title: '发起通话失败',
						icon: 'none'
					});
				}
			}
		}
	}
</script>

<style>
.container {
	margin: 30px;
}
.call-section {
	margin-top: 30px;
	display: flex;
	flex-direction: column;
	align-items: center;
}
.container button {
	margin-top: 20px;
	background-color: #007AFF;
	color: #fff;
	border: none;
	border-radius: 4px;
	padding: 15px 30px;
	font-size: 16px;
	width: 80%;
}
.container button:disabled {
	background-color: #ccc;
}
.container text {
	font-size: 16px;
	color: #333;
	margin-top: 10px;
}
.hint-text {
	color: #666;
	font-size: 14px;
	text-align: center;
	margin-top: 15px;
}
</style>