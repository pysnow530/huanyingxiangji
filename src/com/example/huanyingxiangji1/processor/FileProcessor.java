package com.example.huanyingxiangji1.processor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.util.Log;

import com.example.huanyingxiangji1.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileProcessor {
    private static final String TAG = FileProcessor.class.getName();

    final String tag = "FileProcessor";

    String groupDirFullPath = MyApplication.group_path;
    String tmpDirFullPath = MyApplication.tmp_path;

    public ArrayList<String> getGroup(String parent, String groupName) {
        ArrayList<String> list = new ArrayList<String>();
        String[] tmp = new File(groupDirFullPath).list();
        for (int i = 0; i < tmp.length; i++) {
            String aGroupNamne = this.getGroupName(tmp[i]);
            if (aGroupNamne != null && aGroupNamne.equals(groupName)) {
                list.add(parent + tmp[i]);
                // Log.e("jin", tmp[i]);
            }
        }
        return list;
    }

    public ArrayList<String> getGroup(String groupName) {
        return getGroup(groupDirFullPath, groupName);
    }

    public List<String> getAllGroupName() {
        ArrayList<String> list = new ArrayList<String>() {
            // @Override
            // public boolean contains(Object o) {
            //
            // for (int i = 0; i < size(); i++) {
            // if (((String)o).equals(get(i))) {
            // return true;
            // }
            // }
            // return false;
            // }
        };
        Log.d(TAG, "groupDirFullPath = " + groupDirFullPath);
        String[] tmp = new File(groupDirFullPath).list();
        for (int i = 0; i < tmp.length; i++) {
            String aGroupNamne = this.getGroupName(tmp[i]);
            if (aGroupNamne != null && !list.contains(aGroupNamne)) {
                list.add(aGroupNamne);
                Log.e(tag, aGroupNamne);
            }
        }
        return list;
    }

    public String getGroupName(String fileName) {
        int indexofq = fileName.lastIndexOf("&");
        String tmp = null;
        if (indexofq != -1) {
            tmp = fileName.substring(0, indexofq);
        }
        return tmp;
    }

    public void removeGroup(String groupName, boolean isRealDel) {

        for (Iterator<String> iterator = getGroup(groupDirFullPath, groupName)
                .iterator(); iterator.hasNext(); ) {
            String filename = iterator.next();
            if (!isRealDel) {
                String destFilePath = tmpDirFullPath + filename;
                copyFile(filename, destFilePath);
            }
            Log.e(TAG, "remove the file: " + filename);
            removeFile(filename);
        }

    }

    public boolean removeFile(String fileName) {
        File file = new File(fileName);
        return file.delete();
    }

    public void createGroup(Context context, String groupName, Uri pic1Uri) throws IOException {
        checkDirs();

        String destFilePath1 = groupDirFullPath + groupName + "&1.jpg";
        InputStream in = context.getContentResolver().openInputStream(pic1Uri);
        OutputStream out = new FileOutputStream(new File(destFilePath1));
        copyFile(in, out);
        in.close();
        out.close();
    }

    public boolean copyFile(InputStream in, OutputStream out) {
        int byteread = 0;

        try {
            byte[] buffer = new byte[1024];

            while ((byteread = in.read(buffer)) != -1) {
                Log.d(TAG, "pppp");
                out.write(buffer, 0, byteread);
            }
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean copyFile(String srcFilePath, String destFilePath) {
        File srcFile = new File(srcFilePath);
        File destFile = new File(destFilePath);
        int byteread = 0;
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);

            return copyFile(in, out);
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    static public InputStream getInputStreamFrom(Uri uri, Context context) {
        String path = "";
        InputStream in = null;
        if (uri.getScheme().equals("content")) {
            Cursor c = context.getContentResolver().query(uri, null, null,
                    null, null);
            c.moveToFirst();
            byte buf[] = c.getBlob(c.getColumnIndex(Media.DATA));

            try {
                in = context.getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                in = new FileInputStream(uri.getPath());
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return in;
    }

    public void addToGroup(Context context, String groupName, Uri uri) throws IOException {
        InputStream in = context.getContentResolver().openInputStream(uri);

        int currentIndex = getGroup(groupDirFullPath, groupName).size() + 1;
        String destFileName = groupDirFullPath + groupName + "&" + currentIndex
                + ".jpg";
        OutputStream out = new FileOutputStream(destFileName);

        copyFile(in, out);
    }

    public static ArrayList<String> getWorksPaths() {
        ArrayList<String> list = new ArrayList<String>();
        File dir = new File(MyApplication.out_path);
        Log.e(TAG, "dir = " + dir.toString());
        if (dir.exists() && dir.isDirectory()) {
            for (String f : dir.list()) {
                Log.e(TAG, "f = " + f);
                list.add(MyApplication.out_path + f);

            }
        }
        return list;
    }


    private static boolean checkMedia() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            return false;
        }
    }

    public static void checkDirs() {
        Log.d(TAG, "check Dirs");
        if (checkMedia()) {
            Log.d(TAG, "check Media finish");
            checkDir(MyApplication.APP_SD_DIR);
            Log.d(TAG, "group_path = " + MyApplication.group_path);
            checkDir(MyApplication.group_path);
            checkDir(MyApplication.tmp_path);
            checkDir(MyApplication.out_path);
            checkDir(MyApplication.pic_path);
        } else {
            Log.e(TAG, "media card is not mounted");
        }
    }

    public static void checkDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }


}
