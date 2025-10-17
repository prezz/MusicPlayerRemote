package net.prezz.mpr.ui.state;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.util.HashMap;
import java.util.Map;

public class DataState extends ViewModel {

    public static class State {
        final Map<String, Object> data = new HashMap<>();
    }

    private final MutableLiveData<State> state = new MutableLiveData<State>(new State());

    public static DataState get(ViewModelStoreOwner owner) {
        return new ViewModelProvider(owner).get(DataState.class);
    }

    public Object getData(String key, Object defaultValue) {
        return state.getValue().data.getOrDefault(key, defaultValue);
    }

    public void setData(String key, Object value) {
        state.getValue().data.put(key, value);
    }
}
