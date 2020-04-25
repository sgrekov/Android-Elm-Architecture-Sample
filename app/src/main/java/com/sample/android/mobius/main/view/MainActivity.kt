package com.sample.android.mobius.main.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.sample.android.mobius.R
import com.sample.android.mobius.SampleApp
import com.sample.android.mobius.data.GitHubService
import com.sample.android.mobius.domain.main.IdleEvent
import com.sample.android.mobius.domain.main.LoadReposEffect
import com.sample.android.mobius.domain.main.MainEffect
import com.sample.android.mobius.domain.main.MainEvent
import com.sample.android.mobius.domain.main.MainModel
import com.sample.android.mobius.domain.main.MainUpdate
import com.sample.android.mobius.domain.main.ReposLoadedEvent
import com.spotify.mobius.First
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.rx2.RxConnectables
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.eclipse.egit.github.core.Repository

class MainActivity : AppCompatActivity(), IMainView {

    @BindView(R.id.repos_list) lateinit var reposList: RecyclerView
    @BindView(R.id.repos_progress) lateinit var progressBar: ProgressBar
    @BindView(R.id.error_text) lateinit var errorText: TextView

    lateinit var api: GitHubService

    var rxEffectHandler = RxMobius.subtypeEffectHandler<MainEffect, MainEvent>()
        .add(LoadReposEffect::class.java, this::handleLoadRepos)
        .build()

    var loopFactory: MobiusLoop.Factory<MainModel, MainEvent, MainEffect> =
        RxMobius
            .loop(MainUpdate(), rxEffectHandler)
            .init {
                First.first(
                    MainModel(userName = api.getUserName()), setOf(LoadReposEffect)
                )
            }
            .logger(AndroidLogger.tag<MainModel, MainEvent, MainEffect>("my_app"))


    lateinit var controller: MobiusLoop.Controller<MainModel, MainEvent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        reposList.layoutManager = LinearLayoutManager(applicationContext)

        api = (application as SampleApp).service
        controller = MobiusAndroid.controller(
            loopFactory,
            MainModel(userName = api.getUserName())
        )
        controller.connect(RxConnectables.fromTransformer(this::connectViews))
    }

    override fun onResume() {
        super.onResume()
        controller.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.stop()
    }

    fun handleLoadRepos(request: Observable<LoadReposEffect>): Observable<MainEvent> {
        return request.flatMap { effect ->
            api.getStarredRepos().map { repos ->
                ReposLoadedEvent(
                    repos
                )
            }.toObservable()
        }
    }

    fun connectViews(models: Observable<MainModel>): Observable<MainEvent> {
        val disposable = models
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { render(it) }

        return Observable
            .just(IdleEvent as MainEvent)
            .doOnDispose(disposable::dispose)
    }

    fun render(state: MainModel) {
        state.apply {
            setTitle(state.userName + "'s starred repos")

            if (isLoading) {
                if (reposList.isEmpty()) {
                    showProgress()
                }
            } else {
                hideProgress()
                if (reposList.isEmpty()) {
                    setErrorText("User has no starred repos")
                    showErrorText()
                }
            }
            setRepos(reposList)
        }
    }

    override fun setTitle(title: String) {
        supportActionBar!!.setTitle(title)
    }

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun setErrorText(error: String) {
        errorText.text = error
    }

    override fun showErrorText() {
        errorText.visibility = View.VISIBLE
    }

    override fun setRepos(repos: List<Repository>) {
        reposList.adapter = ReposAdapter(repos, layoutInflater)
    }

    private inner class ReposAdapter(private val repos: List<Repository>, private val inflater: LayoutInflater) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return RepoViewHolder(inflater.inflate(R.layout.repos_list_item_layout, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as RepoViewHolder).bind(repos[position])
        }

        override fun getItemCount(): Int {
            return repos.size
        }

        internal inner class RepoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var repoName: TextView
            var repoStarsCount: TextView


            init {
                repoName = itemView.findViewById(R.id.repo_name) as TextView
                repoStarsCount = itemView.findViewById(R.id.repo_stars_count) as TextView
            }


            fun bind(repository: Repository) {
                repoName.text = repository.name
                repoStarsCount.text = "watchers:" + repository.watchers
            }
        }
    }
}
