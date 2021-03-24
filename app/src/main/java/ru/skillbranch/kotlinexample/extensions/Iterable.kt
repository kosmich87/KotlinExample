package ru.skillbranch.kotlinexample.extensions

import android.util.Patterns

fun String.phone() = this.filter { it.isDigit() || it in setOf('+')}

fun String.email() = this.trim().toLowerCase()

fun String.isValidEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): () -> List<T> = {
    this.dropLastWhile(predicate)
    this.dropLast(1)
}