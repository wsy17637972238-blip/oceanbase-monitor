<template>
  <el-container>
    <el-header>
      <el-menu mode="horizontal" router :default-active="$route.path">
        <el-menu-item index="/dashboard">看板</el-menu-item>
        <el-menu-item index="/inspection">巡检</el-menu-item>
        <el-menu-item index="/alerts">告警</el-menu-item>
      </el-menu>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
</script>
