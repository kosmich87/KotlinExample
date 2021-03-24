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
    ) : User = User.makeUser(fullName, email = email, password = password)
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
            loginTmp = login
        }
        map[loginTmp].let {
            it?.changePassword(it.accessCode!!, it?.generateAccessCode())
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
        map.clear()
    }

    fun importUsers(list: List<String>): List<User>{
        var listOfUser: List<User> = emptyList()
        for (str in list){
            var (fullName, email, saltHash, phone) = str.split(';').map {it.trim()}
            var user: User = User.makeUser(fullName, email = email, phone = phone, saltHash = saltHash)
                .also { user ->
                    if (!checkUserIsCreated(user.login)){
                        map[user.login] = user
                    }
                }
            listOfUser.plus(user)
        }
        return listOfUser
    }
}