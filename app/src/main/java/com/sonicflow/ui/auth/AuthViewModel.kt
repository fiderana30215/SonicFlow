package com.sonicflow.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing authentication state and operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {
    
    // Email state
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    // Password state
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    // Name state (for sign up)
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Update email field
     */
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        _error.value = null
    }
    
    /**
     * Update password field
     */
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        _error.value = null
    }
    
    /**
     * Update name field
     */
    fun updateName(newName: String) {
        _name.value = newName
        _error.value = null
    }
    
    /**
     * Sign in user with email and password
     */
    fun signIn(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Validate inputs
            if (_email.value.isBlank()) {
                _error.value = "Email is required"
                _isLoading.value = false
                return@launch
            }
            
            if (_password.value.isBlank()) {
                _error.value = "Password is required"
                _isLoading.value = false
                return@launch
            }
            
            if (!isValidEmail(_email.value)) {
                _error.value = "Invalid email format"
                _isLoading.value = false
                return@launch
            }
            
            try {
                // TODO: Implement actual authentication logic
                // Simulating network call
                delay(1500)
                
                // For now, accept any valid email/password combination
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Sign in failed"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Sign up new user with name, email, and password
     */
    fun signUp(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Validate inputs
            if (_name.value.isBlank()) {
                _error.value = "Name is required"
                _isLoading.value = false
                return@launch
            }
            
            if (_email.value.isBlank()) {
                _error.value = "Email is required"
                _isLoading.value = false
                return@launch
            }
            
            if (_password.value.isBlank()) {
                _error.value = "Password is required"
                _isLoading.value = false
                return@launch
            }
            
            if (!isValidEmail(_email.value)) {
                _error.value = "Invalid email format"
                _isLoading.value = false
                return@launch
            }
            
            if (_password.value.length < 6) {
                _error.value = "Password must be at least 6 characters"
                _isLoading.value = false
                return@launch
            }
            
            try {
                // TODO: Implement actual registration logic
                // Simulating network call
                delay(1500)
                
                // For now, accept any valid inputs
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Sign up failed"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
