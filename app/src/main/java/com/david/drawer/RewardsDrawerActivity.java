package com.david.drawer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RewardsDrawerActivity extends AppCompatActivity implements View.OnClickListener {


    private Button mButton;
    private Button mButton_2;
    private RewardsDrawer mRoot;
    private Button mBtnInRoot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rewards_drawer);
        mButton = findViewById(R.id.btn);
        mRoot = findViewById(R.id.root);
        mButton_2 = findViewById(R.id.btn_2);
        mButton_2.setOnClickListener(this);
        mButton.setOnClickListener(this);
        mBtnInRoot = findViewById(R.id.btn_in_root);
        mBtnInRoot.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn:
                mRoot.openLeftDrawer();
                break;
            case R.id.btn_in_root:
                Toast.makeText(this, "GOtcha", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_2:
                mRoot.openRightDrawer();
                break;
        }
    }


}

