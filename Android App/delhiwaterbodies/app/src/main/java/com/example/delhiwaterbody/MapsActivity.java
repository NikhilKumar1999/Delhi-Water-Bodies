package com.example.delhiwaterbody;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener,GoogleMap.OnMarkerClickListener{

     static String database_path;
     String data_path;
    List<Marker> current_list;
    List<String> villages;
    private GoogleMap mMap;
    private static configuration current_config;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data_path = getDataDir().getPath();
        database_path = data_path+"/database/delhi_waterbody.db";
        setContentView(R.layout.activity_maps);
        int errors = checklist();
        if(errors >0)
            rectify(errors);
        AutoCompleteTextView Find_village = (AutoCompleteTextView) findViewById(R.id.village_finder);
        load_village(Find_village);
        Find_village.setOnItemClickListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void load_village(AutoCompleteTextView village_list) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(database_path,null,SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.rawQuery("Select Village from locations group by Village",null);
        cursor.moveToNext();
        int size = cursor.getCount();
        ArrayAdapter<String> districts = new ArrayAdapter<>(this,R.layout.spinner_style);
        districts.setDropDownViewResource(R.layout.spinner_style);
        for(int i=0;i<size;i++)
        {
            districts.add(cursor.getString(0));
            cursor.moveToNext();
        }

        village_list.setAdapter(districts);
    }

    int checklist()
    {
        byte result = 0;
        //check if database exist
        boolean db = false;
        File aux_db = new File(data_path+"/database");
        if(aux_db.isDirectory())
        {
            File database_file = new File(database_path);
            if ((database_file.exists())) {
                try {
                    SQLiteDatabase database = SQLiteDatabase.openDatabase(database_file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
                    Cursor check_cursor = database.rawQuery("select name from sqlite_master", null);
                    if (check_cursor.getCount() > 0) {
                        check_cursor.moveToNext();
                        if (check_cursor.getString(0) == "locations") ;
                        db = true;
                    }
                    database.close();
                } catch (Exception e) {
                    db = false;
                }
            }
        }
        if(!db)
            result |=64;
       //return value
        return result;
    }

    void rectify(int errors)
    {
        //database does not exist
        if((errors&64)>=64) {
            (new File(getDataDir() + "/database")).mkdir();
            copy_file(getResources().openRawResource(R.raw.delhi_waterbody), new File(getDataDir() + "/database/delhi_waterbody.db"));
            errors &= 63;
        }
    }
    void copy_file(InputStream incomming,File to)
    {
        try {
            to.createNewFile();
            FileOutputStream outgoing = new FileOutputStream(to);
            byte incomming_buffer[] = new byte[200];
            int length = 200;
            while ((length=incomming.read(incomming_buffer,0,length))>0)
            {
                outgoing.write(incomming_buffer,0,length);
            }
            length = 0;
        }
        catch (IOException e)
        {
            Log.d("IOException","Unable to make file");
            Toast.makeText(this,"Unable to make file", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(28.7041,77.1025)));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(8));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mMap == null)
            return;
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(database_path,null,SQLiteDatabase.OPEN_READONLY);

        Cursor water_body_list = sqLiteDatabase.rawQuery("Select \"Lat \",Long,SNo from locations where Village like \'" + parent.getItemAtPosition(position)+"\'",null);

        if(current_list!=null)
            remove(current_list);
        current_list = new ArrayList<Marker>();
        int size = water_body_list.getCount();
        water_body_list.moveToNext();
        for(int i = 0;i<size;i++)
        {
            current_list.add(mMap.addMarker(new MarkerOptions().position(new LatLng(Float.parseFloat(water_body_list.getString(0)),Float.parseFloat(water_body_list.getString(1)))).visible(true)));
            current_list.get(i).setTag(water_body_list.getInt(2));

            water_body_list.moveToNext();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current_list.get(current_list.size()-1).getPosition()));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }

    void remove(List<Marker> list)
    {

        int size = list.size();
        for(int i = 0;i<size;i++)
        {
            list.get(i).remove();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        final Intent to_next = new Intent(this,Report_Activity.class);
        to_next.putExtra("SNo",(int)marker.getTag());
        AlertDialog.Builder alert  = new AlertDialog.Builder(this);
        alert.setMessage("Do you want to report current status of the given waterbody?");
        alert.setTitle("Report Current Status");
        alert.setCancelable(false);
        alert.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE)
                    startActivity(to_next);
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }).show();

        return false;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

}