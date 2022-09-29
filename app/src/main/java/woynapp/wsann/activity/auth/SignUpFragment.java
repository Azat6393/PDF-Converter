package woynapp.wsann.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import woynapp.wsann.R;
import woynapp.wsann.activity.NewMainActivity;
import woynapp.wsann.util.Constants;

public class SignUpFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @BindView(R.id.email_et)
    EditText emailEt;
    @BindView(R.id.password_et)
    EditText passwordEt;
    @BindView(R.id.confirm_password)
    EditText confirmPasswordEt;
    @BindView(R.id.name_et)
    EditText nameEt;
    @BindView(R.id.last_name_et)
    EditText lastNameEt;
    @BindView(R.id.sign_up)
    CardView signUpBtn;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.phone_number)
    EditText phoneNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);
        ButterKnife.bind(this, rootView);
        signUpBtn.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        return rootView;
    }

    public void createAccount(String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            createUser(
                                    nameEt.getText().toString(),
                                    lastNameEt.getText().toString(),
                                    "",
                                    user.getUid(),
                                    email,
                                    phoneNumber.getText().toString()
                            );
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Authentication failed." + e.getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                        isLoading(false);
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
                        Intent intent = new Intent(requireActivity(), NewMainActivity.class);
                        requireActivity().startActivity(intent);
                        requireActivity().finish();
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


    private void isLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            signUpBtn.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            signUpBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_up:
                if (isValid()) {
                    isLoading(true);
                    createAccount(emailEt.getText().toString(), passwordEt.getText().toString());
                }
                break;
        }
    }

    private boolean isValid() {
        if (emailEt.getText().toString().equals("")) {
            Toast.makeText(getContext(), getString(R.string.please_enter_email), Toast.LENGTH_LONG).show();
            return false;
        } else if (nameEt.getText().toString().equals("") || lastNameEt.getText().toString().equals("")) {
            Toast.makeText(getContext(), getString(R.string.please_enter_name), Toast.LENGTH_LONG).show();
            return false;
        } else if (passwordEt.getText().toString().equals("")) {
            Toast.makeText(getContext(), getString(R.string.please_enter_email), Toast.LENGTH_LONG).show();
            return false;
        } else if (!passwordEt.getText().toString().equals(confirmPasswordEt.getText().toString())) {
            Toast.makeText(getContext(), getString(R.string.confirm_password_message), Toast.LENGTH_LONG).show();
            return false;
        } else if (phoneNumber.getText().toString().equals("")) {
            Toast.makeText(getContext(), getString(R.string.please_enter_phone_number), Toast.LENGTH_LONG).show();
            return false;
        } else return true;
    }
}
