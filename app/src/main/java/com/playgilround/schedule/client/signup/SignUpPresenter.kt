package com.playgilround.schedule.client.signup

import android.content.Context
import com.google.gson.JsonObject
import com.playgilround.schedule.client.ScheduleApplication
import com.playgilround.schedule.client.data.User
import com.playgilround.schedule.client.model.ResponseMessage
import com.playgilround.schedule.client.retrofit.APIClient
import com.playgilround.schedule.client.retrofit.UserAPI
import com.playgilround.schedule.client.signup.model.UserDataModel
import com.playgilround.schedule.client.signup.view.SignUpAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class SignUpPresenter constructor(mContext: Context, private val mView: SignUpContract.View, private val mUserDataModel: UserDataModel): SignUpContract.Presenter {

    companion object {
        const val ERROR_SIGN_UP = 0x0001
        const val ERROR_NETWORK_CUSTOM = 0x0002
    }

    @Inject
    internal lateinit var mUser: User

    init {
        mView.setPresenter(this)
        (mContext.applicationContext as ScheduleApplication).appComponent.signUpInject(this)
    }

    override fun start() {

    }

    override fun onClickNext(position: Int) {
        var check = false

        when (position) {
            SignUpAdapter.TYPE_NAME -> {
                mUser.username = mUserDataModel.getNameField()
                check = mUser.username != null
            }

            SignUpAdapter.TYPE_EMAIL -> {
                mUser.email = mUserDataModel.getEmailField()
                check = validateEmail(mUser.email)
            }

            SignUpAdapter.TYPE_PASSWORD -> {
                mUser.password = mUserDataModel.getPasswordField()
                check = mUser.password != null
            }

            SignUpAdapter.TYPE_REPEAT_PASSWORD -> {
                mUser.password2 = mUserDataModel.getRepeatPasswordField()
                check = mUser.password2 != null
            }

            SignUpAdapter.TYPE_NICK_NAME -> {
                mUser.nickname = mUserDataModel.getNicknameField()
                check = validateNickName(mUser.nickname)
            }

            SignUpAdapter.TYPE_BIRTH -> {
                mUser.birth = mUserDataModel.getBirthField()
                check = mUser.birth != null
            }
        }
        mView.fieldCheck(check)
    }

    override fun onClickBack(position: Int) {
        when (position) {
            SignUpAdapter.TYPE_NAME -> mUser.username = null
            SignUpAdapter.TYPE_EMAIL -> mUser.email = null
            SignUpAdapter.TYPE_PASSWORD -> mUser.password = null
            SignUpAdapter.TYPE_REPEAT_PASSWORD -> mUser.password2 = null
            SignUpAdapter.TYPE_NICK_NAME -> mUser.nickname = null
            SignUpAdapter.TYPE_BIRTH -> mUser.birth = null
        }
    }

    override fun validateEmail(email: String): Boolean {
        val retrofit = APIClient.getClient()
        val userAPI = retrofit.create(UserAPI::class.java)
        var isValidEmail = false

        runBlocking {
            launch(Dispatchers.Default) {
                val response = userAPI.checkEmail(email).execute()
                isValidEmail = response.isSuccessful && response.body() != null
            }
        }
        return isValidEmail
    }

    override fun validateNickName(nickName: String): Boolean {
        val retrofit = APIClient.getClient()
        val userAPI = retrofit.create(UserAPI::class.java)
        var isValidNickname = false

        runBlocking {
            launch(Dispatchers.Default) {
                val response = userAPI.checkNickName(nickName).execute()
                isValidNickname = response.isSuccessful && response.body() != null
            }
        }
        return isValidNickname
    }

    override fun signUp() {
        //추후 Disposable (rx) 변경 예정
        val retrofit = APIClient.getClient()
        val userAPI = retrofit.create(UserAPI::class.java)

        mUser.password = User.base64Encoding(mUser.password)
        mUser.password2 = User.base64Encoding(mUser.password2)

        val jsonObject = JsonObject()
        val userJson = JsonObject()

        userJson.addProperty("username", mUser.username)
        userJson.addProperty("nickname", mUser.nickname)
        userJson.addProperty("email", mUser.email)
        userJson.addProperty("password", mUser.password)
        userJson.addProperty("password2", mUser.password2)

        jsonObject.add("user", userJson)
        jsonObject.addProperty("birth", mUser.birth)
        jsonObject.addProperty("language", mUser.language)

        //mView.signUpComplete()
        userAPI.signUp(jsonObject).enqueue(object: Callback<ResponseMessage> {
            override fun onResponse(call: Call<ResponseMessage>, response: Response<ResponseMessage>) {
                if (response.isSuccessful && response.body() != null) {
                    mView.signUpComplete()
                } else {
                    mView.signUpError(ERROR_SIGN_UP)
                }
            }

            override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                mView.signUpError(ERROR_NETWORK_CUSTOM)
            }
        })
        /*userAPI.signUp(jsonObject).enqueue(new Callback<ResponseMessage>() {
          @Override
          public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
              if (response.isSuccessful() && response.body() != null) {
                  mView.singUpComplete();
              } else {
                  CrashlyticsCore.getInstance().log(response.toString());
                  mView.signUpError(ERROR_SIGN_UP);
              }
          }

          @Override
          public void onFailure(Call<ResponseMessage> call, Throwable t) {
              CrashlyticsCore.getInstance().log(t.toString());
              mView.signUpError(ERROR_NETWORK_CUSTOM);
          }
      });*/
    }



}