package com.example.huanyingxiangji1.processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.example.huanyingxiangji1.gif.GifEncoder;
import com.example.huanyingxiangji1.utils.LogHelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import com.example.huanyingxiangji1.thirdpart.AnimatedGifEncoder;

public class PicProcessor {
    private static final String TAG = "PicProcessor";
    String tag = "GifProcessor";
    // AnimatedGifEncoder animatedGifEncoder;

    static final public float SCALE_SMALL = 0.2f;
    static final public float SCALE_MID = 0.5f;
    static final public float SCALE_BIG = 1f;

    public PicProcessor() {
    }

    /**
     * 根据 uri 获取一个缩放了的bitmap
     *
     * @param c
     * @param uri
     * @param scale
     * @return 图片
     */
    public static Bitmap getBitmapFromUri(Context c, Uri uri, float scale) {
        Bitmap b;
        if (uri.getScheme().equals("content")) {
            try {
                b = BitmapFactory.decodeStream(c.getContentResolver()
                        .openInputStream(uri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            b = BitmapFactory.decodeFile(uri.getPath());
        }
        if (b == null) {
            return null;
        }

        // 缩小
        Bitmap tmp = Bitmap.createScaledBitmap(b, (int) (b.getWidth() * scale),
                (int) (b.getHeight() * scale), true);
        b.recycle();
        return tmp;
    }

    static public void storePic(Bitmap bitmap, String picPath)
            throws IOException {
        LogHelper.i(TAG, "the size of the picture to be writen is (" + bitmap.getWidth() + ", " + bitmap.getHeight() + ")");

        File file = new File(picPath);
        BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream(file));
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        out.flush();
        out.close();
    }

    static public Bitmap rotatePic(Bitmap bitmap, int angle) {
        // Log.e("imageview","width:"+mengImageView.getWidth()+" : height: "+mengImageView.getHeight());
        // Log.e("surface","width:"+surfaceView.getWidth()+" : height: "+surfaceView.getHeight());
        // Log.e("bitmap","width:"+bitmap.getWidth()+" : height: "+bitmap.getHeight());
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(angle);
        Bitmap tmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        // Log.e("bitmap","width:"+bitmap.getWidth()+" : height: "+bitmap.getHeight());
        return tmp;
    }

    public static void generateGif(List<String> picPathList,
                                   String gifPath, int delay) throws Exception {
        try {
            String tmpFile = gifPath + ".tmp";
            long start = SystemClock.currentThreadTimeMillis();
            GifEncoder gifEncoder = new GifEncoder();
            gifEncoder.start(new FileOutputStream(tmpFile));

            gifEncoder.setDelay(delay); // 500ms between frames

            // Grab frames and encode them

            Iterator<String> iter = picPathList.iterator();
            while (iter.hasNext()) {
                String filename = iter.next();
                Bitmap bitmap = getBitmapFromUri(null, SomeTool.getUriFromPath(filename), PicProcessor.SCALE_MID);
                gifEncoder.addFrame(bitmap);
                bitmap.recycle();
            }

            // Make the gif
            gifEncoder.finish();
            LogHelper.d(TAG, "the time of gif encode  = " + (SystemClock.currentThreadTimeMillis() - start));
            new File(tmpFile).renameTo(new File(gifPath));
        } catch (IOException err) {
            Log.getStackTraceString(err);
        }

    }

    //
    // public int seperateGif(String gifPath,ArrayList<BufferedImage>list) {
    // ArrayList<BufferedImage>tmp=new ArrayList<BufferedImage>();
    // int t = 0;
    // GifDecoder d = new GifDecoder();
    // d.read(gifPath);
    // int n = d.getFrameCount();
    // for (int i = 0; i < n; i++) {
    // BufferedImage frame = d.getFrame(i); // frame i
    // t = d.getDelay(i); // display duration of frame in milliseconds
    // list.add(frame);
    // }
    //
    // return t;
    // }

    public void combinate(ArrayList<String> picPathList,
                          String destFilePath, int orientation) throws Exception {
        Bitmap bitmap = null;
        Bitmap destBitmap = null;
        Canvas canvas = null;
        int cellWidth = 0, cellHeight = 0;

        for (int i = 0; i < picPathList.size(); i++) {
            String fileName = picPathList.get(i);
            Log.e(tag, fileName);

            bitmap = getBitmapFromUri(null, SomeTool.getUriFromPath(fileName), PicProcessor.SCALE_MID);
            if (i == 0) {
                cellWidth = bitmap.getWidth();
                cellHeight = bitmap.getHeight();
                if (orientation == 0) {
                    destBitmap = Bitmap.createBitmap(picPathList.size()
                            * cellWidth, cellHeight, Bitmap.Config.RGB_565);
                } else if (orientation == 1) {
                    destBitmap = Bitmap.createBitmap(cellWidth,
                            picPathList.size() * cellHeight,
                            Bitmap.Config.RGB_565);
                }
            } else {
                bitmap = resizePicture(bitmap, cellWidth, cellHeight);
            }

            canvas = new Canvas(destBitmap);

            if (orientation == 0) {
                canvas.drawBitmap(bitmap, i * cellWidth, 0, null);
            } else if (orientation == 1) {
                canvas.drawBitmap(bitmap, 0, i * cellHeight, null);
            }
            bitmap.recycle();
        }

        // 这里使用临时文件，防止在文件还在写的过程中被读
        File tmpFile = new File(destFilePath + ".tmp");
        File outputFile = new File(destFilePath);
        tmpFile.createNewFile();
        long start = SystemClock.currentThreadTimeMillis();
        destBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                new FileOutputStream(tmpFile));
        tmpFile.renameTo(outputFile);
    }

    public Bitmap resizePicture(Bitmap bitmap, int cw, int ch) {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        float ratioX = (float) cw / w;
        float ratioY = (float) ch / h;
        Matrix matrix = new Matrix();
        matrix.postScale(ratioX, ratioY);
        Bitmap tmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        bitmap.recycle();
        return tmp;
    }

    public static Bitmap turnPicture(Bitmap b) {
        Paint p = new Paint();
        Matrix m = new Matrix();
        Bitmap result = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        Camera c = new Camera();
        c.rotateY(180);
        c.getMatrix(m);
        m.postTranslate(b.getWidth(), 0);

        canvas.drawBitmap(b, m, p);
        b.recycle();

        return result;
    }

    public class Size {
        public int width;
        public int height;
    }
}
