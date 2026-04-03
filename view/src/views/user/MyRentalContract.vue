<template>
    <div class="container">
        <div class="top-header">
            <div class="nav-left">
                <Tab :buttons="statusTabs" initialActive="null" @change="handleChange" />
            </div>
            <div class="nav-right">
                <AutoInput placeholder="搜索合同/房源" @listener="listener" />
            </div>
        </div>

        <el-table :data="apiResult.data">
            <el-table-column prop="contractNo" label="合同编号" width="220"></el-table-column>
            <el-table-column prop="title" label="合同标题"></el-table-column>
            <el-table-column prop="houseName" label="房源名"></el-table-column>
            <el-table-column prop="landlordName" label="房东"></el-table-column>
            <el-table-column label="租期" width="220">
                <template #default="scope">
                    {{ scope.row.startDate }} 至 {{ scope.row.endDate }}
                </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
                <template #default="scope">
                    <el-tag :type="statusType(scope.row.status)" size="mini">{{ statusText(scope.row.status) }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="280">
                <template #default="scope">
                    <div class="table-actions">
                        <span @click="showDetail(scope.row.id)">详情</span>
                        <span v-if="scope.row.status === 2" @click="confirmContract(scope.row.id)">确认</span>
                        <span v-if="scope.row.status === 2" @click="rejectContract(scope.row.id)">拒绝</span>
                        <span v-if="scope.row.status === 3" @click="goPayDeposit()">去支付押金</span>
                        <span v-if="[4, 9, 10, 11, 12].includes(scope.row.status)" @click="goTerminationPage()">退租管理</span>
                    </div>
                </template>
            </el-table-column>
        </el-table>

        <div class="pager">
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="queryDto.current" :page-sizes="[10, 20, 50]" :page-size="queryDto.size"
                layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
        </div>

        <el-dialog title="合同详情" :visible.sync="detailVisible" width="46%">
            <div v-if="detail.id">
                <div class="detail-block">
                    <div><strong>合同编号：</strong>{{ detail.contractNo }}</div>
                    <div><strong>合同标题：</strong>{{ detail.title }}</div>
                    <div><strong>房源：</strong>{{ detail.houseName }}</div>
                    <div><strong>房东：</strong>{{ detail.landlordName }}</div>
                    <div><strong>状态：</strong>{{ statusText(detail.status) }}</div>
                    <div><strong>租期：</strong>{{ detail.startDate }} 至 {{ detail.endDate }}</div>
                    <div><strong>月租：</strong>{{ detail.monthlyRent }}</div>
                    <div><strong>押金：</strong>{{ detail.depositAmount }}</div>
                    <div><strong>付款方式：</strong>{{ detail.payCycle }}</div>
                    <div><strong>水电费支付方式：</strong>{{ utilityModeText(detail.utilityPaymentMode) }}</div>
                    <div v-if="detail.contractContent"><strong>合同内容：</strong>{{ detail.contractContent }}</div>
                    <div v-if="detail.attachmentUrl"><strong>合同附件：</strong><span class="link" @click="openAttachment(detail.attachmentUrl)">点击查看</span></div>
                    <div v-if="detail.rejectReason"><strong>拒绝原因：</strong>{{ detail.rejectReason }}</div>
                    <div v-if="detail.cancelReason"><strong>取消原因：</strong>{{ detail.cancelReason }}</div>
                </div>
                <StatusFlow :status-records="statusList" title="合同状态流转记录" :status-config="contractStatusConfig" />
            </div>
        </el-dialog>
    </div>
</template>

<script>
import AutoInput from "@/components/AutoInput.vue";
import Tab from "@/components/Tab.vue";
import StatusFlow from "@/components/StatusFlow.vue";

export default {
    components: { AutoInput, Tab, StatusFlow },
    data() {
        return {
            statusTabs: [
                { label: '全部', value: 'null' },
                { label: '待确认', value: '2' },
                { label: '待支付押金', value: '3' },
                { label: '已生效', value: '4' },
                { label: '已拒绝', value: '6' },
                { label: '已取消', value: '7' },
                { label: '已到期', value: '8' }
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
            detail: {},
            statusList: [],
            contractStatusConfig: {
                1: { text: "待管理员审核", icon: "el-icon-s-check", color: "#E6A23C", status: "process" },
                2: { text: "待租客确认", icon: "el-icon-time", color: "#409EFF", status: "process" },
                3: { text: "待支付押金", icon: "el-icon-wallet", color: "#E6A23C", status: "process" },
                4: { text: "已生效", icon: "el-icon-success", color: "#67C23A", status: "success" },
                6: { text: "已拒绝", icon: "el-icon-close", color: "#F56C6C", status: "error" },
                7: { text: "已取消", icon: "el-icon-warning", color: "#909399", status: "error" },
                8: { text: "已到期", icon: "el-icon-finished", color: "#909399", status: "success" },
                9: { text: "待提交退租结算", icon: "el-icon-document", color: "#E6A23C", status: "process" },
                10: { text: "待退租审核", icon: "el-icon-s-check", color: "#409EFF", status: "process" },
                11: { text: "待退押", icon: "el-icon-money", color: "#E6A23C", status: "process" },
                12: { text: "待审核退押", icon: "el-icon-s-order", color: "#409EFF", status: "process" },
                13: { text: "已退租", icon: "el-icon-circle-close", color: "#909399", status: "success" },
                14: { text: "待对方确认退租", icon: "el-icon-user", color: "#E6A23C", status: "process" }
            }
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        statusText(status) {
            if (status === 13) {
                return '已取消';
            }
            return (this.contractStatusConfig[status] && this.contractStatusConfig[status].text) || '未知状态';
        },
        statusType(status) {
            if (status === 4) return 'success';
            if ([2, 3].includes(status)) return 'warning';
            if (status === 6) return 'danger';
            return 'info';
        },
        utilityModeText(type) {
            return type === 1 ? '自行缴费' : '房东结算';
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
                const { data } = await this.$axios.post('/rental-contract/listUser', { current: 1, size: 1000 });
                this.rawList = data || [];
                this.applyFilters();
            } catch (error) {
                this.$message.error(error.message || '加载合同失败');
            }
        },
        applyFilters() {
            const keyword = (this.queryDto.keyword || '').trim().toLowerCase();
            let list = [...this.rawList].filter(item => [2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13].includes(item.status));
            if (this.queryDto.status !== null) {
                if (this.queryDto.status === 7) {
                    list = list.filter(item => [7, 13].includes(item.status));
                } else {
                    list = list.filter(item => item.status === this.queryDto.status);
                }
            }
            if (keyword) {
                list = list.filter(item => {
                    const values = [
                        item.contractNo,
                        item.title,
                        item.houseName,
                        item.landlordName
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
        async confirmContract(id) {
            try {
                await this.$confirm('确定要确认当前租赁合同吗？确认后将进入押金支付流程。', '确认租赁合同', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                });
                await this.$axios.put(`/rental-contract/tenantConfirm/${id}`);
                this.$message.success('合同已确认，请先支付押金');
                this.fetchFreshData();
            } catch (error) {
                if (error === 'cancel' || error === 'close') {
                    return;
                }
                this.$message.error(error.message || '确认失败');
            }
        },
        async rejectContract(id) {
            try {
                const { value } = await this.$prompt('请输入拒绝原因（可为空）', '拒绝合同', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    inputPlaceholder: '拒绝原因'
                });
                await this.$axios.put('/rental-contract/tenantReject', { id, rejectReason: value || '' });
                this.$message.success('合同已拒绝');
                this.fetchFreshData();
            } catch (error) {
                if (error === 'cancel') {
                    return;
                }
                this.$message.error(error.message || '拒绝失败');
            }
        },
        goPayDeposit() {
            this.$router.push('/my-rental-bill');
        },
        goTerminationPage() {
            this.$router.push('/my-rental-termination');
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
