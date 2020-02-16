package com.walkly.walkly.ui.profile

import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class AccountSettingsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private var oldUserName: String
    private var oldUserEmail: String

    val userName = MutableLiveData<String>()
    val userEmail = MutableLiveData<String>()
    val oldPassword = MutableLiveData<String>()
    val newPassword = MutableLiveData<String>()



    private val _userNameUpdateSuccess = MutableLiveData<String>()
    val userNameUpdateSuccess: LiveData<String>
    get() = _userNameUpdateSuccess

    private val _userEmailUpdateSuccess = MutableLiveData<String>()
    val userEmailUpdateSuccess: LiveData<String>
        get() = _userEmailUpdateSuccess

    private val _userPasswordUpdateSuccess = MutableLiveData<String>()
    val userPasswordUpdateSuccess: LiveData<String>
        get() = _userPasswordUpdateSuccess

    private val _reAuthSuccess = MutableLiveData<String>()
    val reAuthSuccess : LiveData<String>
        get() = _reAuthSuccess




    init {
        // load user name & email
        oldUserName= auth.currentUser?.displayName!!
        oldUserEmail = auth.currentUser?.email!!

        userName.value = oldUserName
        userEmail.value = oldUserEmail
    }

    fun onSaveChanges(){


        // TODO integrate Bitmoji

        changeUserName()

        // re-authenticate
        auth.currentUser?.reauthenticate(
            EmailAuthProvider.getCredential(oldUserEmail, oldPassword.value.toString())
        )
            ?.addOnSuccessListener {
                Log.d("AccountSettingsVM", "re-auth success")
                changeEmailOrPassword()
            }
            ?.addOnFailureListener{
                Log.d("AccountSettingsVM", "old password = ${oldPassword.value}")
                Log.d("AccountSettingsVM", "new password = ${newPassword.value}")
                Log.d("AccountSettingsVM", "re-auth failure", it)
                _reAuthSuccess.value = "failure"
            }


    }

    private fun changeUserName() {
        if (!TextUtils.isEmpty(userName.value) &&
            oldUserName != userName.value
        )

            auth.currentUser?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(userName.value)
                    .build()
            )
                ?.addOnSuccessListener {
                    _userNameUpdateSuccess.value = "success"
                }
                ?.addOnFailureListener {
                    _userNameUpdateSuccess.value = "failure"
                    Log.d("AccountSettingsVM", "name failure", it)
                }
    }


    private fun changeEmailOrPassword() {

        if (!TextUtils.isEmpty(userEmail.value) &&
            userEmail.value != oldUserEmail &&
            Patterns.EMAIL_ADDRESS.matcher(userEmail.value).matches())

            auth.currentUser?.updateEmail(userEmail.value!!)
                ?.addOnSuccessListener {
                    _userEmailUpdateSuccess.value = "success"
                }
                ?.addOnFailureListener{
                    _userEmailUpdateSuccess.value = "failure"
                    Log.d("AccountSettingsVM", "email failure", it)
                }

        if (!TextUtils.isEmpty(oldPassword.value) &&
            oldPassword.value != newPassword.value)

            auth.currentUser?.updatePassword(oldPassword.value!!)
                ?.addOnSuccessListener {
                    _userPasswordUpdateSuccess.value = "success"
                }
                ?.addOnFailureListener {
                    _userPasswordUpdateSuccess.value = "failure"
                    Log.d("AccountSettingsVM", "password failure", it)
                }
    }


}