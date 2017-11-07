package com.github.syafiqq.fitnesscounterstudent.controller.auth

import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity: AppCompatActivity(), OnCompleteListener<AuthResult>, View.OnClickListener
{

    private lateinit var dialog: MaterialDialog
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?)
    {
        Timber.d("onCreate")

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
                this.onClick(null)
                return@OnEditorActionListener true
            }
            false
        })
        this.button_sumbit.setOnClickListener(this)
    }

    override fun onDestroy()
    {
        Timber.d("onDestroy")

        super.onDestroy()
        this.dialog.dismiss()
    }

    private fun isEmailValid(email: String): Boolean
    {
        Timber.d("isEmailValid")

        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean
    {
        Timber.d("isPasswordValid")

        return password.length >= 8
    }

    override fun onClick(view: View?)
    {
        Timber.d("onClick")

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
            this.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, this)
        }
    }

    override fun onComplete(result: Task<AuthResult>)
    {
        Timber.d("onComplete")

        this.dialog.dismiss()
        if (result.isSuccessful)
        {
            Timber.d("Success Login")
        }
        else
        {
            Timber.e(result.exception)
            Toast.makeText(this@LoginActivity, super@LoginActivity.getResources().getString(R.string.label_login_failed), Toast.LENGTH_SHORT).show()
        }
    }
}
