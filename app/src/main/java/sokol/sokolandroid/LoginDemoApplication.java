package sokol.sokolandroid;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Initialize Firebase with the application context. This must happen before the client is used.
 *
 * @author mimming
 * @since 12/17/14
 */
public class LoginDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Firebase.setAndroidContext(this);
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);
    }
}
