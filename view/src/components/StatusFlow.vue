<template>
    <div class="status-flow-container">
        <div class="status-title">{{ title }}</div>
        <div v-if="normalizedSteps.length === 0">
            <el-empty description="暂无状态流转路径"></el-empty>
        </div>
        <el-steps direction="vertical" :active="activeStep" :process-status="processStatus" :space="100">
            <el-step v-for="(step, index) in normalizedSteps" :key="'step-' + index" :title="getStatusText(step.status)"
                :description="formatTime(step.createTime)" :icon="getStatusIcon(step.status)"
                :status="getStepStatus(step.status)"></el-step>
        </el-steps>
    </div>
</template>

<script>
export default {
    props: {
        statusRecords: {
            type: Array,
            default: () => [],
            validator: value => {
                return value.every(record =>
                    record.hasOwnProperty('originStatus') &&
                    record.hasOwnProperty('newId') &&
                    record.hasOwnProperty('createTime')
                )
            }
        },
        title: {
            type: String,
            default: '预约状态流转记录'
        },
        statusConfig: {
            type: Object,
            default: null
        }
    },
    data() {
        return {
            defaultStatusConfig: {
                1: { text: "预约中", icon: "el-icon-time", color: "#409EFF", status: "process" },
                2: { text: "已预约", icon: "el-icon-success", color: "#67C23A", status: "success" },
                3: { text: "预约失败", icon: "el-icon-warning", color: "#E6A23C", status: "error" },
                4: { text: "已取消", icon: "el-icon-circle-close", color: "#F56C6C", status: "error" },
                5: { text: "已完成", icon: "el-icon-finished", color: "#909399", status: "success" }
            }
        }
    },
    computed: {
        activeStep() {
            return this.normalizedSteps.length ? this.normalizedSteps.length - 1 : 0
        },
        processStatus() {
            if (!this.normalizedSteps.length) return 'wait'
            const lastStatus = this.normalizedSteps[this.normalizedSteps.length - 1].status
            return this.getStepStatus(lastStatus)
        },
        currentStatusConfig() {
            return this.statusConfig || this.defaultStatusConfig
        },
        normalizedSteps() {
            if (!this.statusRecords.length) return []
            const steps = []

            this.statusRecords.forEach(record => {
                const currentTime = record.createTime
                if (!steps.length || steps[steps.length - 1].status !== record.originStatus) {
                    steps.push({
                        status: record.originStatus,
                        createTime: currentTime
                    })
                }
                if (steps[steps.length - 1].status !== record.newId) {
                    steps.push({
                        status: record.newId,
                        createTime: currentTime
                    })
                } else {
                    steps[steps.length - 1].createTime = currentTime
                }
            })

            return steps
        }
    },
    methods: {
        formatTime(time) {
            if (!time) return '--'
            try {
                const date = new Date(time)
                if (isNaN(date.getTime())) return '无效日期'

                const pad = num => num.toString().padStart(2, '0')
                return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
            } catch (e) {
                console.error('日期格式错误:', e)
                return '无效日期'
            }
        },

        getStatusText(status) {
            return (this.currentStatusConfig[status] && this.currentStatusConfig[status].text) || "未知状态"
        },

        getStatusIcon(status) {
            return (this.currentStatusConfig[status] && this.currentStatusConfig[status].icon) || "el-icon-info"
        },

        getStepStatus(status) {
            return (this.currentStatusConfig[status] && this.currentStatusConfig[status].status) || "wait"
        }
    }
}
</script>

<style scoped lang="scss">
.status-flow-container {
    padding: 20px 0;
    background: #fff;
    border-radius: 8px;
    // box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;

    .status-title {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 20px;
        color: #333;
        padding-left: 10px;
        border-left: 4px solid #409EFF;
    }
    ::v-deep {
        .el-step {
            &__head {
                &.is-process {
                    color: #409EFF;
                    border-color: #409EFF;

                    .el-step__icon {
                        background: rgba(64, 158, 255, 0.1);
                    }
                }

                &.is-success {
                    color: #67C23A;
                    border-color: #67C23A;

                    .el-step__icon {
                        background: rgba(103, 194, 58, 0.1);
                    }
                }

                &.is-error {
                    color: #F56C6C;
                    border-color: #F56C6C;

                    .el-step__icon {
                        background: rgba(245, 108, 108, 0.1);
                    }
                }

                &.is-wait {
                    color: #909399;
                    border-color: #909399;
                }
            }

            &__title {
                font-size: 14px;
                font-weight: bold;

                &.is-process {
                    color: #409EFF;
                }

                &.is-success {
                    color: #67C23A;
                }

                &.is-error {
                    color: #F56C6C;
                }

                &.is-wait {
                    color: #909399;
                }
            }

            &__description {
                font-size: 12px;
                color: #999;
            }

            &__line {
                left: 11px !important;
                background: linear-gradient(to bottom,
                        rgba(64, 158, 255, 0.3),
                        rgba(103, 194, 58, 0.3)) !important;
            }
        }
    }
}
</style>
