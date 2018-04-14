package com.github.syafiqq.fitnesscounter.role.tester.controller.auth

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.syafiqq.fitnesscounter.core.helpers.AuthHelper
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.model.Settings
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_register.*
import timber.log.Timber

class RegisterActivity: AppCompatActivity()
{
    private lateinit var dialog: MaterialDialog
    private lateinit var auth: FirebaseAuth

    override fun onCreate(state: Bundle?)
    {
        Timber.d("onCreate [$state]")

        super.onCreate(state)
        super.setContentView(R.layout.activity_register)
        this.setupActionBar()

        this.auth = FirebaseAuth.getInstance()
        this.dialog = MaterialDialog.Builder(this)
                .canceledOnTouchOutside(false)
                .content(super.getResources().getString(R.string.label_please_wait))
                .progress(true, 0)
                .build()

        this.edittext_password_conf.setOnEditorActionListener(this::onEditorActionClicked)
        this.button_submit.setOnClickListener(this::onSubmitButtonClicked)
        this.button_login.setOnClickListener(this::onLoginButtonClicked)
    }

    override fun onDestroy()
    {
        Timber.d("onDestroy")

        this.dialog.dismiss()
        super.onDestroy()
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setupActionBar() = supportActionBar?.setDisplayHomeAsUpEnabled(true)

    private fun isEmailValid(email: String) = email.contains("@")

    private fun isPasswordValid(password: String) = password.length >= 4

    private fun isPasswordSame(password: String, passwordConf: String) = TextUtils.equals(password, passwordConf)

    private fun onEditorActionClicked(view: TextView?, id: Int, event: KeyEvent?): Boolean
    {
        Timber.d("onEditorActionClicked [$view, $id, $event]")

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

    private fun onLoginButtonClicked(view: View?)
    {
        Timber.d("onLoginButtonClicked [$view]")

        onBackPressed()
    }

    private fun onSubmitButtonClicked(view: View?)
    {
        Timber.d("onSubmitButtonClicked [$view]")

        edittext_email.error = null
        edittext_password.error = null
        edittext_password_conf.error = null

        val email = edittext_email.text.toString()
        val password = edittext_password.text.toString()
        val passwordConf = edittext_password_conf.text.toString()

        var cancel = false
        var focusView: View? = null

        if ((!TextUtils.isEmpty(password) && !isPasswordValid(password)) || !isPasswordSame(password, passwordConf))
        {
            edittext_password.error = getString(
                    if (!isPasswordSame(password, passwordConf))
                        R.string.error_password_not_same
                    else
                        R.string.error_invalid_password)
            focusView = edittext_password
            cancel = true
        }

        if (TextUtils.isEmpty(email) || !isEmailValid(email))
        {
            edittext_email.error = getString(
                    if (TextUtils.isEmpty(email))
                        R.string.error_field_required
                    else
                        R.string.error_invalid_email)
            focusView = edittext_email
            cancel = true
        }

        if (cancel)
        {
            focusView?.requestFocus()
        }
        else
        {
            this.dialog.setContent(super.getResources().getString(R.string.label_try_to_register))
            this.dialog.show()
            this.auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(this::onRegisterSuccess)
                    .addOnFailureListener(this::onRegisterFailed)
        }
    }

    private fun onRegisterSuccess(result: AuthResult)
    {
        Timber.d("onRegisterSuccess [$result]")

        fun registerSuccess()
        {
            this.dialog.dismiss()
            Toast.makeText(this, super.getResources().getString(R.string.label_register_success), Toast.LENGTH_SHORT).show()

            this.auth.signOut()

            Handler(mainLooper).postDelayed({
                super@RegisterActivity.setResult(RESULT_OK, Intent().run {
                    putExtra(RegisterActivity.EMAIL, this@RegisterActivity.edittext_email.text.toString())
                    putExtra(RegisterActivity.PASSWORD, this@RegisterActivity.edittext_password.text.toString())
                })
                this.onLoginButtonClicked(null)
            }, 1000)
        }

        fun grantTo(user: FirebaseUser, name: String)
        {
            AuthHelper.initializeUser(user, name, Settings.GROUP_NAME, DatabaseReference.CompletionListener { error, _ ->
                error?.run { grantTo(user, name) } ?: registerSuccess()
            })
        }

        this.auth.currentUser?.run { grantTo(this, this@RegisterActivity.edittext_name.text.toString()) }
    }

    private fun onRegisterFailed(e: Exception?)
    {
        Timber.d("onRegisterFailed [$e]")

        this.dialog.dismiss()

        Timber.e(e)
        Toast.makeText(this, super.getResources().getString(
                when (e ?: false)
                {
                    is FirebaseAuthWeakPasswordException       -> R.string.label_auth_weak_password
                    is FirebaseAuthInvalidCredentialsException -> R.string.label_auth_email_malformed
                    is FirebaseAuthUserCollisionException      -> R.string.label_auth_email_exists
                    is FirebaseNetworkException                -> R.string.label_auth_network_issue
                    else                                       -> R.string.label_register_failed
                }
        ), Toast.LENGTH_SHORT).show()
    }

    companion object
    {
        const val EMAIL = "email"
        const val PASSWORD = "password"
    }
}
