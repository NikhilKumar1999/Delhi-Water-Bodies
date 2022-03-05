package com.example.delhiwaterbody;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Outputdata {
    public Bitmap image;
    public String present_condition;
    public String status_of_maintenance;
    public String dry_or_wet;
    public String sewage;
    public String enroachment;
    public String builtup;
    public String traceable;
    public int water_body_id;
    public String remark;
    public HashMap<String,Object> remarks_map;
    public void submit(final Context context)
    {
        try {
            if(image == null)
            {
                Toast.makeText(context,"Take a photo",Toast.LENGTH_LONG).show();
            }
            final String Firestorepath = "report_images/"+Calendar.getInstance().getTime().toString()+water_body_id+".jpeg";
            final StorageReference streference = FirebaseStorage.getInstance().getReference().child(Firestorepath);
            final File temp_storage = new File(context.getDataDir() + "/temp.jpg");
            image.compress(Bitmap.CompressFormat.JPEG, 70, new FileOutputStream(temp_storage));
            final Uri image_file = Uri.fromFile(temp_storage);

            streference.putFile(image_file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(context,"Image Uploaded Successfully",Toast.LENGTH_LONG).show();
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    final  Map<String ,Object> data = new HashMap<>();
                    data.put("id",water_body_id);
                    data.put("present_condition",present_condition);
                    data.put("Status_of_maintenance",status_of_maintenance);
                    data.put("dry_or_wet",dry_or_wet);
                    data.put("sewage",sewage);
                    data.put("enroachment",enroachment);
                    data.put("buildup",builtup);
                    data.put("traceable",traceable);
                    SQLiteDatabase dblocations = SQLiteDatabase.openDatabase(MapsActivity.database_path,null,SQLiteDatabase.OPEN_READONLY);
                    Cursor raw = dblocations.rawQuery("Select Village from locations where SNo like "+water_body_id,null);
                    raw.moveToNext();
                    data.put("village",raw.getString(0));
                    data.putAll(remarks_map);
                    streference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            data.put("image_path",uri.toString());
                            db.collection("report_db").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(context,"Data Uploaded Successfully",Toast.LENGTH_LONG).show();
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context,"Data Uploaded Failed",Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    });
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context,"Image Uploading Failed",Toast.LENGTH_LONG).show();
                }
            });

        }
        catch (IOException e)
        {
            Log.d("Submission","Exception");
        }

    }
}
