package in.sunilpaulmathew.appvaultx.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.RemovedPackagesAdapter;
import in.sunilpaulmathew.appvaultx.dialogs.BatchDialog;
import in.sunilpaulmathew.appvaultx.dialogs.ProgressDialog;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;
import in.sunilpaulmathew.appvaultx.utils.Async;
import in.sunilpaulmathew.appvaultx.utils.Packages;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class RemovedPackagesFragment extends Fragment {

    private ContentLoadingProgressBar progressBar;
    private LinearLayoutCompat contentLayout, headerLayout, progressLayout;
    private MaterialButton batchOptions;
    private RecyclerView recyclerView;
    private TextInputEditText editText;
    private RemovedPackagesAdapter adapter;
    private List<PackagesEntry> mPackages;
    private final List<PackagesEntry> selectedApps = new CopyOnWriteArrayList<>();
    private String mSearchText = null;

    @SuppressLint({"SetTextI18n", "StringFormatMatches"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packages, container, false);

        AppCompatImageButton apply = mRootView.findViewById(R.id.apply);
        progressBar = mRootView.findViewById(R.id.progress);
        batchOptions = mRootView.findViewById(R.id.batch_options);
        MaterialCardView searchCard = mRootView.findViewById(R.id.search_card);
        contentLayout = mRootView.findViewById(R.id.content_layout);
        headerLayout = mRootView.findViewById(R.id.header_layout);
        progressLayout = mRootView.findViewById(R.id.progress_layout);
        recyclerView = mRootView.findViewById(R.id.recycler_view);
        editText = mRootView.findViewById(R.id.search_word);
        MaterialButton searchButton = mRootView.findViewById(R.id.search);
        MaterialButton sortButton = mRootView.findViewById(R.id.sort);
        MaterialTextView title = mRootView.findViewById(R.id.title);

        title.setText(getString(R.string.apps_removed));

        recyclerView.setItemAnimator(null);

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

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchCard.getVisibility() == VISIBLE) {
                    searchCard.setVisibility(GONE);
                    editText.setText(null);
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
            @Override
            public void onPreExecute() {
                progressLayout.setVisibility(VISIBLE);
                headerLayout.setVisibility(GONE);
                contentLayout.setVisibility(GONE);
            }

            @Override
            public void doInBackground() {
                try {
                    mPackages = new CopyOnWriteArrayList<>();
                    progressBar.setMax(Packages.getUninstalledPackages().size());
                    for (PackagesEntry packageItems : Packages.getUninstalledPackages()) {
                        if (searchText == null || packageItems.getPackageName().toLowerCase().contains(searchText.toLowerCase())) {
                            mPackages.add(new PackagesEntry(packageItems.getPackageName(), packageItems.getAPKPath(), packageItems.getRemovalRecommendation(), packageItems.getUADDescription(), packageItems.getAppType(), packageItems.isInstalled()));
                        }
                        if (progressBar.getProgress() < Packages.getUninstalledPackages().size()) {
                            progressBar.setProgress(progressBar.getProgress() + 1);
                        } else {
                            progressBar.setProgress(0);
                        }
                    }

                    mPackages.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getPackageName(), rhs.getPackageName()));

                    if (Utils.getBoolean("reverse_order", false, requireActivity())) {
                        Collections.reverse(mPackages);
                    }
                    adapter = new RemovedPackagesAdapter(mPackages, selectedApps, batchOptions);
                } catch (NullPointerException ignored) {}
            }

            @Override
            public void onPostExecute() {
                if (!isAdded()) return;

                progressLayout.setVisibility(GONE);
                headerLayout.setVisibility(VISIBLE);
                contentLayout.setVisibility(VISIBLE);
                mSearchText = searchText;
                editText.setHint(getString(R.string.search_hint, adapter.getItemCount()));
                recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
                while (recyclerView.getItemDecorationCount() > 0) {
                    recyclerView.removeItemDecorationAt(0);
                }
                recyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
                recyclerView.setAdapter(adapter);
            }
        };
    }

    private Async restoreApps(List<String> apps) {
        return new Async() {
            private final List<Integer> positions = new CopyOnWriteArrayList<>();
            private ProgressDialog progressDialog;
            @Override
            public void onPreExecute() {
                progressDialog = new ProgressDialog(requireActivity());
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
                        positions.add(index);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if (!isAdded()) return;
                progressDialog.dismissDialog();
                for (Integer position : positions) {
                    adapter.notifyItemRemoved(position);
                }
                adapter.notifyItemRangeChanged(0, mPackages.size());
            }
        };
    }

}