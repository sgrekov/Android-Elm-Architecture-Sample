package com.sample.android.elm

import android.os.Bundle
import android.support.v4.app.Fragment
import com.sample.android.elm.main.presenter.MainPresenter


/**
 * A simple [Fragment] subclass.
 */
class StateHolderFragment : Fragment() {

    companion object {
        const val keyMain = "KEY_MAIN_STATE"
    }

    private val statesMap = mutableMapOf<String, State?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun putMainState(state: MainPresenter.MainState) {
        statesMap[keyMain] = state
    }

    fun getMainScreenState(): MainPresenter.MainState? {
        return  statesMap[keyMain] as? MainPresenter.MainState
    }

}
