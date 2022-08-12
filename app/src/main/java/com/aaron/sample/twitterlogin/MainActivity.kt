package com.aaron.sample.twitterlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.TwitterAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.models.User


class MainActivity : AppCompatActivity() {

    private val firebaseAuth: FirebaseAuth by lazy { Firebase.auth }
    private val twitterAuthClient: TwitterAuthClient by lazy {
        TwitterAuthClient()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Twitter.initialize(
            TwitterConfig.Builder(this)
                .twitterAuthConfig(TwitterAuthConfig("h72oh63PDBk1IBBcT2El73gTQ", "3xp42Su157XKROwkF7rhyu8vPEhPRkZsiP97ZPq3tAj5yqDVeE"))
                .debug(true)
                .build()
        )

        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button>(R.id.btn_login)
        btnLogin.setOnClickListener {
            authTwitter()
        }

        val btnLoginFirebase = findViewById<Button>(R.id.btn_login_firebase)
        btnLoginFirebase.setOnClickListener {
            authFirebaseTwitter()
        }
    }

    private fun authFirebaseTwitter() {
        val provider = OAuthProvider.newBuilder("twitter.com").build()
        firebaseAuth
            .startActivityForSignInWithProvider(this, provider)
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "[test] success - ${authResult.credential?.provider}")
                Log.d(TAG, "[test] success - ${authResult.credential?.signInMethod}")
                toast("startActivityForSignInWithProvider - Success")
            }
            .addOnFailureListener { t ->
                Log.e(TAG, "[test] failed - ${t.message}", )
                firebaseAuth.signOut()
                toast("startActivityForSignInWithProvider - Failed")
            }
    }

    private fun authTwitter() {
        twitterAuthClient.authorize(this, object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession?>) {
                Log.d(TAG, "[test] authorize - success")
                val twitterSession = result.data
                Log.d(TAG, "[test] authorize - token: ${twitterSession?.authToken?.token}")
                Log.d(TAG, "[test] authorize - secret: ${twitterSession?.authToken?.secret}")

                TwitterCore.getInstance()
                    .getApiClient(twitterSession)
                    .accountService
                    .verifyCredentials(false, false, true)
                    .enqueue(object : Callback<User>() {
                        override fun success(result: Result<User?>) {
                            Log.d(TAG, "[test] verifyCredentials - success")
                            Log.d(TAG, "[test] verifyCredentials - ${twitterSession?.authToken?.token}")
                            Log.d(TAG, "[test] verifyCredentials - ${twitterSession?.authToken?.secret}")
                            Log.d(TAG, "[test] verifyCredentials - ${twitterSession?.userId}")
                            Log.d(TAG, "[test] verifyCredentials - ${result.data?.email}")
                        }

                        override fun failure(e: TwitterException) {
                            Log.e(TAG, "[test] verifyCredentials - failed: ${e.message}", e)
                        }
                    })

//                twitterSession?.authToken?.let { authToken ->
//                    val credential = TwitterAuthProvider.getCredential(authToken.token, authToken.secret)
//                    firebaseAuth.signInWithCredential(credential)
//                        .addOnSuccessListener {
//                            Log.d(TAG, "[test] signInWithCredential - success")
//                            Log.d(TAG, "[test] signInWithCredential - credential: ${it.credential}")
//                            Log.d(TAG, "[test] signInWithCredential - user: ${it.user}")
//                            toast("signInWithCredential - Success")
//                        }
//                        .addOnFailureListener { t ->
//                            Log.e(TAG, "[test] signInWithCredential - failed: ${t.message}", )
//                            toast("signInWithCredential - Failed")
//                        }
//                }

            }

            override fun failure(e: TwitterException) {
                Log.e(TAG, "[test] authorize - failed: ${e.message}", )
                twitterAuthClient.cancelAuthorize()
                toast("authorize - Failed")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "[test] onActivityResult - $requestCode, $resultCode, ${data?.extras}")
        twitterAuthClient.onActivityResult(requestCode, resultCode, data)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}