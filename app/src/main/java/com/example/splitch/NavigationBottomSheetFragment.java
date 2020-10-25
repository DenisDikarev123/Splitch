package com.example.splitch;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.splitch.databinding.FragmentBottomSheetNavigationBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NavigationBottomSheetFragment extends BottomSheetDialogFragment {

    private FragmentBottomSheetNavigationBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBottomSheetNavigationBinding.inflate(inflater, container, false);

        binding.navigationView.setNavigationItemSelectedListener(item -> {
            NavHostFragment navHostFragment =
                    (NavHostFragment) requireActivity().getSupportFragmentManager()
                            .findFragmentById(R.id.main_fragment_container);
            NavController navController = navHostFragment.getNavController();
            switch (item.getItemId()) {
                case R.id.menu_settings:
                    navController.navigate(R.id.action_navigationBottomSheetFragment_to_settingsFragment);
                    return true;
                case R.id.menu_send_feedback:
                    navController.navigate(R.id.action_navigationBottomSheetFragment_to_feedbackFragment);
                    return true;
                default:
                    return false;
            }
        });

        return binding.getRoot();
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
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
