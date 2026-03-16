<template>
  <div></div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

onMounted(() => {
  // 重定向到登录页面，并设置 tab 为注册
  router.replace('/login');
});
</script>
