package woynapp.wsann.fragment;

import static woynapp.wsann.R.string.fui_error_unknown;
import static woynapp.wsann.util.Constants.THEME_BLACK;
import static woynapp.wsann.util.Constants.THEME_DARK;
import static woynapp.wsann.util.Constants.THEME_SYSTEM;
import static woynapp.wsann.util.Constants.THEME_WHITE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.Font;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
//import butterknife.OnClick;
//import lib.folderpicker.FolderPicker;
import woynapp.wsann.R;
import woynapp.wsann.activity.AboutUsActivity;
import woynapp.wsann.activity.AuthActivity;
import woynapp.wsann.activity.WebViewActivity;
import woynapp.wsann.activity.auth.UserUtils;
import woynapp.wsann.adapter.ProfileSettingsAdapter;
import woynapp.wsann.interfaces.OnItemClickListener;
import woynapp.wsann.model.EnhancementOptionsEntity;
import woynapp.wsann.model.User;
import woynapp.wsann.util.CircleTransform;
import woynapp.wsann.util.Constants;
import woynapp.wsann.util.DialogUtils;
import woynapp.wsann.util.ImageUtils;
import woynapp.wsann.util.PageSizeUtils;
import woynapp.wsann.util.SharedPreferencesUtil;
import woynapp.wsann.util.StringUtils;
import woynapp.wsann.util.ThemeUtils;
import woynapp.wsann.util.SettingsOptions;

public class SettingsFragment extends Fragment implements OnItemClickListener, View.OnClickListener {

    @BindView(R.id.settings_list)
    RecyclerView mEnhancementOptionsRecycleView;

    @BindView(R.id.log_out)
    CardView logOutBtn;
    @BindView(R.id.rate_us)
    CardView rate_us;
    @BindView(R.id.about_us)
    CardView about_us;
    @BindView(R.id.website)
    CardView website;
    @BindView(R.id.privacy_policy)
    CardView privacy_policy;
    @BindView(R.id.terms_of_service)
    CardView terms_of_service;

    @BindView(R.id.profile_photo)
    ImageView profilePhoto;
    @BindView(R.id.user_name)
    EditText userNameTv;
    @BindView(R.id.save_btn)
    Button saveBtn;
    @BindView(R.id.edit_btn)
    ImageView editBtn;
    @BindView(R.id.edit_photo_icon)
    ImageView editPhotoIcon;

    FirebaseAuth mAuth;

    User user;

    private Activity mActivity;
    private SharedPreferences mSharedPreferences;

    UserUtils userUtils;

    boolean isEditMode = false;
    Uri selectedUri = null;

    private ActivityResultLauncher<Intent> pickSingleMediaLauncher;
    private ActivityResultLauncher<String> getContent;

    private void registerPickSingleMediaLauncher(){
        pickSingleMediaLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), it -> {
                    if (it.getResultCode() == Activity.RESULT_OK) {
                        selectedUri = it.getData().getData();
                        Picasso.with(requireContext())
                                .load(it.getData().getData())
                                .resize(800, 0)
                                .placeholder(R.drawable.avatar_icon)
                                .error(R.drawable.avatar_icon)
                                .transform(new CircleTransform())
                                .into(profilePhoto);
                        if (it.getData().getData() == null || it.getData().getData().equals("")) {
                            if (!user.getUser_photo().equals("") && user.getUser_photo() != null) {
                                Picasso.with(requireContext())
                                        .load(user.getUser_photo())
                                        .placeholder(R.drawable.avatar_icon)
                                        .error(R.drawable.avatar_icon)
                                        .into(profilePhoto);
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), getString(fui_error_unknown), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void registerGetContent(){
        getContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    selectedUri = uri;
                    Picasso.with(requireContext())
                            .load(uri)
                            .resize(800, 0)
                            .placeholder(R.drawable.avatar_icon)
                            .error(R.drawable.avatar_icon)
                            .transform(new CircleTransform())
                            .into(profilePhoto);
                    if (uri == null || uri.equals("")) {
                        if (!user.getUser_photo().equals("") && user.getUser_photo() != null) {
                            Picasso.with(requireContext())
                                    .load(user.getUser_photo())
                                    .placeholder(R.drawable.avatar_icon)
                                    .error(R.drawable.avatar_icon)
                                    .into(profilePhoto);
                        }
                    }
                });
    }


    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (isEditMode) {
                        getContent.launch("image/*");
                    }
                }
            });

    public SettingsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPickSingleMediaLauncher();
        registerGetContent();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pickSingleMediaLauncher.unregister();
        getContent.unregister();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, root);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        userUtils = new UserUtils(requireActivity());

        user = userUtils.getUser();
        userNameTv.setText(user.getUser_name() + " " + user.getSurname());
        if (!user.getUser_photo().equals("") && user.getUser_photo() != null) {
            Picasso.with(requireContext())
                    .load(user.getUser_photo())
                    .resize(800, 0)
                    .placeholder(R.drawable.avatar_icon)
                    .error(R.drawable.avatar_icon)
                    .transform(new CircleTransform())
                    .into(profilePhoto);
        }

        mAuth = FirebaseAuth.getInstance();
        logOutBtn.setOnClickListener(this);
        rate_us.setOnClickListener(this);
        about_us.setOnClickListener(this);
        website.setOnClickListener(this);
        privacy_policy.setOnClickListener(this);
        terms_of_service.setOnClickListener(this);

        profilePhoto.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);

        showSettingsOptions();
        return root;
    }

    private static final int INTENT_REQUEST_GET_IMAGES = 13;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.MODIFY_STORAGE_LOCATION_CODE) {
            if (data.getExtras() != null) {
                String folderLocation = data.getExtras().getString("data") + "/";
                mSharedPreferences.edit().putString(Constants.STORAGE_LOCATION, folderLocation).apply();
                StringUtils.getInstance().showSnackbar(mActivity, R.string.storage_location_modified);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null)
            return;

        switch (requestCode) {
            case INTENT_REQUEST_GET_IMAGES:
                selectedUri = data.getData();
                Picasso.with(requireContext())
                        .load(data.getData())
                        .resize(800, 0)
                        .placeholder(R.drawable.avatar_icon)
                        .error(R.drawable.avatar_icon)
                        .transform(new CircleTransform())
                        .into(profilePhoto);
                if (data.getData() == null || data.getData().equals("")) {
                    if (!user.getUser_photo().equals("") && user.getUser_photo() != null) {
                        Picasso.with(requireContext())
                                .load(user.getUser_photo())
                                .placeholder(R.drawable.avatar_icon)
                                .error(R.drawable.avatar_icon)
                                .into(profilePhoto);
                    }
                }
                break;
        }
    }

    private void showSettingsOptions() {
        LinearLayoutManager mLinearManager = new LinearLayoutManager(mActivity);
        mEnhancementOptionsRecycleView.setLayoutManager(mLinearManager);
        ArrayList<EnhancementOptionsEntity> mEnhancementOptionsEntityArrayList = SettingsOptions.getEnhancementOptions(mActivity);
        ProfileSettingsAdapter adapter =
                new ProfileSettingsAdapter(this, mEnhancementOptionsEntityArrayList);
        mEnhancementOptionsRecycleView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position) {
        switch (position) {
            case 0:
                changeCompressImage();
                break;
            case 1:
                setPageSize();
                break;
            case 2:
                editFontSize();
                break;
            case 3:
                changeFontFamily();
                break;
            case 4:
                setTheme();
                break;
            case 5:
                ImageUtils.getInstance().showImageScaleTypeDialog(mActivity, true);
                break;
            case 6:
                changeMasterPassword();
                break;
            case 7:
                setShowPageNumber();
        }
    }

    /**
     * To modify master password of PDFs
     */
    private void changeMasterPassword() {
        MaterialDialog.Builder builder = DialogUtils.getInstance().createCustomDialogWithoutContent(mActivity,
                R.string.change_master_pwd);
        MaterialDialog materialDialog =
                builder.customView(R.layout.dialog_change_master_pwd, true)
                        .onPositive((dialog1, which) -> {
                            View view = dialog1.getCustomView();
                            EditText et = view.findViewById(R.id.value);
                            String value = et.getText().toString();
                            if (!value.isEmpty())
                                mSharedPreferences.edit().putString(Constants.MASTER_PWD_STRING, value).apply();
                            else
                                StringUtils.getInstance().showSnackbar(mActivity, R.string.invalid_entry);


                        }).build();
        View view = materialDialog.getCustomView();
        TextView tv = view.findViewById(R.id.content);
        tv.setText(String.format(mActivity.getString(R.string.current_master_pwd),
                mSharedPreferences.getString(Constants.MASTER_PWD_STRING, Constants.appName)));
        materialDialog.show();
    }

    /**
     * To modify default image compression value
     */
    private void changeCompressImage() {

        MaterialDialog dialog = DialogUtils.getInstance()
                .createCustomDialogWithoutContent(mActivity, R.string.compression_image_edit)
                .customView(R.layout.compress_image_dialog, true)
                .onPositive((dialog1, which) -> {
                    final EditText qualityInput = dialog1.getCustomView().findViewById(R.id.quality);
                    int check;
                    try {
                        check = Integer.parseInt(String.valueOf(qualityInput.getText()));
                        if (check > 100 || check < 0) {
                            StringUtils.getInstance().showSnackbar(mActivity, R.string.invalid_entry);
                        } else {
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putInt(Constants.DEFAULT_COMPRESSION, check);
                            editor.apply();
                            showSettingsOptions();
                        }
                    } catch (NumberFormatException e) {
                        StringUtils.getInstance().showSnackbar(mActivity, R.string.invalid_entry);
                    }
                }).build();
        View customView = dialog.getCustomView();
        customView.findViewById(R.id.cbSetDefault).setVisibility(View.GONE);
        dialog.show();
    }

    /**
     * To modify font size
     */
    private void editFontSize() {
        MaterialDialog.Builder builder = DialogUtils.getInstance()
                .createCustomDialogWithoutContent(mActivity, R.string.font_size_edit);
        MaterialDialog dialog = builder.customView(R.layout.dialog_font_size, true)
                .onPositive((dialog1, which) -> {
                    final EditText fontInput = dialog1.getCustomView().findViewById(R.id.fontInput);
                    try {
                        int check = Integer.parseInt(String.valueOf(fontInput.getText()));
                        if (check > 1000 || check < 0) {
                            StringUtils.getInstance().showSnackbar(mActivity, R.string.invalid_entry);
                        } else {
                            StringUtils.getInstance().showSnackbar(mActivity, R.string.font_size_changed);
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putInt(Constants.DEFAULT_FONT_SIZE_TEXT, check);
                            editor.apply();
                            showSettingsOptions();
                        }
                    } catch (NumberFormatException e) {
                        StringUtils.getInstance().showSnackbar(mActivity, R.string.invalid_entry);
                    }
                })
                .build();
        View customView = dialog.getCustomView();
        customView.findViewById(R.id.cbSetFontDefault).setVisibility(View.GONE);
        dialog.show();
    }

    /**
     * To modify font family
     */
    private void changeFontFamily() {
        String fontFamily = mSharedPreferences.getString(Constants.DEFAULT_FONT_FAMILY_TEXT,
                Constants.DEFAULT_FONT_FAMILY);
        int ordinal = Font.FontFamily.valueOf(fontFamily).ordinal();
        MaterialDialog.Builder builder = DialogUtils.getInstance().createCustomDialogWithoutContent(mActivity,
                R.string.font_family_edit);
        MaterialDialog materialDialog = builder.customView(R.layout.dialog_font_family, true)
                .onPositive((dialog, which) -> {
                    View view = dialog.getCustomView();
                    RadioGroup radioGroup = view.findViewById(R.id.radio_group_font_family);
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    RadioButton radioButton = view.findViewById(selectedId);
                    String fontFamily1 = radioButton.getText().toString();
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString(Constants.DEFAULT_FONT_FAMILY_TEXT, fontFamily1);
                    editor.apply();
                    showSettingsOptions();
                })
                .build();
        View customView = materialDialog.getCustomView();
        RadioGroup radioGroup = customView.findViewById(R.id.radio_group_font_family);
        RadioButton rb = (RadioButton) radioGroup.getChildAt(ordinal);
        rb.setChecked(true);
        customView.findViewById(R.id.cbSetDefault).setVisibility(View.GONE);
        materialDialog.show();
    }

    /**
     * To modify page size
     */
    private void setPageSize() {
        PageSizeUtils utils = new PageSizeUtils(mActivity);
        MaterialDialog materialDialog = utils.showPageSizeDialog(true);
        materialDialog.setOnDismissListener(dialog -> showSettingsOptions());
    }

    /**
     * To modify theme
     */
    private void setTheme() {
        MaterialDialog.Builder builder = DialogUtils.getInstance().createCustomDialogWithoutContent(mActivity,
                R.string.theme_edit);
        MaterialDialog materialDialog = builder.customView(R.layout.dialog_theme_default, true)
                .onPositive(((dialog, which) -> {
                    View view = dialog.getCustomView();
                    RadioGroup radioGroup = view.findViewById(R.id.radio_group_themes);
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    RadioButton radioButton = view.findViewById(selectedId);
                    String themeName = radioButton.getText().toString();
                    switch (selectedId) {
                        case R.id.theme_system:
                            ThemeUtils.getInstance().saveTheme(mActivity, THEME_SYSTEM);
                            break;
                        case R.id.theme_black:
                            ThemeUtils.getInstance().saveTheme(mActivity, THEME_BLACK);
                            break;
                        case R.id.theme_dark:
                            ThemeUtils.getInstance().saveTheme(mActivity, THEME_DARK);
                            break;
                        case R.id.theme_white:
                            ThemeUtils.getInstance().saveTheme(mActivity, THEME_WHITE);
                            break;
                    }
                    mActivity.recreate();
                }))
                .build();
        RadioGroup radioGroup = materialDialog.getCustomView().findViewById(R.id.radio_group_themes);
        RadioButton rb = (RadioButton) radioGroup
                .getChildAt(ThemeUtils.getInstance().getSelectedThemePosition(mActivity));
        rb.setChecked(true);
        materialDialog.show();
    }

    /**
     * To set page number
     */
    private void setShowPageNumber() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        int currChoseId = mSharedPreferences.getInt(Constants.PREF_PAGE_STYLE_ID, -1);

        RelativeLayout dialogLayout = (RelativeLayout) getLayoutInflater()
                .inflate(R.layout.add_pgnum_dialog, null);

        RadioButton rbOpt1 = dialogLayout.findViewById(R.id.page_num_opt1);
        RadioButton rbOpt2 = dialogLayout.findViewById(R.id.page_num_opt2);
        RadioButton rbOpt3 = dialogLayout.findViewById(R.id.page_num_opt3);
        RadioGroup rg = dialogLayout.findViewById(R.id.radioGroup);
        CheckBox cbDefault = dialogLayout.findViewById(R.id.set_as_default);

        if (currChoseId > 0) {
            cbDefault.setChecked(true);
            rg.clearCheck();
            rg.check(currChoseId);
        }

        MaterialDialog materialDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.choose_page_number_style)
                .customView(dialogLayout, false)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .neutralText(R.string.remove_dialog)
                .onPositive(((dialog, which) -> {
                    int id = rg.getCheckedRadioButtonId();
                    String style = null;
                    if (id == rbOpt1.getId()) {
                        style = Constants.PG_NUM_STYLE_PAGE_X_OF_N;
                    } else if (id == rbOpt2.getId()) {
                        style = Constants.PG_NUM_STYLE_X_OF_N;
                    } else if (id == rbOpt3.getId()) {
                        style = Constants.PG_NUM_STYLE_X;
                    }
                    if (cbDefault.isChecked()) {
                        SharedPreferencesUtil.getInstance().setDefaultPageNumStyle(editor, style, id);
                    } else {
                        SharedPreferencesUtil.getInstance().clearDefaultPageNumStyle(editor);
                    }
                }))
                .build();
        materialDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_us:
                Intent about_us = new Intent(requireActivity(), AboutUsActivity.class);
                requireActivity().startActivity(about_us);
                break;
            case R.id.website:
                Intent websiteIntent = new Intent(requireActivity(), WebViewActivity.class);
                websiteIntent.putExtra("url", "http://woynapp.com");
                requireActivity().startActivity(websiteIntent);
                break;
            case R.id.privacy_policy:
                Intent privacyIntent = new Intent(requireActivity(), WebViewActivity.class);
                privacyIntent.putExtra("url", "http://woynapp.com/gizlilik-politikasi/");
                requireActivity().startActivity(privacyIntent);
                break;
            case R.id.terms_of_service:
                Intent termsIntent = new Intent(requireActivity(), WebViewActivity.class);
                termsIntent.putExtra("url", "http://woynapp.com/terms-of-service/");
                requireActivity().startActivity(termsIntent);
                break;
            case R.id.rate_us:
                try {
                    requireContext().startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" +
                                    requireContext().getApplicationContext().getPackageName())));
                } catch (Exception e) {
                    openWebPage("https://play.google.com/store/apps/details?id=swati4star.createpdf");
                }
                break;
            case R.id.log_out:
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);

                builder
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mAuth.signOut();
                                userUtils.deleteUser();
                                Intent intent = new Intent(requireActivity(), AuthActivity.class);
                                requireActivity().startActivity(intent);
                                requireActivity().finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

                break;
            case R.id.profile_photo:
                if (isEditMode) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                            intent.setType("image/*");
                            pickSingleMediaLauncher.launch(intent);
                        }else {
                            getContent.launch("image/*");
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= 33) {
                            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                            intent.setType("image/*");
                            pickSingleMediaLauncher.launch(intent);
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }
                }
                break;
            case R.id.save_btn:
                changeEditMode();
                String oldName = user.getUser_name() + " " + user.getSurname();
                String newName = userNameTv.getText().toString();
                if (!oldName.equals(newName)) {
                    saveNewName(newName);
                }
                if (selectedUri != null) {
                    saveNewPhoto();
                }
                break;
            case R.id.edit_btn:
                changeEditMode();
                break;
        }
    }

    public void openWebPage(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(requireContext().getPackageManager()) != null)
            requireContext().startActivity(intent);
    }

    private void saveNewName(String newName) {
        String firstName = newName.split(" ")[0];
        String lastName = newName.split(" ")[1];
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.USER_COLLECTION_NAME)
                .document(user.getUser_uuid())
                .update(Constants.USER_NAME, firstName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            userUtils.updateName(firstName);
                        } else {
                            Toast.makeText(requireContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
        db.collection(Constants.USER_COLLECTION_NAME)
                .document(user.getUser_uuid())
                .update(Constants.USER_SURNAME, lastName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            userUtils.updateSurname(lastName);
                        } else {
                            Toast.makeText(requireContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveNewPhoto() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images");
        StorageReference profileImageRef = storageRef.child(user.getUser_uuid());
        StorageReference imageRef = profileImageRef.child(UUID.randomUUID().toString() + ".jpg");
        imageRef.putFile(selectedUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        db.collection(Constants.USER_COLLECTION_NAME)
                                                .document(user.getUser_uuid())
                                                .update(Constants.USER_PHOTO, uri.toString())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        userUtils.updatePhoto(uri.toString());
                                                    }
                                                });

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void changeEditMode() {
        if (isEditMode) {
            isEditMode = false;
            editBtn.setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.GONE);
            userNameTv.setInputType(InputType.TYPE_NULL);
            userNameTv.setEnabled(false);
            editPhotoIcon.setVisibility(View.GONE);
        } else {
            isEditMode = true;
            editBtn.setVisibility(View.GONE);
            saveBtn.setVisibility(View.VISIBLE);
            userNameTv.setInputType(InputType.TYPE_CLASS_TEXT);
            userNameTv.setEnabled(true);
            editPhotoIcon.setVisibility(View.VISIBLE);
        }
    }
}
