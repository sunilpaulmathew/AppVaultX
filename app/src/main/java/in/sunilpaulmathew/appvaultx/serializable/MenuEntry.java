package in.sunilpaulmathew.appvaultx.serializable;

import android.content.Context;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class MenuEntry implements Serializable {

    private final int titleRes;
    private final int iconRes;
    private final int id;

    public MenuEntry(int titleRes, int iconRes, int id) {
        this.titleRes = titleRes;
        this.iconRes = iconRes;
        this.id = id;
    }

    public int getIcon() {
        return iconRes;
    }

    public int getID() {
        return id;
    }

    public String getTile(Context context) {
        return context.getString(titleRes);
    }

}