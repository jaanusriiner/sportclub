<template>
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-12 col-lg-8 home-text">
        <h1 class="home-headline">Liikumine on elu</h1>
        <p class="home-lead">
          Iga samm, iga trenn, iga kord kui valid liikumise paigalseisu asemel — see on investeering
          sinu tervisesse, energiasse ja heaolusse.
        </p>
        <p class="home-closing">Alustamiseks pole vaja täiuslikku hetke. Piisab ühest klikist.</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeView',
}
</script>

<style scoped>
.home-text {
  padding-top: 40px;
}

.home-headline {
  font-family: 'Manrope', 'Familjen Grotesk', ui-sans-serif, system-ui, sans-serif;
  font-weight: 200;
  font-size: 64px;
  letter-spacing: -0.035em;
  line-height: 1;
  margin-bottom: 32px;
}

.home-lead {
  font-size: 20px;
  color: var(--sc-muted);
  margin-bottom: 24px;
  max-width: 640px;
}

.home-closing {
  font-size: 20px;
  font-weight: 500;
  max-width: 640px;
}

@media (max-width: 640px) {
  .home-headline {
    font-size: 44px;
  }
}
</style>
