package com.example.splitch.features.adding_persons;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitch.Injection;
import com.example.splitch.R;
import com.example.splitch.ViewModelsFactory;
import com.example.splitch.databinding.FragmentBaseBinding;
import com.example.splitch.db.entities.Person;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AddingPersonsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AddingPersonsAdapter.ListItemClickListener {

    private final static String TAG = AddingPersonsFragment.class.getSimpleName();

    private final static float MAX_PROGRESS = 0.9882129f;

    private final static int PERMISSIONS_REQUEST_READ_CONTACTS = 153;

    private AddingPersonsViewModel viewModel;

    private FragmentBaseBinding binding;

    private AddingPersonsAdapter adapter;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    private AlertDialog alertDialog;

    private static final String[] PROJECTION = {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            };

    private long mLastClickTime = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBaseBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ViewModelsFactory factory = Injection.provideViewModelFactory(this, getContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(AddingPersonsViewModel.class);

        binding.fragmentEmptyState.textEmptyState.setText(R.string.add_persons_hint);

        setupBottomBar();

        initRecyclerView();

        setupToolbar();

        binding.fabDone.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            Navigation.findNavController(view)
                .navigate(R.id.action_addingPersonsFragment_to_splittingFragment);
        });

        Log.i(TAG, "onCreate");

        binding.fragmentEmptyState.animationEmptyState.setAnimation(R.raw.illustration_add_persons);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mDisposable.add(viewModel.getAllPersons()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(personList -> {
                            adapter.submitList(personList);
                            Log.i(TAG, "updating person list");
                            updateUi(personList);
                        },
                        throwable -> Log.e(TAG, "unable to get persons", throwable))
        );

        mDisposable.add(viewModel.getSubjectNewPersonSnackbar()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    if(aBoolean){
                        showSnackBar(R.string.msg_person_insert);
                    }
                })
        );

        mDisposable.add(viewModel.getSubjectUpdatePersonSnackbar()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    if(aBoolean){
                        showSnackBar(R.string.msg_person_update);
                    }
                })
        );
        Log.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");
        Log.i(TAG, "is dialog open when starting? " + viewModel.isDialogOpen());
        if(viewModel.isDialogOpen()){
            alertDialog = createDialog(viewModel.getContacts().getContacts());
            alertDialog.show();
            viewModel.setDialogOpen(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "onPause");
        float progress = binding.fragmentEmptyState.animationEmptyState.getProgress();
        viewModel.setAnimationProgress(progress);
    }


    @Override
    public void onStop() {
        super.onStop();

        Log.i(TAG, "onStop");
        if(alertDialog != null){
            alertDialog.dismiss();
            alertDialog = null;
        }

        LoaderManager.getInstance(this).destroyLoader(0);

        mDisposable.clear();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //getting contacts from content provider'a
                LoaderManager.getInstance(this).initLoader(0, null, this);
            }
        }
    }

    @Override
    public void onListItemDeleteClick(Person person) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        mDisposable.add(viewModel.deletePerson(person)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> showSnackBar(R.string.msg_person_deleted)));
    }

    @Override
    public void onListItemEditClick(Person person) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        viewModel.setPerson(person);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_addingPersonsFragment_to_personsBottomSheetFragment);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(requireContext(),
                ContactsContract.Contacts.CONTENT_URI, PROJECTION,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC"
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        viewModel.swapCursor(data);
        mDisposable.add(viewModel.makeArrayOfNames()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((contactInfos, throwable) -> {
                    alertDialog = createDialog(contactInfos);
                    alertDialog.show();
                    viewModel.setDialogOpen(true);
                }));
        Log.i(TAG, "cursor is loaded");
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        viewModel.swapCursor(null);
        Log.i(TAG, "onLoaderReset");
    }

    private void updateUi(List<Person> personList){
        if(personList.size() == 0){
            if(binding.fabDone.getVisibility() == View.VISIBLE) {
                binding.fabDone.hide();
            }
            if(binding.fragmentEmptyState.textEmptyState.getVisibility() != View.VISIBLE) {
                binding.fragmentEmptyState.animationEmptyState.setVisibility(View.VISIBLE);
                binding.fragmentEmptyState.textEmptyState.setVisibility(View.VISIBLE);
            }
            float progress = viewModel.getAnimationProgress();
            playOrResumeAnimation(progress);
            Log.i(TAG, "person list size is 0");
        } else {
            if(!(binding.fabDone.getVisibility() == View.VISIBLE)){
                binding.fabDone.show();
            }
            if(binding.fragmentEmptyState.textEmptyState.getVisibility() == View.VISIBLE) {
                binding.fragmentEmptyState.animationEmptyState.setVisibility(View.GONE);
                binding.fragmentEmptyState.textEmptyState.setVisibility(View.GONE);
            }
            viewModel.setAnimationProgress(MAX_PROGRESS);
            Log.i(TAG, "person list size is " + personList.size());
        }
    }

    private AlertDialog createDialog(ContactInfo[] contacts){
        String[] names = new String[contacts.length];
        boolean[] checkedItems = new boolean[contacts.length];
        for(int i = 0; i < contacts.length; i++){
            names[i] = contacts[i].getName();
            checkedItems[i] = contacts[i].isSelected();
        }
        Log.i(TAG, "string size is " + contacts.length);
        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(requireContext());

        builder.setTitle(getString(R.string.title_choose_persons))
                 .setCancelable(false)
                 .setMultiChoiceItems(names, checkedItems, (dialog, which, isChecked) -> {
                     if(isChecked){
                         viewModel.addSelectedItem(which);
                     } else {
                         viewModel.removeSelectedItem(which);
                     }
                 })
                .setPositiveButton(getString(R.string.action_choose), (dialog, which) -> {
                    mDisposable.add(viewModel.saveSelectedItems()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(longs ->  {
                                    Log.i(TAG, "id сохраненого итема " + longs.size());
                                    if(longs.size() != 0) {
                                        showSnackBar(R.string.msg_persons_inserted);
                                    }
                                }));
                    viewModel.clearSelectedList();
                    viewModel.setDialogOpen(false);
                })
                .setNegativeButton(getString(R.string.action_cancel), (dialog, which) -> {
                    viewModel.clearSelectedList();
                    viewModel.setDialogOpen(false);
                });

        return builder.create();
    }

    private void checkPermission(){
        // checking for permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user
                showSnackbar();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_CONTACTS},
                        PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        } else {
            // Permission has already been granted
            LoaderManager.getInstance(this).initLoader(0, null, this);
        }
    }

    private void showSnackbar(){
        Snackbar.make(binding.getRoot(), R.string.msg_contacts_access_required,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_allow, view -> {
                    // Request the permission
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSIONS_REQUEST_READ_CONTACTS);
                })
                .setAnchorView(binding.fabDone)
                .show();
    }

    private void showSnackBar(@StringRes int resId){
        Snackbar.make(binding.getRoot(), resId, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.fabDone)
                .show();
    }

    private void setupBottomBar(){
        binding.bottomAppBar.inflateMenu(R.menu.fragment_adding_persons);

        binding.bottomAppBar.setOnMenuItemClickListener(item -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return false;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (item.getItemId()){
                case R.id.menu_manually:
                    viewModel.setPerson(null);
                    Navigation.findNavController(binding.getRoot())
                            .navigate(R.id.action_addingPersonsFragment_to_personsBottomSheetFragment);
                    return true;
                case R.id.menu_add_from_contacts:
                    checkPermission();
                    return true;
                default:
                    return false;
            }
        });

        binding.bottomAppBar.setNavigationOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_global_navigationBottomSheetFragment);
        });
    }

    private void setupToolbar(){
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);

        binding.toolbar.setNavigationOnClickListener(v ->{
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });

        binding.toolbar.setTitle(getString(R.string.title_add_persons));
    }

    private void initRecyclerView(){
        binding.recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        adapter = new AddingPersonsAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    private void playOrResumeAnimation(float progress){
        if(progress == 0f) {
            binding.fragmentEmptyState.animationEmptyState.playAnimation();
        } else if(progress < MAX_PROGRESS) {
            binding.fragmentEmptyState.animationEmptyState.setProgress(progress);
            binding.fragmentEmptyState.animationEmptyState.resumeAnimation();
        } else if(progress == MAX_PROGRESS){
            binding.fragmentEmptyState.animationEmptyState.setProgress(progress);
        }
        Log.i(TAG, "should play animation? " + progress);
    }
}
