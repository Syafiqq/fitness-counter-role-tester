package com.github.syafiqq.fitnesscounter.role.student.controller.auth

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.syafiqq.fitnesscounter.role.student.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.android.synthetic.main.activity_password_reset.*
import timber.log.Timber

class PasswordResetActivity: AppCompatActivity()
{
    private lateinit var dialog: MaterialDialog
    private lateinit var auth: FirebaseAuth

    override fun onCreate(state: Bundle?)
    {
        Timber.d("onCreate [$state]")

        super.onCreate(state)
        super.setContentView(R.layout.activity_password_reset)
        this.setupActionBar()

        this.auth = FirebaseAuth.getInstance()
        this.dialog = MaterialDialog.Builder(this)
                .canceledOnTouchOutside(false)
                .content(super.getResources().getString(R.string.label_please_wait))
                .progress(true, 0)
                .build()

        this.edittext_email.setOnEditorActionListener(this::onEditorActionClicked)
        this.button_sumbit.setOnClickListener(this::onSubmitButtonClicked)
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

    private fun onSubmitButtonClicked(view: View?)
    {
        Timber.d("onSubmitButtonClicked [$view]")

        edittext_email.error = null

        val email = edittext_email.text.toString()

        var cancel = false
        var focusView: View? = null

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
            this.dialog.setContent(super.getResources().getString(R.string.label_try_to_request_password_reset))
            this.dialog.show()
            this.auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(this::onRequestSuccess)
                    .addOnFailureListener(this::onRequestFailed)
                    .addOnCompleteListener { this@PasswordResetActivity.dialog.dismiss() }
        }
    }

    private fun onRequestSuccess(result: Void?)
    {
        Timber.d("onRequestSuccess [$result]")

        Toast.makeText(this, R.string.label_password_reset_success, Toast.LENGTH_LONG).show()
        this.onBackPressed()
    }

    private fun onRequestFailed(e: Exception?)
    {
        Timber.d("onRequestFailed")

        Timber.e(e)
        Toast.makeText(this, when (e ?: false)
        {
            is FirebaseAuthInvalidUserException -> R.string.label_auth_email_not_exists
            is FirebaseNetworkException         -> R.string.label_auth_network_issue
            else                                -> R.string.label_password_reset_failed
        }, Toast.LENGTH_SHORT).show()
    }
}
