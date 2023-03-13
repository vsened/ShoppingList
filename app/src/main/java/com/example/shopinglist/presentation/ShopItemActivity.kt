package com.example.shopinglist.presentation

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.example.shopinglist.R
import com.example.shopinglist.domain.ShopItem
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ShopItemActivity : AppCompatActivity() {

    private lateinit var viewModel: ShopItemViewModel

    private lateinit var tilName: TextInputLayout
    private lateinit var tilCount: TextInputLayout
    private lateinit var tvName: TextInputEditText
    private lateinit var tvCount: TextInputEditText
    private lateinit var saveButton: Button

    private var screenMode = MODE_UNKNOWN
    private var shopItemId = ShopItem.UNDEFINED_ID
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_item)
        parseIntent()
        initViews()
        viewModel = ViewModelProvider(this)[ShopItemViewModel::class.java]
        addTextChangeListeners()
        launchScreenMode()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.errorInputName.observe(this) {
            if (it) {
                tilName.error = INCORRECT_NAME_MESSAGE
            } else {
                tilName.error = null
            }
        }
        viewModel.errorInputCount.observe(this) {
            if (it) {
                tilName.error = INCORRECT_COUNT_MESSAGE
            } else {
                tilName.error = null
            }
        }
        viewModel.shouldCloseScreen.observe(this) {
            finish()
        }
    }

    private fun launchScreenMode() {
        when (screenMode) {
            MODE_EDIT -> launchEditMode()
            else -> launchAddMode()
        }
    }

    private fun addTextChangeListeners() {
        tvName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.resetErrorInputName()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
        tvCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.resetErrorInputCount()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun parseIntent() {
        if (!intent.hasExtra(EXTRA_SCREEN_MODE)) {
            throw RuntimeException("Param screen mode is absent")
        }
        val mode = intent.getStringExtra(EXTRA_SCREEN_MODE)
        if (mode != MODE_ADD && mode != MODE_EDIT) {
            throw RuntimeException("Unknown screen mode $mode")
        }
        screenMode = mode
        if (mode == MODE_EDIT) {
            if (!intent.hasExtra(EXTRA_SHOP_ITEM_ID)) {
                throw RuntimeException("Param shop item id is absent")
            }
            shopItemId = intent.getIntExtra(EXTRA_SHOP_ITEM_ID, -1)
        }
    }

    private fun initViews() {
        tilName = findViewById(R.id.til_name)
        tilCount = findViewById(R.id.til_count)
        tvName = findViewById(R.id.et_name)
        tvCount = findViewById(R.id.et_count)
        saveButton = findViewById(R.id.button_save_item)
    }

    private fun launchEditMode() {
        viewModel.getShopItem(shopItemId)
        viewModel.currentShopItem.observe(this) {
            tvName.setText(it.name)
            tvCount.setText(it.count.toString())

        }
        saveButton.setOnClickListener {
            val name = tvName.text?.toString()
            val count = tvCount.text?.toString()
            viewModel.editShopItem(name, count)
        }
    }

    private fun launchAddMode() {
        saveButton.setOnClickListener {
            val name = tvName.text?.toString()
            val count = tvCount.text?.toString()
            viewModel.addShopItem(name, count)
        }
    }

    companion object {
        private const val EXTRA_SCREEN_MODE = "extra_mode"
        private const val EXTRA_SHOP_ITEM_ID = "extra_shop_item_id"
        private const val MODE_EDIT = "mode_edit"
        private const val MODE_ADD = "mode_add"
        private const val MODE_UNKNOWN = ""
        private const val INCORRECT_NAME_MESSAGE = "Name is incorrect! Please change."
        private const val INCORRECT_COUNT_MESSAGE = "Count is incorrect! Please change."

        fun newIntentAddItem(context: Context): Intent {
            val intent = Intent(context, ShopItemActivity::class.java)
            intent.putExtra(EXTRA_SCREEN_MODE, MODE_ADD)
            return intent
        }

        fun newIntentEditItem(context: Context, shopItemId: Int): Intent {
            val intent = Intent(context, ShopItemActivity::class.java)
            intent.putExtra(EXTRA_SCREEN_MODE, MODE_EDIT)
            intent.putExtra(EXTRA_SHOP_ITEM_ID, shopItemId)
            return intent
        }
    }
}