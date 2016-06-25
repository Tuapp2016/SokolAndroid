package sokol.sokolandroid.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;

public class Util {

    public static final String TAG = Util.class.getName();

    public static boolean validateEmail(String email){
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean validatePassword(String password){
        return !TextUtils.isEmpty(password);
    }


    public static String getProvider(FirebaseUser user){
        String provider = user.getProviderData().get(1).getProviderId();
        return provider;
    }


    public static class ImageFromURLTask extends AsyncTask<Void, Void, Bitmap>{

        private String URL;

        public ImageFromURLTask(String URL) {
            this.URL = URL;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            InputStream inputStream = null;
            Bitmap bitmap = null;
            try {
                inputStream = new java.net.URL(URL).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }catch (IOException e){
                Log.e(TAG, "Incorrect URL profile image "+ URL);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
        }
    }
}
