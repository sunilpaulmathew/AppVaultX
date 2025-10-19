package in.sunilpaulmathew.appvaultx.dialogs;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import in.sunilpaulmathew.appvaultx.BuildConfig;
import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.PolicyAdapter;
import in.sunilpaulmathew.appvaultx.utils.Settings;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class PolicyDialog extends MaterialAlertDialogBuilder {

    public PolicyDialog(Context context) {
        super(context);

        View root = View.inflate(context, R.layout.layout_privacy_policy, null);
        MaterialTextView version = root.findViewById(R.id.title);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        version.setText(context.getString(R.string.app_version, BuildConfig.VERSION_NAME));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new PolicyAdapter(Settings.getPolicyData()));
        setView(root);
        setPositiveButton(R.string.cancel, (dialog, id) -> {
        });
        show();
    }

}