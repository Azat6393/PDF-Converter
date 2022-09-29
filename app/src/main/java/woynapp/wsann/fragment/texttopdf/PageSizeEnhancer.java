package woynapp.wsann.fragment.texttopdf;

import android.content.Context;
import androidx.annotation.NonNull;

import woynapp.wsann.R;
import woynapp.wsann.interfaces.Enhancer;
import woynapp.wsann.model.EnhancementOptionsEntity;
import woynapp.wsann.preferences.TextToPdfPreferences;
import woynapp.wsann.util.PageSizeUtils;

/**
 * An {@link Enhancer} that lets you select page size.
 */
public class PageSizeEnhancer implements Enhancer {

    private final PageSizeUtils mPageSizeUtils;
    private final EnhancementOptionsEntity mEnhancementOptionsEntity;

    PageSizeEnhancer(@NonNull final Context context) {
        mPageSizeUtils = new PageSizeUtils(context);
        mEnhancementOptionsEntity = new EnhancementOptionsEntity(
                context, R.drawable.new_file_two, R.string.set_page_size_text);

        PageSizeUtils.mPageSize = new TextToPdfPreferences(context).getPageSize();
    }

    @Override
    public void enhance() {
        mPageSizeUtils.showPageSizeDialog(false);
    }

    @Override
    public EnhancementOptionsEntity getEnhancementOptionsEntity() {
        return mEnhancementOptionsEntity;
    }

}
