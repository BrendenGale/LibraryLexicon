package edu.weber.cs.w1283820.librarylexicon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInFragment extends Fragment {

    private View root;
    private SignInButton signInButton;
    private GoogleSignInClient client;
    private FirebaseAuth mAuth;
    private signInFrag mCallBack;

    public interface signInFrag{
        public void signedIn(FirebaseUser user, GoogleSignInClient client);
    }

    public SignInFragment() {
        // Required empty public constructor
    }

    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        try {
            mCallBack = (SignInFragment.signInFrag) activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + "Must implement signInFrag");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return root = inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        signInButton = root.findViewById(R.id.signInButton);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions signInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        client = GoogleSignIn.getClient(getContext(), signInOptions);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = client.getSignInIntent();
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(getContext(), "Sign in Successful", Toast.LENGTH_SHORT).show();
                FirebaseAuth(account);
            }
            catch(ApiException e){
                Toast.makeText(getContext(), "Sign in Failed", Toast.LENGTH_SHORT).show();
                FirebaseAuth(null);
            }
        }

    }

    private void FirebaseAuth(GoogleSignInAccount account){
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    mCallBack.signedIn(user, client);
                    Toast.makeText(getContext(), user.getDisplayName() + " Signed In", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(), "Firebase Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}