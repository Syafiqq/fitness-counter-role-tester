package com.github.syafiqq.fitnesscounter.role.tester.custom.android.text

import android.text.Editable
import android.text.TextWatcher

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 27 February 2018, 12:25 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
interface CTextWatcher: TextWatcher
{
    override fun afterTextChanged(s: Editable?)
    {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
    {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
    {
    }
}