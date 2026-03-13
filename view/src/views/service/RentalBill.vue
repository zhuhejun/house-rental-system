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
                <div class="primary-bt" @click="openCreateDialog">
                    <i class="el-icon-plus"></i>
                    创建账单
                </div>
                <AutoInput placeholder="搜索账单/合同/房源/租客" @listener="listener" />
            </div>
        </div>

        <el-table :data="apiResult.data">
            <el-table-column prop="billNo" label="账单编号" width="220"></el-table-column>
            <el-table-column prop="contractNo" label="合同编号" width="220"></el-table-column>
            <el-table-column prop="houseName" label="房源名"></el-table-column>
            <el-table-column prop="tenantName" label="租客" width="120"></el-table-column>
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
            <el-table-column label="操作" width="220">
                <template #default="scope">
                    <div class="table-actions">
                        <span @click="showDetail(scope.row.id)">详情</span>
                        <span v-if="scope.row.utilityPaymentMode === 1 && scope.row.utilityProofStatus === 2"
                            @click="openAuditDialog(scope.row)">审核凭证</span>
                    </div>
                </template>
            </el-table-column>
        </el-table>

        <div class="pager">
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="queryDto.current" :page-sizes="[10, 20]" :page-size="queryDto.size"
                layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
        </div>

        <el-dialog title="创建租金账单" :visible.sync="dialogVisible" width="40%" :close-on-click-modal="false">
            <div>
                <p>*所属合同</p>
                <el-select v-model="form.contractId" style="width: 100%;" placeholder="请选择已生效合同"
                    @change="handleContractChange">
                    <el-option v-for="item in contractOptions" :key="item.id"
                        :label="`${item.contractNo} / ${item.houseName} / ${item.tenantName}`" :value="item.id">
                    </el-option>
                </el-select>
                <div class="grid-two">
                    <div>
                        <p>*账期开始月份</p>
                        <el-date-picker v-model="form.billMonth" type="month" value-format="yyyy-MM"
                            placeholder="选择开始月份" style="width: 100%;" :picker-options="billMonthPickerOptions">
                        </el-date-picker>
                    </div>
                    <div>
                        <p>*到期日期</p>
                        <el-date-picker v-model="form.dueDate" type="date" value-format="yyyy-MM-dd"
                            placeholder="选择日期" style="width: 100%;"></el-date-picker>
                    </div>
                </div>
                <div class="grid-two">
                    <div>
                        <p>*本期租金</p>
                        <el-input-number v-model="form.rentAmount" :min="1" :precision="2" :step="100"
                            style="width: 100%;"></el-input-number>
                    </div>
                    <div>
                        <p>*水电方式</p>
                        <el-select v-model="form.utilityPaymentMode" style="width: 100%;" placeholder="请选择">
                            <el-option label="自行缴费" :value="1"></el-option>
                            <el-option label="房东结算" :value="2"></el-option>
                        </el-select>
                    </div>
                </div>
                <div v-if="form.utilityPaymentMode === 2" class="grid-two">
                    <div>
                        <p>水费</p>
                        <el-input-number v-model="form.waterAmount" :min="0" :precision="2" :step="10"
                            style="width: 100%;"></el-input-number>
                    </div>
                    <div>
                        <p>电费</p>
                        <el-input-number v-model="form.electricAmount" :min="0" :precision="2" :step="10"
                            style="width: 100%;"></el-input-number>
                    </div>
                </div>
                <div class="tip">
                    自行缴费模式下，租客只在线支付租金，水电费通过上传缴费凭证完成核验。
                </div>
                <div v-if="selectedContract" class="period-card">
                    <div><strong>合同月租金：</strong>{{ formatMoney(selectedContract.monthlyRent) }}</div>
                    <div><strong>当前付款周期：</strong>{{ selectedContract.payCycle }}</div>
                    <div><strong>本期账期：</strong>{{ billPeriodPreview || '请选择正确的开始月份' }}</div>
                    <div><strong>本期应收租金：</strong>{{ suggestedRentAmount }}</div>
                    <div class="period-formula">
                        计算方式：{{ formatMoney(selectedContract.monthlyRent) }} × {{ getPayMonths(selectedContract) }}个月
                    </div>
                </div>
                <p>备注</p>
                <el-input type="textarea" :rows="3" v-model="form.remark" placeholder="补充账单说明"></el-input>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="dialogVisible = false">取消</span>
                <span class="info-bt" @click="saveBill">确认创建</span>
            </span>
        </el-dialog>

        <el-dialog title="账单详情" :visible.sync="detailVisible" width="44%">
            <div v-if="detail.id" class="detail-block">
                <div><strong>账单编号：</strong>{{ detail.billNo }}</div>
                <div><strong>合同编号：</strong>{{ detail.contractNo }}</div>
                <div><strong>房源：</strong>{{ detail.houseName }}</div>
                <div><strong>租客：</strong>{{ detail.tenantName }}</div>
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

        <el-dialog title="审核缴费凭证" :visible.sync="auditVisible" width="30%" :close-on-click-modal="false">
            <div>
                <p>*审核结果</p>
                <el-radio-group v-model="auditForm.utilityProofStatus">
                    <el-radio :label="3">审核通过</el-radio>
                    <el-radio :label="4">驳回凭证</el-radio>
                </el-radio-group>
                <p>审核备注</p>
                <el-input type="textarea" :rows="3" v-model="auditForm.utilityProofNote"
                    placeholder="填写审核说明"></el-input>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="auditVisible = false">取消</span>
                <span class="info-bt" @click="submitAudit">确认审核</span>
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
            dialogVisible: false,
            detailVisible: false,
            auditVisible: false,
            apiResult: { data: [], total: 0 },
            queryDto: {
                current: 1,
                size: 10,
                keyword: '',
                payStatus: null,
            },
            contractOptions: [],
            form: this.getDefaultForm(),
            detail: {},
            selectedContract: null,
            existingBillPeriods: [],
            auditForm: {
                id: null,
                utilityProofStatus: 3,
                utilityProofNote: '',
            }
        };
    },
    computed: {
        billMonthPickerOptions() {
            return {
                disabledDate: this.disableBillMonth,
            };
        },
        billPeriodPreview() {
            if (!this.selectedContract || !this.form.billMonth) {
                return '';
            }
            return this.formatBillPeriod(this.form.billMonth, this.getPayMonths(this.selectedContract));
        },
        suggestedRentAmount() {
            if (!this.selectedContract) {
                return '0.00';
            }
            const amount = Number(this.selectedContract.monthlyRent || 0) * this.getPayMonths(this.selectedContract);
            return amount.toFixed(2);
        }
    },
    created() {
        this.refresh();
    },
    methods: {
        getDefaultForm() {
            return {
                contractId: null,
                billMonth: '',
                dueDate: '',
                rentAmount: 1000,
                utilityPaymentMode: 2,
                waterAmount: 0,
                electricAmount: 0,
                remark: '',
            };
        },
        formatMoney(value) {
            return Number(value || 0).toFixed(2);
        },
        getPayMonths(contract) {
            const type = Number((contract || {}).depositMethodId);
            const map = { 1: 1, 2: 3, 3: 1, 4: 3 };
            return map[type] || 1;
        },
        monthToNumber(month) {
            if (!month) {
                return null;
            }
            const [year, monthValue] = month.split('-').map(Number);
            return year * 12 + monthValue - 1;
        },
        addMonths(month, offset) {
            const monthNumber = this.monthToNumber(month);
            if (monthNumber === null) {
                return '';
            }
            const target = monthNumber + offset;
            const year = Math.floor(target / 12);
            const monthValue = (target % 12) + 1;
            return `${year}-${String(monthValue).padStart(2, '0')}`;
        },
        formatBillPeriod(startMonth, payMonths) {
            if (!startMonth) {
                return '';
            }
            if (!payMonths || payMonths <= 1) {
                return startMonth;
            }
            return `${startMonth}至${this.addMonths(startMonth, payMonths - 1)}`;
        },
        disableBillMonth(date) {
            if (!this.selectedContract) {
                return false;
            }
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const candidateMonth = `${year}-${month}`;
            const contractStartMonth = (this.selectedContract.startDate || '').slice(0, 7);
            const contractEndMonth = (this.selectedContract.endDate || '').slice(0, 7);
            const payMonths = this.getPayMonths(this.selectedContract);
            const candidateNumber = this.monthToNumber(candidateMonth);
            const contractStartNumber = this.monthToNumber(contractStartMonth);
            const contractEndNumber = this.monthToNumber(contractEndMonth);
            if (candidateNumber === null || contractStartNumber === null || contractEndNumber === null) {
                return false;
            }
            if (candidateNumber < contractStartNumber) {
                return true;
            }
            if ((candidateNumber - contractStartNumber) % payMonths !== 0) {
                return true;
            }
            if (this.monthToNumber(this.addMonths(candidateMonth, payMonths - 1)) > contractEndNumber) {
                return true;
            }
            return this.existingBillPeriods.includes(this.formatBillPeriod(candidateMonth, payMonths));
        },
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
        async refresh() {
            await Promise.all([this.fetchFreshData(), this.fetchContractOptions()]);
        },
        async fetchFreshData() {
            try {
                const { data, total } = await this.$axios.post('/rental-bill/listLandlord', this.queryDto);
                this.apiResult.data = data;
                this.apiResult.total = total;
            } catch (error) {
                this.$message.error(error.message || '加载账单失败');
            }
        },
        async fetchContractOptions() {
            try {
                const { data } = await this.$axios.post('/rental-contract/listLandlord', {
                    current: 1,
                    size: 1000,
                    status: 4,
                });
                this.contractOptions = data || [];
            } catch (error) {
                console.error('加载合同失败:', error);
            }
        },
        openCreateDialog() {
            this.form = this.getDefaultForm();
            this.selectedContract = null;
            this.existingBillPeriods = [];
            this.dialogVisible = true;
        },
        async handleContractChange(contractId) {
            const target = this.contractOptions.find(item => item.id === contractId);
            if (!target) {
                return;
            }
            this.selectedContract = target;
            const payMonths = this.getPayMonths(target);
            this.form.rentAmount = Number(target.monthlyRent || 0) * payMonths;
            this.form.utilityPaymentMode = target.utilityPaymentMode || 2;
            await this.fetchExistingPeriods(contractId);
            this.form.billMonth = this.findNextAvailableMonth(target);
        },
        async fetchExistingPeriods(contractId) {
            try {
                const { data } = await this.$axios.post('/rental-bill/listLandlord', {
                    current: 1,
                    size: 1000,
                    contractId,
                    billType: 2,
                });
                this.existingBillPeriods = (data || []).map(item => item.billMonth).filter(Boolean);
            } catch (error) {
                console.error('加载账期失败:', error);
                this.existingBillPeriods = [];
            }
        },
        findNextAvailableMonth(contract) {
            const contractStartMonth = (contract.startDate || '').slice(0, 7);
            const contractEndMonth = (contract.endDate || '').slice(0, 7);
            const payMonths = this.getPayMonths(contract);
            const startNumber = this.monthToNumber(contractStartMonth);
            const endNumber = this.monthToNumber(contractEndMonth);
            if (startNumber === null || endNumber === null) {
                return '';
            }
            for (let current = startNumber; current <= endNumber; current += payMonths) {
                const year = Math.floor(current / 12);
                const monthValue = (current % 12) + 1;
                const month = `${year}-${String(monthValue).padStart(2, '0')}`;
                const period = this.formatBillPeriod(month, payMonths);
                if (!this.existingBillPeriods.includes(period)
                    && this.monthToNumber(this.addMonths(month, payMonths - 1)) <= endNumber) {
                    return month;
                }
            }
            return '';
        },
        async saveBill() {
            try {
                await this.$axios.post('/rental-bill/save', this.form);
                this.$message.success('账单创建成功');
                this.dialogVisible = false;
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message || '创建失败');
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
        openAuditDialog(row) {
            this.auditForm = {
                id: row.id,
                utilityProofStatus: 3,
                utilityProofNote: '',
            };
            this.auditVisible = true;
        },
        async submitAudit() {
            try {
                await this.$axios.put('/rental-bill/auditUtilityProof', this.auditForm);
                this.$message.success('审核完成');
                this.auditVisible = false;
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message || '审核失败');
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

.tip {
    margin: 12px 0;
    color: #909399;
    font-size: 12px;
}

.period-card {
    margin: 12px 0;
    padding: 12px 14px;
    border-radius: 6px;
    background: #f5f7fa;
    color: #606266;
    display: grid;
    gap: 6px;
}

.period-formula {
    color: #909399;
    font-size: 12px;
}

.detail-block {
    display: grid;
    gap: 10px;
}

.link {
    color: rgb(17, 139, 221);
    cursor: pointer;
}

.pager {
    margin-top: 20px;
}
</style>
