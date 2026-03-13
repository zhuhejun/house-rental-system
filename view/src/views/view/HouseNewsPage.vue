<template>
    <div class="container-house-news">
        <div class="container-detail">
            <div class="title">
                {{ houseNews.title }}
            </div>
            <div class="create-time">
                发布于{{ houseNews.createTime }}
            </div>
            <div class="summary">
                {{ houseNews.summary }}
            </div>
            <div class="content" v-html="houseNews.content"></div>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            houseNewsId: null,
            houseNews: {},
            userId: null,
            avatar: ''
        }
    },
    created() {
        this.getPathId();
    },
    methods: {
        getPathId() {
            this.houseNewsId = this.$route.query.id;
            this.fetchHouseNewsInfo(this.houseNewsId);
        },
        async fetchHouseNewsInfo(id) {
            try {
                const { data } = await this.$axios.get(`/viewer/getNewsById/${id}`);
                this.houseNews = { ...data };
            } catch (error) {
                this.$message.error('获取房屋资讯信息失败');
                console.error(error);
            }
        },
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
            margin-block: 10px;
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