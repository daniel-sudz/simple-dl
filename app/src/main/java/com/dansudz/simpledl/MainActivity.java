package com.dansudz.simpledl;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URI;



public class MainActivity extends AppCompatActivity {
    public String DOWNLOAD_LOCATION = "/sdcard/Download";
    public int DOWNLOAD_LOCATION_REQUEST_CODE = 20;
    public int IS_DOWNLOADER_RUNNING = 0;
    public String user_input;
    public static final String CHANNEL_1_ID = "channel1";
    private int STORAGE_PERMISSION_CODE = 1;
    protected Python py;
    public AsyncTask task;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DOWNLOAD_LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Uri uri = data.getData();
                String path = FileUtil.getFullPathFromTreeUri(uri,this);

                System.out.println(path);
                System.out.println("test");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        createNotificationChannels();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        final TextView textViewToChange = (TextView) findViewById(R.id.logtobedisplayed);
        textViewToChange.setText(
                "Full error log can be viewed in the downloads folder");

        final Handler handler=new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                final TextView textViewToChange = (TextView) findViewById(R.id.actualllog);
                File file = new File("/storage/emulated/0/Download/logger.txt");
                //System.out.println(tail2(file,5));
                //  readFromLast(file,2);
                if (IS_DOWNLOADER_RUNNING == 1) {
                    System.out.println("currently running");
                    textViewToChange.setText(
                            tail2(file,1));
                    sendonChannel(tail2(file,1));
                }
                else {
                    //System.out.println("currently not running");

                    //System.out.println(getApplicationInfo().dataDir);
                }


                handler.postDelayed(this,200); // set time here to refresh textView)
            }
        });



        Button download_location = findViewById(R.id.set_directory);
        download_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(Intent.createChooser(i, "Choose directory"), DOWNLOAD_LOCATION_REQUEST_CODE);
            }
        });

        Button video_cancel_download = findViewById(R.id.cancel_video_download);
        video_cancel_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //task.cancel(true); //kill current download
                ProcessPhoenix.triggerRebirth(MainActivity.this);

            }
        });

        Button video_download_button = findViewById(R.id.download_video_button);
        video_download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    task = new Python_Downloader().execute();
                }
                else {
                    Toast storage_toast= Toast.makeText(getApplicationContext(),
                            "You need write permission for this action", Toast.LENGTH_LONG);
                    storage_toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                    storage_toast.show();
                }
            }
        });

        Button storage_request = findViewById(R.id.permission_button);
        storage_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
                } else {
                    requestStoragePermission();
                }
            }
        });


    }

    private void requestStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this )
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to store videos")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);

                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    public String tail2( File file, int lines) {
        java.io.RandomAccessFile fileHandler = null;
        try {
            fileHandler =
                    new java.io.RandomAccessFile( file, "r" );
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            int line = 0;

            for(long filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek( filePointer );
                int readByte = fileHandler.readByte();

                if( readByte == 0xA ) {
                    if (filePointer < fileLength) {
                        line = line + 1;
                    }
                } else if( readByte == 0xD ) {
                    if (filePointer < fileLength-1) {
                        line = line + 1;
                    }
                }
                if (line >= lines) {
                    break;
                }
                sb.append( ( char ) readByte );
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch( java.io.FileNotFoundException e ) {
            e.printStackTrace();
            return null;
        } catch( java.io.IOException e ) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (fileHandler != null )
                try {
                    fileHandler.close();
                } catch (IOException e) {
                }
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_MIN
            );
            channel1.setDescription("Download information");
            NotificationManager manager = getSystemService(NotificationManager.class);
            channel1.enableVibration(false);
            channel1.setVibrationPattern(new long[]{ 0 });
            manager.createNotificationChannel(channel1);
        }
    }
    public void sendonChannel (String notificationstring) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_stat_download_notification)
                .setContentTitle("Download in Progress")
                .setContentText(notificationstring)
                .setOngoing(true); // Again, THIS is the important line

               // .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager nomanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nomanager.notify(1, notification.build());

    }
    public class Python_Downloader extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPreExecute(){
            user_input = ((EditText)findViewById(R.id.user_url_input)).getText().toString();
        }
        protected Bitmap doInBackground(Void... params){

            py = Python.getInstance();
            PyObject download_prog = py.getModule("download_video");

            //acquire wakelock for download
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
            wakeLock.acquire();

            IS_DOWNLOADER_RUNNING = 1; // downloader about to start, push download status
            download_prog.callAttr("download_youtube", user_input); //call youtube-dl python module

            wakeLock.release();
            //realease wakelock after download has completed or has thrown an error

            //wait for ui update to catch up
            SystemClock.sleep(600);

            IS_DOWNLOADER_RUNNING = 0;
            return null;
        }
    }


}



