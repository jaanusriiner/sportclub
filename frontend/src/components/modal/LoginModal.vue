<script setup>
import { ref } from 'vue'
import BaseModal from '@/components/modal/BaseModal.vue'
// Märkus: Kui sul AlertDanger asub muus kaustas, muuda siin failiteed
// import AlertDanger from '@/components/alerts/AlertDanger.vue'

// 1. Võtame vastu info, kas modal peab avatud olema
defineProps({
  loginModalIsOpen: {
    type: Boolean,
    default: false,
  },
})

// 2. Sündmused, mida see aken välja saadab (ema-komponendile HomeView)
const emit = defineEmits(['event-login-modal-closed'])

// 3. Vormi väljade andmed (Reactive state)
const email = ref('')
const password = ref('')
const errorMessage = ref('') // Siia salvestame backendi "Vale e-post või parool"

// 4. Funktsioon: Sulge nupp (lähtestab väljad ja teatab emale)
function handleClose() {
  email.value = ''
  password.value = ''
  errorMessage.value = ''
  emit('event-login-modal-closed')
}

// 5. Funktsioon: Logi Sisse nupp (Saadab päringu)
function handleLogin() {
  errorMessage.value = '' // Nullime eelneva vea

  // Esialgne test-loogika, et saaksid ilma toimiva backendita pilti ette:
  console.log('Saadan andmed:', { email: email.value, password: password.value })

  if (email.value === 'admin@admin.ee' && password.value === '123') {
    // ÕNNESTUMINE (Simulatsioon):
    sessionStorage.setItem('userId', '1')
    sessionStorage.setItem('roleName', 'admin')
    alert('Login successful!') // Ajutine teade, kuni suunamiseni
    handleClose()
  } else {
    // EBAÕNNESTUMINE (Simulatsioon 403):
    errorMessage.value = 'Vale e-post või parool'
  }
}
</script>

<template>
  <!-- Kasutame sinu tehtud BaseModal komponenti -->
  <BaseModal :is-open="loginModalIsOpen" @event-modal-closed="handleClose">
    <!-- Pealkiri (läheb #title slotiti) -->
    <template #title> Sisselogimine </template>

    <!-- Sisu (läheb #body slotiti) -->
    <template #body>
      <!-- Kui eksisteerib veateade, kuvatakse see siin -->
      <!-- Kui AlertDanger.vue on valmis, vaheta see div selle vastu välja: <AlertDanger :message="errorMessage" /> -->
      <div v-if="errorMessage" class="alert alert-danger role-alert" role="alert">
        {{ errorMessage }}
      </div>

      <form @submit.prevent="handleLogin">
        <!-- E-posti väli -->
        <div class="mb-3 text-start">
          <label for="emailInput" class="form-label">E-post</label>
          <input
            id="emailInput"
            v-model="email"
            type="email"
            class="form-control"
            placeholder="Nimi@eesnimi.ee"
            required
          />
        </div>

        <!-- Parooli väli -->
        <div class="mb-3 text-start">
          <label for="passwordInput" class="form-label">Salasõna</label>
          <input
            id="passwordInput"
            v-model="password"
            type="password"
            class="form-control"
            placeholder="********"
            required
          />
        </div>
      </form>
    </template>

    <!-- Nupud (läheb #buttons slotiti, asuvad all paremal) -->
    <template #buttons>
      <button type="button" class="btn btn-primary me-2" @click="handleLogin">Logi Sisse</button>
    </template>
  </BaseModal>
</template>

<style scoped>
/* Siia võid vajadusel lisada spetsiifilist disaini */
</style>
