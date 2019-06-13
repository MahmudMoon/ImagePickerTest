package com.example.moon.imagepickertest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;
import in.mayanknagwanshi.imagepicker.ImageSelectActivity;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MyTag";
    File file;
    File compressor;
    Uri final_path;
    ImageView imageView;

    StorageReference mStorageRef;
    UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(getApplicationContext());
        imageView = (ImageView)findViewById(R.id.image_view);

        ((Button)findViewById(R.id.btn_pick_image)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getPermission()){
                    Log.i(TAG, "onClick: "+"YES");
                    Intent intent = new Intent(MainActivity.this, ImageSelectActivity.class);
                    intent.putExtra(ImageSelectActivity.FLAG_COMPRESS, false);//default is true
                    intent.putExtra(ImageSelectActivity.FLAG_CAMERA, true);//default is true
                    intent.putExtra(ImageSelectActivity.FLAG_GALLERY, true);//default is true
                    startActivityForResult(intent, 1213);

                }else{
                    Log.i(TAG, "onClick: "+"NOT");
                }
            }
        });
      // ;
    }

    private boolean getPermission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},101);
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "onRequestPermissionsResult: "+"PackageManager.PERMISSION_GRANTED");
        }else{
            Log.i(TAG, "onRequestPermissionsResult: "+grantResults[0]);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1213 && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(ImageSelectActivity.RESULT_FILE_PATH);
            try {
                file = new File(filePath);
            }catch (Exception e){
                Log.i(TAG, "onActivityResult: "+e.toString());
            }

            if(file.exists()){
                Log.i(TAG, "onActivityResult: "+"FILE EXISTS");

                try {
                    compressor = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(40)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .compressToFile(file);
                    final_path = Uri.fromFile(compressor);

                    if (final_path != null) {
                       // Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
                        mStorageRef = FirebaseStorage.getInstance().getReference().child("PRofile" + ".jpg");

                        uploadTask = mStorageRef.putFile(final_path);

// Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Log.i(TAG, "onFailure: "+exception.toString());
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                                Log.i(TAG, "onSuccess: "+"Successful");
                            }
                        });




//                        mStorageRef.putFile(final_path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                Log.i(TAG, "onSuccess: "+"Successfully uploaded");
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.i(TAG, "onFailure: "+e.toString());
//                            }
//                        });

//                        UploadTask uploadTask = mStorageRef.putFile(final_path);
//                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                            @Override
//                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                                if (!task.isSuccessful()) {
//                                    throw task.getException();
//                                }
//
//                                // Continue with the task to get the download URL
//                                return mStorageRef.getDownloadUrl();
//                            }
//                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Uri> task) {
//                                if (task.isSuccessful()) {
//                                    Uri downloadUri = task.getResult();
//                                    Log.i(TAG, "onComplete: " + downloadUri.toString());
//
//                                } else {
//                                    Log.i(TAG, "onComplete: " + "FAILED TO GENERATE DOWNLOAD LINK");
//                                }
//                            }
//                        });
                    }
                } catch (IOException e) {
                    Log.i(TAG, "onActivityResult: "+e.toString());
                    e.printStackTrace();
                }

                Log.i(TAG, "FINAL PATH: "+final_path);

            }
            Log.i(TAG, "onActivityResult: "+filePath);
            Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
            imageView.setImageBitmap(selectedImage);
        }
    }
}
