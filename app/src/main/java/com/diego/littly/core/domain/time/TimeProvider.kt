package com.diego.littly.core.domain.time

import java.time.LocalDate
import javax.inject.Inject

interface TimeProvider {
    fun today(): LocalDate
    fun nowMillis(): Long
}

class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun today(): LocalDate = LocalDate.now()
    override fun nowMillis(): Long = System.currentTimeMillis()
}
