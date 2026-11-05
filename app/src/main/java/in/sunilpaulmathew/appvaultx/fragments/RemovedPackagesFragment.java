package in.sunilpaulmathew.appvaultx.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.ApplicationsAdapter;
import in.sunilpaulmathew.appvaultx.dialogs.BatchDialog;
import in.sunilpaulmathew.appvaultx.dialogs.ProgressDialog;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;
import in.sunilpaulmathew.appvaultx.utils.Async;
import in.sunilpaulmathew.appvaultx.utils.Packages;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import in.sunilpaulmathew.appvaultx.utils.Utils;
import navView.NavView;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class RemovedPackagesFragment extends Fragment {

    private ApplicationsAdapter adapter;
    private ContentLoadingProgressBar progressBar;
    private LinearLayoutCompat progressLayout;
    private List<PackagesEntry> mPackages;
    private RecyclerView recyclerView;
    private NavView navigationView;
    private String mSearchText = null;
    private TextInputEditText editText;
    private final List<PackagesEntry> selectedApps = new CopyOnWriteArrayList<>();

    @SuppressLint({"SetTextI18n", "StringFormatMatches"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packages, container, false);

        AppCompatImageButton apply = mRootView.findViewById(R.id.apply);
        progressBar = mRootView.findViewById(R.id.progress);
        MaterialButton batchOptions = mRootView.findViewById(R.id.batch_options);
        MaterialCardView searchCard = mRootView.findViewById(R.id.search_card);
        MaterialTextView title = mRootView.findViewById(R.id.title);
        progressLayout = mRootView.findViewById(R.id.progress_layout);
        recyclerView = mRootView.findViewById(R.id.recycler_view);
        editText = mRootView.findViewById(R.id.search_word);
        MaterialButton searchButton = mRootView.findViewById(R.id.search);
        MaterialButton sortButton = mRootView.findViewById(R.id.sort);

        title.setText(R.string.apps_removed);

        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), Settings.isPortraitMode(requireActivity()) ? 1 : Settings.isTablet(requireActivity()) ? 3 : 2));
        adapter = new ApplicationsAdapter(new CopyOnWriteArrayList<>(), selectedApps, batchOptions, 2, requireActivity());
        recyclerView.setAdapter(adapter);

        navigationView = Settings.getNavView(requireActivity());

        searchButton.setOnClickListener(v -> {
            if (searchCard.getVisibility() == VISIBLE) {
                searchCard.setVisibility(GONE);
                editText.setText(null);
            } else {
                searchCard.setVisibility(VISIBLE);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    loadPackages(null).execute();
                    apply.setVisibility(GONE);
                } else {
                    apply.setVisibility(VISIBLE);
                }
            }
        });

        apply.setOnClickListener(view -> loadPackages(Objects.requireNonNull(editText.getText()).toString().trim()).execute());

        sortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(0, 0, Menu.NONE, R.string.reverse).setIcon(R.drawable.ic_sort_az).setCheckable(true).setChecked(Utils.getBoolean("reverse_order", false, requireActivity()));

            popupMenu.setForceShowIcon(true);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    Utils.saveBoolean("reverse_order", !Utils.getBoolean("reverse_order", false, requireActivity()), requireActivity());
                    loadPackages(mSearchText).execute();
                }
                return false;
            });
            popupMenu.show();
        });

        loadPackages(mSearchText).execute();

        batchOptions.setOnClickListener(v -> new BatchDialog(selectedApps, Utils.getDrawable(R.drawable.ic_reset, requireActivity()), getString(R.string.restore_selected, selectedApps.size()), getString(R.string.restore), v.getContext()) {
            @Override
            public void positiveButtonListener(List<String> apps) {
                restoreApps(apps).execute();
            }
        });

        Settings.applyMargin(recyclerView, requireActivity());

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchCard.getVisibility() == VISIBLE) {
                    searchCard.setVisibility(GONE);
                    editText.setText(null);
                } else if (!selectedApps.isEmpty()) {
                    selectedApps.clear();
                    adapter.refreshData();
                } else {
                    Settings.navigateToFragment(0, requireActivity());
                }
            }
        });

        return mRootView;
    }

    @SuppressLint("StringFormatMatches")
    private Async loadPackages(String searchText) {
        return new Async() {
            private final Activity activity = requireActivity();
            @Override
            public void onPreExecute() {
                progressLayout.setVisibility(VISIBLE);
                navigationView.setVisibility(GONE);
                mPackages = new CopyOnWriteArrayList<>();
            }

            private List<PackagesEntry> getData() {
                List<PackagesEntry> data = new CopyOnWriteArrayList<>();
                try {
                    String query = searchText == null ? "" : searchText.toLowerCase(Locale.ROOT);

                    int max = Packages.getUninstalledPackages().size();
                    progressBar.setMax(max);
                    for (PackagesEntry pkg : Packages.getUninstalledPackages()) {
                        if (!query.isEmpty() && !pkg.getPackageName().toLowerCase(Locale.ROOT).contains(query)) continue;

                        data.add(pkg);
                        if (progressBar.getProgress() < max) {
                            progressBar.setProgress(progressBar.getProgress() + 1);
                        } else {
                            progressBar.setProgress(0);
                        }
                    }

                    data.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getPackageName(), rhs.getPackageName()));

                    if (Utils.getBoolean("reverse_order", false, activity)) {
                        Collections.reverse(data);
                    }
                } catch (NullPointerException ignored) {}
                return data;
            }

            @Override
            public void doInBackground() {
                mPackages = getData();
            }

            @Override
            public void onPostExecute() {
                if (!isAdded() || isDetached() || activity.isFinishing() || activity.isDestroyed()) return;

                progressLayout.setVisibility(GONE);
                navigationView.setVisibility(VISIBLE);
                adapter.updateData(mPackages);
                editText.setHint(getString(R.string.search_hint, adapter.getItemCount()));
                mSearchText = searchText;
            }
        };
    }

    private Async restoreApps(List<String> apps) {
        return new Async() {
            private final Activity activity = requireActivity();
            private ProgressDialog progressDialog;
            @Override
            public void onPreExecute() {
                progressDialog = new ProgressDialog(activity);
                progressDialog.setProgressStatus(R.string.applying);
                progressDialog.startDialog();
            }

            @Override
            public void doInBackground() {
                String cmd = apps.stream()
                        .map(pkg -> "cmd package install-existing " + pkg)
                        .collect(Collectors.joining("; "));
                ShizukuShell.runCommands(cmd);

                for (String packageName : apps) {
                    PackagesEntry entry = Packages.getUninstalledPackages().stream().filter(i -> i.getPackageName().equals(packageName)).findFirst().orElse(null);
                    int index = IntStream.range(0, mPackages.size())
                            .filter(i -> mPackages.get(i).getPackageName().equals(packageName))
                            .findFirst()
                            .orElse(-1);
                    if (entry != null) {
                        selectedApps.remove(entry);
                        Packages.getUninstalledPackages().remove(entry);
                        Packages.getPackages().add(new PackagesEntry(entry.getPackageName(), entry.getAPKPath(), entry.getRemovalRecommendation(), entry.getUADDescription(), entry.getAppType(), true));
                    }
                    if (index != -1) {
                        mPackages.remove(index);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if (!isAdded() || isDetached() || activity.isFinishing() || activity.isDestroyed()) return;
                progressDialog.dismissDialog();
                adapter.updateData(mPackages);
            }
        };
    }

    public void handleOrientationChange() {
        if (!isAdded() || isDetached() || requireActivity().isFinishing() || requireActivity().isDestroyed()) return;
        Settings.updateSpanCount(recyclerView);
    }

}