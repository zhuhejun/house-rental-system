<template>
  <div class="landlord-container">
    <div class="header-section">
      <h2 class="section-title">认证信息</h2>
      <div class="auth-status" :class="{ 'is-audited': landlord.isAudit }">
        <i :class="landlord.isAudit ? 'el-icon-success' : 'el-icon-warning'"></i>
        <span>{{ landlord.isAudit ? '已审核' : '审核中' }}</span>
      </div>
    </div>

    <div class="content-section">
      <!-- 用户信息 -->
      <div class="auth-user-info card-style">
        <el-avatar :size="60" :src="landlord.avatar" />
        <div class="user-name">{{ landlord.username }}</div>
      </div>

      <!-- 身份证信息 -->
      <div class="info-group">
        <p class="info-label">*身份证号</p>
        <div class="idcard card-style">
          {{ landlord.idcard || '--' }}
        </div>
      </div>

      <!-- 证件照 -->
      <div class="info-group">
        <p class="info-label">*证件照</p>
        <div class="image-idcard">
          <div class="idcard-image-wrapper">
            <div class="image-label">身份证正面</div>
            <img :src="landlord.idcardFront" alt="身份证正面" class="card-style">
          </div>
          <div class="idcard-image-wrapper">
            <div class="image-label">身份证反面</div>
            <img :src="landlord.idcardOpposite" alt="身份证反面" class="card-style">
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      landlord: {
        isAudit: false,
        avatar: '',
        username: '',
        idcard: '',
        idcardFront: '',
        idcardOpposite: ''
      },
    }
  },
  created() {
    this.fetchLandlordInfo();
  },
  methods: {
    async fetchLandlordInfo() {
      try {
        const { data } = await this.$axios.post('/landlord/listUser', {});
        this.landlord = data;
      } catch (error) {
        console.log("查询房东信息异常：", error);
        this.$message.error('获取房东信息失败');
      }
    },
  }
}
</script>

<style scoped lang="scss">
.landlord-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.auth-status {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  
  &.is-audited {
    color: #67C23A;
    background-color: rgba(103, 194, 58, 0.1);
  }
  
  &:not(.is-audited) {
    color: #E6A23C;
    background-color: rgba(230, 162, 60, 0.1);
  }
  
  i {
    margin-right: 6px;
    font-size: 16px;
  }
}

.content-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.card-style {
  background-color: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
}

.auth-user-info {
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 16px;
  
  .user-name {
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }
}

.info-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.idcard {
  padding: 16px;
  font-size: 18px;
  letter-spacing: 1px;
  color: #333;
  font-family: monospace;
}

.image-idcard {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.idcard-image-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  .image-label {
    font-size: 13px;
    color: #909399;
  }
  
  img {
    width: 240px;
    height: 160px;
    object-fit: cover;
    transition: transform 0.2s;
    
    &:hover {
      transform: scale(1.02);
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .landlord-container {
    padding: 16px;
  }
  
  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .image-idcard {
    flex-direction: column;
  }
  
  .idcard-image-wrapper img {
    width: 100%;
    height: auto;
    max-height: 200px;
  }
}
</style>