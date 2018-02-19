package data;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by MalikHassnain on 9/22/2017.
 */

public class CityPreferences {

    SharedPreferences preferences;

    public CityPreferences(Activity activity){
        preferences = activity.getPreferences(Activity.MODE_PRIVATE);

    }

    public String getCity(){
        return preferences.getString("city","Rawalpindi,PK");

    }

    public void setCity (String city){

        preferences.edit().putString("city",city).commit();

    }

}

