<template>
    <div class="container-house-news">
        <div class="container-detail">
            <div class="title">
                {{ houseNews.title }}
            </div>
            <div class="create-time">
                <div class="time">
                    发布于{{ houseNews.createTime }}
                </div>
                <div class="save" @click="collectHouseOperation">
                    <i :class="saveList.length > 0 ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
                    {{ saveList.length > 0 ? '取消收藏' : '收藏' }}
                </div>
            </div>
            <div class="summary">
                {{ houseNews.summary }}
            </div>
            <div class="content" v-html="houseNews.content"></div>
            <div class="evaluations">
                <Evaluations :userId="Number(userId)" :avatar="String(avatar)" :contentId="Number(houseNewsId)"
                    contentType="HOUSE_NEWS_INFO" />
            </div>
        </div>
    </div>
</template>

<script>
import Evaluations from "@/components/Evaluations"
export default {
    components: { Evaluations },
    data() {
        return {
            houseNewsId: null,
            houseNews: {},
            userId: null,
            avatar: '',
            saveList: [],
            startTime: 0,
            hasRecorded: false, // 防止重复记录

        }
    },
    created() {
        this.getPathId();
        this.fetchUserInfo();
    },
    mounted() {
        this.startTime = performance.now(); // 更精确的高精度时间
        window.addEventListener('beforeunload', this.handlePageLeave);
    },
    beforeDestroy() {
        window.removeEventListener('beforeunload', this.handlePageLeave);
    },
    methods: {
        handlePageLeave() {
            if (this.hasRecorded) return;

            const endTime = performance.now();
            const stayTime = Math.floor(endTime - this.startTime);

            this.hasRecorded = true;

            this.sendStayTime(stayTime);
        },
        async sendStayTime(duration) {
            try {
                await this.$axios.post('/flow-index/stayOperation', {
                    contentId: this.houseNewsId,
                    times: duration,
                    contentType: 'HOUSE_NEWS'
                });
            } catch (e) {
                console.error('记录停留时间失败:', e);
            }
        },
        getPathId() {
            this.houseNewsId = this.$route.query.id;
            this.fetchHouseNewsInfo(this.houseNewsId);
            this.recordViewOperation(this.houseNewsId);
            this.recordSaveStatus(this.houseNewsId);
        },
        // 查询用户对于内容的收藏情况
        async recordSaveStatus(id) {
            try {
                const flowIndexQueryDto = {
                    contentId: id,
                    contentType: 'HOUSE_NEWS',
                    type: 2
                }
                const { data } = await this.$axios.post(`/flow-index/listUser`, flowIndexQueryDto);
                this.saveList = data;
            } catch (error) {
                console.info(error);
            }
        },
        // 浏览操作
        async recordViewOperation(id) {
            try {
                const flowIndex = {
                    contentId: id,
                    contentType: 'HOUSE_NEWS'
                }
                await this.$axios.post(`/flow-index/viewOperation`, flowIndex);
            } catch (error) {
                console.info(error);
            }
        },
        async fetchUserInfo() {
            try {
                const { data } = await this.$axios.get(`/user/auth`);
                this.userId = data.id;
                this.avatar = data.avatar;
            } catch (error) {
                this.$message.error('获取用户信息失败');
                console.error(error);
            }
        },
        async fetchHouseNewsInfo(id) {
            try {
                const { data } = await this.$axios.get(`/house-news/getById/${id}`);
                this.houseNews = { ...data };
            } catch (error) {
                this.$message.error('获取房屋资讯信息失败');
                console.error(error);
            }
        },
        // 收藏操作
        async collectHouseOperation() {
            try {
                const flowIndex = {
                    contentId: this.houseNewsId,
                    contentType: 'HOUSE_NEWS'
                }
                const { message } = await this.$axios.post(`/flow-index/saveOperation`, flowIndex);
                this.$notify.success({
                    title: '收藏操作',
                    message: message,
                    position: 'buttom-right',
                    duration: 1000,
                });
                this.recordSaveStatus(this.houseNewsId);
            } catch (error) {
                this.$message.error(error);
                console.info(error);
            }
        }
    }
}
</script>

<style scoped lang="scss">
.container-house-news {
    width: 100%;
    min-height: 100vh;
    background-color: rgb(244, 244, 244);

    .container-detail {
        width: 700px;
        min-height: 100vh;
        margin: 0 auto;
        background-color: rgb(255, 255, 255);
        box-shadow: 0 4px 8px rgb(220, 220, 220);
        padding: 20px 30px;
        box-sizing: border-box;

        .title {
            font-size: 32px;
            font-weight: 800;
        }

        .create-time {
            font-size: 14px;
            margin-block: 20px;
            display: flex;
            gap: 10px;
            align-items: center;

            .save {
                background-color: rgb(240, 240, 240);
                padding: 4px 10px;
                border-radius: 5px;
                cursor: pointer;

                &:hover {
                    background-color: rgb(234, 234, 234);
                }
            }
        }

        .summary {
            background-color: rgb(244, 246, 248);
            padding: 10px;
            font-size: 14px;
            line-height: 1.5;
        }
    }
}
</style>