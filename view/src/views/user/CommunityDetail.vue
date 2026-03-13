<template>
    <div class="container-community">
        <div class="info">
            <!-- 小区信息标题 -->
            <div class="title">
                {{ community.name }}
            </div>
            <div class="description-section">
                <div class="section-title"> 小区简介</div>
                <div class="description-content" v-html="community.detail"></div>
            </div>
            <!-- 顶部图片展示区 -->
            <div class="gallery-section">
                <div class="main-image">
                    <img v-if="coverList.length > 0" :src="coverList[currentImageIndex].url" alt="房屋封面图">
                </div>
                <div class="thumbnail-list">
                    <div v-for="(cover, index) in coverList" :key="cover.uid" class="thumbnail"
                        @click="currentImageIndex = index">
                        <img :src="cover.url" alt="房屋实景图">
                    </div>
                </div>
            </div>
            <!-- 评论模块嵌入 -->
            <div style="background-color: rgb(255,255,255);">
                <Evaluations :userId="Number(userId)" :avatar="String(avatar)" :contentId="Number(communityId)"
                    contentType="COMMUNITY" />
            </div>
        </div>
        <!-- 渲染小区下面的所有房产信息 -->
        <div class="house">
            <h2 style="padding-left: 10px;">小区下待租房源</h2>
            <div class="house-list">
                <div v-if="houseList.length === 0">
                    <el-empty description="暂无房屋信息"></el-empty>
                </div>
                <div @click="houseItemClick(item.id)" class="house-item" v-for="item in houseList" :key="item.id">
                    <img :src="item.cover" alt="">
                    <div>
                        <div class="name">{{ item.name }}</div>
                        <div class="point">
                            <div>
                                <i class="el-icon-location"></i>
                                {{ item.cityAreaName }}&nbsp;·&nbsp;{{ item.communityName }}
                            </div>
                            <div>
                                {{ item.depositMethodName }}
                            </div>
                            <div>
                                {{ item.sizeNumber }}m²
                            </div>
                            <div>
                                {{ item.directionName }}
                            </div>
                            <div>
                                {{ item.fitmentStatusName }}
                            </div>
                        </div>
                        <div class="rent">
                            ¥{{ item.rent }}
                        </div>
                    </div>
                </div>
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
            communityId: null,
            community: {},
            userId: null,
            avatar: '',
            houseList: [],
            coverList: [],
            currentImageIndex: 0,
        }
    },
    created() {
        this.getPathId();
        this.fetchUserInfo();
    },
    methods: {
        // 跳转至房屋详情页
        houseItemClick(id) {
            window.open(`/house-detail?id=${id}`, '_blank');
        },
        getPathId() {
            this.communityId = this.$route.query.id;
            this.fetchCommunityInfo(this.communityId);
            this.fetchHouseList(this.communityId);
        },
        async fetchHouseList(id) {
            try {
                const { data } = await this.$axios.post(`/house/listUser`, { communityId: id });
                this.houseList = data;
            } catch (error) {
                this.$message.error('获取房屋信息失败：', error);
                console.error(error);
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
        async fetchCommunityInfo(id) {
            try {
                const { data } = await this.$axios.get(`/community/getById/${id}`);
                this.community = { ...data };
                this.coverList = (this.community.covers || '')
                    .split(',')
                    .filter(url => url.trim())
                    .map(url => ({
                        uid: Date.now() + Math.floor(Math.random() * 1000),
                        url: url.trim()
                    }));
            } catch (error) {
                this.$message.error('获取小区信息信息失败');
                console.error(error);
            }
        },
    }
}
</script>

<style scoped lang="scss">
.container-community {
    display: flex;
    // padding: 10px 40px;
    box-sizing: border-box;
    gap: 20px;
    background-color: rgb(248,248,248);

    .info {
        width: 800px;

        .description-section {

            padding: 20px 40px;
            background: #fff;
            // border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

            .section-title {
                font-size: 18px;
                font-weight: 600;
                margin-bottom: 20px;
                color: #333;
                padding-bottom: 10px;
                border-bottom: 1px solid #eee;
            }

            .description-content {
                line-height: 1.8;
                color: #666;

                v::deep p {
                    margin-bottom: 15px;
                }

                v::deep img {
                    max-width: 100%;
                    height: auto;
                    margin: 10px 0;
                    border-radius: 4px;
                }
            }
        }

        /* 图片展示区 */
        .gallery-section {
            padding: 20px 40px;
            background-color: rgb(255,255,255);
            // border-radius: 8px;
            overflow: hidden;
            // box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

            .main-image {
                height: auto;
                // background: #f5f5f5;

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }
            }

            .thumbnail-list {
                display: flex;
                padding: 10px;
                background: #fff;
                border: 1px solid rgb(240, 240, 240);
                overflow-x: auto;

                .thumbnail {
                    width: 100px;
                    height: 80px;
                    margin-right: 10px;
                    cursor: pointer;
                    border: 2px solid transparent;
                    transition: all 0.3s;

                    &:hover {
                        border-color: #409EFF;
                    }

                    img {
                        width: 100%;
                        height: 100%;
                        object-fit: cover;
                    }
                }
            }
        }
    }

    .house {
        width: calc(100% - 800px);

        .house-list {
            display: flex;
            flex-wrap: wrap;
            margin-block: 30px;

            .house-item {
                flex: 1 1 400px;
                display: flex;
                gap: 10px;
                padding: 10px;
                box-sizing: border-box;
                cursor: pointer;

                .name {
                    font-size: 18px;
                }

                img {
                    width: 120px;
                    height: 80px;
                    border-radius: 5px;
                }

                .point {
                    font-size: 12px;
                    margin-block: 10px;
                    display: flex;
                    justify-content: left;
                    align-items: center;
                    gap: 10px;
                    box-sizing: border-box;

                    div {
                        background-color: rgb(245, 246, 247);
                        padding: 2px 4px;
                    }
                }

                .rent {
                    font-size: 22px;
                    font-weight: 800;
                    color: rgb(222, 88, 78);
                }
            }
        }
    }

    .title {
        font-size: 40px;

        position: sticky;
        top: 0;
        background-color: rgb(255,255,255);
        padding: 30px 40px;
    }
}
</style>