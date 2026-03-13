<template>
    <div class="container">
        <div class="top-header">
            <div class="nav-left">
                <Tab :buttons="[
                    { label: '全部', value: 'null' },
                    { label: '预约中', value: '1' },
                    { label: '已预约', value: '2' },
                    { label: '预约失败', value: '3' },
                    { label: '已取消', value: '4' },
                    { label: '已完成', value: '5' },
                ]" initialActive="null" @change="handleChange" />
            </div>
            <div class="nav-right">

            </div>
        </div>

        <div class="house-container">
            <div>
                <el-table :data="apiResult.data">
                    <el-table-column prop="houseName" label="房源名">
                        <template #default="scope">
                            <div class="over-text">
                                {{ scope.row.houseName }}
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="orderDate" :sortable="true" width="168" label="预约日期"></el-table-column>
                    <el-table-column prop="orderTimeSplit" :sortable="true" width="168" label="预约时间段"></el-table-column>
                    <el-table-column prop="orderStatus" width="168" label="状态">
                        <template #default="scope">
                            <el-tag size="mini" v-if="scope.row.orderStatus === 1">预约中</el-tag>
                            <el-tag size="mini" v-else-if="scope.row.orderStatus === 2" type="success">已预约</el-tag>
                            <el-tag size="mini" v-else-if="scope.row.orderStatus === 3" type="danger">预约失败</el-tag>
                            <el-tag size="mini" v-else-if="scope.row.orderStatus === 4" type="info">已取消</el-tag>
                            <el-tag size="mini" v-else-if="scope.row.orderStatus === 5" type="success">已完成</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="createTime" :sortable="true" width="168" label="发布时间"></el-table-column>
                    <el-table-column label="" width="118" align="center">
                        <template #default="scope">
                            <div class="operate-buttons">
                                <el-dropdown trigger="click" placement="bottom-end">
                                    <span class="el-dropdown-link">
                                        <i class="el-icon-more"></i>
                                    </span>
                                    <el-dropdown-menu slot="dropdown">
                                        <el-dropdown-item @click.native="update(scope.row)" icon="el-icon-view">
                                            详情
                                        </el-dropdown-item>
                                        <el-dropdown-item @click.native="deletedhouseOrderInfo(scope.row)"
                                            icon="el-icon-delete">删除</el-dropdown-item>
                                    </el-dropdown-menu>
                                </el-dropdown>
                            </div>
                        </template>
                    </el-table-column>
                </el-table>
                <!-- 分页组件区域 -->
                <div class="pager">
                    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                        :current-page="houseOrderInfoQueryDto.current" :page-sizes="[10, 20]"
                        :page-size="houseOrderInfoQueryDto.size" layout="total, sizes, prev, pager, next, jumper"
                        :total="apiResult.total"></el-pagination>
                </div>
            </div>

        </div>
        <el-dialog title="删除预约看房记录信息" :visible.sync="dialogDeletedVisible" width="400px">
            <span>确定删除预约看房记录信息？</span>
            <span slot="footer" class="dialog-footer">
                <el-button size="mini" @click="dialogDeletedVisible = false">取消</el-button>
                <el-button size="mini" type="primary" @click="confirmDeleted">确定</el-button>
            </span>
        </el-dialog>


        <!-- 预约看房弹窗 -->
        <el-dialog title="预约看房记录" :show-close="false" :visible.sync="dialogVisible" :closeOnClickModal="false"
            width="35%">
            <div style="display: flex;gap: 10px;">
                <div>
                    <img style="border-radius: 5px;width: 100px;height: 70px;" :src="houseOrderInfo.cover" alt=""
                        srcset="">
                </div>
                <div>
                    <div style="font-size: 18px;">{{ houseOrderInfo.houseName }}</div>
                    <div style="margin-top: 20px;display: flex;gap: 10px;">
                        <div>
                            <el-tag size="mini" v-if="houseOrderInfo.orderStatus === 1">状态:预约中</el-tag>
                            <el-tag size="mini" v-else-if="houseOrderInfo.orderStatus === 2"
                                type="success">状态:已预约</el-tag>
                            <el-tag size="mini" v-else-if="houseOrderInfo.orderStatus === 3"
                                type="danger">状态:预约失败</el-tag>
                            <el-tag size="mini" v-else-if="houseOrderInfo.orderStatus === 4" type="info">状态:已取消</el-tag>
                            <el-tag size="mini" v-else-if="houseOrderInfo.orderStatus === 5"
                                type="success">状态:已完成</el-tag>
                        </div>
                        <div>
                            <el-tag size="mini" type="info">预约日期:{{ houseOrderInfo.orderDate }}</el-tag>
                        </div>
                        <div>
                            <el-tag size="mini" type="info">{{ houseOrderInfo.orderTimeSplit }}</el-tag>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 状态流转区域 -->
            <div>
                <status-flow :status-records="houseOrderStatusList" />
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="cancelOperation">关闭</span>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import Tab from "@/components/Tab" // 导入封装好的Tab组件
import StatusFlow from '@/components/StatusFlow.vue'
export default {
    components: { Tab, StatusFlow },
    data() {
        return {
            dialogVisible: false,
            direction: 'rtl',
            dialogDeletedVisible: false,
            id: null,
            apiResult: {
                data: [],
                total: 0,
            },
            houseOrderInfo: {},
            houseOrderInfoQueryDto: {
                current: 1,
                size: 10,
            },
            deletedItem: {},
            content: '',
            houseOrderEvaluations: [],
            houseOrderStatusList: [],
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {

        cancelOperation() {
            this.houseOrderInfo = {};
            this.dialogVisible = false;

        },
        // 预约状态选中事件
        handleChange(obj) {
            this.houseOrderInfoQueryDto.orderStatus = Number(obj.value); // 转成数值类型，再赋值
            this.fetchFreshData(); // 重新加载数据
        },
        async update(item) {
            // 查询预约看房状态流转
            try {
                const queryDto = {
                    houseOrderInfoId: item.id
                };
                const { data } = await this.$axios.post(`/house-order-status/list`, queryDto);
                this.houseOrderStatusList = data;
                this.dialogVisible = true;
                this.houseOrderInfo = { ...item };
            } catch (error) {
                this.$notify.info({
                    title: '错误',
                    message: error.message,
                    duration: 1000,
                });
            }
        },

        deletedhouseOrderInfo(item) {
            this.deletedItem = { ...item };
            this.id = item.id;
            this.dialogDeletedVisible = true;
        },
        async confirmDeleted() {
            try {
                await this.$axios.delete(`/house-order-info/${this.id}`);
                this.$message.success("删除成功");
                this.fetchFreshData();
                this.dialogDeletedVisible = false;
            } catch (error) {
                this.$message.error(error.message);
            }
        },
        async fetchFreshData() {
            try {
                const { data, total } = await this.$axios.post('/house-order-info/list', this.houseOrderInfoQueryDto);
                this.apiResult.data = data;
                this.apiResult.total = total;
            } catch (error) {
                this.$message.error(error.message);
            }
        },
        handleSizeChange(size) {
            this.houseOrderInfoQueryDto.size = size;
            this.houseOrderInfoQueryDto.current = 1;
            this.fetchFreshData();
        },
        handleCurrentChange(current) {
            this.houseOrderInfoQueryDto.current = current;
            this.fetchFreshData();
        }
    },
};
</script>

<style scoped>
.actions {
    display: flex;
    gap: 20px;

    div {
        font-size: 14px;
        border: 1px solid rgb(193, 193, 193);
        color: rgb(78, 77, 77);
        cursor: pointer;
        padding: 4px 16px;

        &:hover {
            border: 1px solid rgb(139, 174, 236);
            color: rgb(139, 174, 236);
        }
    }
}

.container {
    padding: 20px;
    background-color: #fff;
}

.top-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
}

.nav-right {
    display: flex;
    align-items: center;
    gap: 10px;
}

.house-container {
    margin-bottom: 20px;
}

.house-item {
    display: flex;
    gap: 20px;
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;
}

.house-item img {
    width: 160px;
    height: 120px;
    border-radius: 4px;
    object-fit: cover;
}

.house-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.name {
    font-size: 18px;
    font-weight: 500;
    color: #333;
}

.summary {
    font-size: 14px;
    color: #666;
    line-height: 1.5;
    background-color: #fafafa;
    padding: 8px;
    border-radius: 4px;
}

.meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;
}

.stats {
    display: flex;
    gap: 15px;
    font-size: 12px;
    color: #888;
}

.actions {
    display: flex;
    gap: 10px;
}

.drawer-content {
    padding: 20px;
}

.cover-upload {
    display: flex;
    align-items: center;
    gap: 20px;
}

.cover-upload img {
    width: 200px;
    height: 140px;
    border-radius: 4px;
    object-fit: cover;
}

.uploader {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100px;
    height: 100px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    cursor: pointer;
    color: #999;
}

.uploader:hover {
    border-color: #409EFF;
}

.upload-tip {
    margin-top: 8px;
    font-size: 12px;
}

.pager {
    display: flex;
    justify-content: left;
    margin-top: 20px;
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
</style>