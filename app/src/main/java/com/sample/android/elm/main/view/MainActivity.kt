package com.sample.android.elm.main.view

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.androidjacoco.sample.R
import com.sample.android.elm.AndroidNavigator
import com.sample.android.elm.Navigator
import com.sample.android.elm.Program
import com.sample.android.elm.SampleApp
import com.sample.android.elm.StateHolderFragment
import com.sample.android.elm.data.AppPrefs
import com.sample.android.elm.main.presenter.MainPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import org.eclipse.egit.github.core.Repository

class MainActivity : AppCompatActivity(), IMainView {
    lateinit var presenter: MainPresenter
    lateinit var reposList: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var errorText: TextView
    lateinit var stateHolderFragment: StateHolderFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        reposList = findViewById(R.id.repos_list) as RecyclerView
        reposList.layoutManager = LinearLayoutManager(applicationContext)
        progressBar = findViewById(R.id.repos_progress) as ProgressBar
        errorText = findViewById(R.id.error_text) as TextView

        val fragment = supportFragmentManager.findFragmentByTag(STATE_HOLDER_TAG)
        if (fragment == null) {
            stateHolderFragment = StateHolderFragment()
            supportFragmentManager.beginTransaction()
                .add(stateHolderFragment, STATE_HOLDER_TAG)
                .commit()
        } else {
            stateHolderFragment = fragment as StateHolderFragment
        }


        val navigator : Navigator = AndroidNavigator(this)
        presenter = MainPresenter(
            this,
            Program(AndroidSchedulers.mainThread()),
            AppPrefs(getPreferences(Context.MODE_PRIVATE)),
            (application as SampleApp).service,
            navigator
        )
        presenter.init(stateHolderFragment.getMainScreenState())
    }

    override fun onResume() {
        super.onResume()
        presenter.render()
    }

    override fun onDestroy() {
        super.onDestroy()
        stateHolderFragment.putMainState(presenter.getState())
        presenter.destroy()
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

    companion object {

        val STATE_HOLDER_TAG = "STATE_HOLDER_TAG"
    }
}
