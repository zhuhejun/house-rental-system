<template>
    <div class="payment-result">
        <div class="card">
            <div class="status-icon" :class="statusClass">
                <i :class="statusIcon"></i>
            </div>
            <h2>{{ statusTitle }}</h2>
            <p class="desc">{{ statusDesc }}</p>

            <div v-if="bill.id" class="detail-block">
                <div><strong>账单编号：</strong>{{ bill.billNo }}</div>
                <div><strong>合同编号：</strong>{{ bill.contractNo }}</div>
                <div><strong>房源：</strong>{{ bill.houseName }}</div>
                <div><strong>账单类型：</strong>{{ bill.billType === 1 ? '押金单' : '租金单' }}</div>
                <div><strong>应付金额：</strong>{{ bill.totalAmount }}</div>
                <div><strong>支付状态：</strong>{{ payStatusText(bill.payStatus) }}</div>
            </div>

            <div class="actions">
                <span class="primary-bt" @click="refreshStatus">刷新结果</span>
                <span class="info-bt" @click="$router.push('/my-rental-bill')">返回我的账单</span>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            bill: {},
            loading: false,
        };
    },
    computed: {
        statusClass() {
            if (this.bill.payStatus === 2) {
                return 'success';
            }
            if (this.bill.payStatus === 3) {
                return 'cancel';
            }
            return 'pending';
        },
        statusIcon() {
            if (this.bill.payStatus === 2) {
                return 'el-icon-success';
            }
            if (this.bill.payStatus === 3) {
                return 'el-icon-warning';
            }
            return 'el-icon-time';
        },
        statusTitle() {
            if (this.bill.payStatus === 2) {
                return '支付成功';
            }
            if (this.bill.payStatus === 3) {
                return '账单已取消';
            }
            return '支付处理中';
        },
        statusDesc() {
            if (this.bill.payStatus === 2) {
                return '系统已完成账单入账，你可以返回账单页查看明细。';
            }
            if (this.bill.payStatus === 3) {
                return '当前账单已取消，如有疑问请联系房东。';
            }
            return '支付宝异步通知可能稍有延迟，建议点击刷新结果确认最新状态。';
        }
    },
    created() {
        this.refreshStatus();
    },
    methods: {
        payStatusText(status) {
            const map = { 1: '待支付', 2: '已支付', 3: '已取消' };
            return map[status] || '未知';
        },
        async refreshStatus() {
            const { billId } = this.$route.query;
            if (!billId) {
                this.$message.warning('缺少账单编号');
                return;
            }
            this.loading = true;
            try {
                const { data } = await this.$axios.get(`/payment-order/queryBillStatus/${billId}`);
                this.bill = data || {};
            } catch (error) {
                this.$message.error(error.message || '查询支付结果失败');
            } finally {
                this.loading = false;
            }
        }
    }
}
</script>

<style scoped lang="scss">
.payment-result {
    min-height: 100vh;
    background: linear-gradient(180deg, #f7f9fc 0%, #eef3f9 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
    box-sizing: border-box;
}

.card {
    width: min(640px, 100%);
    background: #fff;
    border-radius: 18px;
    padding: 36px;
    box-shadow: 0 16px 40px rgba(20, 45, 90, 0.08);
    text-align: center;
}

.status-icon {
    width: 76px;
    height: 76px;
    border-radius: 50%;
    margin: 0 auto 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 34px;

    &.success {
        background: rgba(103, 194, 58, 0.15);
        color: #67c23a;
    }

    &.pending {
        background: rgba(64, 158, 255, 0.15);
        color: #409eff;
    }

    &.cancel {
        background: rgba(144, 147, 153, 0.15);
        color: #909399;
    }
}

.desc {
    color: #6b7280;
    margin-bottom: 26px;
}

.detail-block {
    text-align: left;
    display: grid;
    gap: 10px;
    background: #f8fafc;
    border-radius: 14px;
    padding: 20px;
    margin-bottom: 24px;
}

.actions {
    display: flex;
    justify-content: center;
    gap: 14px;
}
</style>
