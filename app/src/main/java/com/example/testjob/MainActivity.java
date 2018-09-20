package com.example.testjob;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {

    ArrayList<Message> myMessages;
    EditText messageInput;
    Adapter adapter;
    RecyclerView recyclerView;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myMessages = new ArrayList<>();
        messageInput = findViewById(R.id.editText);

        recyclerView = findViewById(R.id.myRecyc);
        LinearLayoutManager lm = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(lm);
        adapter = new Adapter(getApplicationContext(), myMessages);
        recyclerView.setAdapter(adapter);

        btnSend = findViewById(R.id.buttonSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
    }

    public void SendMessage() {
        final String message = messageInput.getText().toString();

        Retrofit retrofit2 = new Retrofit.Builder()
                .baseUrl("https://api.pushover.net/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PushoverApi myPushoverApi = retrofit2.create(PushoverApi.class);
        Call<ApiAnswer> myRequest = myPushoverApi.sendMessage(
                "aqv5ti5bmwxsmx952zan6mhm2tdgka", //pushover api token
                "u7meidpxn8d6fhfq5e9j45kpexngcn", //pushover api user id
                message);

        if(internetCheck()) {
            myRequest.enqueue(new Callback<ApiAnswer>() {
                @Override
                public void onResponse(Call<ApiAnswer> call, Response<ApiAnswer> response) {
                    try {
                        if (response.body().getStatus()==1) {
                            saveMessage(message);
                        }
                    } catch (NullPointerException e) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Message not sent", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                @Override
                public void onFailure(Call<ApiAnswer> call, Throwable t) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Message not sent. Error", Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No internet connection", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(myMessages);
        prefsEditor.putString("MyObject", json);
        prefsEditor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        Gson gson = new Gson();
        String jsonFromSharedPrefs = appSharedPrefs.getString("MyObject", "");
        Type type = new TypeToken<ArrayList<Message>>(){}.getType();
        ArrayList<Message> retrievedMessages = gson.fromJson(jsonFromSharedPrefs, type);

        if (retrievedMessages != null && retrievedMessages.size()>0) {
            myMessages.clear();
            for (int i = 0; i < retrievedMessages.size(); i++) {
                myMessages.add(retrievedMessages.get(i));
            }
            adapter.notifyDataSetChanged();
        }
    }

    public interface PushoverApi {
        @POST("messages.json")
        @FormUrlEncoded
        Call<ApiAnswer> sendMessage(@Field("token") String token,
                                    @Field("user") String user,
                                    @Field("message") String message);
    }

    public void saveMessage (String message) {
        Message sentMessage = new Message();
        sentMessage.setMessage(message);
        sentMessage.setDateSent(Calendar.getInstance().getTime().toString());
        myMessages.add(sentMessage);
        adapter.notifyDataSetChanged();

        messageInput.setText("", TextView.BufferType.EDITABLE);

        Toast toast = Toast.makeText(getApplicationContext(),
                "Message sent", Toast.LENGTH_SHORT);
        toast.show();
    }

    public boolean internetCheck(){
        InetCheck iCheck = new InetCheck();
        boolean result = false;
        try{
            result = iCheck.execute().get();
        } catch (Exception e) {
            // smth is wrong, so result will stay false
        }
        return result;
    }
}
