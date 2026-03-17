<template>
    <div class="container">
        <div class="top-header">
            <div class="nav-left">
                <Tab :buttons="statusTabs" initialActive="null" @change="handleChange" />
            </div>
            <div class="nav-right">
                <div class="primary-bt" @click="openCreateDialog">
                    <i class="el-icon-plus"></i>
                    发起报修
                </div>
                <AutoInput placeholder="搜索工单/合同/房源" @listener="listener" />
            </div>
        </div>

        <el-table :data="apiResult.data">
            <el-table-column prop="repairNo" label="工单编号" width="220"></el-table-column>
            <el-table-column prop="title" label="报修标题"></el-table-column>
            <el-table-column prop="houseName" label="房源名"></el-table-column>
            <el-table-column label="报修类型" width="120">
                <template #default="scope">
                    {{ repairTypeText(scope.row.repairType) }}
                </template>
            </el-table-column>
            <el-table-column label="紧急程度" width="100">
                <template #default="scope">
                    {{ urgentText(scope.row.isUrgent) }}
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
            <el-table-column prop="expectVisitTime" label="期望上门时间" width="180"></el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="180"></el-table-column>
            <el-table-column label="操作" width="260">
                <template #default="scope">
                    <div class="table-actions">
                        <span @click="showDetail(scope.row.id)">详情</span>
                        <span v-if="[1, 2].includes(scope.row.status)" @click="cancelRepair(scope.row)">取消</span>
                        <span v-if="scope.row.status === 4 && scope.row.paymentStatus === 1" @click="payRepair(scope.row)">支付</span>
                        <span v-if="scope.row.status === 4 && scope.row.paymentStatus === 1" @click="refreshPayStatus(scope.row.id)">刷新状态</span>
                        <span v-if="canConfirmFinish(scope.row)" @click="confirmFinish(scope.row.id)">确认完成</span>
                    </div>
                </template>
            </el-table-column>
        </el-table>

        <div class="pager">
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="queryDto.current" :page-sizes="[10, 20, 50]" :page-size="queryDto.size"
                layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
        </div>

        <el-dialog title="发起报修" :visible.sync="dialogVisible" width="40%" :close-on-click-modal="false">
            <div>
                <p>*关联合同</p>
                <el-select v-model="form.contractId" style="width: 100%;" placeholder="请选择当前可报修合同"
                    @change="handleContractChange">
                    <el-option v-for="item in contractOptions" :key="item.id"
                        :label="`${item.contractNo} / ${item.houseName}`" :value="item.id">
                    </el-option>
                </el-select>
                <div v-if="selectedContract" class="tip-card">
                    <div><strong>房源：</strong>{{ selectedContract.houseName }}</div>
                    <div><strong>房东：</strong>{{ selectedContract.landlordName }}</div>
                    <div><strong>合同状态：</strong>{{ contractStatusText(selectedContract.status) }}</div>
                </div>
                <p>*报修标题</p>
                <el-input v-model="form.title" placeholder="例如：卫生间热水器无法启动"></el-input>
                <div class="grid-two">
                    <div>
                        <p>*报修类型</p>
                        <el-select v-model="form.repairType" style="width: 100%;" placeholder="请选择">
                            <el-option v-for="item in repairTypeOptions" :key="item.value" :label="item.label"
                                :value="item.value"></el-option>
                        </el-select>
                    </div>
                    <div>
                        <p>*紧急程度</p>
                        <el-select v-model="form.isUrgent" style="width: 100%;" placeholder="请选择">
                            <el-option label="普通" :value="0"></el-option>
                            <el-option label="紧急" :value="1"></el-option>
                        </el-select>
                    </div>
                </div>
                <p>期望上门时间</p>
                <el-date-picker v-model="form.expectVisitTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
                    placeholder="选择日期时间" style="width: 100%;"></el-date-picker>
                <p>*问题描述</p>
                <el-input type="textarea" :rows="4" v-model="form.content"
                    placeholder="尽量描述清楚故障现象、影响范围和已尝试的处理方式"></el-input>
                <p>现场附件</p>
                <div class="upload-tip">支持上传 PDF / JPG / JPEG / PNG，建议上传清晰现场照片。</div>
                <el-upload :action="uploadAction" :show-file-list="false" :on-success="handleAttachmentUpload"
                    accept=".pdf,.jpg,.jpeg,.png">
                    <span class="primary-bt">上传附件</span>
                </el-upload>
                <div v-if="form.attachmentUrl" class="link" @click="openAttachment(form.attachmentUrl)">
                    已上传附件，点击查看
                </div>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="dialogVisible = false">取消</span>
                <span class="info-bt" @click="saveRepair">确认提交</span>
            </span>
        </el-dialog>

        <el-dialog title="报修详情" :visible.sync="detailVisible" width="46%">
            <div v-if="detail.id">
                <div class="detail-block">
                    <div><strong>工单编号：</strong>{{ detail.repairNo }}</div>
                    <div><strong>报修标题：</strong>{{ detail.title }}</div>
                    <div><strong>合同编号：</strong>{{ detail.contractNo }}</div>
                    <div><strong>房源：</strong>{{ detail.houseName }}</div>
                    <div><strong>房东：</strong>{{ detail.landlordName }}</div>
                    <div><strong>报修类型：</strong>{{ repairTypeText(detail.repairType) }}</div>
                    <div><strong>紧急程度：</strong>{{ urgentText(detail.isUrgent) }}</div>
                    <div><strong>状态：</strong>{{ displayStatusText(detail) }}</div>
                    <div><strong>维修费用：</strong>{{ detail.repairAmount || 0 }}</div>
                    <div><strong>支付状态：</strong>{{ paymentStatusText(detail.paymentStatus) }}</div>
                    <div><strong>期望上门时间：</strong>{{ detail.expectVisitTime || '-' }}</div>
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
            repairTypeOptions: REPAIR_TYPES,
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
            dialogVisible: false,
            detailVisible: false,
            contractOptions: [],
            selectedContract: null,
            uploadAction: '',
            form: this.getDefaultForm(),
            detail: {},
            statusList: [],
        };
    },
    created() {
        const backendOrigin = new URL(this.$axios.defaults.baseURL).origin;
        this.uploadAction = `${backendOrigin}/api/v1.0/house-rental-api/file/upload`;
        this.refresh();
    },
    methods: {
        getDefaultForm() {
            return {
                contractId: null,
                title: '',
                repairType: 1,
                content: '',
                isUrgent: 0,
                expectVisitTime: '',
                attachmentUrl: '',
            };
        },
        contractStatusText(status) {
            const map = {
                4: '已生效',
                9: '退租申请中',
                10: '待退租审核',
                11: '待退押',
                12: '待审核退押'
            };
            return map[status] || '其他';
        },
        repairTypeText(type) {
            const target = this.repairTypeOptions.find(item => item.value === Number(type));
            return target ? target.label : '其他';
        },
        urgentText(value) {
            return Number(value) === 1 ? '紧急' : '普通';
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
        canConfirmFinish(row) {
            return Number(row.status) === 4 && Number(row.paymentStatus) !== 1;
        },
        openAttachment(url) {
            const backendOrigin = new URL(this.$axios.defaults.baseURL).origin;
            const targetUrl = url.startsWith('http') ? url : `${backendOrigin}${url}`;
            window.open(targetUrl, '_blank');
        },
        async refresh() {
            await Promise.all([this.fetchFreshData(), this.fetchContractOptions()]);
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
                const { data, total } = await this.$axios.post('/repair-order/listUser', this.queryDto);
                this.apiResult.data = data || [];
                this.apiResult.total = total || 0;
            } catch (error) {
                this.$message.error(error.message || '加载报修工单失败');
            }
        },
        async fetchContractOptions() {
            try {
                const { data } = await this.$axios.post('/rental-contract/listUser', { current: 1, size: 1000 });
                this.contractOptions = (data || []).filter(item => [4, 9, 10, 11, 12].includes(item.status));
            } catch (error) {
                console.error('加载合同失败:', error);
            }
        },
        openCreateDialog() {
            this.form = this.getDefaultForm();
            this.selectedContract = null;
            this.dialogVisible = true;
        },
        handleContractChange(id) {
            this.selectedContract = this.contractOptions.find(item => item.id === id) || null;
        },
        handleAttachmentUpload(res) {
            if (res.code === 200) {
                this.form.attachmentUrl = res.data;
                this.$message.success('附件上传成功');
            } else {
                this.$message.error(res.message || '附件上传失败');
            }
        },
        async saveRepair() {
            try {
                await this.$axios.post('/repair-order/save', this.form);
                this.$message.success('报修工单已提交');
                this.dialogVisible = false;
                this.refresh();
            } catch (error) {
                this.$message.error(error.message || '提交失败');
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
        async cancelRepair(row) {
            try {
                const { value } = await this.$prompt('请输入取消原因（可为空）', '取消报修', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    inputPlaceholder: '取消原因'
                });
                await this.$axios.put('/repair-order/tenantCancel', { id: row.id, cancelReason: value || '' });
                this.$message.success('报修工单已取消');
                this.fetchFreshData();
            } catch (error) {
                if (error === 'cancel') {
                    return;
                }
                this.$message.error(error.message || '取消失败');
            }
        },
        async confirmFinish(id) {
            try {
                await this.$confirm('确认本次报修已处理完成吗？确认后工单将结束。', '确认完成', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                });
                await this.$axios.put(`/repair-order/tenantConfirmFinish/${id}`);
                this.$message.success('报修工单已完成');
                this.fetchFreshData();
            } catch (error) {
                if (error === 'cancel' || error === 'close') {
                    return;
                }
                this.$message.error(error.message || '确认失败');
            }
        },
        async payRepair(row) {
            try {
                const payWindow = window.open('', '_blank');
                const { data } = await this.$axios.post(`/payment-order/payRepair/${row.id}`);
                if (!payWindow) {
                    this.$message.warning('浏览器拦截了支付窗口，请允许弹窗后重试');
                    return;
                }
                payWindow.document.write(data.htmlForm);
                payWindow.document.close();
            } catch (error) {
                this.$message.error(error.message || '拉起支付失败');
            }
        },
        async refreshPayStatus(id) {
            try {
                await this.$axios.get(`/payment-order/queryRepairStatus/${id}`);
                this.$message.success('已同步最新支付状态');
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message || '同步状态失败');
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

.grid-two {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
}

.tip-card {
    margin: 12px 0;
    padding: 12px 14px;
    border-radius: 6px;
    background: #f5f7fa;
    color: #606266;
    display: grid;
    gap: 6px;
}

.upload-tip {
    margin-bottom: 8px;
    color: #909399;
    font-size: 12px;
}

.detail-block {
    display: grid;
    gap: 10px;
    margin-bottom: 10px;
}

.link {
    color: rgb(17, 139, 221);
    cursor: pointer;
    margin-top: 8px;
}

.pager {
    margin-top: 20px;
}
</style>
