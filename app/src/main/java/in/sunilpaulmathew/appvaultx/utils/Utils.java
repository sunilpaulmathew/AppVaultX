package in.sunilpaulmathew.appvaultx.utils;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import in.sunilpaulmathew.appvaultx.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class Utils {

    public static boolean delete(File file) {
        if (file.isDirectory()) {
            for (File files : Objects.requireNonNull(file.listFiles()))
                delete(files);
        }
        return file.delete();
    }

    public static boolean getBoolean(String name, boolean defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaults);
    }

    public static boolean isGooglePlayVersion(Context context) {
        String installer = getInstaller(context.getPackageName(), context);
        return installer != null && installer.matches("com\\.android\\.vending|com\\.google\\.android\\.feedback");
    }

    public static boolean mkdir(File folder) {
        return folder.mkdirs();
    }

    public static Drawable getDrawable(int drawable, Context context) {
        return ContextCompat.getDrawable(context, drawable);
    }

    public static File getExportPath(Context context) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                context.getString(R.string.app_name));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (file.exists() && file.isFile()) {
                delete(file);
            }
            mkdir(file);
        } else {
            if (!file.exists()) {
                mkdir(file);
            }
        }
        return file;
    }

    public static int getColor(int color, Context context) {
        return ContextCompat.getColor(context, color);
    }

    public static int getInt(String name, int defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(name, defaults);
    }

    public static int getUserID() {
        int uid = android.os.Process.myUid();
        return  uid / 100000;
    }

    private static String getInstaller(String packageName, Context context) {
        try {
            return context.getPackageManager().getInstallerPackageName(packageName);
        } catch (Exception ignored) {}
        return null;
    }

    public static String readAssetFile(String assetName, Context context) throws IOException {
        return read(context.getAssets().open(assetName));
    }

    private static String read(InputStream inputStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        for (int result = bis.read(); result != -1; result = bis.read()) {
            buf.write((byte) result);
        }
        return buf.toString("UTF-8");
    }

    public static Toast toast(String message, Context context) {
        return Toast.makeText(context, message, Toast.LENGTH_LONG);
    }

    public static void copy(InputStream inputStream, File dest) {
        try {
            copyStream(Objects.requireNonNull(inputStream), dest);
        } catch (IOException ignored) {}
    }

    public static void copy(File source, File dest) {
        try (FileInputStream fis = new FileInputStream(source)) {
            copyStream(fis, dest);
        } catch (IOException ignored) {}
    }

    public static void copy(Uri uri, File dest, Context context) {
        try {
            copyStream(Objects.requireNonNull(context.getContentResolver().openInputStream(uri)), dest);
        } catch (IOException ignored) {}
    }

    private static void copyStream(InputStream from, File dest) throws IOException {
        try (FileOutputStream to = new FileOutputStream(dest)) {
            byte[] buf = new byte[1024 * 1024];
            int len;
            while ((len = from.read(buf)) > 0) {
                to.write(buf, 0, len);
            }
        }
    }

    public static void copyToClipboard(String text, Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied to clipboard", text);
        clipboard.setPrimaryClip(clip);
        toast("Copied to clipboard", context).show();
    }

    public static void create(String text, File path) {
        try {
            FileWriter writer = new FileWriter(path);
            writer.write(text);
            writer.close();
        } catch (IOException ignored) {
        }
    }

    public static void loadUrl(String url, Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (ActivityNotFoundException ignored) {}
    }

    public static void saveBoolean(String name, boolean value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name, value).apply();
    }

    public static void saveInt(String name, int value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(name, value).apply();
    }

}