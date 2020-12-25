package com.example.studentmanagement;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.javafaker.Faker;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements StudentAdapter.StudentClickListener{
    private final String database = "students";
    private static SQLiteDatabase db;
    private static String myDbPath;
    private StudentAdapter studentAdapter;
    private List<Student> studentList;
    private List<Boolean> checkList;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recycle_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                                                                           RecyclerView.VERTICAL,
                                                                           false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLongClickable(true);
        registerForContextMenu(recyclerView);

        File storagePath = getApplication().getFilesDir();
        myDbPath = storagePath + "/" + database;

        //deleteOldData();
        try{
            NewDatabase newDatabase = new NewDatabase();
            newDatabase.execute();
        }catch(Exception e){
            e.printStackTrace();
        }
        db = getDatabase();

        studentList = new ArrayList<>();
        checkList = new ArrayList<>();

        studentAdapter = new StudentAdapter(studentList, checkList, this);
        recyclerView.setAdapter(studentAdapter);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try{
            db.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume(){
        try{
            SyncTask syncTask = new SyncTask();
            syncTask.execute();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            studentAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        int position;
        position = studentAdapter.getPosition();
        Log.v(item.getTitle() + "", position + "");
        if(item.getItemId() == 100){
            Student student = studentList.get(position);
            Intent edit = new Intent(this, DetailActivity.class);
            Bundle bundle = new Bundle();

            bundle.putString("id", student.getId());
            bundle.putString("fullname", student.getFullName());
            bundle.putString("email", student.getEmail());
            bundle.putString("birthday", student.getBirthDay().toString());
            bundle.putString("address", student.getAddress());

            bundle.putBoolean("isNew", false);
            bundle.putBoolean("isEdit", true);

            edit.putExtras(bundle);
            startActivity(edit);
        }else if(item.getItemId() == 101){
            new AlertDialog.Builder(this).setTitle("").setMessage("CONFIRM DELETE ?")
                                         .setPositiveButton("DELETE", (dialog, which) -> {
                                             try{
                                                 DeleteTask deleteTask = new DeleteTask();
                                                 if(deleteTask.doInBackground(
                                                         studentList.get(position).getId())){
                                                     studentList.remove(position);
                                                     studentAdapter.notifyDataSetChanged();
                                                 }
                                             }catch(Exception e){
                                                 e.printStackTrace();
                                             }
                                         }).setNegativeButton("CANCEL",
                                                              (dialog, which) -> dialog.dismiss())
                                         .setCancelable(false).create().show();

        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.action_bar, menu);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Student Management");
        actionBar.setDisplayShowCustomEnabled(true);
        /**/
        SearchView searchView = (SearchView) menu.findItem(R.id.search_bar).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query){
                Log.v("Search for", query);
                Log.v("Searching", query);
                try{
                    SearchTask searchTask = new SearchTask();
                    if(searchTask.doInBackground(query)){
                        studentAdapter.notifyDataSetChanged();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText){
                Log.v("Searching", newText);
                try{
                    SearchTask searchTask = new SearchTask();
                    if(searchTask.doInBackground(newText)){
                        studentAdapter.notifyDataSetChanged();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        });
        searchView.setOnCloseListener(() -> {
            searchView.clearFocus();
            onResume();
            return false;
        });

        Button multiSelect = (Button) menu.findItem(R.id.multi_select).getActionView();
        multiSelect.setText("DELETE");
        Button newButton = (Button) menu.findItem(R.id.new_btn).getActionView();
        newButton.setText("NEW");

        multiSelect.setOnClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("").setMessage("DELETE ALL SELECTED ?")
                                         .setPositiveButton("DELETE", (dialog, which) -> {
                                             try{
                                                 int count = 0;
                                                 for(int position = 0;
                                                     position < checkList.size(); ){
                                                     boolean checked = checkList.get(position);
                                                     if(checked){
                                                         ++count;
                                                         DeleteTask deleteTask = new DeleteTask();
                                                         if(deleteTask.doInBackground(
                                                                 studentList.get(position)
                                                                            .getId())){
                                                             studentList.remove(position);
                                                             checkList.remove(position);
                                                         }
                                                     }else{
                                                         ++position;
                                                     }
                                                 }
                                                 studentAdapter.notifyDataSetChanged();
                                                 Log.v("Total delete", count + "");
                                             }catch(Exception e){
                                                 e.printStackTrace();
                                             }
                                         }).setNegativeButton("CANCEL",
                                                              (dialog, which) -> dialog.dismiss())
                                         .setCancelable(false).create().show();
        });
        newButton.setOnClickListener(v -> {
            Intent edit = new Intent(this, DetailActivity.class);
            Bundle bundle = new Bundle();

            bundle.putBoolean("isEdit", false);
            bundle.putBoolean("isNew", true);

            edit.putExtras(bundle);
            startActivity(edit);
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void OnItemClick(int position){
        Log.v("student", position + "");
    }

    public static SQLiteDatabase getDatabase(){
        if(db == null){
            db = SQLiteDatabase.openDatabase(myDbPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        }
        return db;
    }

    public void createDatabase(){
        String createTable = "create table student( " + "id varchar(8) primary key not null, " +
                             "fullname varchar(30) not null, " + "email varchar(30) not null, " +
                             "birthday Date not null, " + "address varchar(40) not null)";
        db = getDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(
                "SELECT \n" + "    name\n" + "FROM \n" + "    sqlite_master \n" + "WHERE \n" +
                "    type ='table' AND \n" + "    name = 'student';", null);
        cursor.moveToPosition(-1);
        if(!cursor.moveToNext()){
            db.execSQL(createTable);
            Log.v("Create new table", "successfully");
        }
        cursor = db.rawQuery("select * from student", null);
        cursor.moveToPosition(-1);
        if(!cursor.moveToNext()){
            for(int i = 0; i < 200; i++){
                Faker faker = new Faker();
                ContentValues contentValues = new ContentValues();
                contentValues.put("id", faker.idNumber().valid());
                contentValues.put("fullname", faker.name().fullName());
                contentValues.put("email", faker.internet().emailAddress());
                contentValues.put("birthday",
                                  String.valueOf(new Date(faker.date().birthday().getTime())));
                contentValues.put("address", faker.address().fullAddress());
                db.insert("student", null, contentValues);
            }
            Log.v("Insert values", "successfully");
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void deleteOldData(){
        db = getDatabase();
        db.beginTransaction();
        db.execSQL("drop table student");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    class SyncTask extends AsyncTask<Void, Void, Boolean>{

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Boolean doInBackground(Void... voids){
            studentList.clear();
            checkList.clear();
            try{
                db = getDatabase();
                db.beginTransaction();
                String select = "select * from student";
                Cursor cursor = db.rawQuery(select, null);
                cursor.moveToPosition(-1);
                while(cursor.moveToNext()){
                    Student student = new Student();
                    student.setId(cursor.getString(0));
                    student.setFullName(cursor.getString(1));
                    student.setEmail(cursor.getString(2));
                    student.setBirthDay(Date.valueOf(cursor.getString(3)));
                    student.setAddress(cursor.getString(4));
                    studentList.add(student);
                    checkList.add(false);
                }
                cursor.close();
                db.setTransactionSuccessful();
                System.out.println("Synced successfully");

                return true;
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                db.endTransaction();
            }
            return false;
        }
    }

    class SearchTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... strings){
            studentList.clear();
            checkList.clear();
            try{
                db.beginTransaction();
                String select = "select * from student where fullname like '%" + strings[0] +
                                "%' or id like '%" + strings[0] + "%'";
                Cursor cursor = db.rawQuery(select, null);
                cursor.moveToPosition(-1);
                while(cursor.moveToNext()){
                    Student student = new Student();
                    student.setId(cursor.getString(0));
                    student.setFullName(cursor.getString(1));
                    student.setEmail(cursor.getString(2));
                    long date = cursor.getLong(3) * 1000;
                    student.setBirthDay(new Date(date));
                    student.setAddress(cursor.getString(4));
                    studentList.add(student);
                    checkList.add(false);
                }
                cursor.close();
                db.setTransactionSuccessful();

                return true;
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                db.endTransaction();
            }
            return false;
        }
    }

    class DeleteTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... strings){
            try{
                db.beginTransaction();
                db.delete("student", "id = ? ", strings);
                db.setTransactionSuccessful();
                return true;
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                db.endTransaction();
            }
            return false;
        }
    }

    class NewDatabase extends AsyncTask<Void, Void, Boolean>{
        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        String waitMsg = "Creating/fetching database... ";

        @Override
        protected void onPreExecute(){
            dialog.setTitle(waitMsg);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids){
            try{
                createDatabase();
                return true;
            }catch(Exception e){
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean unused){
            if(dialog.isShowing()){
                dialog.dismiss();
            }
            onResume();
        }
    }
}