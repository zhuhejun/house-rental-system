<template>
    <div class="reply-container">
        <div class="content-area">
            <h1 style="margin-top: 0;">申请成为房东</h1>
            <p>*请上传身份证正面照</p>
            <div>
                <img style="width: 200px;height: 140px;" v-if="idcardFront" :src="idcardFront || ''" alt="">
                <el-upload class="idcard-uploader" action="api/v1.0/house-rental-api/file/upload"
                    :show-file-list="false" :on-success="handleImageSuccess">
                    <i class="el-icon-camera-solid"></i>
                </el-upload>
            </div>
            <p>*请上传身份证反面照</p>
            <div>
                <img style="width: 200px;height: 140px;" v-if="idcardOpposite" :src="idcardOpposite || ''" alt="">
                <el-upload class="idcard-uploader" action="api/v1.0/house-rental-api/file/upload"
                    :show-file-list="false" :on-success="handleImageOppsiteSuccess">
                    <i class="el-icon-camera-solid"></i>
                </el-upload>
            </div>
            <p>*身份证号</p>
            <el-input placeholder="请输入身份证号" v-model="idcard" show-password></el-input>
            <div style="margin-block: 20px;">
                <el-button @click="reply" plain>立即提交申请</el-button>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            idcardFront: null, // 身份证正面照
            idcardOpposite: null, // 身份证反面照
            idcard: null,
        }
    },
    created() {

    },
    methods: {
        handleImageOppsiteSuccess(res, file) {
            // 通知提示
            this.$notify({
                title: '身份证证件反面照上传',
                type: res.code === 200 ? 'success' : 'error',
                message: res.code === 200 ? '上传成功' : res.data,
                position: 'buttom-right',
                suration: 1000,
            })
            if (res.code === 200) {
                this.idcardOpposite = res.data; // 响应里面的data，即后端返回的上传后的图片链接
            }
        },
        handleImageSuccess(res, file) {
            // 通知提示
            this.$notify({
                title: '身份证证件正面照上传',
                type: res.code === 200 ? 'success' : 'error',
                message: res.code === 200 ? '上传成功' : res.data,
                position: 'buttom-right',
                suration: 1000,
            })
            if (res.code === 200) {
                this.idcardFront = res.data; // 响应里面的data，即后端返回的上传后的图片链接
            }
        },
        async reply() {
            try {
                const landlord = {
                    idcardFront: this.idcardFront, // 设置身份证正面照
                    idcardOpposite: this.idcardOpposite, // 设置身份证反面照
                    idcard: this.idcard, // 设置身份证号
                };
                const { message } = await this.$axios.post('/landlord/save', landlord);
                this.$message.success(message);
                this.$router.push('/user');
            } catch (error) {
                this.$notify({
                    title: '填写校验',
                    type:  'info',
                    message: error.message,
                    position: 'buttom-right',
                    suration: 1000,
                })
                console.log("查询房东申请信息异常：", error);
            }
        },
    }
}
</script>

<style scoped lang="scss">
.reply-container {
    background-color: rgb(250, 250, 250);
    width: 100%;
    height: 100vh;

    .content-area {
        width: 600px;
        margin: 0 auto;
        padding: 20px 40px;
        box-sizing: border-box;
        background-color: rgb(255, 255, 255);
        min-height: 100vh;
    }
}
</style>