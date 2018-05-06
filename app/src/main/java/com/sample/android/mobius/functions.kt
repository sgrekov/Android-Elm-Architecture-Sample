package com.sample.android.mobius

import com.spotify.mobius.Next
import com.spotify.mobius.internal_util.ImmutableUtil

fun <M, F> next(model: M, effect: F): Next<M, F> {
    return Next.next(model, ImmutableUtil.immutableSet(setOf(effect)))
}