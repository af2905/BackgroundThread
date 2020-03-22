package ru.job4j.backgroundthread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "log";
    private volatile boolean stopThread;
    TextView text;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = findViewById(R.id.text);
        image = findViewById(R.id.image);
        /*LoadImageFromNetwork load = new LoadImageFromNetwork();
        new Thread(load).start();*/
        LoadImageAsyncTask asyncTask = new LoadImageAsyncTask(this);
        asyncTask.execute("https://secure-static.schutz.com.br/medias/sys_master/images/h65/he7/h00/h00/9140293435422/Header-Sale.jpg");

    }

    public void startThread(View view) {
        TestRunnable runnable = new TestRunnable(10);
        new Thread(runnable).start();
    }

    public void stopThread(View view) {
        if (!stopThread) {
            stopThread = true;
        }
        Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
    }

    class TestRunnable implements Runnable {
        private int times;

        TestRunnable(int times) {
            this.times = times;
        }

        @Override
        public void run() {
            int count = 0;
            while (count != times) {
                if (count < 3) {
                    runOnUiThread(() -> text.setText("We consider your final price ... "));
                }
                if (count == 5) {
                    runOnUiThread(() -> {
                        int sale = (int) (Math.random() * 100);
                        text.setText("final PRICE for You " + sale + "% off");
                    });
                }
                Log.d(TAG, "startThread: " + count);
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (stopThread) {
                    stopThread = false;
                    break;
                }
            }
        }
    }

    private static Bitmap loadImageFromNetwork(String url) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static class LoadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private WeakReference<MainActivity> activityWeakReference;

        LoadImageAsyncTask(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            return loadImageFromNetwork(strings[0]);
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.image.setImageBitmap(bitmap);
        }
    }

     /* class LoadImageFromNetwork implements Runnable {
        @Override
        public void run() {
            final Bitmap bitmap = loadImageFromNetwork(
                    "https://secure-static.schutz.com.br/medias/sys_master/images/h65/he7/h00/h00/9140293435422/Header-Sale.jpg");
            runOnUiThread(() -> image.setImageBitmap(bitmap));
        }
    }*/
}

