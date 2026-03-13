<template>
    <div class="container">
        <div class="top-header">

        </div>

        <div class="house-container">
            <div>
                <el-table :data="apiResult.data">
                    <el-table-column prop="title" label="标题">
                        <template #default="scope">
                            <div class="over-text">
                                {{ scope.row.title }}
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="viewNumber" :sortable="true" width="148" label="阅读量">
                        <template #default="scope">
                            <div>{{ scope.row.viewNumber }}次阅读</div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="saveNumber" :sortable="true" width="118" label="收藏量">
                        <template #default="scope">
                            <div>{{ scope.row.saveNumber }}人收藏</div>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="139" align="left">
                        <template #default="scope">
                            <div style="cursor: pointer;" @click="viewHouseNews(scope.row)">
                                <i class="el-icon-view"></i>
                                去阅读
                            </div>
                        </template>
                    </el-table-column>
                </el-table>
                <!-- 分页组件区域 -->
                <div class="pager">
                    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                        :current-page="flowIndexQueryDto.current" :page-sizes="[10, 20]"
                        :page-size="flowIndexQueryDto.size" layout="total, sizes, prev, pager, next, jumper"
                        :total="apiResult.total"></el-pagination>
                </div>
            </div>

        </div>
    </div>
</template>

<script>
import AutoInput from "@/components/AutoInput.vue";

export default {
    components: { AutoInput },
    data() {
        return {
            controlTextOperation: false,
            direction: 'rtl',
            dialogDeletedVisible: false,
            id: null,
            apiResult: {
                data: [],
                total: 0,
            },
            notice: {},
            flowIndexQueryDto: {
                current: 1,
                size: 10,
            },
            deletedItem: {},
            content: '',
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        viewHouseNews(houseNews) {
            window.open(`/house-news-detail?id=${houseNews.id}`, '_blank');
        },
        async fetchFreshData() {
            try {
                const { data, total } = await this.$axios.post('/flow-index/saveListHouseNews', this.flowIndexQueryDto);
                this.apiResult.data = data;
                this.apiResult.total = total;
            } catch (error) {
                this.$message.error(error.message);
            }
        },
        handleSizeChange(size) {
            this.flowIndexQueryDto.size = size;
            this.flowIndexQueryDto.current = 1;
            this.fetchFreshData();
        },
        handleCurrentChange(current) {
            this.flowIndexQueryDto.current = current;
            this.fetchFreshData();
        }
    },
};
</script>

<style scoped>
.actions {
    display: flex;
    gap: 20px;

    div {
        font-size: 14px;
        border: 1px solid rgb(193, 193, 193);
        color: rgb(78, 77, 77);
        cursor: pointer;
        padding: 4px 16px;

        &:hover {
            border: 1px solid rgb(139, 174, 236);
            color: rgb(139, 174, 236);
        }
    }
}

.container {
    padding: 20px;
    background-color: #fff;
}

.top-header {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 20px;
}

.nav-right {
    display: flex;
    align-items: center;
    gap: 10px;
}

.house-container {
    margin-bottom: 20px;
}

.house-item {
    display: flex;
    gap: 20px;
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;
}

.house-item img {
    width: 160px;
    height: 120px;
    border-radius: 4px;
    object-fit: cover;
}

.house-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.name {
    font-size: 18px;
    font-weight: 500;
    color: #333;
}

.summary {
    font-size: 14px;
    color: #666;
    line-height: 1.5;
    background-color: #fafafa;
    padding: 8px;
    border-radius: 4px;
}

.meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;
}

.stats {
    display: flex;
    gap: 15px;
    font-size: 12px;
    color: #888;
}

.actions {
    display: flex;
    gap: 10px;
}

.drawer-content {
    padding: 20px;
}

.cover-upload {
    display: flex;
    align-items: center;
    gap: 20px;
}

.cover-upload img {
    width: 200px;
    height: 140px;
    border-radius: 4px;
    object-fit: cover;
}

.uploader {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100px;
    height: 100px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    cursor: pointer;
    color: #999;
}

.uploader:hover {
    border-color: #409EFF;
}

.upload-tip {
    margin-top: 8px;
    font-size: 12px;
}

.pager {
    display: flex;
    justify-content: right;
    margin-top: 20px;
}


/* 默认隐藏操作按钮 */
.operate-buttons {
    opacity: 0;
    transition: opacity 0.3s;
    /* 添加过渡效果 */
    cursor: pointer;

    i {
        padding: 8px;
        border-radius: 6px;
        transition: all .5s ease;

        &:hover {
            background-color: rgb(236, 237, 238);
        }
    }

}

/* 行悬停时显示操作按钮 */
.el-table__body tr:hover .operate-buttons {
    opacity: 1;
}
</style>