package sokol.sokolandroid.activities;

/**
 * Created by yeisondavid on 01/07/2016.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sokol.sokolandroid.R;
import sokol.sokolandroid.util.Util;

/**
 * Created by yeisondavid on 17/06/2016.
 */
public class SignUp extends Activity{

    static final int IMAGE_CAPTURE = 1;
    static final int IMAGE_GALERY = 2;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;

    private EditText name;
    private EditText email;
    private EditText password1;
    private EditText password2;
    private Button btSignUp;
    private Button btSelectPhoto;
    private ImageView photo;

    @Override
    public void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        setContentView(R.layout.sign_up);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        getGraphicElements();
        configureImageView();
        setListener();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            if ( requestCode == IMAGE_CAPTURE )
            {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                photo.setImageBitmap(imageBitmap);
            }
            else if ( requestCode == IMAGE_GALERY )
            {
                Bitmap bm=null;
                try {
                    bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                photo.setImageBitmap(bm);
            }
        }
    }

    private void configureImageView()
    {
        photo.setDrawingCacheEnabled(true);
        photo.buildDrawingCache();
    }

    private void getGraphicElements()
    {
        photo = (ImageView)findViewById(R.id.signUpPhoto);
        btSelectPhoto = (Button)findViewById(R.id.signUpBtSelectPhoto);
        name = (EditText)findViewById(R.id.signUpUserName);
        email = (EditText)findViewById(R.id.signUpUserEmail);
        password1 = (EditText)findViewById(R.id.signUpPassword1);
        password2 = (EditText)findViewById(R.id.signUpPassword2);
        btSignUp = (Button)findViewById(R.id.signUpButton);

    }

    private void setListener()
    {
        btSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

    }
    private void signUp()
    {
        if ( password1.getText().toString().equals(password2.getText().toString()))
        {
            if ( Util.isEmail(email.getText().toString())) {
                if ( email.getText().toString().length() >= 6 )
                {
                    createUser();
                }
                else
                {
                   //showMessage();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "The email isn't valid", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "The passwords don't match", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMessage(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void selectImage()
    {
        final CharSequence[] items = {"Take a Photo", "Photo of Galery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ( items[which].equals("Take a Photo") ) {
                    dispatchTakePictureIntent();
                }
                else if (items[which].equals("Photo of Galery"))
                {
                    galleryIntent();
                }
                else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), IMAGE_GALERY );
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE);
        }
    }

    private void createUser()
    {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password1.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("name", name.getText().toString());
                    map.put("email", email.getText().toString());
                    map.put("profileImage", Util.bitMaptoBase64(photo.getDrawingCache()));
                    map.put("provider", "sokol");
                    myRef.child("users").child(task.getResult().getUser().getUid()).setValue(map);
                    //UserState.setUser(FirebaseAuth.getInstance().getCurrentUser()); save the user!
                    enterToApp();
                }
            }
        });

    }

    private void enterToApp()
    {
        Intent intentEnterToApp = new Intent(SignUp.this, BaseActivity.class);
        startActivity(intentEnterToApp);
    }
}