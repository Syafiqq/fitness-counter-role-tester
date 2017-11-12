package com.github.syafiqq.fitnesscounterstudent.controller.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.syafiqq.fitnesscounterstudent.R
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber

@Suppress("UNUSED_PARAMETER")
/**
 * A login screen that offers login via email/password.
 */
class LoginActivity: AppCompatActivity()
{
    private lateinit var dialog: MaterialDialog
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?)
    {
        Timber.d("onCreate")

        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_login)

        this.populateField("syafiq.rezpector@gmail.com", "12345678")

        this.auth = FirebaseAuth.getInstance()
        this.dialog = MaterialDialog.Builder(this)
                .canceledOnTouchOutside(false)
                .content(super.getResources().getString(R.string.label_please_wait))
                .progress(true, 0)
                .build()

        this.edittext_password.setOnEditorActionListener(this::onEditorActionClicked)
        this.button_sumbit.setOnClickListener(this::onSubmitButtonClicked)
        this.button_register.setOnClickListener(this::onRegisterButtonClicked)
    }

    override fun onDestroy()
    {
        Timber.d("onDestroy")

        super.onDestroy()
        this.dialog.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode)
        {
            LOGIN_CALLBACK -> when (resultCode)
            {
                Activity.RESULT_OK ->
                {
                    this.populateField(
                            data?.getStringExtra(RegisterActivity.EMAIL) ?: "",
                            data?.getStringExtra(RegisterActivity.PASSWORD) ?: ""
                    )
                }
            }
        }
    }

    private fun isEmailValid(email: String) = email.contains("@")

    private fun isPasswordValid(password: String) = password.length >= 4

    private fun populateField(email: String, password: String)
    {
        Timber.d("Populate Field [$email - $password]")
        this.edittext_email.setText(email)
        this.edittext_password.setText(password)
    }

    private fun onEditorActionClicked(view: TextView?, id: Int, event: KeyEvent?): Boolean
    {
        return when (id)
        {
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_NULL ->
            {
                this.onSubmitButtonClicked(null)
                true
            }
            else                -> false
        }
    }

    private fun onRegisterButtonClicked(view: View?) = super.startActivityForResult(Intent(this, RegisterActivity::class.java), LOGIN_CALLBACK)

    private fun onSubmitButtonClicked(view: View?)
    {
        Timber.d("onSubmitButtonClicked")

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
            this.dialog.show()
            this.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, this::onAuthComplete)
        }
    }

    private fun onAuthComplete(result: Task<AuthResult>)
    {
        Timber.d("onAuthComplete")

        this.dialog.dismiss()
        if (result.isSuccessful)
        {
            Timber.d("Success Login")
        }
        else
        {
            Timber.d("Failed Login")

            Timber.e(result.exception)
            Toast.makeText(this, super.getResources().getString(
                    when (result.exception!!)
                    {
                        is FirebaseAuthInvalidUserException        -> R.string.label_auth_email_not_exists
                        is FirebaseAuthWeakPasswordException       -> R.string.label_auth_weak_password
                        is FirebaseAuthInvalidCredentialsException -> R.string.label_auth_invalid_credential
                        is FirebaseAuthUserCollisionException      -> R.string.label_auth_email_exists
                        is FirebaseNetworkException                -> R.string.label_auth_network_issue
                        else                                       -> R.string.label_login_failed
                    }
            ), Toast.LENGTH_SHORT).show()
        }
    }

    companion object
    {
        val LOGIN_CALLBACK = 0x01
    }
}
