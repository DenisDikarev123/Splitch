package com.example.splitch.features.receipt;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitch.Injection;
import com.example.splitch.R;
import com.example.splitch.ViewModelsFactory;
import com.example.splitch.databinding.FragmentBaseBinding;
import com.example.splitch.db.entities.Product;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AddingReceiptFragment extends Fragment implements AddingReceiptAdapter.ListItemClickListener{

    private static final String TAG = AddingReceiptFragment.class.getSimpleName();

    private static final float MAX_PROGRESS = 0.9944997f;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    private AddingReceiptViewModel viewModel;

    private AddingReceiptAdapter adapter;

    private FragmentBaseBinding binding;

    private long mLastClickTime = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBaseBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ViewModelsFactory factory = Injection.provideViewModelFactory(this, getContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(AddingReceiptViewModel.class);
        Log.i(TAG, "init view models");

        setupBottomBar();

        initRecyclerView();

        binding.fragmentEmptyState.textEmptyState
                .setText(getString(R.string.add_products_hint));

        binding.fabDone.setOnClickListener(v ->
                Navigation.findNavController(view)
                .navigate(R.id.action_receiptFragment_to_addingPersonsFragment));

        binding.toolbar.setNavigationIcon(null);

        binding.fragmentEmptyState.animationEmptyState.setAnimation(R.raw.illustration_add_receipt);
        binding.fragmentEmptyState.animationEmptyState.setSpeed(2.5f);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");

        mDisposable.add(viewModel.getAllProducts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(productList -> {
                    adapter.submitList(productList);
                    Log.i(TAG, "updating product list");
                    updateUi(productList);
                    },
                        throwable -> Log.e(TAG, "unable to get products", throwable))
        );

        mDisposable.add(viewModel.getSubjectNewProductSnackbar()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    Log.i(TAG, "getting boolean value");
                    if(aBoolean){
                        Log.i(TAG, "showing insert toast");
                        showSnackbar(R.string.msg_item_insert);
                    }
                })
        );

        mDisposable.add(viewModel.getSubjectUpdateProductSnackbar()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    Log.i(TAG, "getting boolean value");
                    if(aBoolean){
                        Log.i(TAG, "showing update toast");
                        showSnackbar(R.string.msg_item_update);
                    }
                })
        );
    }

    @Override
    public void onPause() {
        super.onPause();

        float progress = binding.fragmentEmptyState.animationEmptyState.getProgress();
        viewModel.setAnimationProgress(progress);
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
    public void onListItemDeleteClick(Product product) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        deleteProduct(product);
    }

    @Override
    public void onListItemEditClick(Product product) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        viewModel.setProduct(product);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_receiptFragment_to_receiptBottomSheetDialogFragment);
    }

    private void updateUi(List<Product> products){
        if(products.size() == 0){
            binding.fabDone.hide();
            if(binding.fragmentEmptyState.textEmptyState.getVisibility() != View.VISIBLE) {
                binding.fragmentEmptyState.animationEmptyState.setVisibility(View.VISIBLE);
                binding.fragmentEmptyState.textEmptyState.setVisibility(View.VISIBLE);
            }
            Log.i(TAG, "updated list size is 0");
            float progress = viewModel.getAnimationProgress();
            playOrResumeAnimation(progress);
        } else {
            if(!(binding.fabDone.getVisibility() == View.VISIBLE)){
                binding.fabDone.show();
            }
            if(binding.fragmentEmptyState.textEmptyState.getVisibility() == View.VISIBLE) {
                binding.fragmentEmptyState.animationEmptyState.setVisibility(View.GONE);
                binding.fragmentEmptyState.textEmptyState.setVisibility(View.GONE);
            }
            viewModel.setAnimationProgress(MAX_PROGRESS);
        }
    }

    private void deleteProduct(Product product){
        mDisposable.add(viewModel.deleteItem(product)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> showSnackbar(R.string.msg_item_deleted),
                        throwable -> Log.e(TAG, "unable to delete product", throwable))
        );
        Log.i(TAG, "нажал на ");
    }

    private void showSnackbar(@StringRes int stringId){
        Snackbar.make(requireView(), stringId, Snackbar.LENGTH_LONG)
                .setAnchorView(binding.fabDone)
                .show();
    }

    private void setupBottomBar(){
        binding.bottomAppBar.inflateMenu(R.menu.fragment_receipt);

        binding.bottomAppBar.setOnMenuItemClickListener(item -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return false;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            if (item.getItemId() == R.id.menu_manually) {
                viewModel.setProduct(null);
                Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_receiptFragment_to_receiptBottomSheetDialogFragment);

                return true;
            }
            return false;
        });

        binding.bottomAppBar.setNavigationOnClickListener(v -> {
            // mis-clicking prevention, using threshold of 1000 ms
            Log.i(TAG, "time from last click (ms)" + (SystemClock.elapsedRealtime() - mLastClickTime));
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_global_navigationBottomSheetFragment);
        });
    }

    private void initRecyclerView(){
        binding.recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        adapter = new AddingReceiptAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);

        DividerItemDecoration itemDecorationTest =
                new DividerItemDecoration(requireContext(), layoutManager.getOrientation());
        itemDecorationTest.setDrawable(getResources().getDrawable(R.drawable.divider));
        binding.recyclerView.addItemDecoration(itemDecorationTest);
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
