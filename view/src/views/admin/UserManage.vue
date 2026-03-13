<template>
  <div class="container">
    <div class="top-header">
      <div class="nav-left">
        <Tab :buttons="[
          { label: '全部', value: 'null' },
          { label: '管理员', value: '1' },
          { label: '普通用户', value: '2' }
        ]" initialActive="null" @change="handleChange" />
      </div>
      <div class="nav-right">
        <div>
          <AutoInput placeholder="搜索用户" @listener="listener" />
        </div>
        <div class="primary-bt" @click="dialogVisible = true">
          <i class="el-icon-plus"></i>
          新增用户
        </div>
      </div>
    </div>
    <!-- 表格及分页信息 -->
    <div>
      <el-table :data="apiResult.data">
        <el-table-column width="240" prop="username" label="用户">
          <template #default="scope">
            <div class="over-text">
              <img width="20px" height="20px" style="border-radius: 50%;" :src="scope.row.avatar" alt="">
              {{ scope.row.username }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="account" :sortable="true" width="118" label="账号"></el-table-column>
        <el-table-column prop="email" :sortable="true" width="158" label="邮件">
          <template #default="scope">
            <div class="over-text">
              {{ scope.row.email }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" :sortable="true" width="88" label="性别">
          <template #default="scope">
            <div>{{ scope.row.gender === 1 ? '女' : '男' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" :sortable="true" width="128" label="联系电话"></el-table-column>
        <el-table-column prop="birthday" :sortable="true" width="128" label="出生年月"></el-table-column>
        <el-table-column label="" align="center">
          <template #default="scope">
            <div class="operate-buttons">
              <el-dropdown trigger="click" placement="bottom-end">
                <span class="el-dropdown-link">
                  <i class="el-icon-more"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click.native="handleEdit(scope.row)" icon="el-icon-edit">
                    修改信息
                  </el-dropdown-item>
                  <el-dropdown-item @click.native="handleDelete(scope.row)"
                    icon="el-icon-delete">删除用户</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页组件区域 -->
      <div class="pager">
        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
          :current-page="userQueryDto.current" :page-sizes="[10, 20]" :page-size="userQueryDto.size"
          layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
      </div>
    </div>

    <!-- 用户信息操作弹窗 -->
    <el-dialog :title="dialogControlOperation ? '新增用户信息' : '修改用户信息'" :show-close="false" :visible.sync="dialogVisible"
      :closeOnClickModal="false" width="45%">
      <el-tabs v-model="tabActiveName" :tab-position="tabPosition" style="height: 420px;">
        <el-tab-pane label="核心信息" name="first">
          <div>
            <div class="user-avatar">
              <p>点击📷处即可上传头像</p>
              <img v-if="avatar" :src="avatar || ''" alt="">
              <el-upload class="avatar-uploader" action="api/v1.0/house-rental-api/file/upload" :show-file-list="false"
                :on-success="handleImageSuccess">
                <i class="el-icon-camera-solid"></i>
              </el-upload>
            </div>
            <div>
              <p>*用户账号</p>
              <span v-if="!dialogControlOperation"
                style="display: inline-block;margin-bottom: 10px;font-size: 10px;">账号一经注册，不可修改</span>
              <el-input :disabled="!dialogControlOperation" placeholder="输入" v-model="apiParam.account" clearable>
              </el-input>
            </div>
            <div>
              <p>*用户名</p>
              <el-input placeholder="输入" v-model="apiParam.username" clearable>
              </el-input>
            </div>
            <div>
              <p>*登录密码</p>
              <el-input show-password placeholder="输入" type="password" v-model="password" clearable>
              </el-input>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="基本信息" name="second">
          <div>
            <div>
              <p>*用户角色</p>
              <Tab :buttons="[
                { label: '管理员', value: '1' },
                { label: '普通用户', value: '2' }
              ]" :initialActive="String(apiParam.role)" @change="handleRoleChange" />
            </div>
            <div>
              <p>用户性别</p>
              <Tab :buttons="[
                { label: '女', value: '1' },
                { label: '男', value: '2' }
              ]" :initialActive="String(apiParam.gender) || '2'" @change="handleGenderChange" />
            </div>
            <div>
              <p>电子邮件</p>
              <el-input placeholder="输入" v-model="apiParam.email" clearable>
              </el-input>
            </div>
            <div>
              <p>联系电话</p>
              <el-input placeholder="输入" v-model="apiParam.phone" clearable>
              </el-input>
            </div>
            <div>
              <p>出生年月</p>
              <el-date-picker style="width: 100%;" v-model="apiParam.birthday" type="date" placeholder="选择日期">
              </el-date-picker>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <span slot="footer" class="dialog-footer">
        <span class="primary-bt" @click="cancelOperation">取消</span>
        <span class="info-bt" @click="handleConfirm">
          {{ dialogControlOperation ? '确定新增' : '确定修改' }}
        </span>
      </span>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog title="删除用户" :show-close="false" :visible.sync="dialogDeletedVisible" width="20%">
      <span>确定删除用户数据？</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="dialogDeletedVisible = false">取消</el-button>
        <el-button size="mini" type="primary" @click="confirmDeleted">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// B站 「程序员辰星」原创出品
import AutoInput from "@/components/AutoInput.vue"; // 导入封装好的输入框组件
import Tab from "@/components/Tab" // 导入封装好的Tab组件
export default {
  components: { AutoInput, Tab }, // 注册组件
  data() {
    return {
      tabActiveName: 'first', // 操作弹窗初始tab页选中第一项
      tabPosition: 'right', // 操作弹窗排布靠右
      id: null, // 页面即将删除的数据ID
      apiResult: { // 后端返回的查询数据的响应数据
        data: [], // 数据项
        total: 0, // 符合条件的数据总想 - 初始赋值为0
      },
      userQueryDto: { // 搜索条件
        current: 1, // 当前页 - 初始是第一页
        size: 10, // 页面显示大小 - 初始是10条
        role: null, // 用户角色 - null：查询全部；1：管理员；2普通用户
        username: null, // 用户名
      },
      dialogDeletedVisible: false, // 删除弹窗控制开关变量 - 初始是关（false）
      dialogVisible: false, // 操作弹窗控制开关变量 - 初始是关（false）
      dialogControlOperation: true, // 弹窗当前操作标识 - true：新增；false：修改
      apiParam: {}, // 传送进后端的数据 - 用于新增与修改场景
      avatar: '', // 用户头像
      password: '', // 用户密码
    };
  },
  created() {
    this.fetchFreshData(); // 页面创建时，先去加载用户数据
  },
  methods: {
    handleConfirm() {
      // 判断行为类型 - 判断是新增操作还是修改操作
      this.dialogControlOperation ? this.saveUserOperation() : this.updateUserOperation();
    },
    // 账号状态切换
    handleIsLoginChange(obj) {
      this.apiParam.isLogin = Number(obj.value);
    },
    // 禁言状态切换
    handleIsWordChange(obj) {
      this.apiParam.isWord = Number(obj.value);
    },
    // 角色切换
    handleRoleChange(obj) {
      this.apiParam.role = Number(obj.value);
    },
    // 性别选择切换
    handleGenderChange(obj) {
      this.apiParam.gender = Number(obj.value);
    },
    // 修改用户信息操作
    async updateUserOperation() {
      try {
        this.apiParam.avatar = this.avatar; // 头像赋值处理
        this.apiParam.password = !this.password ? null : this.$md5(this.$md5(this.password)); // 没输入就是不用改，否则就是密码二次md5加密
        const { data, message } = await this.$axios.put('/user/backUpdate', this.apiParam);
        this.apiParam = data;
        this.cancelOperation(); // 状态置位
        this.$message.success(message); // 消息提示
        this.fetchFreshData(); // 重新加载列表数据
      } catch (error) {
        this.$message.warning(error.message);
      }
    },
    // 保存用户信息操作
    async saveUserOperation() {
      try {
        if (!this.password) { // 密码未输入的情况
          this.$message.warning('密码不能为空哦');
          return;
        }
        this.apiParam.avatar = this.avatar; // 头像赋值处理
        this.apiParam.password = this.$md5(this.$md5(this.password)); // 密码二次md5加密
        const { message } = await this.$axios.post('/user/save', this.apiParam);
        this.cancelOperation(); // 状态置位
        this.$message.success(message); // 消息提示
        this.fetchFreshData(); // 重新加载列表数据
      } catch (error) {
        this.$message.warning(error.message);
      }
    },
    // 取消处理
    cancelOperation() {
      this.apiParam = {}; // 将参数对象清置
      this.avatar = ''; // 用户头像清置
      this.password = '';// 密码信息清空
      this.tabActiveName = 'first'; // 操作列表恢复默认选中第一项
      this.dialogVisible = false; // 关闭用户新增弹窗
      this.dialogControlOperation = true; // 操作标识变量设置为最初的状态，即true，为新增
    },
    // 头像上传响应
    handleImageSuccess(res, file) {
      // 通知提示
      this.$notify({
        title: '头像上传',
        type: res.code === 200 ? 'success' : 'error',
        message: res.code === 200 ? '上传成功' : res.data,
        position: 'buttom-right',
        suration: 1000,
      })
      if (res.code === 200) {
        this.avatar = res.data; // 响应里面的data，即后端返回的上传后的图片链接
      }
    },
    // 用户角色状态选中事件
    handleChange(obj) {
      this.userQueryDto.role = Number(obj.value); // 转成数值类型，再赋值
      this.fetchFreshData(); // 重新加载用户数据
    },
    // 输入框组件输入回传
    listener(text) {
      this.userQueryDto.username = text; // 赋值查询条件的内容
      this.fetchFreshData(); // 重新加载数据
    },
    // 查询用户数据
    async fetchFreshData() {
      try {
        const { data, total } = await this.$axios.post('/user/query', this.userQueryDto);
        this.apiResult.data = data;
        this.apiResult.total = total;
      } catch (error) {
        console.error('查询用户信息异常:', error);
      }
    },
    // 分页 - 处理页面页数切换
    handleSizeChange(size) {
      this.userQueryDto.size = size; // 当前页面大小重置
      this.userQueryDto.currrent = 1; // 当前页设置为第一页
      this.fetchFreshData(); // 重新加载页面数据
    },
    // 分页 - 处理页面当前页切换
    handleCurrentChange(current) {
      this.userQueryDto.current = current; // 当前页选中
      this.fetchFreshData(); // 重新加载页面数据
    },
    // 表格点击修改用户
    handleEdit(row) {
      this.cancelOperation();
      this.apiParam = { ...row }; // 当前操作用户信息
      this.dialogControlOperation = false; // 标识为修改操作
      this.dialogVisible = true; // 打开操作面板
      this.avatar = row.avatar; // 头像手动赋值
    },
    // 表格点击删除用户
    handleDelete(row) {
      this.dialogDeletedVisible = true; // 开启删除弹窗确认
      this.id = row.id;
    },
    // 用户删除
    async confirmDeleted() {
      try {
        const { code } = await this.$axios.delete(`/user/${this.id}`);
        if (code === 200) {
          this.$notify.success({
            title: '用户删除',
            message: '删除成功',
            position: 'buttom-right',
            duration: 1000,
          });
          this.dialogDeletedVisible = false; // 关闭删除确认弹窗
          this.id = null; // 将标识ID置位
          this.fetchFreshData(); // 删除用户数据之后，重新加载用户数据
        }
      } catch (error) {
        console.log("删除用户数据异常：", error);
      }
    }
  },
};
</script>
<style scoped lang="scss">
.pager {
  margin-block: 20px;
}

/* 默认隐藏操作按钮 */
.operate-buttons {
  opacity: 0;
  transition: opacity 0.3s;
  /* 添加过渡效果 */
  cursor: pointer;

  i {
    padding: 8px;
    border-radius: 6px;
    transition: all .5s ease;

    &:hover {
      background-color: rgb(236, 237, 238);
    }
  }

}

/* 行悬停时显示操作按钮 */
.el-table__body tr:hover .operate-buttons {
  opacity: 1;
}

.container {
  margin: 10px 20px;
}

.top-header {
  margin-block: 10px;
  padding-inline: 10px;
  border-radius: 5px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .nav-left,
  .nav-right {
    display: flex;
    justify-content: left;
    align-items: center;
    gap: 10px;
  }

  .nav-left {
    display: flex;
  }

}
</style>