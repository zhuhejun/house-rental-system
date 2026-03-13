<template>
    <div class="save-container">
        <div>
            <h3>我收藏的房屋信息</h3>
            <MyHouseSaveList />
        </div>
        <div>
            <h3>我收藏的房屋资讯</h3>
            <MyHouseNewsSaveList />
        </div>
    </div>
</template>

<script>
import MyHouseSaveList from "@/views/user/MyHouseSaveList"
import MyHouseNewsSaveList from "@/views/user/MyHouseNewsSaveList"
export default {
    components: { MyHouseSaveList,MyHouseNewsSaveList },
    data() {
        return {

        }
    },
    created() {

    },
    methods: {

    }
}
</script>

<style scoped lang="scss">

.save-container{
    background-color: rgb(255,255,255);
    padding: 20px 60px;
}

</style>