<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark px-3 mb-3">
    <RouterLink class="navbar-brand" to="/">SportClub</RouterLink>
    <button
      class="navbar-toggler"
      type="button"
      data-bs-toggle="collapse"
      data-bs-target="#navMenu"
    >
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse justify-content-center" id="navMenu">
      <div class="navbar-nav">
        <RouterLink class="nav-link" to="/">Kodu</RouterLink>
        <RouterLink class="nav-link" to="/">Tutvustus</RouterLink>
        <RouterLink class="nav-link" to="/">Treeningud</RouterLink>
        <a class="nav-link" href="#" @click.prevent="$emit('event-open-login-modal')">Logi sisse</a>
        <RouterLink class="nav-link" to="/register">Registreeru</RouterLink>
      </div>
    </div>
  </nav>

  <RouterView />
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>
