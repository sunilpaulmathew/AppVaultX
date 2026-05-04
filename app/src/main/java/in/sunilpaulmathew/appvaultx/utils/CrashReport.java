package in.sunilpaulmathew.appvaultx.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import in.sunilpaulmathew.appvaultx.activities.CrashReportActivity;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 05, 2026
 * Ref: https://stackoverflow.com/questions/601503/how-do-i-obtain-crash-data-from-my-android-application
 */
public class CrashReport implements Thread.UncaughtExceptionHandler {

    private final Context mContext;
    private final Thread.UncaughtExceptionHandler mDefaultUEH;

    public CrashReport(Context context) {
        this.mContext = context;
        this.mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @SuppressLint("SimpleDateFormat")
    public void uncaughtException(@NonNull Thread t, Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        Utils.create(stacktrace, new File(mContext.getExternalFilesDir("logs"), "crashLog_" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())));
        Intent intent = new Intent(mContext, CrashReportActivity.class);
        intent.putExtra("crashLog", stacktrace);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
        mDefaultUEH.uncaughtException(t, e);
    }

    public void initialize() {
        Thread.setDefaultUncaughtExceptionHandler(new CrashReport(mContext));
    }

}