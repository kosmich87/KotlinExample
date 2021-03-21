package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import ru.skillbranch.kotlinexample.extensions.email
import ru.skillbranch.kotlinexample.extensions.phone
import kotlin.math.log

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ) : User = User.makeUser(fullName, email = email.email(), password = password)
        .also { user ->
            if (checkUserIsCreated(user.login)){
                throw IllegalArgumentException("A user with this email already exists")
            }else {
                map[user.login] = user
            }
            }

    fun checkUserIsCreated(login: String): Boolean = map.containsKey(login)

    fun loginUser(login: String, password: String): String? {
        var loginTmp = login
        if (!login.contains('@', false)){
            loginTmp = login.phone()
        }
        return map[loginTmp.trim()]?.let {
            if (it.checkPassword(password)) it.userInfo
            else null
        }
    }

    fun registerUserByPhone(
        fullName: String,
        rawPhone: String
    ): User = User.makeUser(fullName, phone = rawPhone)
        .also { user ->
            if (checkUserIsCreated(user.login)) {
                throw IllegalArgumentException("A user with this phone already exists")
            } else {
                map[user.login] = user
            }
        }

    fun requestAccessCode(login: String){
        var loginTmp = login
        if (!login.contains('@', false)){
            loginTmp = login.phone()
        }else {
            loginTmp = login.email()
        }
        map[loginTmp].let {
            it?.changePassword(it.accessCode!!, it?.generateAccessCode())
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
        map.clear()
    }
}