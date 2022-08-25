package com.firmino.neurossaude;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.firmino.neurossaude.alerts.MessageAlert;
import com.firmino.neurossaude.user.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private Button mLoginButton;
    private Button mLogoffButton;

    private FirebaseAuth mAuth;
    private FirebaseUser mAccount;
    private GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> authActivityResultLauncher;

    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mLoginButton = findViewById(R.id.Login_Login);
        mLogoffButton = findViewById(R.id.Login_Logoff);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        authActivityResultLauncher = createAuthResultLauncher();

        mLoginButton.setOnClickListener(v -> signIn());
        mLogoffButton.setOnClickListener(v -> signOut());

        mFirestore = FirebaseFirestore.getInstance();
    }

    private ActivityResultLauncher<Intent> createAuthResultLauncher() {
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(ApiException.class);
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            mAccount = mAuth.getCurrentUser();
                            signed();
                        } else {
                            signOut();
                            MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Falha ao tentar fazer login com o Google.");
                        }
                    });
                } catch (ApiException e) {
                    signOut();
                    MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Falha ao tentar fazer login com o Google.");
                }
            }
        });
    }

    public void onStart() {
        super.onStart();
        mAccount = mAuth.getCurrentUser();
        if (mAccount != null) {
            mLoginButton.setText(String.format("Continuar como %s", mAccount.getDisplayName()));
            mLoginButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_google_logged, 0, 0, 0);
            mLogoffButton.setVisibility(View.VISIBLE);
        }
    }

    private void signIn() {
        setLoadingMode();
        if (mAccount == null)
            authActivityResultLauncher.launch(mGoogleSignInClient.getSignInIntent());
        else signed();
    }

    private void signOut() {
        setLoadingMode();
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            mAuth.signOut();
            mAccount = null;
            this.recreate();
            MessageAlert.create(this, MessageAlert.TYPE_SUCESS, "Você saiu da conta.");
        });
    }

    private void signed() {
        User.email = mAccount.getEmail();
        User.username = mAccount.getDisplayName();
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("email", mAccount.getEmail());
        newUser.put("username", mAccount.getDisplayName());
        newUser.put("image", String.valueOf(mAccount.getPhotoUrl()));

        mFirestore.collection("users").document(Objects.requireNonNull(mAccount.getEmail())).set(newUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try {
                    if (mAccount.getPhotoUrl() != null) {
                        Uri uri = Uri.parse(mAccount.getPhotoUrl().toString());
                        URL newurl = new URL(uri.toString());
                        User.image = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                    } else
                        User.image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_google_logged);

                } catch (IOException e) {
                    MessageAlert.create(this, MessageAlert.TYPE_ALERT, "Não foi possível carregar imagem da conta.");
                }
                runOnUiThread(() -> {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                });
            } else {
                MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Não foi possível carregar dados da conta, tente novamente mais tarde.");
                signOut();
            }
        });
    }

    private void setLoadingMode() {
        mLoginButton.setEnabled(false);
        FrameLayout ripple = findViewById(R.id.Login_Ripple);
        ripple.setVisibility(View.VISIBLE);
        int maxSize = getResources().getDisplayMetrics().heightPixels * 2;
        ValueAnimator anim = ValueAnimator.ofInt(0, maxSize);
        anim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = ripple.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            params.width = (int) animation.getAnimatedValue();
            ripple.setLayoutParams(params);
        });
        anim.setDuration(1000);
        anim.start();
    }
}