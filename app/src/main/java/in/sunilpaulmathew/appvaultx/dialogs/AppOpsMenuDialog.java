package in.sunilpaulmathew.appvaultx.dialogs;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.AppOpsMenuAdapter;
import in.sunilpaulmathew.appvaultx.serializable.AppOpsMenuEntry;

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

        setContentView(rootView);
        show();
    }

    public abstract void onMenuItemClicked(int menuID);

}