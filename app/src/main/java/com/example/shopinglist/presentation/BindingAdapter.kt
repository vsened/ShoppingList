package com.example.shopinglist.presentation

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("errorInputName")
fun bindErrorInputName(textInputLayout: TextInputLayout, isError: Boolean) {
    if (isError) {
        textInputLayout.error = INCORRECT_NAME_MESSAGE
    } else {
        textInputLayout.error = null
    }
}

@BindingAdapter("errorInputCount")
fun bindErrorInputCount(textInputLayout: TextInputLayout, isError: Boolean) {
    if (isError) {
        textInputLayout.error = INCORRECT_COUNT_MESSAGE
    } else {
        textInputLayout.error = null
    }
}

private const val INCORRECT_NAME_MESSAGE = "Name is incorrect! Please change."
private const val INCORRECT_COUNT_MESSAGE = "Count is incorrect! Please change."
