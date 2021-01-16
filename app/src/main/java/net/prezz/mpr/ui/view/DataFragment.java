package net.prezz.mpr.ui.view;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.HashMap;
import java.util.Map;


public class DataFragment extends Fragment {

    private Map<String, Object> data = null;

    public static DataFragment getRestoreFragment(Activity activity, Class<?> clazz) {
        FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
        DataFragment dataFragment = (DataFragment) fm.findFragmentByTag(getTag(clazz));
        if (dataFragment == null) {
            dataFragment = new DataFragment();
            fm.beginTransaction().add(dataFragment, getTag(clazz)).commit();
        }

        return (dataFragment.data != null) ? dataFragment : null;
    }

    public static DataFragment getSaveFragment(Activity activity, Class<?> clazz) {
        FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
        DataFragment dataFragment = (DataFragment) fm.findFragmentByTag(getTag(clazz));

        return dataFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setData(String key, Object value) {
        if (data == null) {
            data = new HashMap<String, Object>();
        }

        data.put(key, value);
    }

    public Object getData(String key, Object defaultValue) {
        if (data != null) {
            Object value = data.get(key);
            if (value != null) {
                return value;
            }
        }

        return defaultValue;
    }

    private static String getTag(Class<?> clazz) {
        return clazz.getName() + ".data";
    }
}