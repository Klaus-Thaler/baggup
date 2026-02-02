package de.thaler.baggup.ui.home;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "myLOG ViewModel";
    private final MutableLiveData<String> ip;
    private final MutableLiveData<String> center1;
    private final MutableLiveData<String> center2;
    private final MutableLiveData<String> center3;
    public HomeViewModel() {
        ip = new MutableLiveData<>();
        center1 = new MutableLiveData<>();
        center2 = new MutableLiveData<>();
        center3 = new MutableLiveData<>();
    }
    public void changeText1 (String text) {
        center3.setValue(text);
    }public void changeText2 (String text) {
        center3.setValue(text);
    }public void changeText3 (String text) {
        center3.setValue(text);
    }
    public void changeIP () {center1.setValue("neu");}
    public LiveData<String> getTextIP() { return ip; }
    public LiveData<String> getTextCenter1() { return center1; }
    public LiveData<String> getTextCenter2() { return center2; }
    public LiveData<String> getTextCenter3() { return center3; }

}