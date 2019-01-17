package com.example.mjkim.wheelchair2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


public class MenuScreen extends AppCompatActivity {

    ImageButton back_button;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_screen);

        back_button = (ImageButton)findViewById(R.id.back_b);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void openFirstScreen(){
        Intent intent = new Intent(this, FirstScreen.class);
        startActivity(intent);
    }

    // 아직 준비중임, 다되면 열 예정
//    public void picto(View v){
//        Intent intent = new Intent(this,PictogramActivity.class);
//        startActivity(intent);
//    }

    public void prepare(View v) {
//        Intent intent = new Intent(this, FindNearLocationActivity.class);
//        startActivity(intent);
        Toast.makeText(MenuScreen.this, "준비중입니다", Toast.LENGTH_SHORT).show();
    }


}
