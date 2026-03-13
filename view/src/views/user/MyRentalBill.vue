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
                <AutoInput placeholder="搜索账单/合同/房源" @listener="listener" />
            </div>
        </div>

        <el-table :data="apiResult.data">
            <el-table-column prop="billNo" label="账单编号" width="220"></el-table-column>
            <el-table-column prop="contractNo" label="合同编号" width="220"></el-table-column>
            <el-table-column prop="houseName" label="房源名"></el-table-column>
            <el-table-column label="账单类型" width="100">
                <template #default="scope">
                    {{ billTypeText(scope.row.billType) }}
                </template>
            </el-table-column>
            <el-table-column prop="billMonth" label="账期" width="160"></el-table-column>
            <el-table-column prop="totalAmount" label="应付金额" width="120"></el-table-column>
            <el-table-column label="水电方式" width="120">
                <template #default="scope">
                    {{ utilityModeText(scope.row.utilityPaymentMode) }}
                </template>
            </el-table-column>
            <el-table-column label="凭证状态" width="120">
                <template #default="scope">
                    <el-tag size="mini" :type="proofTagType(scope.row.utilityProofStatus)">
                        {{ proofStatusText(scope.row.utilityProofStatus) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="支付状态" width="120">
                <template #default="scope">
                    <el-tag size="mini" :type="payTagType(scope.row.payStatus)">
                        {{ payStatusText(scope.row.payStatus) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="dueDate" label="到期日期" width="120"></el-table-column>
            <el-table-column label="操作" width="280">
                <template #default="scope">
                    <div class="table-actions">
                        <span @click="showDetail(scope.row.id)">详情</span>
                        <span v-if="scope.row.payStatus === 1" @click="payBill(scope.row)">支付</span>
                        <span v-if="showProofAction(scope.row)" @click="openProofDialog(scope.row)">上传凭证</span>
                        <span v-if="scope.row.payStatus === 1" @click="refreshPayStatus(scope.row.id)">刷新状态</span>
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
                <div><strong>账单类型：</strong>{{ billTypeText(detail.billType) }}</div>
                <div><strong>账期：</strong>{{ detail.billMonth || '-' }}</div>
                <div><strong>租金：</strong>{{ detail.rentAmount }}</div>
                <div><strong>水费：</strong>{{ detail.waterAmount }}</div>
                <div><strong>电费：</strong>{{ detail.electricAmount }}</div>
                <div><strong>应付金额：</strong>{{ detail.totalAmount }}</div>
                <div><strong>水电方式：</strong>{{ utilityModeText(detail.utilityPaymentMode) }}</div>
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

        <el-dialog title="上传水电费凭证" :visible.sync="proofVisible" width="34%" :close-on-click-modal="false">
            <div>
                <div class="upload-tip">支持上传 PDF / JPG / JPEG / PNG，建议上传清晰的缴费截图或 PDF 回执。</div>
                <el-upload :action="uploadAction" :show-file-list="false" :on-success="handleProofUpload"
                    accept=".pdf,.jpg,.jpeg,.png">
                    <span class="primary-bt">上传缴费凭证</span>
                </el-upload>
                <div v-if="proofForm.utilityProofUrl" class="link proof-link"
                    @click="openAttachment(proofForm.utilityProofUrl)">
                    已上传凭证，点击查看
                </div>
                <p>备注</p>
                <el-input type="textarea" :rows="3" v-model="proofForm.utilityProofNote"
                    placeholder="例如：已于小程序自行完成水电缴费"></el-input>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="proofVisible = false">取消</span>
                <span class="info-bt" @click="submitProof">确认提交</span>
            </span>
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
            proofVisible: false,
            proofForm: {
                id: null,
                utilityProofUrl: '',
                utilityProofNote: '',
            },
            uploadAction: '',
        };
    },
    created() {
        const backendOrigin = new URL(this.$axios.defaults.baseURL).origin;
        this.uploadAction = `${backendOrigin}/api/v1.0/house-rental-api/file/upload`;
        this.fetchFreshData();
    },
    methods: {
        billTypeText(type) {
            return type === 1 ? '押金单' : '租金单';
        },
        utilityModeText(type) {
            return type === 1 ? '自行缴费' : '房东结算';
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
        showProofAction(row) {
            return row.billType === 2 && row.utilityPaymentMode === 1 && row.utilityProofStatus !== 3;
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
                const { data, total } = await this.$axios.post('/rental-bill/listUser', this.queryDto);
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
        },
        async payBill(row) {
            try {
                const payWindow = window.open('', '_blank');
                const { data } = await this.$axios.post(`/payment-order/payBill/${row.id}`);
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
                await this.$axios.get(`/payment-order/queryBillStatus/${id}`);
                this.$message.success('已同步最新支付状态');
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message || '同步状态失败');
            }
        },
        openProofDialog(row) {
            this.proofForm = {
                id: row.id,
                utilityProofUrl: row.utilityProofUrl || '',
                utilityProofNote: row.utilityProofNote || '',
            };
            this.proofVisible = true;
        },
        handleProofUpload(res) {
            if (res.code === 200) {
                this.proofForm.utilityProofUrl = res.data;
                this.$message.success('凭证上传成功');
            } else {
                this.$message.error(res.message || '凭证上传失败');
            }
        },
        async submitProof() {
            try {
                await this.$axios.put('/rental-bill/submitUtilityProof', this.proofForm);
                this.$message.success('缴费凭证提交成功');
                this.proofVisible = false;
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message || '提交失败');
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
}

.upload-tip {
    color: #909399;
    font-size: 12px;
    margin-bottom: 12px;
}

.link {
    color: rgb(17, 139, 221);
    cursor: pointer;
}

.proof-link {
    display: inline-block;
    margin-top: 10px;
}

.pager {
    margin-top: 20px;
}
</style>
