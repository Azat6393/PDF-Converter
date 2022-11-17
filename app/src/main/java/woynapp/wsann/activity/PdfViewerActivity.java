package woynapp.wsann.activity;

import static woynapp.wsann.util.Constants.AUTHORITY_APP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.poi.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import woynapp.wsann.R;
import woynapp.wsann.database.DatabaseHelper;
import woynapp.wsann.fragment.new_fragments.UpdateScreenListener;
import woynapp.wsann.interfaces.ExtractImagesListener;
import woynapp.wsann.interfaces.OnPDFCreatedInterface;
import woynapp.wsann.model.ImageToPDFOptions;
import woynapp.wsann.util.Constants;
import woynapp.wsann.util.CreatePdfCustomSize;
import woynapp.wsann.util.DialogUtils;
import woynapp.wsann.util.FileUtils;
import woynapp.wsann.util.ImageUtils;
import woynapp.wsann.util.PDFEncryptionUtility;
import woynapp.wsann.util.PDFRotationUtils;
import woynapp.wsann.util.PDFUtils;
import woynapp.wsann.util.PageSizeUtils;
import woynapp.wsann.util.PdfToImages;
import woynapp.wsann.util.StringUtils;
import woynapp.wsann.util.ThemeUtils;
import woynapp.wsann.util.WatermarkUtils;

public class PdfViewerActivity extends AppCompatActivity implements View.OnClickListener, UpdateScreenListener,
        ExtractImagesListener, OnPDFCreatedInterface {

    public static final int INTENT_REQUEST_GET_ADD_TEXT = 123;
    public static final String INTENT_RESULT_GET_ADD_TEXT = "CROP_IMAGE_EXTRA_RESULT";


    @BindView(R.id.rotate_bnt)
    LinearLayout rotateBtn;
    @BindView(R.id.crop_btn)
    LinearLayout cropBtn;
    @BindView(R.id.add_watermark_btn_pdf_viewew)
    LinearLayout addWaterMarkBtn;
    @BindView(R.id.add_text_btn_pdf_viewer)
    LinearLayout addTextBtn;

    public static ArrayList<String> mImagesUri = new ArrayList<>();

    Toolbar toolbar;
    PDFView pdfView;

    private File mFile;
    private Uri fileUri;

    private FrameLayout progressBar;

    private String[] mInputPassword;

    private DatabaseHelper mDatabaseHelper;
    private PDFEncryptionUtility mPDFEncryptionUtils;
    private WatermarkUtils mWatermarkUtils;
    private PDFRotationUtils mPDFRotationUtils;
    private PDFUtils mPDFUtils;
    private FileUtils mFileUtils;
    private ImageToPDFOptions mPdfOptions;
    private PageSizeUtils mPageSizeUtils;

    private String mHomePath;
    SharedPreferences mSharedPreferences;

    private int cropMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.getInstance().setThemeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        ButterKnife.bind(this, this);

        rotateBtn.setOnClickListener(this);
        cropBtn.setOnClickListener(this);
        addWaterMarkBtn.setOnClickListener(this);
        addTextBtn.setOnClickListener(this);
        progressBar = findViewById(R.id.progress_bar_add_text);

        toolbar = findViewById(R.id.pdf_viewer_toolbar);
        setSupportActionBar(toolbar);

        mFileUtils = new FileUtils(this);
        mPDFUtils = new PDFUtils(this);
        mPDFRotationUtils = new PDFRotationUtils(this);
        mPDFEncryptionUtils = new PDFEncryptionUtility(this);
        mWatermarkUtils = new WatermarkUtils(this);
        mDatabaseHelper = new DatabaseHelper(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        pdfView = findViewById(R.id.pdfViewer);
        mHomePath = mSharedPreferences.getString(Constants.STORAGE_LOCATION,
                StringUtils.getInstance().getDefaultStorageLocation());
        Intent intent = getIntent();
        String path = intent.getStringExtra("pdf_viewer");
        if (path.equals("")) {
            StringUtils.getInstance().showSnackbar(this, R.string.error_open_file);
        } else {
            File file = new File(path);
            mFile = file;
            initPdfViewer();
        }
    }

    private void initPdfViewer() {
        fileUri = getIntent().getData();
        if (fileUri == null) {
            cropMode = 0;
        } else {
            mFile = getFilePathFromURI(this, fileUri);
        }
        if (mPDFUtils.isPDFEncrypted(mFile.getPath())) {
            showPasswordDialog();
        } else {
            setPDF();
        }
    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int indexDisplayName = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (indexDisplayName != -1) {
                        result = cursor.getString(indexDisplayName);
                    }
                }
            } catch (Exception e) {
                Log.w("TAG", "Couldn't retrieve file name", e);
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    public File getFilePathFromURI(Context context, Uri contentUri) {
        mHomePath = mSharedPreferences.getString(Constants.STORAGE_LOCATION,
                StringUtils.getInstance().getDefaultStorageLocation());
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(mHomePath + fileName);
            if (copyFile.exists()) {
                return copyFile;
            } else {
                copy(context, contentUri, copyFile);
            }
            return copyFile;
        }
        return null;
    }

    public void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPasswordDialog() {
        mInputPassword = new String[1];
        new MaterialDialog.Builder(this)
                .title(R.string.enter_password)
                .content(R.string.decrypt_protected_file)
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input(null, null, (dialog, input) -> {
                    if (StringUtils.getInstance().isEmpty(input)) {
                        StringUtils.getInstance().showSnackbar(this, R.string.snackbar_name_not_blank);
                    } else {
                        final String inputName = input.toString();
                        mInputPassword[0] = inputName;
                        setPDF(mInputPassword[0]);
                    }
                })
                .show();
    }

    private void setPDF() {
        try {
            Uri uri = FileProvider.getUriForFile(this, AUTHORITY_APP, mFile);

            pdfView.setBackgroundColor(Color.LTGRAY);
            pdfView.fromUri(uri)
                    .enableAnnotationRendering(true)
                    .enableAntialiasing(true)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(10)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .swipeHorizontal(false)
                    .autoSpacing(false)
                    .pageSnap(false)
                    .pageFling(false)
                    .nightMode(false)
                    .load();

        } catch (Exception e) {
            StringUtils.getInstance().showSnackbar(this, R.string.error_open_file);
        }
    }

    private void setPDF(String password) {
        try {
            Uri uri = FileProvider.getUriForFile(this, AUTHORITY_APP, mFile);

            pdfView.setBackgroundColor(Color.LTGRAY);
            pdfView.fromUri(uri)
                    .enableAnnotationRendering(true)
                    .enableAntialiasing(true)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(10)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .swipeHorizontal(false)
                    .autoSpacing(false)
                    .pageSnap(false)
                    .pageFling(false)
                    .nightMode(false)
                    .password(password)
                    .load();
        } catch (Exception e) {
            StringUtils.getInstance().showSnackbar(this, R.string.error_open_file);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pdf_viewer, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
        }
        return true;
    }

    private void deleteFile() {
        int messageAlert;
        messageAlert = R.string.delete_alert_singular;
        AlertDialog.Builder dialogAlert = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .setTitle(messageAlert)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    mDatabaseHelper.insertRecord(mFile.getAbsolutePath(),
                            this.getString(R.string.deleted));
                    if (mFile.exists() && !mFile.delete())
                        StringUtils.getInstance().showSnackbar(this,
                                R.string.snackbar_file_not_deleted);
                    else {
                        this.finish();
                    }
                });
        dialogAlert.create().show();
    }

    private void onRenameFileClick() {
        new MaterialDialog.Builder(this)
                .title(R.string.creating_pdf)
                .content(R.string.enter_file_name)
                .input(this.getString(R.string.example), null, (dialog, input) -> {
                    if (input == null || input.toString().trim().isEmpty())
                        StringUtils.getInstance().showSnackbar(this, R.string.snackbar_name_not_blank);
                    else {
                        if (!mFileUtils.isFileExist(input + this.getString(R.string.pdf_ext))) {
                            renameFile(input.toString());
                        } else {
                            MaterialDialog.Builder builder = DialogUtils.getInstance().createOverwriteDialog(this);
                            builder.onPositive((dialog2, which) -> renameFile(input.toString()))
                                    .onNegative((dialog1, which) -> onRenameFileClick())
                                    .show();
                        }
                    }
                }).show();
    }

    private void renameFile(String newName) {
        File oldfile = mFile;
        String oldPath = oldfile.getPath();
        String newfilename = oldPath.substring(0, oldPath.lastIndexOf('/'))
                + "/" + newName + this.getString(R.string.pdf_ext);
        File newfile = new File(newfilename);
        if (oldfile.renameTo(newfile)) {
            StringUtils.getInstance().showSnackbar(this, R.string.snackbar_file_renamed);
            toolbar.setTitle(newName);
            mDatabaseHelper.insertRecord(newfilename, this.getString(R.string.renamed));
        } else
            StringUtils.getInstance().showSnackbar(this, R.string.snackbar_file_not_renamed);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pdf_viewer_share:
                if (fileUri != null) {
                    mFileUtils.shareFile(fileUri);
                } else {
                    mFileUtils.shareFile(mFile);
                }
                break;
            case R.id.menu_pdf_viewer_print:
                mFileUtils.printFile(mFile);
                break;
            case R.id.menu_pdf_viewer_rename:
                onRenameFileClick();
                break;
            case R.id.menu_pdf_viewer_add_password:
                mPDFEncryptionUtils.setPassword(mFile.getPath());
                break;
            case R.id.menu_pdf_viewer_delete_password:
                mPDFEncryptionUtils.removePassword(mFile.getPath());
                break;
            case R.id.menu_pdf_viewer_delete:
                deleteFile();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rotate_bnt:
                mPDFRotationUtils.rotatePages(mFile.getPath(), this);
                break;
            case R.id.crop_btn:
                progressBar.setVisibility(View.VISIBLE);
                cropMode = 1;
                Uri cropImageUri = Uri.fromFile(mFile);
                new PdfToImages(this, mInputPassword, mFile.getPath(), cropImageUri, this)
                        .execute();
                break;
            case R.id.add_text_btn_pdf_viewer:
                progressBar.setVisibility(View.VISIBLE);
                cropMode = 2;
                Uri addTextImageUri = Uri.fromFile(mFile);
                new PdfToImages(this, mInputPassword, mFile.getPath(), addTextImageUri, this)
                        .execute();

                break;
            case R.id.add_watermark_btn_pdf_viewew:
                mWatermarkUtils.setWatermark(mFile.getPath(), this);
                break;

        }
    }

    private void cropImage() {
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(PdfViewerActivity.this, CropImageActivity.class);
        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void addText() {
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(PdfViewerActivity.this, AddTextActivity.class);
        startActivityForResult(intent, PdfViewerActivity.INTENT_REQUEST_GET_ADD_TEXT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null)
            return;
        switch (requestCode) {
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                HashMap<Integer, Uri> croppedImageUris =
                        (HashMap) data.getSerializableExtra(CropImage.CROP_IMAGE_EXTRA_RESULT);

                for (int i = 0; i < mImagesUri.size(); i++) {
                    if (croppedImageUris.get(i) != null) {
                        mImagesUri.set(i, croppedImageUris.get(i).getPath());
                    }
                }
                if (!mImagesUri.isEmpty()) {
                    imageToPdf(FileUtils.getFileName(mImagesUri.get(0)));
                }
                break;
            case PdfViewerActivity.INTENT_REQUEST_GET_ADD_TEXT:
                HashMap<Integer, Uri> addTextImageUris =
                        (HashMap) data.getSerializableExtra(PdfViewerActivity.INTENT_RESULT_GET_ADD_TEXT);

                for (int i = 0; i < mImagesUri.size(); i++) {
                    if (addTextImageUris.get(i) != null) {
                        mImagesUri.set(i, addTextImageUris.get(i).getPath());
                    }
                }
                if (!mImagesUri.isEmpty()) {
                    imageToPdf(FileUtils.getFileName(mImagesUri.get(0)));
                }
                break;
        }
    }


    private void imageToPdf(String filename) {
        mPdfOptions = new ImageToPDFOptions();
        mPageSizeUtils = new PageSizeUtils(this);
        String mPageNumStyle = mSharedPreferences.getString(Constants.PREF_PAGE_STYLE, null);
        int mPageColor = mSharedPreferences.getInt(Constants.DEFAULT_PAGE_COLOR_ITP,
                Constants.DEFAULT_PAGE_COLOR);
        ImageUtils.getInstance().mImageScaleType = mSharedPreferences.getString(Constants.DEFAULT_IMAGE_SCALE_TYPE_TEXT,
                Constants.IMAGE_SCALE_TYPE_ASPECT_RATIO);


        mPdfOptions.setImagesUri(mImagesUri);
        mPdfOptions.setPageSize(PageSizeUtils.mPageSize);
        mPdfOptions.setImageScaleType(ImageUtils.getInstance().mImageScaleType);
        mPdfOptions.setPageNumStyle(mPageNumStyle);
        mPdfOptions.setMasterPwd(mSharedPreferences.getString(Constants.MASTER_PWD_STRING, Constants.appName));
        mPdfOptions.setPageColor(mPageColor);
        mPdfOptions.setOutFileName(filename);
        String path = mFile.getPath();
        new CreatePdfCustomSize(mPdfOptions, path, this).execute();
    }

    private void resetValues() {
        mPdfOptions = new ImageToPDFOptions();
        mPdfOptions.setBorderWidth(mSharedPreferences.getInt(Constants.DEFAULT_IMAGE_BORDER_TEXT,
                Constants.DEFAULT_BORDER_WIDTH));
        mPdfOptions.setQualityString(
                Integer.toString(mSharedPreferences.getInt(Constants.DEFAULT_COMPRESSION,
                        Constants.DEFAULT_QUALITY_VALUE)));
        mPdfOptions.setPageSize(mSharedPreferences.getString(Constants.DEFAULT_PAGE_SIZE_TEXT,
                Constants.DEFAULT_PAGE_SIZE));
        mPdfOptions.setPasswordProtected(false);
        mPdfOptions.setWatermarkAdded(false);
        mImagesUri.clear();
        ImageUtils.getInstance().mImageScaleType = mSharedPreferences.getString(Constants.DEFAULT_IMAGE_SCALE_TYPE_TEXT,
                Constants.IMAGE_SCALE_TYPE_ASPECT_RATIO);
        mPdfOptions.setMargins(0, 0, 0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        resetValues();
    }

    @Override
    public void resetView() {

    }

    @Override
    public void extractionStarted() {

    }

    @Override
    public void updateView(int imageCount, ArrayList<String> outputFilePaths) {
        if (!outputFilePaths.isEmpty()) {
            for (String path : outputFilePaths) {
                mImagesUri.add(path);
            }
            if (!mImagesUri.isEmpty()) {
                if (cropMode == 1) {
                    cropImage();
                } else if (cropMode == 2) {
                    addText();
                }
            }
        }
    }

    @Override
    public void onPDFCreationStarted() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPDFCreated(boolean success, String path) {
        progressBar.setVisibility(View.GONE);

        String oldPath = mFile.getPath().substring(0, mFile.getPath().lastIndexOf('/'))
                + "/" + mFileUtils.getFileName(Uri.fromFile(mFile)) + ".W Scann" + this.getString(R.string.pdf_ext);
        File oldFile = new File(oldPath);
        if (oldFile.exists()) {
            if (oldFile.delete()) {
                File newFile = new File(path);
                if (newFile.renameTo(oldFile)){
                    mFile = new File(oldPath);
                }
                for (String imagePath : mImagesUri) {
                    File file = new File(imagePath);
                    if (file.exists()) {
                        if (file.delete()) {
                            System.out.println("image Deleted :");
                        } else {
                            System.out.println("image not Deleted :");
                        }
                    }
                }
            }
        } else {
            File newFile = new File(path);
            newFile.renameTo(oldFile);
            if (newFile.renameTo(oldFile)){
                mFile = new File(oldPath);
            }
            for (String imagePath : mImagesUri) {
                File file = new File(imagePath);
                if (file.exists()) {
                    if (file.delete()) {
                        System.out.println("image Deleted :");
                    } else {
                        System.out.println("image not Deleted :");
                    }
                }
            }
        }
        mImagesUri.clear();
        initPdfViewer();
    }


    @Override
    public void updateScreen() {
        initPdfViewer();
    }
}