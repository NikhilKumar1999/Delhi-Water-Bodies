package com.example.delhiwaterbody;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class Report_Activity extends AppCompatActivity implements View.OnClickListener {

    Bitmap img;
    boolean blank = false;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_);
        Button b = findViewById(R.id.button2);
        Button submit = findViewById(R.id.submit);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,getDataDir()+"capturedphoto.jpg");
                if(intent.resolveActivity(getPackageManager())!=null)
                    startActivityForResult(intent,1);
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blank = false;
                if(img == null)
                {
                    Toast.makeText(getApplicationContext(),"Take a photo",Toast.LENGTH_LONG).show();
                    return;
                }
                Outputdata outpudata = new Outputdata();
                outpudata.builtup = getradiovalue(R.id.bult_up);
                outpudata.dry_or_wet = getradiovalue(R.id.dry_or_wet);
                outpudata.enroachment = getradiovalue(R.id.enroachment);
                outpudata.present_condition = getradiovalue(R.id.present_conditon);
                outpudata.sewage = getradiovalue(R.id.sewage);
                outpudata.status_of_maintenance = getradiovalue(R.id.status_of_maintenance);
                outpudata.traceable = getradiovalue(R.id.traceable);
                outpudata.image = img;
                outpudata.remarks_map = new HashMap<String,Object>();
                outpudata.water_body_id = getIntent().getIntExtra("SNo",0);
                gettexteditvalue(R.id.remark_built_up,outpudata.remarks_map,"remark_built_up");
                gettexteditvalue(R.id.remark_dry_or_wet,outpudata.remarks_map,"remark_dry_or_wet");
                gettexteditvalue(R.id.remark_enroachment,outpudata.remarks_map,"remark_enroachment");
                gettexteditvalue(R.id.remark_present_condition,outpudata.remarks_map,"remark_present_condition");
                gettexteditvalue(R.id.remark_sewage,outpudata.remarks_map,"remark_sewage");
                gettexteditvalue(R.id.remark_traceable,outpudata.remarks_map,"remark_traceable");
                gettexteditvalue(R.id.remark_status_of_maintenance,outpudata.remarks_map,"remark_status_of_maintenance");
                if(!blank) {
                    outpudata.submit(getApplicationContext());
                    finish();
                }

            }
        });
    }
    void gettexteditvalue(int res_id, HashMap<String,Object> add,String key)
    {

       String result =  ((EditText)findViewById(res_id)).getText().toString();
       if(result == "")
           return ;
       add.put(key,result);
    }
    String getradiovalue(int res_id)
    {
        RadioButton rb = ((RadioButton)findViewById(((RadioGroup)findViewById(res_id)).getCheckedRadioButtonId()));
        if(rb==null)
            {
                Toast.makeText(this,"Fields cannot be blank",Toast.LENGTH_LONG).show();
                blank = true;
                return null;
            }
        else
        {
            if(rb.getText() == null)
            {
                Toast.makeText(this,"Fields cannot be blank",Toast.LENGTH_LONG).show();
                blank = true;
                return null;
            }
            else
                return rb.getText().toString();
        }
    }

    @Override
    protected void onActivityResult(int RequesCode,int ResultCode,Intent data)
    {
            super.onActivityResult(RequesCode, ResultCode, data);
            if (RequesCode == 1 && ResultCode == RESULT_OK) {
                Bitmap thumpnail = data.getParcelableExtra("data");
                ImageView image = (ImageView)findViewById(R.id.photo);
                image.setImageBitmap(thumpnail);
                img = thumpnail;
            }
    }

    @Override
    public void onClick(View v) {

    }
}