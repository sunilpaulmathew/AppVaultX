package in.sunilpaulmathew.appvaultx.serializable;

import android.widget.TextView;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 15, 2026
 */
public class PackageDetailsEntry implements Serializable {

    private final String title, text;

    public PackageDetailsEntry(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public void load(TextView title, TextView text) {
        if (title != null) {
            title.setText(this.title);
        }
        if (text != null) {
            text.setText(this.text);
        }
    }

}