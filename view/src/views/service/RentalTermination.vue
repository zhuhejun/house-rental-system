<template>
    <div class="container">
        <div class="page-tip">
            退租相关操作已独立到本页处理。房东可在这里发起退租、提交协商结果，以及在管理员通过后上传退押凭证。
        </div>

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
            <el-table-column prop="tenantName" label="租客"></el-table-column>
            <el-table-column prop="depositAmount" label="原始押金" width="120"></el-table-column>
            <el-table-column label="状态" width="130">
                <template #default="scope">
                    <el-tag :type="statusType(scope.row.status)" size="mini">{{ statusText(scope.row.status) }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="320">
                <template #default="scope">
                    <div class="table-actions">
                        <span @click="showDetail(scope.row.id)">详情</span>
                        <span v-if="scope.row.status === 4" @click="openTerminateDialog(scope.row)">申请退租</span>
                        <span v-if="scope.row.status === 9" @click="openSettlementDialog(scope.row)">提交退租审核</span>
                        <span v-if="scope.row.status === 11" @click="openRefundDialog(scope.row)">上传退押凭证</span>
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
                    <div><strong>租客：</strong>{{ detail.tenantName }}</div>
                    <div><strong>当前状态：</strong>{{ statusText(detail.status) }}</div>
                    <div><strong>租期：</strong>{{ detail.startDate }} 至 {{ detail.endDate }}</div>
                    <div><strong>月租：</strong>{{ detail.monthlyRent }}</div>
                    <div><strong>原始押金：</strong>{{ detail.depositAmount }}</div>
                    <div v-if="detail.terminationReason"><strong>退租原因：</strong>{{ detail.terminationReason }}</div>
                    <div v-if="detail.terminationRefundAmount !== null && detail.terminationRefundAmount !== undefined">
                        <strong>协商退押金额：</strong>{{ detail.terminationRefundAmount }}
                    </div>
                    <div v-if="detail.terminationVoucherUrl"><strong>协商凭证：</strong><span class="link"
                            @click="openAttachment(detail.terminationVoucherUrl)">点击查看</span></div>
                    <div v-if="detail.terminationVoucherNote"><strong>协商备注：</strong>{{ detail.terminationVoucherNote }}</div>
                    <div v-if="detail.terminationAuditNote"><strong>管理员意见：</strong>{{ detail.terminationAuditNote }}</div>
                    <div v-if="detail.terminationRefundVoucherUrl"><strong>退押凭证：</strong><span class="link"
                            @click="openAttachment(detail.terminationRefundVoucherUrl)">点击查看</span></div>
                    <div v-if="detail.terminationRefundVoucherNote"><strong>退押说明：</strong>{{ detail.terminationRefundVoucherNote }}</div>
                </div>
                <StatusFlow :status-records="statusList" title="退租流转记录" :status-config="contractStatusConfig" />
            </div>
        </el-dialog>

        <el-dialog title="申请退租" :visible.sync="terminateVisible" width="34%" :close-on-click-modal="false">
            <div>
                <p>*退租原因</p>
                <el-input type="textarea" :rows="4" v-model="terminateForm.terminationReason"
                    placeholder="请填写退租原因，便于租客和管理员核对"></el-input>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="terminateVisible = false">取消</span>
                <span class="info-bt" @click="submitTerminate">提交申请</span>
            </span>
        </el-dialog>

        <el-dialog title="提交退租审核" :visible.sync="settlementVisible" width="38%" :close-on-click-modal="false">
            <div>
                <div class="calc-card" v-if="currentRow.id">
                    <div><strong>原始押金：</strong>{{ Number(currentRow.depositAmount || 0).toFixed(2) }}</div>
                    <div><strong>管理员驳回意见：</strong>{{ currentRow.terminationAuditNote || '暂无' }}</div>
                </div>
                <p>*退还押金金额</p>
                <el-input-number v-model="settlementForm.terminationRefundAmount" :min="0" :precision="2" :step="100"
                    style="width: 100%;"></el-input-number>
                <p>*协商凭证</p>
                <div class="upload-tip">支持上传 PDF / JPG / JPEG / PNG，建议上传协商记录或双方确认截图。</div>
                <el-upload action="http://localhost:21090/api/v1.0/house-rental-api/file/upload"
                    :show-file-list="false" :on-success="handleSettlementUpload" :before-upload="beforeAttachmentUpload"
                    accept=".pdf,.jpg,.jpeg,.png">
                    <span class="primary-bt">上传凭证</span>
                </el-upload>
                <div v-if="settlementForm.terminationVoucherUrl" class="uploaded-file"
                    @click="openAttachment(settlementForm.terminationVoucherUrl)">
                    已上传凭证，点击查看
                </div>
                <p>协商备注</p>
                <el-input type="textarea" :rows="3" v-model="settlementForm.terminationVoucherNote"
                    placeholder="填写与租客协商后的退押说明"></el-input>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="settlementVisible = false">取消</span>
                <span class="info-bt" @click="submitSettlement">提交审核</span>
            </span>
        </el-dialog>

        <el-dialog title="上传退押凭证" :visible.sync="refundVisible" width="38%" :close-on-click-modal="false">
            <div>
                <div class="calc-card" v-if="currentRow.id">
                    <div><strong>协商退押金额：</strong>{{ Number(currentRow.terminationRefundAmount || 0).toFixed(2) }}</div>
                    <div><strong>管理员状态：</strong>{{ statusText(currentRow.status) }}</div>
                </div>
                <div class="upload-tip">管理员已同意退租，请在完成退款后上传转账截图或退款回执。</div>
                <p>*退押凭证</p>
                <el-upload action="http://localhost:21090/api/v1.0/house-rental-api/file/upload"
                    :show-file-list="false" :on-success="handleRefundUpload" :before-upload="beforeAttachmentUpload"
                    accept=".pdf,.jpg,.jpeg,.png">
                    <span class="primary-bt">上传凭证</span>
                </el-upload>
                <div v-if="refundForm.terminationRefundVoucherUrl" class="uploaded-file"
                    @click="openAttachment(refundForm.terminationRefundVoucherUrl)">
                    已上传凭证，点击查看
                </div>
                <p>退押说明</p>
                <el-input type="textarea" :rows="3" v-model="refundForm.terminationRefundVoucherNote"
                    placeholder="补充退款时间、方式等说明"></el-input>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="refundVisible = false">取消</span>
                <span class="info-bt" @click="submitRefund">确认提交</span>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import AutoInput from "@/components/AutoInput.vue";
import Tab from "@/components/Tab.vue";
import StatusFlow from "@/components/StatusFlow.vue";

const TERMINATION_STATUSES = [4, 9, 10, 11, 12, 13];

export default {
    components: { AutoInput, Tab, StatusFlow },
    data() {
        return {
            statusTabs: [
                { label: '全部', value: 'null' },
                { label: '可申请退租', value: '4' },
                { label: '退租申请中', value: '9' },
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
            terminateVisible: false,
            settlementVisible: false,
            refundVisible: false,
            detail: {},
            statusList: [],
            currentRow: {},
            terminateForm: {
                id: null,
                terminationReason: '',
            },
            settlementForm: {
                id: null,
                terminationRefundAmount: 0,
                terminationVoucherUrl: '',
                terminationVoucherNote: '',
            },
            refundForm: {
                id: null,
                terminationRefundVoucherUrl: '',
                terminationRefundVoucherNote: '',
            },
            contractStatusConfig: {
                4: { text: "已生效", icon: "el-icon-success", color: "#67C23A", status: "success" },
                9: { text: "退租申请中", icon: "el-icon-warning-outline", color: "#E6A23C", status: "process" },
                10: { text: "待退租审核", icon: "el-icon-s-check", color: "#409EFF", status: "process" },
                11: { text: "待退押", icon: "el-icon-money", color: "#E6A23C", status: "process" },
                12: { text: "待审核退押", icon: "el-icon-s-check", color: "#409EFF", status: "process" },
                13: { text: "已退租", icon: "el-icon-circle-check", color: "#67C23A", status: "success" }
            }
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        statusText(status) {
            return (this.contractStatusConfig[status] && this.contractStatusConfig[status].text) || '未知状态';
        },
        statusType(status) {
            if (status === 4 || status === 13) return 'success';
            if ([9, 10, 11, 12].includes(status)) return 'warning';
            return 'info';
        },
        openAttachment(url) {
            const backendOrigin = new URL(this.$axios.defaults.baseURL).origin;
            const targetUrl = url.startsWith('http') ? url : `${backendOrigin}${url}`;
            window.open(targetUrl, '_blank');
        },
        beforeAttachmentUpload(file) {
            const fileName = (file.name || '').toLowerCase();
            const allowTypes = ['.pdf', '.jpg', '.jpeg', '.png'];
            const valid = allowTypes.some(type => fileName.endsWith(type));
            if (!valid) {
                this.$message.warning('附件仅支持 PDF、JPG、JPEG、PNG');
            }
            return valid;
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
                const { data } = await this.$axios.post('/rental-contract/listLandlord', { current: 1, size: 1000 });
                this.rawList = data || [];
                this.applyFilters();
            } catch (error) {
                this.$message.error(error.message || '加载退租列表失败');
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
        openTerminateDialog(row) {
            this.currentRow = row;
            this.terminateForm = {
                id: row.id,
                terminationReason: row.terminationReason || '',
            };
            this.terminateVisible = true;
        },
        async submitTerminate() {
            try {
                await this.$axios.put('/rental-contract/applyTerminate', this.terminateForm);
                this.$message.success('退租申请已提交');
                this.terminateVisible = false;
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message || '提交失败');
            }
        },
        openSettlementDialog(row) {
            this.currentRow = row;
            this.settlementForm = {
                id: row.id,
                terminationRefundAmount: row.terminationRefundAmount || row.depositAmount || 0,
                terminationVoucherUrl: row.terminationVoucherUrl || '',
                terminationVoucherNote: row.terminationVoucherNote || '',
            };
            this.settlementVisible = true;
        },
        handleSettlementUpload(res) {
            if (res.code === 200) {
                this.settlementForm.terminationVoucherUrl = res.data;
                this.$message.success('凭证上传成功');
            } else {
                this.$message.error(res.msg || '凭证上传失败');
            }
        },
        async submitSettlement() {
            try {
                await this.$axios.put('/rental-contract/submitTerminateSettlement', this.settlementForm);
                this.$message.success('退租结算已提交管理员审核');
                this.settlementVisible = false;
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message || '提交失败');
            }
        },
        openRefundDialog(row) {
            this.currentRow = row;
            this.refundForm = {
                id: row.id,
                terminationRefundVoucherUrl: row.terminationRefundVoucherUrl || '',
                terminationRefundVoucherNote: row.terminationRefundVoucherNote || '',
            };
            this.refundVisible = true;
        },
        handleRefundUpload(res) {
            if (res.code === 200) {
                this.refundForm.terminationRefundVoucherUrl = res.data;
                this.$message.success('凭证上传成功');
            } else {
                this.$message.error(res.msg || '凭证上传失败');
            }
        },
        async submitRefund() {
            try {
                await this.$axios.put('/rental-contract/submitTerminateRefund', this.refundForm);
                this.$message.success('退押凭证已提交');
                this.refundVisible = false;
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

.calc-card {
    margin-bottom: 12px;
    padding: 12px 14px;
    border-radius: 6px;
    background: #f5f7fa;
    color: #606266;
    display: grid;
    gap: 6px;
}

.detail-block {
    display: grid;
    gap: 10px;
    margin-bottom: 10px;
}

.uploaded-file,
.link {
    color: rgb(17, 139, 221);
    cursor: pointer;
    margin-top: 8px;
}

.upload-tip {
    margin-bottom: 8px;
    color: #909399;
    font-size: 12px;
}

.pager {
    margin-top: 20px;
}
</style>
