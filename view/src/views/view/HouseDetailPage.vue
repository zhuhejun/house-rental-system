<template>
    <div class="container">
        <div class="house-detail-container">
            <div style="margin-bottom: 20px;">
                <el-breadcrumb separator-class="el-icon-arrow-right">
                    <el-breadcrumb-item :to="{ path: '/view' }">首页</el-breadcrumb-item>
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
                    <span class="price">¥{{ house.rent }}</span>
                    <span class="unit">/月</span>
                    <span class="deposit-method">{{ house.depositMethodName }}</span>
                </div>

                <div class="meta-info">
                    <div class="meta-item">
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

            <!-- 底部操作栏 -->
            <div class="action-bar">
                <div class="contact-info">
                    <div class="avatar">
                        <img :src="house.landlordAvatar" alt="房东头像">
                    </div>
                    <div class="info">
                        <div class="name">{{ house.landlordName }}</div>
                        <div class="role">房东</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

</template>

<script>
export default {
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
        }
    },
    created() {
        this.getPathId();
    },
    mounted() {
        this.startTime = performance.now(); // 更精确的高精度时间
        window.addEventListener('beforeunload', this.handlePageLeave);
    },
    beforeDestroy() {
        window.removeEventListener('beforeunload', this.handlePageLeave);
    },
    methods: {
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
        getPathId() {
            this.houseId = this.$route.query.id;
            this.fetchHouseInfo(this.houseId);
        },
        async fetchHouseInfo(id) {
            try {
                const { data } = await this.$axios.get(`/viewer/getHouseById/${id}`);
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
    justify-content: center;
    align-items: center;
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