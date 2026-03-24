<template>
    <div class="ai-page">
        <div class="hero">
            <div class="hero-copy">
                <h1>AI 智能助手</h1>
                <p>现在支持智能找房、平台业务问答和通用问答三种场景，既能查真实房源，也能按场景稳定回答问题。</p>
            </div>
            <div class="scene-switch">
                <Tab :buttons="sceneButtons" :initialActive="scene" @change="handleSceneChange" />
            </div>
        </div>

        <div class="content-grid">
            <aside class="quick-panel">
                <h3>快捷提问</h3>
                <div class="quick-list">
                    <span v-for="item in quickQuestions" :key="item" class="quick-item" @click="useQuickQuestion(item)">
                        {{ item }}
                    </span>
                </div>
                <div class="tips">
                    <h4>当前能力</h4>
                    <p v-if="scene === 'house-search'">支持预算、区域、户型、面积、装修、地铁、配套设施等自然语言筛选。</p>
                    <p v-else-if="scene === 'business-qa'">支持合同、押金、账单、水电、退租、报修等当前系统业务规则问答。登录后还能结合你当前账号的合同、账单和报修状态回答。</p>
                    <p v-else>通用问答直接接千问，可回答天气、常识、写作和闲聊；如果你要问平台流程，建议切回“业务问答”。</p>
                </div>
                <div class="panel-actions">
                    <span class="panel-action" @click="clearCurrentHistory">清空当前场景对话</span>
                </div>
            </aside>

            <section class="chat-panel">
                <div class="chat-history" v-if="history.length">
                    <div v-for="(item, index) in history" :key="index" class="chat-item" :class="item.role">
                        <div class="bubble">
                            <div class="role-name">{{ item.role === 'user' ? '我' : 'AI 助手' }}</div>
                            <div class="message-text">{{ item.content }}</div>

                            <template v-if="item.role === 'assistant' && item.response">
                                <div v-if="item.response.provider" class="provider-badge">
                                    当前使用：{{ item.response.provider }}
                                </div>
                                <div class="answer-block">{{ item.response.answer }}</div>
                                <div v-if="item.response.recommendedScene && item.response.recommendedSceneLabel" class="scene-guide">
                                    <span class="scene-guide-text">更适合切到：{{ item.response.recommendedSceneLabel }}</span>
                                    <span class="scene-guide-action" @click="switchSceneAndReuse(item.response.recommendedScene, item.content)">切换并继续提问</span>
                                </div>

                                <div v-if="item.response.parsedFilters" class="filter-block">
                                    <div class="filter-title">识别到的筛选条件</div>
                                    <div class="filter-tags">
                                        <span v-for="tag in buildFilterTags(item.response.parsedFilters)" :key="tag" class="filter-tag">
                                            {{ tag }}
                                        </span>
                                    </div>
                                </div>

                                <div v-if="item.response.houseList && item.response.houseList.length" class="house-block">
                                    <div class="filter-title">推荐房源</div>
                                    <div class="house-grid">
                                        <div v-for="house in item.response.houseList" :key="house.id" class="house-card"
                                            @click="openHouse(house.id)">
                                            <img :src="house.cover" :alt="house.name">
                                            <div class="house-info">
                                                <h4>{{ house.name }}</h4>
                                                <p>{{ house.cityAreaName }}<span v-if="house.communityName"> · {{ house.communityName }}</span></p>
                                                <div class="meta">
                                                    <span>{{ house.sizeNumber }}㎡</span>
                                                    <span v-if="house.directionName">{{ house.directionName }}</span>
                                                    <span>{{ house.depositMethodName || '-' }}</span>
                                                </div>
                                                <div class="house-footer">
                                                    <strong>¥{{ house.rent }}/月</strong>
                                                    <span class="detail-link" @click.stop="openHouse(house.id)">查看详情</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div v-if="item.response.suggestions && item.response.suggestions.length" class="suggestion-block">
                                    <div class="filter-title">你还可以继续问</div>
                                    <div class="suggestion-list">
                                        <span v-for="suggestion in item.response.suggestions" :key="suggestion"
                                            class="suggestion-item" @click="useQuickQuestion(suggestion)">
                                            {{ suggestion }}
                                        </span>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>

                <div v-else class="empty-state">
                    <i class="el-icon-chat-line-round"></i>
                    <p>输入你的需求，我会按当前场景帮你找房、回答平台流程，或直接进行通用问答。</p>
                </div>

                <div class="input-panel">
                    <el-input type="textarea" :rows="4" v-model="message"
                        :placeholder="inputPlaceholder"
                        @keydown.native="handleInputKeydown"
                        resize="none">
                    </el-input>
                    <div class="input-actions">
                        <span class="scene-hint">当前场景：{{ sceneLabel }}</span>
                        <span class="info-bt" @click="sendMessage">
                            <i v-if="loading" class="el-icon-loading"></i>
                            <span v-else>发送</span>
                        </span>
                    </div>
                </div>
            </section>
        </div>
    </div>
</template>

<script>
import Tab from "@/components/Tab.vue";

export default {
    components: { Tab },
    data() {
        return {
            scene: 'house-search',
            loading: false,
            message: '',
            historyMap: {
                'house-search': [],
                'business-qa': [],
                'general-qa': []
            },
            sceneButtons: [
                { label: '智能找房', value: 'house-search' },
                { label: '业务问答', value: 'business-qa' },
                { label: '通用问答', value: 'general-qa' }
            ],
            quickQuestionMap: {
                'house-search': [
                    '帮我找深圳南山 3000 以内的一室一厅',
                    '预算 5000，想要整租，最好带空调和阳台',
                    '找一套近地铁的两室一厅，最好精装'
                ],
                'business-qa': [
                    '我现在下一步做什么？',
                    '合同确认后下一步做什么？',
                    '退租流程怎么走？',
                    '报修费用什么时候支付？'
                ],
                'general-qa': [
                    '桂林的天气怎么样？',
                    '帮我写一段礼貌的请假说明',
                    '什么是协同过滤推荐？',
                    '周末适合做什么放松一下？'
                ]
            }
        };
    },
    computed: {
        history() {
            return this.historyMap[this.scene] || [];
        },
        quickQuestions() {
            return this.quickQuestionMap[this.scene] || [];
        },
        inputPlaceholder() {
            if (this.scene === 'house-search') {
                return '例如：帮我找深圳南山 3000 以内的一室一厅，最好带空调和阳台';
            }
            if (this.scene === 'business-qa') {
                return '例如：我现在下一步做什么？';
            }
            return '例如：桂林的天气怎么样？';
        },
        sceneLabel() {
            const mapping = {
                'house-search': '智能找房',
                'business-qa': '业务问答',
                'general-qa': '通用问答'
            };
            return mapping[this.scene] || 'AI 助手';
        }
    },
    methods: {
        handleSceneChange(obj) {
            this.scene = obj.value;
            this.$nextTick(() => this.scrollToBottom());
        },
        useQuickQuestion(text) {
            this.message = text;
        },
        switchSceneAndReuse(scene, text) {
            this.scene = scene;
            this.message = text;
            this.$nextTick(() => this.scrollToBottom());
        },
        clearCurrentHistory() {
            this.$set(this.historyMap, this.scene, []);
        },
        handleInputKeydown(event) {
            if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault();
                this.sendMessage();
            }
        },
        async sendMessage() {
            const content = (this.message || '').trim();
            if (!content || this.loading) {
                return;
            }

            const currentHistory = [...this.history, { role: 'user', content }];
            this.$set(this.historyMap, this.scene, currentHistory);
            this.loading = true;
            this.message = '';
            try {
                const { data } = await this.$axios.post('/ai-assistant/chat', {
                    scene: this.scene,
                    message: content
                });
                currentHistory.push({
                    role: 'assistant',
                    content,
                    response: data
                });
                this.$set(this.historyMap, this.scene, currentHistory);
            } catch (error) {
                this.$message.error(error.message || 'AI 助手暂时不可用');
            } finally {
                this.loading = false;
                this.$nextTick(() => this.scrollToBottom());
            }
        },
        scrollToBottom() {
            const container = this.$el && this.$el.querySelector('.chat-history');
            if (container) {
                container.scrollTop = container.scrollHeight;
            }
        },
        buildFilterTags(filters) {
            const tags = [];
            if (filters.city) tags.push(`城市：${filters.city}`);
            if (filters.district) tags.push(`区域：${filters.district}`);
            if (filters.maxRent !== null && filters.maxRent !== undefined) tags.push(`最高租金：${filters.maxRent}`);
            if (filters.minRent !== null && filters.minRent !== undefined && Number(filters.minRent) > 0) tags.push(`最低租金：${filters.minRent}`);
            if (filters.sizedName) tags.push(`户型：${filters.sizedName}`);
            if (filters.rentalTypeName) tags.push(`租赁方式：${filters.rentalTypeName}`);
            if (filters.directionName) tags.push(`朝向：${filters.directionName}`);
            if (filters.fitmentStatusName) tags.push(`装修：${filters.fitmentStatusName}`);
            if (filters.isSubway) tags.push('偏好：近地铁');
            if (filters.facilities && filters.facilities.length) tags.push(`设施：${filters.facilities.join('、')}`);
            return tags;
        },
        openHouse(id) {
            const route = this.$router.resolve({
                path: '/house-detail',
                query: { id }
            });
            window.open(route.href, '_blank');
        }
    }
}
</script>

<style scoped lang="scss">
.ai-page {
    min-height: 100vh;
    padding: 28px;
    background:
        radial-gradient(circle at top left, rgba(114, 172, 53, 0.12), transparent 28%),
        linear-gradient(180deg, #f6f9f2 0%, #eef4eb 100%);
    box-sizing: border-box;
}

.hero {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 20px;
    margin-bottom: 24px;
}

.hero-copy {
    h1 {
        margin: 0 0 10px;
        font-size: 34px;
        color: #24351a;
    }

    p {
        margin: 0;
        color: #5a6b4f;
        font-size: 15px;
    }
}

.content-grid {
    display: grid;
    grid-template-columns: 280px 1fr;
    gap: 20px;
}

.quick-panel,
.chat-panel {
    background: rgba(255, 255, 255, 0.9);
    border-radius: 18px;
    box-shadow: 0 14px 30px rgba(52, 79, 32, 0.08);
}

.quick-panel {
    padding: 22px;

    h3 {
        margin: 0 0 18px;
        color: #24351a;
    }
}

.quick-list,
.suggestion-list,
.filter-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.quick-item,
.suggestion-item,
.filter-tag {
    padding: 8px 12px;
    border-radius: 999px;
    background: #edf5e5;
    color: #446029;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
        background: #dfeecf;
    }
}

.tips {
    margin-top: 22px;
    padding-top: 18px;
    border-top: 1px solid #eef2eb;

    h4 {
        margin: 0 0 10px;
        color: #24351a;
    }

    p {
        margin: 0;
        color: #627257;
        line-height: 1.7;
        font-size: 14px;
    }
}

.panel-actions {
    margin-top: 16px;
}

.panel-action {
    color: #4d7a2f;
    font-size: 13px;
    cursor: pointer;

    &:hover {
        text-decoration: underline;
    }
}

.chat-panel {
    display: flex;
    flex-direction: column;
    min-height: 720px;
    padding: 20px;
}

.chat-history {
    flex: 1;
    overflow-y: auto;
    padding-right: 6px;
}

.chat-item {
    display: flex;
    margin-bottom: 16px;

    &.user {
        justify-content: flex-end;

        .bubble {
            background: #e4f0d7;
        }
    }

    &.assistant .bubble {
        background: #f8faf5;
    }
}

.bubble {
    max-width: 88%;
    border-radius: 16px;
    padding: 16px;
    box-shadow: inset 0 0 0 1px rgba(93, 122, 68, 0.06);
}

.role-name {
    font-size: 12px;
    color: #7c8a71;
    margin-bottom: 8px;
}

.message-text,
.answer-block {
    color: #2c3a23;
    line-height: 1.8;
    white-space: pre-wrap;
}

.provider-badge {
    display: inline-flex;
    align-items: center;
    margin-top: 10px;
    padding: 4px 10px;
    border-radius: 999px;
    background: #edf5e5;
    color: #547137;
    font-size: 12px;
    font-weight: 600;
}

.answer-block {
    margin-top: 10px;
}

.scene-guide {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 12px;
}

.scene-guide-text {
    color: #6a7a5d;
    font-size: 13px;
}

.scene-guide-action {
    display: inline-flex;
    align-items: center;
    padding: 6px 12px;
    border-radius: 999px;
    background: #edf5e5;
    color: #446029;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;

    &:hover {
        background: #dfeecf;
    }
}

.filter-block,
.house-block,
.suggestion-block {
    margin-top: 14px;
}

.filter-title {
    font-size: 13px;
    color: #5f6d54;
    margin-bottom: 10px;
}

.house-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 14px;
}

.house-card {
    overflow: hidden;
    border-radius: 14px;
    background: #fff;
    cursor: pointer;
    box-shadow: 0 8px 20px rgba(48, 65, 34, 0.08);
    transition: transform 0.2s ease;

    &:hover {
        transform: translateY(-3px);
    }

    img {
        width: 100%;
        height: 140px;
        object-fit: cover;
        display: block;
    }
}

.house-info {
    padding: 12px;

    h4 {
        margin: 0 0 8px;
        color: #24351a;
        font-size: 14px;
    }

    p {
        margin: 0 0 8px;
        color: #69795f;
        font-size: 12px;
    }

    strong {
        color: #2d6e1f;
    }
}

.house-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.detail-link {
    color: #4d7a2f;
    font-size: 12px;
    font-weight: 600;
    white-space: nowrap;

    &:hover {
        color: #2d6e1f;
        text-decoration: underline;
    }
}

.meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
    font-size: 12px;
    color: #7b8a70;
}

.empty-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #7a8a6f;

    i {
        font-size: 42px;
        margin-bottom: 12px;
    }
}

.input-panel {
    margin-top: 18px;
    padding-top: 18px;
    border-top: 1px solid #edf2e8;
}

.input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
}

.scene-hint {
    color: #6b7b61;
    font-size: 13px;
}

@media (max-width: 900px) {
    .content-grid {
        grid-template-columns: 1fr;
    }

    .hero {
        flex-direction: column;
        align-items: stretch;
    }

    .chat-panel {
        min-height: 640px;
    }
}
</style>
