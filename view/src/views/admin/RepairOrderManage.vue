<template>
    <div class="container">
        <div class="top-header">
            <div class="nav-left">
                <Tab :buttons="statusTabs" initialActive="null" @change="handleChange" />
            </div>
            <div class="nav-right">
                <AutoInput placeholder="搜索工单/合同/房源/租客" @listener="listener" />
            </div>
        </div>

        <el-table :data="apiResult.data">
            <el-table-column prop="repairNo" label="工单编号" width="220"></el-table-column>
            <el-table-column prop="title" label="报修标题"></el-table-column>
            <el-table-column prop="houseName" label="房源名"></el-table-column>
            <el-table-column prop="landlordName" label="房东" width="120"></el-table-column>
            <el-table-column prop="tenantName" label="租客" width="120"></el-table-column>
            <el-table-column label="报修类型" width="120">
                <template #default="scope">
                    {{ repairTypeText(scope.row.repairType) }}
                </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
                <template #default="scope">
                    <el-tag size="mini" :type="statusType(scope.row)">
                        {{ displayStatusText(scope.row) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="repairAmount" label="维修费用" width="120"></el-table-column>
            <el-table-column label="支付状态" width="120">
                <template #default="scope">
                    <el-tag size="mini" :type="paymentStatusType(scope.row.paymentStatus)">
                        {{ paymentStatusText(scope.row.paymentStatus) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
            <el-table-column label="操作" width="220">
                <template #default="scope">
                    <div class="table-actions">
                        <span @click="showDetail(scope.row.id)">详情</span>
                        <span v-if="[1, 2, 3, 4].includes(scope.row.status)" @click="openRejectDialog(scope.row)">驳回</span>
                    </div>
                </template>
            </el-table-column>
        </el-table>

        <div class="pager">
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="queryDto.current" :page-sizes="[10, 20, 50]" :page-size="queryDto.size"
                layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
        </div>

        <el-dialog title="报修详情" :visible.sync="detailVisible" width="46%">
            <div v-if="detail.id">
                <div class="detail-block">
                    <div><strong>工单编号：</strong>{{ detail.repairNo }}</div>
                    <div><strong>报修标题：</strong>{{ detail.title }}</div>
                    <div><strong>合同编号：</strong>{{ detail.contractNo }}</div>
                    <div><strong>房源：</strong>{{ detail.houseName }}</div>
                    <div><strong>房东：</strong>{{ detail.landlordName }}</div>
                    <div><strong>租客：</strong>{{ detail.tenantName }}</div>
                    <div><strong>报修类型：</strong>{{ repairTypeText(detail.repairType) }}</div>
                    <div><strong>状态：</strong>{{ displayStatusText(detail) }}</div>
                    <div><strong>维修费用：</strong>{{ detail.repairAmount || 0 }}</div>
                    <div><strong>支付状态：</strong>{{ paymentStatusText(detail.paymentStatus) }}</div>
                    <div><strong>问题描述：</strong>{{ detail.content }}</div>
                    <div v-if="detail.attachmentUrl"><strong>现场附件：</strong><span class="link"
                            @click="openAttachment(detail.attachmentUrl)">点击查看</span></div>
                    <div v-if="detail.handleNote"><strong>处理结果：</strong>{{ detail.handleNote }}</div>
                    <div v-if="detail.handleVoucherUrl"><strong>处理凭证：</strong><span class="link"
                            @click="openAttachment(detail.handleVoucherUrl)">点击查看</span></div>
                    <div v-if="detail.cancelReason"><strong>取消原因：</strong>{{ detail.cancelReason }}</div>
                    <div v-if="detail.rejectReason"><strong>驳回原因：</strong>{{ detail.rejectReason }}</div>
                </div>
                <StatusFlow :status-records="statusList" title="报修流转记录" :status-config="statusConfig" />
            </div>
        </el-dialog>

        <el-dialog title="驳回报修工单" :visible.sync="rejectVisible" width="34%" :close-on-click-modal="false">
            <div>
                <p>*驳回原因</p>
                <el-input type="textarea" :rows="4" v-model="rejectForm.rejectReason"
                    placeholder="请填写驳回原因"></el-input>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="rejectVisible = false">取消</span>
                <span class="info-bt" @click="submitReject">确认驳回</span>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import AutoInput from "@/components/AutoInput.vue";
import Tab from "@/components/Tab.vue";
import StatusFlow from "@/components/StatusFlow.vue";

const REPAIR_TYPES = [
    { label: '水路问题', value: 1 },
    { label: '电路问题', value: 2 },
    { label: '门锁窗户', value: 3 },
    { label: '家具家电', value: 4 },
    { label: '渗水漏水', value: 5 },
    { label: '卫浴厨房', value: 6 },
    { label: '墙面地面', value: 7 },
    { label: '其他', value: 8 }
];

export default {
    components: { AutoInput, Tab, StatusFlow },
    data() {
        return {
            statusTabs: [
                { label: '全部', value: 'null' },
                { label: '待处理', value: '1' },
                { label: '已受理', value: '2' },
                { label: '处理中', value: '3' },
                { label: '待支付/确认', value: '4' },
                { label: '已完成', value: '5' },
                { label: '已取消', value: '6' },
                { label: '已驳回', value: '7' }
            ],
            statusConfig: {
                1: { text: "待处理", icon: "el-icon-time", color: "#E6A23C", status: "process" },
                2: { text: "已受理", icon: "el-icon-s-claim", color: "#409EFF", status: "process" },
                3: { text: "处理中", icon: "el-icon-loading", color: "#409EFF", status: "process" },
                4: { text: "待租户确认", icon: "el-icon-s-check", color: "#E6A23C", status: "process" },
                5: { text: "已完成", icon: "el-icon-success", color: "#67C23A", status: "success" },
                6: { text: "已取消", icon: "el-icon-circle-close", color: "#909399", status: "error" },
                7: { text: "已驳回", icon: "el-icon-warning", color: "#F56C6C", status: "error" }
            },
            apiResult: { data: [], total: 0 },
            queryDto: {
                current: 1,
                size: 10,
                keyword: '',
                status: null,
            },
            detailVisible: false,
            rejectVisible: false,
            detail: {},
            statusList: [],
            rejectForm: {
                id: null,
                rejectReason: '',
            }
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        repairTypeText(type) {
            const target = REPAIR_TYPES.find(item => item.value === Number(type));
            return target ? target.label : '其他';
        },
        statusText(status) {
            return (this.statusConfig[status] && this.statusConfig[status].text) || '未知状态';
        },
        displayStatusText(row) {
            if (Number(row.status) === 4 && Number(row.paymentStatus) === 1) {
                return '待支付';
            }
            return this.statusText(row.status);
        },
        statusType(row) {
            if (Number(row.status) === 4 && Number(row.paymentStatus) === 1) return 'warning';
            if (Number(row.status) === 5) return 'success';
            if (Number(row.status) === 6) return 'info';
            if (Number(row.status) === 7) return 'danger';
            return 'warning';
        },
        paymentStatusText(status) {
            const map = { 1: '待支付', 2: '已支付', 3: '无需支付' };
            return map[status] || '未知';
        },
        paymentStatusType(status) {
            if (Number(status) === 2) return 'success';
            if (Number(status) === 3) return 'info';
            return 'warning';
        },
        openAttachment(url) {
            const backendOrigin = new URL(this.$axios.defaults.baseURL).origin;
            const targetUrl = url.startsWith('http') ? url : `${backendOrigin}${url}`;
            window.open(targetUrl, '_blank');
        },
        listener(text) {
            this.queryDto.keyword = text;
            this.fetchFreshData();
        },
        handleChange(obj) {
            this.queryDto.status = obj.value === 'null' ? null : Number(obj.value);
            this.fetchFreshData();
        },
        handleSizeChange(size) {
            this.queryDto.size = size;
            this.fetchFreshData();
        },
        handleCurrentChange(current) {
            this.queryDto.current = current;
            this.fetchFreshData();
        },
        async fetchFreshData() {
            try {
                const { data, total } = await this.$axios.post('/repair-order/list', this.queryDto);
                this.apiResult.data = data || [];
                this.apiResult.total = total || 0;
            } catch (error) {
                this.$message.error(error.message || '加载报修工单失败');
            }
        },
        async showDetail(id) {
            try {
                const [{ data }, statusRes] = await Promise.all([
                    this.$axios.get(`/repair-order/getById/${id}`),
                    this.$axios.post('/repair-order-status/list', { repairOrderId: id, current: 1, size: 100 })
                ]);
                this.detail = data;
                this.statusList = statusRes.data || [];
                this.detailVisible = true;
            } catch (error) {
                this.$message.error(error.message || '加载详情失败');
            }
        },
        openRejectDialog(row) {
            this.rejectForm = {
                id: row.id,
                rejectReason: '',
            };
            this.rejectVisible = true;
        },
        async submitReject() {
            try {
                await this.$axios.put('/repair-order/adminReject', this.rejectForm);
                this.$message.success('报修工单已驳回');
                this.rejectVisible = false;
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message || '驳回失败');
            }
        }
    }
}
</script>

<style scoped lang="scss">
.container {
    padding: 20px;
    background-color: #fff;
}

.top-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.nav-right {
    display: flex;
    align-items: center;
    gap: 12px;
}

.table-actions {
    display: flex;
    gap: 12px;

    span {
        color: rgb(17, 139, 221);
        cursor: pointer;
    }
}

.detail-block {
    display: grid;
    gap: 10px;
    margin-bottom: 10px;
}

.link {
    color: rgb(17, 139, 221);
    cursor: pointer;
}

.pager {
    margin-top: 20px;
}
</style>
