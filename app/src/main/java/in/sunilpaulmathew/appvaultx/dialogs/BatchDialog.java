package in.sunilpaulmathew.appvaultx.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.BatchAdapter;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;
import in.sunilpaulmathew.appvaultx.utils.Async;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public abstract class BatchDialog extends BottomSheetDialog {

    public BatchDialog(List<PackagesEntry> selectedApps, Drawable headerIcon, String headerText, String positiveButtonText, Context context) {
        super(context);

        View root = View.inflate(context, R.layout.layout_batch, null);

        MaterialButton title = root.findViewById(R.id.title);
        MaterialButton apply = root.findViewById(R.id.apply);
        MaterialButton cancel = root.findViewById(R.id.cancel);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new BatchAdapter(selectedApps));

        title.setText(headerText);
        title.setIcon(headerIcon);
        apply.setText(positiveButtonText);

        setContentView(root);
        show();

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

        cancel.setOnClickListener(v -> dismiss());

        apply.setOnClickListener(v -> new Async() {
                    private List<String> apps;
                    @Override
                    public void onPreExecute() {
                        apps = new CopyOnWriteArrayList<>();

                    }

                    @Override
                    public void doInBackground() {
                        for (PackagesEntry entry : selectedApps) {
                            if (entry.isSelected()) {
                                apps.add(entry.getPackageName());
                            }
                        }
                    }

                    @Override
                    public void onPostExecute() {
                        if (!apps.isEmpty()) {
                            positiveButtonListener(apps);
                        }
                        dismiss();
                    }
                }.execute()
        );
    }

    public abstract void positiveButtonListener(List<String> packageNames);

}