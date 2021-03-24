package ru.skillbranch.kotlinexample.extensions

import android.util.Patterns

fun String.phone() = this.filter { it.isDigit() || it in setOf('+')}

fun String.email() = this.trim().toLowerCase()

fun String.isValidEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

/*fun List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T>{

}*/