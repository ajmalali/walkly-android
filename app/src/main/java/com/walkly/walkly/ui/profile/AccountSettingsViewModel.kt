package com.walkly.walkly.ui.profile

import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class AccountSettingsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    val userName = MutableLiveData<String>()
    val userEmail = MutableLiveData<String>()
    val userPassword = MutableLiveData<String>()
    val userPasswordConfirm = MutableLiveData<String>()


    init {
        // load user name & email
        userName.value = auth.currentUser?.displayName
        userEmail.value = auth.currentUser?.email
    }

    fun onSaveChanges(){

        // TODO show toast on success

        if (!TextUtils.isEmpty(userName.value))
            auth.currentUser?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(userName.value)
                    .build()
            )


        if (!TextUtils.isEmpty(userEmail.value) &&
                Patterns.EMAIL_ADDRESS.matcher(userEmail.value).matches())
            auth.currentUser?.updateEmail(userEmail.value!!)

        if (!TextUtils.isEmpty(userPassword.value) &&
                userPassword.value == userPasswordConfirm.value)
            auth.currentUser?.updatePassword(userPassword.value!!)


    }



}