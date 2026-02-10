package de.thaler.baggup.ui.home;

import static de.thaler.baggup.MainActivity.defaultPort;

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
    private String IPViewNameString;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        SharedPreferences mPreference = context.getSharedPreferences("MyPref", 0);
        port = mPreference.getInt("port", defaultPort);
    }

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView IPViewNumber = binding.homeFragmentIpNumber;
        IPViewNumber.setText(R.string.no_wifi);
        if (Helper.isWifiReady(context)) {
            IPViewNumber.setText(Helper.showIPAddress(context));
        }
        homeViewModel.getIPViewNumber().observe(getViewLifecycleOwner(), IPViewNumber::setText);

        final  TextView doublePoint =binding.homeFragmentDoublepoint;

        final TextView IPPort = binding.homeFragmentIpPort;
        if (Helper.isWifiReady(context)) {
            IPPort.setText(String.valueOf(port));
            doublePoint.setText(R.string.double_point);
        }
        homeViewModel.getIPPort().observe(getViewLifecycleOwner(), IPPort::setText);

        final TextView center1 = binding.homeFragmentDescription1;
        center1.setText(getString(R.string.home_fragment_desription1));
        homeViewModel.getTextCenter1().observe(getViewLifecycleOwner(),center1::setText);

        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}