<template>
  <div class="container">
    <div class="top-header">
      <div class="nav-left">
        <Tab :buttons="sceneTabs" :initialActive="activeScene" @change="handleSceneChange" />
        <Tab :buttons="statusTabButtons" :initialActive="String(queryStatus)" @change="handleStatusChange" />
      </div>
      <div class="nav-right">
        <AutoInput :placeholder="activeScene === 'comment' ? '搜索评论内容' : '搜索服务评价内容'" @listener="listener" />
      </div>
    </div>

    <div v-if="activeScene === 'comment'">
      <el-table :data="apiResult.data">
        <el-table-column width="180" prop="username" label="评论者"></el-table-column>
        <el-table-column width="300" prop="content" label="评论内容">
          <template #default="scope">
            <div class="over-text">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="contentType" width="130" align="center" label="所属模块"></el-table-column>
        <el-table-column width="110" align="center" label="举报次数">
          <template #default="scope">
            {{ formatReportCount(scope.row.reportCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" width="120" align="center" label="审核状态">
          <template #default="scope">
            <el-tag size="mini" :type="statusTagType(scope.row.status)">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column width="168" align="center" label="评论时间">
          <template #default="scope">
            {{ scope.row.createTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180">
          <template #default="scope">
            <div class="operation-links">
              <span v-if="scope.row.status !== 1" @click="moderateComment(scope.row, 1)">通过</span>
              <span v-if="scope.row.status !== 3" @click="moderateComment(scope.row, 3)">屏蔽</span>
              <span class="danger" @click="handleDelete(scope.row, 'comment')">删除</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-else>
      <el-table :data="apiResult.data">
        <el-table-column width="160" prop="username" label="评价用户"></el-table-column>
        <el-table-column width="120" align="center" label="预约单ID">
          <template #default="scope">
            {{ scope.row.houseOrderInfoId || '-' }}
          </template>
        </el-table-column>
        <el-table-column width="90" align="center" label="评分">
          <template #default="scope">
            <span class="score-text">{{ scope.row.score || 0 }} 分</span>
          </template>
        </el-table-column>
        <el-table-column prop="text" label="评价内容">
          <template #default="scope">
            <div class="over-text">{{ scope.row.text }}</div>
          </template>
        </el-table-column>
        <el-table-column width="110" align="center" label="举报次数">
          <template #default="scope">
            {{ formatReportCount(scope.row.reportCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" width="120" align="center" label="审核状态">
          <template #default="scope">
            <el-tag size="mini" :type="statusTagType(scope.row.status)">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column width="168" align="center" label="评价时间">
          <template #default="scope">
            {{ scope.row.createTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180">
          <template #default="scope">
            <div class="operation-links">
              <span v-if="scope.row.status !== 1" @click="moderateServiceEvaluation(scope.row, 1)">通过</span>
              <span v-if="scope.row.status !== 3" @click="moderateServiceEvaluation(scope.row, 3)">屏蔽</span>
              <span class="danger" @click="handleDelete(scope.row, 'service')">删除</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pager">
      <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
        :current-page="queryDto.current" :page-sizes="[10, 20]" :page-size="queryDto.size"
        layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
    </div>

    <el-dialog title="删除内容" :show-close="false" :visible.sync="dialogDeletedVisible" width="20%">
      <span>确定删除这条内容吗？</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="dialogDeletedVisible = false">取消</el-button>
        <el-button size="mini" type="primary" @click="confirmDeleted">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import AutoInput from "@/components/AutoInput.vue";
import Tab from "@/components/Tab.vue";

export default {
  components: { AutoInput, Tab },
  data() {
    return {
      activeScene: 'comment',
      queryStatus: 2,
      sceneTabs: [
        { label: '普通评论', value: 'comment' },
        { label: '服务评价', value: 'service' }
      ],
      statusTabs: [
        { label: '待审核', value: '2' },
        { label: '有举报', value: 'reported' },
        { label: '正常', value: '1' },
        { label: '已屏蔽', value: '3' },
        { label: '全部', value: 'null' }
      ],
      reportSummary: {
        comment: 0,
        service: 0,
        total: 0
      },
      apiResult: {
        data: [],
        total: 0
      },
      queryDto: {
        current: 1,
        size: 10,
        content: ''
      },
      dialogDeletedVisible: false,
      deletedId: null,
      deletedType: 'comment'
    };
  },
  computed: {
    statusTabButtons() {
      const currentCount = this.activeScene === 'comment'
        ? Number(this.reportSummary.comment || 0)
        : Number(this.reportSummary.service || 0);
      return this.statusTabs.map((tab) => ({
        ...tab,
        dot: tab.value === 'reported' && currentCount > 0
      }));
    },
    queryPayload() {
      const payload = {
        current: this.queryDto.current,
        size: this.queryDto.size
      };
      if (this.queryDto.content) {
        if (this.activeScene === 'comment') {
          payload.content = this.queryDto.content;
        } else {
          payload.text = this.queryDto.content;
        }
      }
      if (this.queryStatus === 'reported') {
        payload.reportedOnly = true;
      } else if (this.queryStatus !== null) {
        payload.status = this.queryStatus;
      }
      return payload;
    }
  },
  created() {
    this.fetchReportSummary();
    this.fetchFreshData();
  },
  methods: {
    handleSceneChange({ value }) {
      this.activeScene = value;
      this.queryDto.current = 1;
      this.queryDto.content = '';
      this.fetchFreshData();
    },
    handleStatusChange({ value }) {
      if (value === 'null') {
        this.queryStatus = null;
      } else if (value === 'reported') {
        this.queryStatus = 'reported';
      } else {
        this.queryStatus = Number(value);
      }
      this.queryDto.current = 1;
      this.fetchFreshData();
    },
    listener(text) {
      this.queryDto.content = text;
      this.queryDto.current = 1;
      this.fetchFreshData();
    },
    async fetchReportSummary() {
      try {
        const response = await this.$axios.get('/content-report/pending-summary');
        const summary = response && response.data ? response.data : {};
        this.reportSummary = {
          comment: Number(summary.comment || 0),
          service: Number(summary.service || 0),
          total: Number(summary.total || 0)
        };
        window.dispatchEvent(new Event('content-report-refresh'));
      } catch (error) {
        console.error('查询待处理举报统计失败:', error);
      }
    },
    statusText(status) {
      const map = { 1: '正常', 2: '待审核', 3: '已屏蔽' };
      return map[status] || '未知';
    },
    statusTagType(status) {
      if (status === 1) return 'success';
      if (status === 2) return 'warning';
      return 'danger';
    },
    formatReportCount(reportCount) {
      return `${Number(reportCount || 0)} 次`;
    },
    async fetchFreshData() {
      try {
        const url = this.activeScene === 'comment' ? '/evaluations/query' : '/house-order-evaluations/list';
        const { data, total } = await this.$axios.post(url, this.queryPayload);
        this.apiResult.data = data;
        this.apiResult.total = total;
      } catch (error) {
        this.$message.error(error.message || '查询内容审核数据失败');
      }
    },
    async moderateComment(row, status) {
      try {
        const { message } = await this.$axios.put(`/evaluations/moderate/${row.id}?status=${status}`);
        this.$message.success(message || '操作成功');
        this.fetchReportSummary();
        this.fetchFreshData();
      } catch (error) {
        this.$message.error(error.message || '评论审核失败');
      }
    },
    async moderateServiceEvaluation(row, status) {
      try {
        const { message } = await this.$axios.put(`/house-order-evaluations/moderate/${row.id}?status=${status}`);
        this.$message.success(message || '操作成功');
        this.fetchReportSummary();
        this.fetchFreshData();
      } catch (error) {
        this.$message.error(error.message || '服务评价审核失败');
      }
    },
    handleDelete(row, type) {
      this.deletedId = row.id;
      this.deletedType = type;
      this.dialogDeletedVisible = true;
    },
    async confirmDeleted() {
      try {
        const url = this.deletedType === 'comment'
          ? `/evaluations/${this.deletedId}`
          : `/house-order-evaluations/${this.deletedId}`;
        await this.$axios.delete(url);
        this.$message.success('删除成功');
        this.dialogDeletedVisible = false;
        this.deletedId = null;
        this.fetchReportSummary();
        this.fetchFreshData();
      } catch (error) {
        this.$message.error(error.message || '删除失败');
      }
    },
    handleSizeChange(size) {
      this.queryDto.size = size;
      this.queryDto.current = 1;
      this.fetchFreshData();
    },
    handleCurrentChange(current) {
      this.queryDto.current = current;
      this.fetchFreshData();
    }
  }
};
</script>

<style scoped lang="scss">
.container {
  margin: 10px 20px;
}

.top-header {
  margin-block: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;

  .nav-left,
  .nav-right {
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

.pager {
  margin-block: 20px;
}

.operation-links {
  display: flex;
  justify-content: center;
  gap: 14px;
  font-size: 13px;

  span {
    cursor: pointer;
    color: #409EFF;
  }

  .danger {
    color: #F56C6C;
  }
}

.score-text {
  color: #e6a23c;
  font-weight: 600;
}
</style>
