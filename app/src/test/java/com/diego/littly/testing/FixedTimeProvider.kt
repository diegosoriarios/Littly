package com.diego.littly.testing

import com.diego.littly.core.domain.time.TimeProvider
import java.time.LocalDate

class FixedTimeProvider(
    var today: LocalDate,
    private val baseMillis: Long = 0L
) : TimeProvider {

    private var counter = 0L

    override fun today(): LocalDate = today

    override fun nowMillis(): Long = baseMillis + counter++
}
