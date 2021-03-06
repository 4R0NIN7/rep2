package com.example.r0nin.eventer;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WydarzenieActivity extends AppCompatActivity {
    protected ListView listViewTwojeWydarzenia, listViewWydarzeniaWPoblizu;
    protected ArrayAdapter<String> adapterTwojeWydarzenia, adapterWydarzeniaWPoblizu;
    protected ArrayList<String> listItemsTwojeWydarzenia, listItemsWydarzeniaWPoblizu;
    protected Button btnUtworz;
    protected EditText editTextNazwa, editTextDataPoczatku, editTextDataKonca;
    private String login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wydarzenie);
        editTextNazwa = (EditText) findViewById(R.id.editTextNazwa);
        editTextDataKonca = (EditText) findViewById(R.id.editTextDataKonca);
        editTextDataPoczatku = (EditText) findViewById(R.id.editTextDataPoczatku);
        btnUtworz = (Button) findViewById(R.id.btnUtworz);

        btnUtworz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostNewEvent().execute();
            }
        });

        Bundle loginBundle = getIntent().getExtras();
        if(loginBundle != null){
            login = loginBundle.getString("login");


        }

        listViewTwojeWydarzenia = (ListView) findViewById(R.id.listViewTwojeWydarzenia);
        listItemsTwojeWydarzenia = new ArrayList<String>();
        adapterTwojeWydarzenia = new ArrayAdapter<String>(this,R.layout.list_item, listItemsTwojeWydarzenia);
        listViewTwojeWydarzenia.setAdapter(adapterTwojeWydarzenia);

        listViewWydarzeniaWPoblizu = (ListView) findViewById(R.id.listViewWydarzeniaWPoblizu);
        listItemsWydarzeniaWPoblizu = new ArrayList<String>();
        adapterWydarzeniaWPoblizu = new ArrayAdapter<String>(this,R.layout.list_item, listItemsWydarzeniaWPoblizu);
        listViewWydarzeniaWPoblizu.setAdapter(adapterWydarzeniaWPoblizu);

        //Jak kliknie to np: niech sie zapisze do tego wydarzenia lub z niego sie wypisze

        listViewTwojeWydarzenia.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = listItemsTwojeWydarzenia.get(i);
                String params[] = item.split(": ");
                String id = params[0];
                String data_poczatku = params[4];
                Log.d("godzilla", "id: "+id+", data: "+data_poczatku);

                String login2 = params[3];

                if(login2.equals(login)){
                    Toast.makeText(WydarzenieActivity.this, "You cannot leave your won event", Toast.LENGTH_SHORT).show();
                }else{
                    new LeaveEventTask(Integer.parseInt(id)).execute();
                }

            }
        });

        listViewWydarzeniaWPoblizu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = listItemsWydarzeniaWPoblizu.get(i);
                Log.d("godzilla", "item: "+item);

                String params[] = item.split(": ");
                String id = params[0];
                String data_poczatku = params[4];
                Log.d("godzilla", "id: "+id+", data: "+data_poczatku);
                new JoinEventTask(Integer.parseInt(id), data_poczatku).execute();
            }
        });

        new WydarzeniaTimer(10000, 1000).callTimer();

    }




    private class GetMyEventsTask extends AsyncTask<String, Void, Object> {

        String mlogin;


        public GetMyEventsTask(String me){
            mlogin = me;
            //Log.d("godzilla", "new task "+me);

        }


        @Override
        protected Object doInBackground(String... strings) {
            //Log.d("godzilla", "new background");
            OkHttpClient client = new OkHttpClient();
            Request req;

            req = new Request.Builder().url(getResources().getString(R.string.apiUrl)+"/api/Wydarzenie/moje-wydarzenia/"+mlogin).build();
            //Log.d("godzila", "asking "+getResources().getString(R.string.apiUrl)+"/api/Wydarzenie/moje-wydarzenia/"+mlogin);
            Response res = null;
            try{
                res = client.newCall(req).execute();
                return res.body().string();
            }catch(IOException exception){
                exception.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                if(o != null){
                    //Log.d("godzilla", "getting response");
                    JSONArray json = new JSONArray(o.toString());
                    //Log.d("godzilla", "response: "+json);
                    listItemsTwojeWydarzenia.clear();
                    for(int i = 0; i<json.length(); i++){
                        JSONObject userJson = json.getJSONObject(i);
                        String lat = userJson.getString("szerokosc");
                        String lon = userJson.getString("wysokosc");

                        String login_organizatora = userJson.getString("login_organizatora");
                        String data_poczatku = userJson.getString("data_poczatku");
                        String data_konca = userJson.getString("data_konca");
                        String nazwa_wydarzenia = userJson.getString("nazwa_wydarzenia");
                        String id = userJson.getString("id");
                        //Log.d("godzilla", nazwa_wydarzenia+": "+lat+","+lon+": "+login_organizatora+": "+data_poczatku+","+data_konca);
                        //SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                        //Date d1 = format.parse(data_poczatku);
                        //Date d2 = format.parse(data_konca);

                        listItemsTwojeWydarzenia.add(id+": "+nazwa_wydarzenia+": "+lat+","+lon+": "+login_organizatora+": "+data_poczatku+": "+data_konca);
                    }

                    adapterTwojeWydarzenia.notifyDataSetChanged();
                    listViewTwojeWydarzenia.invalidateViews();
                    listViewTwojeWydarzenia.refreshDrawableState();


                }
            } catch (JSONException e) {

            } //catch (ParseException e) {
               // Log.d("godzilla", "parsing error");
              //  e.printStackTrace();
            //}
        }
    }
    private class GetRecommendedEventsTask extends AsyncTask<String, Void, Object> {

        String mlogin;


        public GetRecommendedEventsTask(String me){
            mlogin = me;

        }


        @Override
        protected Object doInBackground(String... strings) {


            OkHttpClient client = new OkHttpClient();
            Request req;

            req = new Request.Builder().url(getResources().getString(R.string.apiUrl)+"/api/Wydarzenie/proponowane-wydarzenia/"+mlogin).build();
            Response res = null;
            try{
                res = client.newCall(req).execute();
                return res.body().string();
            }catch(IOException exception){
                exception.printStackTrace();
            }

            return null;
        }



        @Override
        protected void onPostExecute(Object o) {
            try {
                if(o != null){
                    //("godzilla", "getting response");
                    JSONArray json = new JSONArray(o.toString());
                    listItemsWydarzeniaWPoblizu.clear();
                    for(int i = 0; i<json.length(); i++){
                        JSONObject userJson = json.getJSONObject(i);
                        String lat = userJson.getString("szerokosc");
                        String lon = userJson.getString("wysokosc");

                        String login_organizatora = userJson.getString("login_organizatora");
                        String data_poczatku = userJson.getString("data_poczatku");
                        String data_konca = userJson.getString("data_konca");
                        String nazwa_wydarzenia = userJson.getString("nazwa_wydarzenia");
                        String id = userJson.getString("id");

                        listItemsWydarzeniaWPoblizu.add(id+": "+nazwa_wydarzenia+": "+lat+","+lon+": "+login_organizatora+": "+data_poczatku+": "+data_konca);
                    }

                    adapterWydarzeniaWPoblizu.notifyDataSetChanged();
                    listViewWydarzeniaWPoblizu.invalidateViews();
                    listViewWydarzeniaWPoblizu.refreshDrawableState();


                }
            } catch (JSONException e) {

            }
        }
    }

    private class PostNewEvent extends AsyncTask<String, Void, Object> {



        @Override
        protected Object doInBackground(String... params) {

            String url = getResources().getString(R.string.apiUrl)+"/api/Wydarzenie/add";


            // Names and values will be url encoded
            final FormBody.Builder formBuilder = new FormBody.Builder();
            formBuilder.add("nazwa_wydarzenia", editTextNazwa.getText().toString());
            formBuilder.add("login_organizatora",login );
            formBuilder.add("data_poczatku", editTextDataPoczatku.getText().toString());
            formBuilder.add("data_konca", editTextDataKonca.getText().toString());
            //formBuilder.add("wysokosc", "1.01.2000");
            //formBuilder.add("szerokosc", "1.01.2000");

            RequestBody requestBody = formBuilder.build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                int responseCode = response.code();
                //("godzilla", "responseCode: " + responseCode);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new Object();
        }

        @Override
        protected void onPostExecute(Object o) {

            if(o != null){
                //("godzilla", "ok");
                editTextNazwa.setText("");
                editTextDataPoczatku.setText("");
                editTextDataKonca.setText("");

            }


        }



    }
    private class JoinEventTask extends AsyncTask<String, Void, Object> {


        private int eventId;
        private String dataPoczatku;
        public JoinEventTask(int eventId, String dataPoczatku){
            this.eventId = eventId;
            this.dataPoczatku = dataPoczatku;
        }

        @Override
        protected Object doInBackground(String... params) {

            String url = getResources().getString(R.string.apiUrl)+"/api/udzial/add";


            // Names and values will be url encoded
            final FormBody.Builder formBuilder = new FormBody.Builder();
            formBuilder.add("login_uczestnika", login);
            formBuilder.add("id_wydarzenia",""+eventId );
            formBuilder.add("data_dolaczenia", dataPoczatku);

            RequestBody requestBody = formBuilder.build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                int responseCode = response.code();
                //("godzilla", "responseCode: " + responseCode);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new Object();
        }

        @Override
        protected void onPostExecute(Object o) {

            if(o != null){
                //("godzilla", "ok");
                editTextNazwa.setText("");
                editTextDataPoczatku.setText("");
                editTextDataKonca.setText("");
                Toast.makeText(WydarzenieActivity.this, "Joined an event!", Toast.LENGTH_SHORT).show();

            }


        }



    }
    private class LeaveEventTask extends AsyncTask<String, Void, Object> {


        private int eventId;

        public LeaveEventTask(int eventId){
            this.eventId = eventId;

        }

        @Override
        protected Object doInBackground(String... params) {

            String url = getResources().getString(R.string.apiUrl)+"/api/udzial/delete/"+eventId+"-"+login;
            //Log.d("godzilla", "calling: "+url);

            // Names and values will be url encoded


            Request request = new Request.Builder()
                    .url(url)
                    .delete()
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                int responseCode = response.code();
                //("godzilla", "responseCode: " + responseCode);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new Object();
        }

        @Override
        protected void onPostExecute(Object o) {

            if(o != null){
                //("godzilla", "ok");
                editTextNazwa.setText("");
                editTextDataPoczatku.setText("");
                editTextDataKonca.setText("");

                Toast.makeText(WydarzenieActivity.this, "Left event!", Toast.LENGTH_SHORT).show();
            }


        }



    }



    public class WydarzeniaTimer {
        protected long period; //mili sekundy
        protected int delay;
        protected final Timer myTimer = new Timer();

        public WydarzeniaTimer(long period, int delay){
            this.period = period;
            this.delay = delay;
        }
        private void callTimer()
        {

            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new GetMyEventsTask(login).execute("");
                    new GetRecommendedEventsTask(login).execute();
                }
            }, delay,period);

        }
    }
}
