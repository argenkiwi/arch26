package com.gethomsefe.arch26.todo

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.gethomesafe.arch26.Database
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Single

@Factory
fun driver(context: Context) = AndroidSqliteDriver(Database.Schema, context, "todo.db")

@Single
fun database(driver: AndroidSqliteDriver) = Database(driver)
