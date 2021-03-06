package com.example.huanyingxiangji1.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.huanyingxiangji1.R;
import com.example.huanyingxiangji1.processor.FileProcessor;
import com.example.huanyingxiangji1.processor.PicProcessor;
import com.example.huanyingxiangji1.utils.LogHelper;
import com.example.huanyingxiangji1.utils.ViewUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

public class CreateNewGroup extends Activity implements OnClickListener {
    private static final String TAG = "CreateNewGroup";

    private static final int REQUEST_SELECT_PIC1 = 1;
    private static final int REQUEST_SELECT_PIC2 = 2;

    private ImageButton picButton1, picButton2;
    private Button okButton, cancelButton;
    private EditText groupNameText;

    private Uri mPic1Uri;
    private Uri mPic2Uri;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_group);
        initView();

        if (getIntent().getExtras() == null) {
            return;
        }

        String pic1 = getIntent().getExtras().getString("mengpic");
        String pic2 = getIntent().getExtras().getString("newpic");
        mPic1Uri = Uri.parse(pic1);
        mPic2Uri = Uri.parse(pic2);

        loadImage();
    }

    private void initView() {
        picButton1 = (ImageButton) findViewById(R.id.picButton1);
        picButton2 = (ImageButton) findViewById(R.id.picButton2);
        okButton = (Button) findViewById(R.id.OK);
        cancelButton = (Button) findViewById(R.id.cancel);
        groupNameText = (EditText) findViewById(R.id.groupNameInput);
        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        picButton1.setOnClickListener(this);
        picButton2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.OK:
                String groupName = groupNameText.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    ViewUtils.showToast(this, getString(R.string.new_group_empty_name));
                    return;
                }
                finishCreateNewGroup(groupName);
                break;
            case R.id.cancel:
                setResult(RESULT_CANCELED);
                this.finish();
                break;
            case R.id.picButton1:
                selectPic(REQUEST_SELECT_PIC1);
                break;
            case R.id.picButton2:
                selectPic(REQUEST_SELECT_PIC2);
                break;
            default:
                break;
        }
    }

    private void selectPic(int which) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("image/*");
        startActivityForResult(intent, which);
    }


    private void finishCreateNewGroup(String groupName) {
        boolean hasPic1 = mPic1Uri != null;
        boolean hasPic2 = mPic2Uri != null;
        if (!hasPic1 && !hasPic2) {
            ViewUtils.showToast(this, getString(R.string.new_group_empty));
            return;
        }
        Uri first = null;
        Uri second = null;
        if (hasPic1) {
            first = mPic1Uri;
        } else {
            first = mPic2Uri;
        }
        if (hasPic1 && hasPic2) {
            second = mPic2Uri;
        }

        FileProcessor processor = new FileProcessor();
        try {
            processor.createGroup(this, groupName, first);
            if (hasPic1 && hasPic2) {
                processor.addToGroup(this, groupName, second);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ViewUtils.showToast(this, getString(R.string.group_create_error));
        }
        this.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void loadImage() {
        loadImage(mPic1Uri, picButton1);
        loadImage(mPic2Uri, picButton2);
    }

    private void loadImage(Uri uri, final ImageButton ib) {
        final Bitmap b = PicProcessor.getBitmapFromUri(this, uri,
                PicProcessor.SCALE_MID);
        if (b == null) {
            ViewUtils.showToast(this, getString(R.string.new_group_load_pic_err));
            return;
        }
        recycleImageView(ib);
        ib.setImageBitmap(b);
        ib.setTag(b);
        ib.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                adjustImageViewHeight(ib, b);
                ib.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    private void recycleImageView(ImageButton ib) {
        if (ib.getTag() != null) {
            Bitmap old = (Bitmap) ib.getTag();
            old.recycle();
            ib.setTag(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleImageView(picButton1);
        recycleImageView(picButton2);
    }

    private void adjustImageViewHeight(ImageButton view, Bitmap bitmap) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        LogHelper.i(TAG, "the bitmap size (" + bitmapWidth + ", " + bitmapHeight + ")");
        LogHelper.i(TAG, "the view size (" + params.width + ", " + params.height + ")");
        int viewWidth = view.getWidth();
        LogHelper.i(TAG, "the view size (" + view.getWidth() + ", " + view.getHeight() + ")");

        params.height = (int) (bitmapHeight * (float) viewWidth / bitmapWidth);
        view.setLayoutParams(params);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            return;
        }

        if (data == null) {
            throw new RuntimeException("no data");
        } else {
            Uri uri = data.getData();
            switch (requestCode) {
                case REQUEST_SELECT_PIC1:
                    mPic1Uri = uri;
                    loadImage(uri, picButton1);
                    break;
                case REQUEST_SELECT_PIC2:
                    mPic2Uri = uri;
                    loadImage(uri, picButton2);
                    break;
                default:
                    break;
            }
        }
    }
}
