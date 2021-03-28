package com.carebase.scannerexploration;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendCellsRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PictureAnalysisActivity extends AppCompatActivity {
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;

    private Button analyzeButton;
    private ImageView ivPreview;

    protected final static int RC_SIGN_IN = RESULT_FIRST_USER;
    private static final String SPREADSHEET_ID = "1p-HSCuDvjvlGrrCCOTpgGBTUNIOeKqQvm9_mCHg4tTk";
    private Executor executor;
    private static final Scope SHEET_SCOPE = new Scope(SheetsScopes.SPREADSHEETS);
    protected GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount account;
    private Sheets service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_analysis);
        findViewById(R.id.take_picture_button).setOnClickListener(this::onLaunchCamera);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener((view) -> finish());
        analyzeButton = findViewById(R.id.analyze_button);
        analyzeButton.setOnClickListener(this::onLaunchAnalyzer);
        ivPreview = findViewById(R.id.image_view_preview);

        executor = Executors.newSingleThreadExecutor();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(SHEET_SCOPE)
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        account = GoogleSignIn.getLastSignedInAccount(this);

        if (account == null) {
            Fragment fragment = new SignInFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.constraint_layout,fragment).addToBackStack(null).commit();
        } else {
            // create Sheets service
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(SheetsScopes.SPREADSHEETS));
            credential.setSelectedAccount(account.getAccount());
            service = new Sheets.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new JacksonFactory(),
                    credential)
                    .build();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ImageView ivPreview = (ImageView) findViewById(R.id.image_view_preview);
                // by this point we have the camera photo on disk
                Bitmap takenImage = decodeSampledBitmapFromFiles(photoFile,ivPreview.getWidth(),ivPreview.getHeight());
                ivPreview.setImageBitmap(takenImage);
                analyzeButton.setEnabled(true);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == RC_SIGN_IN){
            getSupportFragmentManager().popBackStack();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    protected void onLaunchSignIn(View v) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Toast.makeText(this, account.getEmail() + " Signed In", Toast.LENGTH_SHORT).show();

            // create Sheets service
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(SheetsScopes.SPREADSHEETS));
            credential.setSelectedAccount(account.getAccount());
            service = new Sheets.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new JacksonFactory(),
                    credential)
                    .build();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(PictureAnalysisActivity.class.getSimpleName(), "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(PictureAnalysisActivity.this, "com.carebase.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    public void onLaunchAnalyzer(View view) {
        // create AnalyzerFragment
        Fragment analyzerFragment = new ResultFragment();
        Bundle bundle = new Bundle();
        bundle.putString("file_name",photoFileName);
        analyzerFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.constraint_layout,analyzerFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), PictureAnalysisActivity.class.getSimpleName());

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(PictureAnalysisActivity.class.getSimpleName(), "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    public static int getRotationAngle(String photoFilePath) {
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        return rotationAngle;
    }

    public static Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);

        int rotationAngle = getRotationAngle(photoFilePath);

        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }

    public static Bitmap decodeSampledBitmapFromFiles(File photoFile, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return rotateBitmapOrientation(photoFile.getAbsolutePath());
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onSaveAnnotations(List<Pair<String,Boolean>> data) {
        // put in thread executor
        List<RowData> rowData = data.stream().map( d -> new RowData().setValues(
                Arrays.asList(
                        new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(d.first)),
                        new CellData().setUserEnteredValue(new ExtendedValue().setBoolValue(d.second))
                        ))).collect(Collectors.toList());
        AppendCellsRequest cellsRequest = new AppendCellsRequest().setSheetId(0).setRows(rowData).setFields("*");
        Request request = new Request().setAppendCells(cellsRequest);
        BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest().setRequests(Collections.singletonList(request));

        executor.execute(() -> {
            try {
                service.spreadsheets().batchUpdate(SPREADSHEET_ID, batchRequest).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        clearImage();
        Toast.makeText(this,"Saved Annotations",Toast.LENGTH_SHORT).show();
        getSupportFragmentManager().popBackStack();
    }

    public void clearImage() {
        ivPreview.setImageResource(0);
        analyzeButton.setEnabled(false);
    }

}