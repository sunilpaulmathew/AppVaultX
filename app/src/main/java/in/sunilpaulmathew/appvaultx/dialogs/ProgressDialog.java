package in.sunilpaulmathew.appvaultx.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ContentLoadingProgressBar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import in.sunilpaulmathew.appvaultx.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class ProgressDialog extends MaterialAlertDialogBuilder {

    private final AlertDialog mAlertDialog;
    private final ContentLoadingProgressBar mProgressBar;

    public ProgressDialog(Context context) {
        super(context);

        View progressLayout = View.inflate(context, R.layout.layout_progress_view, null);
        mProgressBar = progressLayout.findViewById(R.id.progress);

        mAlertDialog = new MaterialAlertDialogBuilder(context)
                .setView(progressLayout)
                .setCancelable(false)
                .create();
    }

    public void setProgressIcon(Drawable drawable) {
        setIcon(drawable);
    }

    public void setProgressIcon(int resource) {
        setIcon(resource);
    }

    public void setProgressStatus(int resource) {
        setTitle(resource);
    }

    public void setProgressStatus(String title) {
        setTitle(title);
    }

    public void startDialog() {
        if (!mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }
    }

    public void dismissDialog() {
        if (mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    public void updateProgress(int progress) {
        mProgressBar.setProgress(mProgressBar.getProgress() + progress);
    }

    public void setProgressMax(int max) {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setMax(max);
    }

}