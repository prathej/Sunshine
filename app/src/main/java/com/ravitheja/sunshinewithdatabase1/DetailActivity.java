/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ravitheja.sunshinewithdatabase1;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravitheja.sunshinewithdatabase1.data.WeatherContract;


public class DetailActivity extends FragmentActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, fragment)
                    .commit();
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        static final String DETAIL_URI = "URI";
        private Uri mUri;
        private static final String FORECAST_SHARE_HASTAG = "#SunshineApp";

        private ShareActionProvider shareActionProvider;
        private String mForecast;

        private static final int DETAIL_LOADER = 0;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        private static final String[] DETAIL_COLUMNS = {
                WeatherContract.WeatherEntry.TABLE_NAME+"." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                WeatherContract.WeatherEntry.COLUMN_DEGREES,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                // This works because the WeatherProvider returns location data joined with
                // weather data, even though they're stored in two different tables.
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
        };

        // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
        // must change.
        static final int COL_WEATHER_ID = 0;
        static final int COL_WEATHER_DATE = 1;
        static final int COL_WEATHER_DESC = 2;
        static final int COL_WEATHER_MAX_TEMP = 3;
        static final int COL_WEATHER_MIN_TEMP = 4;
        public static final int COL_WEATHER_HUMIDITY = 5;
        public static final int COL_WEATHER_PRESSURE = 6;
        public static final int COL_WEATHER_WIND_SPEED = 7;
        public static final int COL_WEATHER_DEGREES = 8;
        public static final int COL_WEATHER_CONDITION_ID = 9;


        private ImageView mIconView;
        private TextView mFriendlyDateView;
        private TextView mDateView;
        private TextView mDescriptionView;
        private TextView mHighTempView;
        private TextView mLowTempView;
        private TextView mHumidityView;
        private TextView mWindView;
        private TextView mPressureView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

           /* Intent intent = getActivity().getIntent();
            if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
                String forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                TextView messageText = (TextView)getView().findViewById(R.id.detail_text);
                messageText.setText(forecastStr);
            }*/
            // To recieve an URI
            Bundle arguments = getArguments();
            if(arguments!=null){
                mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            }

            View rootView = inflater.inflate(R.layout.fragment_detail,container,false);
            mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
            mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
            mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
            mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
            mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
            mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
            mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
            mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
            mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
                return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }


        void onLocationChanged( String newLocation ) {
            // replace the uri, since the location has changed
            Uri uri = mUri;
            if (null != uri) {
                long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
                Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
                mUri = updatedUri;
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);
           /* MenuItem menuItem = menu.findItem(R.id.action_share);

            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if(mForecast!=null){
                shareActionProvider.setShareIntent(createShareForecastIntent());
            }*/
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");

            if (mUri != null) {
            /*    Intent intent = getActivity().getIntent();
                if(intent == null || intent.getData() == null) {
                    return null;
                }*/

                return new CursorLoader(
                        getActivity(),
                        mUri,
                        DETAIL_COLUMNS,
                        null,
                        null,
                        null
                );
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(LOG_TAG,"In onLoadFinished");
/*
            if (!data.moveToFirst()) { return;}

                // Read date from cursor and update views for day of week and date
                String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));


                // Read description from cursor and update view
                String weatherDescription = data.getString(COL_WEATHER_DESC);


                // Read high temperature from cursor and update view
                boolean isMetric = Utility.isMetric(getActivity());

                double high = data.getDouble(COL_WEATHER_MAX_TEMP);
                String highString = Utility.formatTemperature(high, isMetric);


                // Read low temperature from cursor and update view
                double low = data.getDouble(COL_WEATHER_MIN_TEMP);
            String lowString = Utility.formatTemperature(low, isMetric);

                // We still need this for the share intent
                mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, highString, lowString);

         //   TextView detailTextView = (TextView) getView().findViewById(R.id.detail_text);
            mDescriptionView.setText(mForecast);

                // If onCreateOptionsMenu has already happened, we need to update the share intent now.
                if (shareActionProvider != null) {
                    shareActionProvider.setShareIntent(createShareForecastIntent());
                }*/

            if (data != null && data.moveToFirst()) {
                // Read weather condition ID from cursor
                int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

                // Use weather art image
                mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

                // Read date from cursor and update views for day of week and date
                long date = data.getLong(COL_WEATHER_DATE);
                String friendlyDateText = Utility.getDayName(getActivity(), date);
                String dateText = Utility.getFormattedMonthDay(getActivity(), date);
                mFriendlyDateView.setText(friendlyDateText);
                mDateView.setText(dateText);

                // Read description from cursor and update view
                String description = data.getString(COL_WEATHER_DESC);
                mDescriptionView.setText(description);

                // For accessibility, add a content description to the icon field
                mIconView.setContentDescription(description);

                // Read high temperature from cursor and update view
                boolean isMetric = Utility.isMetric(getActivity());

                double high = data.getDouble(COL_WEATHER_MAX_TEMP);
               // String highString = Utility.formatTemperature(getActivity(), high, isMetric);
                String highString = Utility.formatTemperature(getActivity(), high);
                mHighTempView.setText(highString);

                // Read low temperature from cursor and update view
                double low = data.getDouble(COL_WEATHER_MIN_TEMP);
               // String lowString = Utility.formatTemperature(getActivity(), low, isMetric);
                String lowString = Utility.formatTemperature(getActivity(), low);
                mLowTempView.setText(lowString);

                // Read humidity from cursor and update view
                float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
                mHumidityView.setText(getActivity().getString(R.string.format_humidity,humidity));

                // Read wind speed and direction from cursor and update view
                float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
                float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
                mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

                // Read pressure from cursor and update view
                float pressure = data.getFloat(COL_WEATHER_PRESSURE);
                mPressureView.setText(getActivity().getString(R.string.format_pressure,pressure));

                // We still need this for the share intent
                mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);

                // If onCreateOptionsMenu has already happened, we need to update the share intent now.
                if (shareActionProvider != null) {
                    shareActionProvider.setShareIntent(createShareForecastIntent());
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
             shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASTAG);
            return shareIntent;
        }
    }
}