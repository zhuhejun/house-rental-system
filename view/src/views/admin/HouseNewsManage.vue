<template>
    <div class="container">
        <div class="top-header">
            <div class="nav-right">
                <AutoInput placeholder="资讯标题" @listener="listener" />
                <div class="primary-bt" @click="saveHouseNews">
                    <i class="el-icon-plus"></i>
                    新增房屋资讯
                </div>
            </div>
        </div>

        <div class="house-container">
            <div class="house-item" v-for="item in apiResult.data" :key="item.id">
                <img :src="item.cover" alt="封面图片">
                <div class="house-content">
                    <div class="name">{{ item.title }}</div>
                    <div class="summary">{{ item.summary }}</div>
                    <div class="meta">
                        <div class="stats">
                            <span>收藏量 {{ item.viewNumber }}</span>
                            <span>浏览量 {{ item.saveNumber }}</span>
                            <span>{{ item.createTime }}</span>
                        </div>
                        <div class="actions">
                            <div @click="houseNewsUpdate(item)"><i class="el-icon-edit"></i>修改</div>
                            <div @click="deletedHouse(item)"><i class="el-icon-delete"></i>删除</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="pager">
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="houseNewsQueryDto.current" :page-sizes="[10, 20]" :page-size="houseNewsQueryDto.size"
                layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total">
            </el-pagination>
        </div>

        <el-dialog title="删除房屋资讯信息" :visible.sync="dialogDeletedVisible" width="400px">
            <span>确定删除房屋资讯信息【{{ deletedItem.title }}】？</span>
            <span slot="footer" class="dialog-footer">
                <el-button size="mini" @click="dialogDeletedVisible = false">取消</el-button>
                <el-button size="mini" type="primary" @click="confirmDeleted">确定</el-button>
            </span>
        </el-dialog>

        <el-drawer size="50%" :title="controlTextOperation ? '房屋资讯修改' : '房屋资讯新增'" :visible.sync="houseNewsDrawer"
            :direction="direction">
            <div class="drawer-content">
                <el-form label-width="80px">
                    <el-form-item label="*标题">
                        <el-input placeholder="标题(30个字以内)" v-model="houseNews.title" clearable />
                    </el-form-item>
                    <el-form-item label="*摘要">
                        <el-input type="textarea" :rows="4" placeholder="摘要(200字以内)" v-model="houseNews.summary" />
                    </el-form-item>
                    <el-form-item label="封面">
                        <div class="cover-upload">
                            <img v-if="cover" :src="cover" alt="封面图片">
                            <el-upload class="uploader"
                                action="http://localhost:21090/api/v1.0/house-rental-api/file/upload"
                                :show-file-list="false" :on-success="handleImageSuccess">
                                <i class="el-icon-camera-solid"></i>
                                <div class="upload-tip">点击上传封面</div>
                            </el-upload>
                        </div>
                    </el-form-item>
                    <el-form-item label="*内容">
                        <Editor height="350px" @on-listener="onListener" :receiveContent="content"
                            api="http://localhost:21090/api/v1.0/house-rental-api/file/upload" />
                    </el-form-item>
                    <el-form-item>
                        <el-button @click="handleClose">取消</el-button>
                        <el-button type="primary"
                            @click="controlTextOperation ? handleUpdateConfirm() : handleSaveConfirm()">
                            {{ controlTextOperation ? '确定修改' : '确定新增' }}
                        </el-button>
                    </el-form-item>
                </el-form>
            </div>
        </el-drawer>
    </div>
</template>

<script>
import AutoInput from "@/components/AutoInput.vue";
import Editor from "@/components/Editor.vue";

export default {
    components: { AutoInput, Editor },
    data() {
        return {
            houseNewsDrawer: false,
            controlTextOperation: false,
            direction: 'rtl',
            dialogDeletedVisible: false,
            id: null,
            apiResult: {
                data: [],
                total: 0,
            },
            houseNews: {},
            houseNewsQueryDto: {
                current: 1,
                size: 10,
            },
            deletedItem: {},
            content: '',
            cover: '',
        };
    },
    created() {
        this.fetchFreshData();
    },
    methods: {
        async houseNewsUpdate(item) {
            try {
                const { data } = await this.$axios.get(`/house-news/getById/${item.id}`);
                this.controlTextOperation = true;
                this.houseNewsDrawer = true;
                this.houseNews = { ...data };
                this.content = data.content;
                this.cover = data.cover;
            } catch (error) {
                this.$notify.error({
                    title: '错误',
                    message: error.message,
                    duration: 2000,
                });
            }
        },
        async handleSaveConfirm() {
            try {
                this.houseNews.content = this.content;
                this.houseNews.cover = this.cover;
                await this.$axios.post(`/house-news/save`, this.houseNews);
                this.$message.success("新增成功");
                this.fetchFreshData();
                this.handleClose();
            } catch (error) {
                this.$notify.error({
                    title: '错误',
                    message: error.message,
                    duration: 2000,
                });
            }
        },
        async handleUpdateConfirm() {
            try {
                this.houseNews.content = this.content;
                this.houseNews.cover = this.cover;
                await this.$axios.put(`/house-news/update`, this.houseNews);
                this.$message.success("修改成功");
                this.fetchFreshData();
                this.handleClose();
            } catch (error) {
                this.$notify.error({
                    title: '错误',
                    message: error.message,
                    duration: 2000,
                });
            }
        },
        handleImageSuccess(res, file) {
            if (res.code === 200) {
                this.cover = res.data;
                this.$message.success('封面上传成功');
            } else {
                this.$message.error(res.data || '封面上传失败');
            }
        },
        onListener(content) {
            this.content = content;
        },
        handleClose() {
            this.houseNewsDrawer = false;
            this.controlTextOperation = false;
            this.houseNews = {};
            this.content = '';
            this.cover = '';
        },
        saveHouseNews() {
            this.houseNewsDrawer = true;
        },
        deletedHouse(item) {
            this.deletedItem = { ...item };
            this.id = item.id;
            this.dialogDeletedVisible = true;
        },
        async confirmDeleted() {
            try {
                await this.$axios.delete(`/house-news/${this.id}`);
                this.$message.success("删除成功");
                this.fetchFreshData();
                this.dialogDeletedVisible = false;
            } catch (error) {
                this.$message.error(error.message);
            }
        },
        listener(text) {
            this.houseNewsQueryDto.title = text;
            this.fetchFreshData();
        },
        async fetchFreshData() {
            try {
                const { data, total } = await this.$axios.post('/house-news/list', this.houseNewsQueryDto);
                this.apiResult.data = data;
                this.apiResult.total = total;
            } catch (error) {
                this.$message.error(error.message);
            }
        },
        handleSizeChange(size) {
            this.houseNewsQueryDto.size = size;
            this.houseNewsQueryDto.current = 1;
            this.fetchFreshData();
        },
        handleCurrentChange(current) {
            this.houseNewsQueryDto.current = current;
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
    justify-content: center;
    margin-top: 20px;
}
</style>