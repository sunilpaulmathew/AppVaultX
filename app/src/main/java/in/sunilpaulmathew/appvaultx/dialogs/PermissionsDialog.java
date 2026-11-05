package in.sunilpaulmathew.appvaultx.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.PermissionsAdapter;
import in.sunilpaulmathew.appvaultx.serializable.PermissionsEntry;
import in.sunilpaulmathew.appvaultx.utils.Settings;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 04, 2026
 */
public class PermissionsDialog extends BottomSheetDialog {

    public PermissionsDialog(Drawable appIcon, String appName, String packageName, List<PermissionsEntry> permissions, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_app_options, null);
        AppCompatImageView image = rootView.findViewById(R.id.image);
        MaterialButton cancel = rootView.findViewById(R.id.cancel);
        MaterialTextView title = rootView.findViewById(R.id.title);
        MaterialTextView description = rootView.findViewById(R.id.description);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        image.setImageDrawable(appIcon);
        title.setText(appName);
        description.setText(packageName);

        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new GridLayoutManager(context, Settings.isTablet(context) ? 2 : 1));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new PermissionsAdapter(permissions));

        rootView.post(() -> {
            if (Settings.isPortraitMode(context)) return;
            FrameLayout bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setFitToContents(true);
            }
        });

        cancel.setOnClickListener(v -> dismiss());
        setOnCancelListener(DialogInterface::dismiss);
        setContentView(rootView);
        show();
    }

}