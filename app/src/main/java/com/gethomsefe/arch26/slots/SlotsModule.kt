package com.gethomsefe.arch26.slots

import org.koin.dsl.module

val slotsModule
    get() = module { factory { SlotsModel.Presenter() } }
