<template>
    <div class="container">
        <div class="top-header">
            <div class="nav-left">
                <Tab :buttons="statusTabs" initialActive="null" @change="handleChange" />
            </div>
            <div class="nav-right">
                <AutoInput placeholder="搜索合同/房源/租客" @listener="listener" />
            </div>
        </div>

        <el-table :data="apiResult.data">
            <el-table-column prop="contractNo" label="合同编号" width="220"></el-table-column>
            <el-table-column prop="title" label="合同标题"></el-table-column>
            <el-table-column prop="houseName" label="房源名"></el-table-column>
            <el-table-column prop="landlordName" label="房东"></el-table-column>
            <el-table-column prop="tenantName" label="租客"></el-table-column>
            <el-table-column label="状态" width="120">
                <template #default="scope">
                    <el-tag :type="statusType(scope.row.status)" size="mini">{{ statusText(scope.row.status) }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
            <el-table-column label="操作" width="320">
                <template #default="scope">
                    <div class="table-actions">
                        <span @click="showDetail(scope.row.id)">详情</span>
                        <span v-if="scope.row.status === 10" @click="approveTerminate(scope.row.id)">通过退租</span>
                        <span v-if="scope.row.status === 10" @click="openTerminateReject(scope.row.id)">驳回退租</span>
                        <span v-if="scope.row.status === 12" @click="approveTerminateRefund(scope.row.id)">通过退押</span>
                        <span v-if="scope.row.status === 12" @click="openRefundReject(scope.row.id)">驳回退押</span>
                    </div>
                </template>
            </el-table-column>
        </el-table>

        <div class="pager">
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="queryDto.current" :page-sizes="[10, 20, 50]" :page-size="queryDto.size"
                layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
        </div>

        <el-dialog title="退租详情" :visible.sync="detailVisible" width="46%">
            <div v-if="detail.id">
                <div class="detail-block">
                    <div><strong>合同编号：</strong>{{ detail.contractNo }}</div>
                    <div><strong>合同标题：</strong>{{ detail.title }}</div>
                    <div><strong>房源：</strong>{{ detail.houseName }}</div>
                    <div><strong>房东：</strong>{{ detail.landlordName }}</div>
                    <div><strong>租客：</strong>{{ detail.tenantName }}</div>
                    <div><strong>当前状态：</strong>{{ statusText(detail.status) }}</div>
                    <div><strong>原始押金：</strong>{{ detail.depositAmount }}</div>
                    <div v-if="detail.terminationReason"><strong>{{ applyReasonLabel(detail) }}：</strong>{{ detail.terminationReason }}</div>
                    <div v-if="detail.terminationRefundAmount !== null && detail.terminationRefundAmount !== undefined">
                        <strong>协商退押金额：</strong>{{ detail.terminationRefundAmount }}
                    </div>
                    <div v-if="detail.terminationVoucherUrl"><strong>协商凭证：</strong><span class="link"
                            @click="openAttachment(detail.terminationVoucherUrl)">点击查看</span></div>
                    <div v-if="detail.terminationVoucherNote"><strong>协商备注：</strong>{{ detail.terminationVoucherNote }}</div>
                    <div v-if="detail.terminationCounterpartyRejectReason"><strong>{{ counterpartyReplyLabel(detail) }}：</strong>{{ detail.terminationCounterpartyRejectReason }}</div>
                    <div v-if="detail.terminationAuditNote"><strong>管理员审核意见：</strong>{{ detail.terminationAuditNote }}</div>
                    <div v-if="detail.terminationRefundVoucherUrl"><strong>退押凭证：</strong><span class="link"
                            @click="openAttachment(detail.terminationRefundVoucherUrl)">点击查看</span></div>
                    <div v-if="detail.terminationRefundVoucherNote"><strong>退押说明：</strong>{{ detail.terminationRefundVoucherNote }}</div>
                </div>
                <StatusFlow :status-records="statusList" title="退租流转记录" :status-config="terminationStatusConfig" />
            </div>
        </el-dialog>

        <el-dialog title="驳回退租申请" :visible.sync="terminateRejectVisible" width="32%" :close-on-click-modal="false">
            <div>
                <p>*驳回原因</p>
                <el-input type="textarea" :rows="4" v-model="terminateRejectForm.terminationAuditNote"
                    placeholder="请填写驳回原因"></el-input>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="terminateRejectVisible = false">取消</span>
                <span class="info-bt" @click="submitTerminateReject">确认驳回</span>
            </span>
        </el-dialog>

        <el-dialog title="驳回退押凭证" :visible.sync="refundRejectVisible" width="32%" :close-on-click-modal="false">
            <div>
                <p>*驳回原因</p>
                <el-input type="textarea" :rows="4" v-model="refundRejectForm.terminationAuditNote"
                    placeholder="请填写驳回原因"></el-input>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="refundRejectVisible = false">取消</span>
                <span class="info-bt" @click="submitRefundReject">确认驳回</span>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import AutoInput from "@/components/AutoInput.vue";
import Tab from "@/components/Tab.vue";
import StatusFlow from "@/components/StatusFlow.vue";

const TERMINATION_STATUSES = [9, 10, 11, 12, 13, 14];

export default {
    components: { AutoInput, Tab, StatusFlow },
    data() {
        return {
            statusTabs: [
                { label: '全部', value: 'null' },
                { label: '待对方确认', value: '14' },
                { label: '待提交结算', value: '9' },
                { label: '待退租审核', value: '10' },
                { label: '待退押', value: '11' },
                { label: '待审核退押', value: '12' },
                { label: '已退租', value: '13' }
            ],
            apiResult: {
                data: [],
                total: 0,
            },
            rawList: [],
            queryDto: {
                current: 1,
                size: 10,
                keyword: '',
                status: null,
            },
            detailVisible: false,
            terminateRejectVisible: false,
            refundRejectVisible: false,
            detail: {},
            statusList: [],
            terminateRejectForm: {
                id: null,
                terminationAuditNote: '',
            },
            refundRejectForm: {
                id: null,
                terminationAuditNote: '',
            },
            terminationStatusConfig: {
                1: { text: "待管理员审核", icon: "el-icon-s-check", color: "#E6A23C", status: "process" },
                2: { text: "待租客确认", icon: "el-icon-time", color: "#409EFF", status: "process" },
                3: { text: "待支付押金", icon: "el-icon-wallet", color: "#E6A23C", status: "process" },
                4: { text: "已生效", icon: "el-icon-success", color: "#67C23A", status: "success" },
                9: { text: "待提交退租结算", icon: "el-icon-edit-outline", color: "#E6A23C", status: "process" },
                10: { text: "待退租审核", icon: "el-icon-s-check", color: "#409EFF", status: "process" },
                11: { text: "待退押", icon: "el-icon-money", color: "#E6A23C", status: "process" },
                12: { text: "待审核退押", icon: "el-icon-s-check", color: "#409EFF", status: "process" },
                13: { text: "已退租", icon: "el-icon-circle-check", color: "#67C23A", status: "success" },
                14: { text: "待对方确认退租", icon: "el-icon-user", color: "#E6A23C", status: "process" }
            }
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        statusText(status) {
            return (this.terminationStatusConfig[status] && this.terminationStatusConfig[status].text) || '未知状态';
        },
        statusType(status) {
            if (status === 13) return 'success';
            if ([9, 10, 11, 12].includes(status)) return 'warning';
            return 'info';
        },
        applicantRoleText(row) {
            return row && row.terminationApplyUserId === row.tenantUserId ? '租客' : '房东';
        },
        counterpartyRoleText(row) {
            return this.applicantRoleText(row) === '租客' ? '房东' : '租客';
        },
        applyReasonLabel(row) {
            return `${this.applicantRoleText(row)}申请原因`;
        },
        counterpartyReplyLabel(row) {
            return `${this.counterpartyRoleText(row)}回复`;
        },
        openAttachment(url) {
            const backendOrigin = new URL(this.$axios.defaults.baseURL).origin;
            const targetUrl = url.startsWith('http') ? url : `${backendOrigin}${url}`;
            window.open(targetUrl, '_blank');
        },
        listener(text) {
            this.queryDto.keyword = text;
            this.queryDto.current = 1;
            this.applyFilters();
        },
        handleChange(obj) {
            this.queryDto.status = obj.value === 'null' ? null : Number(obj.value);
            this.queryDto.current = 1;
            this.applyFilters();
        },
        handleSizeChange(size) {
            this.queryDto.size = size;
            this.queryDto.current = 1;
            this.applyFilters();
        },
        handleCurrentChange(current) {
            this.queryDto.current = current;
            this.applyFilters();
        },
        async fetchFreshData() {
            try {
                const { data } = await this.$axios.post('/rental-contract/list', { current: 1, size: 1000 });
                this.rawList = data || [];
                this.applyFilters();
            } catch (error) {
                this.$message.error(error.message || '加载退租数据失败');
            }
        },
        applyFilters() {
            const keyword = (this.queryDto.keyword || '').trim().toLowerCase();
            let list = this.rawList.filter(item => TERMINATION_STATUSES.includes(item.status));
            if (this.queryDto.status !== null) {
                list = list.filter(item => item.status === this.queryDto.status);
            }
            if (keyword) {
                list = list.filter(item => {
                    const values = [
                        item.contractNo,
                        item.title,
                        item.houseName,
                        item.landlordName,
                        item.tenantName
                    ].filter(Boolean).join(' ').toLowerCase();
                    return values.includes(keyword);
                });
            }
            this.apiResult.total = list.length;
            const start = (this.queryDto.current - 1) * this.queryDto.size;
            const end = start + this.queryDto.size;
            this.apiResult.data = list.slice(start, end);
        },
        async showDetail(id) {
            try {
                const [{ data }, statusRes] = await Promise.all([
                    this.$axios.get(`/rental-contract/getById/${id}`),
                    this.$axios.post('/rental-contract-status/list', { rentalContractId: id, current: 1, size: 100 })
                ]);
                this.detail = data;
                this.statusList = statusRes.data;
                this.detailVisible = true;
            } catch (error) {
                this.$message.error(error.message || '加载详情失败');
            }
        },
        async approveTerminate(id) {
            try {
                await this.$axios.put(`/rental-contract/adminApproveTerminate/${id}`);
                this.$message.success('退租已审核通过');
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message || '审核失败');
            }
        },
        openTerminateReject(id) {
            this.terminateRejectForm = {
                id,
                terminationAuditNote: '',
            };
            this.terminateRejectVisible = true;
        },
        async submitTerminateReject() {
            try {
                await this.$axios.put('/rental-contract/adminRejectTerminate', this.terminateRejectForm);
                this.$message.success('退租申请已驳回');
                this.terminateRejectVisible = false;
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message || '驳回失败');
            }
        },
        async approveTerminateRefund(id) {
            try {
                await this.$axios.put(`/rental-contract/adminApproveTerminateRefund/${id}`);
                this.$message.success('退押凭证审核通过');
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message || '审核失败');
            }
        },
        openRefundReject(id) {
            this.refundRejectForm = {
                id,
                terminationAuditNote: '',
            };
            this.refundRejectVisible = true;
        },
        async submitRefundReject() {
            try {
                await this.$axios.put('/rental-contract/adminRejectTerminateRefund', this.refundRejectForm);
                this.$message.success('退押凭证已驳回');
                this.refundRejectVisible = false;
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

.page-tip {
    margin-bottom: 16px;
    padding: 12px 14px;
    border-radius: 6px;
    background: #f4f8ff;
    color: #4c5b70;
    line-height: 1.6;
}

.top-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
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
