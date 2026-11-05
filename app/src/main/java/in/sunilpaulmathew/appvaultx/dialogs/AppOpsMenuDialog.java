package in.sunilpaulmathew.appvaultx.dialogs;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.AppOpsMenuAdapter;
import in.sunilpaulmathew.appvaultx.serializable.AppOpsMenuEntry;
import in.sunilpaulmathew.appvaultx.utils.Settings;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 16, 2026
 */
public abstract class AppOpsMenuDialog extends BottomSheetDialog {

    public AppOpsMenuDialog(List<AppOpsMenuEntry> menuItems, String currentStatus, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_recycler_view, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new AppOpsMenuAdapter(menuItems, currentStatus, id -> {
            onMenuItemClicked(id);
            dismiss();
        }));

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

        setContentView(rootView);
        show();
    }

    public abstract void onMenuItemClicked(int menuID);

}