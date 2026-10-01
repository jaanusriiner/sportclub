<script>
export default {
  name: 'CapacityBar',
  props: {
    userCount: Number,
    maxSize: Number,
  },
  computed: {
    percent() {
      return this.maxSize > 0 ? Math.min(100, (this.userCount / this.maxSize) * 100) : 0
    },
    isFull() {
      return this.userCount >= this.maxSize
    },
  },
}
</script>

<template>
  <div class="sc-cap">
    <span class="sc-strong">{{ userCount }}/{{ maxSize }}</span>
    <span class="sc-bar" :class="{ full: isFull }"><i :style="{ width: percent + '%' }"></i></span>
  </div>
</template>
