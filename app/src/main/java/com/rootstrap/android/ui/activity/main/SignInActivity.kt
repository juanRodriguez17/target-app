package com.rootstrap.android.ui.activity.main

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.rootstrap.android.R
import com.rootstrap.android.databinding.ActivitySignInBinding
import com.rootstrap.android.metrics.Analytics
import com.rootstrap.android.metrics.PageEvents
import com.rootstrap.android.metrics.VISIT_SIGN_IN
import com.rootstrap.android.network.models.User
import com.rootstrap.android.ui.view.AuthView
import com.rootstrap.android.util.NetworkState
import com.rootstrap.android.util.extensions.value
import com.rootstrap.android.util.permissions.PermissionActivity
import com.rootstrap.android.util.permissions.PermissionResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_sign_in.*

@AndroidEntryPoint
class SignInActivity : PermissionActivity(), AuthView {

    private val viewModel: SignInActivityViewModel by viewModels()
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)

        setContentView(binding.root)
        Analytics.track(PageEvents.visit(VISIT_SIGN_IN))

        binding.apply {
            signInButton.setOnClickListener { signIn() }
            signUpButton.setOnClickListener { signOut() }
            emailEditText.setOnFocusChangeListener { _, _ -> changeBackground(binding.emailEditText) }
            passwordEditText.setOnFocusChangeListener { _, _ -> changeBackground(binding.passwordEditText) }
        }

        lifecycle.addObserver(viewModel)

        setObservers()
    }

    override fun showProfile() {
        startActivityClearTask(ProfileActivity())
    }

    private fun signIn() {
        with(binding) {
            val user = User(
                email = emailEditText.value(),
                password = passwordEditText.value()
            )
            viewModel.signIn(user)
        }
    }

    private fun signOut() {
        startActivityClearTask(SignUpActivity())
    }

    private fun setObservers() {
        viewModel.state.observe(this, Observer {
            when (it) {
                SignInState.signInFailure -> showError(viewModel.error)
                SignInState.signInSuccess -> showProfile()
            }
        })

        viewModel.networkState.observe(this, Observer {
            when (it) {
                NetworkState.loading -> showProgress()
                NetworkState.idle -> hideProgress()
                else -> showError(viewModel.error ?: getString(R.string.default_error))
            }
        })
    }

    override fun showError(message: String?) {
        sign_in_message_error.visibility = View.VISIBLE
        email_edit_text.background = resources.getDrawable(R.drawable.edittext_backgorund_error)
        password_edit_text.background = resources.getDrawable(R.drawable.edittext_backgorund_error)
    }

    private fun changeBackground(view: View) {
        view.background = resources.getDrawable(R.drawable.edittext_background)
        binding.signInMessageError.visibility = View.GONE
    }
}
