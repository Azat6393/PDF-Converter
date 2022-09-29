package woynapp.wsann.fragment.new_fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import woynapp.wsann.R;
import woynapp.wsann.activity.NewMainActivity;
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
import woynapp.wsann.model.HomePageItem;
import woynapp.wsann.util.CommonCodeUtils;
import woynapp.wsann.util.Constants;
import woynapp.wsann.util.MergePdf;
import woynapp.wsann.util.RecentUtil;

public class NewToolFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.images_to_pdf_btn)
    CardView imagesToPdf;
    @BindView(R.id.text_to_pdf_btn)
    CardView textToPdf;
    @BindView(R.id.pdf_to_images_btn)
    CardView pdfToImages;
    @BindView(R.id.excel_to_pdf_btn)
    CardView excelToPdf;
    @BindView(R.id.add_password_btn)
    CardView addPassword;
    @BindView(R.id.add_text_btn)
    CardView addText;
    @BindView(R.id.add_watermark_btn)
    CardView addWatermark;
    @BindView(R.id.merge_pdf_btn)
    CardView mergePdf;
    @BindView(R.id.split_pdf_btn)
    CardView splitPdf;
    @BindView(R.id.compress_pdf_btn)
    CardView compressPdf;
    @BindView(R.id.qr_barcode_to_pdf_btn)
    CardView qrBarcode;
    @BindView(R.id.remove_password_btn)
    CardView removePassword;

    private Activity mActivity;

    private Map<Integer, HomePageItem> mFragmentPositionMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tool_new, container, false);
        ButterKnife.bind(this, root);
        mFragmentPositionMap = CommonCodeUtils.getInstance().fillNavigationItemsMap(true);

        imagesToPdf.setOnClickListener(this);
        textToPdf.setOnClickListener(this);
        pdfToImages.setOnClickListener(this);
        excelToPdf.setOnClickListener(this);
        addPassword.setOnClickListener(this);
        addText.setOnClickListener(this);
        addWatermark.setOnClickListener(this);
        mergePdf.setOnClickListener(this);
        splitPdf.setOnClickListener(this);
        compressPdf.setOnClickListener(this);
        qrBarcode.setOnClickListener(this);
        removePassword.setOnClickListener(this);

        return root;
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
            case R.id.text_to_pdf_btn:
                fragment = new TextToPdfFragment();
                break;
            case R.id.pdf_to_images_btn:
                fragment = new PdfToImageFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.PDF_TO_IMAGES);
                fragment.setArguments(bundle);
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
            case R.id.qr_barcode_to_pdf_btn:
                fragment = new QrBarcodeScanFragment();
                break;
            case R.id.remove_password_btn:
                fragment = new RemovePagesFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.REMOVE_PWd);
                fragment.setArguments(bundle);
                break;
            case R.id.add_text_btn:
                fragment = new AddTextFragment();
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
