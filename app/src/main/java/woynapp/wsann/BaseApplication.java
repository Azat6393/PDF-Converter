package woynapp.wsann;

import android.app.Application;

import androidx.multidex.MultiDex;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
    }
}
