package woynapp.wsann.fragment.new_fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import woynapp.wsann.R;
import woynapp.wsann.activity.MainActivity;
import woynapp.wsann.activity.NewMainActivity;
import woynapp.wsann.adapter.RecentListAdapter;
import woynapp.wsann.fragment.AddTextFragment;
import woynapp.wsann.fragment.ExceltoPdfFragment;
import woynapp.wsann.fragment.ImageToPdfFragment;
import woynapp.wsann.fragment.QrBarcodeScanFragment;
import woynapp.wsann.fragment.RemovePagesFragment;
import woynapp.wsann.fragment.ViewFilesFragment;
import woynapp.wsann.fragment.texttopdf.TextToPdfFragment;
import woynapp.wsann.model.HomePageItem;
import woynapp.wsann.util.CommonCodeUtils;
import woynapp.wsann.util.Constants;
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

    private RecentListAdapter mAdapter;

    private Map<Integer, HomePageItem> mFragmentPositionMap;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_new, container, false);
        ButterKnife.bind(this, rootView);
        mFragmentPositionMap = CommonCodeUtils.getInstance().fillNavigationItemsMap(true);

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

