package com.example.splitch.features.adding_persons;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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
import com.example.splitch.databinding.FragmentPersonBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class PersonsBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = PersonsBottomSheetFragment.class.getSimpleName();

    private FragmentPersonBottomSheetBinding binding;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    private AddingPersonsViewModel viewModel;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            viewModel.updatePersonName(binding.editTextPersonsName.getText().toString());
            Log.i(TAG, "onTextChanged");
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPersonBottomSheetBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Dialog dialog = getDialog();
            Window window = Objects.requireNonNull(dialog).getWindow();
            Objects.requireNonNull(window).setNavigationBarColor(Color.BLACK);
        }

        ViewModelsFactory factory = Injection.provideViewModelFactory(this, getContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(AddingPersonsViewModel.class);

        initPerson();

        Log.i(TAG, "on create view");

        binding.buttonBottomSheetPersons.setOnClickListener(v -> {
            String name = Objects.requireNonNull(binding.editTextPersonsName.getText()).toString().trim();
            int id = binding.radioGroupColors.getCheckedRadioButtonId();
            int colorId = findColorById(id);
            Log.i(TAG, name);
            boolean isVerified = verifyInputData(name);
            if(isVerified){
                if(viewModel.isAdding()) {
                    addNewPerson(name, colorId);
                } else {
                    updatePerson(name, colorId);
                }
            }
        });

        binding.radioGroupColors.setOnCheckedChangeListener((group, checkedId) -> {
            int color = findColorById(checkedId);
            viewModel.updatePersonColor(color);
        });

        binding.editTextPersonsName.addTextChangedListener(textWatcher);

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android
                    .material.R.id.design_bottom_sheet);
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
        binding.editTextPersonsName.removeTextChangedListener(textWatcher);
        binding = null;
        Log.i(TAG, "on destroy view method called");
        super.onDestroyView();
    }

    private boolean verifyInputData(String name){
        boolean checkName;

        if(TextUtils.isEmpty(name)){
            checkName = false;
            binding.inputLayoutPersonsName.setError(getString(R.string.error_empty_name));
        } else {
            checkName = true;
            binding.inputLayoutPersonsName.setError(null);
        }

        return checkName;
    }

    private int findColorById(int id) {
        switch (id){
            case R.id.radio_button_indigo:
                return R.color.material_indigo;
            case R.id.radio_button_light_blue:
                return R.color.material_light_blue;
            case R.id.radio_button_red:
                return R.color.material_red;
            case R.id.radio_button_pink:
                return R.color.material_pink;
            case R.id.radio_button_purple:
                return R.color.material_purple;
            default:
                throw new IllegalArgumentException("unknown radio button id was passed.");
        }
    }

    private void setRadioButtonChecked(int colorId){
        switch (colorId){
            case R.color.material_indigo:
                binding.radioButtonIndigo.setChecked(true);
                break;
            case R.color.material_light_blue:
                binding.radioButtonLightBlue.setChecked(true);
                break;
            case R.color.material_red:
                binding.radioButtonRed.setChecked(true);
                break;
            case R.color.material_pink:
                binding.radioButtonPink.setChecked(true);
                break;
            case R.color.material_purple:
                binding.radioButtonPurple.setChecked(true);
                break;
            default:
                throw new IllegalArgumentException("unknown color id was passed.");
        }
    }

    private void addNewPerson(String name, int colorId){
        mDisposable.add(viewModel.addNewPerson(name, colorId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() ->{
                                viewModel.setSubjectNewPersonSnackbar(true);
                                viewModel.setLastPersonColor(colorId);
                                Navigation.findNavController(requireParentFragment().requireView())
                                        .popBackStack();
                        },
                        throwable -> Log.e(TAG, "unable to add new person", throwable)));
    }

    private void updatePerson(String name, int colorId){
        mDisposable.add(viewModel.updatePerson(name, colorId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            viewModel.setSubjectUpdatePersonSnackbar(true);
                            Navigation.findNavController(requireParentFragment().requireView())
                                    .popBackStack();
                        },
                        throwable -> Log.e(TAG, "unable to update person", throwable)));
    }

    private void initPerson(){
        mDisposable.add(viewModel.getObservablePerson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(person -> {
                    binding.editTextPersonsName.setText(person.name);
                    setRadioButtonChecked(person.colorId);
                    if(viewModel.isAdding()){
                        binding.buttonBottomSheetPersons.setText(getString(R.string.action_add));
                    } else {
                        binding.buttonBottomSheetPersons.setText(getString(R.string.action_change));
                    }
                    Log.i(TAG, person.name);
                }, throwable -> {
                    //nullPointerException passed here, when dialog opens for adding product
                    binding.editTextPersonsName.setText("");
                    setRadioButtonChecked(viewModel.getLastPersonColor());
                    binding.buttonBottomSheetPersons.setText(getString(R.string.action_add));
                    Log.i(TAG, "product is null");
                }));
    }
}
