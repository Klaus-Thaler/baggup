package de.thaler.baggup.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "myLOG ViewModel";
    private static MutableLiveData<String> center1;
    public static MutableLiveData<String> IPViewNumber;
    public static MutableLiveData<String> IPPort;
    public HomeViewModel() {
        IPViewNumber =  new MutableLiveData<>();
        IPPort =        new MutableLiveData<>();
        center1 =       new MutableLiveData<>();
    }
    public LiveData<String> getIPViewNumber()   { return IPViewNumber; }
    public LiveData<String> getIPPort()         { return IPPort; }
    public LiveData<String> getTextCenter1()    { return center1; }
}