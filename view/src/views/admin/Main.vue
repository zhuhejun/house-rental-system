<template>
    <div class="main-container">
        <div class="left">
            <div class="static-value">
                <el-row :gutter="20">
                    <el-col :span="6" v-for="(item, index) in staticValues" :key="index">
                        <div>
                            <el-statistic group-separator="," :precision="0" :value="item.count"
                                :title="item.name"></el-statistic>
                        </div>
                    </el-col>
                </el-row>
            </div>
            <div style="margin-top: 30px;">
                <LineChart :tooltipFormatter="customTooltipFormatter" height="450px" tag="房源存量趋势" :values="values"
                    :date="dateList" @on-selected="onSelected" />
            </div>
        </div>
        <div class="right">
            <h2 style="margin-top: 0;">城市待租房源分布</h2>
            <div class="city-list">
                <div v-for="(city, index) in cityHouseList" :key="index" class="city-item">
                    <div class="city-info">
                        <span class="city-name">{{ city.name }}</span>
                        <span class="city-count">{{ city.count }}套</span>
                    </div>
                    <el-progress :percentage="calculatePercentage(city.count)" :show-text="false" :stroke-width="12"
                        :color="getProgressColor(index)" />
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import LineChart from "@/components/LineChart"
export default {
    components: { LineChart },
    data() {
        return {
            staticValues: [],
            values: [], // y轴
            dateList: [], // x轴
            cityHouseList: [], // 城市待租房源分布
            defaultSelectDays: 365, //默认查一年
            defaultSelectCity: 10, // 默认查询城市数
            selectedSort: 'count-desc', // 添加排序方式
            colorPalette: [ // 添加颜色配置
                '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
                '#8E44AD', '#1ABC9C', '#D35400', '#3498DB', '#E74C3C'
            ]
        }
    },
    created() {
        this.fetchStaticValues();
        this.fetchHouseLineChart(this.defaultSelectDays);
        this.fetchCityHouseRange(this.defaultSelectCity);
    },
    computed: {
        sortedCityHouseList() {
            const list = [...this.cityHouseList];
            switch (this.selectedSort) {
                case 'count-desc':
                    return list.sort((a, b) => b.count - a.count);
                case 'count-asc':
                    return list.sort((a, b) => a.count - b.count);
                case 'name-asc':
                    return list.sort((a, b) => a.name.localeCompare(b.name));
                case 'name-desc':
                    return list.sort((a, b) => b.name.localeCompare(a.name));
                default:
                    return list;
            }
        },
        maxCount() {
            if (this.cityHouseList.length === 0) return 1;
            return Math.max(...this.cityHouseList.map(city => city.count));
        }
    },
    methods: {
        getProgressColor(index) {
            return this.colorPalette[index % this.colorPalette.length];
        },
        calculatePercentage(count) {
            return (count / this.maxCount) * 100;
        },
        // 自定义提示框格式
        customTooltipFormatter(params) {
            // params是一个数组，包含当前点的所有信息
            const date = params[0].axisValue; // 获取日期
            const value = params[0].data;     // 获取数值
            return `${date}号，共上线${value}套房源`;
        },
        onSelected(day) {
            console.log(day);
            
            this.fetchHouseLineChart(day);
        },
        async fetchCityHouseRange(limit) {
            try {
                const { data } = await this.$axios.get(`/dashboard/cityHouseRange/${limit}`);
                this.cityHouseList = data;
            } catch (error) {
                console.error('城市待租房源分布:', error);
            }
        },
        async fetchHouseLineChart(day) {
            try {
                const { data } = await this.$axios.get(`/dashboard/houseLineChart/${day}`);
                this.dateList = data.map(entity => entity.name); // 处理成x轴数据格式
                this.values = data.map(entity => entity.count); // 处理成y轴数据格式
            } catch (error) {
                console.error('房源存量趋势统计异常:', error);
            }
        },
        async fetchStaticValues() {
            try {
                const { data } = await this.$axios.get('/dashboard/mainStatic');
                this.staticValues = data;
            } catch (error) {
                console.error('静态数据统计异常:', error);
            }
        },
    }
};
</script>

<style scoped lang="scss">
.main-container {
    display: flex;
    width: 100%;

    .left {
        width: 65%;
        padding-inline: 30px;
        box-sizing: border-box;

        .static-value {
            background-color: rgb(248, 248, 248);
            padding: 20px;
            box-sizing: border-box;
            border-radius: 5px;
        }
    }

    .right {
        width: 35%;
        padding: 20px;
        background: #fff;
        border-radius: 4px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }
}

.city-list {
    .city-item {
        margin-bottom: 15px;

        &:last-child {
            margin-bottom: 0;
        }

        .city-info {
            display: flex;
            justify-content: space-between;
            margin-bottom: 5px;
            font-size: 14px;

            .city-name {
                color: #606266;
            }

            .city-count {
                color: #409EFF;
                font-weight: bold;
            }
        }
    }
}
</style>