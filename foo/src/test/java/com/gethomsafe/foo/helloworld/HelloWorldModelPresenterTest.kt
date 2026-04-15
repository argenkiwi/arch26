package com.gethomsafe.foo.helloworld

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HelloWorldModelPresenterTest {

    @Test
    fun `initial state is correct`() = runTest {
        val presenter = HelloWorldModel.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke()
        }.test {
            with(awaitItem()) {
                assertEquals("Hello, Architecture!", message)
                assertEquals(0, count)
            }
        }
    }

    @Test
    fun `increment increases count`() = runTest {
        val presenter = HelloWorldModel.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke()
        }.test {
            with(awaitItem()) {
                assertEquals(0, count)
                actions.increment()
            }
            with(awaitItem()) {
                assertEquals(1, count)
            }
        }
    }
}
