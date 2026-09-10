/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package in.sunilpaulmathew.appvaultx.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 07, 2026
 */
public class ApplicationsViewModel extends ViewModel {
    private final MutableLiveData<Void> searchTrigger = new MutableLiveData<>(), sortTrigger = new MutableLiveData<>();

    public LiveData<Void> getSearchTrigger() {
        return searchTrigger;
    }

    public LiveData<Void> getSortTrigger() {
        return sortTrigger;
    }

    public void triggerSearch() {
        searchTrigger.setValue(null);
    }

    public void triggerSort() {
        sortTrigger.setValue(null);
    }

}