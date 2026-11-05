package in.sunilpaulmathew.appvaultx.dialogs;

import android.content.Context;
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
import in.sunilpaulmathew.appvaultx.adapters.AppOpsAdapter;
import in.sunilpaulmathew.appvaultx.serializable.AppOpsEntry;
import in.sunilpaulmathew.appvaultx.utils.Settings;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 16, 2026
 */
public class AppOpsDialog extends BottomSheetDialog {

    public AppOpsDialog(Drawable headerIcon, List<AppOpsEntry> appOps, String headerTitle, String headerDescription, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_app_options, null);
        AppCompatImageView image = rootView.findViewById(R.id.image);
        MaterialButton cancel = rootView.findViewById(R.id.cancel);
        MaterialTextView title = rootView.findViewById(R.id.title);
        MaterialTextView description = rootView.findViewById(R.id.description);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        title.setText(headerTitle);
        description.setText(headerDescription);
        image.setImageDrawable(headerIcon);

        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new GridLayoutManager(context, Settings.isTablet(context) ? 2 : 1));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new AppOpsAdapter(appOps, headerDescription));

        cancel.setOnClickListener(v -> dismiss());

        rootView.post(() -> {
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

}