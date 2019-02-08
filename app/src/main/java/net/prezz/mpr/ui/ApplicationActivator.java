package net.prezz.mpr.ui;

import android.app.Application;
import android.content.Context;

public class ApplicationActivator extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        
        ApplicationActivator.context = getApplicationContext();
    }

    public static Context getContext() {
        return ApplicationActivator.context;
    }
}
