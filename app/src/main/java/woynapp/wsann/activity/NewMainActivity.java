package woynapp.wsann.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.multidex.BuildConfig;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.zhihu.matisse.Matisse;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;
import woynapp.wsann.R;
import woynapp.wsann.fragment.ImageToPdfFragment;
import woynapp.wsann.fragment.SettingsFragment;
import woynapp.wsann.fragment.ViewFilesFragment;
import woynapp.wsann.fragment.new_fragments.NewHomeFragment;
import woynapp.wsann.fragment.new_fragments.NewToolFragment;
import woynapp.wsann.providers.fragmentmanagement.FragmentManagement;
import woynapp.wsann.util.Constants;
import woynapp.wsann.util.DirectoryUtils;
import woynapp.wsann.util.FeedbackUtils;
import woynapp.wsann.util.ImageUtils;
import woynapp.wsann.util.PermissionsUtils;
import woynapp.wsann.util.ThemeUtils;
import woynapp.wsann.util.WhatsNewUtils;

public class NewMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;
    NewHomeFragment newHomeFragment;
    private NavigationView mNavigationView;
    ViewFilesFragment newDocumentFragment;
    NewToolFragment newToolFragment;
    SettingsFragment newProfileFragment;

    private FeedbackUtils mFeedbackUtils;
    private SharedPreferences mSharedPreferences;
    private SparseIntArray mFragmentSelectedMap;
    private FragmentManagement mFragmentManagement;

    private static final int INTENT_REQUEST_GET_IMAGES = 13;


    private FloatingActionButton fab;

    private boolean mIsButtonAlreadyClicked = false;

    @Override
    public void recreate() {
        super.recreate();
        bottomNavigationView.setSelectedItemId(R.id.homeFragment);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.getInstance().setThemeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);

        newHomeFragment = new NewHomeFragment();
        newDocumentFragment = new ViewFilesFragment();
        newToolFragment = new NewToolFragment();
        newProfileFragment = new SettingsFragment();
        GifImageView gifImageView = findViewById(R.id.kargo_bul_banner);

        mNavigationView = findViewById(R.id.nav_view);
        fab = findViewById(R.id.fab);

        gifImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewMainActivity.this, WebViewActivity.class);
                startActivity(intent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsButtonAlreadyClicked) {
                    if (isStoragePermissionGranted()) {
                        selectImages();
                        mIsButtonAlreadyClicked = true;
                    } else {
                        getRuntimePermissions();
                    }
                }
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.homeFragment:
                    setCurrentFragment(newHomeFragment);
                    break;
                case R.id.documentFragment:
                    setCurrentFragment(newDocumentFragment);
                    break;
                case R.id.toolFragment:
                    setCurrentFragment(newToolFragment);
                    break;
                case R.id.profileFragment:
                    setCurrentFragment(newProfileFragment);
                    break;
            }
            return true;
        });

        setThemeOnActivityExclusiveComponents();

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);


        // Set navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.app_name, R.string.app_name);

        //Replaced setDrawerListener with addDrawerListener because it was deprecated.
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // initialize values
        initializeValues();

        //check for Ads


        setXMLParsers();
        // Check for app shortcuts & select default fragment
        Fragment fragment = mFragmentManagement.checkForAppShortcutClicked();

        // Check if  images are received
        handleReceivedImagesIntent(fragment);

        displayFeedBackAndWhatsNew();

        openWelcomeActivity();

        setCurrentFragment(newHomeFragment);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }


    private void getRuntimePermissions() {
        if (Build.VERSION.SDK_INT < 29) {
            PermissionsUtils.getInstance().requestRuntimePermissions(this,
                    Constants.WRITE_PERMISSIONS,
                    Constants.REQUEST_CODE_FOR_WRITE_PERMISSION);
        } else {
            PermissionsUtils.getInstance().requestRuntimePermissions(this,
                    Constants.READ_PERMISSIONS,
                    Constants.REQUEST_CODE_FOR_READ_PERMISSION);
        }
    }


    private void setXMLParsers() {
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory",
                "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory",
                "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory",
                "com.fasterxml.aalto.stax.EventFactoryImpl");
    }

    private void displayFeedBackAndWhatsNew() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int count = mSharedPreferences.getInt(Constants.LAUNCH_COUNT, 0);
        if (count > 0 && count % 15 == 0) {
            mFeedbackUtils.rateUs();
        }
        if (count != -1) {
            mSharedPreferences.edit().putInt(Constants.LAUNCH_COUNT, count + 1).apply();
        }

        String versionName = mSharedPreferences.getString(Constants.VERSION_NAME, "");
        if (versionName != null && !versionName.equals(BuildConfig.VERSION_NAME)) {
            WhatsNewUtils.getInstance().displayDialog(this);
            mSharedPreferences.edit().putString(Constants.VERSION_NAME, BuildConfig.VERSION_NAME).apply();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isStoragePermissionGranted()) {
            DirectoryUtils.makeAndClearTemp();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        if (actionBar != null)
            actionBar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_favourites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_favourites_item) {
            setTitle(R.string.favourites);
            mFragmentManagement.favouritesFragmentOption();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openWelcomeActivity() {
        if (!mSharedPreferences.getBoolean(Constants.IS_WELCOME_ACTIVITY_SHOWN, false)) {
            Intent intent = new Intent(NewMainActivity.this, WelcomeActivity.class);
            mSharedPreferences.edit().putBoolean(Constants.IS_WELCOME_ACTIVITY_SHOWN, true).apply();
            startActivity(intent);
        }
    }

    /**
     * Ininitializes default values
     */
    private void initializeValues() {
        mFeedbackUtils = new FeedbackUtils(this);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(R.id.nav_home);

        mFragmentManagement = new FragmentManagement(this, mNavigationView);
        setTitleMap();
    }

    private void handleReceivedImagesIntent(Fragment fragment) {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (type == null || !type.startsWith("image/"))
            return;

        if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            handleSendMultipleImages(intent, fragment); // Handle multiple images
        } else if (Intent.ACTION_SEND.equals(action)) {
            handleSendImage(intent, fragment); // Handle single image
        }
    }

    private void handleSendImage(Intent intent, Fragment fragment) {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        ArrayList<Uri> imageUris = new ArrayList<>();
        imageUris.add(uri);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.bundleKey), imageUris);
        fragment.setArguments(bundle);
    }

    /**
     * Get ArrayList of image uris from intent and send the image to homeFragment
     *
     * @param intent   - intent containing image uris
     * @param fragment - instance of homeFragment
     */
    private void handleSendMultipleImages(Intent intent, Fragment fragment) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(getString(R.string.bundleKey), imageUris);
            fragment.setArguments(bundle);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            boolean shouldExit = mFragmentManagement.handleBackPressed();
            if (shouldExit)
                super.onBackPressed();
        }
        bottomNavigationView.setSelectedItemId(R.id.homeFragment);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        setTitleFragment(mFragmentSelectedMap.get(item.getItemId()));
        return mFragmentManagement.handleNavigationItemSelected(item.getItemId());
    }

    public void setNavigationViewSelection(int id) {
        mNavigationView.setCheckedItem(id);
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= 29) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else
            return true;
    }

    public void convertImagesToPdf(ArrayList<Uri> imageUris) {
        Fragment fragment = new ImageToPdfFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.bundleKey), imageUris);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }

    public void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }

    private void setTitleFragment(int title) {
        if (title != 0)
            setTitle(title);
    }


    private void setTitleMap() {
        mFragmentSelectedMap = new SparseIntArray();
        mFragmentSelectedMap.append(R.id.nav_home, R.string.app_name);
        mFragmentSelectedMap.append(R.id.nav_camera, R.string.images_to_pdf);
        mFragmentSelectedMap.append(R.id.nav_qrcode, R.string.qr_barcode_pdf);
        mFragmentSelectedMap.append(R.id.nav_add_text, R.string.add_text);
        mFragmentSelectedMap.append(R.id.nav_gallery, R.string.viewFiles);
        mFragmentSelectedMap.append(R.id.nav_merge, R.string.merge_pdf);
        mFragmentSelectedMap.append(R.id.nav_split, R.string.split_pdf);
        mFragmentSelectedMap.append(R.id.nav_text_to_pdf, R.string.text_to_pdf);
        mFragmentSelectedMap.append(R.id.nav_history, R.string.history);
        mFragmentSelectedMap.append(R.id.nav_add_password, R.string.add_password);
        mFragmentSelectedMap.append(R.id.nav_remove_password, R.string.remove_password);
        mFragmentSelectedMap.append(R.id.nav_about, R.string.about_us);
        mFragmentSelectedMap.append(R.id.nav_settings, R.string.settings);
        mFragmentSelectedMap.append(R.id.nav_extract_images, R.string.extract_images);
        mFragmentSelectedMap.append(R.id.nav_pdf_to_images, R.string.pdf_to_images);
        mFragmentSelectedMap.append(R.id.nav_remove_pages, R.string.remove_pages);
        mFragmentSelectedMap.append(R.id.nav_rearrange_pages, R.string.reorder_pages);
        mFragmentSelectedMap.append(R.id.nav_compress_pdf, R.string.compress_pdf);
        mFragmentSelectedMap.append(R.id.nav_add_images, R.string.add_images);
        mFragmentSelectedMap.append(R.id.nav_remove_duplicate_pages, R.string.remove_duplicate_pages);
        mFragmentSelectedMap.append(R.id.nav_invert_pdf, R.string.invert_pdf);
        mFragmentSelectedMap.append(R.id.nav_add_watermark, R.string.add_watermark);
        mFragmentSelectedMap.append(R.id.nav_zip_to_pdf, R.string.zip_to_pdf);
        mFragmentSelectedMap.append(R.id.nav_rotate_pages, R.string.rotate_pages);
        mFragmentSelectedMap.append(R.id.nav_excel_to_pdf, R.string.excel_to_pdf);
        mFragmentSelectedMap.append(R.id.nav_faq, R.string.faqs);
    }

    private void setThemeOnActivityExclusiveComponents() {
        RelativeLayout toolbarBackgroundLayout = findViewById(R.id.toolbar_background_layout);
        CardView content = findViewById(R.id.content);
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String themeName = mSharedPreferences.getString(Constants.DEFAULT_THEME_TEXT, Constants.DEFAULT_THEME);
        switch (themeName) {
            case Constants.THEME_WHITE:
                toolbarBackgroundLayout.setBackgroundResource(R.drawable.toolbar_bg);
                content.setCardBackgroundColor(getResources().getColor(R.color.lighter_gray));
                mNavigationView.setBackgroundResource(R.color.white);
                break;
            case Constants.THEME_BLACK:
                toolbarBackgroundLayout.setBackgroundResource(R.color.black);
                content.setCardBackgroundColor(getResources().getColor(R.color.black));
                mNavigationView.setBackgroundResource(R.color.black);
                mNavigationView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                mNavigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                mNavigationView.setItemBackgroundResource(R.drawable.navigation_item_selected_bg_selector_dark);
                break;
            case Constants.THEME_DARK:
                toolbarBackgroundLayout.setBackgroundResource(R.color.colorBlackAltLight);
                content.setCardBackgroundColor(getResources().getColor(R.color.colorBlackAlt));
                mNavigationView.setBackgroundResource(R.color.colorBlackAlt);
                mNavigationView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                mNavigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                mNavigationView.setItemBackgroundResource(R.drawable.navigation_item_selected_bg_selector_dark);
                break;
            case Constants.THEME_SYSTEM:
            default:
                if ((this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    toolbarBackgroundLayout.setBackgroundResource(R.color.colorBlackAltLight);
                    content.setCardBackgroundColor(getResources().getColor(R.color.colorBlackAlt));
                    mNavigationView.setBackgroundResource(R.color.colorBlackAlt);
                    mNavigationView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    mNavigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    mNavigationView.setItemBackgroundResource(R.drawable.navigation_item_selected_bg_selector_dark);
                } else {
                    toolbarBackgroundLayout.setBackgroundResource(R.drawable.toolbar_bg);
                    content.setCardBackgroundColor(getResources().getColor(R.color.lighter_gray));
                    mNavigationView.setBackgroundResource(R.color.white);
                }
        }
    }

    private void selectImages() {
        ImageUtils.selectImages(this, INTENT_REQUEST_GET_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mIsButtonAlreadyClicked = false;
        if (resultCode != Activity.RESULT_OK || data == null)
            return;

        switch (requestCode) {
            case INTENT_REQUEST_GET_IMAGES:
                ArrayList<String> mImagesUriString = new ArrayList<String>(Matisse.obtainPathResult(data));
                ArrayList<Uri> mImagesUri = new ArrayList<Uri>();
                for (String uri : mImagesUriString) {
                    Uri myUri = Uri.parse(uri);
                    mImagesUri.add(myUri);
                }
                convertImagesToPdf(mImagesUri);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        RelativeLayout toolBarLayout = findViewById(R.id.toolbar_background_layout);
        toolBarLayout.setVisibility(View.GONE);
    }
}