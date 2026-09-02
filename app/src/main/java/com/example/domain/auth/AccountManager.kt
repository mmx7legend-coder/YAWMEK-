package com.example.domain.auth

import com.example.data.local.dao.UserAccountDao
import com.example.data.local.model.UserAccountEntity
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.UUID

sealed class AuthResult {
    data class Success(val account: UserAccountEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AccountManager(private val userAccountDao: UserAccountDao) {

    val activeAccountFlow: Flow<UserAccountEntity?> = userAccountDao.getActiveAccountFlow()

    suspend fun getActiveAccount(): UserAccountEntity? = userAccountDao.getActiveAccount()

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun signUp(email: String, name: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return AuthResult.Error("Please enter a valid email address.")
        }
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters.")
        }

        val existing = userAccountDao.getAccountByEmail(cleanEmail)
        if (existing != null) {
            return AuthResult.Error("An account with this email already exists.")
        }

        userAccountDao.markAllLoggedOut()

        val newAccount = UserAccountEntity(
            id = UUID.randomUUID().toString(),
            email = cleanEmail,
            displayName = name.ifBlank { cleanEmail.substringBefore("@") },
            passwordHash = hashPassword(password),
            isLoggedIn = true,
            isCloudSyncEnabled = true,
            lastLoginMillis = System.currentTimeMillis(),
            createdAtMillis = System.currentTimeMillis(),
            token = "jwt_${UUID.randomUUID()}"
        )

        userAccountDao.insertOrUpdateAccount(newAccount)
        return AuthResult.Success(newAccount)
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        val account = userAccountDao.getAccountByEmail(cleanEmail)
            ?: return AuthResult.Error("No account found with this email. Please sign up.")

        if (account.passwordHash != hashPassword(password)) {
            return AuthResult.Error("Incorrect password. Please try again.")
        }

        userAccountDao.markAllLoggedOut()
        val loggedIn = account.copy(
            isLoggedIn = true,
            lastLoginMillis = System.currentTimeMillis()
        )
        userAccountDao.insertOrUpdateAccount(loggedIn)
        return AuthResult.Success(loggedIn)
    }

    suspend fun signOut() {
        userAccountDao.markAllLoggedOut()
    }

    suspend fun requestPasswordReset(email: String): String {
        val cleanEmail = email.trim().lowercase()
        val account = userAccountDao.getAccountByEmail(cleanEmail)
        return if (account != null) {
            "Password reset instructions have been prepared for $cleanEmail."
        } else {
            "If an account exists with $cleanEmail, instructions have been sent."
        }
    }

    suspend fun deleteAccount(accountId: String) {
        userAccountDao.deleteAccountById(accountId)
    }

    suspend fun updateProfile(name: String, cloudSyncEnabled: Boolean) {
        val current = getActiveAccount() ?: return
        val updated = current.copy(
            displayName = name.trim(),
            isCloudSyncEnabled = cloudSyncEnabled
        )
        userAccountDao.insertOrUpdateAccount(updated)
    }
}
