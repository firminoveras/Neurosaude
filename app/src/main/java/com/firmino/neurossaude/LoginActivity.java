package com.firmino.neurossaude;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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

    //TODO: fazer com que seja impossivel conectar sem internet.

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
                            MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Falha ao tentar fazer login com o Google.");
                            setLoadingMode(false);
                        }
                    });
                } catch (ApiException e) {
                    MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Falha ao tentar fazer login com o Google.");
                    setLoadingMode(false);
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
        setLoadingMode(true);
        if (mAccount == null) {
            authActivityResultLauncher.launch(mGoogleSignInClient.getSignInIntent());
        } else {
            signed();
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        this.recreate();
        MessageAlert.create(this, MessageAlert.TYPE_SUCESS, "Você saiu da conta.");
    }

    private void signed() {
        User.email = mAccount.getEmail();
        User.username = mAccount.getDisplayName();


        mFirestore.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> newUser = new HashMap<>();
                newUser.put("email", mAccount.getEmail());
                newUser.put("username", mAccount.getDisplayName());
                newUser.put("image", String.valueOf(mAccount.getPhotoUrl()));
                mFirestore.collection("users").document(Objects.requireNonNull(mAccount.getEmail())).set(newUser);
                new Thread(() -> {
                    try {
                        if (mAccount.getPhotoUrl() != null) {
                            Uri uri = Uri.parse(mAccount.getPhotoUrl().toString());
                            URL newurl = new URL(uri.toString());
                            User.image = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                        }else{
                            User.image =BitmapFactory.decodeResource(getResources(),R.drawable.ic_google_logged);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(() -> {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    });
                }).start();
            } else {
                MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Não foi possível carregar dados da conta, tente novamente mais tarde.");
                setLoadingMode(false);
            }
        });
    }

    private void setLoadingMode(boolean isOn) {
        mLoginButton.setEnabled(!isOn);
        FrameLayout ripple = findViewById(R.id.Login_Ripple);
        ripple.setVisibility(View.VISIBLE);
        int maxSize = getResources().getDisplayMetrics().heightPixels * 2;
        ValueAnimator anim = ValueAnimator.ofInt(isOn ? 0 : maxSize, isOn ? maxSize : 0);
        anim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = ripple.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            params.width = (int) animation.getAnimatedValue();
            ripple.setLayoutParams(params);
        });
        findViewById(R.id.Login_Progress).setVisibility(isOn ? View.VISIBLE : View.GONE);
        anim.setDuration(1000);
        anim.start();
    }
}