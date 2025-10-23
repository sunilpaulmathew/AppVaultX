package in.sunilpaulmathew.appvaultx.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;

import in.sunilpaulmathew.appvaultx.BuildConfig;
import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class WelcomeDialog extends BottomSheetDialog {

    @SuppressLint("SourceLockedOrientationActivity")
    public WelcomeDialog(Context context) {
        super(context);

        View root = View.inflate(context, R.layout.layout_welcome, null);

        MaterialTextView title = root.findViewById(R.id.title);
        MaterialTextView summary = root.findViewById(R.id.summary);
        SwitchMaterial neverShow = root.findViewById(R.id.never_show);
        MaterialButton start = root.findViewById(R.id.start);
        MaterialButton learnShizuku = root.findViewById(R.id.shizuku_learn);

        title.setText(context.getString(R.string.app_version, BuildConfig.VERSION_NAME));
        summary.setText(Html.fromHtml(getSummaryText(), Html.FROM_HTML_MODE_LEGACY));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            summary.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        neverShow.setChecked(false);

        root.post(() -> {
            FrameLayout bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setFitToContents(true);
            }
        });

        setCancelable(false);
        setContentView(root);
        show();

        learnShizuku.setOnClickListener(v -> {
            Utils.loadUrl("https://shizuku.rikka.app/", v.getContext());
            dismiss();
        });

        start.setOnClickListener(v -> dismiss());

        neverShow.setOnCheckedChangeListener((buttonView, isChecked) -> Utils.saveBoolean("welcome_dialog_viewed", isChecked, context));
    }

    private static String getSummaryText() {
        return "Welcome to <b>AppVaultX</b> - App Control Made Easy<br>" +
                "<br>" +
                "\uD83D\uDCA1 AppVaultX helps you take control of your Android device with smart recommendations on which apps are safe to remove. Each app is categorized as <b><i>Recommended</i></b>, <b><i>Advanced</i></b>, <b><i>Expert</i></b>, or <b><i>Unsafe</i></b>, using trusted data from the Universal Android Debloater Next Generation project.<br>" +
                "<br>" +
                "⚡ With Shizuku support, AppVaultX unlocks powerful system-level tools — all within a sleek, modern Material Design interface. You can <b><i>force close</i></b> apps, <b><i>clear data and cache</i></b>, <b><i>back up APKs and icons</i></b>, <b><i>uninstall apps individually or in batches</i></b>, and even <b><i>restore removed system apps</i></b> with ease.<br>" +
                "<br>" +
                "\uD83D\uDCA1 Without Shizuku, AppVaultX works as a handy package viewer, letting you open installed apps, save APKs and icons, and access system settings.<br>" +
                "<br>" +
                "<b>Important Notes</b><br>" +
                "<br>" +
                "\uD83C\uDF10 <b><font color='#00B894'>Open Source</font></b>: AppVaultX is free and open-source. Contributions, bug reports, and translations are always welcome.<br>" +
                "<br>" +
                "⚠️ <b><font color='#FF0000'>Disclaimer</font></b>: Use AppVaultX at your own risk. The developers are not responsible for any data loss, system instability, or device damage.";
    }

}