package com.github.syafiqq.fitnesscounter.role.tester.controller.auth

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
import com.github.syafiqq.fitnesscounter.core.helpers.AuthHelper
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.Dashboard
import com.github.syafiqq.fitnesscounter.role.tester.model.Settings
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber

class LoginActivity: AppCompatActivity()
{
    private lateinit var dialog: MaterialDialog
    private lateinit var auth: FirebaseAuth

    override fun onCreate(state: Bundle?)
    {
        Timber.d("onCreate [$state]")

        super.onCreate(state)
        super.setContentView(R.layout.activity_login)

        this.auth = FirebaseAuth.getInstance()
        this.dialog = MaterialDialog.Builder(this)
                .canceledOnTouchOutside(false)
                .content(super.getResources().getString(R.string.label_please_wait))
                .progress(true, 0)
                .build()

        this.edittext_password.setOnEditorActionListener(this::onEditorActionClicked)
        this.button_submit.setOnClickListener(this::onSubmitButtonClicked)
        this.button_register.setOnClickListener(this::onRegisterButtonClicked)
    }

    override fun onDestroy()
    {
        Timber.d("onDestroy")

        this.dialog.dismiss()
        super.onDestroy()
    }

    override fun onStart()
    {
        Timber.d("onStart")

        this.populateField("mohammadsyahronimeizu@gmail.com", "3514140405940002")
        super.onStart()
    }

    override fun onStop()
    {
        Timber.d("onStop")

        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        Timber.d("onActivityResult [$requestCode, $resultCode, $data]")

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
        Timber.d("Populate Field [$email, $password]")

        this.edittext_email.setText(email)
        this.edittext_password.setText(password)
    }

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

    private fun onRegisterButtonClicked(view: View?)
    {
        Timber.d("onRegisterButtonClicked [$view]")

        super.startActivityForResult(Intent(this, RegisterActivity::class.java), LOGIN_CALLBACK)
    }

    private fun onSubmitButtonClicked(view: View?)
    {
        Timber.d("onSubmitButtonClicked [$view]")

        edittext_email.error = null
        edittext_password.error = null

        val email = edittext_email.text.toString()
        val password = edittext_password.text.toString()

        var cancel = false
        var focusView: View? = null

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
        {
            edittext_password.error = getString(R.string.error_invalid_password)
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
            this.dialog.setContent(super.getResources().getString(R.string.label_try_to_login))
            this.dialog.show()
            this.auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(this::onLoginSuccess)
                    .addOnFailureListener(this::onLoginFailure)
        }
    }

    private fun onLoginSuccess(result: AuthResult)
    {
        Timber.d("onLoginSuccess [$result]")

        this.auth.currentUser?.run {
            AuthHelper.checkAuthorities(this, Settings.GROUP_NAME, object: AuthHelper.AuthorizationListener
            {
                override fun onUnauthorized()
                {
                    super.onUnauthorized()
                    Toast.makeText(this@LoginActivity, R.string.auth_no_previlege_access, Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError?)
                {
                    super.onCancelled(error)
                    Toast.makeText(this@LoginActivity, R.string.label_auth_network_issue, Toast.LENGTH_SHORT).show()
                }

                override fun onAuthorized(snapshot: DataSnapshot)
                {
                    super.onAuthorized(snapshot)
                    this@LoginActivity.onPostLogin()
                }

                override fun onCompleted()
                {
                    super.onCompleted()
                    this@LoginActivity.dialog.dismiss()
                }
            })
        }
    }

    private fun onPostLogin()
    {
        val intent = Intent(this, Dashboard::class.java)
        super.startActivity(intent)
        super.finish()
    }

    private fun onLoginFailure(e: Exception?)
    {
        Timber.d("onLoginFailure [$e]")

        Toast.makeText(this, when (e ?: false)
        {
            is FirebaseAuthInvalidUserException        -> R.string.label_auth_email_not_exists
            is FirebaseAuthWeakPasswordException       -> R.string.label_auth_weak_password
            is FirebaseAuthInvalidCredentialsException -> R.string.label_auth_invalid_credential
            is FirebaseAuthUserCollisionException      -> R.string.label_auth_email_exists
            is FirebaseNetworkException                -> R.string.label_auth_network_issue
            else                                       -> R.string.label_login_failed
        }, Toast.LENGTH_SHORT).show()

        this.dialog.dismiss()
    }

    companion object
    {
        const val LOGIN_CALLBACK = 0x01
    }
}
