package com.example.splitch.features.splitting;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitch.Injection;
import com.example.splitch.R;
import com.example.splitch.ViewModelsFactory;
import com.example.splitch.databinding.FragmentBaseBinding;
import com.example.splitch.db.entities.Person;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SplittingFragment extends Fragment implements SplittingAdapter.OnChipClickListener {

    private static final String TAG = SplittingFragment.class.getSimpleName();

    private CompositeDisposable mDisposable = new CompositeDisposable();

    private SplittingViewModel viewModel;

    private SplittingAdapter adapter;

    private FragmentBaseBinding binding;

    private long mLastClickTime = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBaseBinding.inflate(inflater, container, false);

        ViewModelsFactory factory = Injection.provideViewModelFactory(this, getContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(SplittingViewModel.class);
        Log.i(TAG, "init view models");

        loadAllPersons();

        binding.recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        setupBottomBar();
        setupToolbar();

        binding.fabDone.setEnabled(false);

        binding.fabDone.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            checkAllItems();
        });

        return binding.getRoot();
    }

    private void checkAllItems() {
        //getting all products with persons
        List<ProductWithPersonMap> productWithPersonMapList = viewModel.getProductWithPersonMapList();
        //check whether all product have payers
        boolean isEmptyProductFound = findEmptyProducts(productWithPersonMapList);
        if(!isEmptyProductFound){
            //calculate price and add to person's total
            mDisposable.add(viewModel.calculateTotalPrice()
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        Log.i(TAG, "navigating to result screen");
                        Navigation.findNavController(binding.getRoot())
                                .navigate(R.id.action_splittingFragment_to_resultFragment);
                    }));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

        mDisposable.clear();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onChipClicked(boolean isChecked, ProductWithPersonMap productWithPersonMap, Person person) {
        if(isChecked){
            mDisposable.add(viewModel.addJunction(productWithPersonMap, person)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe());
        } else {
            mDisposable.add(viewModel.deleteJunction(productWithPersonMap, person)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe());
        }
    }

    private void setupBottomBar(){
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
        binding.toolbar.setTitle(getString(R.string.title_splitting));
        binding.toolbar.inflateMenu(R.menu.fragment_splitting);

        binding.toolbar.setNavigationOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
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
                        .navigate(R.id.action_splittingFragment_to_helpFragment, bundle);
                return true;
            }
            return false;
        });
    }

    private void loadAllPersons(){
        Log.i(TAG, TAG + " onStart");
        // firstly getting all person list to create all chips in view holder
        mDisposable.add(viewModel.getProductWithPersonMapListFromDb()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(productWithPersonMapList -> {
                            adapter = new SplittingAdapter(productWithPersonMapList, this);
                            adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
                            binding.recyclerView.setAdapter(adapter);
                            binding.fabDone.setEnabled(true);
                        },
                        throwable ->
                                Log.e(TAG, "unable to get persons to create adapter", throwable))
        );
    }

    private boolean findEmptyProducts(List<ProductWithPersonMap> productWithPersonMapList){
        List<Integer> emptyProductsWithPersons = new ArrayList<>();

        for(ProductWithPersonMap productWithPersonMap: productWithPersonMapList){
            int position = productWithPersonMapList.indexOf(productWithPersonMap);
            Collection<Boolean> collection = productWithPersonMap.getPersonStatusMap().values();

            if(!collection.contains(true)) {
                emptyProductsWithPersons.add(position);
                productWithPersonMap.setShowError(true);
            } else {
                productWithPersonMap.setShowError(false);
            }
        }

        for(Integer i: emptyProductsWithPersons){
            Log.i(TAG, "product with position " + i + " is empty");
        }

        if(emptyProductsWithPersons.size() > 0){
            //if some products have no payers, scroll to first one.
            int firstEmptyItemPosition = emptyProductsWithPersons.get(0);
            Snackbar.make(requireView(), R.string.error_product_without_person, Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.action_show), v -> binding.recyclerView.smoothScrollToPosition(firstEmptyItemPosition))
                    .setAnchorView(binding.fabDone)
                    .show();
            return true;
        }

        return false;
    }
}
