<template>
	<view class="admin-container">
		<!-- 顶部信息栏 -->
		<view class="admin-header">
			<view class="admin-info">
				
				<view class="admin-details">
					<text class="admin-name">管理员页面</text>
				
				</view>
			</view>
			<view class="admin-stats">
				<view class="stat-item">
					<text class="stat-num">{{ stats.userCount }}</text>
					<text class="stat-label">用户总数</text>
				</view>
				<view class="stat-item">
					<text class="stat-num">{{ stats.policyCount }}</text>
					<text class="stat-label">政策总数</text>
				</view>
				<view class="stat-item">
					<text class="stat-num">{{ stats.faqCount }}</text>
					<text class="stat-label">常见问题总数</text>
				</view>
				<view class="stat-item">
					<text class="stat-num">{{ stats.feedbackCount }}</text>
					<text class="stat-label">用户反馈总数</text>
				</view>
			</view>
		</view>
		
		<!-- 快捷功能入口 -->
		<view class="quick-actions">
			<view class="action-grid">
				<view class="action-item" @click="toggleManagementSection('user')" aria-label="用户管理" tabindex="0">
					<text class="iconfont icon-user"></text>
					<text class="action-label">用户管理</text>
				</view>
				<view class="action-item" @click="toggleManagementSection('policy')" aria-label="政策管理" tabindex="0">
					<text class="iconfont icon-policy"></text>
					<text class="action-label">政策管理</text>
				</view>
				<view class="action-item" @click="toggleManagementSection('faq')" aria-label="常见问题管理" tabindex="0">
					<text class="iconfont icon-faq"></text>
					<text class="action-label">常见问题管理</text>
				</view>
				<view class="action-item" @click="toggleManagementSection('feedback')" aria-label="用户反馈" tabindex="0">
					<text class="iconfont icon-feedback"></text>
					<text class="action-label">用户反馈</text>
				</view>
			</view>
		</view>
		
		<!-- 最近活动列表 -->
		<view class="recent-activities">
			<view class="section-title">最近活动</view>
			<view class="activity-list">
				<view class="activity-item" v-for="(activity, index) in recentActivities" :key="index" aria-label="活动项" tabindex="0">
					<view class="activity-time">{{activity.time}}</view>
					<view class="activity-content">{{activity.content}}</view>
				</view>
			</view>
		</view>

		<!-- 用户管理模块 -->
		<view class="user-manage-card" v-if="activeManagementSection === 'user'">
			<view class="manage-header">
				<text class="manage-title">用户管理</text>
				<button class="create-btn" @tap="openUserForm()" aria-label="新建用户" tabindex="0">新建用户</button>
			</view>
			<view class="manage-search">
				<input class="search-input" v-model="searchUserEmail" placeholder="搜索用户邮箱" @confirm="loadUsers(true)" aria-label="搜索用户邮箱" tabindex="0" />
				<picker mode="selector" :range="userRoles" @change="onUserRoleChange" class="region-picker" aria-label="选择用户角色" tabindex="0">
					<view class="picker-content">
						<text class="selected-region">{{ searchUserRole || '所有角色' }}</text>
						<text class="picker-arrow">▼</text>
					</view>
				</picker>
				<picker mode="selector" :range="userStatuses" @change="onUserStatusChange" class="region-picker" aria-label="选择用户状态" tabindex="0">
					<view class="picker-content">
						<text class="selected-region">{{ formatUserStatus(searchUserStatus) || '所有状态' }}</text>
						<text class="picker-arrow">▼</text>
					</view>
				</picker>
			</view>
			<view class="manage-list">
				<view class="manage-item" v-for="user in userList" :key="user.id" @click="openUserForm(user)" role="button" :aria-label="'用户' + user.nickname + '，点击编辑或查看详情'" tabindex="0">
					<view class="item-info">
						<text class="item-title">{{ user.nickname || user.email }}</text>
						<text class="item-summary">角色: {{ user.role }} | 状态: {{ formatUserStatus(user.status) }}</text>
					</view>
					<view class="item-actions">
						<button class="edit-btn" @tap.stop="openUserForm(user)" aria-label="编辑用户" tabindex="0">编辑</button>
						<button class="delete-btn" @tap.stop="deleteUser(user.id)" aria-label="删除用户" tabindex="0">删除</button>
					</view>
				</view>
				<view v-if="userLoading" class="loading">加载中...</view>
				<view v-if="!userHasMore && !userLoading" class="no-more">没有更多用户数据</view>
			</view>
		</view>

		<!-- 政策管理模块 -->
		<view class="policy-manage-card" v-if="activeManagementSection === 'policy'">
			<view class="manage-header">
				<text class="manage-title">政策管理</text>
				<button class="create-btn" @tap="openPolicyForm()" aria-label="新建政策" tabindex="0">新建政策</button>
			</view>
			<view class="manage-search">
				<input class="search-input" v-model="searchTitle" placeholder="搜索政策标题" @confirm="onSearch" aria-label="搜索政策标题" tabindex="0" />
				<picker mode="selector" :range="regions" @change="onRegionChange" class="region-picker" aria-label="选择政策区域" tabindex="0">
					<view class="picker-content">
						<text class="selected-region">{{ currentRegion }}</text>
						<text class="picker-arrow">▼</text>
					</view>
				</picker>
			</view>
			<view class="manage-list">
				<view class="manage-item" v-for="item in leftColumn" :key="item.id" @click="openPolicyForm(item)" role="button" :aria-label="'政策' + item.title + '，点击编辑或查看详情'" tabindex="0">
					<view class="item-info">
						<text class="item-title">{{ item.title }}</text>
						<text class="item-summary">{{ item.summary }}</text>
					</view>
					<view class="item-actions">
						<button class="edit-btn" @tap.stop="openPolicyForm(item)" aria-label="编辑政策" tabindex="0">编辑</button>
						<button class="delete-btn" @tap.stop="deletePolicy(item.id)" aria-label="删除政策" tabindex="0">删除</button>
					</view>
				</view>
				<view v-if="loading" class="loading">加载中...</view>
				<view v-if="!hasMore && !loading" class="no-more">没有更多数据</view>
			</view>
		</view>

		<!-- 常见问题管理模块 -->
		<view class="faq-manage-card" v-if="activeManagementSection === 'faq'">
			<view class="manage-header">
				<text class="manage-title">常见问题管理</text>
				<button class="create-btn" @tap="openFaqForm()" aria-label="新建FAQ" tabindex="0">新建FAQ</button>
			</view>
			<view class="manage-search">
				<input class="search-input" v-model="searchFaqQuestion" placeholder="搜索问题" @confirm="loadFaqs(true)" aria-label="搜索问题" tabindex="0" />
			</view>
			<view class="manage-list">
				<view class="manage-item" v-for="item in faqList" :key="item.id" @click="openFaqForm(item)" role="button" :aria-label="'FAQ' + item.question" tabindex="0">
					<view class="item-info">
						<text class="item-title">{{ item.question }}</text>
						<text class="item-summary">{{ item.answer.slice(0, 20) }}...</text>
					</view>
					<view class="item-actions">
						<button class="edit-btn" @tap.stop="openFaqForm(item)" aria-label="编辑FAQ" tabindex="0">编辑</button>
						<button class="delete-btn" @tap.stop="deleteFaq(item.id)" aria-label="删除FAQ" tabindex="0">删除</button>
					</view>
				</view>
				<view v-if="faqLoading" class="loading">加载中...</view>
				<view v-if="!faqHasMore && !faqLoading" class="no-more">没有更多FAQ</view>
			</view>
		</view>

		<!-- 用户反馈管理模块 -->
		<view class="feedback-manage-card" v-if="activeManagementSection === 'feedback'">
			<view class="manage-header">
				<text class="manage-title">用户反馈</text>
			</view>
			<view class="filter-bar" style="display: flex; align-items: center; gap: 16rpx; margin-bottom: 20rpx;">
				<text style="font-weight: bold; font-size: 28rpx; margin-right: 16rpx;">筛选用户反馈</text>
				<input class="search-input short-input" v-model="searchFeedbackUserId" placeholder="用户ID" aria-label="用户ID" tabindex="0" />
				<picker mode="date" :value="searchFeedbackStartTime" @change="onFeedbackStartTimeChange">
					<view class="search-input date-input">{{ searchFeedbackStartTime ? searchFeedbackStartTime : '开始日期' }}</view>
				</picker>
				<picker mode="date" :value="searchFeedbackEndTime" @change="onFeedbackEndTimeChange">
					<view class="search-input date-input">{{ searchFeedbackEndTime ? searchFeedbackEndTime : '结束日期' }}</view>
				</picker>
				<button class="filter-btn" @tap="onFeedbackFilterConfirm" aria-label="确定筛选" tabindex="0">确定</button>
			</view>
			<view class="manage-list">
				<view class="manage-item" v-for="item in feedbackList" :key="item.id" @click="openFeedbackDetail(item.id)" role="button" :aria-label="'反馈' + item.id" tabindex="0">
					<view class="item-info">
						<text class="item-title">反馈 {{ item.id }}</text>
						<text class="item-summary">用户ID: {{ item.userId }} | {{ item.createTime }}</text>
					</view>
					<view class="item-actions">
						<button class="edit-btn" @tap.stop="openFeedbackDetail(item.id)" aria-label="查看详情" tabindex="0">查看详情</button>
					</view>
				</view>
				<view v-if="feedbackLoading" class="loading">加载中...</view>
				<view v-if="!feedbackHasMore && !feedbackLoading" class="no-more">没有更多反馈</view>
			</view>
		</view>

		<!-- 新建/编辑政策弹窗 -->
		<view v-if="showPolicyForm" class="policy-form-modal">
			<view class="policy-form">
				<view class="form-title">{{ editingPolicy ? '编辑政策' : '新建政策' }}</view>
				<input v-model="policyForm.title" placeholder="政策标题" class="form-input" aria-label="政策标题" tabindex="0" />
				<input v-model="policyForm.policyLevel" placeholder="政策级别" class="form-input" aria-label="政策级别" tabindex="0" />
				<picker mode="date" :value="policyForm.publishDate" @change="onDateChange" class="form-input date-picker" aria-label="选择发布日期" tabindex="0">
					<view class="picker-text">{{ policyForm.publishDate || '选择发布日期' }}</view>
				</picker>
				<input v-model="policyForm.tags" placeholder="标签（逗号分隔）" class="form-input" aria-label="标签" tabindex="0" />
				<input v-model="policyForm.attachmentUrl" placeholder="附件URL" class="form-input" aria-label="附件URL" tabindex="0" />
				<textarea v-model="policyForm.content" placeholder="政策内容" class="form-textarea" aria-label="政策内容" tabindex="0"></textarea>
				<view class="form-actions">
					<button class="save-btn" @tap="submitPolicyForm" aria-label="保存政策" tabindex="0">{{ editingPolicy ? '保存' : '新建' }}</button>
					<button class="cancel-btn" @tap="closePolicyForm" aria-label="取消" tabindex="0">取消</button>
				</view>
			</view>
		</view>

		<!-- 新建/编辑用户弹窗 -->
		<view v-if="showUserForm" class="policy-form-modal">
			<view class="policy-form">
				<view class="form-title">{{ editingUser ? '编辑用户' : '新建用户' }}</view>
				<input v-model="userForm.email" placeholder="用户邮箱" class="form-input" aria-label="用户邮箱" tabindex="0" />
				<input v-model="userForm.nickname" placeholder="用户昵称" class="form-input" aria-label="用户昵称" tabindex="0" />
				<input v-if="!editingUser" v-model="userForm.password" type="password" placeholder="用户密码 (新建时必填)" class="form-input" aria-label="用户密码" tabindex="0" />
				<picker mode="selector" :range="userRoles" @change="onUserFormRoleChange" class="form-input date-picker" aria-label="选择用户角色" tabindex="0">
					<view class="picker-text">{{ userForm.role || '选择角色' }}</view>
				</picker>
				<picker mode="selector" :range="userStatuses" @change="onUserFormStatusChange" class="form-input date-picker" aria-label="选择用户状态" tabindex="0">
					<view class="picker-text">{{ formatUserStatus(userForm.status) || '选择状态' }}</view>
				</picker>
				<view class="form-actions">
					<button class="save-btn" @tap="submitUserForm" aria-label="保存用户" tabindex="0">{{ editingUser ? '保存' : '新建' }}</button>
					<button class="cancel-btn" @tap="closeUserForm" aria-label="取消" tabindex="0">取消</button>
				</view>
			</view>
		</view>

		<!-- 新建/编辑FAQ弹窗 -->
		<view v-if="showFaqForm" class="policy-form-modal">
			<view class="policy-form">
				<view class="form-title">{{ editingFaq ? '编辑FAQ' : '新建FAQ' }}</view>
				<input v-model="faqForm.question" placeholder="问题" class="form-input" aria-label="问题" tabindex="0" />
				<textarea v-model="faqForm.answer" placeholder="答案" class="form-textarea" aria-label="答案" tabindex="0" />
				<view class="form-actions">
					<button class="save-btn" @tap="submitFaqForm" aria-label="保存FAQ" tabindex="0">{{ editingFaq ? '保存' : '新建' }}</button>
					<button class="cancel-btn" @tap="closeFaqForm" aria-label="取消" tabindex="0">取消</button>
				</view>
			</view>
		</view>

		<!-- 用户反馈详情弹窗 -->
		<view v-if="showFeedbackDetail" class="policy-form-modal">
			<view class="policy-form">
				<view class="form-title">反馈详情</view>
				<view class="form-input">反馈ID：{{ feedbackDetail.id }}</view>
				<view class="form-input">用户ID：{{ feedbackDetail.userId }}</view>
				<view class="form-input">提交时间：{{ feedbackDetail.createTime }}</view>
				<view class="form-textarea">内容：{{ feedbackDetail.content }}</view>
				<view class="form-actions">
					<button class="cancel-btn" @tap="closeFeedbackDetail" aria-label="关闭" tabindex="0">关闭</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			stats: {
				userCount: 0,
				policyCount: 0,
				faqCount: 0,
				feedbackCount: 0
			},
			recentActivities: [],
			regions: ['全国', '北京', '上海', '广州', '深圳'],
			currentRegion: '全国',
			leftColumn: [],
			rightColumn: [],
			page: 1,
			pageSize: 10,
			loading: false,
			hasMore: true,
			error: false,
			searchTitle: '',
			// 收藏状态缓存，key为政策id，value为true/false
			favoriteMap: {},
			showPolicyForm: false,
			editingPolicy: null,
			policyForm: {
				title: '',
				publishDate: '',
				policyLevel: '',
				tags: '',
				content: '',
				attachmentUrl: ''
			},
			activeManagementSection: null, // 控制当前显示的管理模块: 'policy' 或 'user'

			// 用户管理相关数据
			userList: [],
			userPage: 1,
			userPageSize: 10,
			userLoading: false,
			userHasMore: true,
			userError: false,
			searchUserEmail: '',
			searchUserRole: '',
			searchUserStatus: null, // null表示所有状态
			userRoles: ['所有角色', '普通用户', '管理员'], // 用于picker选择
			userStatuses: ['所有状态', '启用', '禁用'], // 用于picker选择
			showUserForm: false,
			editingUser: null,
			userForm: {
				id: null,
				email: '',
				nickname: '',
				password: '',
				role: '',
				status: null,
			},
			// FAQ管理相关
			faqList: [],
			faqPage: 1,
			faqPageSize: 10,
			faqLoading: false,
			faqHasMore: true,
			searchFaqQuestion: '',
			showFaqForm: false,
			editingFaq: null,
			faqForm: {
				id: null,
				question: '',
				answer: ''
			},
			// 用户反馈管理相关
			feedbackList: [],
			feedbackPage: 1,
			feedbackPageSize: 10,
			feedbackLoading: false,
			feedbackHasMore: true,
			searchFeedbackUserId: '',
			searchFeedbackStartTime: '',
			searchFeedbackEndTime: '',
			showFeedbackDetail: false,
			feedbackDetail: {},
		}
	},
	computed: {
		policiesToDisplay() {
			// 在管理后台列表，不需要瀑布流布局，直接合并显示
			return this.leftColumn.concat(this.rightColumn);
		}
	},
	onLoad() {
		this.loadStats()
		this.loadRecentActivities()
		// this.loadPolicies() // 默认不加载，点击政策管理才加载
	},
	methods: {
		async loadStats() {
			try {
				const token = uni.getStorageSync('token');
				if (!token) {
					uni.showToast({ title: '请先登录', icon: 'none' });
					return;
				}
				// 1. 获取用户总数
				const userRes = await uni.request({
					url: `${this.$config.baseUrl}/user/manage/page`,
					method: 'GET',
					data: { pageNum: 1, pageSize: 1 },
					header: { 'Authorization': `Bearer ${token}` }
				});
				const userCount = userRes.data.data.total || 0;
				console.log('userRes', userRes.data);
				// 2. 获取政策总数
				const policyRes = await uni.request({
					url: `${this.$config.baseUrl}/policy/page`,
					method: 'GET',
					data: { pageNum: 1, pageSize: 1 },
					header: { 'Authorization': `Bearer ${token}` }
				});
				const policyCount = policyRes.data.data.total || 0;
				console.log('policyRes', policyRes.data);
				// 3. 获取常见问题总数
				const faqRes = await uni.request({
					url: `${this.$config.baseUrl}/faq/page`,
					method: 'GET',
					data: { pageNum: 1, pageSize: 1 },
					header: { 'Authorization': `Bearer ${token}` }
				});
				const faqCount = faqRes.data.data.total || 0;
				console.log('faqRes', faqRes.data);
				// 4. 获取用户反馈总数
				const feedbackRes = await uni.request({
					url: `${this.$config.baseUrl}/user/manage/feedback/page`,
					method: 'GET',
					data: { pageNum: 1, pageSize: 1 },
					header: { 'Authorization': `Bearer ${token}` }
				});
				const feedbackCount = feedbackRes.data.data.total || 0;
				console.log('feedbackRes', feedbackRes.data);
				this.stats = {
					userCount,
					policyCount,
					faqCount,
					feedbackCount
				};
			} catch (error) {
				uni.showToast({ title: '获取统计数据失败', icon: 'none' });
			}
		},
		async loadRecentActivities() {
			try {
				// TODO: 调用后端API获取最近活动
				this.recentActivities = [
					{
						time: '2024-03-20 10:30',
						content: '新用户注册：张三'
					},
					{
						time: '2024-03-20 09:15',
						content: '新课程发布：Java编程基础'
					},
					{
						time: '2024-03-19 16:45',
						content: '订单完成：订单号202403190001'
					}
				]
			} catch (error) {
				uni.showToast({
					title: '获取活动记录失败',
					icon: 'none'
				})
			}
		},
		async loadPolicies(reset = false) {
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

				const res = await uni.request({
					url: `${this.$config.baseUrl}/policy/page`,
					method: 'GET',
					data: params,
				});

				if (res.data.code !== 200) throw new Error(res.data.message || '接口错误');

				const newPolicies = res.data.data.records || [];
				if (newPolicies.length === 0) {
					this.hasMore = false;
					return;
				}

				// 查询每条政策的收藏状态 (在管理后台不需要展示收藏状态，可以移除或保持不动)
				// await Promise.all(newPolicies.map(item => this.fetchFavoriteStatus(item.id)));

				// 瀑布流分列 - 在管理后台列表直接合并显示
				newPolicies.forEach((policy) => {
					const card = { ...policy, loaded: true };
					// 为了简化管理后台的显示，不严格区分左右列，直接添加到leftColumn
					// 实际渲染时，通过 computed property policiesToDisplay 来合并
					this.leftColumn.push(card);
				});

				this.hasMore = newPolicies.length === this.pageSize;
				this.page++;
			} catch (error) {
				this.error = true;
				console.error('加载数据失败:', error);
			} finally {
				this.loading = false;
			}
		},
		// fetchFavoriteStatus 和 toggleFavorite 在管理后台不再需要，可以考虑移除或调整
		async fetchFavoriteStatus(policyId) {
			// 在管理后台可能不需要获取收藏状态，此方法可以根据实际需求调整或移除
			// 保持原有逻辑，但注意这里的URL是否需要修改为 /policy/favorite/{id}
			try {
				const token = uni.getStorageSync('token');
				if (!token) return;
				const res = await uni.request({
					url: `${this.$config.baseUrl}/policy/favorite/${policyId}`,
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
		async toggleFavorite(policyId) {
			// 在管理后台可能不需要收藏功能，此方法可以根据实际需求调整或移除
			const isFav = this.favoriteMap[policyId];
			const token = uni.getStorageSync('token');
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				return;
			}
			try {
				if (isFav) {
					await uni.request({
						url: `${this.$config.baseUrl}/policy/favorite/${policyId}`,
						method: 'DELETE',
						header: { 'Authorization': `Bearer ${token}` }
					});
					this.$set(this.favoriteMap, policyId, false);
					uni.showToast({ title: '已取消收藏', icon: 'none' });
				} else {
					await uni.request({
						url: `${this.$config.baseUrl}/policy/favorite/${policyId}`,
						method: 'POST',
						header: { 'Authorization': `Bearer ${token}` }
					});
					this.$set(this.favoriteMap, policyId, true);
					uni.showToast({ title: '已收藏', icon: 'none' });
				}
			} catch (e) {
				uni.showToast({ title: '操作失败', icon: 'none' });
			}
		},
		onRegionChange(e) {
			this.currentRegion = this.regions[e.detail.value];
			this.loadPolicies(true);
		},
		onSearch() {
			this.loadPolicies(true);
		},
		onImageLoad(id) {
			// 这个方法在政策管理中可能不需要，因为这里没有瀑布流图片加载
			const updateColumn = (column) => {
				const index = column.findIndex(item => item.id === id);
				if (index !== -1) column[index].loaded = true;
			};
			updateColumn(this.leftColumn);
			updateColumn(this.rightColumn);
		},
		navigateToDetail(id) {
			// 在管理后台点击政策列表项通常是编辑，而不是跳转详情，这里可能需要调整
			uni.navigateTo({ url: `/pages/policy/detail?id=${id}` });
		},
		formatDate(timestamp) {
			if (!timestamp) return '';
			const date = new Date(timestamp);
			return date.toISOString().split('T')[0];
		},
		formatViews(views) {
			// 在管理后台可能不需要显示阅读量，此方法可以根据实际需求调整或移除
			return views >= 10000 ? `${(views / 10000).toFixed(1)}万` : views;
		},
		navigateTo(url) {
			uni.navigateTo({
				url: url
			})
		},
		onDateChange(e) {
			this.policyForm.publishDate = e.detail.value;
		},
		openPolicyForm(policy = null) {
			this.showPolicyForm = true;
			if (policy) {
				this.editingPolicy = policy;
				// 填充表单
				this.policyForm = {
					id: policy.id,
					title: policy.title,
					publishDate: this.formatDateForPicker(policy.publishDate), // 确保日期格式正确
					policyLevel: policy.policyLevel,
					tags: policy.tags.join(','), // 标签数组转字符串
					content: policy.content,
					attachmentUrl: policy.attachmentUrl || ''
				}
			} else {
				this.editingPolicy = null;
				// 重置表单
				this.policyForm = {
					title: '',
					publishDate: '',
					policyLevel: '',
					tags: '',
					content: '',
					attachmentUrl: ''
				}
			}
		},
		closePolicyForm() {
			this.showPolicyForm = false;
			this.editingPolicy = null;
			// 重置表单
			this.policyForm = {
				title: '',
				publishDate: '',
				policyLevel: '',
				tags: '',
				content: '',
				attachmentUrl: ''
			}
		},
		async submitPolicyForm() {
			const token = uni.getStorageSync('token');
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				return;
			}

			// 简单的表单验证
			if (!this.policyForm.title || !this.policyForm.publishDate || !this.policyForm.policyLevel || !this.policyForm.content) {
				uni.showToast({ title: '请填写所有必填项 (标题、发布日期、级别、内容)', icon: 'none' });
				return;
			}

			uni.showLoading({ title: '提交中...' });
			try {
				const url = this.editingPolicy ? `${this.$config.baseUrl}/policy/update/${this.editingPolicy.id}` : `${this.$config.baseUrl}/policy`;
				const method = this.editingPolicy ? 'PUT' : 'POST';
				
				const requestData = {
					title: this.policyForm.title,
					// 注意：publishDate需要是后端期望的LocalDate格式，例如"YYYY-MM-DD"
					publishDate: this.policyForm.publishDate,
					policyLevel: this.policyForm.policyLevel,
					tags: this.policyForm.tags,
					content: this.policyForm.content,
					attachmentUrl: this.policyForm.attachmentUrl || null
				};

				const res = await uni.request({
					url: url,
					method: method,
					data: requestData,
					header: {
						'Authorization': `Bearer ${token}`,
						'Content-Type': 'application/json'
					}
				});

				if (res.data.code === 200) {
					uni.showToast({ title: this.editingPolicy ? '政策更新成功' : '政策创建成功', icon: 'success' });
					this.closePolicyForm();
					this.loadPolicies(true); // 刷新列表
				} else {
					throw new Error(res.data.message || '操作失败');
				}
			} catch (error) {
				console.error('政策提交失败:', error);
				uni.showToast({ title: error.message || '提交失败', icon: 'none' });
			} finally {
				uni.hideLoading();
			}
		},
		deletePolicy(id) {
			const token = uni.getStorageSync('token');
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				return;
			}
			uni.showModal({
				title: '确认删除',
				content: '确定要删除这条政策吗？',
				success: async (res) => {
					if (res.confirm) {
						uni.showLoading({ title: '删除中...' });
						try {
							const res = await uni.request({
								url: `${this.$config.baseUrl}/policy/delete/${id}`,
								method: 'DELETE',
								header: { 'Authorization': `Bearer ${token}` }
							});
							
							if (res.data.code === 200) {
								uni.showToast({ title: '删除成功', icon: 'success' });
								this.loadPolicies(true); // 刷新列表
							} else {
								throw new Error(res.data.message || '删除失败');
							}
						} catch (error) {
							console.error('政策删除失败:', error);
							uni.showToast({ title: error.message || '删除失败', icon: 'none' });
						} finally {
							uni.hideLoading();
						}
					}
				}
			});
		},
		// 新增方法：切换管理模块显示
		toggleManagementSection(section) {
			if (this.activeManagementSection === section) {
				this.activeManagementSection = null;
			} else {
				this.activeManagementSection = section;
				if (section === 'policy') {
					this.loadPolicies(true);
				} else if (section === 'user') {
					this.loadUsers(true);
				} else if (section === 'faq') {
					this.loadFaqs(true);
				} else if (section === 'feedback') {
					this.loadFeedbacks(true);
				}
			}
		},
		// 用户管理相关方法
		async loadUsers(reset = false) {
			const token = uni.getStorageSync('token');
			if (!token) {
				uni.showToast({ title: '请先登录才能管理用户', icon: 'none' });
				this.userError = true;
				this.userHasMore = false;
				return;
			}

			if (this.userLoading || (!this.userHasMore && !reset)) return;

			if (reset) {
				this.userList = [];
				this.userPage = 1;
				this.userHasMore = true;
				this.userError = false;
			}

			this.userLoading = true;
			this.userError = false;

			try {
				const params = {
					pageNum: this.userPage,
					pageSize: this.userPageSize,
				};
				if (this.searchUserEmail) {
					params.email = this.searchUserEmail;
				}
				if (this.searchUserRole && this.searchUserRole !== '所有角色') {
					params.role = this.searchUserRole;
				}
				if (this.searchUserStatus !== null && this.searchUserStatus !== '所有状态') {
					params.status = this.mapUserStatusToNumber(this.searchUserStatus);
				}

				const res = await uni.request({
					url: `${this.$config.baseUrl}/user/manage/page`,
					method: 'GET',
					data: params,
					header: { 'Authorization': `Bearer ${token}` }
				});

				if (res.data.code !== 200) throw new Error(res.data.message || '接口错误');

				const newUsers = res.data.data.records || [];
				if (newUsers.length === 0) {
					this.userHasMore = false;
					return;
				}

				this.userList = this.userList.concat(newUsers);
				this.userHasMore = newUsers.length === this.userPageSize;
				this.userPage++;
			} catch (error) {
				this.userError = true;
				console.error('加载用户数据失败:', error);
				uni.showToast({ title: error.message || '加载用户失败', icon: 'none' });
			} finally {
				this.userLoading = false;
			}
		},
		onUserRoleChange(e) {
			this.searchUserRole = this.userRoles[e.detail.value];
			this.loadUsers(true);
		},
		onUserStatusChange(e) {
			this.searchUserStatus = this.userStatuses[e.detail.value];
			this.loadUsers(true);
		},
		formatUserStatus(status) {
			if (status === 0 || status === '禁用') return '禁用';
			if (status === 1 || status === '启用') return '启用';
			return '所有状态';
		},
		mapUserStatusToNumber(statusText) {
			if (statusText === '启用') return 1;
			if (statusText === '禁用') return 0;
			return null; // 表示所有状态
		},
		openUserForm(user = null) {
			this.showUserForm = true;
			if (user) {
				this.editingUser = user;
				// 填充表单
				this.userForm = {
					id: user.id,
					email: user.email,
					nickname: user.nickname || '',
					password: '', // 不回显密码
					role: user.role,
					status: user.status,
				}
			} else {
				this.editingUser = null;
				// 重置表单
				this.userForm = {
					id: null,
					email: '',
					nickname: '',
					password: '',
					role: '',
					status: null,
				}
			}
		},
		closeUserForm() {
			this.showUserForm = false;
			this.editingUser = null;
			// 重置表单
			this.userForm = {
				id: null,
				email: '',
				nickname: '',
				password: '',
				role: '',
				status: null,
			}
		},
		onUserFormRoleChange(e) {
			this.userForm.role = this.userRoles[e.detail.value];
			if (this.userForm.role === '所有角色') this.userForm.role = ''; // 转换为后端期望的空字符串或null
		},
		onUserFormStatusChange(e) {
			const selectedStatusText = this.userStatuses[e.detail.value];
			this.userForm.status = this.mapUserStatusToNumber(selectedStatusText);
		},
		async submitUserForm() {
			const token = uni.getStorageSync('token');
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				return;
			}

			// 简单的表单验证
			if (!this.userForm.email || !this.userForm.role || this.userForm.status === null) {
				uni.showToast({ title: '请填写用户邮箱、角色和状态', icon: 'none' });
				return;
			}
			if (!this.editingUser && !this.userForm.password) {
				uni.showToast({ title: '新建用户时密码不能为空', icon: 'none' });
				return;
			}

			uni.showLoading({ title: '提交中...' });
			try {
				const url = this.editingUser ? `${this.$config.baseUrl}/user/manage/${this.editingUser.id}` : `${this.$config.baseUrl}/user/manage`;
				const method = this.editingUser ? 'PUT' : 'POST';
				
				const requestData = {
					email: this.userForm.email,
					nickname: this.userForm.nickname || null,
					role: this.userForm.role,
					status: this.userForm.status,
				};
				if (!this.editingUser && this.userForm.password) {
					requestData.password = this.userForm.password;
				}

				const res = await uni.request({
					url: url,
					method: method,
					data: requestData,
					header: {
						'Authorization': `Bearer ${token}`,
						'Content-Type': 'application/json'
					}
				});

				if (res.data.code === 200) {
					uni.showToast({ title: this.editingUser ? '用户更新成功' : '用户创建成功', icon: 'success' });
					this.closeUserForm();
					this.loadUsers(true); // 刷新列表
				} else {
					throw new Error(res.data.message || '操作失败');
				}
			} catch (error) {
				console.error('用户提交失败:', error);
				uni.showToast({ title: error.message || '提交失败', icon: 'none' });
			} finally {
				uni.hideLoading();
			}
		},
		async deleteUser(id) {
			const token = uni.getStorageSync('token');
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				return;
			}
			uni.showModal({
				title: '确认删除',
				content: '确定要删除该用户吗？',
				success: async (res) => {
					if (res.confirm) {
						uni.showLoading({ title: '删除中...' });
						try {
							const res = await uni.request({
								url: `${this.$config.baseUrl}/user/manage/${id}`,
								method: 'DELETE',
								header: { 'Authorization': `Bearer ${token}` }
							});
							
							if (res.data.code === 200) {
								uni.showToast({ title: '删除成功', icon: 'success' });
								this.loadUsers(true); // 刷新列表
							} else {
								throw new Error(res.data.message || '删除失败');
							}
						} catch (error) {
							console.error('用户删除失败:', error);
							uni.showToast({ title: error.message || '删除失败', icon: 'none' });
						} finally {
							uni.hideLoading();
						}
					}
				}
			});
		},
		// 工具方法：格式化日期为 YYYY-MM-DD 格式，用于日期选择器回显
		formatDateForPicker(timestamp) {
			if (!timestamp) return '';
			const date = new Date(timestamp);
			const year = date.getFullYear();
			const month = String(date.getMonth() + 1).padStart(2, '0');
			const day = String(date.getDate()).padStart(2, '0');
			return `${year}-${month}-${day}`;
		},
		async loadFaqs(reset = false) {
			const token = uni.getStorageSync('token');
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				return;
			}
			if (this.faqLoading || (!this.faqHasMore && !reset)) return;
			if (reset) {
				this.faqList = [];
				this.faqPage = 1;
				this.faqHasMore = true;
			}
			this.faqLoading = true;
			try {
				const params = {
					pageNum: this.faqPage,
					pageSize: this.faqPageSize,
				};
				if (this.searchFaqQuestion) {
					params.question = this.searchFaqQuestion;
				}
				const res = await uni.request({
					url: `${this.$config.baseUrl}/faq/page`,
					method: 'GET',
					data: params,
					header: { 'Authorization': `Bearer ${token}` }
				});
				if (res.data.code !== 200) throw new Error(res.data.message || '接口错误');
				const newFaqs = res.data.data.records || [];
				if (newFaqs.length === 0) {
					this.faqHasMore = false;
					return;
				}
				this.faqList = this.faqList.concat(newFaqs);
				this.faqHasMore = newFaqs.length === this.faqPageSize;
				this.faqPage++;
			} catch (error) {
				uni.showToast({ title: error.message || '加载FAQ失败', icon: 'none' });
			} finally {
				this.faqLoading = false;
			}
		},
		openFaqForm(item = null) {
			this.showFaqForm = true;
			if (item) {
				this.editingFaq = item;
				this.faqForm = { ...item };
			} else {
				this.editingFaq = null;
				this.faqForm = { id: null, question: '', answer: '' };
			}
		},
		closeFaqForm() {
			this.showFaqForm = false;
			this.editingFaq = null;
			this.faqForm = { id: null, question: '', answer: '' };
		},
		async submitFaqForm() {
			const token = uni.getStorageSync('token');
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				return;
			}
			if (!this.faqForm.question || !this.faqForm.answer) {
				uni.showToast({ title: '请填写问题和答案', icon: 'none' });
				return;
			}
			uni.showLoading({ title: this.editingFaq ? '保存中...' : '新建中...' });
			try {
				let res;
				if (this.editingFaq) {
					// 编辑
					res = await uni.request({
						url: `${this.$config.baseUrl}/faq/${this.faqForm.id}`,
						method: 'PUT',
						data: {
							question: this.faqForm.question,
							answer: this.faqForm.answer
						},
						header: { 'Authorization': `Bearer ${token}` }
					});
				} else {
					// 新建
					res = await uni.request({
						url: `${this.$config.baseUrl}/faq`,
						method: 'POST',
						data: {
							question: this.faqForm.question,
							answer: this.faqForm.answer
						},
						header: { 'Authorization': `Bearer ${token}` }
					});
				}
				if (res.data.code === 200) {
					uni.showToast({ title: this.editingFaq ? '保存成功' : '新建成功', icon: 'success' });
					this.closeFaqForm();
					this.loadFaqs(true);
				} else {
					throw new Error(res.data.message || '操作失败');
				}
			} catch (error) {
				uni.showToast({ title: error.message || '操作失败', icon: 'none' });
			} finally {
				uni.hideLoading();
			}
		},
		async deleteFaq(id) {
			const token = uni.getStorageSync('token');
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				return;
			}
			uni.showModal({
				title: '确认删除',
				content: '确定要删除该FAQ吗？',
				success: async (res) => {
					if (res.confirm) {
						uni.showLoading({ title: '删除中...' });
						try {
							const res = await uni.request({
								url: `${this.$config.baseUrl}/faq/${id}`,
								method: 'DELETE',
								header: { 'Authorization': `Bearer ${token}` }
							});
							if (res.data.code === 200) {
								uni.showToast({ title: '删除成功', icon: 'success' });
								this.loadFaqs(true);
							} else {
								throw new Error(res.data.message || '删除失败');
							}
						} catch (error) {
							uni.showToast({ title: error.message || '删除失败', icon: 'none' });
						} finally {
							uni.hideLoading();
						}
					}
				}
			});
		},
		async loadFeedbacks(reset = false) {
			const token = uni.getStorageSync('token');
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				return;
			}
			if (this.feedbackLoading || (!this.feedbackHasMore && !reset)) return;
			if (reset) {
				this.feedbackList = [];
				this.feedbackPage = 1;
				this.feedbackHasMore = true;
			}
			this.feedbackLoading = true;
			try {
				const params = {
					pageNum: this.feedbackPage,
					pageSize: this.feedbackPageSize,
				};
				if (this.searchFeedbackUserId) params.userId = this.searchFeedbackUserId;
				if (this.searchFeedbackStartTime) params.startTime = this.searchFeedbackStartTime;
				if (this.searchFeedbackEndTime) params.endTime = this.searchFeedbackEndTime;
				const res = await uni.request({
					url: `${this.$config.baseUrl}/user/manage/feedback/page`,
					method: 'GET',
					data: params,
					header: { 'Authorization': `Bearer ${token}` }
				});
				if (res.data.code !== 200) throw new Error(res.data.message || '接口错误');
				const newFeedbacks = res.data.data.records || [];
				if (newFeedbacks.length === 0) {
					this.feedbackHasMore = false;
					return;
				}
				this.feedbackList = this.feedbackList.concat(newFeedbacks);
				this.feedbackHasMore = newFeedbacks.length === this.feedbackPageSize;
				this.feedbackPage++;
			} catch (error) {
				uni.showToast({ title: error.message || '加载反馈失败', icon: 'none' });
			} finally {
				this.feedbackLoading = false;
			}
		},
		async openFeedbackDetail(id) {
			const token = uni.getStorageSync('token');
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				return;
			}
			uni.showLoading({ title: '加载详情...' });
			try {
				const res = await uni.request({
					url: `${this.$config.baseUrl}/user/manage/feedback/${id}`,
					method: 'GET',
					header: { 'Authorization': `Bearer ${token}` }
				});
				if (res.data.code === 200) {
					this.feedbackDetail = res.data.data || {};
					this.showFeedbackDetail = true;
				} else {
					throw new Error(res.data.message || '获取详情失败');
				}
			} catch (error) {
				uni.showToast({ title: error.message || '获取详情失败', icon: 'none' });
			} finally {
				uni.hideLoading();
			}
		},
		closeFeedbackDetail() {
			this.showFeedbackDetail = false;
			this.feedbackDetail = {};
		},
		onFeedbackStartTimeChange(e) {
			this.searchFeedbackStartTime = e.detail.value;
			this.loadFeedbacks(true);
		},
		onFeedbackEndTimeChange(e) {
			this.searchFeedbackEndTime = e.detail.value;
			this.loadFeedbacks(true);
		},
		onFeedbackFilterConfirm() {
			this.loadFeedbacks(true);
		},
		onReachBottom() {
			if (this.activeManagementSection === 'policy' && !this.loading && this.hasMore) {
				this.loadPolicies();
			}
		},
	}
}
</script>

<style lang="scss">
/* 顶部信息栏 */
.admin-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 40rpx 30rpx;
	background: linear-gradient(135deg, #1a237e 0%, #0d47a1 100%);
	color: #fff;
	border-radius: 0 0 24rpx 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.admin-info {
	display: flex;
	align-items: center;
}

.admin-avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	border: 4rpx solid rgba(255, 255, 255, 0.8);
	margin-right: 20rpx;
}

.admin-details {
	display: flex;
	flex-direction: column;
}

.admin-name {
	font-size: 38rpx;
	font-weight: bold;
}

.admin-role {
	font-size: 26rpx;
	opacity: 0.8;
}

.admin-stats {
	display: flex;
	gap: 40rpx;
}

.stat-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.stat-num {
	font-size: 34rpx;
	font-weight: bold;
}

.stat-label {
	font-size: 24rpx;
	opacity: 0.8;
}

/* 快捷功能入口 */
.quick-actions {
	margin: 40rpx 30rpx;
	background: #fff;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
	padding: 30rpx;
}

.action-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 20rpx;
}

.action-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 20rpx;
	background-color: #f8f9fa;
	border-radius: 12rpx;
}

.action-item .iconfont {
	font-size: 48rpx;
	color: #007AFF;
	margin-bottom: 10rpx;
}

.action-label {
	font-size: 24rpx;
	color: #333;
}

/* 最近活动列表 */
.recent-activities {
	margin: 40rpx 30rpx;
	background: #fff;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
	padding: 30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
}

.activity-list {
	.activity-item {
		padding: 20rpx 0;
		border-bottom: 1rpx solid #eee;
		
		&:last-child {
			border-bottom: none;
		}
	}
}

.activity-time {
	font-size: 24rpx;
	color: #999;
	margin-bottom: 8rpx;
}

.activity-content {
	font-size: 28rpx;
	color: #333;
}

.policy-manage-card {
	background: #fff;
	border-radius: 16rpx;
	margin: 30rpx 0;
	padding: 30rpx;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05);
}
.manage-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}
.manage-title {
	font-size: 32rpx;
	font-weight: bold;
}
.create-btn {
	background: #1976d2;
	color: #fff;
	border-radius: 8rpx;
	padding: 12rpx 36rpx;
	font-size: 28rpx;
}
.manage-search {
	display: flex;
	gap: 20rpx;
	margin-bottom: 20rpx;
}
.search-input {
	flex: 1;
	padding: 16rpx;
	border-radius: 8rpx;
	border: 1rpx solid #eee;
	background: #f8f8f8;
	font-size: 28rpx;
}
.manage-list {
	margin-top: 10rpx;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}
.manage-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #eee;
	background: #fff;
	border-radius: 12rpx;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.03);
}
.item-info {
	flex: 1;
	cursor: pointer;
}
.item-title {
	font-size: 30rpx;
	font-weight: 500;
	color: #333;
}
.item-summary {
	font-size: 26rpx;
	color: #888;
	margin-left: 20rpx;
}
.item-actions {
	display: flex;
	gap: 10rpx;
}
.edit-btn, .delete-btn {
	padding: 8rpx 24rpx;
	border-radius: 8rpx;
	font-size: 26rpx;
}
.edit-btn {
	background: #4caf50;
	color: #fff;
}
.delete-btn {
	background: #f44336;
	color: #fff;
}
.policy-form-modal {
	position: fixed;
	left: 0; top: 0; right: 0; bottom: 0;
	background: rgba(0,0,0,0.3);
	z-index: 999;
	display: flex;
	align-items: center;
	justify-content: center;
}
.policy-form {
	background: #fff;
	border-radius: 16rpx;
	padding: 40rpx 30rpx;
	width: 90vw;
	max-width: 600rpx;
	box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.12);
}
.form-title {
	font-size: 32rpx;
	font-weight: bold;
	margin-bottom: 20rpx;
}
.form-input, .form-textarea {
	width: 100%;
	margin-bottom: 20rpx;
	padding: 16rpx;
	border-radius: 8rpx;
	border: 1rpx solid #eee;
	font-size: 28rpx;
	background: #f8f8f8;
}
.form-textarea {
	min-height: 120rpx;
	resize: vertical;
}
.form-actions {
	display: flex;
	justify-content: flex-end;
	gap: 20rpx;
}
.save-btn {
	background: #1976d2;
	color: #fff;
	border-radius: 8rpx;
	padding: 12rpx 36rpx;
	font-size: 28rpx;
}
.cancel-btn {
	background: #eee;
	color: #333;
	border-radius: 8rpx;
	padding: 12rpx 36rpx;
	font-size: 28rpx;
}
.date-picker {
	display: flex;
	align-items: center;
	justify-content: space-between;
}
.picker-text {
	color: #333;
	font-size: 30rpx;
}
.faq-manage-card {
	background: #fff;
	border-radius: 16rpx;
	margin: 30rpx 0;
	padding: 30rpx;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05);
}
.manage-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}
.manage-title {
	font-size: 32rpx;
	font-weight: bold;
}
.create-btn {
	background: #1976d2;
	color: #fff;
	border-radius: 8rpx;
	padding: 12rpx 36rpx;
	font-size: 28rpx;
}
.manage-search {
	display: flex;
	gap: 20rpx;
	margin-bottom: 20rpx;
}
.search-input {
	flex: 1;
	padding: 16rpx;
	border-radius: 8rpx;
	border: 1rpx solid #eee;
	background: #f8f8f8;
	font-size: 28rpx;
}
.manage-list {
	margin-top: 10rpx;
}
.manage-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #eee;
}
.item-info {
	flex: 1;
	cursor: pointer;
}
.item-title {
	font-size: 30rpx;
	font-weight: 500;
	color: #333;
}
.item-summary {
	font-size: 26rpx;
	color: #888;
	margin-left: 20rpx;
}
.item-actions {
	display: flex;
	gap: 10rpx;
}
.edit-btn, .delete-btn {
	padding: 8rpx 24rpx;
	border-radius: 8rpx;
	font-size: 26rpx;
}
.edit-btn {
	background: #4caf50;
	color: #fff;
}
.delete-btn {
	background: #f44336;
	color: #fff;
}
.feedback-manage-card {
	background: #fff;
	border-radius: 16rpx;
	margin: 30rpx 0;
	padding: 30rpx;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05);
}
.manage-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}
.manage-title {
	font-size: 32rpx;
	font-weight: bold;
}
.filter-bar {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-bottom: 20rpx;
}
.short-input {
	width: 200rpx;
	min-width: 120rpx;
	max-width: 240rpx;
}
.date-input {
	width: 180rpx;
	min-width: 120rpx;
	max-width: 220rpx;
	text-align: center;
	background: #fff;
}
.filter-btn {
	background: #1976d2;
	color: #fff;
	border-radius: 8rpx;
	padding: 12rpx 36rpx;
	font-size: 28rpx;
	margin-left: 8rpx;
}
</style> 