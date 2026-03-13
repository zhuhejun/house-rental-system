<template>
    <div class="app-container">

        <div v-if="loading" class="global-loading">
            <i class="el-icon-loading"></i>
            <div>正在加载中...</div>
        </div>

        <div v-else>
            <!-- 顶部导航栏 -->
            <header class="app-header">
                <div class="position" v-if="userArea.length !== 0">
                    <i style="font-size: 18px;margin-right: 6px;" class="el-icon-location"></i>
                    <span style="font-size: 18px;">{{ userArea[0].topAreaName }} · {{ userArea[0].cityAreaName }}</span>
                    <span @click="changeAddress" class="change-address">更换常居住地</span>
                </div>
                <div class="header-content">
                    <div class="main-nav">
                        <Logo class="logo" :logoSize="Number(26)" :fontSize="Number(20)" textColor="rgb(51,51,51)"
                            sysName="海螺租房" />
                    </div>
                    <nav class="main-nav">
                        <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="nav-item"
                            active-class="active" @click="closeMobileMenu(item)">
                            <i style="margin-right: 6px;" :class="item.icon"></i>
                            {{ item.title }}
                        </router-link>
                        <el-dropdown trigger="click">
                            <span class="user-dropdown-trigger">
                                <div class="user-info">
                                    <img :src="userInfo.avatar" alt="用户头像" class="user-avatar">
                                    <span class="username">{{ userInfo.username }}</span>
                                    <i style="color: rgb(51,51,51);" class="el-icon-arrow-down dropdown-icon"></i>
                                </div>
                            </span>
                            <el-dropdown-menu slot="dropdown" class="user-dropdown">
                                <el-dropdown-item v-if="landlord !== null" @click.native="serviceCenter"
                                    icon="el-icon-menu">
                                    服务中心
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="updateUserInfo" icon="el-icon-user-solid">
                                    修改信息
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="dialogPasswordOperation = true"
                                    icon="el-icon-finished">
                                    修改密码
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="dialogOutOperation = true" icon="el-icon-s-fold">
                                    退出登录
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </el-dropdown>
                    </nav>
                </div>
            </header>

            <!-- 主内容区 -->
            <main class="app-main">
                <router-view></router-view>
            </main>
        </div>



        <!-- 退出登录弹窗 -->
        <el-dialog title="退出登录" :show-close="false" :visible.sync="dialogOutOperation" width="20%">
            <span>确定退出登录？</span>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="dialogOutOperation = false">取消</span>
                <span class="info-bt" @click="confirmLoginOut">确定</span>
            </span>
        </el-dialog>

        <!-- 修改密码弹窗 -->
        <el-dialog title="修改密码" :closeOnClickModal="false" :show-close="false" :visible.sync="dialogPasswordOperation"
            width="28%">
            <div>
                <div>
                    <p>*原始密码</p>
                    <el-input style="height: 60px;line-height: 60px;" show-password placeholder="输入" type="password"
                        v-model="oldPassword" clearable>
                    </el-input>
                </div>
                <div>
                    <p>*新密码</p>
                    <el-input style="height: 60px;line-height: 60px;" show-password placeholder="输入" type="password"
                        v-model="newPassword" clearable>
                    </el-input>
                </div>
                <div>
                    <p>*确认密码</p>
                    <el-input style="height: 60px;line-height: 60px;" show-password placeholder="输入" type="password"
                        v-model="againPassword" clearable>
                    </el-input>
                </div>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="dialogPasswordOperation = false">取消</span>
                <span class="info-bt" @click="confirmUpdatePassword">确定</span>
            </span>
        </el-dialog>

        <!-- 用户信息操作弹窗 -->
        <el-dialog title="修改信息" :show-close="false" :visible.sync="dialogUserInfoVisible" :closeOnClickModal="false"
            width="35%">
            <el-tabs v-model="tabActiveName" :tab-position="tabPosition" style="height: 420px;">
                <el-tab-pane label="核心信息" name="first">
                    <div>
                        <div class="user-avatar-container">
                            <p>点击📷处即可上传头像</p>
                            <img v-if="avatar" :src="avatar || ''" alt="用户头像" class="user-avatar-large">
                            <el-upload style="margin-left: 20px;" class="avatar-uploader"
                                action="api/v1.0/house-rental-api/file/upload" :show-file-list="false"
                                :on-success="handleImageSuccess">
                                <i class="el-icon-camera-solid upload-icon"></i>
                            </el-upload>
                        </div>
                        <div class="form-group">
                            <p>*用户账号</p>
                            <el-input :disabled="true" placeholder="输入" v-model="apiParam.account" clearable>
                            </el-input>
                        </div>
                        <div class="form-group">
                            <p>*用户名</p>
                            <el-input placeholder="输入" v-model="apiParam.username" clearable>
                            </el-input>
                        </div>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="基本信息" name="second">
                    <div>
                        <div class="form-group">
                            <p>用户性别</p>
                            <Tab :buttons="[
                                { label: '女', value: '1' },
                                { label: '男', value: '2' }
                            ]" :initialActive="String(apiParam.gender) || '2'" @change="handleGenderChange" />
                        </div>
                        <div class="form-group">
                            <p>电子邮件</p>
                            <el-input placeholder="输入" v-model="apiParam.email" clearable>
                            </el-input>
                        </div>
                        <div class="form-group">
                            <p>联系电话</p>
                            <el-input placeholder="输入" v-model="apiParam.phone" clearable>
                            </el-input>
                        </div>
                        <div class="form-group">
                            <p>出生年月</p>
                            <el-date-picker style="width: 100%;" v-model="apiParam.birthday" type="date"
                                placeholder="选择日期">
                            </el-date-picker>
                        </div>
                    </div>
                </el-tab-pane>
            </el-tabs>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="dialogUserInfoVisible = false">取消</span>
                <span class="info-bt" @click="handleConfirm">
                    确定修改
                </span>
            </span>
        </el-dialog>

        <!-- 设置地区信息 -->
        <el-dialog title="设置地区信息" :closeOnClickModal="false" :show-close="false" :visible.sync="dialogUserArea"
            width="30%">
            <div>
                <p>*所属省份</p>
                <el-select @change="handleAreaChange" style="width: 100%;" v-model="topAreaId" placeholder="请选择">
                    <el-option v-for="item in topArea" :key="item.id" :label="item.name" :value="item.id">
                    </el-option>
                </el-select>
                <p>*所属市区</p>
                <el-select style="width: 100%;" v-model="cityAreaId" placeholder="请选择">
                    <el-option v-for="item in cityArea" :key="item.id" :label="item.name" :value="item.id">
                    </el-option>
                </el-select>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="dialogUserArea = false">取消</span>
                <span v-if="userArea.length === 0" class="info-bt" @click="confirmUserAreaSet">确定设置</span>
                <span v-else class="info-bt" @click="confirmUserAreaUpdate">确定修改</span>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import Logo from "@/components/Logo"
import AutoInput from '@/components/AutoInput.vue';
import Tab from "@/components/Tab"
import {
    setUserInfo,
    getUserInfo,
    clearToken,
    clearRole
} from "@/utils/storage"

export default {
    components: { Logo, AutoInput, Tab },
    name: "AppLayout",
    data() {
        return {
            loading: true,
            dialogUserArea: false, // 居住地弹窗默认不显示
            oldPassword: '', // 原始密码
            newPassword: '', // 新密码
            againPassword: '', // 确认密码
            tabActiveName: 'first', // 修改信息Tab默认选中第一项
            tabPosition: 'right', // 修改信息Tab靠右
            dialogPasswordOperation: false, // 修改密码弹窗开关
            dialogUserInfoVisible: false, // 修改信息弹窗开关
            dialogOutOperation: false, // 退出登录弹窗控制开关
            navItems: [
                { path: '/home', icon: '', title: '首页' },
                { path: '/house-news', icon: 'el-icon-tickets', title: '房屋资讯' },
                { path: '/my-house-order-info', icon: 'el-icon-c-scale-to-original', title: '预约看房' },
                { path: '/my-rental-contract', icon: 'el-icon-document', title: '我的合同' },
                { path: '/my-rental-bill', icon: 'el-icon-wallet', title: '我的账单' },
                { path: '/notice-list', icon: '', title: '系统公告' },
                { path: '/my-save', icon: '', title: '我的收藏' },
            ],
            userInfo: {},
            isAuthChecked: false,
            isMobileMenuOpen: false,
            apiParam: {},
            avatar: '', // 用户头像
            userArea: [],
            topArea: [], // 省份信息存放数组
            cityArea: [], // 市区存放数组
            topAreaId: null,
            cityAreaId: null,
            landlord: {},
        }
    },
    async created() {
        // 修改：使用Promise.all确保所有数据加载完成
        try {
            await Promise.all([
                this.handleAuthentication(),
                this.fetchUserArea(),
                this.fetchLadlordReplyInfo()
            ]);

            if (this.isAuthChecked) {
                this.initRouteHandling();
            }
        } catch (error) {
            console.error('初始化失败:', error);
        } finally {
            this.loading = false;
        }
    },
    methods: {
         async fetchLadlordReplyInfo() {
            try {
                const { data } = await this.$axios.post('/landlord/listUser', {});
                this.landlord = data;
            } catch (error) {
                console.log("查询房东申请信息异常：", error);
                this.landlord = null; // 确保有默认值
            }
        },
        changeAddress() {
            this.dialogUserArea = true;
            this.fetchTopArea();
            this.topAreaId = this.userArea[0].topAreaId;
            this.handleAreaChange();
            this.cityAreaId = this.userArea[0].cityAreaId;
        },
         async confirmUserAreaUpdate() {
            try {
                const userArea = {
                    id: this.userArea[0].id,
                    areaId: this.cityAreaId,
                }
                const { message } = await this.$axios.put('/user-area/update', userArea);
                this.$message.success(message);
                this.dialogUserArea = false;
                await this.fetchUserArea(); // 重新加载
                
                // 通知其他组件地区已更新
                if (this.$bus) {
                    this.$bus.$emit('areaChanged');
                }
            } catch (error) {
                console.log("修改常居住地异常", error);
            }
        },
        // 设置常居住地信息
         async confirmUserAreaSet() {
            try {
                const userArea = { areaId: this.cityAreaId }
                const { message } = await this.$axios.post('/user-area/save', userArea);
                this.$message.success(message);
                this.dialogUserArea = false;
                await this.fetchUserArea(); // 重新加载
                
                // 通知其他组件地区已更新
                if (this.$bus) {
                    this.$bus.$emit('areaChanged');
                }
            } catch (error) {
                console.log("设置常居住地异常", error);
            }
        },
        async handleAreaChange() {
            this.cityAreaId = null;
            try {
                const areaQueryDto = { parentId: this.topAreaId }
                const { data } = await this.$axios.post('/area/list', areaQueryDto);
                this.cityArea = data;
            } catch (error) {
                console.log("查询省份下的市区信息异常：", error);
            }
        },
        async fetchTopArea() {
            try {
                const areaQueryDto = { parentId: 0 }
                const { data } = await this.$axios.post('/area/list', areaQueryDto);
                this.topArea = data;
            } catch (error) {
                console.log("查询省份信息异常：", error);
            }
        },
        // 查询用户常居住地信息
        async fetchUserArea() {
            try {
                const { data } = await this.$axios.post('/user-area/listUser', {});
                this.userArea = data;
                // 缓存到localStorage，供其他页面使用
                if (data.length > 0) {
                    localStorage.setItem('userAreaInfo', JSON.stringify({
                        topAreaId: data[0].topAreaId,
                        cityAreaId: data[0].cityAreaId,
                        timestamp: Date.now()
                    }));
                }
            } catch (error) {
                console.log("查询用户常居住地信息异常：", error);
            }
        },
        // 性别选择切换
        handleGenderChange(obj) {
            this.apiParam.gender = Number(obj.value);
        },
        handleImageSuccess(res, file) {
            this.$notify({
                title: '头像上传',
                type: res.code === 200 ? 'success' : 'error',
                message: res.code === 200 ? '上传成功' : res.data,
                position: 'bottom-right',
                duration: 1000,
            })
            if (res.code === 200) {
                this.avatar = res.data;
            }
        },
        serviceCenter() {
            window.open('service-center', '_blank');
        },
        updateUserInfo() {
            this.avatar = this.userInfo.avatar;
            this.dialogUserInfoVisible = true;
        },
        // 修改信息
        async handleConfirm() {
            try {
                this.apiParam.avatar = this.avatar;
                const { data, message } = await this.$axios.put('/user/update', this.apiParam);
                this.apiParam = data;
                this.$message.success(message);
                this.handleAuthentication();
                this.dialogUserInfoVisible = false; // 关闭弹窗
            } catch (error) {
                this.$message.warning(error.message);
            }
        },
        // 修改密码
        async confirmUpdatePassword() {
            if (!this.oldPassword || !this.newPassword || !this.againPassword) {
                this.$message('原始密码、新密码、确认密码不能为空哦');
                return;
            }
            if (this.newPassword !== this.againPassword) {
                this.$message('前后密码输入不一致');
                return;
            }
            const updatePasswordDto = {
                oldPassword: this.$md5(this.$md5(this.oldPassword)),
                newPassword: this.$md5(this.$md5(this.newPassword)),
                againPassword: this.$md5(this.$md5(this.againPassword))
            }
            try {
                const { message } = await this.$axios.put('/user/updatePassword', updatePasswordDto);
                this.$notify.success({
                    title: '密码修改',
                    message: message,
                    duration: 1000,
                    position: 'bottom-right'
                })
                this.confirmLoginOut();
            } catch (error) {
                this.$message.error(error.message);
            }
        },
        // 退出登录
        confirmLoginOut() {
            clearToken();
            clearRole();
            this.$router.push('/');
        },
        closeMobileMenu(item) {
            this.isMobileMenuOpen = false;
        },
        async handleAuthentication() {
            if (this.$route.path === '/login') {
                this.isAuthChecked = true;
                return;
            }

            try {
                const localUser = getUserInfo();
                if (localUser && localUser.token) {
                    this.userInfo = localUser;
                    this.isAuthChecked = true;
                    return;
                }

                const data = await this.$axios.get('/user/auth');
                if (data.code === 200) {
                    this.userInfo = data.data;
                    this.apiParam = { ...data.data };
                    setUserInfo(this.userInfo);
                    this.isAuthChecked = true;
                } else {
                    this.redirectToLogin();
                }
            } catch (error) {
                console.error('认证失败:', error);
                this.redirectToLogin();
            }
        },
        initRouteHandling() {
            this.setupRouteWatcher();
            if (!this.isValidRoute(this.$route.path)) {
                this.$router.replace('/home');
            }
            if (this.$route.path === '/') {
                this.$router.replace('/home');
            }
        },
        isValidRoute(path) {
            return this.navItems.some(item => item.path === path);
        },
        setupRouteWatcher() {
            this.$router.afterEach((to) => {
                if (this.isValidRoute(to.path)) {
                    // 可以在这里记录访问路径
                }
            });
        },
         redirectToLogin() {
            if (this.$route.path !== '/login') {
                this.loading = false; // 确保loading被关闭
                this.$notify({
                    type: 'error',
                    title: '安全认证',
                    message: '请重新登录',
                    duration: 2000,
                    position: 'bottom-right'
                });
                this.$router.replace('/login');
            }
        }
    }
}
</script>

<style scoped lang="scss">
.position {
    padding: 20px 65px;
    background-color: rgb(51,51,51);
    color: #fff;

    .change-address {
        font-size: 12px;
        margin-left: 10px;
        cursor: pointer;
        background-color: rgb(51,51,51);
        padding: 4px 10px;
        border-radius: 5px;

        // &:hover {
        //     background-color: rgb(230, 230, 230);
        // }
    }
}

.publish-pet-post {
    background-color: rgb(26, 147, 62);
    color: rgb(255, 255, 255);
    padding: 4px 16px;
    border-radius: 20px;
    font-size: 14px;
    cursor: pointer;

    &:hover {
        background-color: rgb(26, 134, 58);
    }
}

.app-container {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    // background-color: rgb(240, 240, 240);

}

.app-header {
    background-color: rgb(255, 255, 255);
    margin-bottom: 10px;
    z-index: 1000;
    position: sticky;
    top: 0;
    transition: all 0.3s ease;
    //box-shadow: 0 4px 8px rgb(230, 230, 230);
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 0 auto;
    padding: 20px 50px;
    height: 25px;
    position: relative;
}

.logo {
    display: flex;
    align-items: center;
    cursor: pointer;
    transition: transform 0.3s ease;
}

.main-nav {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 26px;
}

.nav-item {
    color: rgb(51, 51, 51);
    font-size: 16px;
    text-decoration: none;
    height: 74px;
    line-height: 74px;
    position: relative;
    transition: all 0.2s ease;
    white-space: nowrap;
    display: flex;
    align-items: center;
    justify-content: center;

    &.active {
        color: rgb(31, 31, 31);
    }

    &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 2px;
        background: white;
        border-radius: 2px;
        transform: scaleX(0);
        transform-origin: left;
        transition: transform 0.3s ease;
    }
}

.input {
    width: 188px;
    transition: width 0.3s ease;
}

.user-dropdown-trigger {
    cursor: pointer;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: 16px;
    cursor: pointer;
    padding: 8px 16px;
    border-radius: 20px;
    position: relative;
    transition: all 0.2s ease;
    background-color: rgba(255, 255, 255, 0.15);
}

.user-avatar {
    width: 25px;
    height: 25px;
    border-radius: 50%;
    object-fit: cover;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.username {
    font-size: 14px;
    color: rgb(51, 51, 51);
    font-weight: 500;
}

.dropdown-icon {
    font-size: 12px;
    margin-left: 4px;
    transition: transform 0.3s ease;
}

.user-dropdown {
    border-radius: 8px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
    padding: 8px 0;
    animation: dropdownFadeIn 0.3s ease;
}

.dropdown-item {
    display: flex;
    align-items: center;
    padding: 10px 24px;
    color: #4a5568;
    text-decoration: none;
    transition: all 0.2s ease;
    font-size: 14px;

    i {
        margin-right: 12px;
        font-size: 16px;
        color: #15559a;
    }

    &:hover {
        background-color: #f0f7ff;
        color: #15559a;
        padding-left: 28px;
    }
}

.app-main {
    flex: 1;
    box-sizing: border-box;
    // background-color: rgb(240, 240, 240);
    border-radius: 5px;
    padding-inline: 20px;
    box-sizing: border-box;
}

.user-avatar-container {
    text-align: center;
    margin-bottom: 30px;

    p {
        font-size: 14px;
        color: #6b7280;
        margin-bottom: 15px;
    }
}

.user-avatar-large {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    margin-bottom: 15px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;

    &:hover {
        transform: scale(1.05);
    }
}

.avatar-uploader {
    display: inline-block;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: rgba(21, 85, 154, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    margin-top: -20px;

    i {
        color: #15559a;
        font-size: 18px;
    }

    &:hover {
        background-color: rgba(21, 85, 154, 0.2);
        transform: scale(1.1);
    }
}

.upload-icon {
    color: #15559a;
}

.form-group {
    margin-bottom: 24px;

    p {
        font-size: 14px;
        color: #4b5563;
        margin-bottom: 8px;
        font-weight: 500;
    }

    .el-input {
        width: 100%;
        border-radius: 6px;
        transition: border-color 0.3s ease, box-shadow 0.3s ease;

        &:focus-within {
            border-color: #15559a;
            box-shadow: 0 0 0 2px rgba(21, 85, 154, 0.2);
        }
    }
}

/* 动画定义 */
@keyframes dropdownFadeIn {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.global-loading {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: white;
    z-index: 9999;
    
    .loading-icon {
        font-size: 40px;
        color: #15559a;
        margin-bottom: 20px;
        animation: rotate 2s linear infinite;
    }
    
    div {
        color: #666;
        font-size: 16px;
    }
}

/* 添加旋转动画 */
@keyframes rotate {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}

.position {
    padding: 10px 65px;
    background-color: rgb(51,51,51);
    color: rgb(250,250,250);
    min-height: 38px; /* 确保高度固定，避免闪烁 */
    display: flex;
    align-items: center;
    
    .change-address {
        font-size: 12px;
        margin-left: 10px;
        cursor: pointer;
        background-color: rgb(51,51,51);
        padding: 4px 10px;
        border-radius: 5px;

    }
}

</style>
