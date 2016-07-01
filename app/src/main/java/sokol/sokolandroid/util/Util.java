package sokol.sokolandroid.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static final String TAG = Util.class.getName();

    public static boolean isEmail(String email)
    {
        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public static String bitMaptoBase64(Bitmap bm)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString( byteArray, Base64.DEFAULT);
    }

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
        private ImageView profileImage;

        public ImageFromURLTask(String URL, ImageView profileImage) {
            this.URL = URL;
            this.profileImage = profileImage;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = null;
            if(URL.startsWith("http")) {
                InputStream inputStream = null;

                try {
                    inputStream = new java.net.URL(URL).openStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (IOException e) {
                    Log.e(TAG, "Incorrect URL profile image " + URL);
                }
            }else{
                byte[] decodedImage = Base64.decode(URL, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(decodedImage, 0 , decodedImage.length);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            profileImage.setImageBitmap(bitmap);
        }
    }
}
