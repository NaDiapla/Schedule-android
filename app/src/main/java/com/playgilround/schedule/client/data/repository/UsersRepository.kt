package com.playgilround.schedule.client.data.repository

import android.content.Context
import android.content.Intent
import com.playgilround.schedule.client.data.User
import com.playgilround.schedule.client.data.source.UsersDataSource
import com.playgilround.schedule.client.data.source.network.UsersRemoteDataSource

class UsersRepository(
        private val usersLocalDataSource: UsersDataSource,
        private val usersRemoteDataSource: UsersRemoteDataSource) : UsersDataSource, UsersDataSource.SNSLogin {

    override fun getCurrentUser(context: Context): User? {
        return usersLocalDataSource.getCurrentUser(context)
    }

    override fun login(email: String, password: String, loginCallBack: UsersDataSource.LoginCallBack) {
        usersRemoteDataSource.login(email, password, loginCallBack)
    }

    override fun tokenLogin(loginCallBack: UsersDataSource.LoginCallBack) {
        usersRemoteDataSource.tokenLogin(loginCallBack)
    }

    override fun googleLogin(): Intent {
        return usersRemoteDataSource.googleLogin()
    }

    override fun firebaseAuthGoogle(data: Intent) {
        usersRemoteDataSource.firebaseAuthGoogle(data)
    }

}