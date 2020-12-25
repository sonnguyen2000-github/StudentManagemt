package com.example.studentmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentHolder>{
    private List<Student> studentList;
    private StudentClickListener listener;
    public List<Boolean> checkList;
    private int position;


    public StudentAdapter(List<Student> studentList, List<Boolean> checkList,
            StudentClickListener listener){
        this.studentList = studentList;
        this.listener = listener;
        this.checkList = checkList;
    }

    @NonNull
    @Override
    public StudentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.student_layout, parent, false);
        return new StudentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentHolder holder, int position){
        Student student = studentList.get(position);

        holder.idTxt.setText(student.getId());
        holder.fullNameTxt.setText(student.getFullName());
        holder.emailTxt.setText(student.getEmail());
        holder.checkBox.setChecked(checkList.get(position));
    }

    @Override
    public int getItemCount(){
        return studentList.size();
    }

    public int getPosition(){
        return position;
    }

    public void setPosition(int position){
        this.position = position;
    }

    public class StudentHolder extends RecyclerView.ViewHolder implements
            View.OnCreateContextMenuListener{
        public TextView idTxt;
        public TextView fullNameTxt;
        public TextView emailTxt;
        public CheckBox checkBox;

        public StudentHolder(@NonNull View itemView){
            super(itemView);
            idTxt = itemView.findViewById(R.id.id);
            fullNameTxt = itemView.findViewById(R.id.full_name);
            emailTxt = itemView.findViewById(R.id.email);
            checkBox = itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(v -> {
                if(listener != null){
                    listener.OnItemClick(getAdapterPosition());
                }

                Student student = studentList.get(getAdapterPosition());
                Intent detail = new Intent(itemView.getContext(), DetailActivity.class);
                Bundle bundle = new Bundle();

                bundle.putString("id", student.getId());
                bundle.putString("fullname", student.getFullName());
                bundle.putString("email", student.getEmail());
                bundle.putString("birthday", student.getBirthDay().toString());
                bundle.putString("address", student.getAddress());

                bundle.putBoolean("isNew", false);
                bundle.putBoolean("isEdit", false);

                detail.putExtras(bundle);
                itemView.getContext().startActivity(detail);
            });
            itemView.setOnLongClickListener(v -> {
                setPosition(getAdapterPosition());
                return false;
            });

            checkBox.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> checkList.set(getAdapterPosition(), isChecked));

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenu.ContextMenuInfo menuInfo){
            menu.add(0, 100, 0, "EDIT");
            menu.add(0, 101, 1, "REMOVE");
        }
    }

    public interface StudentClickListener{
        void OnItemClick(int position);
    }
}
