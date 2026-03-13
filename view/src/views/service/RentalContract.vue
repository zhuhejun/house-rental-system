<template>
    <div class="container">
        <div class="top-header">
            <div class="nav-left">
                <Tab :buttons="[
                    { label: '全部', value: 'null' },
                    { label: '待管理员审核', value: '1' },
                    { label: '待租客确认', value: '2' },
                    { label: '已生效', value: '3' },
                    { label: '已驳回', value: '4' },
                    { label: '已拒绝', value: '5' },
                    { label: '已取消', value: '6' },
                    { label: '已到期', value: '7' }
                ]" initialActive="null" @change="handleChange" />
            </div>
            <div class="nav-right">
                <div class="primary-bt" @click="dialogVisible = true">
                    <i class="el-icon-plus"></i>
                    发起合同
                </div>
                <AutoInput placeholder="搜索合同/房源/租客" @listener="listener" />
            </div>
        </div>

        <el-table :data="apiResult.data">
            <el-table-column prop="contractNo" label="合同编号" width="220"></el-table-column>
            <el-table-column prop="title" label="合同标题"></el-table-column>
            <el-table-column prop="houseName" label="房源名"></el-table-column>
            <el-table-column prop="tenantName" label="租客"></el-table-column>
            <el-table-column label="租期" width="220">
                <template #default="scope">
                    {{ scope.row.startDate }} 至 {{ scope.row.endDate }}
                </template>
            </el-table-column>
            <el-table-column prop="monthlyRent" label="月租" width="120"></el-table-column>
            <el-table-column label="状态" width="140">
                <template #default="scope">
                    <el-tag :type="statusType(scope.row.status)" size="mini">{{ statusText(scope.row.status) }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发起时间" width="180"></el-table-column>
            <el-table-column label="操作" width="180">
                <template #default="scope">
                    <div class="table-actions">
                        <span @click="showDetail(scope.row.id)">详情</span>
                        <span v-if="[1, 2, 3].includes(scope.row.status)" @click="cancelContract(scope.row)">取消</span>
                    </div>
                </template>
            </el-table-column>
        </el-table>

        <div class="pager">
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="queryDto.current" :page-sizes="[10, 20]" :page-size="queryDto.size"
                layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
        </div>

        <el-dialog title="发起租赁合同" :visible.sync="dialogVisible" width="42%" :close-on-click-modal="false">
            <div>
                <p>*关联预约</p>
                <el-select v-model="form.houseOrderInfoId" style="width: 100%;" placeholder="请选择已完成的预约记录">
                    <el-option v-for="item in orderOptions" :key="item.id"
                        :label="`${item.houseName} / ${item.username} / ${item.orderDate}`" :value="item.id">
                    </el-option>
                </el-select>
                <p>*合同标题</p>
                <el-input v-model="form.title" placeholder="例如：海螺租房房屋租赁合同"></el-input>
                <div class="grid-two">
                    <div>
                        <p>*起租日期</p>
                        <el-date-picker v-model="form.startDate" type="date" value-format="yyyy-MM-dd"
                            placeholder="选择日期" style="width: 100%;"></el-date-picker>
                    </div>
                    <div>
                        <p>*到期日期</p>
                        <el-date-picker v-model="form.endDate" type="date" value-format="yyyy-MM-dd"
                            placeholder="选择日期" style="width: 100%;"></el-date-picker>
                    </div>
                </div>
                <div class="grid-two">
                    <div>
                        <p>*月租金</p>
                        <el-input-number v-model="form.monthlyRent" :min="1" :precision="2" :step="100"
                            style="width: 100%;"></el-input-number>
                    </div>
                    <div>
                        <p>*押金</p>
                        <el-input-number v-model="form.depositAmount" :min="0" :precision="2" :step="100"
                            style="width: 100%;"></el-input-number>
                    </div>
                </div>
                <p>*付款方式</p>
                <el-input v-model="form.payCycle" placeholder="例如：押一付三"></el-input>
                <p>*水电费支付方式</p>
                <el-select v-model="form.utilityPaymentMode" style="width: 100%;" placeholder="请选择">
                    <el-option label="自行缴费" :value="1"></el-option>
                    <el-option label="房东结算" :value="2"></el-option>
                </el-select>
                <p>合同内容</p>
                <el-input type="textarea" :rows="4" v-model="form.contractContent" placeholder="补充合同条款"></el-input>
                <p>合同附件</p>
                <div class="upload-tip">支持上传 `PDF / JPG / JPEG / PNG`，建议优先上传 PDF 便于预览。</div>
                <el-upload action="http://localhost:21090/api/v1.0/house-rental-api/file/upload"
                    :show-file-list="false" :on-success="handleUploadSuccess" :before-upload="beforeAttachmentUpload"
                    accept=".pdf,.jpg,.jpeg,.png">
                    <span class="primary-bt">上传附件</span>
                </el-upload>
                <div v-if="form.attachmentUrl" class="uploaded-file" @click="openAttachment(form.attachmentUrl)">
                    已上传附件，点击查看
                </div>
            </div>
            <span slot="footer" class="dialog-footer">
                <span class="primary-bt" @click="dialogVisible = false">取消</span>
                <span class="info-bt" @click="saveContract">确认发起</span>
            </span>
        </el-dialog>

        <el-dialog title="合同详情" :visible.sync="detailVisible" width="46%">
            <div v-if="detail.id">
                <div class="detail-block">
                    <div><strong>合同编号：</strong>{{ detail.contractNo }}</div>
                    <div><strong>合同标题：</strong>{{ detail.title }}</div>
                    <div><strong>房源：</strong>{{ detail.houseName }}</div>
                    <div><strong>租客：</strong>{{ detail.tenantName }}</div>
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
            dialogVisible: false,
            detailVisible: false,
            apiResult: {
                data: [],
                total: 0,
            },
            queryDto: {
                current: 1,
                size: 10,
                keyword: '',
                status: null,
            },
            form: this.getDefaultForm(),
            orderOptions: [],
            detail: {},
            statusList: [],
            contractStatusConfig: {
                1: { text: "待管理员审核", icon: "el-icon-s-check", color: "#E6A23C", status: "process" },
                2: { text: "待租客确认", icon: "el-icon-time", color: "#409EFF", status: "process" },
                3: { text: "已生效", icon: "el-icon-success", color: "#67C23A", status: "success" },
                4: { text: "已驳回", icon: "el-icon-close", color: "#F56C6C", status: "error" },
                5: { text: "已拒绝", icon: "el-icon-close", color: "#F56C6C", status: "error" },
                6: { text: "已取消", icon: "el-icon-warning", color: "#909399", status: "error" },
                7: { text: "已到期", icon: "el-icon-finished", color: "#909399", status: "success" }
            }
        };
    },
    created() {
        this.refresh();
    },
    methods: {
        getDefaultForm() {
            return {
                houseOrderInfoId: null,
                title: '',
                startDate: '',
                endDate: '',
                monthlyRent: 1000,
                depositAmount: 0,
                payCycle: '',
                utilityPaymentMode: 2,
                contractContent: '',
                attachmentUrl: '',
            };
        },
        utilityModeText(type) {
            return type === 1 ? '自行缴费' : '房东结算';
        },
        statusText(status) {
            return (this.contractStatusConfig[status] && this.contractStatusConfig[status].text) || '未知状态';
        },
        statusType(status) {
            if (status === 3) return 'success';
            if (status === 1 || status === 2) return 'warning';
            if (status === 4 || status === 5) return 'danger';
            if (status === 6) return 'info';
            return 'info';
        },
        async refresh() {
            await Promise.all([
                this.fetchFreshData(),
                this.fetchAvailableOrders()
            ]);
        },
        async fetchFreshData() {
            try {
                const { data, total } = await this.$axios.post('/rental-contract/listLandlord', this.queryDto);
                this.apiResult.data = data;
                this.apiResult.total = total;
            } catch (error) {
                this.$message.error(error.message || '加载合同失败');
            }
        },
        async fetchAvailableOrders() {
            try {
                const [orderRes, contractRes] = await Promise.all([
                    this.$axios.post('/house-order-info/listLandlord', { current: 1, size: 1000, orderStatus: 5 }),
                    this.$axios.post('/rental-contract/listLandlord', { current: 1, size: 1000 })
                ]);
                const usedOrderIds = new Set((contractRes.data || []).map(item => item.houseOrderInfoId));
                this.orderOptions = (orderRes.data || []).filter(item => !usedOrderIds.has(item.id));
            } catch (error) {
                console.error('加载可签约预约失败:', error);
            }
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
        handleUploadSuccess(res) {
            if (res.code === 200) {
                this.form.attachmentUrl = res.data;
                this.$message.success('附件上传成功');
            } else {
                this.$message.error(res.msg || '附件上传失败');
            }
        },
        beforeAttachmentUpload(file) {
            const fileName = (file.name || '').toLowerCase();
            const allowTypes = ['.pdf', '.jpg', '.jpeg', '.png'];
            const valid = allowTypes.some(type => fileName.endsWith(type));
            if (!valid) {
                this.$message.warning('合同附件仅支持 PDF、JPG、JPEG、PNG');
            }
            return valid;
        },
        openAttachment(url) {
            const backendOrigin = new URL(this.$axios.defaults.baseURL).origin;
            const targetUrl = url.startsWith('http') ? url : `${backendOrigin}${url}`;
            window.open(targetUrl, '_blank');
        },
        async saveContract() {
            try {
                await this.$axios.post('/rental-contract/save', this.form);
                this.$message.success('合同发起成功');
                this.dialogVisible = false;
                this.form = this.getDefaultForm();
                this.refresh();
            } catch (error) {
                this.$message.error(error.message || '发起失败');
            }
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
        async cancelContract(row) {
            try {
                const { value } = await this.$prompt('请输入取消原因（可为空）', '取消合同', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    inputPlaceholder: '取消原因'
                });
                await this.$axios.put('/rental-contract/cancel', { id: row.id, cancelReason: value || '' });
                this.$message.success('合同已取消');
                this.refresh();
            } catch (error) {
                if (error === 'cancel') {
                    return;
                }
                this.$message.error(error.message || '取消失败');
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

.detail-block {
    display: grid;
    gap: 10px;
    margin-bottom: 10px;
}

.pager {
    margin-top: 20px;
}
</style>
