package in.sunilpaulmathew.appvaultx.dialogs;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class AccessUnavilableDialog extends MaterialAlertDialogBuilder {

    public AccessUnavilableDialog(Context context) {
        super(context);
        setCancelable(false);
                setIcon(R.mipmap.ic_launcher);
                setTitle(context.getString(R.string.shizuku_unavailable_title));
                setMessage(context.getString(R.string.shizuku_unavailable_description));
                setNeutralButton(R.string.ignore, (dialogInterface, i) -> Utils.saveInt("shizuku_mode", 1, context));
                setPositiveButton(R.string.shizuku_learn, (dialogInterface, i) ->
                        Utils.loadUrl("https://shizuku.rikka.app/", context)
                );
    }

}