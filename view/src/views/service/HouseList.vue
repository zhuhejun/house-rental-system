<template>
    <div class="house-management-container">
        <!-- 顶部标题和操作栏 -->
        <div class="management-header">
            <h2 class="page-title">我发布的房源</h2>
            <div class="action-bar">
                <div class="filter-section">
                    <Tab :buttons="[
                        { label: '全部', value: 'null' },
                        { label: '待租', value: '1' },
                        { label: '下架', value: '2' },
                        { label: '已出租', value: '3' }
                    ]" initialActive="null" @change="handleChange" />
                </div>
                <div class="operation-section">
                    <div class="primary-bt" @click="addHouse">
                        <i class="el-icon-plus"></i>
                        新增房屋
                    </div>
                    <AutoInput placeholder="搜索房源名称" @listener="listener" class="search-input" />
                </div>
            </div>
        </div>

        <!-- 房源列表 -->
        <div class="house-list">
            <div v-for="item in apiResult.data" :key="item.id" class="house-card"
                :class="{ 'status-available': item.status === 1, 'status-offline': item.status === 2, 'status-rented': item.status === 3 }">
                <div class="house-image">
                    <img :src="item.cover" alt="房源封面">
                    <div class="status-badge">
                        {{ statusText(item.status) }}
                    </div>
                </div>

                <div class="house-info">
                    <div class="info-header">
                        <h3 class="house-name">{{ item.name }}</h3>
                        <div class="create-time">{{ formatDate(item.createTime) }}</div>
                    </div>

                    <div class="info-meta">
                        <div class="meta-item">
                            <i class="el-icon-location-information"></i>
                            {{ item.cityAreaName }} · {{ item.communityName }}
                        </div>
                        <div class="meta-item">
                            <i class="el-icon-house"></i>
                            {{ item.sizeNumber }}㎡ · {{ item.directionName }}
                        </div>
                        <div class="meta-item">
                            <i class="el-icon-setting"></i>
                            {{ item.fitmentStatusName }} · {{ item.depositMethodName }}
                        </div>
                    </div>

                    <div class="info-footer">
                        <div class="rent-price">¥{{ item.rent }}</div>
                        <div class="action-buttons">
                            <div v-if="item.status !== 3" @click="houseStatusDeal(item.id)">
                                <i :class="item.status === 1 ? 'el-icon-bottom' : 'el-icon-top'"></i>
                                {{ item.status === 1 ? '下架房源' : '上架房源' }}
                            </div>
                            <div @click="updateHouseInfo(item.id)">
                                <i class="el-icon-edit"></i>
                                修改
                            </div>
                            <div @click="deletedHouse(item)">
                                <i class="el-icon-delete"></i>
                                删除
                            </div>
                            <!-- <el-button size="mini" @click="houseStatusDeal(item.id)"
                                :type="item.status === 1 ? 'warning' : 'success'">
                                {{ item.status === 1 ? '下架房源' : '上架房源' }}
                            </el-button>
                            <el-button size="mini" @click="updateHouseInfo(item.id)" icon="el-icon-edit">
                                编辑
                            </el-button>
                            <el-button size="mini" type="danger" @click="deletedHouse(item)" icon="el-icon-delete">
                                删除
                            </el-button> -->
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-container">
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="houseQueryDto.current" :page-sizes="[10, 20, 50]" :page-size="houseQueryDto.size"
                layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total" />
        </div>

        <!-- 删除确认对话框 -->
        <el-dialog title="删除确认" :visible.sync="dialogDeletedVisible" width="400px" center>
            <div class="delete-dialog-content">
                <i class="el-icon-warning" style="color:#F56C6C;font-size:24px;"></i>
                <p>确定删除房源 <span class="highlight">{{ deletedItem.name }}</span> 吗？</p>
            </div>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogDeletedVisible = false">取消</el-button>
                <el-button type="primary" @click="confirmDeleted" :loading="deleting">确定</el-button>
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
            dialogDeletedVisible: false,
            deleting: false,
            apiResult: {
                data: [],
                total: 0
            },
            houseQueryDto: {
                current: 1,
                size: 10,
                status: null,
                name: ''
            },
            deletedItem: {}
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        statusText(status) {
            if (status === 1) return '待租';
            if (status === 2) return '下架';
            if (status === 3) return '已出租';
            return '未知状态';
        },
        formatDate(date) {
            if (!date) return '';
            return new Date(date).toLocaleDateString();
        },
        deletedHouse(item) {
            this.deletedItem = item;
            this.dialogDeletedVisible = true;
        },
        async confirmDeleted() {
            this.deleting = true;
            try {
                await this.$axios.delete(`/house/${this.deletedItem.id}`);
                this.$message.success('删除成功');
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message);
            } finally {
                this.dialogDeletedVisible = false;
                this.deleting = false;
            }
        },
        updateHouseInfo(id) {
            this.$router.push(`/service-center/update-house?houseId=${id}`);
        },
        async houseStatusDeal(id) {
            try {
                const { message } = await this.$axios.put(`/house/houseStatusDeal/${id}`);
                this.$message.success(message);
                this.fetchFreshData();
            } catch (error) {
                this.$message.error(error.message);
            }
        },
        addHouse() {
            this.$router.push('/service-center/post-house');
        },
        handleChange(val) {
            this.houseQueryDto.status = val.value === 'null' ? null : Number(val.value);
            this.fetchFreshData();
        },
        listener(text) {
            this.houseQueryDto.name = text;
            this.fetchFreshData();
        },
        async fetchFreshData() {
            try {
                const { data, total } = await this.$axios.post('/house/landlordHouseList', this.houseQueryDto);
                this.apiResult.data = data;
                this.apiResult.total = total;
            } catch (error) {
                this.$message.error('加载房源数据失败');
            }
        },
        handleSizeChange(size) {
            this.houseQueryDto.size = size;
            this.fetchFreshData();
        },
        handleCurrentChange(current) {
            this.houseQueryDto.current = current;
            this.fetchFreshData();
        }
    }
};
</script>

<style scoped lang="scss">
.house-management-container {
    padding: 24px;
    background-color: #fff;
    // border-radius: 8px;
    // box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.management-header {
    margin-bottom: 24px;
    display: flex;
    flex-direction: column;
    gap: 16px;

    .page-title {
        font-size: 20px;
        font-weight: 600;
        color: #303133;
        margin: 0;
    }

    .action-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 16px;

        .operation-section {
            display: flex;
            gap: 12px;
            align-items: center;

            .add-button {
                padding: 10px 16px;
            }

            .search-input {
                width: 240px;
            }
        }
    }
}

.house-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.house-card {
    display: flex;
    gap: 16px;
    padding: 16px;
    border-radius: 8px;
    //   border: 1px solid #EBEEF5;
    transition: all 0.3s;

    // &:hover {
    //     box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    //     transform: translateY(-2px);
    // }

    //   &.status-available {
    //     border-left: 4px solid #67C23A;
    //   }

    &.status-offline {
        background-color: rgb(248,248,248);
    }
}

.house-image {
    position: relative;
    width: 200px;
    height: 140px;
    flex-shrink: 0;
    border-radius: 4px;
    overflow: hidden;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .status-badge {
        position: absolute;
        top: 8px;
        left: 8px;
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
        background-color: rgba(0, 0, 0, 0.6);
        color: white;
    }
}

.house-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;

    .info-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .house-name {
            font-size: 18px;
            font-weight: 600;
            margin: 0;
            color: #303133;
        }

        .create-time {
            font-size: 20px;
            color: #909399;
        }
    }

    .info-meta {
        display: flex;
        flex-direction: column;
        gap: 6px;
        margin: 8px 0;

        .meta-item {
            font-size: 14px;
            color: #606266;

            i {
                margin-right: 6px;
                color: #909399;
            }
        }
    }

    .info-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: auto;

        .rent-price {
            font-size: 20px;
            font-weight: 700;
            color: #F56C6C;
        }

        .action-buttons {
            display: flex;
            gap: 16px;

            div {
                color: #4c4c4d;
                font-size: 16px;
                cursor: pointer;

                &:hover {
                    color: #8e8eca;
                }
            }
        }
    }
}

.pagination-container {
    margin-top: 24px;
    display: flex;
    justify-content: right;
}

.delete-dialog-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    text-align: center;

    p {
        margin: 0;
        font-size: 16px;
    }

    .highlight {
        color: #F56C6C;
        font-weight: 600;
    }
}

@media (max-width: 768px) {
    .house-card {
        flex-direction: column;
    }

    .house-image {
        width: 100%;
        height: 180px;
    }

    .action-bar {
        flex-direction: column;
        align-items: flex-start !important;

        .operation-section {
            width: 100%;

            .search-input {
                flex: 1;
            }
        }
    }
}
</style>
