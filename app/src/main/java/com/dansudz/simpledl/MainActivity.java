package com.dansudz.simpledl;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
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
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.Charset;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;
import static java.lang.System.in;
import static java.security.AccessController.getContext;


public class MainActivity extends AppCompatActivity {
    public String DOWNLOAD_LOCATION = "/sdcard/Download";
    public String LAST_LINE;
    public int DOWNLOAD_LOCATION_REQUEST_CODE = 20;
    public int IS_DOWNLOADER_RUNNING = 0;
    public String user_input;
    public String user_input_for_customexecution;
    public static final String CHANNEL_1_ID = "channel1";
    private int STORAGE_PERMISSION_CODE = 1;
    protected Python py;
    public AsyncTask task;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DOWNLOAD_LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Uri uri = data.getData();
                String path = FileUtil.getFullPathFromTreeUri(uri, this);

                System.out.println(path);
                DOWNLOAD_LOCATION = path;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); //makes keyboard not mess up UI

        createNotificationChannels(); //Sets notification channel for pushing download progress
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        final TextView textViewToChange = (TextView) findViewById(R.id.logtobedisplayed);
        textViewToChange.setText(
                "Full error log can be viewed in the downloads folder");

        final Handler handler = new Handler(); //updates the console window
        handler.post(new Runnable() {
            @Override
            public void run() {
                final TextView console_text_window = (TextView) findViewById(R.id.actualllog);
                console_text_window.setMovementMethod(new ScrollingMovementMethod());
                File log_file = new File("/storage/emulated/0/Download/logger.txt");
                if (IS_DOWNLOADER_RUNNING == 1) {
                    String LAST_LINE_2 = tail2(log_file, 1);
                    if (LAST_LINE != LAST_LINE_2 && LAST_LINE_2 != "" && LAST_LINE_2 != "\n") {
                        if ( LAST_LINE_2.contains(LAST_LINE) || LAST_LINE.contains(LAST_LINE_2)) {
                        }
                        else {
                            console_text_window.append(LAST_LINE_2);
                            sendonChannel(LAST_LINE_2);


                            if (LAST_LINE_2.contains("\n")) {} else {
                                console_text_window.append("\n");
                            }
                            System.out.println(LAST_LINE_2);
                        }
                    }
                    LAST_LINE = tail2(log_file, 1);
                }
                handler.postDelayed(this, 200); // set time here to refresh textView)
            }
        });

        Button update_app = findViewById(R.id.update_app);
        update_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        Button Run_with_arguments = findViewById(R.id.cli_arguments);
        Run_with_arguments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText cli_text_command = new EditText(MainActivity.this);
                cli_text_command.setHint("-v -f best https://www.examplelink.com");
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Download Arguments")
                        .setMessage("Enter download arguments just as you would on a command line, including the link.")
                        .setView(cli_text_command)
                        .setPositiveButton("Execute", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //String user_input_for_customexecution = cli_text_command.getText().toString();
                                //System.out.println(user_input_for_customexecution);

                                    //.task = new Custom_Python_downloader().execute();
                                //task1 = new Python_Downloader().execute();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();

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

                ProcessPhoenix.triggerRebirth(MainActivity.this); //kill's app and reloads it, canceling all downloads

            }
        });

        Button video_download_button = findViewById(R.id.download_video_button);
        video_download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("test");
                    task = new Python_Downloader().execute();
                } else {
                    Toast storage_toast = Toast.makeText(getApplicationContext(),
                            "You need write permission for this action", Toast.LENGTH_LONG);
                    storage_toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                    storage_toast.show();
                }
            }
        });

        Button storage_request = findViewById(R.id.permission_button);
        storage_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
                } else {
                    requestStoragePermission();
                }
            }
        });


    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to store videos")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    public String tail2(File file, int lines) {
        java.io.RandomAccessFile fileHandler = null;
        try {
            fileHandler =
                    new java.io.RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            int line = 0;

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer < fileLength) {
                        line = line + 1;
                    }
                } else if (readByte == 0xD) {
                    if (filePointer < fileLength - 1) {
                        line = line + 1;
                    }
                }
                if (line >= lines) {
                    break;
                }
                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null)
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
            channel1.setVibrationPattern(new long[]{0});
            manager.createNotificationChannel(channel1);
        }
    }

    public void sendonChannel(String notificationstring) {
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
        protected void onPreExecute() {
            user_input = ((EditText) findViewById(R.id.user_url_input)).getText().toString();
        }

        protected Bitmap doInBackground(Void... params) {

            py = Python.getInstance();
            PyObject download_prog = py.getModule("download_video");

            //acquire wakelock for download
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
            wakeLock.acquire();

            IS_DOWNLOADER_RUNNING = 1; // downloader about to start, push download status
            download_prog.callAttr("download_youtube", user_input, DOWNLOAD_LOCATION); //call youtube-dl python module
            wakeLock.release();
            //realease wakelock after download has completed or has thrown an error

            //wait for ui update to catch up
            SystemClock.sleep(600);

            IS_DOWNLOADER_RUNNING = 0;
            return null;
        }
    }

    public class Custom_Python_downloader extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPreExecute() {

        }

        protected Bitmap doInBackground(Void... params) {

            py = Python.getInstance();
            PyObject download_prog_with_args = py.getModule("download_video_with_args");

            //acquire wakelock for download
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
            wakeLock.acquire();

            IS_DOWNLOADER_RUNNING = 1; // downloader about to start, push download status
            download_prog_with_args.callAttr("run_custom_arguments", user_input_for_customexecution); //call youtube-dl python module


            wakeLock.release();
            //realease wakelock after download has completed or has thrown an error

            //wait for ui update to catch up
            SystemClock.sleep(600);
            IS_DOWNLOADER_RUNNING = 0;
            return null;
        }
    }


}



