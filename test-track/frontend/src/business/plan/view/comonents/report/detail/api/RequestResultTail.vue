<template>
  <div class="request-result" v-loading="loading">
    <ms-request-metric v-if="showMetric" :response="response" />
    <ms-response-result
      :currentProtocol="currentProtocol"
      :response="response"
      :isTestPlan="isTestPlan"
    />
  </div>
</template>

<script>
import MsResponseResult from "./ResponseResult";
import MsRequestMetric from "./RequestMetric";

export default {
  name: "MsRequestResultTail",
  components: { MsRequestMetric, MsResponseResult },
  props: {
    response: Object,
    currentProtocol: String,
    reportId: String,
    showMetric: {
      type: Boolean,
      default() {
        return true;
      },
    },
    isTestPlan: {
      type: Boolean,
      default() {
        return false;
      },
    },
  },
  data() {
    return {
      loading: false,
      report: {},
    };
  },
};
</script>

<style scoped>
.request-result {
  width: 100%;
  min-height: 40px;
  padding: 2px 0;
}

.request-result .info {
  background-color: #f9f9f9;
  margin-left: 20px;
  cursor: pointer;
}

.request-result .method {
  color: #1e90ff;
  font-size: 14px;
  font-weight: 500;
  line-height: 40px;
  padding-left: 5px;
}

.request-result .url {
  color: #7f7f7f;
  font-size: 12px;
  font-weight: 400;
  margin-top: 4px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
}

.request-result .tab .el-tabs__header {
  margin: 0;
}

.request-result .text {
  height: 300px;
  overflow-y: auto;
}

.sub-result .info {
  background-color: #fff;
}

.sub-result .method {
  border-left: 5px solid #1e90ff;
  padding-left: 20px;
}

.sub-result:last-child {
  border-bottom: 1px solid #ebeef5;
}

.request-result .icon.is-active {
  transform: rotate(90deg);
}
</style>
