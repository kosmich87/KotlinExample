package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import ru.skillbranch.kotlinexample.extensions.email
import ru.skillbranch.kotlinexample.extensions.isValidEmail
import ru.skillbranch.kotlinexample.extensions.phone
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    var email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {

    val userInfo: String

    private val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .capitalize()

    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .map { it.first().toUpperCase() }
            .joinToString(" ")

    private var phone: String? = null
        set(value) {
            field = value?.phone()//replace("""[^+\d]""".toRegex(), "")
        }

    private var _login: String? = null
    var login: String
        set(value) {
            _login = value.toLowerCase()
        }
        get() = _login!!

    private var salt: String? = null

    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null

    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ) : this(
        firstName = firstName,
        lastName = lastName,
        email = email,
        meta = mapOf("auth" to "password")
    ) {
        println("Secondary email constructor")
        passwordHash = encrypt(password)
    }

    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("Secondary phone constructor")
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        println("PasswordHash is ${passwordHash}")
        accessCode = code
        sendAccessCodeToUser(rawPhone, code)
    }

    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        phone: String?,
        salt: String,
        hash: String
    ) : this(firstName, lastName, email = email, rawPhone = phone, meta = mapOf("src" to "csv")) {
        passwordHash = hash
        this.salt = salt
    }

    init {
        check(firstName.isNotBlank()) { "FirstName must not be blank" }
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) { "Email or phone must be not null or blank" }

        phone = rawPhone
        login = email ?: phone!!

        userInfo = """
      firstName: $firstName
      lastName: $lastName
      login: $login
      fullName: $fullName
      initials: $initials
      email: $email
      phone: $phone
      meta: $meta
      """.trimIndent()
    }

    fun checkPassword(pass: String) = encrypt(pass) == passwordHash.also {
        println("Checking passwordHash is $passwordHash")
    }

    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass)) {
            passwordHash = encrypt(newPass)
            if (!accessCode.isNullOrEmpty()) accessCode = newPass
            println("Password $oldPass has been change on new password $newPass")
        } else throw IllegalArgumentException("The entered password doesn't match the current password")
    }

    private fun encrypt(password: String): String {
        if (salt.isNullOrEmpty()) {
            salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
        }
        println("Salt: $salt")
        return salt.plus(password).md5()
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }

    fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also { index ->
                    append(possible[index])
                }
            }
        }.toString()
    }

    fun sendAccessCodeToUser(phone: String, code: String) {
        println("..... sending access code: $code to $phone")
    }

    companion object Factory {
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null,
            saltHash: String? = null
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()
            var phoneTmp = phone?.phone()

            if (!phoneTmp.isNullOrBlank()) {
                phoneTmp
                    .let {
                        if (it.first() != '+' ||
                            it.length != 12
                                ) throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
                    }
            }

            var salt: String? = null
            var hash: String? = null
            if (!saltHash.isNullOrBlank()){
                var (s, h) = saltHash.split(':')
                salt = s
                hash = h
            }


            return when {
                phoneTmp != null && email != null ->
                    User(firstName, lastName, if (email.isBlank()) null else email, if (phoneTmp.isBlank()) null else phoneTmp, salt = salt!!, hash = hash!!)
                !phoneTmp.isNullOrBlank() -> User(firstName, lastName, phoneTmp)
                !email.isNullOrBlank() && !password.isNullOrBlank() ->
                    User(
                        firstName,
                        lastName,
                        email,
                        password
                    )
                else -> throw  IllegalArgumentException("Email or phone must not be null or blank")
            }
        }

        private fun String.fullNameToPair(): Pair<String, String?> =
            this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when (size) {
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException("FullName must contain only first name and last " +
                                "name, current split result: ${this@fullNameToPair}")
                    }
                }
    }


}
