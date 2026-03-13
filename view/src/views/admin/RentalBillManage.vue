<template>
    <div class="container">
        <div class="top-header">
            <div class="nav-left">
                <Tab :buttons="[
                    { label: '全部', value: 'null' },
                    { label: '待支付', value: '1' },
                    { label: '已支付', value: '2' },
                    { label: '已取消', value: '3' }
                ]" initialActive="null" @change="handleStatusChange" />
            </div>
            <div class="nav-right">
                <AutoInput placeholder="搜索账单/合同/房源/租客" @listener="listener" />
            </div>
        </div>

        <el-table :data="apiResult.data">
            <el-table-column prop="billNo" label="账单编号" width="220"></el-table-column>
            <el-table-column prop="contractNo" label="合同编号" width="220"></el-table-column>
            <el-table-column prop="houseName" label="房源名"></el-table-column>
            <el-table-column prop="landlordName" label="房东" width="120"></el-table-column>
            <el-table-column prop="tenantName" label="租客" width="120"></el-table-column>
            <el-table-column label="账单类型" width="100">
                <template #default="scope">
                    {{ billTypeText(scope.row.billType) }}
                </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="应付金额" width="120"></el-table-column>
            <el-table-column label="支付状态" width="120">
                <template #default="scope">
                    <el-tag size="mini" :type="payTagType(scope.row.payStatus)">
                        {{ payStatusText(scope.row.payStatus) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="凭证状态" width="120">
                <template #default="scope">
                    <el-tag size="mini" :type="proofTagType(scope.row.utilityProofStatus)">
                        {{ proofStatusText(scope.row.utilityProofStatus) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="dueDate" label="到期日期" width="120"></el-table-column>
            <el-table-column label="操作" width="120">
                <template #default="scope">
                    <div class="table-actions">
                        <span @click="showDetail(scope.row.id)">详情</span>
                    </div>
                </template>
            </el-table-column>
        </el-table>

        <div class="pager">
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="queryDto.current" :page-sizes="[10, 20]" :page-size="queryDto.size"
                layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
        </div>

        <el-dialog title="账单详情" :visible.sync="detailVisible" width="44%">
            <div v-if="detail.id" class="detail-block">
                <div><strong>账单编号：</strong>{{ detail.billNo }}</div>
                <div><strong>合同编号：</strong>{{ detail.contractNo }}</div>
                <div><strong>房源：</strong>{{ detail.houseName }}</div>
                <div><strong>房东：</strong>{{ detail.landlordName }}</div>
                <div><strong>租客：</strong>{{ detail.tenantName }}</div>
                <div><strong>账单类型：</strong>{{ billTypeText(detail.billType) }}</div>
                <div><strong>账单月份：</strong>{{ detail.billMonth || '-' }}</div>
                <div><strong>应付金额：</strong>{{ detail.totalAmount }}</div>
                <div><strong>支付状态：</strong>{{ payStatusText(detail.payStatus) }}</div>
                <div><strong>凭证状态：</strong>{{ proofStatusText(detail.utilityProofStatus) }}</div>
                <div><strong>备注：</strong>{{ detail.remark || '-' }}</div>
                <div v-if="detail.utilityProofUrl">
                    <strong>缴费凭证：</strong>
                    <span class="link" @click="openAttachment(detail.utilityProofUrl)">点击查看</span>
                </div>
                <div v-if="detail.utilityProofNote"><strong>凭证备注：</strong>{{ detail.utilityProofNote }}</div>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import AutoInput from "@/components/AutoInput.vue";
import Tab from "@/components/Tab.vue";

export default {
    components: { AutoInput, Tab },
    data() {
        return {
            apiResult: { data: [], total: 0 },
            queryDto: {
                current: 1,
                size: 10,
                keyword: '',
                payStatus: null,
            },
            detailVisible: false,
            detail: {},
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        billTypeText(type) {
            return type === 1 ? '押金单' : '租金单';
        },
        proofStatusText(status) {
            const map = { 1: '待提交', 2: '待审核', 3: '已通过', 4: '已驳回', 5: '无需凭证' };
            return map[status] || '未知';
        },
        payStatusText(status) {
            const map = { 1: '待支付', 2: '已支付', 3: '已取消' };
            return map[status] || '未知';
        },
        proofTagType(status) {
            if (status === 2) return 'warning';
            if (status === 3 || status === 5) return 'success';
            if (status === 4) return 'danger';
            return 'info';
        },
        payTagType(status) {
            if (status === 2) return 'success';
            if (status === 3) return 'info';
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
        handleStatusChange(obj) {
            this.queryDto.payStatus = obj.value === 'null' ? null : Number(obj.value);
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
                const { data, total } = await this.$axios.post('/rental-bill/list', this.queryDto);
                this.apiResult.data = data;
                this.apiResult.total = total;
            } catch (error) {
                this.$message.error(error.message || '加载账单失败');
            }
        },
        async showDetail(id) {
            try {
                const { data } = await this.$axios.get(`/rental-bill/getById/${id}`);
                this.detail = data;
                this.detailVisible = true;
            } catch (error) {
                this.$message.error(error.message || '加载详情失败');
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

.table-actions span,
.link {
    color: rgb(17, 139, 221);
    cursor: pointer;
}

.detail-block {
    display: grid;
    gap: 10px;
}

.pager {
    margin-top: 20px;
}
</style>
