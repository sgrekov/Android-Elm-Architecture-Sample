package com.sample.android.mobius.data

import io.reactivex.Scheduler
import io.reactivex.Single
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.StargazerService
import org.eclipse.egit.github.core.service.UserService

class GitHubService(private val scheduler: Scheduler) : IApiService {
    private var client = GitHubClient()
    private var userName : String = ""
        @Synchronized get
        @Synchronized set

    override fun login(login: String, pass: String): Single<Boolean> {
        return Single.fromCallable {
            userName = login
            true
        }.subscribeOn(scheduler)
    }

    override fun getUserName(): String {
        return userName
    }

    override fun getStarredRepos(): Single<List<Repository>> {
        return Single.fromCallable {
            val stargazerService = StargazerService(client)
            stargazerService.getStarred(userName)
        }.subscribeOn(scheduler)
    }
}
