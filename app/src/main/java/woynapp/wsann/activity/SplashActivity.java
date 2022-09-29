package woynapp.wsann.activity;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import woynapp.wsann.activity.auth.UserUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(SplashActivity.this, AuthActivity.class));
        } else {
            UserUtils user = new UserUtils(this);
            if (user.getUser().getPhone_number().equals("")) {
                startActivity(new Intent(SplashActivity.this, InputNumberActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, NewMainActivity.class));
            }
        }
        finish();
    }
}