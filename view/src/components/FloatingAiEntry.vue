<template>
    <div v-if="visible" class="floating-ai-entry" @click="goAssistant">
        <div class="icon-wrap">
            <i class="el-icon-chat-line-round"></i>
        </div>
        <div class="text-wrap">
            <div class="title">AI助手</div>
            <div class="subtitle">智能找房 / 业务问答</div>
        </div>
    </div>
</template>

<script>
import { getRole, getToken } from "@/utils/storage";

export default {
    name: "FloatingAiEntry",
    computed: {
        visible() {
            return this.$route.path !== "/ai-assistant"
                && this.$route.path !== "/my-ai-assistant"
                && this.$route.path !== "/service-center/ai-assistant";
        }
    },
    methods: {
        goAssistant() {
            const token = getToken();
            const role = getRole();
            let targetPath = token && role !== 1 ? "/my-ai-assistant" : "/ai-assistant";
            if (this.$route.path.startsWith("/service-center")) {
                targetPath = "/service-center/ai-assistant";
            }
            this.$router.push(targetPath);
        }
    }
}
</script>

<style scoped lang="scss">
.floating-ai-entry {
    position: fixed;
    right: 20px;
    bottom: 24px;
    z-index: 1200;
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 176px;
    padding: 12px 14px;
    border-radius: 18px;
    background: linear-gradient(135deg, #5f9f22, #7dc338);
    color: #fff;
    box-shadow: 0 16px 34px rgba(67, 107, 22, 0.3);
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
        transform: translateY(-3px);
        box-shadow: 0 20px 40px rgba(67, 107, 22, 0.36);
    }
}

.icon-wrap {
    width: 40px;
    height: 40px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.18);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
}

.text-wrap {
    min-width: 0;
}

.title {
    font-size: 14px;
    font-weight: 700;
    line-height: 1.2;
}

.subtitle {
    margin-top: 4px;
    font-size: 11px;
    opacity: 0.92;
    line-height: 1.2;
}

@media (max-width: 768px) {
    .floating-ai-entry {
        right: 14px;
        bottom: 16px;
        min-width: auto;
        padding: 10px 12px;
        border-radius: 16px;
    }

    .subtitle {
        display: none;
    }
}
</style>
