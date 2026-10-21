package com.diego.littly.core.domain.model

enum class HabitIcon(val key: String) {
    SPARKLES("sparkles"),
    BRUSH("brush"),
    BOOK("book"),
    WALK("walk"),
    MEDITATE("meditate"),
    FITNESS("fitness"),
    MUSIC("music"),
    LEAF("leaf"),
    WATER("water"),
    SLEEP("sleep"),
    CODE("code"),
    HEART("heart"),
    TASK("task"),
    CAMERA("camera"),
    PET("pet"),
    COFFEE("coffee");

    companion object {
        val default: HabitIcon = SPARKLES

        fun fromKey(key: String?): HabitIcon =
            entries.firstOrNull { it.key == key } ?: default
    }
}
