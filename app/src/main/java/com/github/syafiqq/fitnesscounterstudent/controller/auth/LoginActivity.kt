package com.github.syafiqq.fitnesscounterstudent.controller.auth

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.syafiqq.fitnesscounterstudent.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity: AppCompatActivity(), OnCompleteListener<AuthResult>
{

    private lateinit var dialog: MaterialDialog
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_login)

        this.auth = FirebaseAuth.getInstance()
        this.dialog = MaterialDialog.Builder(this)
                .canceledOnTouchOutside(false)
                .content(super.getResources().getString(R.string.label_please_wait))
                .progress(true, 0)
                .build()

        this.edittext_password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL)
            {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        this.button_sumbit.setOnClickListener { attemptLogin() }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        this.dialog.dismiss()
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin()
    {
        // Reset errors.
        edittext_email.error = null
        edittext_password.error = null

        // Store values at the time of the login attempt.
        val email = edittext_email.text.toString()
        val password = edittext_password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
        {
            edittext_password.error = getString(R.string.error_invalid_password)
            focusView = edittext_password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email))
        {
            edittext_email.error = getString(R.string.error_field_required)
            focusView = edittext_email
            cancel = true
        }
        else if (!isEmailValid(email))
        {
            edittext_email.error = getString(R.string.error_invalid_email)
            focusView = edittext_email
            cancel = true
        }

        if (cancel)
        {
            focusView?.requestFocus()
        }
        else
        {
            this.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, this)
        }
    }

    private fun onAccountAvailable(user: FirebaseUser?)
    {
        fun createSnackbar(user: FirebaseUser?, dialog: MaterialDialog)
        {
            val snackbar = Snackbar.make(constraintlayout_root, "Akun Butuh Verifikasi", Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction("Verifikasi !", {
                dialog.show()
                user?.sendEmailVerification()
                        ?.addOnCompleteListener(this) {
                            snackbar.dismiss()
                            dialog.dismiss()
                            if (it.isSuccessful)
                            {
                                Timber.d("Success Send To ${user.email}")
                                Toast.makeText(this, super.getResources().getString(R.string.label_verification_success), Toast.LENGTH_SHORT).show()
                            }
                            else
                            {
                                Timber.d(it.exception)
                                Toast.makeText(this, super.getResources().getString(R.string.label_verification_failed), Toast.LENGTH_SHORT).show()
                                createSnackbar(user, dialog)
                            }
                        }
            })
            snackbar.show()
        }

        if (user?.isEmailVerified == true)
        {
            Timber.d("Email Verified")
        }
        else
        {
            Timber.d("Need Verification")
            createSnackbar(user, dialog)
        }
    }

    private fun isEmailValid(email: String): Boolean
    {
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean
    {
        return password.length >= 8
    }

    override fun onComplete(result: Task<AuthResult>)
    {
        dialog.dismiss()
        if (result.isSuccessful)
        {
            Timber.d("Success Login")
            this@LoginActivity.onAccountAvailable(this@LoginActivity.auth.currentUser)
        }
        else
        {
            Timber.e(result.exception)
            Toast.makeText(this@LoginActivity, super@LoginActivity.getResources().getString(R.string.label_login_failed), Toast.LENGTH_SHORT).show()
        }
    }
}
