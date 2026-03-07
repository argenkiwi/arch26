package com.gethomsefe.arch26

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EditModelPresenterTest {

    @Test
    fun `initial state is correct`() = runTest {
        val presenter = EditModel.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke(initialCount = 5)
        }.test {
            with(awaitItem()) {
                assertEquals(5, count)
            }
        }
    }

    @Test
    fun `increment increases count`() = runTest {
        val presenter = EditModel.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke(initialCount = 0)
        }.test {
            with(awaitItem()) {
                assertEquals(0, count)
                actions.increment()
            }

            with(awaitItem()) {
                assertEquals(1, count)
                actions.increment()
            }

            with(awaitItem()) {
                assertEquals(2, count)
            }
        }
    }

    @Test
    fun `decrement decreases count`() = runTest {
        val presenter = EditModel.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke(initialCount = 10)
        }.test {
            with(awaitItem()) {
                assertEquals(10, count)
                actions.decrement()
            }

            with(awaitItem()) {
                assertEquals(9, count)
                actions.decrement()
            }

            with(awaitItem()) {
                assertEquals(8, count)
            }
        }
    }
}
