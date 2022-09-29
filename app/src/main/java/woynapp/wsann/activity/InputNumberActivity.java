package woynapp.wsann.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import woynapp.wsann.R;
import woynapp.wsann.activity.auth.UserUtils;
import woynapp.wsann.util.Constants;
import woynapp.wsann.util.ThemeUtils;

public class InputNumberActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView closeBtn;
    Button continueBtn;
    EditText numberEt;
    UserUtils user;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.getInstance().setThemeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_number);

        closeBtn = findViewById(R.id.close_button);
        continueBtn = findViewById(R.id.continue_btn);
        numberEt = findViewById(R.id.phone_number_edit_text);
        progressBar = findViewById(R.id.progress_bar);

        closeBtn.setOnClickListener(this);
        continueBtn.setOnClickListener(this);
        user = new UserUtils(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_button:
                navigateToAuthActivity();
                break;
            case R.id.continue_btn:
                progressBar.setVisibility(View.VISIBLE);
                continueBtn.setVisibility(View.GONE);
                if (numberEt.getText().toString().equals("")) {
                    Toast.makeText(this, getString(R.string.please_enter_phone_number), Toast.LENGTH_LONG).show();
                } else {
                    String phoneNumber = numberEt.getText().toString();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection(Constants.USER_COLLECTION_NAME)
                            .document(user.getUser().getUser_uuid())
                            .update(Constants.USER_PHONE_NUMBER, phoneNumber)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    user.updatePhoneNumber(phoneNumber);
                                    Intent intent = new Intent(InputNumberActivity.this, NewMainActivity.class);
                                    startActivity(intent);
                                    finish();                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(InputNumberActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                    continueBtn.setVisibility(View.VISIBLE);
                                }
                            });
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        navigateToAuthActivity();
    }

    public void navigateToAuthActivity() {
        FirebaseAuth.getInstance().signOut();
        user.deleteUser();
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}