package com.example.mindflayerzzz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    EditText mResultEt;
    ImageView mPreviewIv;

    private static final int CAMERA_REQUEST_CODE=200;
    private static final int STORAGE_REQUEST_CODE=400;
    private static final int IMAGE_PICK_GALLERY_CODE=1000;
    private static final int IMAGE_PICK_CAMERA_CODE=1001;

    String cameraPermission[];
    String storagePermission[];
    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionbar=getSupportActionBar();
        mResultEt=findViewById(R.id.resultEt);
        mPreviewIv=findViewById(R.id.imageresult);
        //camera permission
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


    }
    //action bar


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflatemenu
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }
    //handle actionbar item clicks

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.addImage)
        {
            showImageImportDiaglog();

        }
        if(id==R.id.settings)
        {
            String text= mResultEt.getText().toString();
            File dataDir= Environment.getExternalStorageDirectory();
            Log.i("TAG",dataDir.getAbsolutePath());
            File myfile=new File(dataDir,"file.txt");
            try
            {
                FileOutputStream fos=new FileOutputStream(myfile);
                fos.write(text.getBytes());
                Log.d("inside",text);

            } catch (FileNotFoundException e) {
                Toast.makeText(this,"file not found",Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this,"Error with writing",Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();


        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDiaglog() {
        //items to display in showImageImportDiaglog()
        String[] items={"Camera","Gallery"};
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        //set title
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0)
                {
                    //camera option clicked
                    if(!checkCameraPermission())
                    {
                        //camera permission not allowed request it
                        requestCameraPermission();
                    }
                    else
                    {
                        //take pic
                        pickCamera();

                    }
                }
                if(which==1)
                {

                    //gallery option cliced
                    if(!checkStoragePermission())
                    {
                        //storage permission not allowed request it
                        requestStoragePermission();
                    }
                    else
                    {
                        //select image
                        pickGallery();
                    }
                }



            }
        });
        dialog.create().show();//show dialog

    }

    private void pickGallery() {
        //intent topick image fromgallery
        Intent intent=new Intent(Intent.ACTION_PICK);
        //set intent type to image
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        //intent to take image from camera ,it will also be saves in external storage for high qualitu

        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPic");//title of the picture
        values.put(MediaStore.Images.Media.DESCRIPTION,"Images to Text");//description
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        Log.d("camera","camear");
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
        Log.d("camera","camear");
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {

        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);

    }

    private boolean checkCameraPermission() {
        //in order to get high quality image we have to save image to external
        //   storage first before inserting to image view thats why storage permission is required
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1=ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }


    //handle permission result

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writestorageAccepted=grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writestorageAccepted)
                    {
                        pickCamera();
                    }
                    else
                    {
                        Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean writestorageAccepted=grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if(writestorageAccepted)
                    {
                        pickGallery();
                    }
                    else
                    {
                        Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;

        }
    }
    //handle image result


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("textmsg","enter");
        if(resultCode==RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //got image from gallery now crop it
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);//enable image guidlines
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //got image from camera  now crop it
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);//enable image guidlines


            }
        }

        //get croped image
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {

                Uri resulturi=result.getUri();//get image uri
                //set image to image view
                mPreviewIv.setImageURI(resulturi);

                //get drawable bitmap for text recognition
                BitmapDrawable bitmapdrawable=(BitmapDrawable)mPreviewIv.getDrawable();
                Bitmap bitmap=bitmapdrawable.getBitmap();
                TextRecognizer recognizer=new TextRecognizer.Builder(getApplicationContext()).build();
                if(!recognizer.isOperational())

                {
                    Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Frame frame=new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items=recognizer.detect(frame);
                    StringBuilder sb=new StringBuilder();
                    //get text from sb until there is no text
                    for(int i=0;i<items.size();i++) {
                        TextBlock myitem=items.valueAt(i);
                        sb.append(myitem.getValue());
                        sb.append("\n");
                    }
                    //set text to edittext
                    mResultEt.setText(sb.toString());
                    //    mPreviewIv.setBackgroundColor(R.color.colorPrimaryDark);
                }
            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            //if there is any error shoe it
            {
                Exception error=result.getError();
                Toast.makeText(this,"Error in cropping",Toast.LENGTH_SHORT).show();

            }
        }







    }
}

