package com.example.studentmanagement;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Date;

public class DetailActivity extends AppCompatActivity{
    private Student student;
    private EditText idText, fullNameText, emailText, addressText;
    private TextView birthdayText;
    private Button backBtn, saveBtn;
    private boolean isNew;
    private boolean isEdit;
    private SQLiteDatabase db;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle bundle = getIntent().getExtras();
        isNew = bundle.getBoolean("isNew");
        isEdit = bundle.getBoolean("isEdit");

        student = new Student();
        if(!isNew){
            student.setId(bundle.getString("id"));
            student.setFullName(bundle.getString("fullname"));
            student.setEmail(bundle.getString("email"));
            student.setBirthDay(Date.valueOf(bundle.getString("birthday")));
            student.setAddress(bundle.getString("address"));
        }
        idText = findViewById(R.id.id);
        fullNameText = findViewById(R.id.full_name);
        emailText = findViewById(R.id.email);
        birthdayText = findViewById(R.id.birthday);
        addressText = findViewById(R.id.address);

        birthdayText.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, null, 2000, 0, 20);
            dialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                Date date = Date.valueOf(year + "-" + (month + 1) + "-" + dayOfMonth);
                birthdayText.setText(date.toString());
            });
            dialog.show();
        });

        backBtn = findViewById(R.id.back_btn);
        saveBtn = findViewById(R.id.save_btn);

        backBtn.setOnClickListener(v -> {
            finish();
        });
        saveBtn.setOnClickListener(v -> {
            if(idText.getText().toString().isEmpty()||fullNameText.getText().toString().isEmpty()||
               emailText.getText().toString().isEmpty()||
               birthdayText.getText().toString().isEmpty()||
               addressText.getText().toString().isEmpty()){
                Toast.makeText(this, "Empty fields", Toast.LENGTH_SHORT).show();
                return;
            }
            student.setId(idText.getText() + "");
            student.setFullName(fullNameText.getText() + "");
            student.setEmail(emailText.getText() + "");
            student.setBirthDay(Date.valueOf(birthdayText.getText() + ""));
            student.setAddress(addressText.getText() + "");
            save();
        });

        if(isNew){
            setAddNew();
        }else if(isEdit){
            setEditable();
        }
    }

    @Override
    public void onResume(){
        setStudent();
        super.onResume();
    }

    public void setStudent(){
        idText.setText(student.getId());
        fullNameText.setText(student.getFullName());
        emailText.setText(student.getEmail());
        birthdayText.setText(
                student.getBirthDay() != null ? student.getBirthDay().toString() : "2000-01-20");
        addressText.setText(student.getAddress());
    }

    public void setEditable(){
        fullNameText.setEnabled(true);
        emailText.setEnabled(true);
        birthdayText.setEnabled(true);
        addressText.setEnabled(true);

        saveBtn.setEnabled(true);
    }

    public void setAddNew(){
        clear();
        idText.setEnabled(true);
        setEditable();
    }

    public void clear(){
        idText.setText("");
        fullNameText.setText("");
        emailText.setText("");
        birthdayText.setText("");
        addressText.setText("");
    }

    public void save(){
        try{
            db = MainActivity.getDatabase();
            db.beginTransaction();

            ContentValues contentValues = new ContentValues();
            contentValues.put("id", student.getId());
            contentValues.put("fullname", student.getFullName());
            contentValues.put("email", student.getEmail());
            contentValues.put("birthday", student.getBirthDay().toString());
            contentValues.put("address", student.getAddress());
            if(isNew){
                db.insert("student", null, contentValues);
            }else if(isEdit){
                db.update("student", contentValues, "id = ?", new String[]{student.getId()});
            }
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db.endTransaction();
            finish();
        }
    }
}