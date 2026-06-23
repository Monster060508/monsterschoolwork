<template>
  <router-view />
</template>

<script setup lang="ts">
// 应用根组件
</script>

<style>
/* 全局样式 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

#app {
  height: 100%;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}

::-webkit-scrollbar-track {
  background-color: #f5f5f5;
}
</style>