package com.example.webmvcemployees.util

import com.example.webmvcemployees.Boundary.BirthDate
import java.time.LocalDate

fun BirthDate.toLocalDate(): LocalDate = LocalDate.of(
    this.year.toInt(),
    this.month.toInt(),
    this.day.toInt()
)

fun LocalDate.toBirthDate(): BirthDate = BirthDate(
    day = this.dayOfMonth.toString().padStart(2, '0'),
    month = this.monthValue.toString().padStart(2, '0'),
    year = this.year.toString()
)
