<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand">
        <span class="brand-title">OceanBase 智能巡检</span>
        <span class="brand-sub">Intelligent Inspection</span>
      </div>
      <el-menu
        mode="horizontal"
        router
        :default-active="$route.path"
        background-color="#001529"
        text-color="#a6adb4"
        active-text-color="#ffffff"
        class="nav-menu"
      >
        <el-menu-item index="/dashboard">看板</el-menu-item>
        <el-menu-item index="/inspection">巡检</el-menu-item>
        <el-menu-item index="/alerts">告警</el-menu-item>
      </el-menu>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
</script>

<style>
html,
body,
#app {
  margin: 0;
  height: 100%;
  background-color: #f0f2f5;
}

.layout {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  background-color: #001529;
  padding: 0 24px;
}

.brand {
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-right: 48px;
  line-height: 1.2;
}

.brand-title {
  color: #ffffff;
  font-size: 17px;
  font-weight: 600;
}

.brand-sub {
  color: #5c6b7a;
  font-size: 11px;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.main {
  padding: 20px 24px;
}
</style>
