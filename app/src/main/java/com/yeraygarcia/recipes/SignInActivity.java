package com.yeraygarcia.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.yeraygarcia.recipes.database.remote.ResourceData;
import com.yeraygarcia.recipes.database.remote.RetrofitInstance;
import com.yeraygarcia.recipes.database.remote.User;
import com.yeraygarcia.recipes.database.remote.Webservice;
import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.util.NetworkUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private static final int SIGNING_IN = 1;
    private static final int SIGNED_IN = 2;
    private static final int SIGNED_OUT = 3;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton mGoogleSignInButton;
    private ProgressBar mProgressBar;
    private TextView mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        setupGoogleSignInClient();
        setupGoogleSignInButton();
        setupProgressBar();
        setupInfoTextView();
    }

    private void setupGoogleSignInClient() {
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build());
    }

    private void setupGoogleSignInButton() {
        mGoogleSignInButton = findViewById(R.id.google_sign_in_button);
        mGoogleSignInButton.setSize(SignInButton.SIZE_WIDE);
        mGoogleSignInButton.setOnClickListener(v -> signIn());
    }

    private void setupProgressBar() {
        mProgressBar = findViewById(R.id.progressbar_signing_in);
    }

    private void setupInfoTextView() {
        mInfo = findViewById(R.id.textview_sign_in_info);
    }

    private void setStatus(int status) {
        switch (status) {
            case SIGNING_IN:
                mInfo.setText(R.string.sign_in_signing_in);
                mInfo.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                mGoogleSignInButton.setEnabled(false);
                break;
            case SIGNED_IN:
                mInfo.setText(R.string.sign_in_signed_in);
                mInfo.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                mGoogleSignInButton.setEnabled(false);
                break;
            case SIGNED_OUT:
            default:
                mInfo.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                mGoogleSignInButton.setEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setStatus(SIGNING_IN);
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this, this::handleSignInResult);
    }

    private void signIn() {
        setStatus(SIGNING_IN);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            Debug.d(this, idToken);

            if (idToken != null) {

                if (NetworkUtil.isOnline(this)) {

                    Webservice webservice = RetrofitInstance.getStandardRetrofitInstance().create(Webservice.class);

                    Call<ResourceData<User>> call = webservice.postToken(idToken);

                    call.enqueue(new Callback<ResourceData<User>>() {
                        @Override
                        public void onResponse(@NonNull Call<ResourceData<User>> call, @NonNull Response<ResourceData<User>> response) {
                            Debug.d(this, "call successful");
                            if (response.isSuccessful()) {
                                ResourceData<User> body = response.body();
                                if (body != null && body.getResult() != null & body.getResult().getToken() != null) {
                                    updateUI(account);
                                } else {
                                    updateUI(null, getString(R.string.sign_in_error_empty_response));
                                }
                            } else {
                                updateUI(null, response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResourceData<User>> call, @NonNull Throwable t) {
                            Debug.d(this, t.getMessage());
                            updateUI(null, t.getMessage());
                        }
                    });
                } else {
                    // sign in the user in offline mode
                    updateUI(account);
                }
            } else {
                updateUI(null, getString(R.string.sign_in_error_empty_token));
            }
        } catch (ApiException e) {
            Debug.d(this, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        updateUI(account, null);
    }

    private void updateUI(GoogleSignInAccount account, String errorMessage) {
        Debug.d(this, "updateUI(account, errorMessage)");
        if (account != null) {
            setStatus(SIGNED_IN);
            RetrofitInstance.setIdToken(account.getIdToken());
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            setStatus(SIGNED_OUT);
            if (errorMessage != null) {
                mInfo.setText(R.string.sign_in_error_fail);
                mInfo.setError(errorMessage);
                mInfo.setVisibility(View.VISIBLE);
            }
        }
    }
}
