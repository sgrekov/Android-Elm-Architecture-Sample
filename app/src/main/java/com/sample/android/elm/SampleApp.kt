package com.sample.android.elm

import android.app.Application
import com.sample.android.elm.data.GitHubService
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class SampleApp : Application() {

    lateinit var service: GitHubService

    override fun onCreate() {
        super.onCreate()
        service = GitHubService(Schedulers.io())
        Timber.plant(Timber.DebugTree())
    }
}
