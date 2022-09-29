package woynapp.wsann.activity.auth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import woynapp.wsann.R;
import woynapp.wsann.activity.AuthActivity;
import woynapp.wsann.activity.InputNumberActivity;
import woynapp.wsann.activity.NewMainActivity;
import woynapp.wsann.model.User;
import woynapp.wsann.util.Constants;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private Activity mActivity;

    private SignInClient oneTapClient;
    private static final int REQ_ONE_TAP = 2;

    @BindView(R.id.sign_up)
    CardView signUpBtn;
    @BindView(R.id.email_et)
    EditText emailEditText;
    @BindView(R.id.password_et)
    EditText passwordEditText;
    @BindView(R.id.sign_in)
    CardView signIn;
    @BindView(R.id.login_with_google_btn)
    CardView logInWithGoogleBtn;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.buttons_group)
    LinearLayout buttons_group;

    BeginSignInRequest signInRequest;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_log_in, container, false);
        ButterKnife.bind(this, rootView);
        signUpBtn.setOnClickListener(this);
        signIn.setOnClickListener(this);
        logInWithGoogleBtn.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_up:
                ((AuthActivity) mActivity).navigate(SignUpFragment.newInstance());
                break;
            case R.id.sign_in:
                if (isValid()) {
                    isLoading(true);
                    String email = emailEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    logInWithEmail(email, password);
                }
                break;
            case R.id.login_with_google_btn:
                isLoading(true);
                initGoogleSignInRequest();
                beginSignIn();
                break;

        }
    }

    private void isLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            buttons_group.setVisibility(View.INVISIBLE);
        } else {
            buttons_group.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void beginSignIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult beginSignInResult) {
                        try {
                            startIntentSenderForResult(
                                    beginSignInResult.getPendingIntent().getIntentSender(), REQ_ONE_TAP, null, 0, 0, 0, null
                            );
                        } catch (Exception e) {
                            Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        isLoading(false);
                    }
                });
    }

    private void initGoogleSignInRequest() {
        oneTapClient = Identity.getSignInClient(requireActivity());
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId("174832374276-2vl6jtt609svdooqv4ke13oebo3teumu.apps.googleusercontent.com")
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .setAutoSelectEnabled(true)
                .build();
    }

    private void logInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getUser() != null) {
                                getUser(task.getResult().getUser().getUid());
                            }
                        } else {
                            Toast.makeText(requireContext(), "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                            isLoading(false);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    String firstName = credential.getGivenName();
                    String lastName = credential.getFamilyName();
                    if (idToken != null) {
                        logInWithGoogle(idToken, firstName, lastName);
                    } else isLoading(false);

                } catch (ApiException e) {
                    e.printStackTrace();
                    isLoading(false);
                }
                break;
        }
    }

    private void logInWithGoogle(String idToken, String firstName, String lastName) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        getUserIfExists(
                                firstName,
                                lastName,
                                authResult.getUser().getPhotoUrl().toString(),
                                authResult.getUser().getUid(),
                                authResult.getUser().getEmail(),
                                ""
                        );
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Sign with Google failed." + e.getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                        isLoading(false);
                    }
                });
    }

    public void getUserIfExists(String name, String surname, String photoUrl, String uuid, String email, String phoneNumber) {
        UserUtils userUtils = new UserUtils(requireActivity());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.USER_COLLECTION_NAME)
                .document(uuid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                userUtils.updateUser(user.getUser_name(), user.getSurname(), user.getUser_photo(), user.getUser_uuid(), user.getUser_email(), user.getPhone_number());
                                isLoading(false);
                                if (user.getPhone_number().equals("") || user.getPhone_number() == null){
                                    navigateToInputNumberActivity();
                                }else {
                                    navigateToNewMainActivity();
                                }
                            } else {
                                createUser(name, surname, photoUrl, uuid, email, phoneNumber);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Authentication failed." + task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void getUser(String uuid) {
        UserUtils userUtils = new UserUtils(requireActivity());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.USER_COLLECTION_NAME)
                .document(uuid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            userUtils.updateUser(user.getUser_name(),
                                    user.getSurname(),
                                    user.getUser_photo(),
                                    user.getUser_uuid(),
                                    user.getUser_email(),
                                    user.getPhone_number());
                            isLoading(false);
                            navigateToNewMainActivity();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Authentication failed." + e.getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void createUser(String name, String surname, String photoUrl, String uuid, String email, String phoneNumber) {
        UserUtils userUtils = new UserUtils(requireActivity());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put(Constants.USER_NAME, name);
        user.put(Constants.USER_SURNAME, surname);
        user.put(Constants.USER_PHOTO, photoUrl);
        user.put(Constants.USER_UUID, uuid);
        user.put(Constants.USER_EMAIL, email);
        user.put(Constants.USER_PHONE_NUMBER, phoneNumber);
        db.collection(Constants.USER_COLLECTION_NAME)
                .document(uuid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        userUtils.updateUser(name, surname, photoUrl, uuid, email, phoneNumber);
                        isLoading(false);
                        navigateToInputNumberActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Authentication failed." + e.getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void navigateToNewMainActivity(){
        Intent intent = new Intent(requireActivity(), NewMainActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finish();
    }

    public void navigateToInputNumberActivity(){
        Intent intent = new Intent(requireActivity(), InputNumberActivity.class);
        requireActivity().startActivity(intent);
        requireActivity().finish();
    }

    private boolean isValid() {
        if (emailEditText.getText().toString().equals("")) {
            Toast.makeText(getContext(), getString(R.string.please_enter_email), Toast.LENGTH_LONG).show();
            return false;
        } else if (passwordEditText.getText().toString().equals("")) {
            Toast.makeText(getContext(), getString(R.string.please_enter_email), Toast.LENGTH_LONG).show();
            return false;
        } else return true;
    }
}