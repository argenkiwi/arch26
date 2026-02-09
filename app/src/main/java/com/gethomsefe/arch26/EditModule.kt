package com.gethomsefe.arch26

import org.koin.dsl.module

val editModule
    get() = module { factory { EditModel.Presenter() } }
