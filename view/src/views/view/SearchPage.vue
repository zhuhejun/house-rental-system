<template>
    <div class="container-home">
        <div class="content">
            <div class="house-container">
                <div>
                    <div style="margin-bottom: 20px;">
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
                <div>
                    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                        :current-page="houseQueryDto.current" :page-sizes="[10, 20]" :page-size="houseQueryDto.size"
                        layout="total, sizes, prev, pager, next, jumper" :total="total"></el-pagination>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import AutoInput from "@/components/AutoInput.vue";

export default {
    components: { AutoInput },
    data() {
        return {
            conditionFilter: {
                sizeNumber: null,
                rent: null,
                sizedId: null,
            },
            houseQueryDto: {
                current: 1,
                size: 10,
            },
            houseList: [],
            total: null,
            houseSizedList: [],
            houseSizeNumberList: [],
            houseRentRangeList: [],
        }
    },
    created() {
        this.getPathId();
        this.fetchHouseData();
        this.fetchHouseSized();
        this.fetchHouseSizeNumber();
        this.fetchHouseRent();
    },
    methods: {
        getPathId() {
            const key = this.$route.query.key;
            this.listener(key === '-1' ? null : key);
        },
        listener(text) {
            this.houseQueryDto.name = text;
            this.fetchHouseData();
        },
        houseSizedClick(item) {
            this.conditionFilter.sizedId = item.label;
            this.houseQueryDto.sizedId = item.value;
            this.fetchHouseData();
        },
        houseSizeNumberClick(item) {
            this.conditionFilter.sizeNumber = item.label;

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
        async fetchHouseSized() {
            try {
                const { data } = await this.$axios.get('/viewer/houseSizedList');
                this.houseSizedList = data;
                this.houseSizedList.unshift({ value: null, label: '不限' });
                this.houseSizedClick(this.houseSizedList[0]);
            } catch (error) {
                console.log("查询房屋户型异常：", error);
            }
        },
        async fetchHouseSizeNumber() {
            try {
                const { data } = await this.$axios.get('/viewer/houseSizeNumber');
                this.houseSizeNumberList = data;
                this.houseSizeNumberList.unshift({ value: null, label: '不限' });
                this.houseSizeNumberClick(this.houseSizeNumberList[0]);
            } catch (error) {
                console.log("查询房屋面积查询条件异常：", error);
            }
        },
        async fetchHouseRent() {
            try {
                const { data } = await this.$axios.get('/viewer/houseRentRange');
                this.houseRentRangeList = data;
                this.houseRentRangeList.unshift({ value: null, label: '不限' });
                this.houseRentClick(this.houseRentRangeList[0]);
            } catch (error) {
                console.log("查询房屋租金查询条件异常：", error);
            }
        },
        houseItemClick(id) {
            window.open(`/house-detail-page?id=${id}`, '_blank');
        },
        handleSizeChange(size) {
            this.houseQueryDto.size = size;
            this.houseQueryDto.current = 1;
            this.fetchHouseData();
        },
        handleCurrentChange(current) {
            this.houseQueryDto.current = current;
            this.fetchHouseData();
        },
        async fetchHouseData() {
            try {
                const { data, total } = await this.$axios.post('/viewer/listHouse', this.houseQueryDto);
                this.houseList = data;
                this.total = total;
            } catch (error) {
                console.log("查询房屋信息异常：", error);
            }
        },
    }
}
</script>

<style scoped lang="scss">
.condition-container {
    padding: 10px 20px;
}

.condition {
    display: flex;
    justify-content: left;
    align-items: center;
    padding-block: 12px;

    .point-text {
        min-width: 60px;
        font-size: 16px;
        color: rgb(156, 158, 160);
    }

    .item-condition {
        display: flex;
        font-size: 18px;

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
    justify-content: center;
    gap: 1px;

    .house-container {
        width: 80%;
        background-color: rgb(255, 255, 255);
        padding: 20px 40px;
        box-sizing: border-box;
    }
}

.container-home {
    height: 2000px;
}
</style>