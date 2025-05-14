package com.example.pantallalogin.ui.bienvenida

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pantallalogin.R
import android.widget.Button

class BienvenidaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenida)

        val btnOpciones = findViewById<Button>(R.id.btnIrOpciones)

        btnOpciones.setOnClickListener {
            // Ir a la pantalla de la ruleta
            val intent = Intent(this, RuletaActivity::class.java)
            startActivity(intent)
        }
    }
    class LoginActivity : AppCompatActivity() {

        private lateinit var googleSignInClient: GoogleSignInClient
        private val RC_SIGN_IN = 101   // request code arbitrario

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)

            // --- Config GoogleSignIn -------------
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            findViewById<SignInButton>(R.id.btnGoogleSignIn).setOnClickListener {
                val intent = googleSignInClient.signInIntent
                startActivityForResult(intent, RC_SIGN_IN)

                override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                    super.onActivityResult(requestCode, resultCode, data)
                    if (requestCode == RC_SIGN_IN) {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                        try {
                            val account = task.getResult(ApiException::class.java)!!
                            firebaseAuthWithGoogle(account.idToken!!)
                        } catch (e: ApiException) {
                            Toast.makeText(this, "Fallo al iniciar sesión", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                private fun firebaseAuthWithGoogle(idToken: String) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // ¡Login OK!
                                startActivity(Intent(this, BienvenidaActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Auth con Firebase falló", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }

        }
        }
    }
