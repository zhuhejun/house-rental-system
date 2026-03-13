<template>
    <div class="visitor-home">
        <!-- 动态渐变背景 -->
        <div class="dynamic-bg"></div>

        <!-- 导航栏 -->
        <header class="main-header">
            <div class="logo-container">
                <Logo sysName="海螺租房" textColor="#fff" />
            </div>
            <button class="login-btn" @click="goLogin">
                登录/注册
            </button>
        </header>

        <!-- 主标题区 -->
        <section class="hero-section">
            <div class="hero-content">
                <h1 class="hero-title">
                    <span class="title-word" v-for="(word, i) in titleWords" :key="i" :style="titleStyle(i)">
                        {{ word }}
                    </span>
                </h1>
                <p class="hero-subtitle">发现最适合您的理想住所</p>

                <div class="search-container">
                    <div class="search-box">
                        <input type="text" v-model="houseName" placeholder="搜索房屋">
                        <button class="search-btn" @click="searchHouse">
                            <i class="el-icon-search"></i>
                        </button>
                    </div>
                </div>
            </div>
        </section>

        <!-- 精选房源 -->
        <section class="featured-section">
            <div class="section-header">
                <h2>精选房源</h2>
                <a class="view-all" @click="goExplore">查看全部 <i class="el-icon-arrow-right"></i></a>
            </div>

            <div class="house-grid">
                <div class="house-card" @click="houseView(house)" v-for="(house, i) in featuredHouses" :key="i">
                    <div class="card-image">
                        <img :src="house.cover" :alt="house.name">
                        <div class="price-badge">¥{{ house.rent }}/月</div>
                    </div>
                    <div class="card-details">
                        <h3>{{ house.name }}</h3>
                        <p class="location">
                            <i class="el-icon-location"></i>
                            {{ house.cityAreaName }}
                            <span v-if="house.communityName">·</span>
                            {{ house.communityName }}
                        </p>
                    </div>
                </div>
            </div>
        </section>

        <!-- 热门资讯 -->
        <section class="news-section">
            <h2>热门房屋资讯</h2>
            <div class="news-scroller">
                <div class="news-card" v-for="(news, i) in houseNewList" :key="i" @click="selectNews(news)">
                    <img :src="news.cover" :alt="news.title">
                    <div class="news-info">
                        <h3>{{ news.title }}</h3>
                        <p>{{ news.createTime }}</p>
                    </div>
                </div>
            </div>
        </section>

        <!-- 底部CTA -->
        <section class="cta-section">
            <h2>立即加入海螺租房</h2>
            <p>注册即可查看完整房源信息并预约看房</p>
            <button class="cta-button" @click="goLogin">
                免费注册
            </button>
        </section>
    </div>
</template>

<script>
import Logo from '@/components/Logo.vue';

export default {
    components: { Logo },
    data() {
        return {
            houseName: '',
            titleWords: ['发现', '城市', '美好', '生活'],
            featuredHouses: [],
            houseNewList: []
        }
    },
    created() {
        this.fetchHouses();
        this.fetchHousesNews();
    },
    methods: {
        houseView(house) {
            window.open(`/house-detail-page?id=${house.id}`, '_blank');
        },
        searchHouse() {
            if (!this.houseName) {
                this.$message('请输入关键词');
                return;
            }
            window.open(`/search-house?key=${this.houseName}`, '_blank');
        },
        async fetchHousesNews() {
            try {
                const { data } = await this.$axios.post('/viewer/listHouseNews', { current: 1, size: 4 });
                this.houseNewList = data;
            } catch (error) {
                console.log("查询房屋资讯信息异常：", error);
            }
        },
        async fetchHouses() {
            try {
                const { data } = await this.$axios.post('/viewer/listHouse', { current: 1, size: 3 });
                this.featuredHouses = data;
            } catch (error) {
                console.log("查询房屋信息异常：", error);
            }
        },
        goLogin() {
            window.open('/login', '_blank');
        },
        goExplore() {
            window.open(`/search-house?key=-1`, '_blank');
        },
        selectNews(news) {
            window.open(`/house-news-page?id=${news.id}`, '_blank');
        },
        titleStyle(i) {
            return {
                animationDelay: `${i * 0.15}s`
            }
        }
    }
}
</script>

<style scoped lang="scss">
.visitor-home {
    font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
    color: #2b2d42;
    position: relative;
}

.dynamic-bg {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 540px;
    background: linear-gradient(-45deg, #72ac35, #3e6b0d);
    z-index: -1;
}

.main-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 5%;
    position: relative;
    z-index: 10;
}

.login-btn {
    padding: 12px 24px;
    background: white;
    color: #333;
    border: none;
    border-radius: 30px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
        transform: translateY(-2px);
        box-shadow: 0 5px 15px rgba(0,0,0,0.1);
    }
}

.hero-section {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 0 5% 100px;
    color: white;
    position: relative;
}

.hero-content {
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
}

.hero-title {
    font-size: 4.5rem;
    font-weight: 700;
    line-height: 1.2;
    margin-bottom: 20px;

    .title-word {
        display: inline-block;
        opacity: 0;
        transform: translateY(30px);
        animation: fadeInUp 0.8s forwards;
    }
}

@keyframes fadeInUp {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.hero-subtitle {
    font-size: 1.5rem;
    margin-bottom: 40px;
    opacity: 0.9;
}

.search-container {
    max-width: 800px;
}

.search-box {
    position: relative;
    margin-bottom: 20px;

    input {
        width: 100%;
        padding: 18px 30px;
        border-radius: 50px;
        border: none;
        font-size: 1.1rem;
        background: rgba(255,255,255,0.9);
        box-shadow: 0 5px 20px rgba(0,0,0,0.1);
        transition: all 0.3s ease;

        &:focus {
            outline: none;
            box-shadow: 0 8px 25px rgba(0,0,0,0.15);
        }
    }

    .search-btn {
        position: absolute;
        right: 10px;
        top: 50%;
        transform: translateY(-50%);
        width: 50px;
        height: 50px;
        border-radius: 50%;
        background: #72ac35;
        color: white;
        border: none;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
            transform: translateY(-50%) scale(1.05);
        }
    }
}

.featured-section {
    padding: 100px 5%;
    background: #f8f9fa;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 50px;

    h2 {
        font-size: 2.5rem;
        font-weight: 700;
        margin: 0;
        color: #2b2d42;
    }

    .view-all {
        display: flex;
        align-items: center;
        font-size: 1.1rem;
        color: #4361ee;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
            color: #3f37c9;
            transform: translateX(5px);
        }
    }
}

.house-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 30px;
}

.house-card {
    background: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 5px 15px rgba(0,0,0,0.05);
    transition: all 0.3s ease;

    &:hover {
        transform: translateY(-10px);
        box-shadow: 0 15px 30px rgba(0,0,0,0.1);
    }
}

.card-image {
    position: relative;
    height: 220px;
    overflow: hidden;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.5s ease;
    }

    &:hover img {
        transform: scale(1.05);
    }

    .price-badge {
        position: absolute;
        bottom: 20px;
        left: 20px;
        background: rgba(0,0,0,0.7);
        color: white;
        padding: 8px 16px;
        border-radius: 4px;
        font-weight: 600;
    }
}

.card-details {
    padding: 20px;

    h3 {
        margin: 0 0 10px;
        font-size: 1.3rem;
        color: #2b2d42;
    }

    .location {
        color: #666;
        margin: 0 0 15px;
        display: flex;
        align-items: center;

        i {
            margin-right: 5px;
            color: #4361ee;
        }
    }
}

.news-section {
    padding: 80px 5%;
    background: white;

    h2 {
        text-align: center;
        font-size: 2.5rem;
        margin-bottom: 50px;
        color: #2b2d42;
    }
}

.news-scroller {
    display: flex;
    gap: 20px;
    overflow-x: auto;
    padding-bottom: 20px;

    &::-webkit-scrollbar {
        height: 8px;
    }

    &::-webkit-scrollbar-thumb {
        background: #4361ee;
        border-radius: 10px;
    }
}

.news-card {
    flex: 0 0 220px;
    height: 180px;
    border-radius: 10px;
    overflow: hidden;
    position: relative;
    cursor: pointer;

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.5s ease;
    }

    &:hover img {
        transform: scale(1.1);
    }

    .news-info {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 20px;
        background: linear-gradient(transparent, rgba(0,0,0,0.7));
        color: white;

        h3 {
            margin: 0;
            font-size: 1.2rem;
        }

        p {
            margin: 5px 0 0;
            font-size: 0.9rem;
            opacity: 0.8;
        }
    }
}

.cta-section {
    padding: 100px 5%;
    background: linear-gradient(135deg, #4361ee, #3f37c9);
    color: white;
    text-align: center;

    h2 {
        font-size: 2.5rem;
        margin-bottom: 20px;
    }

    p {
        font-size: 1.2rem;
        margin-bottom: 40px;
        opacity: 0.9;
    }
}

.cta-button {
    padding: 16px 40px;
    background: white;
    color: #4361ee;
    border: none;
    border-radius: 50px;
    font-size: 1.2rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
        transform: translateY(-3px);
        box-shadow: 0 15px 30px rgba(0,0,0,0.2);
    }
}

@media (max-width: 768px) {
    .hero-title {
        font-size: 3rem;
    }

    .hero-subtitle {
        font-size: 1.2rem;
    }

    .section-header h2,
    .news-section h2,
    .cta-section h2 {
        font-size: 2rem;
    }

    .house-grid {
        grid-template-columns: 1fr;
    }

    .news-card {
        flex: 0 0 240px;
        height: 160px;
    }
}
</style>