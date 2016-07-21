package com.arcgis.androidsupportcases.backgroundsyncgeodatabase;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.Feature;
import com.esri.arcgisruntime.datasource.arcgis.Geodatabase;
import com.esri.arcgisruntime.datasource.arcgis.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  Button gdbSyncBtn;
  Button insertBtn;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    gdbSyncBtn = (Button) findViewById(R.id.gdbSyncBtn);
    gdbSyncBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Intent i = new Intent(MainActivity.this, GDBSyncService.class);
        MainActivity.this.startService(i);
      }
    });

    final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator
        + "data" + File.separator + "default.geodatabase";

    insertBtn = (Button) findViewById(R.id.addLocation);

    if (new File(PATH).exists())
    {
      insertBtn.setEnabled(true);
    }

    insertBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        final Geodatabase gdb = new Geodatabase(PATH);
        gdb.addDoneLoadingListener(new Runnable() {
          @Override public void run() {
            final GeodatabaseFeatureTable gdbFT = gdb.getGeodatabaseFeatureTable("NoheLayer2");
            Log.e("NOHE", "GDB LOADED");
            gdbFT.loadAsync();
            gdbFT.addLoadStatusChangedListener(new LoadStatusChangedListener() {
              @Override
              public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                Log.e("NOHE", loadStatusChangedEvent.getNewLoadStatus().toString());
              }
            });
            gdbFT.addDoneLoadingListener(new Runnable() {
              @Override public void run() {
                Log.e("NOHE", "TABLE LOADED");
                Feature feature = gdbFT.createFeature();
                feature.setGeometry(new Point(-8514013.932, 4811225.050));
                Log.e("NOHE", "FEATURE CREATED");
                final ListenableFuture<Boolean> x = gdbFT.addFeatureAsync(feature);
                Log.e("NOHE", "FEATURE LOADED CALLED");
                x.addDoneListener(new Runnable() {
                  @Override public void run() {
                    try {
                      Log.e("NOHE", "FEATURE LOADED");
                      Toast.makeText(getApplicationContext(), "DONE" + x.get(), Toast.LENGTH_SHORT).show();
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    } catch (ExecutionException e) {
                      e.printStackTrace();
                    }
                  }
                });

              }
            });
          }
        });

        gdb.loadAsync();


      }
    });



  }
}
