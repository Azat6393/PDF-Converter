package woynapp.wsann.activity.auth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import woynapp.wsann.R;
import woynapp.wsann.activity.AuthActivity;
import woynapp.wsann.activity.WebViewActivity;

public class CommitmentFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.privacy_policy_btn)
    TextView privacyPolicyBtn;
    @BindView(R.id.terms_of_service_btn)
    TextView termsOfServiceBtn;
    @BindView(R.id.agree_check_box)
    CheckBox agreeCheck;
    @BindView(R.id.continue_btn)
    Button continueBtn;

    private Activity mActivity;


    private SharedPreferences sharedPreferences;

    public static CommitmentFragment newInstance() {
        return new CommitmentFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_commitment, container, false);
        ButterKnife.bind(this, rootView);
         privacyPolicyBtn.setOnClickListener(this);
         termsOfServiceBtn.setOnClickListener(this);
         continueBtn.setOnClickListener(this);
        return rootView;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.privacy_policy_btn:
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra("url","http://woynapp.com/gizlilik-politikasi/");
                startActivity(intent);
                break;
            case R.id.terms_of_service_btn:
                Intent intentTwo = new Intent(getActivity(), WebViewActivity.class);
                intentTwo.putExtra("url","http://woynapp.com/terms-of-service/");
                startActivity(intentTwo);
                break;
            case R.id.continue_btn:
                if (agreeCheck.isChecked()){
                    sharedPreferences.edit().putBoolean("privacy_policy", true).apply();
                    ((AuthActivity) mActivity).navigate(LoginFragment.newInstance());
                }else {
                    Toast.makeText(requireContext(), getString(R.string.please_accept_permission), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
