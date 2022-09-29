package woynapp.wsann.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

import woynapp.wsann.R;
import woynapp.wsann.model.EnhancementOptionsEntity;
import woynapp.wsann.model.ImageToPDFOptions;

public class ImageEnhancementOptionsUtils {

    public ImageEnhancementOptionsUtils() {
    }

    /**
     * Singleton Implementation
     */
    private static class SingletonHolder {
        private static final ImageEnhancementOptionsUtils INSTANCE = new ImageEnhancementOptionsUtils();
    }

    public static ImageEnhancementOptionsUtils getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public ArrayList<EnhancementOptionsEntity> getEnhancementOptions(Context context,
                                                                     ImageToPDFOptions pdfOptions) {
        ArrayList<EnhancementOptionsEntity> options = new ArrayList<>();
        int passwordIcon = R.drawable.new_protect;
        if (pdfOptions.isPasswordProtected())
            passwordIcon = R.drawable.baseline_done_24;

        options.add(new EnhancementOptionsEntity(
                context, passwordIcon, R.string.password_protect_pdf_text));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_edit, R.string.edit_images_text));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_row_blue,
                String.format(context.getResources().getString(R.string.compress_image),
                        pdfOptions.getQualityString())));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_sort_two, R.string.filter_images_Text));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_file_two, R.string.set_page_size_text));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_file_two, R.string.image_scale_type));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_see, R.string.preview_image_to_pdf));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_corner,
                String.format(context.getResources().getString(R.string.border_dialog_title),
                        pdfOptions.getBorderWidth())));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_up_down, R.string.rearrange_images));

        Drawable iconGrayScale = context.getResources().getDrawable(R.drawable.new_pdf_star);
        //iconGrayScale.setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.SRC_IN);

        options.add(new EnhancementOptionsEntity(
                iconGrayScale,
                context.getResources().getString(R.string.grayscale_images)));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_row_two, R.string.add_margins));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_size_blue, R.string.show_pg_num));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_watermark, R.string.add_watermark));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_color_blue, R.string.page_color));

        return options;
    }
}
