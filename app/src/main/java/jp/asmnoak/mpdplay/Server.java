package jp.asmnoak.mpdplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static jp.asmnoak.mpdplay.MainActivity.ipaddr;
import static jp.asmnoak.mpdplay.MainActivity.mpdport;

public class Server extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        Button setButton = (Button)findViewById(R.id.btnServer);
        Button cancelButton = (Button)findViewById(R.id.btnCancel);
        Button shutButton = (Button)findViewById(R.id.btnShutdown);
        final EditText edtIP = (EditText)findViewById(R.id.edtIP);
        final EditText edtPort = (EditText)findViewById(R.id.edtPort);
        edtIP.setText(ipaddr);
        edtPort.setText(String.valueOf(mpdport));

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mpdport = (int)Integer.valueOf(edtPort.getText().toString());
                ipaddr = edtIP.getText().toString();
                SharedPreferences pref = getSharedPreferences("user_data", MODE_PRIVATE);
                if (pref!=null) {  // save it
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt("port", mpdport);
                    editor.putString("ipaddr", ipaddr);
                    editor.commit();
                }
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        shutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://" + ipaddr + ":8000/bb/01/";  // uri of optional command server
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
           }
        });

    }
}
