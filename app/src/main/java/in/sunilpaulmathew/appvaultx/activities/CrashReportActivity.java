package in.sunilpaulmathew.appvaultx.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;

import in.sunilpaulmathew.appvaultx.BuildConfig;
import in.sunilpaulmathew.appvaultx.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 05, 2026
 */
public class CrashReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crashreport);

        AppCompatImageButton mCopyButton = findViewById(R.id.copy);
        MaterialAutoCompleteTextView mCrashSteps = findViewById(R.id.crash_steps);
        MaterialButton mCancelButton = findViewById(R.id.cancel_button);
        MaterialButton mReportButton = findViewById(R.id.report_button);
        MaterialTextView mRequestMessage = findViewById(R.id.request_message);
        MaterialTextView mCrashLog = findViewById(R.id.crash_log);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_root), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        mRequestMessage.setText(getString(R.string.crash_report_message));

        if (getIntent().getStringExtra("crashLog") != null) {
            String mCrashText = "App Name: " + getString(R.string.app_name) +
                    "\nPackage Name: " + getPackageName() + "\nApp Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")" +
                    "\nSDK Version: " + Build.VERSION.SDK_INT + "\n\n==========\n Stacktrace\n==========\n\n" + getIntent().getStringExtra("crashLog");
            mCrashLog.setText(mCrashText);
        }

        mCancelButton.setOnClickListener(view -> finish());

        mCopyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied to clipboard", mCrashLog.getText() +
                    "\n\n" + getString(R.string.crash_reproduce_steps) + ": " + mCrashSteps.getText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        mReportButton.setOnClickListener(view -> {
            String mSteps = "";
            if (mCrashSteps.getText() != null && !mCrashSteps.getText().toString().trim().isEmpty()) {
                mSteps = mCrashSteps.getText().toString();
            }
            Intent share_log = new Intent(Intent.ACTION_SENDTO);
            share_log.setData(Uri.parse("mailto:"));
            share_log.putExtra(Intent.EXTRA_EMAIL, new String[] {
                    "smartpack.org@gmail.com"
            });
            share_log.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crash_report) + "/" + getString(R.string.app_name));
            share_log.putExtra(Intent.EXTRA_TEXT, mCrashLog.getText() +
                    "\n\n" + getString(R.string.crash_reproduce_steps) + ": " + mSteps);
            Intent shareIntent = Intent.createChooser(share_log, "Share");
            startActivity(shareIntent);
            finish();
        });
    }

}