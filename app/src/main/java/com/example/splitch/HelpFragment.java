package com.example.splitch;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.airbnb.lottie.LottieDrawable;
import com.example.splitch.databinding.FragmentHintBinding;

public class HelpFragment extends Fragment {

    private static final String TAG = HelpFragment.class.getSimpleName();

    private static final String FLAG_SHARE_ANIMATION = "share_animation";

    private float animationProgress = 0f;

    private long mLastClickTime = 0;

    private FragmentHintBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHintBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        /*Material transaction animation cause text alpha problem !*/
        if(binding.fragmentHintContent.animationEmptyState.getVisibility() == View.GONE) {
            binding.fragmentHintContent.animationEmptyState.setVisibility(View.VISIBLE);
            binding.fragmentHintContent.textEmptyState.setVisibility(View.VISIBLE);
        }

        binding.toolbarHelp.setNavigationOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });

        binding.toolbarHelp.setTitle(R.string.action_help);

        String tag = getArguments().getString("tag");
        switch (tag){
            case "SplittingFragment":
                binding.fragmentHintContent.animationEmptyState.setAnimation(R.raw.illustration_splitting);
                binding.fragmentHintContent.animationEmptyState.setRepeatCount(LottieDrawable.INFINITE);
                binding.fragmentHintContent.textEmptyState.setText(R.string.msg_splitting_hint);
                binding.fragmentHintContent.animationEmptyState.playAnimation();
                break;
            case "ResultFragment":
                if (savedInstanceState != null) {
                    animationProgress = savedInstanceState.getFloat(FLAG_SHARE_ANIMATION, 0f);
                }
                binding.fragmentHintContent.animationEmptyState.setAnimation(R.raw.illustration_share);
                binding.fragmentHintContent.animationEmptyState.setRepeatCount(0);
                binding.fragmentHintContent.animationEmptyState.setSpeed(0.75f);
                Log.i(TAG, "should play animation? " + animationProgress);
                binding.fragmentHintContent.textEmptyState.setText(R.string.msg_result_hint);
                if(animationProgress == 0f) {
                    binding.fragmentHintContent.animationEmptyState.playAnimation();
                } else if(animationProgress < 0.9933995f) {
                    binding.fragmentHintContent.animationEmptyState.setProgress(animationProgress);
                    binding.fragmentHintContent.animationEmptyState.resumeAnimation();
                }
                break;
            default:
                throw new IllegalArgumentException("unknown call source");
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        animationProgress = binding.fragmentHintContent.animationEmptyState.getProgress();
        Log.i(TAG, "onSaveInstanceState" + " animationProgress " + animationProgress);
        outState.putFloat(FLAG_SHARE_ANIMATION, animationProgress);
    }
}
