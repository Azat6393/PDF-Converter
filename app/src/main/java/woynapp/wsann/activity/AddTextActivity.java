package woynapp.wsann.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import butterknife.ButterKnife;
import woynapp.wsann.R;
import woynapp.wsann.util.Constants;
import woynapp.wsann.util.FileUtils;
import woynapp.wsann.util.StringUtils;
import woynapp.wsann.util.ThemeUtils;

public class AddTextActivity extends AppCompatActivity {

    private int mCurrentImageIndex = 0;
    private ArrayList<String> mImages;
    private final HashMap<Integer, Uri> mCroppedImageUris = new HashMap<>();
    private boolean mCurrentImageEdited = false;
    private boolean mFinishedClicked = false;
    private ImageView mCropImageView;
    private EditText textEditText;
    private FrameLayout addTextLayout;

    private Button textColorBtn;

    float xDown = 0, yDown = 0;

    private int mTextColor;
    private boolean isEdited = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeUtils.getInstance().setThemeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        ButterKnife.bind(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_add_text);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mCropImageView = findViewById(R.id.imageView_add_text);
        textEditText = findViewById(R.id.text_edit_text);
        textColorBtn = findViewById(R.id.text_color_button);
        addTextLayout = findViewById(R.id.add_text_layout);

        mTextColor = Color.BLACK;

        textColorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextColor();
            }
        });

        addTextLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        xDown = event.getX();
                        yDown = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float movedX, movedY;
                        movedX = event.getX();
                        movedY = event.getY();

                        float distanceX = movedX - xDown;
                        float distanceY = movedY - yDown;

                        addTextLayout.setX(addTextLayout.getX() + distanceX);
                        addTextLayout.setY(addTextLayout.getY() + distanceY);

                        textEditText.setX(textEditText.getX() + distanceX);
                        textEditText.setY(textEditText.getY() + distanceY);
                        break;
                }
                return true;
            }
        });

        mImages = PdfViewerActivity.mImagesUri;
        toolbar.setTitle("");

        mFinishedClicked = false;

        for (int i = 0; i < mImages.size(); i++)
            mCroppedImageUris.put(i, Uri.fromFile(new File(mImages.get(i))));

        if (mImages.size() == 0)
            finish();

        setImage(0);
        Button cropImageButton = findViewById(R.id.cropButton_add_text);
        cropImageButton.setOnClickListener(view -> saveButtonClicked());

        ImageView nextImageButton = findViewById(R.id.nextimageButton_add_text);
        nextImageButton.setOnClickListener(view -> nextImageClicked());

        ImageView previousImageButton = findViewById(R.id.previousImageButton_add_text);
        previousImageButton.setOnClickListener(view -> prevImgBtnClicked());


    }

    public void saveButtonClicked() {
            try {
                if (textEditText.getText().toString().equals("") && isEdited) {
                    if (mFinishedClicked){
                        Intent intent = new Intent();
                        intent.putExtra(PdfViewerActivity.INTENT_RESULT_GET_ADD_TEXT, mCroppedImageUris);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }else {
                    if (!textEditText.getText().toString().equals("")){
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCroppedImageUris.get(mCurrentImageIndex));
                        mCropImageView.setImageBitmap(textAsBitmap(bitmap, textEditText.getText().toString(), 45f, mTextColor));
                        textEditText.setText("");
                        isEdited = true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    private Bitmap textAsBitmap(Bitmap image, String text,
                                float textSize, int textColor) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setAntiAlias(true);

        float[] f = new float[9];
        mCropImageView.getImageMatrix().getValues(f);
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        final int origW = mCropImageView.getDrawable().getIntrinsicWidth();
        final int origH = mCropImageView.getDrawable().getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        Bitmap newMapBitmap = Bitmap.createBitmap(actW, actH, Bitmap.Config.ARGB_8888);
        try {
            Bitmap newBitmap = Bitmap.createBitmap(mCropImageView.getWidth(), mCropImageView.getHeight(), Bitmap.Config.ARGB_8888);
            Bitmap newImage = image.copy(Bitmap.Config.ARGB_8888, false);

            Canvas canvas = new Canvas(newBitmap);

            Point centerOfCanvas = new Point(canvas.getWidth() / 2, canvas.getHeight() / 2);
            int left = centerOfCanvas.x - (actW / 2);
            int top = centerOfCanvas.y - (actH / 2);
            int right = centerOfCanvas.x + (actW / 2);
            int bottom = centerOfCanvas.y + (actH / 2);

            Rect dest = new Rect(left, top, right, bottom);

            Paint paint2 = new Paint();
            paint.setFilterBitmap(true);
            canvas.drawBitmap(newImage, null, dest, paint2);

            canvas.drawText(text, addTextLayout.getX(), addTextLayout.getY(), paint);

            Canvas canvas1 = new Canvas(newMapBitmap);
            int centreX = (canvas1.getWidth()  - newBitmap.getWidth()) /2;
            int centreY = (canvas1.getHeight() - newBitmap.getHeight()) /2;
            canvas1.drawBitmap(newBitmap, centreX, centreY, paint2);

        } catch (Exception e) {
            Log.e("textAsBitmap", e.getMessage());
        }

        String root = Environment.getExternalStorageDirectory().toString();
        File folder = new File(root + Constants.pdfDirectory);

        String path = mCroppedImageUris.get(mCurrentImageIndex).getPath();
        String filename = "add_text_im";
        if (path != null) {
            filename = "added_text_" + FileUtils.getFileName(path);
        }
        File file = new File(folder, filename);

        try {
            FileOutputStream out = new FileOutputStream(file);
            newMapBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            setUpdatedImage(Uri.fromFile(file));
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newMapBitmap;
    }


    public void nextImageClicked() {
        if (mImages.size() == 0)
            return;

        if (!mCurrentImageEdited) {
            mCurrentImageIndex = (mCurrentImageIndex + 1) % mImages.size();
            setImage(mCurrentImageIndex);
        } else {
            StringUtils.getInstance().showSnackbar(this, R.string.save_first);
        }
    }

    public void prevImgBtnClicked() {
        if (mImages.size() == 0)
            return;

        if (!mCurrentImageEdited) {
            if (mCurrentImageIndex == 0) {
                mCurrentImageIndex = mImages.size();
            }
            mCurrentImageIndex = (mCurrentImageIndex - 1) % mImages.size();
            setImage(mCurrentImageIndex);
        } else {
            StringUtils.getInstance().showSnackbar(this, R.string.save_first);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_crop_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        } else if (item.getItemId() == R.id.action_done) {
            mFinishedClicked = true;
            saveButtonClicked();
        } else if (item.getItemId() == R.id.action_skip) {
            mCurrentImageEdited = false;
            nextImageClicked();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpdatedImage(Uri newImage) {
        String path = mCroppedImageUris.get(mCurrentImageIndex).getPath();
        File file = new File(path);
        file.delete();

        mCroppedImageUris.put(mCurrentImageIndex, newImage);
        mCropImageView.setImageURI(mCroppedImageUris.get(mCurrentImageIndex));

        if (mFinishedClicked) {
            Intent intent = new Intent();
            intent.putExtra(PdfViewerActivity.INTENT_RESULT_GET_ADD_TEXT, mCroppedImageUris);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    /**
     * Set image in crop image view & increment counters
     *
     * @param index - image index
     */
    private void setImage(int index) {

        mCurrentImageEdited = false;
        if (index < 0 || index >= mImages.size())
            return;
        TextView mImageCount = findViewById(R.id.imagecount_add_text);
        mImageCount.setText(String.format("%s %d of %d", ""
                , index + 1, mImages.size()));
        mCropImageView.setImageURI(mCroppedImageUris.get(index));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        for (Uri imagePath : mCroppedImageUris.values()) {
            File file = new File(imagePath.getPath());
            if (file.exists()) {
                file.delete();
            }
        }
        mImages.clear();
        isEdited = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Uri imagePath : mCroppedImageUris.values()) {
            File file = new File(imagePath.getPath());
            if (file.exists()) {
                file.delete();
            }
        }
        mImages.clear();
        isEdited = false;
    }

    private void setTextColor() {
        MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                .title(R.string.font_color)
                .customView(R.layout.dialog_text_color, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> {
                    View view = dialog.getCustomView();
                    ColorPickerView colorPickerView = view.findViewById(R.id.color_picker);
                    String hexColor = String.format("#%06X", (0xFFFFFF & colorPickerView.getColor()));
                    mTextColor = Color.parseColor(hexColor);
                    textColorBtn.setBackgroundColor(mTextColor);
                    textEditText.setTextColor(mTextColor);
                })
                .build();
        ColorPickerView colorPickerView = materialDialog.getCustomView().findViewById(R.id.color_picker);
        colorPickerView.setColor(mTextColor);
        materialDialog.show();
    }
}