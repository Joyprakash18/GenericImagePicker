package com.example.admin.genericimagepicker.SDCardPicProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.admin.genericimagepicker.R;
import com.example.admin.genericimagepicker.filechooser.FileUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

public class SdCardPicProvider {
    private static final String TAG = "SD Card pic provider";
    private static boolean isOval;
    private Context context;
    private static GetBitmapListener listener;
    private static GetVideofileListener fileListener;
    private static GetAnyFileListener anyFileListener;
    private static boolean wantToCrop;
    private static String displayName;

    /**
     * <p>
     * Add the following dependency in app level build gradle
     * </p>
     * {@code
     * implementation 'com.theartofdev.edmodo:android-image-cropper:2.7.+'
     * }
     * <p>
     * <p>
     * Add the following line of code in the manifest under the application tag
     * </p>
     * {@code
     * <activity
     * <activity android:name=".sdcardpichooser.SdCardPicProvider$ResultReciverActivity"
     * android:theme="@style/Theme.AppCompat.Translucent"
     * />
     * }
     * <p>
     * {@code
     * <activity
     * android:name="com.theartofdev.edmodo.cropper.CropImageActivity"
     * android:theme="@style/crop_imge_theme" />
     * }
     * </p>
     * <p>
     * {@code
     * <provider
     * android:name="android.support.v4.content.FileProvider"
     * android:authorities="com.encoders.eva.eva.provider"
     * android:exported="false"
     * android:grantUriPermissions="true">
     * <meta-data
     * android:name="android.support.FILE_PROVIDER_PATHS"
     * android:resource="@xml/provider_paths" />
     * </provider>
     * }
     * </p>
     * <p>
     * add a xml file named provider_path under xml folder in res folder,if not existed create one
     * </p>
     * <p>
     * {@code
     * <paths xmlns:android="http://schemas.android.com/apk/res/android">
     * <external-path name="external_files" path="."/>
     * </paths>
     * }
     * </p>
     * <p>
     * Add the following line of code in the style.xml
     * </p>
     * <p>
     * {@code
     * <style name="Theme.AppCompat.Translucent" parent="Theme.AppCompat.NoActionBar">
     * <item name="android:background">#33000000</item> <!-- Or any transparency or color you need -->
     * <item name="android:windowNoTitle">true</item>
     * <item name="android:windowBackground">@android:color/transparent</item>
     * <item name="android:colorBackgroundCacheHint">@null</item>
     * <item name="android:windowIsTranslucent">true</item>
     * <item name="android:windowAnimationStyle">@android:style/Animation</item>
     * </style>
     * }
     * </p>
     *
     * @param context    activity context
     *                   </p><p>
     * @param wantToCrop if true,This provider class open an image cropper activity screen
     *                   where you cancrop the picked image.
     *                   </p><p>
     * @param listener   listener for getting the bitmap image and the file path
     *                   </p>
     */

    public SdCardPicProvider(Context context, boolean wantToCrop, boolean isOval, GetBitmapListener listener) {
        this.context = context;
        SdCardPicProvider.isOval = isOval;
        SdCardPicProvider.listener = listener;
        SdCardPicProvider.wantToCrop = wantToCrop;
        openPermissionAndResultReciver("image");
    }

    public SdCardPicProvider(Context context, GetVideofileListener listener) {
        this.context = context;
        fileListener = listener;
        openPermissionAndResultReciver("video");
    }

    public SdCardPicProvider(Context context, GetAnyFileListener listener) {
        this.context = context;
        anyFileListener = listener;
        openPermissionAndResultReciver("any");
    }

    private void openPermissionAndResultReciver(String type) {
        Intent intent = new Intent(context, ResultReciverActivity.class);
        intent.putExtra("fileType", type);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static class ResultReciverActivity extends Activity {

        public static final int SELECT_IMAGE = 0;
        public static final int SELECT_VIDEO = 1;
        public static final int SELECT_ANY = 2;
        public static final int REQUEST_FOR_READ_EXTERNAL_STORAGE=0;
        private Bitmap bitmapImage;
        private String isVideo;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                isVideo = getIntent().getStringExtra("fileType");
                requestPermissionForReadExternalStorage();
            } else {

            }

        }

        private void proceedAfterPermission() {
            switch (isVideo) {
                case "image":
                    showImageFileChooser();
                    break;
                case "video":
                    showVideoFileChooser();
                    break;
                case "any":
                    showAnyFileChooser();
                    break;
                default:
                    break;

            }
        }

        private boolean requestPermissionForReadExternalStorage() {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
// explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
//Toast.makeText(getApplicationContext(), "External storage permission is mandatory",Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_FOR_READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_FOR_READ_EXTERNAL_STORAGE);
                }
                return true;
            } else {
                proceedAfterPermission();
                return false;
            }
        }

        private void showAnyFileChooser() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, SELECT_ANY);
        }

        private void showVideoFileChooser() {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(galleryIntent, "Select video"), SELECT_VIDEO);

        }

        private void showImageFileChooser() {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
            switch (requestCode) {
                case REQUEST_FOR_READ_EXTERNAL_STORAGE: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted
//                    Toast.makeText(getApplicationContext(), "SMS Permission granted", Toast.LENGTH_LONG).show();
                        proceedAfterPermission();
                    } else {
//                    Toast.makeText(getApplicationContext(), "",Toast.LENGTH_LONG).show();
                        Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

            }
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            if (outState != null) {
//                outState.putString("file", file.toString());
            }
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, final Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SELECT_IMAGE) {
                    onSelectFromGalleryResult(data);
                } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        getImageFromCropActivity(result);
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                        error.printStackTrace();
                        finish();
                    }
                } else if (requestCode == SELECT_VIDEO) {
                    onVideoSelectFromGalleryResult(data);
                } else if (requestCode == SELECT_ANY) {
                    onAnyFileSelectFromGalleryResult(data);

                }

            } else if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(this, "selection cancel", Toast.LENGTH_SHORT).show();
                finish();
            }


        }

        private void onAnyFileSelectFromGalleryResult(Intent data) {
            if (data != null) {
                Uri selectFile = data.getData();
                dumpImageMetaData(selectFile);

                if (selectFile != null) {
                    File file = FileUtils.getFile(this, selectFile);
                    anyFileListener.onGetFile(file);
                }

            } else {

            }
            finish();


        }

        private void onVideoSelectFromGalleryResult(Intent data) {
           /* Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String decodableString = cursor.getString(columnIndex);
            cursor.close();
            File file = new File(decodableString);
            if (file != null) {
                fileListener.onGetFile(file);
                finish();
            } else {
                finish();
            }*/

            if (data != null) {
                File file;
                Uri selectFile = data.getData();
                dumpImageMetaData(selectFile);
                if (selectFile != null) {
                    file = FileUtils.getFile(this, selectFile);
                    if (file == null) {
                        file = FileUtils.getFile(this, selectFile);
                    }
                    fileListener.onGetFile(file);
                } else {
                    Log.e("onVideoSelect ","selected file not found" );
                }
            }
            finish();

        }

        private void getImageFromCropActivity(CropImage.ActivityResult result) {
            if (result != null) {
                try {
                    Uri selectedImage = result.getUri();
                    bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    listener.onGetBitmap(bitmapImage, displayName);
                    this.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    this.finish();
                }
            } else {
                finish();
            }

        }

        private void onSelectFromGalleryResult(Intent data) {
            if (data != null) {
                try {
                    Uri selectedImage = data.getData();
                    dumpImageMetaData(selectedImage);
                    bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    if (!wantToCrop) {
                        listener.onGetBitmap(bitmapImage, displayName);
                        finish();
                    } else {
                        showImageCroperActivity(data, isOval);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    finish();
                }
            }
        }

        private void showImageCroperActivity(Intent data, boolean isOval) {

            if (data != null) {
                try {
                    Uri selectedImage = data.getData();
                    bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    CropImage.activity(selectedImage)
                            .setCropShape(isOval ? CropImageView.CropShape.OVAL : CropImageView.CropShape.RECTANGLE)
                            .setActivityMenuIconColor(getResources().getColor(R.color.colorWhite))
                            .setBorderCornerColor(getResources().getColor(R.color.colorAccent))
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void dumpImageMetaData(Uri uri) {

            // The query, since it only applies to a single document, will only return
            // one row. There's no need to filter, sort, or select fields, since we want
            // all fields for one document.
            Cursor cursor = this.getContentResolver()
                    .query(uri, null, null, null, null, null);

            try {
                // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
                // "if there's anything to look at, look at it" conditionals.
                if (cursor != null && cursor.moveToFirst()) {

                    // Note it's called "Display Name".  This is
                    // provider-specific, and might not necessarily be the file name.
                    displayName = cursor.getString(
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    Log.i(TAG, "Display Name: " + displayName);

                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    // If the size is unknown, the value stored is null.  But since an
                    // int can't be null in Java, the behavior is implementation-specific,
                    // which is just a fancy term for "unpredictable".  So as
                    // a rule, check if it's null before assigning to an int.  This will
                    // happen often:  The storage API allows for remote files, whose
                    // size might not be locally known.
                    String size = null;
                    if (!cursor.isNull(sizeIndex)) {
                        // Technically the column stores an int, but cursor.getString()
                        // will do the conversion automatically.
                        size = cursor.getString(sizeIndex);
                    } else {
                        size = "Unknown";
                    }
                    Log.i(TAG, "Size: " + size);
                }
            } finally {
                cursor.close();
            }
        }
    }

    public interface GetBitmapListener {
        void onGetBitmap(Bitmap bitmap, String imagePath);
    }

    public interface GetVideofileListener {
        void onGetFile(File file);
    }

    public interface GetAnyFileListener {
        void onGetFile(File file);
    }
}
