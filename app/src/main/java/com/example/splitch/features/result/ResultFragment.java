package com.example.splitch.features.result;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitch.Injection;
import com.example.splitch.R;
import com.example.splitch.ViewModelsFactory;
import com.example.splitch.databinding.FragmentBaseBinding;
import com.example.splitch.db.MainDatabase;
import com.example.splitch.db.entities.Person;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.MaterialFadeThrough;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ResultFragment extends Fragment implements ResultAdapter.ListItemClickListener {

    private final static String TAG = ResultFragment.class.getSimpleName();

    private FragmentBaseBinding binding;

    private ResultViewModel viewModel;

    private ResultAdapter adapter;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    private long mLastClickTime = 0;

    private AlertDialog dialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBaseBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ViewModelsFactory factory = Injection.provideViewModelFactory(this, getContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(ResultViewModel.class);

        setupBottomBar();

        initRecyclerView();

        setupToolbar();

        dialog = createDialog();

        binding.fabDone.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            dialog.show();
            viewModel.setDialogOpened(true);
        });

        setExitTransition(new MaterialFadeThrough());
        setReenterTransition(new MaterialFadeThrough());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(viewModel.isDialogOpened()){
            dialog.show();
            viewModel.setDialogOpened(true);
        }

        mDisposable.add(viewModel.getPersons()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(personList -> {
                            adapter.submitList(personList);
                            Log.i(TAG, "updating list");
                            updateUi(personList);
                        },
                        throwable -> Log.e(TAG, "unable to get persons", throwable))
        );
    }

    @Override
    public void onStop() {
        super.onStop();

        if(dialog != null){
            dialog.dismiss();
        }

        mDisposable.clear();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onListItemDeleteClick(Person person) {
        //todo navigate to detail screen.
    }

    private androidx.appcompat.app.AlertDialog createDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.title_complete)
                .setMessage(R.string.msg_complete_with_app)
                .setNeutralButton(getString(R.string.action_sharing), (dialog1, which) -> {
                    shareData();
                    viewModel.setDialogOpened(false);
                })
                .setNegativeButton(getString(R.string.action_cancel), (dialog1, which) ->
                        viewModel.setDialogOpened(false))
                .setPositiveButton(getString(R.string.action_complete), (dialog1, which) -> {
                    viewModel.setDialogOpened(false);
                    clearAllDatabase();
                })
                .setOnCancelListener(dialog1 -> {
                    viewModel.setDialogOpened(false);
                    Log.i(TAG, "dialog is canceled.");
                });

        return builder.create();
    }

    private void clearAllDatabase() {
        mDisposable.add(Completable.fromAction(() -> MainDatabase.getInstance(requireContext()).clearAllTables())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Navigation.findNavController(requireView())
                        .popBackStack(R.id.receiptFragment, false)));
    }

    private void shareData() {
        mDisposable.add(viewModel.getPersons()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(personList -> {
                    String message = formMessage(personList);
                    sendMessageIntent(message);
                }));
    }

    private String formMessage(List<Person> personList) {
        StringBuilder message = new StringBuilder(getString(R.string.msg_total));
        message.append("\n");
        for(Person person: personList){
            message.append(person.name).append("...").append(person.total).append("\n");
        }
        message.append("\n").append(getString(R.string.msg_thank_you));
        return message.toString();
    }

    private void sendMessageIntent(String message){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
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
            Log.i(TAG, "person list size is 0");
        } else {
            if(!(binding.fabDone.getVisibility() == View.VISIBLE)){
                binding.fabDone.show();
            }
            if(binding.fragmentEmptyState.textEmptyState.getVisibility() == View.VISIBLE) {
                binding.fragmentEmptyState.animationEmptyState.setVisibility(View.GONE);
                binding.fragmentEmptyState.textEmptyState.setVisibility(View.GONE);
            }
            Log.i(TAG, "person list size is " + personList.size());
        }
    }

    private void initRecyclerView(){
        binding.recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        adapter = new ResultAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    private void setupBottomBar(){
        binding.bottomAppBar.inflateMenu(R.menu.fragment_result);

        binding.bottomAppBar.setOnMenuItemClickListener(item -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return false;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            if (item.getItemId() == R.id.menu_share) {
                shareData();
                return true;
            }
            return false;
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
        binding.toolbar.setTitle(getString(R.string.title_results));
        binding.toolbar.inflateMenu(R.menu.fragment_splitting);

        binding.toolbar.setNavigationOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            viewModel.resetAllTotals();
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return false;
            }

            if(item.getItemId() == R.id.menu_help){
                Bundle bundle = new Bundle();
                bundle.putString("tag", TAG);
                Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_resultFragment_to_helpFragment, bundle);
                return true;
            }
            return false;
        });
    }
}
