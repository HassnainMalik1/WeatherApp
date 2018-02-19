package com.greentechnology.malikhassnain.weatherapp;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import Utils.Utils;
import data.CityPreferences;
import data.JSONWeatherParser;
import data.WeatherHttpClient;
import model.Weather;


public class MainActivity extends AppCompatActivity {

    private TextView cityName;
    private TextView temp;
    private ImageView iconView;
    private TextView description;
    private TextView humidity;
    private TextView pressure;
    private TextView wind;
    private TextView sunrise;
    private TextView sunset;
    private TextView updated;

     Weather weather = new Weather();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cityName = (TextView) findViewById(R.id.cityText);
        iconView = (ImageView) findViewById(R.id.thumbnailIcon);
        temp = (TextView) findViewById(R.id.tempText);
        description = (TextView) findViewById(R.id.cloudText);
        humidity = (TextView) findViewById(R.id.humidText);
        pressure = (TextView) findViewById(R.id.pressureText);

        wind = (TextView) findViewById(R.id.windText);
        sunrise = (TextView) findViewById(R.id.riseText);
        sunset = (TextView) findViewById(R.id.setText);
        updated = (TextView) findViewById(R.id.updateText);


        CityPreferences cityPreferences = new CityPreferences(MainActivity.this);


        renderWeatherData(cityPreferences.getCity());

    }

    public void renderWeatherData(String city){
        WeatherTask weatherTask = new WeatherTask();
        weatherTask.execute(new String[]{city + "&units=metric&appid=cd1c2692e957159c275b50b31cc0f570"});

    }

    private class DownloadImageAsyncTask extends AsyncTask<String, Void, Bitmap>{
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            iconView.setImageBitmap(bitmap);
            //super.onPostExecute(bitmap);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadImage(params[0]);
        }

        private Bitmap downloadImage(String code){
            final DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

            final HttpGet getRequest = new HttpGet(Utils.ICON_URL + code + ".png");
          //  final HttpGet getRequest = new HttpGet("https://lh5.ggpht.com/gCCgLdV9YOsiBHWa5EMZIB06hFSPPKUYCE_9MRlyv21Pjv0xIg02ayFEiDyfwCoAtSsj=w300-rw");
          try {

              HttpResponse response = defaultHttpClient.execute(getRequest);

              final int statusCode = response.getStatusLine().getStatusCode();
              if(statusCode != HttpStatus.SC_OK){
                  Log.e("DownloadImage", "Error" + statusCode);
                    return null;
              }

              final HttpEntity entity = response.getEntity();
              if(entity != null){
                  InputStream inputStream = null;

                  inputStream = entity.getContent();
                  //decode contents from the stream
                  final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                  return bitmap;
              }

          }catch (IOException e){
              e.printStackTrace();
          }

            return null;
          }
    }



    private class WeatherTask extends AsyncTask<String, Void , Weather>{

        @Override
        protected Weather doInBackground(String... params) {
            String data = ((new WeatherHttpClient()).getWeatherData(params[0]));

            weather.iconData = weather.currentCondition.getIcon();
            weather = JSONWeatherParser.getWeather(data);

            Log.v("Data ", weather.currentCondition.getDescription());


            new DownloadImageAsyncTask().execute(weather.iconData);


            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {

            super.onPostExecute(weather);

            DateFormat df = DateFormat.getTimeInstance();
            String sunriseDate = df.format(new Date(weather.place.getSunrise()));
            String sunsetDate = df.format(new Date(weather.place.getSunset()));
            String updateDate = df.format(new Date(weather.place.getLastupdate()));

            DecimalFormat decimalFormat = new DecimalFormat("#.#");//round it to 2 decimal
            String tempFormat = decimalFormat.format(weather.currentCondition.getTemperature());


            cityName.setText(weather.place.getCity() + ", " + weather.place.getCountry());
            temp.setText(""+tempFormat + "°C");
            humidity.setText("Humidity :" + weather.currentCondition.getHumidity() + "%");
            pressure.setText("Pressure :" + weather.currentCondition.getPressure() + " hpa");
            wind.setText("Wind :" + weather.wind.getSpeed() + "mps");
            sunrise.setText("Sunrise :" + sunriseDate);
            sunset.setText("Sunset :" + sunsetDate);
            updated.setText("Last Updated :" + updateDate);
            description.setText("  Condition :" + weather.currentCondition.getCondition() + "("
            + weather.currentCondition.getDescription() + " )");
        }
    }


    private void showInputDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Change City");

        final EditText cityInput = new EditText(MainActivity.this);
        cityInput.setInputType(InputType.TYPE_CLASS_TEXT);
        cityInput.setHint("Rawalpindi,PK");
        builder.setView(cityInput);
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CityPreferences cityPreferences = new CityPreferences(MainActivity.this);
                cityPreferences.setCity(cityInput.getText().toString());

                String newCity = cityPreferences.getCity();
                renderWeatherData(newCity);
            }
        });
        builder.show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.change_cityId) {
            showInputDialog();

        }

        return super.onOptionsItemSelected(item);
    }
}
