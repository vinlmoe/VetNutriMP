package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.AuthState
import fr.vetbrain.vetnutri_mp.Service.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authService: AuthService) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _savedEmail = MutableStateFlow("")
    val savedEmail: StateFlow<String> = _savedEmail.asStateFlow()

    init {
        viewModelScope.launch {
            _savedEmail.value = authService.loadLastEmail()
            _state.value = authService.restoreSession()
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = authService.signIn(email, password)
            if (result.isSuccess) {
                val session = authService.restoreSession()
                if (session is AuthState.Authenticated) {
                    authService.saveLastEmail(email)
                    _savedEmail.value = email
                }
                _state.value = session
            } else {
                _state.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Erreur de connexion")
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val result = authService.signUp(email, password)
            if (result.isSuccess) {
                val session = authService.restoreSession()
                _state.value = when (session) {
                    is AuthState.Authenticated -> {
                        authService.saveLastEmail(email)
                        _savedEmail.value = email
                        session
                    }
                    else -> AuthState.ConfirmationPending(email)
                }
            } else {
                _state.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Erreur de création de compte")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            _state.value = AuthState.Unauthenticated
        }
    }

    fun resetToUnauthenticated() {
        _state.value = AuthState.Unauthenticated
    }
}
