package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Data.AuthState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthService(private val supabase: SupabaseClient) {

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        try { supabase.auth.signOut() } catch (_: Exception) {}
    }

    suspend fun restoreSession(): AuthState {
        return try {
            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                val email = session.user?.email ?: ""
                AuthState.Authenticated(session.user?.id ?: "", email)
            } else {
                AuthState.Unauthenticated
            }
        } catch (_: Exception) {
            AuthState.Unauthenticated
        }
    }

    fun currentAuthState(): AuthState {
        val session = supabase.auth.currentSessionOrNull() ?: return AuthState.Unauthenticated
        return AuthState.Authenticated(session.user?.id ?: "", session.user?.email ?: "")
    }

    suspend fun getCurrentUserId(): String? = supabase.auth.currentSessionOrNull()?.user?.id
}
