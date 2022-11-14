package woynapp.wsann.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import woynapp.wsann.R;
import woynapp.wsann.activity.auth.CommitmentFragment;
import woynapp.wsann.activity.auth.LoginFragment;
import woynapp.wsann.activity.auth.SignUpFragment;
import woynapp.wsann.util.Constants;
import woynapp.wsann.util.ThemeUtils;

public class AuthActivity extends AppCompatActivity {

    private SharedPreferences mSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.getInstance().setThemeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAccepted = mSharedPreferences.getBoolean("privacy_policy", false);
        if (isAccepted) {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, LoginFragment.newInstance())
                        .commitNow();
            }
        } else {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, CommitmentFragment.newInstance())
                        .commitNow();
            }
        }

    }

    public void navigate(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow();
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof SignUpFragment) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, LoginFragment.newInstance())
                    .commitNow();
        } else {
            super.onBackPressed();
        }
    }
}