package com.example.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.coolweather.R;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

/**
 * Created by Administrator on 2016/2/5 0005.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {
    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;
    private TextView publishText;
    private TextView weatherDespText;
    private TextView temp1Text;
    private TextView temp2Text;
    private TextView currentDateText;
    private Button switchCity;
    private Button refreshWeather;

    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.weather_layout);
        weatherInfoLayout =(LinearLayout)findViewById(R.id.weather_info_layout);
        cityNameText = (TextView)findViewById(R.id.city_name);
        publishText =(TextView)findViewById(R.id.publish_text);
        weatherDespText = (TextView)findViewById(R.id.weather_desp);
        temp1Text = (TextView)findViewById(R.id.temp1);
        temp2Text =(TextView)findViewById(R.id.temp2);
        currentDateText =(TextView)findViewById(R.id.current_date);
        switchCity = (Button)findViewById(R.id.switch_city);
        refreshWeather = (Button)findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
        String countyCode = getIntent().getStringExtra("county_code");
        if (countyCode != null){
            Log.d("yes", "county in weatherActivity"+countyCode);
        }else {Log.d("yes", "countyCode is null");}

        if (!TextUtils.isEmpty(countyCode)){
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            Log.d("yes", "before queryWeatherCode");
            queryWeatherCode(countyCode);
        }else {
// 没有县级代号时就直接显示本地天气
            Log.d("yes", "no countyCode");
            showWeather();
        }
    }

    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" +countyCode + ".xml";
        Log.d("yes",address);
        queryFromServer(address, "countyCode");
    }

    private void queryFromServer(final String address, final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

            @Override
            public void onFinish(final String response) {
                Log.d("yes", "onFinish");
                if ("countyCode".equals(type)) {
                    //String[] array = response.split("\\|");
                    //if (array != null && array.length == 2) {
                      //  String weatherCode = array[1];
                        //queryWeatherInfo(weatherCode);
                    //}
                    if (!TextUtils.isEmpty(response)) {
// 从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                            Log.d("fuck", "countyCode");
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                            Log.d("fuck", "weatherCode");
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("fail update");
                    }
                });

            }
        });
    }

    private void queryWeatherInfo(String weatherCode){
        String address = "http://www.weather.com.cn/data/cityinfo/" +weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    private void showWeather(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("fuck", "showWeather");
        cityNameText.setText(sharedPreferences.getString("city_name", ""));
        temp1Text.setText(sharedPreferences.getString("temp1", ""));
        temp2Text.setText(sharedPreferences.getString("temp2", ""));
        currentDateText.setText(sharedPreferences.getString("current_date", ""));
        weatherDespText.setText(sharedPreferences.getString("weather_desp", ""));
        publishText.setText("今天" + sharedPreferences.getString("publish_time", "") + "发布");
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.switch_city:
                Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                Log.d("yes","switch_city, to ChooseAreaActivity");
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = sharedPreferences.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }
}
