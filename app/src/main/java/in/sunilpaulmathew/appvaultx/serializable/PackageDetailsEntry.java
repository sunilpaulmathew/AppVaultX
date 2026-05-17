package in.sunilpaulmathew.appvaultx.serializable;

import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 15, 2026
 */
public class PackageDetailsEntry implements Serializable {

    private final List<PermissionsEntry> permissions;
    private final String title, text;

    public PackageDetailsEntry(String title, String text) {
        this.title = title;
        this.text = text;
        permissions = null;
    }

    public PackageDetailsEntry(String title, String text, List<PermissionsEntry> permissions) {
        this.title = title;
        this.text = text;
        this.permissions = permissions;
    }

    public List<PermissionsEntry> getPermissions() {
        return this.permissions;
    }

    public void load(TextView title, TextView text) {
        if (title != null) {
            title.setText(this.title);
        }
        if (text != null) {
            String updatedTxt =  " : " + this.text;
            text.setText(updatedTxt);
        }
    }

}