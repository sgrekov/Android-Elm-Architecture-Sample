package com.sample.android.mobius.main.view

import org.eclipse.egit.github.core.Repository

interface IMainView {
    fun setTitle(s: String)

    fun showProgress()

    fun hideProgress()

    fun setErrorText(s: String)

    fun showErrorText()

    fun setRepos(reposList: List<Repository>)
}
