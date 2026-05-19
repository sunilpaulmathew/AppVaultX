package in.sunilpaulmathew.appvaultx.serializable;

import android.graphics.drawable.Drawable;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 15, 2026
 */
public class PermissionsEntry implements Serializable {

    private final Drawable drawable;
    private final int icon;
    private final String name;

    public PermissionsEntry(String name, Drawable drawable) {
        this.name = name;
        this.drawable = drawable;
        this.icon = Integer.MIN_VALUE;
    }

    public PermissionsEntry(String name, int icon) {
        this.name = name;
        this.icon = icon;
        this.drawable = null;
    }

    public String getPermission() {
        return this.name;
    }

    public void load(ImageButton button, TextView textView) {
        if (this.drawable != null) {
            button.setImageDrawable(this.drawable);
        } else if (this.icon != Integer.MIN_VALUE) {
            button.setImageDrawable(Utils.getDrawable(this.icon, button.getContext()));
        }
        if (this.name != null) {
            Pattern pattern = Pattern.compile("([A-Z_]+)$");
            Matcher matcher = pattern.matcher(this.name);

            if (matcher.find()) {
                textView.setText(matcher.group(1));
            } else {
                textView.setText(this.name);
            }
        }
    }

}