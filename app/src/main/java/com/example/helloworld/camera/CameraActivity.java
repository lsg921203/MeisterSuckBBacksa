package com.example.helloworld.camera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.helloworld.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {
    ImageView imageView = null;
    TextView myPosition = null;

    String mCurrentPhotoPath;

    LocationManager lm = null;
    Location myLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("Permission", "권한 설정 완료");
                lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else {
                Log.d("Permission", "권한 설정 요청");
                String[] permissionArr = {
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };
                ActivityCompat.requestPermissions(CameraActivity.this, permissionArr, 1);
            }
        }

        if( myLocation != null){
            Log.d("Location" , " " +myLocation.getLongitude());
        }else{
            Log.d("Location" , "Can't find my position");
        }
        imageView = (ImageView) findViewById(R.id.imageView);
        myPosition = (TextView) findViewById(R.id.positionText);

    }

    static public String getAddress(Context mContext, double lat, double lng) {
        String nowAddress = "현 위치를 찾고 있습니다";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
            //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
            address = geocoder.getFromLocation(lat, lng, 1);

            if (address != null && address.size() > 0) {
                // 주소 받아오기
                nowAddress = address.get(0).getAddressLine(0);

            }

        } catch (IOException e) {
            //Toast.makeText(MainActivity.this, "잘못된 포인트 설정입니다.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return nowAddress;
    }

    public void onClickCameraBtn(View view) {
        dispatchTakePictureIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            )
            {
                Log.d("Permission", "권한 설정 완료");
            } else {
                Log.d("Permission", "권한 설정 요청");
                String[] permissionArr = {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };
                ActivityCompat.requestPermissions(CameraActivity.this, permissionArr, 1);
            }
        }
        myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if( myLocation != null){
            String locationStr = getAddress(CameraActivity.this.getBaseContext() , myLocation.getLatitude() , myLocation.getLongitude());
            Log.d("Location", locationStr);
            myPosition.setText( locationStr );
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == RESULT_OK){
            File file = new File(mCurrentPhotoPath);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver() , Uri.fromFile(file));
                if( bitmap != null ){
                    ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    Bitmap rotateBitMap = null;
                    showExif(ei);
                    switch (orientation){
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotateBitMap = rotateImage(bitmap , 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotateBitMap = rotateImage(bitmap , 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotateBitMap = rotateImage(bitmap , 270);
                            break;
                        default:
                            rotateBitMap = bitmap;
                    }
                    imageView.setImageBitmap(rotateBitMap);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //이미지 파일을 저장하는 Method
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile( imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private Bitmap rotateImage(Bitmap source , float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source ,0 ,0 ,source.getWidth(), source.getHeight(), matrix ,true);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.helloworld.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    private void showExif(ExifInterface exif) {

        String myAttribute = "[Exif information] \n\n";

        myAttribute += getTagString(ExifInterface.TAG_DATETIME, exif);
        myAttribute += getTagString(ExifInterface.TAG_FLASH, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_MAKE, exif);
        myAttribute += getTagString(ExifInterface.TAG_MODEL, exif);
        myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif);
        myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);
        myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);

        Log.d("ExifData" , myAttribute);
    }
    private String getTagString(String tag, ExifInterface exif) {
        return (tag + " : " + exif.getAttribute(tag) + "\n");
    }
}