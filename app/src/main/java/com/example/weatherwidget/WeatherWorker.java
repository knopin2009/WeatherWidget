package com.example.weatherwidget;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

public class WeatherWorker extends Worker {

   // private static final String URL_STRING = "https://api.openweathermap.org/data/2.5/weather?q=Kazan&units=metric&appid=3142a54243e071c4224117439370a564";

    private String key = "ea8f840a0b54886862d0e22468658343";
    private String baseUrl="https://api.openweathermap.org/data/2.5/weather";
    private String city ="Казань";
    public static final String  TAG="WeatherApp";
    //private String ipcheck = "https://ipwho.is/";
    private String localHostAddress;
    private String cityCheck = "https://suggestions.dadata.ru/suggestions/api/4_1/rs/iplocate/address?ip=";

    private String URL_STRING = baseUrl + "?q=" + city + "&units=metric&lang=ru&appid="+key;
    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters){
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {

        GetPublicIP host = new GetPublicIP();
        String ip = host.getPublicIP();

        Log.d(TAG, "doWork: "+ip);

        try {
            //Log.d(TAG, "doWork: "+URL_STRING);
            URL urlip = new URL(cityCheck+ip);
            HttpURLConnection connectionip = (HttpURLConnection) urlip.openConnection();
            connectionip.setRequestMethod("GET");
            connectionip.setRequestProperty("Content-Type","application/json");
            connectionip.setRequestProperty("Authorization","Token e064d9159e9bb6bb2b78a69c31e79231048b4a66");
            connectionip.setRequestProperty("Accept","application/json");

            if (connectionip.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader isrip = new InputStreamReader(connectionip.getInputStream());
                BufferedReader readerip = new BufferedReader(isrip);
                StringBuilder resultip = new StringBuilder();
                String line;
                while ((line = readerip.readLine()) != null) {
                    resultip.append(line);
                }
                readerip.close();

                JSONObject jsonResponse = new JSONObject(resultip.toString());
                JSONObject mainObject = jsonResponse.getJSONObject("location").getJSONObject("data");
                city = mainObject.getString("city");
                Log.d(TAG, "doWork: "+city);
                //return Result.success();
            } else {
                return Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }

        try {
            Log.d(TAG, "doWork: "+URL_STRING);
            URL url = new URL(URL_STRING);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(isr);
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(result.toString());
                JSONObject mainObject = jsonResponse.getJSONObject("main");

                RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_layout);

                String pressure = String.valueOf(mainObject.getDouble("pressure")*0.75);

                views.setTextViewText(R.id.tvCity,"Город: "+ city);
                views.setTextViewText(R.id.tvTemperature, "Температура: " + mainObject.getDouble("temp") + " C");
                views.setTextViewText(R.id.tvHumidity, "Влажность: " + mainObject.getString("humidity") + " %");
                views.setTextViewText(R.id.tvPressure, "Давление: " + pressure + " мм.рт.ст.");

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                ComponentName componentName = new ComponentName(getApplicationContext(), WeatherWidget.class);
                appWidgetManager.updateAppWidget(componentName, views);

                Thread.sleep(200000);

                return Result.success();
            } else {
                return Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }

    }


}