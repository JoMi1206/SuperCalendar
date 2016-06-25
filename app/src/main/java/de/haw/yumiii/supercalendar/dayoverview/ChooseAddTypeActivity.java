package de.haw.yumiii.supercalendar.dayoverview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import de.haw.yumiii.supercalendar.R;

public class ChooseAddTypeActivity extends AppCompatActivity {

    final static String PARA_TYPE = "add_type";
    final static String ADD_TODO = "add_todo";
    final static String ADD_IMAGE = "add_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_add_type);

        setTitle(R.string.title_activity_choose_add_type);

        final Button addTodo = (Button) findViewById(R.id.choose_add_todo);
        addTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeChosen(ADD_TODO);
            }
        });

        final Button addImage = (Button) findViewById(R.id.choose_add_image);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeChosen(ADD_IMAGE);
            }
        });

        final Button cancel = (Button) findViewById(R.id.choose_add_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                setResult(RESULT_CANCELED, result);
                finish();
            }
        });
    }

    private void typeChosen(String type) {
        Intent result = new Intent();
        result.putExtra(PARA_TYPE, type);
        setResult(RESULT_OK, result);

        finish();
    }
}
