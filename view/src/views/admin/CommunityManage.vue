<template>
  <div class="container">
    <div class="top-header">
      <div class="nav-left">
        <p style="font-size: 12px;color: rgb(130,130,130);">省份</p>
        <el-select size="mini" @change="handleAreaChange" style="width: 100%;" v-model="topAreaId" placeholder="不限">
          <el-option v-for="item in topArea" :key="item.id" :label="item.name" :value="item.id">
          </el-option>
        </el-select>
        <p style="font-size: 12px;color: rgb(130,130,130);">市区</p>
        <el-select size="mini" style="width: 100%;" v-model="queryCityAreaId" placeholder="不限">
          <el-option v-for="item in cityArea" :key="item.id" :label="item.name" :value="item.id">
          </el-option>
        </el-select>
        <div style="text-align: center;" class="primary-bt" @click="reset">
          重置
        </div>
      </div>
      <div class="nav-right">
        <div>
          <AutoInput placeholder="搜索小区" @listener="listener" />
        </div>
        <div class="primary-bt" @click="saveCommunityOperation">
          <i class="el-icon-plus"></i>
          新增小区
        </div>
      </div>
    </div>
    <!-- 表格及分页信息 -->
    <div>
      <el-table :data="apiResult.data">
        <el-table-column width="200" prop="username" label="小区名">
          <template #default="scope">
            <div class="over-text">
              {{ scope.row.name }}
            </div>
          </template>
        </el-table-column>
        <el-table-column width="100" prop="topAreaName" label="所在省份"></el-table-column>
        <el-table-column prop="cityAreaName" width="108" label="所在城市"></el-table-column>
        <el-table-column prop="createTime" :sortable="true" width="168" label="创建时间"></el-table-column>
        <el-table-column label="" align="center">
          <template #default="scope">
            <div class="operate-buttons">
              <el-dropdown trigger="click" placement="bottom-end">
                <span class="el-dropdown-link">
                  <i class="el-icon-more"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click.native="handleEdit(scope.row)" icon="el-icon-edit">修改</el-dropdown-item>
                  <el-dropdown-item @click.native="handleDetail(scope.row)"
                    icon="el-icon-finished">详情</el-dropdown-item>
                  <el-dropdown-item @click.native="handleDelete(scope.row)" icon="el-icon-delete">删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页组件区域 -->
      <div class="pager">
        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
          :current-page="communityQueryDto.current" :page-sizes="[10, 20]" :page-size="communityQueryDto.size"
          layout="total, sizes, prev, pager, next, jumper" :total="apiResult.total"></el-pagination>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <el-dialog title="删除小区信息" :show-close="false" :visible.sync="dialogDeletedVisible" width="20%">
      <span>确定删除小区信息数据？</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="dialogDeletedVisible = false">取消</el-button>
        <el-button size="mini" type="primary" @click="confirmDeleted">确定</el-button>
      </span>
    </el-dialog>


    <!-- 小区详情信息 -->
    <el-dialog title="小区详情信息" :show-close="false" :visible.sync="dialogCommunityVisible" width="70%">
      <div style="display: flex;gap: 30px;">
        <div>
          <div>
            <p style="color: aliceblue;background-color: rgb(55, 171, 33);padding: 10px 20px;margin-bottom: 0;">*小区封面图
            </p>
            <img style="width: 100%;height: 200px;border-radius: 0px;" :src="community.cover" alt="" srcset="">
          </div>
          <div>
            <p style="color: aliceblue;background-color: rgb(55, 171, 33);padding: 10px 20px;margin-bottom: 0;">*实况图</p>
            <Carousel :showBtn="false" containerHeight="200px" :carouselItems="carouselItems" />
          </div>
        </div>

        <div>
          <p style="color: aliceblue;background-color: rgb(55, 171, 33);padding: 10px 20px;margin-bottom: 0;">*介绍</p>
          <div v-html="community.detail"></div>
        </div>
      </div>
    </el-dialog>

    <!-- 小区信息抽屉 -->
    <el-drawer title="新增小区" :modal="false" :wrapperClosable="false" :visible.sync="drawer" :direction="direction"
      size="40%" :before-close="handleClose">
      <div style="margin-inline: 20px;">
        <div>
          <el-steps :active="active" finish-status="success">
            <el-step title="补充地区信息"></el-step>
            <el-step title="补充小区基本信息"></el-step>
            <el-step title="补充小区介绍"></el-step>
          </el-steps>
        </div>
        <div>
          <!-- 小区地区信息 -->
          <div v-if="active === 0">
            <div>
              <p>*所属省份</p>
              <el-select @change="handleAreaChange" style="width: 100%;" v-model="topAreaId" placeholder="请选择">
                <el-option v-for="item in topArea" :key="item.id" :label="item.name" :value="item.id">
                </el-option>
              </el-select>
              <p>*所属市区</p>
              <el-select style="width: 100%;" v-model="cityAreaId" placeholder="请选择">
                <el-option v-for="item in cityArea" :key="item.id" :label="item.name" :value="item.id">
                </el-option>
              </el-select>
            </div>
          </div>
          <!-- 小区基本信息 -->
          <div v-if="active === 1">
            <div>
              <p>*小区标题</p>
              <el-input v-model="community.name" placeholder="请输入内容"></el-input>
            </div>
            <div>
              <p>*小区封面</p>
              <div class="user-avatar">
                <p>点击📷处即可上传小区封面</p>
                <img v-if="cover" style="width: 200px;height: 140px;border-radius: 5px;" :src="cover || ''" alt="">
                <el-upload class="avatar-uploader" action="api/v1.0/house-rental-api/file/upload"
                  :show-file-list="false" :on-success="handleImageSuccess">
                  <i class="el-icon-camera-solid"></i>
                </el-upload>
              </div>
            </div>
          </div>
          <!-- 小区介绍 -->
          <div v-if="active === 2">
            <div>
              <p>*小区实况图</p>
              <el-upload :on-success="handleCovers" action="api/v1.0/house-rental-api/file/upload"
                list-type="picture-card" :on-preview="handlePictureCardPreview" :on-remove="handleRemove">
                <i class="el-icon-plus"></i>
              </el-upload>
              <el-dialog :modal="false" :visible.sync="dialogVisible">
                <img style="z-index: 1000;" width="100%" :src="dialogImageUrl" alt="">
              </el-dialog>
            </div>
            <div>
              <p>*补充小区介绍</p>
              <Editor :receiveContent="content" height="300px" api="api/v1.0/house-rental-api/file/upload"
                @on-listener="onListener" />
            </div>
          </div>
        </div>
        <div style="display: flex;margin-block: 20px;">
          <div v-if="active !== 0" class="primary-bt" @click="last" style="text-align: center;">
            <i class="el-icon-caret-left"></i>
            上一步
          </div>
          <div v-if="active !== 2" class="primary-bt" @click="next" style="text-align: center;">
            <i class="el-icon-caret-right"></i>
            下一步
          </div>
          <div v-if="active === 2" class="info-bt" @click="saveCommunity" style="text-align: center;">
            新增小区信息
          </div>
        </div>
      </div>
    </el-drawer>

  </div>
</template>

<script>
// B站 「程序辰星」原创出品
import AutoInput from "@/components/AutoInput.vue"; // 自己封装好的输入框组件
import Tab from "@/components/Tab.vue";
import Editor from "@/components/Editor.vue";
import Carousel from "@/components/Carousel.vue";
export default {
  components: { AutoInput, Tab, Editor, Carousel }, // 注册组件
  data() {
    return {
      dialogCommunityVisible: false,
      dialogImageUrl: '',
      content: '',
      carouselItems: [],
      queryCityAreaId: null,
      dialogVisible: false,
      topAreaId: null,
      cityAreaId: null,
      topArea: [],
      cityArea: [],
      cover: '', // 小区封面
      active: 0,
      drawer: false,
      direction: 'rtl',
      id: null, // 页面即将删除的数据ID
      apiResult: { // 后端返回的查询数据的响应数据
        data: [], // 数据项
        total: 0, // 符合条件的数据总想 - 初始赋值为0
      },
      community: {}, // 小区信息
      communityQueryDto: { // 搜索条件
        current: 1, // 当前页 - 初始是第一页
        size: 10, // 页面显示大小 - 初始是10条
      },
      coverList: [], // 小区实况图图片链接数组
      dialogDeletedVisible: false, // 删除弹窗控制开关变量 - 初始是关（false）
    };
  },
  watch: {
    queryCityAreaId(newVal, oldValue) {
      this.communityQueryDto.areaId = newVal;
      this.fetchFreshData();
    }
  },
  created() {
    this.fetchFreshData();
    this.fetchTopArea();
  },
  methods: {
    handleEdit(data) {
      localStorage.setItem('communityInfo',JSON.stringify(data));
      this.$router.push('/community-update');
    },
    reset() {
      this.communityQueryDto.areaId = null;
      this.topAreaId = null;
      this.queryCityAreaId = null;
      this.fetchFreshData();
    },
    handleCityQuery() {
      console.log(this.queryCityAreaId);
    },
    saveCommunityOperation() {
      this.handleClose();
      this.drawer = true;
    },
    onListener(text) {
      this.content = text;
    },
    handleCovers(response, file, fileList) {
      this.coverList.push(response.data);
      console.log("上传，此时的图片数组：", this.coverList);
    },
    handleRemove(file, fileList) {
      if (fileList.length === 0) return;
      this.coverList = this.coverList.filter(entity => entity !== fileList[0].response.data);
      console.log("移除，此时的图片数组：", this.coverList);
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url;
      this.dialogVisible = true;
    },
    // 新增小区信息
    async saveCommunity() {
      try {
        this.community.cover = this.cover;
        this.community.detail = this.content;
        this.community.areaId = this.cityAreaId;
        this.community.covers = this.coverList.length === 0 ? null : this.coverList.join(',');
        await this.$axios.post('/community/save', this.community);
        this.handleClose();
        this.$notify({
          title: '小区新增',
          type: 'success',
          message: '小区新增成功',
          position: 'buttom-right',
          suration: 1000,
        })
        this.fetchFreshData();
      } catch (error) {
        console.log("新增小区信息异常：", error);
        this.$notify({
          title: '小区新增',
          type: 'info',
          message: error.message,
          position: 'buttom-right',
          suration: 1000,
        })
      }
    },
    // 封面上传响应
    handleImageSuccess(res, file) {
      // 通知提示
      this.$notify({
        title: '封面上传',
        type: res.code === 200 ? 'success' : 'error',
        message: res.code === 200 ? '上传成功' : res.data,
        position: 'buttom-right',
        suration: 1000,
      })
      if (res.code === 200) {
        this.cover = res.data; // 响应里面的data，即后端返回的上传后的图片链接
      }
    },
    async fetchTopArea() {
      try {
        const areaQueryDto = { parentId: 0 }
        const { data } = await this.$axios.post('/area/list', areaQueryDto);
        this.topArea = data;
      } catch (error) {
        console.log("查询省份信息异常：", error);
      }
    },
    async handleAreaChange() {
      this.cityAreaId = null;
      try {
        const areaQueryDto = { parentId: this.topAreaId }
        const { data } = await this.$axios.post('/area/list', areaQueryDto);
        this.cityArea = data;
      } catch (error) {
        console.log("查询省份下的市区信息异常：", error);
      }
    },
    last() {
      if (this.active-- <= 0) this.active = 2;
    },
    next() {
      if (this.active++ > 2) this.active = 0;
    },
    handleChange(val) {
      this.communityQueryDto.isAudit = Number(val.value);
      this.fetchFreshData();
    },
    handleClose() {
      this.drawer = false;
      this.cover = '';
      this.active = 0;
      this.covers = [];
      this.coverList = [];
      this.topAreaId = null;
      this.cityAreaId = null;
      this.content = '';
      this.community = {};
    },
    handleDetail(data) {
      this.dialogCommunityVisible = true;
      this.community = data;
      this.carouselItems = data.covers.split(',').map(entity => {
        return {
          image: entity,
          title: data.name,
          subtitle: data.topAreaName + "·" + data.cityAreaName
        }
      });
    },
    // 输入框组件输入回传
    listener(text) {
      this.communityQueryDto.name = text; // 赋值查询条件的内容
      this.fetchFreshData(); // 重新加载数据
    },
    async auditcommunity() {
      try {
        const community = {
          id: this.community.id,
          isAudit: true,
        }
        await this.$axios.put('/community/update', community);
        this.$message.success('审核成功');
        this.drawer = false; // 关闭详情抽屉
        this.fetchFreshData(); // 重新加载小区数据
      } catch (error) {
        this.$message.info(error.message);
        console.error('审核小区信息信息异常:', error);
      }
    },
    // 查询小区信息数据
    async fetchFreshData() {
      try {
        const { data, total } = await this.$axios.post('/community/list', this.communityQueryDto);
        this.apiResult.data = data;
        this.apiResult.total = total;
      } catch (error) {
        console.error('查询小区信息信息异常:', error);
      }
    },
    // 分页 - 处理页面页数切换
    handleSizeChange(size) {
      this.communityQueryDto.size = size; // 当前页面大小重置
      this.communityQueryDto.currrent = 1; // 当前页设置为第一页
      this.fetchFreshData(); // 重新加载页面数据
    },
    // 分页 - 处理页面当前页切换
    handleCurrentChange(current) {
      this.communityQueryDto.current = current; // 当前页选中
      this.fetchFreshData(); // 重新加载页面数据
    },
    // 表格点击删除小区信息
    handleDelete(row) {
      this.dialogDeletedVisible = true; // 开启删除弹窗确认
      this.id = row.id;
    },
    // 小区信息删除
    async confirmDeleted() {
      try {
        const { code } = await this.$axios.delete(`/community/${this.id}`);
        if (code === 200) {
          this.$notify.success({
            title: '小区信息删除',
            message: '删除成功',
            position: 'buttom-right',
            suration: 1000,
          });
          this.dialogDeletedVisible = false; // 关闭删除确认弹窗
          this.id = null; // 将标识ID置位
          this.fetchFreshData(); // 删除小区信息数据之后，重新加载小区信息数据
        }
      } catch (error) {
        console.log("删除小区信息数据异常：", error);
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