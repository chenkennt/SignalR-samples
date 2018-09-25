package com.microsoft.aspnet.signalr.samples.androidjavaclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private void error(Exception ex) {
        TextView textView = (TextView)findViewById(R.id.tvMain);
        ex.printStackTrace();
        textView.setText("There was an error: " + ex.getMessage());
    }

    private void connect() {
        ChatRoomApplication app = (ChatRoomApplication)getApplication();

        class HubConnectTask extends AsyncTask<Void, Void, Exception> {
            protected Exception doInBackground(Void... args) {
                try {
                    app.connect();
                } catch (Exception ex) {
                    return ex;
                }

                return null;
            }
        }

        try {
            Exception ex = new HubConnectTask().execute().get();
            if (ex != null) error(ex);
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void sendMessage() {
        ChatRoomApplication app = (ChatRoomApplication)getApplication();
        EditText editText = findViewById(R.id.etMessageText);
        String message = editText.getText().toString();
        editText.setText("");
        try {
            app.sendMessage(message);
        } catch (Exception ex) {
            error(ex);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.lvMessages);
        Button sendButton = findViewById(R.id.bSend);
        EditText editText = findViewById(R.id.etMessageText);

        List<String> messageList = new ArrayList<String>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, messageList);
        listView.setAdapter(arrayAdapter);

        ChatRoomApplication app = (ChatRoomApplication)getApplication();
        app.onMessage((name, message) -> {
            runOnUiThread(() -> {
                String combinedMessage;
                if (name.equals("_SYSTEM_")) combinedMessage = "[" + message + "]";
                else combinedMessage = name + ": " + message;
                arrayAdapter.add(combinedMessage);
                arrayAdapter.notifyDataSetChanged();
            });
        });

        app.onClosed((ex) -> {
            error(ex);
        });

        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendButton.performClick();
                    return true;
                }
                return false;
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        if (!app.isConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Input your name");

            // Set up the input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(app.getUsername());
            input.selectAll();
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    app.setUsername(input.getText().toString());
                    connect();
                }
            });

            builder.show();
        }
    }
}
