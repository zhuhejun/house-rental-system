<template>
    <div class="container-home">
        <!-- 房东认证 -->
        <div class="reply" v-if="landlord === null">
            <div class="title">
                <i class="el-icon-document"></i>
                空置房别闲置，你的房源值得更多租客看见
            </div>
            <div class="content">
                告别传统贴条招租，平台千万级流量曝光，精准匹配优质租客，让空房快速 “活” 起来，租金收益不空置。
                <span @click="reply" style="text-decoration: underline;cursor: pointer;">申请成为房东</span>
            </div>
        </div>
        <div class="content">

            <!-- 房屋列表信息 -->
            <div class="house-container">

                <!-- 额外查询条件区域 -->
                <div>
                    <div style="display: flex;justify-content: space-between;align-items: center;">
                        <!-- 常居住地信息 -->
                        <div class="area" v-if="userArea !== undefined">
                            <div style="display: flex;justify-content: left;align-items: center;gap: 4px;">
                                <i class="el-icon-location"></i>
                                <div v-if="userArea.cityAreaName !== ''">
                                    {{ userArea.topAreaName }} > {{ userArea.cityAreaName }}
                                </div>
                                <div v-else>地区不限</div>
                            </div>
                            <div>
                                <span @click="changeAreaOperation" class="change-area">切换地区</span>
                            </div>
                        </div>
                        <div>
                            <AutoInput placeholder="搜索房源" @listener="listener" />
                        </div>
                    </div>

                    <!-- 查询结果提示 -->
                    <div
                        style="font-size: 14px;color: rgb(51,51,51);background-color: rgb(246,246,246);padding: 12px 20px;">
                        共为你查询到
                        <span style="font-size: 20px;font-weight: 600;padding: 6px;">{{ total }}</span>
                        套房源
                    </div>

                    <!-- 房屋属性查询条件 -->
                    <div class="condition-container">
                        <!-- 房屋面积-->
                        <div class="condition">
                            <div class="point-text">
                                面积
                            </div>
                            <div class="item-condition">
                                <div :style="{
                                    'color': conditionFilter.sizeNumber === item.label ? 'rgb(31, 176, 129)' : '',
                                    'fontWeight': conditionFilter.sizeNumber === item.label ? '600' : ''
                                }" @click="houseSizeNumberClick(item)" v-for="(item, index) in houseSizeNumberList"
                                    :key="item.value">
                                    {{ item.label }}
                                    <span v-if="index !== 0">m²</span>
                                </div>
                            </div>
                        </div>
                        <!-- 租金-->
                        <div class="condition">
                            <div class="point-text">
                                租金
                            </div>
                            <div class="item-condition">
                                <div :style="{
                                    'color': conditionFilter.rent === item.label ? 'rgb(31, 176, 129)' : '',
                                    'fontWeight': conditionFilter.rent === item.label ? '600' : ''
                                }" @click="houseRentClick(item)" v-for="(item, index) in houseRentRangeList"
                                    :key="item.value">
                                    {{ item.label }}
                                    <span v-if="index !== 0">元</span>
                                </div>
                            </div>
                        </div>
                        <!-- 房屋类型 -->
                        <div class="condition">
                            <div class="point-text">
                                类型
                            </div>
                            <div class="item-condition">
                                <div :style="{
                                    'color': conditionFilter.typeId === item.label ? 'rgb(31, 176, 129)' : '',
                                    'fontWeight': conditionFilter.typeId === item.label ? '600' : ''
                                }" @click="houseTypeClick(item)" v-for="item in houseTypeList" :key="item.value">
                                    {{ item.label }}
                                </div>
                            </div>
                        </div>
                        <!-- 房屋朝向 -->
                        <div class="condition">
                            <div class="point-text">
                                朝向
                            </div>
                            <div class="item-condition">
                                <div :style="{
                                    'color': conditionFilter.directionId === item.label ? 'rgb(31, 176, 129)' : '',
                                    'fontWeight': conditionFilter.directionId === item.label ? '600' : ''
                                }" @click="houseDirectionClick(item)" v-for="item in houseDirectionList"
                                    :key="item.value">
                                    {{ item.label }}
                                </div>
                            </div>
                        </div>
                        <!-- 房屋户型 -->
                        <div class="condition">
                            <div class="point-text">
                                户型
                            </div>
                            <div class="item-condition">
                                <div :style="{
                                    'color': conditionFilter.sizedId === item.label ? 'rgb(31, 176, 129)' : '',
                                    'fontWeight': conditionFilter.sizedId === item.label ? '600' : ''
                                }" @click="houseSizedClick(item)" v-for="item in houseSizedList" :key="item.value">
                                    {{ item.label }}
                                </div>
                            </div>
                        </div>
                        <!-- 房屋租金方式 -->
                        <div class="condition">
                            <div class="point-text">
                                租金方式
                            </div>
                            <div class="item-condition">
                                <div :style="{
                                    'color': conditionFilter.depositMethodId === item.label ? 'rgb(31, 176, 129)' : '',
                                    'fontWeight': conditionFilter.depositMethodId === item.label ? '600' : ''
                                }" @click="houseDepositMethodClick(item)" v-for="item in houseDepositMethodList"
                                    :key="item.value">
                                    {{ item.label }}
                                </div>
                            </div>
                        </div>
                        <!-- 房屋是否临近地铁 -->
                        <div class="condition">
                            <div class="point-text">
                                临近地铁
                            </div>
                            <div class="item-condition">
                                <div :style="{
                                    'color': conditionFilter.isSubway === item.label ? 'rgb(31, 176, 129)' : '',
                                    'fontWeight': conditionFilter.isSubway === item.label ? '600' : ''
                                }" @click="houseSubwayClick(item)" v-for="item in houseSubwayList" :key="item.value">
                                    {{ item.label }}
                                </div>
                            </div>
                        </div>
                        <!-- 房屋装修状态-->
                        <div class="condition">
                            <div class="point-text">
                                装修状态
                            </div>
                            <div class="item-condition">
                                <div :style="{
                                    'color': conditionFilter.fitmentStatusId === item.label ? 'rgb(31, 176, 129)' : '',
                                    'fontWeight': conditionFilter.fitmentStatusId === item.label ? '600' : ''
                                }" @click="houseFitmentStatusClick(item)" v-for="item in houseFitmentStatusList"
                                    :key="item.value">
                                    {{ item.label }}
                                </div>
                            </div>
                        </div>
                        <!-- 房屋租赁方式-->
                        <div class="condition">
                            <div class="point-text">
                                租赁方式
                            </div>
                            <div class="item-condition">
                                <div :style="{
                                    'color': conditionFilter.rentalType === item.label ? 'rgb(31, 176, 129)' : '',
                                    'fontWeight': conditionFilter.rentalType === item.label ? '600' : ''
                                }" @click="houseRentalTypeClick(item)" v-for="item in houseRentalTypeList"
                                    :key="item.value">
                                    {{ item.label }}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 房屋显示区域 -->
                <div class="house-list">
                    <div v-if="houseList.length === 0">
                        <el-empty description="暂无房屋信息"></el-empty>
                    </div>
                    <div @click="houseItemClick(item.id)" class="house-item" v-for="item in houseList" :key="item.id">
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

                <!-- 分页区域 -->
                <div style="display: flex;justify-content: right;">
                    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                        :current-page="houseQueryDto.current" :page-sizes="[10, 20]" :page-size="houseQueryDto.size"
                        layout="total, sizes, prev, pager, next, jumper" :total="total"></el-pagination>
                </div>

            </div>

            <!-- 额外拓展信息 -->
            <div class="right">
                <h2 style="margin-top: 0;">房源推荐</h2>
                <div @click="houseItemClick(house.id)" class="item-house" v-for="(house, index) in houseRecommedList"
                    :key="index">
                    <img :src="house.cover" alt="">
                    <div class="text-item">
                        {{ house.name }}
                    </div>
                </div>
            </div>
        </div>

        <!-- 设置地区信息 -->
        <el-dialog title="设置地区信息" :closeOnClickModal="false" :show-close="false" :visible.sync="dialogArea" width="30%">
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
                <span class="primary-bt" @click="dialogArea = false">取消</span>
                <span class="info-bt" @click="confirmFetchArea">确定设置</span>
            </span>
        </el-dialog>

    </div>
</template>

<script>
import AutoInput from "@/components/AutoInput.vue"; // 导入封装好的输入框组件
export default {
    components: { AutoInput },
    data() {
        return {
            dialogArea: false, // 省份信息弹窗
            conditionFilter: { // 选中项
                sizeNumber: null,
                rent: null,
                typeId: null,
                directionId: null,
                sizedId: null,
                depositMethodId: null,
                isSubway: null,
                fitmentStatusId: null,
                rentalType: null,
            },
            userArea: {}, //用户常居住地信息
            landlord: null, // 房东认证信息
            houseQueryDto: {
                current: 1, // 默认查第一页
                size: 10, // 一页查10条
            },// 房屋查询条件
            houseList: [], // 房屋信息
            total: null, // 总页数
            houseTypeList: [], // 房屋类型查询条件数组
            houseDirectionList: [],// 房屋朝向查询条件数组
            houseSizedList: [],// 房屋户型查询条件数组
            houseDepositMethodList: [],// 房屋租金方式查询条件数组
            houseSubwayList: [], // 房屋是否临近地铁查询条件数组
            houseFitmentStatusList: [], // 房屋装修状态查询条件数组
            houseRentalTypeList: [],// 房屋租赁方式查询条件数组
            houseSizeNumberList: [], // 房屋面积查询条件数组
            houseRentRangeList: [], // 房屋租金查询条件数组
            topArea: [], // 查询省份信息
            cityArea: [], // 市区信息
            topAreaId: null, // 省份ID
            cityAreaId: null,
            houseRecommedList: [], // 推荐的房源
            recommendCount: 3, // 推荐房屋数3户
        }
    },
    created() {
        // 先加载用户数据和常居住地
        this.fetchUserAreaData().then(() => {
            // 然后加载其他数据和房屋信息
            Promise.all([
                this.fetchRecommendHouseNews(this.recommendCount),
                this.fetchLandlordData(),
                this.fetchHouseType(),
                this.fetchHouseDirection(),
                this.fetchHouseSized(),
                this.fetchHouseDepositMethod(),
                this.fetchHouseSubway(),
                this.fetchHouseFitmentStatus(),
                this.fetchHouseRentalType(),
                this.fetchHouseSizeNumber(),
                this.fetchHouseRent()
            ]).then(() => {
                // 确保所有数据加载完成后再执行初始查询
                this.fetchHouseData();
            });
        });
    },
    methods: {
        async fetchRecommendHouseNews(count) {
            try {
                const { data } = await this.$axios.get(`/house/recommend/${count}`);
                this.houseRecommedList = data;
            } catch (error) {
                this.$message.error('获取推荐的房屋信息失败');
                console.error(error);
            }
        },
        // 输入框组件输入回传
        listener(text) {
            this.houseQueryDto.name = text; // 赋值查询条件的内容
            this.fetchHouseData(); // 重新加载数据
        },
        confirmFetchArea() {
            // 选中的省份
            const topAreaList = this.topArea.filter(area => area.id === this.topAreaId);
            // 选中的市区
            const cityAreaList = this.cityArea.filter(area => area.id === this.cityAreaId);
            this.userArea.topAreaName = topAreaList[0].name;
            this.userArea.cityAreaName = cityAreaList[0].name;
            this.dialogArea = false;
            this.houseQueryDto.areaId = cityAreaList[0].id; // 通过选中的城市ID去查指定城市下面的数据
            this.fetchHouseData();
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
        async handleAreaChange() {
            try {
                const areaQueryDto = { parentId: this.topAreaId }
                const { data } = await this.$axios.post('/area/list', areaQueryDto);
                this.cityArea = data;
            } catch (error) {
                console.log("查询省份下的市区信息异常：", error);
            }
        },
        changeAreaOperation() {
            this.dialogArea = true;
            this.fetchTopArea();
            this.handleAreaChange();
        },
        houseTypeClick(item) {
            this.conditionFilter.typeId = item.label;
            this.houseQueryDto.typeId = item.value;
            this.fetchHouseData();
        },
        houseDirectionClick(item) {
            this.conditionFilter.directionId = item.label;
            this.houseQueryDto.directionId = item.value;
            this.fetchHouseData();
        },
        houseSizedClick(item) {
            this.conditionFilter.sizedId = item.label;
            this.houseQueryDto.sizedId = item.value;
            this.fetchHouseData();
        },
        houseDepositMethodClick(item) {
            this.conditionFilter.depositMethodId = item.label;
            this.houseQueryDto.depositMethodId = item.value;
            this.fetchHouseData();
        },
        houseSubwayClick(item) {
            this.conditionFilter.isSubway = item.label;
            this.houseQueryDto.isSubway = item.value === null ? null : item.value === 1;
            this.fetchHouseData();
        },
        houseFitmentStatusClick(item) {
            this.conditionFilter.fitmentStatusId = item.label;
            this.houseQueryDto.fitmentStatusId = item.value;
            this.fetchHouseData();
        },
        houseRentalTypeClick(item) {
            this.conditionFilter.rentalType = item.label;
            this.houseQueryDto.rentalType = item.value;
            this.fetchHouseData();
        },
        houseSizeNumberClick(item) {
            this.conditionFilter.sizeNumber = item.label;

            // 如果说不限制
            if (item.value === null) {
                this.houseQueryDto.minSizeNumber = null;
                this.houseQueryDto.maxSizeNumber = null;
            } else {
                const areaRange = item.label.split('-');
                this.houseQueryDto.minSizeNumber = areaRange[0];
                this.houseQueryDto.maxSizeNumber = areaRange[1];
            }
            this.fetchHouseData();
        },
        houseRentClick(item) {
            this.conditionFilter.rent = item.label;
            // 如果说不限制
            if (item.value === null) {
                this.houseQueryDto.minRent = null;
                this.houseQueryDto.maxRent = null;
            } else {
                const rentRange = item.label.split('-');
                this.houseQueryDto.minRent = rentRange[0];
                this.houseQueryDto.maxRent = rentRange[1];
            }
            this.fetchHouseData();
        },
        // 查询房屋类型
        async fetchHouseType() {
            try {
                const { data } = await this.$axios.get('/house/houseTypeList');
                this.houseTypeList = data;
                this.houseTypeList.unshift({ value: null, label: '不限' });
                // this.houseTypeClick(this.houseTypeList[0]);
            } catch (error) {
                console.log("查询房屋类型异常：", error);
            }
        },
        // 查询房屋朝向
        async fetchHouseDirection() {
            try {
                const { data } = await this.$axios.get('/house/houseDirectionList');
                this.houseDirectionList = data;
                this.houseDirectionList.unshift({ value: null, label: '不限' });
                // this.houseDirectionClick(this.houseDirectionList[0]);
            } catch (error) {
                console.log("查询房屋朝向异常：", error);
            }
        },
        // 查询房屋户型
        async fetchHouseSized() {
            try {
                const { data } = await this.$axios.get('/house/houseSizedList');
                this.houseSizedList = data;
                this.houseSizedList.unshift({ value: null, label: '不限' });
                // this.houseSizedClick(this.houseSizedList[0]);
            } catch (error) {
                console.log("查询房屋户型异常：", error);
            }
        },
        // 查询房屋押金方式数组
        async fetchHouseDepositMethod() {
            try {
                const { data } = await this.$axios.get('/house/houseDepositMethodList');
                this.houseDepositMethodList = data;
                this.houseDepositMethodList.unshift({ value: null, label: '不限' });
                // this.houseDepositMethodClick(this.houseDepositMethodList[0]);
            } catch (error) {
                console.log("查询房屋户型异常：", error);
            }
        },
        // 查询房屋临近地铁数组
        async fetchHouseSubway() {
            try {
                const { data } = await this.$axios.get('/house/houseSubwayList');
                this.houseSubwayList = data;
                this.houseSubwayList.unshift({ value: null, label: '不限' });
                // this.houseSubwayClick(this.houseSubwayList[0]);
            } catch (error) {
                console.log("查询房屋是否临近地铁异常：", error);
            }
        },
        // 查询房屋装修状态数组
        async fetchHouseFitmentStatus() {
            try {
                const { data } = await this.$axios.get('/house/houseFitmentStatusList');
                this.houseFitmentStatusList = data;
                this.houseFitmentStatusList.unshift({ value: null, label: '不限' });
                // this.houseFitmentStatusClick(this.houseFitmentStatusList[0]);
            } catch (error) {
                console.log("查询房屋装修状态异常：", error);
            }
        },
        // 查询房屋租赁方式数组
        async fetchHouseRentalType() {
            try {
                const { data } = await this.$axios.get('/house/houseRentalTypeList');
                this.houseRentalTypeList = data;
                this.houseRentalTypeList.unshift({ value: null, label: '不限' });
                // this.houseRentalTypeClick(this.houseRentalTypeList[0]);
            } catch (error) {
                console.log("查询房屋租赁方式异常：", error);
            }
        },
        // 查询房屋面积查询条件数组
        async fetchHouseSizeNumber() {
            try {
                const { data } = await this.$axios.get('/house/houseSizeNumber');
                this.houseSizeNumberList = data;
                this.houseSizeNumberList.unshift({ value: null, label: '不限' });
                // this.houseSizeNumberClick(this.houseSizeNumberList[0]);
            } catch (error) {
                console.log("查询房屋面积查询条件异常：", error);
            }
        },
        // 查询房屋租金查询条件数组
        async fetchHouseRent() {
            try {
                const { data } = await this.$axios.get('/house/houseRentRange');
                this.houseRentRangeList = data;
                this.houseRentRangeList.unshift({ value: null, label: '不限' });
                // this.houseRentClick(this.houseRentRangeList[0]);
            } catch (error) {
                console.log("查询房屋租金查询条件异常：", error);
            }
        },
        // 跳转至房屋详情页
        houseItemClick(id) {
            window.open(`/house-detail?id=${id}`, '_blank');
        },
        // 分页 - 处理页面页数切换
        handleSizeChange(size) {
            this.houseQueryDto.size = size; // 当前页面大小重置
            this.houseQueryDto.currrent = 1; // 当前页设置为第一页
            this.fetchHouseData(); // 重新加载页面数据
        },
        // 分页 - 处理页面当前页切换
        handleCurrentChange(current) {
            this.houseQueryDto.current = current; // 当前页选中
            this.fetchHouseData(); // 重新加载页面数据
        },
        reply() {
            window.open('/reply-landlord', '_blank');
        },
        async fetchHouseData() {
            try {
                const { data, total } = await this.$axios.post('/house/listUser', this.houseQueryDto);
                this.houseList = data;
                this.total = total;
            } catch (error) {
                console.log("查询房屋信息异常：", error);
            }
        },
        async fetchLandlordData() {
            try {
                const { data } = await this.$axios.post('/landlord/listUser', {});
                this.landlord = data;
                console.log(this.landlord);
            } catch (error) {
                console.log("查询房东申请信息异常：", error);
            }
        },
        async fetchUserAreaData() {
            try {
                const { data } = await this.$axios.post('/user-area/listUser', {});
                if (data && data.length > 0) {
                    this.userArea = data[0];
                    this.houseQueryDto.areaId = this.userArea.cityAreaId;
                    // 默认查询的就是用户常居住地的房源信息
                    this.topAreaId = this.userArea.topAreaId;
                    this.cityAreaId = this.userArea.cityAreaId;
                    // 确保数据完全设置后再执行查询
                    this.$nextTick(() => {
                        this.fetchHouseData();
                    });
                } else {
                    // 如果没有常居住地数据，初始化空对象
                    this.userArea = {
                        topAreaName: '',
                        cityAreaName: ''
                    };
                }
            } catch (error) {
                console.log("查询用户常居住地信息异常：", error);
                // 出错时也初始化空对象
                this.userArea = {
                    topAreaName: '',
                    cityAreaName: ''
                };
            }
        },
    }
}
</script>

<style scoped lang="scss">
.item-house {
    /* padding: 10px; */
    box-sizing: border-box;
    position: relative;
    margin-bottom: 10px;
    cursor: pointer;

    img {
        width: 100%;
        border-radius: 5px;
    }

    .text-item {
        position: absolute;
        bottom: 5px;
        font-weight: 900;
        width: 100%;
        padding: 10px;
        font-size: 14px;
        box-sizing: border-box;
        color: rgb(255, 255, 255);
        background-color: rgba(0, 0, 0, 0.3);
    }
}

.area {
    margin-block: 20px;
    font-size: 14px;
    color: rgb(150, 152, 154);
    display: flex;
    justify-content: left;
    align-items: center;
    gap: 10px;

    .change-area {
        font-size: 12px;
        color: rgb(64, 158, 255);
        cursor: pointer;

        &:hover {
            text-decoration: underline;
        }
    }
}

.condition-container {
    box-shadow: 0 4px 6px rgb(240, 240, 240);
    padding: 10px 20px;
    border-radius: 10px;
}

.condition {
    display: flex;
    justify-content: left;
    align-items: center;
    padding-block: 12px;

    .point-text {
        min-width: 60px;
        font-size: 12px;
        color: rgb(156, 158, 160);
    }

    .item-condition {
        display: flex;
        font-size: 14px;


        div {
            margin-right: 20px;

            &:hover {
                cursor: pointer;
                color: rgb(31, 176, 129);
            }
        }
    }
}

.house-list {
    display: flex;
    flex-wrap: wrap;
    margin-block: 30px;

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

.content {
    display: flex;
    justify-content: space-between;


    .house-container {
        width: 80%;
        background-color: rgb(255, 255, 255);
        padding: 20px 40px;
        box-sizing: border-box;
    }

    .right {
        width: 300px;
        margin-top: 10px;
        padding: 10px 20px;
        box-sizing: border-box;
        background-color: rgb(255, 255, 255);
    }
}

.container-home {
    min-height: 100vh;
}

.reply {
    background-color: rgba(244, 244, 244);
    padding: 30px 40px;
    margin-inline: 20px;
    margin-top: 20px;
    box-sizing: border-box;
    border-radius: 5px;

    .title {
        font-size: 24px;
        font-weight: 800;
        margin-bottom: 10px;
        color: rgb(21, 21, 21);
    }

    .content {
        font-size: 14px;
    }
}
</style>