package com.example.admin.genericimagepicker;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.admin.genericimagepicker.SDCardPicProvider.SdCardPicProvider;
import com.example.admin.genericimagepicker.camerapicchooser.CameraPicProvider;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {
    private TextView captureButton;
    private TextView camera;
    private TextView sdCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captureButton = findViewById(R.id.capture);
        camera = findViewById(R.id.camera);
        sdCard = findViewById(R.id.sdCard);
        camera.setVisibility(View.GONE);
        sdCard.setVisibility(View.GONE);
        onClickListener();
    }

    private void onClickListener() {
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.setVisibility(View.VISIBLE);
                sdCard.setVisibility(View.VISIBLE);
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCameraIconClick();
            }
        });
        sdCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleGallerySelect();
            }
        });
    }

    private void handleCameraIconClick() {
        String toastMessage = "Unable to add image";
            new CameraPicProvider(MainActivity.this, true, true, new CameraPicProvider.GetBitmapListener() {
                @Override
                public void onGetBitmap(Bitmap bitmapImage, String filepath) {

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 20, out);
                    byte imgBytes[] = out.toByteArray();
                    RequestBody requestFile =
                            RequestBody.create(MediaType.parse("image/jpeg"), imgBytes);
                    MultipartBody.Part body =
                            MultipartBody.Part.createFormData("profileImage", filepath, requestFile);
                }
            });
    }

    private void handleGallerySelect() {
            new SdCardPicProvider(MainActivity.this, true, true, new SdCardPicProvider.GetBitmapListener() {
                @Override
                public void onGetBitmap(Bitmap bitmap, String imagePath) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream);
                    byte imgBytes[] = outputStream.toByteArray();

                    RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imgBytes);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("profileImage", imagePath, requestBody);
                }
            });
    }
}
