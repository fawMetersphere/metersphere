<template>
  <div v-if="taskStatus">
    <!--    侧边任务按钮-->
    <div class="parentBox" @click="toggle()">
      <div class="contentsBox">
        <div :style="openBox ? 'right: 0;width:120px;cursor: auto;' : ''">
          <font-awesome-icon class="icon global focusing" :icon="['fas', 'compass']" spin style="color: #ffffff;" />
          <span :style="openBox ? 'display: block;color: #fff;cursor: pointer;' : ''">{{$t('side_task.novice_task')}}</span>
        </div>
      </div>
    </div>
    <ms-site-task ref="siteTask" :taskData="taskData" @closeBox="closeBox"/>
  </div>
</template>

<script>
import MsSiteTask from "../../components/sidemenu/components/SiteTask";
import {getSideTask} from "../../api/novice";
import {TASK_DATA} from "../../utils/constants";


export default {
  name: "MsSideTask",
  components: { MsSiteTask },

  data() {
    return {
      taskStatus: false,
      openBox:false,
      totalTask: 0,
      taskData:[]
    };
  },
  created() {
    this.initTaskData()
  },
  methods: {
    initTaskData(){
      getSideTask().then(res=>{
        if(res.data.length > 0 && res.data[0].dataOption){
          this.taskData = JSON.parse(res.data[0].dataOption)
        }else{
          this.taskData = TASK_DATA
        }
        let microApp = JSON.parse(sessionStorage.getItem("micro_apps"));
        let num = 0
        let total = 0
        this.taskData.forEach(item =>{
          if(!(microApp && microApp[item.name])){
            item.status = -1
            total++
          } else {
            item.percentage = Math.floor(item.rate / item.taskData.length * 100)
            if(item.percentage === 100){
              item.status = 1
            }else if(100 > item.percentage && item.percentage > 0){
              item.status = 2
            }
            num += item.rate
          }
        })
        if(total < this.taskData.length){
          this.taskStatus = true
        }
        this.totalTask = num
      })
    },
    toggle(){
      this.openBox = true
      this.initTaskData()
      this.$refs.siteTask.open();
    },
    closeBox(status){
      this.openBox = status
    },
    skipOpen(path){
      this.initTaskData()
      this.$refs.siteTask.skipOpen(path);
    }
  }

}
</script>

<style scoped>

.parentBox {
  height: 100%;
  background: gainsboro;
  overflow: hidden;
  overflow-y: auto;
  z-index: 1000;
  position: fixed;
}
.parentBox .contentsBox div {
  transition: all 1s;
  position: fixed;
  right: 0;
  width: 27px;
  border-radius: 50px;
  background-color: #783787;
  color: #fff;
  padding: 3px;
  cursor: pointer;
  display: flex;
  align-items: center;
}
.parentBox .contentsBox div span {
  display: none;
  color: #fff;
}
.parentBox .contentsBox div span:last-child {
  margin-left: 10px;
}
.parentBox .contentsBox div:nth-child(1) {
  bottom: 100px;
}
.parentBox .contentsBox div:hover {
  right: 0;
  height: 28px;
  width: 120px;
  cursor: auto;
}

.parentBox .contentsBox div:hover span {
  display: block;
  color: #fff;
  cursor: pointer;
}
.parentBox .contentsBox div:not(:last-child) {
  border-bottom: 1px solid #fff;
}

/*隐藏浏览器滚动条*/
::-webkit-scrollbar {
  display: none;
}

.icon {
  width: 2em !important;
  height: 2em;
}

</style>
