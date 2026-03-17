<template>
    <div class="container-center">
        <div class="top">
            <div class="logo">
                <div>
                    <img src="/logo.png" alt="" srcset="">
                </div>
                <div class="text">
                    服务中心
                </div>
            </div>
            <div class="info">
                <div>
                    <img :src="userInfo.avatar" alt="" srcset="">
                </div>
                <div class="text">
                    {{ userInfo.username }}
                </div>
            </div>
        </div>
        <div class="service-cotainer">
            <div class="left">

                <div class="nav">
                    <div @click="select('1')"><i class="el-icon-user"></i>房东信息</div>
                    <div @click="select('2-1')"><i class="el-icon-upload"></i>发布房源</div>
                    <div @click="select('2-2')"><i class="el-icon-help"></i>我的房源</div>
                    <div @click="select('2-3')"><i class="el-icon-s-data"></i>流量统计</div>
                    <div @click="select('3')"><i class="el-icon-s-finance"></i>预约看房</div>
                    <div @click="select('4')"><i class="el-icon-document"></i>租赁合同</div>
                    <div @click="select('5')"><i class="el-icon-switch-button"></i>退租管理</div>
                    <div @click="select('6')"><i class="el-icon-wallet"></i>租赁账单</div>
                    <div @click="select('7')"><i class="el-icon-s-tools"></i>报修管理</div>
                </div>
            </div>
            <div class="right">
                <router-view />
            </div>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            userInfo: {},
        }
    },
    created() {
        if (this.$route.path === '/service-center') {
            this.select('1');
        }
        this.fetchUserInfo();
    },
    methods: {
        select(index) {
            // 路由与菜单index之间的映射
            const pathsMap = {
                '1': 'landlord',
                '2-1': 'post-house',
                '2-2': 'house-list',
                '2-3': 'flow-index',
                '3': 'house-order-info',
                '4': 'rental-contract',
                '5': 'rental-termination',
                '6': 'rental-bill',
                '7': 'repair-order'
            }
            this.$router.push(`/service-center/${pathsMap[index]}`);
        },
        handleOpen(key, keyPath) {
            console.log(key, keyPath);
        },
        handleClose(key, keyPath) {
            console.log(key, keyPath);
        },
        async fetchUserInfo() {
            try {
                const { data } = await this.$axios.get('/user/auth');
                this.userInfo = data;
            } catch (error) {
                this.$router.push('/user');
            }
        },
    }
}
</script>

<style scoped lang="scss">
.right {
    width: calc(100% - 253px);
    padding: 20px 4px;
    box-sizing: border-box;
}

.container-center {
    width: 100%;
    background-color: rgb(248, 248, 248);

    .top {
        height: 64px;
        width: 100%;
        padding-inline: 60px;
        box-sizing: border-box;
        background-color: rgb(255, 255, 255);
        display: flex;
        justify-content: space-between;
        align-items: center;
        position: sticky;
        top: 0;
        z-index: 1000;
        box-shadow: 0 4px 6px rgb(240,240,240);


        .logo {
            display: flex;
            justify-content: left;
            align-items: center;
            gap: 10px;

            img {
                width: 30px;
                height: 30px;
            }

            .text {
                font-size: 20px;
            }

        }

        .info {
            display: flex;
            justify-content: left;
            align-items: center;
            gap: 6px;

            img {
                width: 40px;
                height: 40px;
                border-radius: 50%;
            }

            .text {
                font-size: 20px;
            }

        }


    }
}

.service-cotainer {
    display: flex;

    .left {
        width: 203px;
        padding-inline: 10px 20px;
        height: calc(100vh - 60px);
        background-color: rgb(248, 248, 248);

        .nav {
            position: fixed;
            top: 80px;
            left: 60px;
            text-align: center;
            height: 60px;
            line-height: 60px;
            cursor: pointer;
            font-size: 18px;
            color: rgb(100, 99, 99);

            div {
                &:hover {
                    color: rgb(176, 45, 45);
                }

                i {
                    margin-right: 6px;
                }
            }


        }
    }
}
</style>
