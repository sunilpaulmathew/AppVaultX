package in.sunilpaulmathew.appvaultx.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.BottomMenuAdapter;
import in.sunilpaulmathew.appvaultx.serializable.MenuEntry;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public abstract class BottomMenuDialog extends BottomSheetDialog {

    public BottomMenuDialog(List<MenuEntry> menuItems, Drawable headerIcon, String headerTitle, String headerDescription, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_bottom_menu, null);
        AppCompatImageButton icon = rootView.findViewById(R.id.app_icon);
        MaterialTextView title = rootView.findViewById(R.id.app_name);
        MaterialTextView description = rootView.findViewById(R.id.package_name);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        title.setText(headerTitle);
        description.setText(headerDescription);
        icon.setImageDrawable(headerIcon);

        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new BottomMenuAdapter(menuItems, id -> {
            onMenuItemClicked(id);
            dismiss();
        }));
        setContentView(rootView);
        show();
    }

    public abstract void onMenuItemClicked(int menuID);

}