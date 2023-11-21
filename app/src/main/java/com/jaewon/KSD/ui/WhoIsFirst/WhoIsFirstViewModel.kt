package com.jaewon.KSD.ui.WhoIsFirst

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WhoIsFirstViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is WhoIsFirst Fragment"
    }
    val text: LiveData<String> = _text
}