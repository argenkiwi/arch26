package com.gethomsefe.arch26.counter.edit

import org.koin.dsl.module

val editModule
    get() = module { factory { EditModel.Presenter() } }
