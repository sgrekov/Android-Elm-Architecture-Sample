package com.sample.android.mobius.data

import io.reactivex.Single

interface IAppPrefs {

    fun getUserSavedCredentials() : Single<Pair<String, String>>
    fun saveUserSavedCredentials(login : String, pass : String) : Single<Boolean>
}