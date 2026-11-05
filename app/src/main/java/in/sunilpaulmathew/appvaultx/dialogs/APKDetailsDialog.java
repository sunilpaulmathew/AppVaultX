package in.sunilpaulmathew.appvaultx.dialogs;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.PackageDetailsAdapter;
import in.sunilpaulmathew.appvaultx.adapters.PackageHeaderAdapter;
import in.sunilpaulmathew.appvaultx.serializable.PackageDetailsEntry;
import in.sunilpaulmathew.appvaultx.serializable.PackageHeaderEntry;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public abstract class APKDetailsDialog extends BottomSheetDialog {

    public APKDetailsDialog(Drawable appIcon, String appName, String packageName, List<PackageHeaderEntry> headers,
                            List<PackageDetailsEntry> details, boolean update, boolean downgrade, boolean canInstall,
                            boolean shouldExit, Activity activity) {
        super(activity);

        View rootView = View.inflate(activity, R.layout.layout_apk_details, null);
        AppCompatImageButton app_Icon = rootView.findViewById(R.id.app_icon);
        MaterialButton cancel_Button = rootView.findViewById(R.id.cancel);
        MaterialButton install_Button = rootView.findViewById(R.id.install);
        MaterialTextView app_Name = rootView.findViewById(R.id.app_name);
        MaterialTextView package_Name = rootView.findViewById(R.id.package_name);
        RecyclerView recyclerView_Header = rootView.findViewById(R.id.recycler_view_header);
        RecyclerView recyclerView_Details = rootView.findViewById(R.id.recycler_view_details);

        recyclerView_Header.setLayoutManager(new LinearLayoutManager(activity, Settings.isPortraitMode(activity) ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL, false));
        recyclerView_Header.setAdapter(new PackageHeaderAdapter(headers));

        recyclerView_Details.setLayoutManager(new GridLayoutManager(activity, Settings.isPortraitMode(activity) ? 1 : Settings.isTablet(activity) ? 3 : 2));
        recyclerView_Details.setAdapter(new PackageDetailsAdapter(details,appIcon, appName, packageName));

        app_Name.setText(appName);
        package_Name.setText(packageName);
        app_Icon.setImageDrawable(appIcon);

        if (canInstall) {
            install_Button.setIcon(Utils.getDrawable(downgrade ? R.drawable.ic_downgrade : R.drawable.ic_download, activity));
            install_Button.setText(activity.getString(update ? downgrade ? R.string.downgrade : R.string.update : R.string.install));
            cancel_Button.setVisibility(VISIBLE);
        } else {
            install_Button.setIcon(Utils.getDrawable(R.drawable.ic_cancel, activity));
            install_Button.setText(activity.getString(R.string.cancel));
            cancel_Button.setVisibility(GONE);
        }

        cancel_Button.setOnClickListener(v -> {
            dismiss();
            if (shouldExit) {
                activity.finish();
            }
        });

        install_Button.setOnClickListener(v -> {
            if (canInstall) {
                onInstall();
            } else {
                dismiss();
                if (shouldExit) {
                    activity.finish();
                }
            }
        });

        rootView.post(() -> {
            if (Settings.isPortraitMode(activity)) return;
            FrameLayout bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setFitToContents(true);
            }
        });

        setContentView(rootView);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        setOnCancelListener(dialog -> {
            if (shouldExit) {
                activity.finish();
            }
        });
        show();
    }

    public abstract void onInstall();

}