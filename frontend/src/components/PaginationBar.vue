<script>
export default {
  name: 'PaginationBar',
  props: {
    currentPage: {
      type: Number,
      default: 1,
    },
    totalPages: {
      type: Number,
      default: 1,
    },
  },
  emits: ['event-page-selected'],
  computed: {
    pageNumbers() {
      return Array.from({ length: this.totalPages }, (_, index) => index + 1)
    },
  },
  methods: {
    selectPage(page) {
      if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
        this.$emit('event-page-selected', page)
      }
    },
  },
}
</script>

<template>
  <nav v-if="totalPages > 1" aria-label="Lehekülgede valik">
    <ul class="pagination justify-content-center mb-0">
      <li class="page-item" :class="{ disabled: currentPage === 1 }">
        <button type="button" class="page-link" @click="selectPage(currentPage - 1)">
          Eelmine
        </button>
      </li>
      <li
        v-for="page in pageNumbers"
        :key="page"
        class="page-item"
        :class="{ active: page === currentPage }"
      >
        <button
          type="button"
          class="page-link"
          :aria-current="page === currentPage ? 'page' : null"
          @click="selectPage(page)"
        >
          {{ page }}
        </button>
      </li>
      <li class="page-item" :class="{ disabled: currentPage === totalPages }">
        <button type="button" class="page-link" @click="selectPage(currentPage + 1)">
          Järgmine
        </button>
      </li>
    </ul>
  </nav>
</template>
