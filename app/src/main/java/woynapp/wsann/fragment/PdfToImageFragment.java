package woynapp.wsann.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.dd.morphingbutton.MorphingButton;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import woynapp.wsann.R;
import woynapp.wsann.activity.ImagesPreviewActivity;
import woynapp.wsann.adapter.ExtractImagesAdapter;
import woynapp.wsann.adapter.MergeFilesAdapter;
import woynapp.wsann.fragment.new_fragments.PopUpDialog;
import woynapp.wsann.interfaces.BottomSheetPopulate;
import woynapp.wsann.interfaces.ExtractImagesListener;
import woynapp.wsann.interfaces.OnBackPressedInterface;
import woynapp.wsann.util.BottomSheetCallback;
import woynapp.wsann.util.BottomSheetUtils;
import woynapp.wsann.util.CommonCodeUtils;
import woynapp.wsann.util.DialogUtils;
import woynapp.wsann.util.ExtractImages;
import woynapp.wsann.util.FileUtils;
import woynapp.wsann.util.MorphButtonUtility;
import woynapp.wsann.util.PDFUtils;
import woynapp.wsann.util.PdfToImages;
import woynapp.wsann.util.PermissionsUtils;
import woynapp.wsann.util.RealPathUtil;
import woynapp.wsann.util.StringUtils;
import woynapp.wsann.util.Constants;

import static android.app.Activity.RESULT_OK;
import static woynapp.wsann.util.Constants.AUTHORITY_APP;

public class PdfToImageFragment extends Fragment implements BottomSheetPopulate, MergeFilesAdapter.OnClickListener,
        ExtractImagesListener, ExtractImagesAdapter.OnFileItemClickedListener, OnBackPressedInterface {

    private static final int INTENT_REQUEST_PICK_FILE_CODE = 10;
    private Activity mActivity;
    private String mPath;
    private Uri mUri;
    private MorphButtonUtility mMorphButtonUtility;
    private FileUtils mFileUtils;
    private BottomSheetBehavior mSheetBehavior;
    private BottomSheetUtils mBottomSheetUtils;
    private ArrayList<String> mOutputFilePaths;
    private MaterialDialog mMaterialDialog;
    private String mOperation;
    private Context mContext;
    private PDFUtils mPDFUtils;
    private String[] mInputPassword;

    @BindView(R.id.lottie_progress)
    LottieAnimationView mLottieProgress;
    @BindView(R.id.bottom_sheet)
    LinearLayout mLayoutBottomSheet;
    @BindView(R.id.upArrow)
    ImageView mUpArrow;
    @BindView(R.id.selectFile)
    MorphingButton mSelectFileButton;
    @BindView(R.id.createImages)
    MorphingButton mCreateImagesButton;
    @BindView(R.id.created_images)
    RecyclerView mCreatedImages;
    @BindView(R.id.pdfToImagesText)
    TextView mCreateImagesSuccessText;
    @BindView(R.id.options)
    LinearLayout options;
    @BindView(R.id.layout)
    RelativeLayout mLayout;
    @BindView(R.id.recyclerViewFiles)
    RecyclerView mRecyclerViewFiles;

    /**
     * inflates the layout for the fragment
     *
     * @param inflater           reference to inflater object
     * @param container          parent for the inflated view
     * @param savedInstanceState bundle with saved data, if any
     * @return inflated view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pdf_to_image, container, false);
        ButterKnife.bind(this, rootView);
        mOperation = getArguments().getString(Constants.BUNDLE_DATA);
        mSheetBehavior = BottomSheetBehavior.from(mLayoutBottomSheet);
        mSheetBehavior.setBottomSheetCallback(new BottomSheetCallback(mUpArrow, isAdded()));
        mLottieProgress.setVisibility(View.VISIBLE);
        mBottomSheetUtils.populateBottomSheetWithPDFs(this);
        resetView();
        getRuntimePermissions();
        return rootView;
    }

    @OnClick(R.id.viewImagesInGallery)
    void onImagesInGalleryClick() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri imagesUri = Uri.parse("content:///storage/emulated/0/PDF Converter/");
        intent.setDataAndType(imagesUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    /**
     * called when user chooses to share generated images
     */
    @OnClick(R.id.shareImages)
    void onShareFilesClick() {
        if (mOutputFilePaths != null) {
            ArrayList<File> fileArrayList = new ArrayList<>();
            for (String path : mOutputFilePaths) {
                fileArrayList.add(new File(path));
            }
            mFileUtils.shareMultipleFiles(fileArrayList);
        }
    }

    /**
     * called on click of bottom sheet
     */
    @OnClick(R.id.viewFiles)
    void onViewFilesClick() {
        mBottomSheetUtils.showHideSheet(mSheetBehavior);
    }

    /**
     * called when user chooses to view generated images
     */
    @OnClick(R.id.viewImages)
    void onViewImagesClicked() {
        mActivity.startActivity(ImagesPreviewActivity.getStartIntent(mActivity, mOutputFilePaths));
    }

    /**
     * invoked when user chooses to select a pdf file
     * initiates an intent to pick a pdf file
     */
    @OnClick(R.id.selectFile)
    public void showFileChooser() {
        startActivityForResult(mFileUtils.getFileChooser(),
                INTENT_REQUEST_PICK_FILE_CODE);
    }

    /**
     * receives intent response for selecting a pdf file
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) throws NullPointerException {
        if (data == null || resultCode != RESULT_OK || data.getData() == null)
            return;
        if (requestCode == INTENT_REQUEST_PICK_FILE_CODE) {
            mUri = data.getData();
            //Getting Absolute Path
            String path = RealPathUtil.getInstance().getRealPath(getContext(), data.getData());
            setTextAndActivateButtons(path);
        }
    }

    /**
     * invokes generation of images for pdf pages in the background by checking
     * for encryption first.
     */
    @OnClick(R.id.createImages)
    public void parse() {
        if (mPDFUtils.isPDFEncrypted(mPath)) {
            mInputPassword = new String[1];
            new MaterialDialog.Builder(mActivity)
                    .title(R.string.enter_password)
                    .content(R.string.decrypt_protected_file)
                    .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    .input(null, null, (dialog, input) -> {
                        if (StringUtils.getInstance().isEmpty(input)) {
                            StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_name_not_blank);
                        } else {
                            final String inputName = input.toString();
                            mInputPassword[0] = inputName;
                            pdfToImage(mInputPassword);
                        }
                    })
                    .show();
        } else {
            pdfToImage(mInputPassword);
        }
        PopUpDialog popUpDialog = new PopUpDialog();
        popUpDialog.show(getChildFragmentManager(), "Pop up dialog");
    }

    /**
     * Thia method handles the call to the Async process of conversion
     * from PDF to Image.
     *
     * @param mInputPassword - the password if the file is encrypted.
     */
    private void pdfToImage(String[] mInputPassword) {
        if (mOperation.equals(Constants.PDF_TO_IMAGES)) {
            new PdfToImages(mContext, mInputPassword, mPath, mUri, this)
                    .execute();
        } else
            new ExtractImages(mPath, this).execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mMorphButtonUtility = new MorphButtonUtility(mActivity);
        mFileUtils = new FileUtils(mActivity);
        mBottomSheetUtils = new BottomSheetUtils(mActivity);
        mContext = context;
        mPDFUtils = new PDFUtils(mActivity);
    }

    /**
     * handles choosing a file from bottom sheet list
     *
     * @param path path of the file on the device
     */
    @Override
    public void onItemClick(String path) {
        mUri = null;
        mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        setTextAndActivateButtons(path);
    }

    /**
     * handles text and button states
     *
     * @param path path of the file on the device
     */
    private void setTextAndActivateButtons(String path) {
        if (path == null) {
            StringUtils.getInstance().showSnackbar(mActivity, R.string.error_path_not_found);
            resetView();
            return;
        }
        mCreatedImages.setVisibility(View.GONE);
        options.setVisibility(View.GONE);
        mCreateImagesSuccessText.setVisibility(View.GONE);
        mPath = path;
        mMorphButtonUtility.setTextAndActivateButtons(path,
                mSelectFileButton, mCreateImagesButton);
    }

    /**
     * handles opening a generated image for the given pdf
     *
     * @param path path of the file on the device
     */
    @Override
    public void onFileItemClick(String path) {
        mFileUtils.openImage(path);
    }

    /**
     * initializes interactive views
     */
    @Override
    public void resetView() {
        mPath = null;
        mMorphButtonUtility.initializeButton(mSelectFileButton, mCreateImagesButton);
    }

    /**
     * displays progress indicator
     */
    @Override
    public void extractionStarted() {
        mMaterialDialog = DialogUtils.getInstance().createCustomAnimationDialog(mActivity, getString(R.string.converting_pdf_to_image));
        mMaterialDialog.show();
    }

    /**
     * updates recycler view list items based with the generated images
     *
     * @param imageCount      number of generated images
     * @param outputFilePaths path for each generated image
     */
    @Override
    public void updateView(int imageCount, ArrayList<String> outputFilePaths) {

        mMaterialDialog.dismiss();
        resetView();
        mOutputFilePaths = outputFilePaths;

        CommonCodeUtils.getInstance().updateView(mActivity, imageCount, outputFilePaths,
                mCreateImagesSuccessText, options, mCreatedImages, this);
    }

    /**
     * populates bottom sheet list with pdf files
     *
     * @param paths paths for pdf files on the device
     */
    @Override
    public void onPopulate(ArrayList<String> paths) {
        CommonCodeUtils.getInstance().populateUtil(mActivity, paths,
                this, mLayout, mLottieProgress, mRecyclerViewFiles);
    }

    @Override
    public void closeBottomSheet() {
        CommonCodeUtils.getInstance().closeBottomSheetUtil(mSheetBehavior);
    }

    @Override
    public boolean checkSheetBehaviour() {
        return CommonCodeUtils.getInstance().checkSheetBehaviourUtil(mSheetBehavior);
    }

    /***
     * check runtime permissions for storage and camera
     ***/
    private void getRuntimePermissions() {
        if (Build.VERSION.SDK_INT < 29) {
            PermissionsUtils.getInstance().requestRuntimePermissions(this,
                    Constants.WRITE_PERMISSIONS,
                    Constants.REQUEST_CODE_FOR_WRITE_PERMISSION);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivity.findViewById(R.id.fab).setVisibility(View.GONE);
        mActivity.findViewById(R.id.coordinatorLayout).setVisibility(View.GONE);
        mActivity.findViewById(R.id.kargo_bul_banner).setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        mActivity.findViewById(R.id.fab).setVisibility(View.VISIBLE);
        mActivity.findViewById(R.id.coordinatorLayout).setVisibility(View.VISIBLE);
        mActivity.findViewById(R.id.kargo_bul_banner).setVisibility(View.INVISIBLE);
    }
}