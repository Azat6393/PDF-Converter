package woynapp.wsann.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.itextpdf.text.Font;

import java.util.ArrayList;

import woynapp.wsann.R;
import woynapp.wsann.model.EnhancementOptionsEntity;

public class SettingsOptions {

    public static ArrayList<EnhancementOptionsEntity> getEnhancementOptions(Context context) {
        ArrayList<EnhancementOptionsEntity> options = new ArrayList<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_image,
                String.format(context.getString(R.string.image_compression_value_default),
                        sharedPreferences.getInt(Constants.DEFAULT_COMPRESSION, 30))));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_file,
                String.format(context.getString(R.string.page_size_value_def),
                        sharedPreferences.getString(Constants.DEFAULT_PAGE_SIZE_TEXT,
                                Constants.DEFAULT_PAGE_SIZE))));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_card_text,
                String.format(context.getString(R.string.font_size_value_def),
                        sharedPreferences.getInt(Constants.DEFAULT_FONT_SIZE_TEXT,
                                Constants.DEFAULT_FONT_SIZE))));

        Font.FontFamily fontFamily = Font.FontFamily.valueOf(
                sharedPreferences.getString(Constants.DEFAULT_FONT_FAMILY_TEXT,
                        Constants.DEFAULT_FONT_FAMILY));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_text_text,
                String.format(context.getString(R.string.font_family_value_def),
                        fontFamily.name())));

        options.add(new EnhancementOptionsEntity(
                context, R.drawable.new_color,
                String.format(context.getString(R.string.theme_value_def),
                        sharedPreferences.getString(Constants.DEFAULT_THEME_TEXT,
                                Constants.DEFAULT_THEME))));

        options.add(new EnhancementOptionsEntity(context,
                R.drawable.new_row, R.string.image_scale_type));

        options.add(new EnhancementOptionsEntity(context,
                R.drawable.new_lock_white, R.string.change_master_pwd));

        options.add(new EnhancementOptionsEntity(context,
                R.drawable.new_size_white, R.string.show_pg_num));

        return options;
    }

}
