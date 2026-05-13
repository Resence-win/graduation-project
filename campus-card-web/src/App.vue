<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.el-table {
  --campus-table-header-font-size: 14px;
  --campus-table-body-font-size: 15px;
  --campus-table-line-height: 22px;
  --campus-table-cell-padding: 8px 0;
}

.el-table th.el-table__cell {
  height: 48px;
  padding: 0;
  font-size: var(--campus-table-header-font-size);
  line-height: var(--campus-table-line-height);
  font-weight: 600;
}

.el-table td.el-table__cell {
  height: 56px;
  padding: var(--campus-table-cell-padding);
  font-size: var(--campus-table-body-font-size);
  line-height: var(--campus-table-line-height);
}

.el-table .cell {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  line-height: var(--campus-table-line-height);
}

.el-table th.el-table__cell > .cell {
  font-size: var(--campus-table-header-font-size);
}

.el-table td.el-table__cell > .cell {
  font-size: var(--campus-table-body-font-size);
}

.el-table .el-tag,
.el-table .el-button,
.el-table .el-image {
  vertical-align: middle;
}

.el-table .el-image {
  width: 44px !important;
  height: 44px !important;
}
</style>
