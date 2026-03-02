<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
  </div>
</template>

<script setup>
defineProps({
  msg: {
    type: String,
    default: 'Hello Vue 3'
  }
})
</script>

<style scoped>
.hello {
  padding: 20px;
}
</style>
