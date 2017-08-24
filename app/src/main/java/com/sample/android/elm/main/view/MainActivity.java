package com.sample.android.elm.main.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.androidjacoco.sample.R;
import com.sample.android.elm.Program;
import com.sample.android.elm.SampleApp;
import com.sample.android.elm.StateHolderFragment;
import com.sample.android.elm.data.AppPrefs;
import com.sample.android.elm.main.presenter.MainPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import org.eclipse.egit.github.core.Repository;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainActivity extends AppCompatActivity implements IMainView {

    public static final String STATE_HOLDER_TAG = "STATE_HOLDER_TAG";
    MainPresenter presenter;
    RecyclerView reposList;
    ProgressBar progressBar;
    TextView errorText;
    StateHolderFragment stateHolderFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reposList = (RecyclerView) findViewById(R.id.repos_list);
        reposList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        progressBar = (ProgressBar) findViewById(R.id.repos_progress);
        errorText = (TextView) findViewById(R.id.error_text);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(STATE_HOLDER_TAG);
        if (fragment == null) {
            stateHolderFragment = new StateHolderFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(stateHolderFragment, STATE_HOLDER_TAG)
                    .commit();
        } else {
            stateHolderFragment = (StateHolderFragment) fragment;
        }

        presenter = new MainPresenter(this,
                new Program(AndroidSchedulers.mainThread()),
                new AppPrefs(getPreferences(MODE_PRIVATE)),
                ((SampleApp) getApplication()).getService());
        presenter.init(stateHolderFragment.getMainScreenState());
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.render();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stateHolderFragment.putMainState(presenter.getState());
        presenter.destroy();
    }

    @Override
    public void setTitle(@NotNull String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void setErrorText(@NotNull String error) {
        errorText.setText(error);
    }

    @Override
    public void showErrorText() {
        errorText.setVisibility(View.VISIBLE);
    }

    @Override
    public void setRepos(@NotNull List<Repository> repos) {
        reposList.setAdapter(new ReposAdapter(repos, getLayoutInflater()));
    }

    private class ReposAdapter extends RecyclerView.Adapter {
        private List<Repository> repos;
        private LayoutInflater inflater;

        public ReposAdapter(List<Repository> repos, LayoutInflater inflater) {
            this.repos = repos;
            this.inflater = inflater;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RepoViewHolder(inflater.inflate(R.layout.repos_list_item_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((RepoViewHolder) holder).bind(repos.get(position));
        }

        @Override
        public int getItemCount() {
            return repos.size();
        }

        class RepoViewHolder extends RecyclerView.ViewHolder {
            TextView repoName;
            TextView repoStarsCount;


            public RepoViewHolder(View itemView) {
                super(itemView);
                repoName = (TextView) itemView.findViewById(R.id.repo_name);
                repoStarsCount = (TextView) itemView.findViewById(R.id.repo_stars_count);
            }


            public void bind(Repository repository) {
                repoName.setText(repository.getName());
                repoStarsCount.setText("watchers:" + repository.getWatchers());
            }
        }
    }
}
