package com.example.splitch.features.receipt;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.splitch.Injection;
import com.example.splitch.R;
import com.example.splitch.ViewModelsFactory;
import com.example.splitch.databinding.FragmentDialogReceiptBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class ReceiptBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = ReceiptBottomSheetFragment.class.getSimpleName();

    private static final String ERROR_MAX_NUMBER = "error_max_number";
    private static final String ERROR_EMPTY_PRICE = "error_empty_price";
    private static final String ERROR_EMPTY_NAME = "error_empty_name";

    private FragmentDialogReceiptBottomSheetBinding binding;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    private AddingReceiptViewModel viewModel;

    private TextWatcher nameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String name = binding.editTextName.getText().toString();
            Log.i(TAG, "onNameTextChanged");
            viewModel.updateProductName(name);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher priceTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String price = binding.editTextPrice.getText().toString();
            Log.i(TAG, "onPriceTextChanged");

            viewModel.updateProductPrice(price);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDialogReceiptBottomSheetBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Dialog dialog = getDialog();
            Window window = Objects.requireNonNull(dialog).getWindow();
            Objects.requireNonNull(window).setNavigationBarColor(Color.BLACK);
        }

        ViewModelsFactory factory = Injection.provideViewModelFactory(this, getContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(AddingReceiptViewModel.class);

        initProduct();

        Log.i(TAG, "on create view");

        binding.buttonBottomSheet.setOnClickListener(v -> {
            String name = Objects.requireNonNull(binding.editTextName.getText()).toString().trim();
            String price = Objects.requireNonNull(binding.editTextPrice.getText()).toString();
            Log.i(TAG, name + price);
            String nameOrError = viewModel.verifyName(name);
            String priceOrError = viewModel.verifyPrice(price);
            boolean isStrings = verifyStringOrError(nameOrError, priceOrError);
            if(isStrings){
                if(viewModel.isAdding()) {
                    Log.i(TAG, "product is inserting...");
                    addNewProduct(name, priceOrError);
                } else {
                    Log.i(TAG, "product is updating...");
                    updateProduct(name, priceOrError);
                }
            }
        });

        binding.editTextName.addTextChangedListener(nameTextWatcher);
        binding.editTextPrice.addTextChangedListener(priceTextWatcher);

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        return dialog;
    }

    @Override
    public void onStop() {
        super.onStop();

        mDisposable.clear();
    }

    @Override
    public void onDestroyView() {
        binding.editTextName.removeTextChangedListener(nameTextWatcher);
        binding.editTextPrice.removeTextChangedListener(priceTextWatcher);
        binding = null;
        Log.i(TAG, "on destroy view method called");
        super.onDestroyView();
    }

    private boolean verifyStringOrError(String nameOrError, String priceOrError){
        boolean checkName;
        boolean checkPrice;

        if(nameOrError.equals(ERROR_EMPTY_NAME)){
            checkName = false;
            binding.inputLayoutName.setError(getString(R.string.error_empty_name));
        } else {
            checkName = true;
            binding.inputLayoutName.setError(null);
        }

        if(priceOrError.equals(ERROR_EMPTY_PRICE)){
            checkPrice = false;
            binding.inputLayoutPrice.setError(getString(R.string.error_empty_price));
        } else if(priceOrError.equals(ERROR_MAX_NUMBER)){
            checkPrice = false;
            binding.inputLayoutPrice.setError(getString(R.string.error_max_price));
        } else {
            checkPrice = true;
            binding.inputLayoutPrice.setError(null);
        }

        return checkName && checkPrice;
    }

    private void initProduct() {
        mDisposable.add(viewModel.getObservableProduct()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currentProduct -> {
                    Log.i(TAG, "setting values: " + currentProduct.name + " " + currentProduct.price);
                    binding.editTextName.setText(currentProduct.name);
                    binding.editTextPrice.setText(currentProduct.price);
                    if(viewModel.isAdding()){
                        binding.buttonBottomSheet.setText(getString(R.string.action_add));
                    } else {
                        binding.buttonBottomSheet.setText(getString(R.string.action_change));
                    }
                }, throwable -> {
                    //nullPointerException passed here, when dialog opens for adding products
                    binding.editTextName.setText("");
                    binding.editTextPrice.setText(null);
                    binding.buttonBottomSheet.setText(getString(R.string.action_add));
                    Log.i(TAG, "product is null");
                }));
    }

    private void updateProduct(String name, String priceOrError) {
        mDisposable.add(viewModel.updateItem(name, priceOrError)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            viewModel.setSubjectUpdateProductSnackbar(true);
                            Navigation.findNavController(requireParentFragment().requireView()).popBackStack();
                        },
                        throwable -> Log.e(TAG, "unable to update item", throwable)));
    }

    private void addNewProduct(String name, String priceOrError) {
        mDisposable.add(viewModel.addNewItem(name, priceOrError)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() ->{
                            viewModel.setSubjectNewProductSnackbar(true);
                            Navigation.findNavController(requireParentFragment().requireView()).popBackStack();
                        },
                        throwable -> Log.e(TAG, "unable to add new item", throwable)));
    }
}
