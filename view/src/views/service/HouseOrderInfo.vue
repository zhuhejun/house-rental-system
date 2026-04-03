<template>
    <div class="container">
        <div class="top-header">
            <div class="nav-left">
                <Tab :buttons="[
                    { label: '全部', value: 'null' },
                    { label: '待确认', value: '1' },
                    { label: '已确认', value: '2' },
                    { label: '已拒绝', value: '3' },
                    { label: '用户已取消', value: '4' },
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
                            <el-tag size="mini" v-if="scope.row.orderStatus === 1">待确认</el-tag>
                            <el-tag size="mini" v-else-if="scope.row.orderStatus === 2" type="success">已确认</el-tag>
                            <el-tag size="mini" v-else-if="scope.row.orderStatus === 3" type="danger">已拒绝</el-tag>
                            <el-tag size="mini" v-else-if="scope.row.orderStatus === 4" type="info">用户已取消</el-tag>
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
                                        <!-- 只有预约看房状态完成之后才需要服务评价 -->
                                        <el-dropdown-item v-if="scope.row.orderStatus === 5"
                                            @click.native="serviceEvaluations(scope.row)"
                                            icon="el-icon-document-checked">
                                            服务评价
                                        </el-dropdown-item>
                                        <el-dropdown-item @click.native="update(scope.row)" icon="el-icon-edit">
                                            修改
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
                            <el-tag size="mini" v-if="houseOrderInfo.orderStatus === 1">状态:待确认</el-tag>
                            <el-tag size="mini" v-else-if="houseOrderInfo.orderStatus === 2"
                                type="success">状态:已确认</el-tag>
                            <el-tag size="mini" v-else-if="houseOrderInfo.orderStatus === 3"
                                type="danger">状态:已拒绝</el-tag>
                            <el-tag size="mini" v-else-if="houseOrderInfo.orderStatus === 4" type="info">状态:用户已取消</el-tag>
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
                <status-flow :status-records="houseOrderStatusList" :status-config="landlordStatusConfig" title="预约状态流转记录" />
            </div>
            <div v-if="houseOrderInfo.orderStatus === 5">
                <h3>用户评价</h3>
                <div v-if="houseOrderEvaluations.length > 0" class="evaluation-panel">
                    <div class="evaluation-header">
                        <div class="evaluation-user">
                            <img v-if="houseOrderEvaluations[0].avatar" :src="houseOrderEvaluations[0].avatar" alt="用户头像">
                            <div v-else class="evaluation-avatar-placeholder">用户</div>
                            <div>
                                <div class="evaluation-username">{{ houseOrderEvaluations[0].username || '匿名用户' }}</div>
                                <div class="evaluation-time">{{ houseOrderEvaluations[0].createTime }}</div>
                            </div>
                        </div>
                        <el-rate disabled v-model="houseOrderEvaluations[0].score" show-text></el-rate>
                    </div>
                    <div style="margin-top: 12px;">
                        <el-tag size="mini" :type="evaluationStatusType(houseOrderEvaluations[0])">
                            {{ evaluationStatusText(houseOrderEvaluations[0]) }}
                        </el-tag>
                    </div>
                    <div class="evaluation-text">
                        {{ houseOrderEvaluations[0].text || '该用户暂未填写评价内容' }}
                    </div>
                </div>
                <div v-else class="evaluation-empty">
                    该预约已完成，但用户暂未提交服务评价。
                </div>
            </div>
            <!-- 拒绝预约 -->
            <div>
                <h3>拒绝原因</h3>
                <el-input :disabled="houseOrderInfo.orderStatus === 3" type="textarea" :rows="2" placeholder="如果拒绝客户预约，请补充原因" v-model="apiParam.rejectCause">
                </el-input>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="cancelOperation">关闭</span>
                <span v-if="houseOrderInfo.orderStatus === 1" class="info-bt" @click="handleConfirmNo">
                    拒绝预约
                </span>
                <span v-if="houseOrderInfo.orderStatus === 1" class="info-bt" @click="handleOrderConfirm">
                    确认客户预约
                </span>
                <span v-if="houseOrderInfo.orderStatus === 2" class="info-bt" @click="handleConfirmOk">
                    已完成
                </span>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import Editor from "@/components/Editor.vue";
import Tab from "@/components/Tab" // 导入封装好的Tab组件
import StatusFlow from '@/components/StatusFlow.vue'
export default {
    components: { Editor, Tab, StatusFlow },
    data() {
        return {
            apiParam: {
                score: 3,
                text: '',
                rejectCause: '',
            },
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
            landlordStatusConfig: {
                1: { text: "待确认", icon: "el-icon-time", color: "#409EFF", status: "process" },
                2: { text: "已确认", icon: "el-icon-success", color: "#67C23A", status: "success" },
                3: { text: "已拒绝", icon: "el-icon-warning", color: "#F56C6C", status: "error" },
                4: { text: "用户已取消", icon: "el-icon-circle-close", color: "#909399", status: "error" },
                5: { text: "已完成", icon: "el-icon-finished", color: "#67C23A", status: "success" }
            }
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        async handleOrderConfirm() {
            try {
                this.houseOrderInfo.orderStatus = 2;
                await this.$axios.put(`/house-order-info/update`, this.houseOrderInfo);
                this.$message.success("预约已确认");
                this.fetchFreshData();
                this.cancelOperation();
            } catch (error) {
                this.$notify.info({
                    title: '错误',
                    message: error.message,
                    duration: 1000,
                });
            }
        },
        async handleConfirmNo() {
            try {
                if (this.apiParam.rejectCause === '') {
                    this.$message.info('请补充拒绝原因');
                    return;
                }
                this.houseOrderInfo.orderStatus = 3;
                await this.$axios.put(`/house-order-info/update`, this.houseOrderInfo);
                this.$message.success("已拒绝该预约");
                this.fetchFreshData();
                this.cancelOperation();
            } catch (error) {
                this.$notify.info({
                    title: '错误',
                    message: error.message,
                    duration: 1000,
                });
            }
        },
        async evaluationsSave() {
            try {
                this.apiParam.houseOrderInfoId = this.houseOrderInfo.id;
                await this.$axios.post(`/house-order-evaluations/save`, this.apiParam);
                this.$message.success("服务评价成功");
                this.serviceEvaluations(this.houseOrderInfo);
            } catch (error) {
                this.$notify.info({
                    title: '错误',
                    message: error.message,
                    duration: 1000,
                });
            }
        },
        async handleConfirmOk() {
            try {
                this.houseOrderInfo.orderStatus = 5;
                await this.$axios.put(`/house-order-info/update`, this.houseOrderInfo);
                this.$message.success("操作成功");
                this.fetchFreshData();
                this.cancelOperation();
            } catch (error) {
                this.$notify.info({
                    title: '错误',
                    message: error.message,
                    duration: 1000,
                });
            }
        },
        cancelOperation() {
            this.houseOrderInfo = {};
            this.houseOrderEvaluations = [];
            this.houseOrderStatusList = [];
            this.dialogVisible = false;

        },
        // 用户角色状态选中事件
        handleChange(obj) {
            this.houseOrderInfoQueryDto.orderStatus = Number(obj.value); // 转成数值类型，再赋值
            this.fetchFreshData(); // 重新加载数据
        },
        async serviceEvaluations(item) {
            // 额外查询评价的信息
            try {
                const queryDto = {
                    houseOrderInfoId: item.id
                };
                const { data } = await this.$axios.post(`/house-order-evaluations/list`, queryDto);
                this.houseOrderEvaluations = data;
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
        async update(item) {
            // 查询预约看房状态流转
            try {
                const queryDto = {
                    houseOrderInfoId: item.id
                };
                const [{ data: statusData }, { data: evaluationData }] = await Promise.all([
                    this.$axios.post(`/house-order-status/list`, queryDto),
                    this.$axios.post(`/house-order-evaluations/list`, queryDto)
                ]);
                this.houseOrderStatusList = statusData;
                this.houseOrderEvaluations = evaluationData;
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
        async handleSaveConfirm() {
            try {
                this.houseOrderInfo.content = this.content;
                await this.$axios.post(`/house-order-info/save`, this.houseOrderInfo);
                this.$message.success("新增成功");
                this.fetchFreshData();
                this.handleClose();
            } catch (error) {
                this.$notify.info({
                    title: '错误',
                    message: error.message,
                    duration: 1000,
                });
            }
        },
        async handleUpdateConfirm() {
            try {
                this.houseOrderInfo.content = this.content;
                this.houseOrderInfo.cover = this.cover;
                await this.$axios.put(`/house-order-info/update`, this.houseOrderInfo);
                this.$message.success("修改成功");
                this.fetchFreshData();
                this.handleClose();
            } catch (error) {
                this.$notify.info({
                    title: '错误',
                    message: error.message,
                    duration: 1000,
                });
            }
        },

        handleClose() {
            this.controlTextOperation = false;
            this.houseOrderInfo = {};
            this.content = '';
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
                const { data, total } = await this.$axios.post('/house-order-info/listLandlord', this.houseOrderInfoQueryDto);
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
        },
        evaluationStatusText(evaluation) {
            if (!evaluation || !evaluation.status) {
                return '正常';
            }
            return evaluation.statusText || (evaluation.status === 2 ? '待审核' : evaluation.status === 3 ? '已屏蔽' : '正常');
        },
        evaluationStatusType(evaluation) {
            if (!evaluation || !evaluation.status || evaluation.status === 1) {
                return 'success';
            }
            if (evaluation.status === 2) {
                return 'warning';
            }
            return 'danger';
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

.evaluation-panel {
    background: #fafafa;
    border-radius: 8px;
    padding: 14px 16px;
    margin-bottom: 16px;
}

.evaluation-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
}

.evaluation-user {
    display: flex;
    align-items: center;
    gap: 12px;

    img,
    .evaluation-avatar-placeholder {
        width: 42px;
        height: 42px;
        border-radius: 50%;
        object-fit: cover;
    }
}

.evaluation-avatar-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #eef2f6;
    color: #909399;
    font-size: 12px;
}

.evaluation-username {
    font-size: 14px;
    font-weight: 600;
    color: #333;
}

.evaluation-time {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
}

.evaluation-text {
    margin-top: 14px;
    font-size: 14px;
    line-height: 1.7;
    color: #666;
}

.evaluation-empty {
    margin-bottom: 16px;
    padding: 12px 14px;
    border-radius: 8px;
    background: #fafafa;
    color: #999;
    font-size: 13px;
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
