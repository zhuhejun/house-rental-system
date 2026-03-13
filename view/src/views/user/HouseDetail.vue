<template>
    <div class="container">
        <div class="house-detail-container">
            <div style="margin-bottom: 20px;">
                <el-breadcrumb separator-class="el-icon-arrow-right">
                    <el-breadcrumb-item :to="{ path: '/user' }">首页</el-breadcrumb-item>
                    <el-breadcrumb-item>{{ house.name }}</el-breadcrumb-item>
                </el-breadcrumb>
            </div>
            <!-- 顶部图片展示区 -->
            <div class="gallery-section">
                <div class="main-image">
                    <img v-if="coverList.length > 0" :src="coverList[currentImageIndex].url" alt="房屋封面图">
                </div>
                <div class="thumbnail-list">
                    <div v-for="(cover, index) in coverList" :key="cover.uid" class="thumbnail"
                        @click="currentImageIndex = index">
                        <img :src="cover.url" alt="房屋实景图">
                    </div>
                </div>
            </div>

            <!-- 基本信息区 -->
            <div class="basic-info-section">
                <h1 class="house-title">{{ house.name }}</h1>

                <div class="price-section">
                    <div>
                        <span class="price">¥{{ house.rent }}</span>
                        <span class="unit">/月</span>
                        <span class="deposit-method">{{ house.depositMethodName }}</span>
                    </div>
                    <div style="display: flex;gap: 10px;">
                        <!-- <el-button type="primary" @click="contactLandlord">预约看房</el-button>
                        <el-button @click="collectHouseOperation">{{ saveList.length > 0 ? '取消收藏' : '收藏' }}</el-button> -->
                        <div class="save" @click="contactLandlord">
                            <i class="el-icon-date"></i>
                            预约看房
                        </div>
                        <div class="save" @click="collectHouseOperation">
                            <i :class="saveList.length > 0 ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
                            {{ saveList.length > 0 ? '取消收藏' : '收藏' }}
                        </div>
                    </div>

                </div>

                <div class="meta-info">
                    <div class="meta-item" @click="communityInfo(house)">
                        <i class="el-icon-location"></i>
                        <span>{{ house.cityAreaName }} · {{ house.communityName }}</span>
                    </div>
                    <div class="meta-item">
                        <i class="el-icon-house"></i>
                        <span>{{ house.typeName }} · {{ house.sizeNumber }}m²</span>
                    </div>
                    <div class="meta-item">
                        <i class="el-icon-position"></i>
                        <span>{{ house.directionName }}</span>
                    </div>
                </div>
            </div>

            <!-- 详细信息区 -->
            <div class="detail-section">
                <div class="section-title">房屋详情</div>
                <div class="detail-grid">
                    <div class="detail-item">
                        <span class="label">户型</span>
                        <span class="value">{{ house.sizedName }}</span>
                    </div>
                    <div class="detail-item">
                        <span class="label">楼层</span>
                        <span class="value">{{ house.floor }}</span>
                    </div>
                    <div class="detail-item">
                        <span class="label">装修</span>
                        <span class="value">{{ house.fitmentStatusName }}</span>
                    </div>
                    <div class="detail-item">
                        <span class="label">地铁</span>
                        <span class="value">{{ house.isSubway ? `${house.subwayLine}号线` : '不临近' }}</span>
                    </div>
                    <div class="detail-item">
                        <span class="label">租赁方式</span>
                        <span class="value">{{ house.rentalTypeName }}</span>
                    </div>
                    <div class="detail-item">
                        <span class="label">发布时间</span>
                        <span class="value">{{ house.createTime }}</span>
                    </div>
                </div>
            </div>

            <!-- 设施配置区 -->
            <div class="facilities-section">
                <div class="section-title">生活设施</div>
                <div class="facilities-grid">
                    <div v-for="(item, key) in houseLivingFacilityList" :key="key" class="facility-item"
                        :class="{ 'available': Boolean(item.selected) }">
                        <i :class="getFacilityIcon(key)"></i>
                        <span>{{ item.label }}</span>
                    </div>
                </div>
            </div>

            <!-- 房屋描述 -->
            <div class="description-section">
                <div class="section-title">房屋描述</div>
                <div class="description-content" v-html="house.detail"></div>
            </div>

            <!-- 服务评价 -->
            <div class="description-section">
                <div class="section-title">服务评价</div>
                <div>
                    <div style="margin-bottom: 10px;" v-for="(evaluations, index) in houseOrderEvaluations"
                        :key="index">
                        <div>
                            <div
                                style="margin-bottom: 6px;display: flex;justify-content: left;align-items: center;gap: 10px;">
                                <div>
                                    <img style="width: 30px;height: 30px;border-radius: 50%;" :src="evaluations.avatar"
                                        alt="">
                                </div>
                                <div>
                                    <div style="font-size: 16px;padding-left: 5px;">{{ evaluations.username }}</div>
                                    <div>
                                        <el-rate v-model="evaluations.score" disabled show-score text-color="#ff9900"
                                            score-template="{value}">
                                        </el-rate>
                                    </div>
                                </div>
                            </div>
                            <div
                                style="color: #666666;background-color: rgb(246,246,246);padding: 10px;font-size: 14px;">
                                {{ evaluations.text }}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 底部操作栏 -->
            <div class="action-bar">
                <div class="contact-info">
                    <div class="avatar">
                        <img :src="house.landlordAvatar" alt="房东头像">
                    </div>
                    <div class="info">
                        <div class="name">{{ house.landlordName }}</div>
                        <div class="role">
                            <div class="save" @click="fetchLandlordHouseList">
                                <i class="el-icon-s-home"></i>
                                查看房东名下待租房源
                            </div>
                        </div>
                    </div>
                </div>
                <div class="action-buttons">

                </div>
            </div>
        </div>
        <!-- 功能区 -->
        <div class="fun-container">
            <Evaluations :userId="Number(userId)" :avatar="String(avatar)" :contentId="Number(houseId)"
                contentType="HOUSE_INFO" />
        </div>


        <!-- 预约看房信息确认框 -->
        <el-drawer title="预约看房" size="40%" :visible.sync="drawerHouseOrder" :direction="direction"
            :before-close="handleClose">
            <div class="house-order-drawer">
                <div class="contact-info">
                    <div class="avatar">
                        <img :src="house.landlordAvatar" alt="房东头像">
                    </div>
                    <div class="info">
                        <div class="name">{{ house.landlordName }}</div>
                        <div class="role">房东</div>
                    </div>
                </div>
                <!-- 步骤条 -->
                <div>
                    <el-steps :space="200" :active="activePath" finish-status="success">
                        <el-step title="选择日期"></el-step>
                        <el-step title="选择时间段"></el-step>
                    </el-steps>
                </div>
                <!-- 参数选择信息 -->
                <div style="padding-block: 10px;">
                    <div v-if="activePath === 0">
                        <div :style="{ border: selctedDateItem === date ? '1px solid rgb(0, 119, 184)' : '' }"
                            class="item-date" @click="dateSelected(date)" v-for="(date, index) in dateOrderList"
                            :key="index">
                            {{ date }}
                        </div>
                    </div>
                    <div v-else-if="activePath === 1">
                        <div :style="{ border: selctedDateSplitItem && selctedDateSplitItem.value === split.value ? '1px solid rgb(0, 119, 184)' : '' }"
                            class="item-date" @click="dateSplitSelected(split)"
                            v-for="(split, index) in dateOrderSplitList" :key="index">
                            {{ split.label }}
                        </div>
                    </div>
                </div>
                <!-- 下一步 -->
                <div style="text-align: center;display: flex;align-items: center;justify-content: center;">
                    <div class="primary-bt" @click="last">
                        <i class="el-icon-caret-right"></i>
                        上一步
                    </div>
                    <div class="primary-bt" @click="next">
                        <i class="el-icon-caret-right"></i>
                        下一步
                    </div>
                    <div class="info-bt" @click="handleConfirmOrder">
                        确定预约
                    </div>
                </div>
            </div>
        </el-drawer>

        <el-drawer title="待租房源" size="40%" :visible.sync="drawerLandlordHouseList"
            :direction="directionLandlordHouseList" :before-close="handleLandlordHouseClose">
            <div>
                <div class="house-list">
                    <div v-if="landlordHouseList.length === 0">
                        <el-empty description="暂无房屋信息"></el-empty>
                    </div>
                    <div @click="houseItemClick(item.id)" class="house-item" v-for="item in landlordHouseList"
                        :key="item.id">
                        <img :src="item.cover" alt="">
                        <div>
                            <div class="name">{{ item.name }}</div>
                            <div class="point">
                                <div>
                                    <i class="el-icon-location"></i>
                                    {{ item.cityAreaName }}&nbsp;·&nbsp;{{ item.communityName }}
                                </div>
                                <div>
                                    {{ item.depositMethodName }}
                                </div>
                                <div>
                                    {{ item.sizeNumber }}m²
                                </div>
                                <div>
                                    {{ item.directionName }}
                                </div>
                                <div>
                                    {{ item.fitmentStatusName }}
                                </div>
                            </div>
                            <div class="rent">
                                ¥{{ item.rent }}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </el-drawer>
    </div>

</template>

<script>
import Evaluations from "@/components/Evaluations"
export default {
    components: { Evaluations },
    data() {
        return {
            activePath: 0,
            drawerHouseOrder: false,
            direction: 'rtl',
            startTime: 0,
            hasRecorded: false, // 防止重复记录
            indexCover: null,
            houseId: null,
            house: {},
            coverList: [],
            houseLivingFacilityList: {},
            currentImageIndex: 0,
            userId: null,
            avatar: '',
            saveList: [], // 用户收藏情况
            dateOrderSplitList: [],
            defaultSelectDays: 15,
            dateOrderList: [],
            selctedDateItem: null,
            selctedDateSplitItem: null,
            houseOrderEvaluations: [],
            landlordHouseList: [],
            directionLandlordHouseList: 'rtl', // 房东名下房源显示抽屉方向
            drawerLandlordHouseList: false,
        }
    },
    created() {
        this.getPathId();
        this.fetchUserInfo();
        this.fetchFutureDate(this.defaultSelectDays);
        this.fetchFutureDateSplit();
    },
    mounted() {
        this.startTime = performance.now(); // 更精确的高精度时间
        window.addEventListener('beforeunload', this.handlePageLeave);
    },
    beforeDestroy() {
        window.removeEventListener('beforeunload', this.handlePageLeave);
    },
    methods: {
        handleLandlordHouseClose() {
            this.drawerLandlordHouseList = false;
        },
        // 跳转至房屋详情页
        houseItemClick(id) {
            window.open(`/house-detail?id=${id}`, '_blank');
        },
        async fetchLandlordHouseList() {
            try {
                const { data } = await this.$axios.post(`/house/listUser`, { landlordId: this.house.landlordId });
                this.landlordHouseList = data;
                this.drawerLandlordHouseList = true;
            } catch (e) {
                this.$message.info(e.message);
                console.error('预约看房失败:', e);
            }
        },
        // 跳转小区详情页
        communityInfo(house) {
            window.open(`/community-detail?id=${house.communityId}`);
        },
        async handleConfirmOrder() {
            if (this.selctedDateItem === null) {
                this.$message.info('请选中日期');
                return;
            }
            if (this.selctedDateSplitItem === null) {
                this.$message.info('请选中预约时间段');
                return;
            }
            try {
                const houseOrderInfo = {
                    orderDate: this.selctedDateItem,
                    orderTimeSplit: this.selctedDateSplitItem.label,
                    houseId: this.houseId
                };
                await this.$axios.post(`/house-order-info/save`, houseOrderInfo);
                this.$message('预约看房成功');
                this.handleClose();
            } catch (e) {
                this.$message.info(e.message);
                console.error('预约看房失败:', e);
            }
        },
        dateSelected(date) {
            this.selctedDateItem = date;
        },
        dateSplitSelected(date) {
            this.selctedDateSplitItem = date;
        },
        last() {
            if (this.activePath-- <= 0) this.activePath = 1;
        },
        next() {
            if (this.activePath++ >= 1) this.activePath = 0;
        },
        handleClose() {
            this.drawerHouseOrder = false;
            this.selctedDateItem = null;
            this.selctedDateSplitItem = null;
        },
        handlePageLeave() {
            if (this.hasRecorded) return;

            const endTime = performance.now();
            const stayTime = Math.floor(endTime - this.startTime);

            this.hasRecorded = true;

            this.sendStayTime(stayTime);
        },
        async fetchFutureDate(days) {
            try {
                const { data } = await this.$axios.get(`/house-order-info/${days}`);
                this.dateOrderList = data;
            } catch (e) {
                console.error('查询预约日期失败:', e);
            }
        },
        // 查询服务评价
        async fetchHouseOrderEvaluations(id) {
            try {
                const { data } = await this.$axios.get(`/house-order-evaluations/houseList/${id}`);
                this.houseOrderEvaluations = data;
            } catch (e) {
                console.error('查询预约看房服务评价失败:', e);
            }
        },
        async fetchFutureDateSplit() {
            try {
                const { data } = await this.$axios.get(`/house-order-info/split`);
                this.dateOrderSplitList = data;
            } catch (e) {
                console.error('查询预约日期时间段失败:', e);
            }
        },
        async sendStayTime(duration) {
            try {
                await this.$axios.post('/flow-index/stayOperation', {
                    contentId: this.houseId,
                    times: duration,
                    contentType: 'HOUSE_INFO'
                });
            } catch (e) {
                console.error('记录停留时间失败:', e);
            }
        },
        getPathId() {
            this.houseId = this.$route.query.id;
            this.fetchHouseInfo(this.houseId);
            this.recordViewOperation(this.houseId);
            this.recordSaveStatus(this.houseId);
            this.fetchHouseOrderEvaluations(this.houseId);
        },
        // 查询用户对于内容的收藏情况
        async recordSaveStatus(id) {
            try {
                const flowIndexQueryDto = {
                    contentId: id,
                    contentType: 'HOUSE_INFO',
                    type: 2
                }
                const { data } = await this.$axios.post(`/flow-index/listUser`, flowIndexQueryDto);
                this.saveList = data;
            } catch (error) {
                console.info(error);
            }
        },
        // 浏览操作
        async recordViewOperation(id) {
            try {
                const flowIndex = {
                    contentId: id,
                    contentType: 'HOUSE_INFO'
                }
                await this.$axios.post(`/flow-index/viewOperation`, flowIndex);
            } catch (error) {
                console.info(error);
            }
        },
        async fetchUserInfo() {
            try {
                const { data } = await this.$axios.get(`/user/auth`);
                this.userId = data.id;
                this.avatar = data.avatar;
            } catch (error) {
                this.$message.error('获取用户信息失败');
                console.error(error);
            }
        },
        async fetchHouseInfo(id) {
            try {
                const { data } = await this.$axios.get(`/house/getById/${id}`);
                this.house = { ...data };
                this.coverList = (data.covers || '')
                    .split(',')
                    .filter(url => url.trim())
                    .map(url => ({
                        uid: Date.now() + Math.floor(Math.random() * 1000),
                        url: url.trim()
                    }));
                this.houseLivingFacilityList = JSON.parse(data.livingFacilities || '{}');
            } catch (error) {
                this.$message.error('获取房屋信息失败');
                console.error(error);
            }
        },
        getFacilityIcon(key) {
            const iconMap = {
                'fridge': 'el-icon-refrigerator',
                'washing': 'el-icon-washing-machine',
                'water': 'el-icon-water-cup',
                'network': 'el-icon-monitor',
                'sofa': 'el-icon-shopping-cart',
                'cooking': 'el-icon-knife-fork',
                'tv': 'el-icon-video-camera',
                'ac': 'el-icon-cold-drink',
                'wardrobe': 'el-icon-suitcase',
                'bed': 'el-icon-bed',
                'bathroom': 'el-icon-sunny',
                'balcony': 'el-icon-office-building',
                'heating': 'el-icon-sunrise',
                'cabinet': 'el-icon-box'
            };
            return iconMap[key] || 'el-icon-circle-check';
        },
        contactLandlord() {
            this.drawerHouseOrder = true;
        },
        // 收藏操作
        async collectHouseOperation() {
            try {
                const flowIndex = {
                    contentId: this.houseId,
                    contentType: 'HOUSE_INFO'
                }
                const { message } = await this.$axios.post(`/flow-index/saveOperation`, flowIndex);
                this.$notify.success({
                    title: '收藏操作',
                    message: message,
                    position: 'buttom-right',
                    duration: 1000,
                });
                this.recordSaveStatus(this.houseId);
            } catch (error) {
                this.$message.error(error);
                console.info(error);
            }
        }
    }
}
</script>

<style scoped lang="scss">
.house-list {
    display: flex;
    flex-wrap: wrap;
    margin: 10px;

    .house-item {
        flex: 1 1 400px;
        display: flex;
        gap: 10px;
        padding: 10px;
        box-sizing: border-box;
        cursor: pointer;

        .name {
            font-size: 18px;
        }

        img {
            width: 120px;
            height: 80px;
            border-radius: 5px;
        }

        .point {
            font-size: 12px;
            margin-block: 10px;
            display: flex;
            justify-content: left;
            align-items: center;
            gap: 10px;
            box-sizing: border-box;

            div {
                background-color: rgb(245, 246, 247);
                padding: 2px 4px;
            }
        }

        .rent {
            font-size: 22px;
            font-weight: 800;
            color: rgb(222, 88, 78);
        }
    }
}

.save {
    background-color: rgb(245, 245, 245);
    padding: 4px 10px;
    border-radius: 5px;
    cursor: pointer;

    &:hover {
        background-color: rgb(240, 240, 240);
    }
}

.house-order-drawer {
    padding: 20px;

    .contact-info {
        display: flex;
        padding: 10px 4px;
        align-items: center;
        background-color: rgb(246, 246, 246);
        margin-bottom: 10px;

        .avatar {
            width: 50px;
            height: 50px;
            margin-right: 15px;
            border-radius: 50%;
            overflow: hidden;

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }

        .info {
            .name {
                font-size: 24px;
                font-weight: 600;
                margin-bottom: 5px;
            }

            .role {
                font-size: 14px;
                color: #999;
            }
        }
    }

    .item-date {
        padding: 10px 0;
        background-color: rgb(246, 246, 246);
        border: 1px solid rgb(246, 246, 246);
        text-align: center;
        cursor: pointer;

        &:hover {
            background-color: rgb(244, 244, 244);
        }
    }


}

.container {
    display: flex;
}

.fun-container {
    width: calc(100% - 900px);
    position: sticky;
    top: 10px; // 与顶部保持间距
    align-self: flex-start; // 避免被拉伸
    height: fit-content; // 高度自适应内容
    margin-left: 10px; // 与左侧内容间隔
    padding: 15px;
    border-radius: 8px;
    background: #fff;
}

.house-detail-container {
    width: 800px;
    padding: 20px;
    background-color: rgb(255, 255, 255);
    // box-shadow: 0 4px 8px rgb(240,240,240);
    font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 图片展示区 */
.gallery-section {
    margin-bottom: 30px;
    border-radius: 8px;
    overflow: hidden;
    // box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

    .main-image {
        height: auto;
        // background: #f5f5f5;

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }

    .thumbnail-list {
        display: flex;
        padding: 10px;
        background: #fff;
        border: 1px solid rgb(240, 240, 240);
        overflow-x: auto;

        .thumbnail {
            width: 100px;
            height: 80px;
            margin-right: 10px;
            cursor: pointer;
            border: 2px solid transparent;
            transition: all 0.3s;

            &:hover {
                border-color: #409EFF;
            }

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }
    }
}

/* 基本信息区 */
.basic-info-section {
    margin-bottom: 30px;
    padding: 10px 20px;
    background: #fff;
    border-radius: 8px;
    // box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    position: sticky;
    top: 0;
    z-index: 1000;

    .house-title {
        font-size: 30px;
        margin-bottom: 15px;
        color: #333;
        font-weight: 600;
    }

    .price-section {
        display: flex;
        justify-content: space-between;
        margin-bottom: 20px;

        .price {
            font-size: 28px;
            color: #f56c6c;
            font-weight: 700;
        }

        .unit {
            font-size: 16px;
            color: #999;
            margin-right: 15px;
        }

        .deposit-method {
            font-size: 14px;
            color: #666;
            background: #f5f5f5;
            padding: 4px 8px;
            border-radius: 4px;
        }
    }

    .meta-info {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;

        .meta-item {
            display: flex;
            align-items: center;
            color: #666;

            i {
                margin-right: 5px;
                font-size: 16px;
            }

            span {
                font-size: 14px;
            }
        }
    }
}

/* 详细信息区 */
.detail-section {
    margin-bottom: 30px;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .section-title {
        font-size: 18px;
        font-weight: 600;
        margin-bottom: 20px;
        color: #333;
        padding-bottom: 10px;
        border-bottom: 1px solid #eee;
    }

    .detail-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 15px;

        .detail-item {
            display: flex;
            justify-content: space-between;

            .label {
                color: #999;
            }

            .value {
                font-weight: 500;
                color: #333;
            }
        }
    }
}

/* 设施配置区 */
.facilities-section {
    margin-bottom: 30px;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    // box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .section-title {
        font-size: 18px;
        font-weight: 600;
        margin-bottom: 20px;
        color: #333;
        padding-bottom: 10px;
        border-bottom: 1px solid #eee;
    }

    .facilities-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 15px;

        .facility-item {
            display: flex;
            align-items: center;
            padding: 8px;
            border-radius: 4px;
            background: #f9f9f9;
            color: #999;

            &.available {
                background: #f0f9eb;
                color: #67c23a;

                i {
                    color: #67c23a;
                }
            }

            i {
                margin-right: 8px;
                font-size: 18px;
            }

            span {
                font-size: 14px;
            }
        }
    }
}

/* 房屋描述区 */
.description-section {
    margin-bottom: 30px;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .section-title {
        font-size: 18px;
        font-weight: 600;
        margin-bottom: 20px;
        color: #333;
        padding-bottom: 10px;
        border-bottom: 1px solid #eee;
    }

    .description-content {
        line-height: 1.8;
        color: #666;

        v::deep p {
            margin-bottom: 15px;
        }

        v::deep img {
            max-width: 100%;
            height: auto;
            margin: 10px 0;
            border-radius: 4px;
        }
    }
}

/* 底部操作栏 */
.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: #fff;
    border-radius: 8px;
    // box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .contact-info {
        display: flex;
        align-items: center;

        .avatar {
            width: 50px;
            height: 50px;
            margin-right: 15px;
            border-radius: 50%;
            overflow: hidden;

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }

        .info {
            .name {
                font-size: 16px;
                font-weight: 600;
                margin-bottom: 5px;
            }

            .role {
                font-size: 12px;
                color: #999;
            }
        }
    }

    .action-buttons {
        .el-button {
            margin-left: 15px;
            padding: 10px 20px;
        }
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .detail-grid {
        grid-template-columns: repeat(2, 1fr) !important;
    }

    .facilities-grid {
        grid-template-columns: repeat(2, 1fr) !important;
    }

    .action-bar {
        flex-direction: column;
        align-items: flex-start;

        .action-buttons {
            margin-top: 15px;
            width: 100%;

            .el-button {
                width: 100%;
                margin-left: 0;
                margin-bottom: 10px;
            }
        }
    }
}
</style>