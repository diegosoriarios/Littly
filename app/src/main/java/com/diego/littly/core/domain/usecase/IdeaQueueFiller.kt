package com.diego.littly.core.domain.usecase

interface IdeaQueueFiller {
    suspend fun fillQueue(habitId: Long, days: Int = 5)
    suspend fun fillAllQueues(days: Int = 5)
}
