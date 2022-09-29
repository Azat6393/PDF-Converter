package woynapp.wsann.util;

import android.app.Activity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import woynapp.wsann.R;
import woynapp.wsann.adapter.ExtractImagesAdapter;
import woynapp.wsann.adapter.MergeFilesAdapter;
import woynapp.wsann.model.HomePageItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static woynapp.wsann.R.drawable.baseline_crop_rotate_24;
import static woynapp.wsann.R.drawable.ic_add_black_24dp;
import static woynapp.wsann.R.drawable.ic_branding_watermark_black_24dp;
import static woynapp.wsann.R.drawable.ic_broken_image_black_24dp;
import static woynapp.wsann.R.drawable.ic_call_split_black_24dp;
import static woynapp.wsann.R.drawable.ic_compress_image;
import static woynapp.wsann.R.drawable.ic_excel;
import static woynapp.wsann.R.drawable.ic_history_black_24dp;
import static woynapp.wsann.R.drawable.ic_image_black_24dp;
import static woynapp.wsann.R.drawable.ic_invert_color_24dp;
import static woynapp.wsann.R.drawable.ic_lock_black_24dp;
import static woynapp.wsann.R.drawable.ic_lock_open_black_24dp;
import static woynapp.wsann.R.drawable.ic_menu_camera;
import static woynapp.wsann.R.drawable.ic_menu_gallery;
import static woynapp.wsann.R.drawable.ic_merge_type_black_24dp;
import static woynapp.wsann.R.drawable.ic_qrcode_24dp;
import static woynapp.wsann.R.drawable.ic_rearrange;
import static woynapp.wsann.R.drawable.ic_remove_circle_black_24dp;
import static woynapp.wsann.R.drawable.ic_text_format_black_24dp;
import static woynapp.wsann.R.drawable.ic_zip_to_pdf;
import static woynapp.wsann.R.id.add_images;
import static woynapp.wsann.R.id.add_images_fav;
import static woynapp.wsann.R.id.add_password;
import static woynapp.wsann.R.id.add_password_btn;
import static woynapp.wsann.R.id.add_password_fav;
import static woynapp.wsann.R.id.add_text_fav;
import static woynapp.wsann.R.id.add_watermark;
import static woynapp.wsann.R.id.add_watermark_btn;
import static woynapp.wsann.R.id.add_watermark_fav;
import static woynapp.wsann.R.id.compress_pdf;
import static woynapp.wsann.R.id.compress_pdf_btn;
import static woynapp.wsann.R.id.compress_pdf_fav;
import static woynapp.wsann.R.id.excel_to_pdf;
import static woynapp.wsann.R.id.excel_to_pdf_fav;
import static woynapp.wsann.R.id.extract_images;
import static woynapp.wsann.R.id.extract_images_fav;
import static woynapp.wsann.R.id.extract_text;
import static woynapp.wsann.R.id.extract_text_fav;
import static woynapp.wsann.R.id.images_to_pdf_fav;
import static woynapp.wsann.R.id.invert_pdf_fav;
import static woynapp.wsann.R.id.merge_pdf;
import static woynapp.wsann.R.id.merge_pdf_btn;
import static woynapp.wsann.R.id.merge_pdf_fav;
import static woynapp.wsann.R.id.nav_add_images;
import static woynapp.wsann.R.id.nav_add_password;
import static woynapp.wsann.R.id.nav_add_text;
import static woynapp.wsann.R.id.nav_add_watermark;
import static woynapp.wsann.R.id.nav_camera;
import static woynapp.wsann.R.id.nav_compress_pdf;
import static woynapp.wsann.R.id.nav_excel_to_pdf;
import static woynapp.wsann.R.id.nav_extract_images;
import static woynapp.wsann.R.id.nav_gallery;
import static woynapp.wsann.R.id.nav_history;
import static woynapp.wsann.R.id.nav_invert_pdf;
import static woynapp.wsann.R.id.nav_merge;
import static woynapp.wsann.R.id.nav_pdf_to_images;
import static woynapp.wsann.R.id.nav_qrcode;
import static woynapp.wsann.R.id.nav_rearrange_pages;
import static woynapp.wsann.R.id.nav_remove_duplicate_pages;
import static woynapp.wsann.R.id.nav_remove_pages;
import static woynapp.wsann.R.id.nav_remove_password;
import static woynapp.wsann.R.id.nav_split;
import static woynapp.wsann.R.id.nav_text_extract;
import static woynapp.wsann.R.id.nav_text_to_pdf;
import static woynapp.wsann.R.id.nav_zip_to_pdf;
import static woynapp.wsann.R.id.pdf_to_images;
import static woynapp.wsann.R.id.pdf_to_images_btn;
import static woynapp.wsann.R.id.pdf_to_images_fav;
import static woynapp.wsann.R.id.qr_barcode_to_pdf;
import static woynapp.wsann.R.id.qr_barcode_to_pdf_btn;
import static woynapp.wsann.R.id.qr_barcode_to_pdf_fav;
import static woynapp.wsann.R.id.rearrange_pages;
import static woynapp.wsann.R.id.rearrange_pages_fav;
import static woynapp.wsann.R.id.remove_duplicates_pages_pdf;
import static woynapp.wsann.R.id.remove_duplicates_pages_pdf_fav;
import static woynapp.wsann.R.id.remove_pages;
import static woynapp.wsann.R.id.remove_pages_fav;
import static woynapp.wsann.R.id.remove_password;
import static woynapp.wsann.R.id.remove_password_btn;
import static woynapp.wsann.R.id.remove_password_fav;
import static woynapp.wsann.R.id.rotate_pages;
import static woynapp.wsann.R.id.rotate_pages_fav;
import static woynapp.wsann.R.id.split_pdf;
import static woynapp.wsann.R.id.split_pdf_btn;
import static woynapp.wsann.R.id.split_pdf_fav;
import static woynapp.wsann.R.id.text_to_pdf;
import static woynapp.wsann.R.id.text_to_pdf_btn;
import static woynapp.wsann.R.id.text_to_pdf_fav;
import static woynapp.wsann.R.id.view_files;
import static woynapp.wsann.R.id.view_files_fav;
import static woynapp.wsann.R.id.view_history;
import static woynapp.wsann.R.id.view_history_fav;
import static woynapp.wsann.R.id.zip_to_pdf_fav;
import static woynapp.wsann.R.string.qr_barcode_pdf;


public class CommonCodeUtils {

    Map<Integer, HomePageItem> mFragmentPositionMap;

    /**
     * updates the output recycler view if paths.size > 0
     * else give the main view
     */
    public void populateUtil(Activity mActivity, ArrayList<String> paths,
                             MergeFilesAdapter.OnClickListener onClickListener,
                             RelativeLayout layout, LottieAnimationView animationView,
                             RecyclerView recyclerView) {

        if (paths == null || paths.size() == 0) {
            layout.setVisibility(View.GONE);
        } else {
            // Init recycler view
            recyclerView.setVisibility(View.VISIBLE);
            MergeFilesAdapter mergeFilesAdapter = new MergeFilesAdapter(mActivity,
                    paths, false, onClickListener);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(mergeFilesAdapter);
            recyclerView.addItemDecoration(new ViewFilesDividerItemDecoration(mActivity));
        }
        animationView.setVisibility(View.GONE);
    }


    /**
     * sets the appropriate text to success Text View & display images in adapter
     */
    public void updateView(Activity mActivity, int imageCount, ArrayList<String> outputFilePaths,
                           TextView successTextView, LinearLayout options, RecyclerView mCreatedImages,
                           ExtractImagesAdapter.OnFileItemClickedListener listener) {

        if (imageCount == 0) {
            StringUtils.getInstance().showSnackbar(mActivity, R.string.extract_images_failed);
            return;
        }

        String text = String.format(mActivity.getString(R.string.extract_images_success), imageCount);
        StringUtils.getInstance().showSnackbar(mActivity, text);
        successTextView.setVisibility(View.VISIBLE);
        options.setVisibility(View.VISIBLE);
        ExtractImagesAdapter extractImagesAdapter = new ExtractImagesAdapter(mActivity, outputFilePaths, listener);
        // init recycler view for displaying generated image list
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        successTextView.setText(text);
        mCreatedImages.setVisibility(View.VISIBLE);
        mCreatedImages.setLayoutManager(mLayoutManager);
        // set up adapter
        mCreatedImages.setAdapter(extractImagesAdapter);
        mCreatedImages.addItemDecoration(new ViewFilesDividerItemDecoration(mActivity));
    }

    /**
     * Closes the bottom sheet if it is expanded
     */

    public void closeBottomSheetUtil(BottomSheetBehavior sheetBehavior) {
        if (checkSheetBehaviourUtil(sheetBehavior))
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * Checks whether the bottom sheet is expanded or collapsed
     */
    public boolean checkSheetBehaviourUtil(BottomSheetBehavior sheetBehavior) {
        return sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    private void addFragmentPosition(boolean homePageItems, int iconA, int iconB,
                                     int iconId, int drawableId, int titleString) {
        mFragmentPositionMap.put(homePageItems ? iconA : iconB, new HomePageItem(iconId, drawableId, titleString));
    }

    public Map<Integer, HomePageItem> fillNavigationItemsMap(boolean homePageItems) {
        mFragmentPositionMap = new HashMap<>();
        addFragmentPosition(homePageItems, R.id.images_to_pdf_btn,
                images_to_pdf_fav, nav_camera, R.drawable.new_picture, R.string.images_to_pdf);
        addFragmentPosition(homePageItems, qr_barcode_to_pdf_btn,
                qr_barcode_to_pdf_fav, nav_qrcode, R.drawable.new_qr, qr_barcode_pdf);
        addFragmentPosition(homePageItems, R.id.excel_to_pdf_btn,
                excel_to_pdf_fav, nav_excel_to_pdf, R.drawable.new_excel, R.string.excel_to_pdf);
        addFragmentPosition(homePageItems, view_files, view_files_fav,
                nav_gallery, ic_menu_gallery, R.string.viewFiles);
        addFragmentPosition(homePageItems, rotate_pages, rotate_pages_fav,
                nav_gallery, baseline_crop_rotate_24, R.string.rotate_pages);
        addFragmentPosition(homePageItems, extract_text, extract_text_fav,
                nav_text_extract, ic_broken_image_black_24dp, R.string.extract_text);
        addFragmentPosition(homePageItems, add_watermark_btn, add_watermark_fav,
                nav_add_watermark, ic_branding_watermark_black_24dp, R.string.add_watermark);
        addFragmentPosition(homePageItems, merge_pdf_btn, merge_pdf_fav,
                nav_merge, R.drawable.new_merge_pdf, R.string.merge_pdf);
        addFragmentPosition(homePageItems, split_pdf_btn, split_pdf_fav,
                nav_split, R.drawable.new_split_pdf, R.string.split_pdf);
        addFragmentPosition(homePageItems, text_to_pdf_btn, text_to_pdf_fav,
                nav_text_to_pdf, R.drawable.new_text_to_pdf, R.string.text_to_pdf);
        addFragmentPosition(homePageItems, compress_pdf_btn, compress_pdf_fav,
                nav_compress_pdf, R.drawable.new_row_blue, R.string.compress_pdf);
        addFragmentPosition(homePageItems, remove_pages, remove_pages_fav,
                nav_remove_pages, ic_remove_circle_black_24dp, R.string.remove_pages);
        addFragmentPosition(homePageItems, rearrange_pages, rearrange_pages_fav,
                nav_rearrange_pages, ic_rearrange, R.string.reorder_pages);
        addFragmentPosition(homePageItems, extract_images, extract_images_fav,
                nav_extract_images, ic_broken_image_black_24dp, R.string.extract_images);
        addFragmentPosition(homePageItems, view_history, view_history_fav,
                nav_history, ic_history_black_24dp, R.string.history);
        addFragmentPosition(homePageItems, pdf_to_images_btn, pdf_to_images_fav,
                nav_pdf_to_images, R.drawable.new_pdf_to_image, R.string.pdf_to_images);
        addFragmentPosition(homePageItems, add_password_btn, add_password_fav,
                nav_add_password, R.drawable.new_lock, R.string.add_password);
        addFragmentPosition(homePageItems, remove_password_btn, remove_password_fav,
                nav_remove_password, R.drawable.new_unlock, R.string.remove_password);
        addFragmentPosition(homePageItems, add_images, add_images_fav,
                nav_add_images, ic_add_black_24dp, R.string.add_images);
        addFragmentPosition(homePageItems, remove_duplicates_pages_pdf,
                remove_duplicates_pages_pdf_fav, nav_remove_duplicate_pages,
                R.drawable.ic_remove_duplicate_square_black, R.string.remove_duplicate_pages);
        addFragmentPosition(homePageItems, R.id.invert_pdf, invert_pdf_fav,
                nav_invert_pdf, ic_invert_color_24dp, R.string.invert_pdf);
        addFragmentPosition(homePageItems, R.id.zip_to_pdf, zip_to_pdf_fav,
                nav_zip_to_pdf, ic_zip_to_pdf, R.string.zip_to_pdf);
        addFragmentPosition(homePageItems, R.id.add_text_btn, add_text_fav,
                nav_add_text, R.drawable.new_text_plus, R.string.add_text);
        return mFragmentPositionMap;
    }

    private static class SingletonHolder {
        static final CommonCodeUtils INSTANCE = new CommonCodeUtils();
    }

    public static CommonCodeUtils getInstance() {
        return CommonCodeUtils.SingletonHolder.INSTANCE;
    }
}
