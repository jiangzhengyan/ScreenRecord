package com.screen.record;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private Button buttonRecord = null;

    private boolean isRecord = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonRecord = (Button) findViewById(R.id.buttonRecord);

        buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isRecord) {
                    ScreenRecordService.stopScreenRecord(MainActivity.this);
                    isRecord = false;
                    buttonRecord.setText(new String("开始录屏"));
                    Toast.makeText(MainActivity.this, "录屏成功", Toast.LENGTH_SHORT).show();
                } else {
                    ScreenRecordService.startScreenRecord(MainActivity.this);
                    isRecord = true;
                    buttonRecord.setText(new String("停止录屏"));
                }
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                ScreenRecordService.start(MainActivity.this, resultCode, data);
                Toast.makeText(this, "录屏开始", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "录屏失败", Toast.LENGTH_SHORT).show();
            }

        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 在这里将BACK键模拟了HOME键的返回桌面功能（并无必要）
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            simulateHome();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 模拟HOME键返回桌面的功能
     */
    private void simulateHome() {
        //Intent.ACTION_MAIN,Activity Action: Start as a main entry point, does not expect to receive data.
        Intent intent = new Intent(Intent.ACTION_MAIN);
        //If set, this activity will become the start of a new task on this history stack.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //This is the home activity, that is the first activity that is displayed when the device boots.
        intent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(intent);

    }
}