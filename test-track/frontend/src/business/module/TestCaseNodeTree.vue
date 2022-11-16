<template>
  <div>
    <slot name="header"></slot>
    <ms-node-tree
      v-loading="loading"
      :tree-nodes="treeNodes"
      :type="'edit'"
      :name-limit="100"
      :delete-permission="['PROJECT_TRACK_CASE:READ+DELETE']"
      :add-permission="['PROJECT_TRACK_CASE:READ+CREATE']"
      :update-permission="['PROJECT_TRACK_CASE:READ+EDIT']"
      default-label="未规划用例"
      local-suffix="test_case"
      @add="add"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @nodeSelectEvent="nodeChange"
      @refresh="list"
      @filter="filter"
      ref="nodeTree">
      <template v-slot:header>
        <ms-search-bar
          :show-operator="showOperator"
          :condition="condition"
          :commands="operators"/>
        <module-trash-button
          :condition="condition"
          :total="total"
          :exe="enableTrash"/>
        <module-public-button
          :condition="condition"
          :public-total="publicTotal"
          :exe="enablePublic"/>
      </template>
    </ms-node-tree>
    <test-case-import
      @refreshAll="importRefresh"
      ref="testCaseImport"/>
    <test-case-export
      @refreshAll="refreshAll"
      @exportTestCase="exportTestCase"
      ref="testCaseExport"/>
    <test-case-create
      :tree-nodes="treeNodes"
      @saveAsEdit="saveAsEdit"
      @createCase="createCase"
      @refresh="refresh"
      ref="testCaseCreate"/>

    <is-change-confirm
      :tip="$t('test_track.case.minder_import_save_confirm_tip')"
      @confirm="changeConfirm"
      ref="isChangeConfirm"/>

  </div>

</template>

<script>
import MsNodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import TestCaseCreate from "@/business/case/components/TestCaseCreate";
import TestCaseImport from "@/business/case/components/import/TestCaseImport";
import TestCaseExport from "@/business/case/components/export/TestCaseExport";
import MsSearchBar from "metersphere-frontend/src/components/search/MsSearchBar";
import {buildTree, buildNodePath} from "metersphere-frontend/src/model/NodeTree";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import ModuleTrashButton from "metersphere-frontend/src/components/ModuleTrashButton";
import {getTestCaseNodesByCaseFilter} from "@/api/testCase";
import IsChangeConfirm from "metersphere-frontend/src/components/IsChangeConfirm";
import ModulePublicButton from "metersphere-frontend/src/components/module/ModulePublicButton";
import {useStore} from "@/store";
import {
  testCaseNodeAdd,
  testCaseNodeDelete,
  testCaseNodeDrag,
  testCaseNodeEdit,
  testCaseNodePos
} from "@/api/test-case-node";

export default {
  name: "TestCaseNodeTree",
  components: {
    IsChangeConfirm,
    MsSearchBar,
    TestCaseImport,
    TestCaseExport,
    TestCaseCreate,
    MsNodeTree,
    ModuleTrashButton,
    ModulePublicButton
  },
  data() {
    return {
      defaultProps: {
        children: "children",
        label: "label"
      },
      loading: false,
      treeNodes: [],
      condition: {
        filterText: "",
        trashEnable: false,
        publicEnable: false
      },
      operators: [
        {
          label: this.$t('test_track.case.create'),
          callback: this.addTestCase,
          permissions: ['PROJECT_TRACK_CASE:READ+CREATE']
        },
        {
          label: this.$t('api_test.api_import.label'),
          callback: this.handleImport,
          permissions: ['PROJECT_TRACK_CASE:READ+IMPORT']
        },
        {
          label: this.$t('api_test.export_config'),
          callback: this.handleExport,
          permissions: ['PROJECT_TRACK_CASE:READ+EXPORT']
        }
      ],
      currentNode: {}
    };
  },
  props: {
    type: {
      type: String,
      default: "view"
    },
    showOperator: Boolean,
    total: Number,
    publicTotal: Number,
    caseCondition: Object
  },
  watch: {
    treeNodes() {
      this.$emit('setTreeNodes', this.treeNodes);
    },
    'condition.filterText'() {
      this.filter();
    },
  },
  mounted() {
    let store = useStore();
    store.testCaseSelectNode = {};
    store.setTestCaseSelectNodeIds = [];
    this.list();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    addTestCase() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$refs.testCaseCreate.open(this.currentModule)
    },
    filter() {
      this.$refs.nodeTree.filter(this.condition.filterText);
    },
    saveAsEdit(data) {
      this.$emit('saveAsEdit', data);
    },
    createCase(data) {
      this.$emit('createCase', data);
    },
    refresh() {
      this.$emit("refreshTable");
    },
    refreshAll() {
      this.$emit('refreshAll');
    },
    importRefresh() {
      this.$emit('importRefresh');
    },
    enableTrash() {
      this.condition.trashEnable = true;
      // 隐藏公共用例库背景色
      this.condition.publicEnable = false;
      this.$emit('enableTrash', this.condition.trashEnable);
      this.$emit('toPublic', 'trash');
    },
    enablePublic() {
      this.condition.publicEnable = true;
      this.condition.trashEnable = false;
      this.$emit('enablePublic', this.condition.publicEnable);
      this.$emit('toPublic', 'public');
    },
    list() {
      if (this.projectId) {
        this.caseCondition.casePublic = false;
        this.loading = true;
        getTestCaseNodesByCaseFilter(this.projectId, this.caseCondition)
          .then(r => {
            this.loading = false;
            this.treeNodes = r.data;
            this.treeNodes.forEach(node => {
              node.name = node.name === '未规划用例' ? this.$t('api_test.unplanned_case') : node.name
              buildTree(node, {path: ''});
            });
            this.setModuleOptions();
            if (this.$refs.nodeTree) {
              this.$refs.nodeTree.filter(this.condition.filterText);
            }
            this.setCurrentKey();
          });
      }
    },
    setCurrentKey() {
      if (this.$refs.nodeTree) {
        this.$refs.nodeTree.setCurrentKey(this.currentNode);
      }
    },
    increase(id) {
      this.$refs.nodeTree.increase(id);
    },
    decrease(id) {
      this.$refs.nodeTree.decrease(id);
    },
    edit(param) {
      param.projectId = this.projectId;
      testCaseNodeEdit(param)
        .then(() => {
          this.$success(this.$t('commons.save_success'));
          this.list();
          this.$emit("refreshTable");
        });
    },
    add(param) {
      param.projectId = this.projectId;
      testCaseNodeAdd(param)
        .then(() => {
          this.$success(this.$t('commons.save_success'));
          this.list();
        });
    },
    handleImport() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      if (!this.openMinderConfirm()) {
        this.$refs.testCaseImport.open();
      }
    },
    handleExport() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$emit('handleExportCheck')
    },
    openExport() {
      this.$refs.testCaseExport.open();
    },
    exportTestCase(type, param) {
      this.$emit('exportTestCase', type, param);
    },
    remove(nodeIds) {
      testCaseNodeDelete(nodeIds)
        .then(() => {
          this.list();
          this.$emit("refreshTable")
        });
    },
    drag(param, list) {
      testCaseNodeDrag(param)
        .then(() => {
          testCaseNodePos(list)
          this.list();
        });
    },
    setModuleOptions() {
      let moduleOptions = [];
      this.treeNodes.forEach(node => {
        buildNodePath(node, {path: ''}, moduleOptions);
      });
      useStore().testCaseModuleOptions = moduleOptions;
    },
    nodeChange(node, nodeIds, pNodes) {
      let store = useStore();
      store.testCaseSelectNode = node;
      store.testCaseSelectNodeIds = nodeIds;
      this.condition.trashEnable = false;
      this.condition.publicEnable = false;
      this.currentModule = node.data;
      this.currentNode = node;

      this.$emit("nodeSelectEvent", node, node.data.id === 'root' ? [] : nodeIds, pNodes);
      // 只在TAB页切换时才刷新树
      // this.nohupReloadTree(node.data.id);
    },
    nohupReloadTree(selectNodeId) {
      if (this.projectId) {
        this.caseCondition.casePublic = false;
        getTestCaseNodesByCaseFilter(this.projectId, this.caseCondition)
          .then(r => {
            this.treeNodes = r.data;
            this.treeNodes.forEach(node => {
              node.name = node.name === '未规划用例' ? this.$t('api_test.unplanned_case') : node.name
              buildTree(node, {path: ''});
            });
            this.$nextTick(() => {
              if (this.$refs.nodeTree) {
                this.$refs.nodeTree.filter(this.condition.filterText);
                if (selectNodeId) {
                  this.$refs.nodeTree.justSetCurrentKey(selectNodeId);
                }
              }
            })
          });
      }
    },
    openMinderConfirm() {
      let isTestCaseMinderChanged = useStore().isTestCaseMinderChanged;
      if (isTestCaseMinderChanged) {
        this.$refs.isChangeConfirm.open();
      }
      return isTestCaseMinderChanged;
    },
    changeConfirm(isSave) {
      this.$emit('importChangeConfirm', isSave);
    },
    closeExport() {
      this.$refs.testCaseExport.close();
    }
  }
};
</script>

<style scoped>

</style>