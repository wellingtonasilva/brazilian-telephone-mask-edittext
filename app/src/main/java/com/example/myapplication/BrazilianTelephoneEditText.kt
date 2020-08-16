package com.example.myapplication

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.View.OnKeyListener
import androidx.appcompat.widget.AppCompatEditText

class BrazilianTelephoneEditText(
    context: Context,
    attrs: AttributeSet?
) : AppCompatEditText(context, attrs) {

    enum class BrazilizanTelefoneType {
        UNDEFINED,
        MOBILE_PHONE,
        LANDLINE
    }

    interface BrazilianTelephoneListener {
        fun onTypeChange(type: BrazilizanTelefoneType)
    }

    private val MAX_NUMBER_MOBILE_PHONE = 10
    private val MAX_NUMBER_LANDLINE = 9
    private var brazilianTelephoneListener = mutableListOf<BrazilianTelephoneListener>()
    private var brazilizanTelefoneType = BrazilizanTelefoneType.UNDEFINED
    private lateinit var mTextWatcher: TextWatcher

    init {
        createTextWatcher()
        inputType = InputType.TYPE_CLASS_PHONE
        addTextChangedListener(mTextWatcher)
        setOnClickListener { v: View? ->
            // disable moving cursor
            setSelection(text!!.length)
        }
        setOnKeyListener(OnKeyListener { v, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_DEL -> removeFormat(text.toString())
            }
            false
        })
        if (text != null) {
            setTextInternal(format(text.toString()))
        }
    }

    fun addBrazilianTelephoneListener(listener: BrazilianTelephoneListener) {
        brazilianTelephoneListener.add(listener)
    }

    fun removeBrazilianTelephoneListener(listener: BrazilianTelephoneListener) {
        if (brazilianTelephoneListener.contains(listener)) {
            brazilianTelephoneListener.remove(listener)
        }
    }

    private fun format(text: String): String {
        // https://www.anatel.gov.br/setorregulado/plano-de-numeracao-brasileiro
        // https://www.anatel.gov.br/setorregulado/plano-de-numeracao-brasileiro?id=330
        // NÚMERO DO ASSINANTE: 9XXXX-XXXX

        // https://www.anatel.gov.br/setorregulado/plano-de-numeracao-brasileiro?id=331
        // NÚMERO DO ASSINANTE: 2XXX-XXXX; 3XXX-XXXX; 4XXX-XXXX; 5XXX-XXXX

        val telefoneWithoutMask: String = text.replace("[^0-9]".toRegex(), "")

        if (telefoneWithoutMask.isNotBlank()) {
            var actualType = BrazilizanTelefoneType.UNDEFINED
            // Define o tipo do telefone
            when(telefoneWithoutMask[0]) {
                '9' -> actualType = BrazilizanTelefoneType.MOBILE_PHONE
                '2', '3', '4', '5' -> actualType = BrazilizanTelefoneType.LANDLINE
            }
            // Envia evento informando que o mudou o tipo do telefone
            if (actualType != brazilizanTelefoneType) {
                brazilizanTelefoneType = actualType
                brazilianTelephoneListener?.forEach {
                    it.onTypeChange(brazilizanTelefoneType)
                }
            }

            // Definir o tamanho máximo para o campo
            when(brazilizanTelefoneType) {
                BrazilizanTelefoneType.MOBILE_PHONE -> {
                    filters = arrayOf(InputFilter.LengthFilter(MAX_NUMBER_MOBILE_PHONE))
                }
                BrazilizanTelefoneType.LANDLINE -> {
                    filters = arrayOf(InputFilter.LengthFilter(MAX_NUMBER_LANDLINE))
                }
            }

            // Adicionar formato
            when(brazilizanTelefoneType) {
                BrazilizanTelefoneType.MOBILE_PHONE -> {
                    return if (telefoneWithoutMask.length >= 5) {
                        telefoneWithoutMask.substring(0, 5) + "-" + telefoneWithoutMask.substring(5)
                    } else {
                        telefoneWithoutMask
                    }
                }
                BrazilizanTelefoneType.LANDLINE -> {
                    return if (telefoneWithoutMask.length >= 4) {
                        telefoneWithoutMask.substring(0, 4) + "-" + telefoneWithoutMask.substring(4)
                    } else {
                        telefoneWithoutMask
                    }
                }
            }
        }

        return telefoneWithoutMask
    }

    private fun removeFormat(text: String?) {
        if (text != null) {
            val telefoneWithoutMask: String = text.replace("[^0-9]".toRegex(), "")
            if (telefoneWithoutMask.isNotBlank()) {
                when(brazilizanTelefoneType) {
                    BrazilizanTelefoneType.MOBILE_PHONE -> {
                        if (telefoneWithoutMask.length == 5) {
                            setTextInternal(telefoneWithoutMask.substring(0, 5))
                            setSelection(getText()!!.length)
                        }
                    }
                    BrazilizanTelefoneType.LANDLINE -> {
                        if (telefoneWithoutMask.length == 4) {
                            setTextInternal(telefoneWithoutMask.substring(0, 4))
                            setSelection(getText()!!.length)
                        }
                    }
                }
            }
        }
    }

    private fun setTextInternal(text: String) {
        removeTextChangedListener(mTextWatcher)
        setText(text)
        addTextChangedListener(mTextWatcher)
    }

    private fun createTextWatcher() {
        mTextWatcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                var text = s.toString()
                setTextInternal(format(text))
                setSelection(getText()!!.length)
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        }
    }
}