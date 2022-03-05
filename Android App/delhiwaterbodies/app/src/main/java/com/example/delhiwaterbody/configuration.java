package com.example.delhiwaterbody;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class configuration {
    //constants
    public final int zoom_default =  9;
    public final int zoom_village = 1;
    public final int zoom_waterbody = 1;
    public final int zoom_district = 11;
    //Selected info
    public String District;
    public String[] Village;
    public int village_index;
    public int[] water_bodyindex;

    //Map Attributes
    public int current_zoom;
    public LatLng current_Focus;

    //Constructors
    public configuration()
    {
        District = null;
        Village = null;
        water_bodyindex = null;
        village_index = 0;
        current_Focus = new LatLng(28.613893, 77.209032);
        current_zoom = zoom_default;
    }

    //functions for class
    public void write(Context context)
    {
        try {
            File config = new File(context.getDataDir() + "/files/config.config");
            if (!config.exists())
                config.createNewFile();
            FileWriter to_config =new FileWriter(config,false);
            to_config.write("true\n");
            to_config.write(District+"\n");
            to_config.write(village_index+"\n");
            to_config.write(current_Focus+"\n");
            to_config.write(current_zoom+"\n");
            to_config.flush();
            to_config.close();
        }
        catch (IOException e)
        {
            Log.d("Config","Cannot make config");
        }
    }
    public configuration(String reader_path,Context context)
    {
        this();
        try {
            FileReader reader = new FileReader(reader_path);
            String check = readline(reader);
            check.hashCode();
            if (!check.equals("true")) {
                this.write(context);
                return;
            }
            this.District = readline(reader);
            if (!this.District.equals("null")) {
                SQLiteDatabase db = SQLiteDatabase.openDatabase(com.example.delhiwaterbody.MapsActivity.database_path, null, SQLiteDatabase.OPEN_READONLY);
                Cursor result = db.rawQuery("Select Village from location where District like " + this.District + " group by Village ;", null);
                result.moveToNext();
                int size = result.getCount();
                this.Village = new String[size];
                for (int i = 0; i < size; i++) {
                    this.Village[i] = result.getString(0);
                    result.moveToNext();
                }

            } else {
                this.write(context);
            }
        }catch (IOException e)
        {
            Log.d("IOExcepton","Unable to find congiguration");
        }

    }
    String readline( FileReader reader)
    {
        String line = "";
        int  buff;
        try {
            while ((buff = reader.read())>-1)
            {
                if(((char)buff)=='\n')
                    break;
                line+=(char)buff;
            }
        }catch (IOException e)
        {
            Log.d("Exception","Unable to read file");
        }
        return line;
    }
}
