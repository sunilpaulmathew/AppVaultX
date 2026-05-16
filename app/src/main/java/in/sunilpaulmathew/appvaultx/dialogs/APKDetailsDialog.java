package in.sunilpaulmathew.appvaultx.dialogs;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.PackageDetailsAdapter;
import in.sunilpaulmathew.appvaultx.adapters.PackageHeaderAdapter;
import in.sunilpaulmathew.appvaultx.serializable.PackageDetailsEntry;
import in.sunilpaulmathew.appvaultx.serializable.PackageHeaderEntry;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public abstract class APKDetailsDialog extends MaterialAlertDialogBuilder {

    private AlertDialog alertDialog;

    public APKDetailsDialog(Drawable appIcon, String appName, String packageName, List<PackageHeaderEntry> headers, List<PackageDetailsEntry> details, boolean update, boolean downgrade, Activity activity) {
        super(activity);

        View rootView = View.inflate(activity, R.layout.layout_apk_details, null);
        AppCompatImageButton app_Icon = rootView.findViewById(R.id.app_icon);
        MaterialButton cancel_button = rootView.findViewById(R.id.cancel);
        MaterialButton install_button = rootView.findViewById(R.id.install);
        MaterialTextView app_Name = rootView.findViewById(R.id.app_name);
        MaterialTextView package_Name = rootView.findViewById(R.id.package_name);
        RecyclerView recyclerView_Header = rootView.findViewById(R.id.recycler_view_header);
        RecyclerView recyclerView_Details = rootView.findViewById(R.id.recycler_view_details);

        recyclerView_Header.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerView_Header.setAdapter(new PackageHeaderAdapter(headers));

        recyclerView_Details.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        recyclerView_Details.setAdapter(new PackageDetailsAdapter(details));


        app_Name.setText(appName);
        package_Name.setText(packageName);
        app_Icon.setImageDrawable(appIcon);

        install_button.setIcon(Utils.getDrawable(downgrade ? R.drawable.ic_downgrade : R.drawable.ic_download, activity));
        install_button.setText(activity.getString(update ? downgrade ? R.string.downgrade : R.string.update : R.string.install));

        cancel_button.setOnClickListener(v -> {
            alertDialog.dismiss();
            activity.finish();
        });

        install_button.setOnClickListener(v -> onInstall());

        setView(rootView);
        setCancelable(false);

        alertDialog = create();
        alertDialog.show();
    }

    public abstract void onInstall();

}