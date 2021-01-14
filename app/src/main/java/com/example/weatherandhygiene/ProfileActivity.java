package com.example.weatherandhygiene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.FormatFlagsConversionMismatchException;

public class ProfileActivity extends AppCompatActivity implements SensorEventListener {

    //firebase auth object
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private static final String OPEN_WEATHER_MAP_API_KEY = "c652d01a2e69210e999282ebb53fcf44";
    public static final String EXTRA_MESSAGE = "com.example.weatherandhygiene.MESSAGE";

    EditText editTxt_city_search;

    TextView txtView_temp;
    TextView txtView_pressure;
    TextView txtView_humidity;
    TextView txtView_windSpeed;
    TextView txtView_cityname;
    TextView txtView_weather_desc;
    ImageView weatherIcon;
    TextView txtView_todaysdate;
    TextView txtView_sunrise;
    TextView txtView_sunset;

    private SensorManager sensorManager;
    private Sensor sensor;
    private long lastUpdateTime;
    private static float SHAKE_THRESHOLD_GRAVITY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //shake event sensor code
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        lastUpdateTime = System.currentTimeMillis();

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        //get current user
        currentUser = firebaseAuth.getCurrentUser();

        //get weather data
        editTxt_city_search = (EditText) findViewById(R.id.editText_city_search);

        txtView_temp = (TextView)findViewById(R.id.textView_display_temp);
        txtView_pressure = (TextView)findViewById(R.id.textView_display_pressure);
        txtView_humidity = (TextView)findViewById(R.id.textView_display_humidity);
        txtView_windSpeed = (TextView)findViewById(R.id.textView_display_wind);
        txtView_cityname = (TextView)findViewById(R.id.textView_cityname);
        txtView_weather_desc = (TextView)findViewById(R.id.textView_weather_description);
        weatherIcon = (ImageView)findViewById(R.id.imageView_weatherIcon);
        txtView_sunrise = (TextView)findViewById(R.id.textView_display_sunrise);
        txtView_sunset= (TextView)findViewById(R.id.textView_display_sunset);

        //update time every second to display accurate time on screen
        Thread thread = new Thread(){
            @Override
            public void run(){
                try
                {
                    while(!isInterrupted()){
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //get current date and time
                                txtView_todaysdate = (TextView)findViewById(R.id.textView_todays_date);
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM   h:mm:ss a ");
                                String dateTime = simpleDateFormat.format(calendar.getTime());
                                txtView_todaysdate.setText(dateTime);
                            }
                        });
                    }
                }catch(InterruptedException e){

                }
            }
        };
        thread.start();

        String q = "London";
        String units = "metric";
        //double lat = 51.51, lon = -0.13;
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=%s",
                q,units, OPEN_WEATHER_MAP_API_KEY);
        new GetWeatherTask().execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //get the profile menu items
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        MenuItem subitem1 = menu.findItem(R.id.subitem1);

        if(currentUser!=null){
            subitem1.setTitle("Logout as " + currentUser.getEmail());
        }else{
            subitem1.setTitle("Logout as guest user");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.subitem1:

                //logging out the user
                firebaseAuth.signOut();

                //closing activity
                finish();

                //starting login activity
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    //method to check current activity open
    @Override
    public void onResume() {
        super.onResume();
        //register this class as a listener for the orientation and
        //accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    //method to check current activity closed
    @Override
    public void onPause() {
        //unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void searchCity(View view){

        String q = editTxt_city_search.getText().toString();
        String units = "metric";
        //double lat = 51.51, lon = -0.13;
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=%s",
                q,units, OPEN_WEATHER_MAP_API_KEY);

            new GetWeatherTask().execute(url);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            getAccelerometer(event);
        }
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        //movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float gX = x / sensorManager.GRAVITY_EARTH;
        float gY = y / sensorManager.GRAVITY_EARTH;
        float gZ = z / sensorManager.GRAVITY_EARTH;

        //gForce will be close to 1 when there is no movement.
        float gForce = (float)Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        long currentTime = System.currentTimeMillis();
        if(gForce >= SHAKE_THRESHOLD_GRAVITY){

            if(currentTime - lastUpdateTime < 200){
                return;
            }
            lastUpdateTime = currentTime;
            Toast.makeText(this, "Device was shaken", Toast.LENGTH_SHORT).show();

            //from profile activity go to establishments
            Intent intent = new Intent(this, EstablishmentsActivity.class);
            EditText editText = (EditText) findViewById(R.id.editText_city_search);
            String message = editText.getText().toString();
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class GetWeatherTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            String temp = "UNDEFINED";
            String pressure = "UNDEFINED";
            String humidity = "UNDEFINED";
            String windSpeed = "UNDEFINED";
            String weatherDesc = "UNDEFINED";
            String city = "UNDEFINED";
            String imgIcon = "UNDEFINED";
            String sunrise = "UNDEFINED";
            String sunset = "UNDEFINED";

            String retrieveWeather = "UNDEFINED";

            try{
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while((inputString = bufferedReader.readLine()) != null){
                    builder.append(inputString);
                }

                JSONObject topLevel = new JSONObject(builder.toString());
                JSONObject main = topLevel.getJSONObject("main");
                JSONObject wind = topLevel.getJSONObject("wind");
                JSONArray weather = topLevel.getJSONArray("weather");
                JSONObject sys = topLevel.getJSONObject("sys");

                for(int i = 0; i < weather.length(); i++){
                    JSONObject weather1 = weather.getJSONObject(i);
                    weatherDesc = weather1.getString("description");
                    imgIcon = weather1.getString("icon");

                }

                double temperature = main.getDouble("temp");
                double tempRoundedDouble = Math.round(temperature);
                int tempRoundedInt = (int) tempRoundedDouble;
                temp = String.valueOf(tempRoundedInt);
                pressure = String.valueOf(main.getInt("pressure"));
                humidity = String.valueOf(main.getInt("humidity"));
                windSpeed = String.valueOf(wind.getDouble("speed"));
                city = topLevel.getString("name");
                sunrise = sys.getString("sunrise");
                sunset = sys.getString("sunset");

                retrieveWeather = temp + "/" + pressure + "/" + humidity + "/"
                        + windSpeed + "/" + weatherDesc + "/" + city + "/"
                        + imgIcon + "/" + sunrise + "/" + sunset;

                urlConnection.disconnect();
            }catch (IOException | JSONException e){
                e.printStackTrace();
            }
            return retrieveWeather;
        }

        @Override
        protected void onPostExecute (String weather){

            String temp = "";
            String pressure = "";
            String humidity = "";
            String windSpeed = "";
            String weatherDesc = "";
            String city = "";
            String imgIcon = "";
            String EpochSunset = "";
            String EpochSunrise = "";

            try {
                String[] details = weather.split("/");
                temp = details[0];
                pressure = details[1];
                humidity = details[2];
                windSpeed = details[3];
                weatherDesc = details[4];
                city = details[5];
                imgIcon = details[6];
                EpochSunset = details[7];
                EpochSunrise = details[8];



            /*convert wind from metres per second to mph and round it to nearest int
            double windSpeedMetresPerSecond;
            try {
                windSpeedMetresPerSecond = Double.parseDouble(windSpeed);
            }
            catch (NumberFormatException e)
            {
                windSpeedMetresPerSecond = 0;
            }
            double windSpeedMphDouble = (windSpeedMetresPerSecond * (2.23693629));
            double windspeed = Math.round(windSpeedMphDouble);

            */

            int sunrise = Integer.parseInt(EpochSunrise);
            int sunset = Integer.parseInt(EpochSunset);
            String sunriseFinal = new java.text.SimpleDateFormat("h:mm a").format(new java.util.Date (sunrise
                    *1000));
            String sunsetFinal = new java.text.SimpleDateFormat("h:mm a").format(new java.util.Date (sunset
                    *1000));

            String tempDisplay = String.format("%s°", temp);
            String pressureDisplay = String.format("%s hpa", pressure);
            String humidityDisplay = String.format("%s %%" , humidity);
            String windDisplay = String.format("%s m/s", windSpeed);
            String weatherImage = String.format("http://openweathermap.org/img/wn/%s@2x.png", imgIcon);

            txtView_temp.setText(tempDisplay);
            txtView_pressure.setText(pressureDisplay);
            txtView_humidity.setText(humidityDisplay);
            txtView_windSpeed.setText(windDisplay);
            txtView_weather_desc.setText(weatherDesc);
            txtView_cityname.setText(city);
            loadImageFromUrl(weatherImage);
            txtView_sunrise.setText(sunriseFinal);
            txtView_sunset.setText(sunsetFinal);

            }catch (ArrayIndexOutOfBoundsException a){
                Toast.makeText(ProfileActivity.this, " No city found.",
                        Toast.LENGTH_LONG).show();
            }catch (NumberFormatException n){
                Toast.makeText(ProfileActivity.this, " No city found.",
                        Toast.LENGTH_LONG).show();
            }

        }

        private void loadImageFromUrl(String url){
            Picasso.with(getBaseContext()).load(url).fit().centerInside().into(weatherIcon);
        }


    }


}