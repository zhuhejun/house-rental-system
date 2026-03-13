<template>
    <div class="container-notice">
        <!-- 公告标题 -->
        <div class="title">
            {{ notice.title }}
        </div>
        <!-- 公告发布时间 -->
        <div class="time">
            发布于{{ notice.createTime }}
        </div>
        <!-- 公告内容 -->
        <div v-html="notice.content"></div>
        <!-- 评论模块嵌入 -->
        <div>
            <Evaluations :userId="Number(userId)" :avatar="String(avatar)" :contentId="Number(noticeId)"
                contentType="NOTICE" />
        </div>
    </div>
</template>

<script>
import Evaluations from "@/components/Evaluations"
export default {
    components: { Evaluations },
    data() {
        return {
            noticeId: null,
            notice: {},
            userId: null,
            avatar: '',
        }
    },
    created() {
        this.getPathId();
        this.fetchUserInfo();
    },
    methods: {
        getPathId() {
            this.noticeId = this.$route.query.id;
            this.fetchNoticeInfo(this.noticeId);
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
        async fetchNoticeInfo(id) {
            try {
                const { data } = await this.$axios.get(`/notice/getById/${id}`);
                this.notice = { ...data };
            } catch (error) {
                this.$message.error('获取公告信息失败');
                console.error(error);
            }
        },
    }
}
</script>

<style scoped lang="scss">

.container-notice{
    width: 700px;
    margin: 0 auto;
    padding-block: 30px;
    .title{
        font-size: 40px;
        margin-block: 10px;
    }
}

</style>