package de.thaler.baggup.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import de.thaler.baggup.utils.Helper;
import de.thaler.baggup.R;
import de.thaler.baggup.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private static final String TAG = "myLOG HomeFragment";
    private FragmentHomeBinding binding;
    private Context context;
    private int port;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        SharedPreferences mPreference = context.getSharedPreferences("MyPref", 0);
        port = mPreference.getInt("port", 8001);
    }

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView IPViewName = binding.homeFragmentIpName;
        homeViewModel.getTextIP().observe(getViewLifecycleOwner(), IPViewName::setText);
        IPViewName.setText(R.string.no_wifi);
        if (Helper.isWifiReady(context)) {
            IPViewName.setText(Helper.getDefaultWifiName(context));
            //IPView.setText(Helper.getDefaultWifiName(mainActivity) + " :" + mPreference.getInt("port", 8000));
        }
        final TextView IPViewNumber = binding.homeFragmentIpNumber;
        homeViewModel.getTextIP().observe(getViewLifecycleOwner(), IPViewNumber::setText);
        IPViewNumber.setText(R.string.no_wifi);
        if (Helper.isWifiReady(context)) {
            IPViewNumber.setText(Helper.showIPAddress(context) + ":" + port);
            //IPView.setText(Helper.getDefaultWifiName(mainActivity) + " :" + mPreference.getInt("port", 8000));
        }

        final TextView center1 = binding.homeFragmentDescription1;
        homeViewModel.getTextCenter1().observe(getViewLifecycleOwner(), center1::setText);
        center1.setText(getString(R.string.home_fragment_desription1));

        return root;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}