package woynapp.wsann.providers.fragmentmanagement;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.widget.Toast;

import java.util.Objects;

import woynapp.wsann.R;
import woynapp.wsann.activity.WelcomeActivity;
import woynapp.wsann.fragment.AboutUsFragment;
import woynapp.wsann.fragment.AddImagesFragment;
import woynapp.wsann.fragment.AddTextFragment;
import woynapp.wsann.fragment.ExceltoPdfFragment;
import woynapp.wsann.fragment.ExtractTextFragment;
import woynapp.wsann.fragment.FAQFragment;
import woynapp.wsann.fragment.FavouritesFragment;
import woynapp.wsann.fragment.HistoryFragment;
import woynapp.wsann.fragment.HomeFragment;
import woynapp.wsann.fragment.ImageToPdfFragment;
import woynapp.wsann.fragment.InvertPdfFragment;
import woynapp.wsann.fragment.MergeFilesFragment;
import woynapp.wsann.fragment.PdfToImageFragment;
import woynapp.wsann.fragment.QrBarcodeScanFragment;
import woynapp.wsann.fragment.RemoveDuplicatePagesFragment;
import woynapp.wsann.fragment.RemovePagesFragment;
import woynapp.wsann.fragment.SettingsFragment;
import woynapp.wsann.fragment.SplitFilesFragment;
import woynapp.wsann.fragment.ViewFilesFragment;
import woynapp.wsann.fragment.ZipToPdfFragment;
import woynapp.wsann.fragment.new_fragments.NewHomeFragment;
import woynapp.wsann.fragment.texttopdf.TextToPdfFragment;
import woynapp.wsann.util.FeedbackUtils;
import woynapp.wsann.util.FragmentUtils;
import woynapp.wsann.util.WhatsNewUtils;
import woynapp.wsann.util.Constants;

/**
 * This is a fragment service that manages the fragments
 * mainly for the MainActivity.
 */
public class FragmentManagement implements IFragmentManagement {
    private final FragmentActivity mContext;
    private final NavigationView mNavigationView;
    private boolean mDoubleBackToExitPressedOnce = false;
    private final FeedbackUtils mFeedbackUtils;
    private final FragmentUtils mFragmentUtils;

    public FragmentManagement(FragmentActivity context, NavigationView navigationView) {
        mContext = context;
        mNavigationView = navigationView;
        mFeedbackUtils = new FeedbackUtils(mContext);
        mFragmentUtils = new FragmentUtils(mContext);
    }

    public void favouritesFragmentOption() {
        Fragment currFragment = mContext.getSupportFragmentManager().findFragmentById(R.id.content);

        Fragment fragment = new FavouritesFragment();
        FragmentManager fragmentManager = mContext.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction()
                .replace(R.id.content, fragment);
        if (!(currFragment instanceof NewHomeFragment)) {
            transaction.addToBackStack(mFragmentUtils.getFragmentName(currFragment));
        }
        transaction.commit();
    }

    public Fragment checkForAppShortcutClicked() {
        Fragment fragment = new NewHomeFragment();
        if (mContext.getIntent().getAction() != null) {
            switch (Objects.requireNonNull(mContext.getIntent().getAction())) {
                case Constants.ACTION_SELECT_IMAGES:
                    fragment = new ImageToPdfFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.OPEN_SELECT_IMAGES, true);
                    fragment.setArguments(bundle);
                    break;
                case Constants.ACTION_VIEW_FILES:
                    fragment = new ViewFilesFragment();
                    setNavigationViewSelection(R.id.nav_gallery);
                    break;
                case Constants.ACTION_TEXT_TO_PDF:
                    fragment = new TextToPdfFragment();
                    setNavigationViewSelection(R.id.nav_text_to_pdf);
                    break;
                case Constants.ACTION_MERGE_PDF:
                    fragment = new MergeFilesFragment();
                    setNavigationViewSelection(R.id.nav_merge);
                    break;
                default:
                    fragment = new NewHomeFragment(); // Set default fragment
                    break;
            }
        }
        if (areImagesReceived())
            fragment = new ImageToPdfFragment();

        mContext.getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();

        return fragment;
    }

    public boolean handleBackPressed() {
        Fragment currentFragment = mContext.getSupportFragmentManager()
                .findFragmentById(R.id.content);
        if (currentFragment instanceof NewHomeFragment) {
            return checkDoubleBackPress();
        } else {
            if (mFragmentUtils.handleFragmentBottomSheetBehavior(currentFragment))
                return false;
        }
        handleBackStackEntry();
        return false;
    }

    public boolean handleNavigationItemSelected(int itemId) {
        Fragment fragment = null;
        FragmentManager fragmentManager = mContext.getSupportFragmentManager();
        Bundle bundle = new Bundle();

        switch (itemId) {
            case R.id.nav_home:
                fragment = new NewHomeFragment();
                break;
            case R.id.nav_camera:
                fragment = new ImageToPdfFragment();
                break;
            case R.id.nav_qrcode:
                fragment = new QrBarcodeScanFragment();
                break;
            case R.id.nav_gallery:
                fragment = new ViewFilesFragment();
                break;
            case R.id.nav_merge:
                fragment = new MergeFilesFragment();
                break;
            case R.id.nav_split:
                fragment = new SplitFilesFragment();
                break;
            case R.id.nav_text_to_pdf:
                fragment = new TextToPdfFragment();
                break;
            case R.id.nav_history:
                fragment = new HistoryFragment();
                break;
            case R.id.nav_add_text:
                fragment = new AddTextFragment();
                break;
            case R.id.nav_add_password:
                fragment = new RemovePagesFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.ADD_PWD);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_remove_password:
                fragment = new RemovePagesFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.REMOVE_PWd);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_share:
                mFeedbackUtils.shareApplication();
                break;
            case R.id.nav_about:
                fragment = new AboutUsFragment();
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                break;
            case R.id.nav_extract_images:
                fragment = new PdfToImageFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.EXTRACT_IMAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_pdf_to_images:
                fragment = new PdfToImageFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.PDF_TO_IMAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_excel_to_pdf:
                fragment = new ExceltoPdfFragment();
                break;
            case R.id.nav_remove_pages:
                fragment = new RemovePagesFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.REMOVE_PAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_rearrange_pages:
                fragment = new RemovePagesFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.REORDER_PAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_compress_pdf:
                fragment = new RemovePagesFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.COMPRESS_PDF);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_add_images:
                fragment = new AddImagesFragment();
                bundle.putString(Constants.BUNDLE_DATA, Constants.ADD_IMAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_help:
                Intent intent = new Intent(mContext, WelcomeActivity.class);
                intent.putExtra(Constants.SHOW_WELCOME_ACT, true);
                mContext.startActivity(intent);
                break;
            case R.id.nav_remove_duplicate_pages:
                fragment = new RemoveDuplicatePagesFragment();
                break;
            case R.id.nav_invert_pdf:
                fragment = new InvertPdfFragment();
                break;
            case R.id.nav_add_watermark:
                fragment = new ViewFilesFragment();
                bundle.putInt(Constants.BUNDLE_DATA, Constants.ADD_WATERMARK);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_zip_to_pdf:
                fragment = new ZipToPdfFragment();
                break;
            case R.id.nav_whatsNew:
                WhatsNewUtils.getInstance().displayDialog(mContext);
                break;
            case R.id.nav_rateus:
                mFeedbackUtils.openWebPage("https://play.google.com/store/apps");
                break;
            case R.id.nav_rotate_pages:
                fragment = new ViewFilesFragment();
                bundle.putInt(Constants.BUNDLE_DATA, Constants.ROTATE_PAGES);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_text_extract:
                fragment = new ExtractTextFragment();
                break;
            case R.id.nav_faq:
                fragment = new FAQFragment();
                break;
        }

        try {
            if (fragment != null)
                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // if help or share or what's new is clicked then return false, as we don't want
        // them to be selected
        return itemId != R.id.nav_share && itemId != R.id.nav_help
                && itemId != R.id.nav_whatsNew;
    }

    /**
     * Closes the app only when double clicked
     */
    private boolean checkDoubleBackPress() {
        if (mDoubleBackToExitPressedOnce) {
            return true;
        }
        mDoubleBackToExitPressedOnce = true;
        Toast.makeText(mContext, R.string.confirm_exit_message, Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Back stack count will be 1 when we open a item from favourite menu
     * on clicking back, return back to fav menu and change title
     */
    private void handleBackStackEntry() {
        int count = mContext.getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            String s = mContext.getSupportFragmentManager().getBackStackEntryAt(count - 1).getName();
            mContext.setTitle(s);
            mContext.getSupportFragmentManager().popBackStack();
        } else {
            Fragment fragment = new NewHomeFragment();
            mContext.getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
            mContext.setTitle(R.string.app_name);
            setNavigationViewSelection(R.id.nav_home);
        }
    }

    private boolean areImagesReceived() {
        Intent intent = mContext.getIntent();
        String type = intent.getType();
        return type != null && type.startsWith("image/");
    }

    private void setNavigationViewSelection(int id) {
        mNavigationView.setCheckedItem(id);
    }
}
