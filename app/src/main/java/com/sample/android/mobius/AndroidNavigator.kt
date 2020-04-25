package com.sample.android.mobius

import android.content.Intent
import android.support.v4.app.FragmentActivity
import com.sample.android.mobius.main.view.MainActivity

class AndroidNavigator(private val activity: FragmentActivity) : Navigator {

    override fun goToMainScreen() {
        val i = Intent(activity, MainActivity::class.java)
        activity.startActivity(i)
        activity.finish()
    }

}