import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class EmailPasswordActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()

    fun createAccount(email: String, password: String) {
        // Validate email
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showErrorMessage("Please enter a valid email address")
            return
        }

        // Validate password
        if (password.length < 6) {
            showErrorMessage("Password must be at least 6 characters")
            return
        }

        // Show loading indicator
        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Hide loading indicator
                showLoading(false)

                if (task.isSuccessful) {
                    // Sign up success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    showSuccessMessage("Account created successfully!")
                    navigateToHomeScreen()
                } else {
                    // Sign up failed, handle specific errors
                    handleSignUpError(task.exception)
                }
            }
    }

    private fun handleSignUpError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                showErrorMessage("Password is too weak. Please choose a stronger password.")
            }
            is FirebaseAuthInvalidCredentialsException -> {
                showErrorMessage("Invalid email format. Please check your email address.")
            }
            is FirebaseAuthUserCollisionException -> {
                showErrorMessage("An account with this email already exists. Please sign in instead.")
            }
            else -> {
                showErrorMessage("Account creation failed: ${exception?.message ?: "Unknown error"}")
            }
        }
    }

    // Helper functions (you'll need to implement these based on your UI framework)
    private fun showLoading(loading: Boolean) {
        // Implement your loading state (e.g., show/hide progress bar)
    }

    private fun showErrorMessage(message: String) {
        // Show error message to user (e.g., Snack bar, Toast, or error text field)
        println("Error: $message")
    }

    private fun showSuccessMessage(message: String) {
        // Show success message to user
        println("Success: $message")
    }

    private fun navigateToHomeScreen() {
        // Navigate to your app's main screen
        println("Navigating to home screen...")
    }
}