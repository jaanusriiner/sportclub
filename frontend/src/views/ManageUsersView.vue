<script>
import AlertDanger from '@/components/alert/AlertDanger.vue'
import AlertSuccess from '@/components/alert/AlertSuccess.vue'
import BaseModal from '@/components/modal/BaseModal.vue'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import SportclubService from '@/services/SportclubService.js'
import UserService from '@/services/UserService.js'

const ROLES = [
  { roleName: 'customer', label: 'Klient' },
  { roleName: 'trainer', label: 'Treener' },
  { roleName: 'admin', label: 'Admin' },
]

export default {
  name: 'ManageUsersView',
  components: { AlertDanger, AlertSuccess, BaseModal },
  beforeMount() {
    this.getUsers()
    this.getSportclubs()
  },
  data() {
    return {
      adminId: SessionStorageService.getUserId(),
      roles: ROLES,
      usersAreLoading: true,
      users: [],
      sportclubs: [],
      searchText: '',
      selectedRoleName: '',
      selectedStatus: '',
      errorMessage: '',
      successMessage: '',
      sportclubsModalIsOpen: false,
      selectedUser: null,
      selectedSportclubIds: [],
      isSaving: false,
    }
  },
  computed: {
    filteredUsers() {
      const searchText = this.searchText.trim().toLowerCase()
      return this.users
        .filter((user) => this.selectedRoleName === '' || user.roleName === this.selectedRoleName)
        .filter((user) => this.selectedStatus === '' || user.status === this.selectedStatus)
        .filter(
          (user) =>
            searchText === '' ||
            [this.getFullName(user), user.email, user.phoneNumber]
              .join(' ')
              .toLowerCase()
              .includes(searchText),
        )
    },

    hasActiveFilters() {
      return this.searchText !== '' || this.selectedRoleName !== '' || this.selectedStatus !== ''
    },
  },
  methods: {
    getUsers() {
      UserService.getUsersRequest(this.adminId)
        .then((response) => (this.users = response.data))
        .catch(() => NavigationService.navigateToErrorView())
        .finally(() => (this.usersAreLoading = false))
    },

    getSportclubs() {
      SportclubService.getSportclubsRequest()
        .then((response) => (this.sportclubs = response.data))
        .catch(() => NavigationService.navigateToErrorView())
    },

    getFullName(user) {
      return [user.firstName, user.lastName].filter(Boolean).join(' ')
    },

    getSportclubNames(user) {
      return this.sportclubs
        .filter((sportclub) => user.sportclubIds.includes(sportclub.sportclubId))
        .map((sportclub) => sportclub.sportclubName)
        .join(', ')
    },

    isCurrentAdmin(user) {
      return user.userId === this.adminId
    },

    clearFilters() {
      this.searchText = ''
      this.selectedRoleName = ''
      this.selectedStatus = ''
    },

    changeUserRole(user, roleName) {
      this.updateUser(user, roleName, user.status, `Kasutaja ${user.email} roll on muudetud`)
    },

    toggleUserStatus(user) {
      const newStatus = user.status === 'A' ? 'D' : 'A'
      const message =
        newStatus === 'A'
          ? `Kasutaja ${user.email} on aktiveeritud`
          : `Kasutaja ${user.email} on deaktiveeritud`
      this.updateUser(user, user.roleName, newStatus, message)
    },

    updateUser(user, roleName, status, successMessage) {
      this.resetMessages()
      UserService.putUserRequest(user.userId, { adminId: this.adminId, roleName, status })
        .then(() => this.handleUserUpdated(successMessage))
        .catch((error) => this.handleUpdateError(error))
    },

    openSportclubsModal(user) {
      this.resetMessages()
      this.selectedUser = user
      this.selectedSportclubIds = [...user.sportclubIds]
      this.sportclubsModalIsOpen = true
    },

    closeSportclubsModal() {
      this.sportclubsModalIsOpen = false
      this.selectedUser = null
    },

    saveUserSportclubs() {
      this.isSaving = true
      const email = this.selectedUser.email
      UserService.putUserSportclubsRequest(this.selectedUser.userId, {
        adminId: this.adminId,
        sportclubIds: this.selectedSportclubIds,
      })
        .then(() => {
          this.closeSportclubsModal()
          this.handleUserUpdated(`Treeneri ${email} spordiklubid on salvestatud`)
        })
        .catch((error) => {
          this.closeSportclubsModal()
          this.handleUpdateError(error)
        })
        .finally(() => (this.isSaving = false))
    },

    handleUserUpdated(successMessage) {
      this.successMessage = successMessage
      this.getUsers()
    },

    handleUpdateError(error) {
      // valik tabelis tuleb taastada serveri seisu järgi
      this.getUsers()
      if (error.response && [400, 403, 404].includes(error.response.status)) {
        this.errorMessage = error.response.data.message
      } else {
        NavigationService.navigateToErrorView()
      }
    },

    resetMessages() {
      this.errorMessage = ''
      this.successMessage = ''
    },
  },
}
</script>

<template>
  <div class="container">
    <div class="row mb-4">
      <div class="col">
        <p class="eyebrow">Admini vaade</p>
        <h1>Halda kasutajaid</h1>
      </div>
    </div>
    <AlertDanger :error-message="errorMessage" />
    <AlertSuccess :success-message="successMessage" />

    <div class="row g-2 mb-3">
      <div class="col-12 col-md-auto">
        <input
          v-model="searchText"
          type="search"
          class="form-control"
          placeholder="Otsi nime, e-posti või telefoni järgi..."
          aria-label="Otsi kasutajat"
        />
      </div>
      <div class="col-12 col-md-auto">
        <select v-model="selectedRoleName" class="form-select" aria-label="Roll">
          <option value="">Kõik rollid</option>
          <option v-for="role in roles" :key="role.roleName" :value="role.roleName">
            {{ role.label }}
          </option>
        </select>
      </div>
      <div class="col-12 col-md-auto">
        <select v-model="selectedStatus" class="form-select" aria-label="Staatus">
          <option value="">Kõik staatused</option>
          <option value="A">Aktiivne</option>
          <option value="D">Deaktiveeritud</option>
        </select>
      </div>
      <div class="col-auto">
        <button
          type="button"
          class="btn btn-outline-secondary"
          :disabled="!hasActiveFilters"
          @click="clearFilters"
        >
          Kustuta filtrid
        </button>
      </div>
    </div>

    <div class="table-responsive rounded shadow-sm border">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-dark">
          <tr>
            <th>Nimi</th>
            <th>E-post</th>
            <th>Telefon</th>
            <th>Roll</th>
            <th>Spordiklubid</th>
            <th>Staatus</th>
            <th class="text-end">Toimingud</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="user in filteredUsers"
            :key="user.userId"
            :class="{ 'text-muted': user.status !== 'A' }"
          >
            <td class="sc-strong">{{ getFullName(user) || '—' }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.phoneNumber || '—' }}</td>
            <td>
              <select
                :value="user.roleName"
                class="form-select form-select-sm role-select"
                :disabled="isCurrentAdmin(user)"
                :title="isCurrentAdmin(user) ? 'Iseenda rolli ei saa muuta' : ''"
                aria-label="Kasutaja roll"
                @change="changeUserRole(user, $event.target.value)"
              >
                <option v-for="role in roles" :key="role.roleName" :value="role.roleName">
                  {{ role.label }}
                </option>
              </select>
            </td>
            <td>
              <template v-if="user.roleName === 'trainer'">
                <span>{{ getSportclubNames(user) || '—' }}</span>
                <button
                  type="button"
                  class="btn btn-link btn-sm"
                  @click="openSportclubsModal(user)"
                >
                  Muuda
                </button>
              </template>
              <span v-else class="sc-sub">—</span>
            </td>
            <td>
              <span
                class="sc-pill"
                :class="user.status === 'A' ? 'status-active' : 'status-inactive'"
              >
                {{ user.status === 'A' ? 'Aktiivne' : 'Deaktiveeritud' }}
              </span>
            </td>
            <td class="text-end">
              <button
                v-if="!isCurrentAdmin(user)"
                type="button"
                class="btn btn-sm"
                :class="user.status === 'A' ? 'btn-outline-danger' : 'btn-outline-primary'"
                @click="toggleUserStatus(user)"
              >
                {{ user.status === 'A' ? 'Deaktiveeri' : 'Aktiveeri' }}
              </button>
            </td>
          </tr>
          <tr v-if="!usersAreLoading && filteredUsers.length === 0">
            <td colspan="7" class="text-center sc-sub py-4">Kasutajaid ei leitud</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-if="!usersAreLoading" class="sc-sub mt-2">
      Kuvatud {{ filteredUsers.length }} / {{ users.length }} kasutajat
    </p>

    <BaseModal :is-open="sportclubsModalIsOpen" @event-modal-closed="closeSportclubsModal">
      <template #title>Treeneri spordiklubid</template>
      <template #body>
        <div v-if="selectedUser">
          <p>
            Vali spordiklubid, kus
            <strong>{{ getFullName(selectedUser) || selectedUser.email }}</strong> treenerina
            tegutseb.
          </p>
          <div v-for="sportclub in sportclubs" :key="sportclub.sportclubId" class="form-check">
            <input
              :id="`sportclub-${sportclub.sportclubId}`"
              v-model="selectedSportclubIds"
              class="form-check-input"
              type="checkbox"
              :value="sportclub.sportclubId"
            />
            <label class="form-check-label" :for="`sportclub-${sportclub.sportclubId}`">
              {{ sportclub.sportclubName }}
            </label>
          </div>
        </div>
      </template>
      <template #buttons>
        <button
          type="button"
          class="btn btn-primary me-2"
          :disabled="isSaving"
          @click="saveUserSportclubs"
        >
          Salvesta
        </button>
      </template>
    </BaseModal>
  </div>
</template>

<style scoped>
.role-select {
  min-width: 120px;
}

.status-active {
  background-color: #e6f0ec;
  color: #2f5148;
}

.status-inactive {
  background-color: #f3e7e4;
  color: #8a3b2e;
}
</style>
