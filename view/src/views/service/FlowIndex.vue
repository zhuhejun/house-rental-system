<template>
    <div class="container">
        <div class="top-header">
            <div class="nav-left">
            </div>
            <div class="nav-right">
                <div>
                    <AutoInput placeholder="搜索房源" @listener="listener" />
                </div>
            </div>
        </div>
        <!-- 表格及分页信息 -->
        <div style="padding-inline: 20px;">
            <el-table :data="apiResult.data">
                <el-table-column prop="username" label="基本信息">
                    <template #default="scope">
                        <div style="display: flex;justify-content: left;gap: 10px;">
                            <img style="border-radius: 5px;width: 70px;height: 50px;" :src="scope.row.houseCover" alt=""
                                srcset="">
                            <div>
                                <div style="font-size: 18px;">
                                    {{ scope.row.houseName }}
                                </div>
                                <div style="margin-top: 4px;">
                                    {{ scope.row.createTime }}
                                </div>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="showNumber" :sortable="true" width="108" label="展现量"></el-table-column>
                <el-table-column prop="viewNumber" :sortable="true" width="108" label="阅读量"></el-table-column>
                <el-table-column prop="clickRate" :sortable="true" width="108" label="点击率">
                    <template #default="scope">
                        <div>{{ scope.row.clickRate }}%</div>
                    </template>
                </el-table-column>
                <el-table-column prop="saveNumber" :sortable="true" width="108" label="收藏量"></el-table-column>
                <el-table-column prop="evaluationsNumber" :sortable="true" width="108" label="评论量"></el-table-column>
                <el-table-column label="操作" width="108" align="center">
                    <template #default="scope">
                        <div @click="handleDetail(scope.row)" style="color: rgb(17, 139, 221);cursor: pointer;">查看详情
                        </div>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页组件区域 -->
            <div class="pager">
                <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                    :current-page="houseQueryDto.current" :page-sizes="[10, 20]" :page-size="houseQueryDto.size"
                    layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
            </div>
        </div>

        <!-- 房源流量申请信息抽屉 -->
        <el-drawer :title="houseFlowIndex.houseName" :modal="false" :wrapperClosable="false" :visible.sync="drawer"
            :direction="direction" size="50%" :before-close="handleClose">
            <div>
                <div style="background-color: rgb(248,248,248);padding: 10px;margin-inline: 20px;">
                    <el-row :gutter="20">
                        <el-col :span="6">
                            <div>
                                <el-statistic group-separator="," :precision="0" :value="houseFlowIndex.showNumber"
                                    :title="title"></el-statistic>
                            </div>
                        </el-col>
                        <el-col :span="6">
                            <div>
                                <el-statistic title="阅读量">
                                    <template slot="formatter">
                                        {{ houseFlowIndex.viewNumber }}
                                    </template>
                                </el-statistic>
                            </div>
                        </el-col>
                        <el-col :span="6">
                            <div>
                                <el-statistic title="收藏量">
                                    <template slot="formatter">
                                        {{ houseFlowIndex.saveNumber }}
                                    </template>
                                </el-statistic>
                            </div>
                        </el-col>
                        <el-col :span="6">
                            <div>
                                <el-statistic title="评论量">
                                    <template slot="formatter">
                                        {{ houseFlowIndex.evaluationsNumber }}
                                    </template>
                                </el-statistic>
                            </div>
                        </el-col>
                    </el-row>
                </div>
                <!-- 折线图 -->
                <div style="padding: 20px;">
                    <div>

                    </div>
                    <!-- <el-radio-group @change="fetchListChart" v-model="houseFlowIndexQueryDto.type" size="small">
                        <el-radio label="1" border>阅读量</el-radio>
                        <el-radio label="2" border>收藏量</el-radio>
                        <el-radio label="4" border>展现量</el-radio>
                    </el-radio-group> -->
                    <Tab :buttons="[
                        { label: '展现量', value: '4' },
                        { label: '阅读量', value: '1' },
                        { label: '收藏量', value: '2' }
                    ]" :initialActive="houseFlowIndexQueryDto.type" @change="handleChange" />
                    <div>
                        <div v-if="values.length === 0">
                            <el-empty description="暂无数据"></el-empty>
                        </div>
                        <div v-else>
                            <LineChart style="margin-top: 20px;" height="360px" @on-selected="onSelected" tag="流量状况" :values="values" :date="date" />
                        </div>

                    </div>
                </div>
            </div>
        </el-drawer>

    </div>
</template>

<script>
// B站 「程序辰星」原创出品
import AutoInput from "@/components/AutoInput.vue"; // 自己封装好的输入框组件
import Tab from "@/components/Tab" // 导入封装好的Tab组件
import LineChart from "@/components/LineChart" // 导入封装好的Tab组件
export default {
    components: { AutoInput, Tab, LineChart }, // 注册组件
    data() {
        return {
            like: true,
            value1: 4154.564,
            value2: 1314,
            title: "展现量",
            drawer: false,
            direction: 'rtl',
            apiResult: { // 后端返回的查询数据的响应数据
                data: [], // 数据项
                total: 0, // 符合条件的数据总想 - 初始赋值为0
            },
            houseFlowIndex: {}, // 房源流量信息
            houseQueryDto: { // 搜索条件
                current: 1, // 当前页 - 初始是第一页
                size: 10, // 页面显示大小 - 初始是10条
            },
            houseFlowIndexQueryDto: {
                id: null,
                type: '1',
                days: 365, // 默认查询一年的
            },
            values: [],
            date: [],
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        onSelected(days) {
            this.houseFlowIndexQueryDto.days = days;
            this.fetchListChart();
        },
        handleChange(val) {
            this.houseFlowIndexQueryDto.type = Number(val.value);
            this.fetchListChart();
        },
        handleClose() {
            this.drawer = false;
        },
        handleDetail(data) {
            this.drawer = true;
            this.houseFlowIndex = { ...data };
            this.houseFlowIndexQueryDto.id = data.id;
            this.fetchListChart();
        },
        // 房源流量可视化指标查询
        async fetchListChart() {
            try {
                const { data } = await this.$axios.post('/house/listChart', this.houseFlowIndexQueryDto);
                this.values = data.map(entity => entity.count);
                this.date = data.map(entity => entity.name);
            } catch (error) {
                console.error('房源流量可视化指标查询异常:', error);
            }
        },
        // 输入框组件输入回传
        listener(text) {
            this.houseQueryDto.name = text; // 赋值查询条件的内容
            this.fetchFreshData(); // 重新加载数据
        },
        // 查询房源流量信息数据
        async fetchFreshData() {
            try {
                const { data, total } = await this.$axios.post('/house/listFlowIndex', this.houseQueryDto);
                this.apiResult.data = data;
                this.apiResult.total = total;
            } catch (error) {
                console.error('查询房源流量信息信息异常:', error);
            }
        },
        // 分页 - 处理页面页数切换
        handleSizeChange(size) {
            this.houseQueryDto.size = size; // 当前页面大小重置
            this.houseQueryDto.currrent = 1; // 当前页设置为第一页
            this.fetchFreshData(); // 重新加载页面数据
        },
        // 分页 - 处理页面当前页切换
        handleCurrentChange(current) {
            this.houseQueryDto.current = current; // 当前页选中
            this.fetchFreshData(); // 重新加载页面数据
        },
    },
};
</script>
<style scoped lang="scss">
.pager {
    margin-block: 20px;
}

/* 默认隐藏操作按钮 */
.operate-buttons {
    opacity: 0;
    transition: opacity 0.3s;
    /* 添加过渡效果 */
    cursor: pointer;

    i {
        padding: 8px;
        border-radius: 6px;
        transition: all .5s ease;

        &:hover {
            background-color: rgb(236, 237, 238);
        }
    }

}

/* 行悬停时显示操作按钮 */
.el-table__body tr:hover .operate-buttons {
    opacity: 1;
}

.container {
    padding-block: 10px;
    background-color: rgb(255, 255, 255);
}

.top-header {
    padding-inline: 10px;
    border-radius: 5px;
    padding-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .nav-left,
    .nav-right {
        display: flex;
        justify-content: left;
        align-items: center;
        gap: 10px;
    }

    .nav-left {
        display: flex;
    }

}
</style>