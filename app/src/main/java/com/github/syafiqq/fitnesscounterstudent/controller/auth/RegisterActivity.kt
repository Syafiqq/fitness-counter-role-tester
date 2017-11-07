package com.github.syafiqq.fitnesscounterstudent.controller.auth

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.android.synthetic.main.activity_register.*
import timber.log.Timber

/**
 * A login screen that offers login via email/password.
 */
class RegisterActivity: AppCompatActivity(), OnCompleteListener<AuthResult>, View.OnClickListener
{
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: MaterialDialog

    override fun onCreate(savedInstanceState: Bundle?)
    {
        Timber.d("onCreate")

        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_register)
        this.setupActionBar()

        this.auth = FirebaseAuth.getInstance()
        this.dialog = MaterialDialog.Builder(this)
                .canceledOnTouchOutside(false)
                .content(super.getResources().getString(R.string.label_please_wait))
                .progress(true, 0)
                .build()

        this.edittext_password_conf.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL)
            {
                this.onClick(null)
                return@OnEditorActionListener true
            }
            false
        })
        this.button_sumbit.setOnClickListener(this)
        this.button_login.setOnClickListener { this.onLoginClick() }
    }

    override fun onDestroy()
    {
        Timber.d("onDestroy")

        super.onDestroy()
        this.dialog.dismiss()
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setupActionBar() = supportActionBar?.setDisplayHomeAsUpEnabled(true)

    private fun isEmailValid(email: String) = email.contains("@")

    private fun isPasswordValid(password: String) = password.length >= 4

    private fun isPasswordSame(password: String, passwordConf: String) = TextUtils.equals(password, passwordConf)

    private fun onLoginClick() = onBackPressed()

    override fun onClick(view: View?)
    {
        Timber.d("onClick")

        // Reset errors.
        edittext_email.error = null
        edittext_password.error = null
        edittext_password_conf.error = null

        // Store values at the time of the login attempt.
        val email = edittext_email.text.toString()
        val password = edittext_password.text.toString()
        val passwordConf = edittext_password_conf.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
        {
            edittext_password.error = getString(R.string.error_invalid_password)
            focusView = edittext_password
            cancel = true
        }
        else if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordConf) && !isPasswordSame(password, passwordConf))
        {
            edittext_password.error = getString(R.string.error_password_not_same)
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
            this.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, this)
        }
    }

    override fun onComplete(result: Task<AuthResult>)
    {
        Timber.d("onComplete")

        this.dialog.dismiss()
        if (result.isSuccessful)
        {
            Timber.d("Success Register")
            Toast.makeText(this, super.getResources().getString(R.string.label_register_success), Toast.LENGTH_SHORT).show()
            Handler(mainLooper).postDelayed({
                super@RegisterActivity.setResult(RESULT_OK, Intent().apply {
                    this.putExtra(RegisterActivity.EMAIL, this@RegisterActivity.edittext_email.text.toString())
                    this.putExtra(RegisterActivity.PASSWORD, this@RegisterActivity.edittext_password.text.toString())
                })
                this.onLoginClick()
            }, 1000)
        }
        else
        {
            Timber.d("Failed Register")
            Timber.e(result.exception)
            Toast.makeText(this, super.getResources().getString(
                    when (result.exception)
                    {
                        is FirebaseAuthWeakPasswordException       -> R.string.label_auth_weak_password
                        is FirebaseAuthInvalidCredentialsException -> R.string.label_auth_email_malformed
                        is FirebaseAuthUserCollisionException      -> R.string.label_auth_email_exists
                        else                                       -> R.string.label_register_failed
                    }
            ), Toast.LENGTH_SHORT).show()
        }
    }

    companion object
    {
        val EMAIL = "email"
        val PASSWORD = "password"
    }
}
