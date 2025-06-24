<template>
	<view class="container">
		<text v-if="!isLogin" aria-label="正在登录语音通话系统">正在登录语音通话系统...</text>
		<text v-else aria-label="已登录语音通话系统，用户ID：{{ userID }}">已登录语音通话系统，用户ID: {{ userID }}</text>

		<view class="input-section">
			<view class="input-item">
				<text class="label">乘车站点:</text>
				<input class="input" v-model="boardingStation" placeholder="请输入乘车站点" aria-label="请输入乘车站点" tabindex="0" />
			</view>
			<view class="input-item">
				<text class="label">出入口:</text>
				<input class="input" v-model="exit" placeholder="请输入出入口信息" aria-label="请输入出入口信息" tabindex="0" />
			</view>
			<view class="input-item">
				<text class="label">乘车时间:</text>
				<input class="input" v-model="travelTime" placeholder="请输入乘车时间 (例如：HH:mm)" aria-label="请输入乘车时间，例如几点几分" tabindex="0" />
			</view>
		</view>

		<view v-if="isLogin" class="call-section">
			<button @click="handleVoiceCall" :disabled="isRequesting" aria-label="{{ isRequesting ? '正在寻找在线志愿者' : '开始语音通话' }}" tabindex="0">
				{{ isRequesting ? '正在寻找在线志愿者...' : '开始语音通话' }}
			</button>
			<text v-if="isRequesting" class="hint-text" aria-label="请稍候，系统正在为您匹配在线志愿者">请稍候，系统正在为您匹配在线志愿者...</text>
		</view>
	</view>
</template>

<script>
	const TUICallKit = uni.requireNativePlugin('TencentCloud-TUICallKit');
	uni.$TUICallKit = TUICallKit;
	import config from '../../../config.js';

	export default {
		data() {
			return {
				isLogin: false,
				userID: '',
				isRequesting: false,
				wsConnection: null,
				roomId: null,
				heartbeatTimer: null,
				boardingStation: '',
				exit: '',
				travelTime: ''
			}
		},
		onLoad() {
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
				this.wsConnection = uni.connectSocket({
					url: `${config.baseUrl.replace('http', 'ws')}/ws?token=${token}`,
					success: () => {
						console.log('WebSocket连接成功');
						this.startHeartbeat();
					}
				});

				this.wsConnection.onMessage((res) => {
					const data = JSON.parse(res.data);
					this.handleWebSocketMessage(data);
				});

				this.wsConnection.onClose(() => {
					console.log('WebSocket连接已关闭');
				});
			},

			startHeartbeat() {
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
				switch (message.type) {
					case 'CALL_REQUEST':
						this.handleIncomingCall(message);
						break;
					case 'CALL_ACCEPT':
						this.handleCallAccepted(message);
						break;
					case 'CALL_REJECT':
						this.handleCallRejected(message);
						break;
					case 'CALL_END':
						this.handleCallEnded(message);
						break;
					case 'CALL_TIMEOUT':
						this.handleCallTimeout(message);
						break;
					case 'ERROR':
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
							const {
								userSig
							} = res.data.data;
							const SDKAppID = 1600089018;
							console.log('[TUICallKit] 解析后的UserSig信息:', {
								userSig,
								SDKAppID,
								userID: this.userID
							});
							const loginParams = {
								SDKAppID,
								userID: this.userID,
								userSig
							};

							uni.$TUICallKit.login(loginParams, res => {
								if (res.code === 0) {
									this.isLogin = true;
									console.log('[TUICallKit] login success');
								} else {
									console.error('[TUICallKit] TUICallKit login failed:', res.msg);
									uni.showToast({
										title: '语音通话系统登录失败',
										icon: 'none'
									});
								}
							});
						} else {
							console.error('[TUICallKit] Failed to get userSig. Status Code:', res.statusCode);
							console.error('[TUICallKit] Failed to get userSig. Response Data:', res.data);
							console.error('[TUICallKit] Failed to get userSig. Message:', res.data.message || res.data
								.msg || '未知错误');
							uni.showToast({
								title: res.data.message || res.data.msg || '获取语音通话凭证失败',
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

			handleVoiceCall() {
				if (this.isRequesting) return;
				
				if (!this.boardingStation || !this.exit || !this.travelTime) {
					uni.showToast({
						title: '请填写完整的乘车信息',
						icon: 'none'
					});
					return;
				}

				this.isRequesting = true;

				uni.request({
					url: `${config.baseUrl}/api/call/request`,
					method: 'POST',
					header: {
						'Authorization': `Bearer ${uni.getStorageSync('token')}`
					},
					data: {
						// 可以考虑将乘车信息也发送给后端
						// 例如：details: `乘车站点：${this.boardingStation}, 出入口：${this.exit}, 乘车时间：${this.travelTime}`
					},
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 200) {
							console.log('语音通话请求已发送，等待志愿者接听');
						} else {
							this.isRequesting = false;
							uni.showToast({
								title: res.data.message || '发起语音通话请求失败',
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
				uni.showModal({
					title: '收到通话请求',
					content: `用户 ${message.callerId} 请求与您通话`,
					confirmText: '接受',
					cancelText: '拒绝',
					success: (res) => {
						if (res.confirm) {
							this.acceptCall(message.roomId);
						} else {
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
						callMediaType: 1, // 语音通话
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
								title: '发起语音通话失败',
								icon: 'none'
							});
						}
					});
				} catch (error) {
					console.error('[TUICallKit] 通话错误:', error);
					this.isRequesting = false;
					uni.showToast({
						title: '发起语音通话失败',
						icon: 'none'
					});
				}
			}
		}
	}
</script>

<style>
	.container {
		padding: 30px;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.input-section {
		width: 100%;
		margin-bottom: 30px;
	}

	.input-item {
		display: flex;
		align-items: center;
		margin-bottom: 15px;
	}

	.label {
		width: 90px;
		font-size: 16px;
		color: #333;
	}

	.input {
		flex: 1;
		border: 1px solid #eee;
		border-radius: 4px;
		padding: 10px;
		font-size: 16px;
	}

	.call-section {
		margin-top: 30px;
		display: flex;
		flex-direction: column;
		align-items: center;
		width: 100%;
	}

	.container button {
		background-color: #007AFF;
		color: #fff;
		border: none;
		border-radius: 4px;
		padding: 15px 30px;
		font-size: 16px;
		width: 80%;
		margin-top: 20px;
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