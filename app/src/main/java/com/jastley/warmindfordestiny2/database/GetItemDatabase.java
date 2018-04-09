package com.jastley.warmindfordestiny2.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jastley.warmindfordestiny2.api.BungieAPI;
import com.jastley.warmindfordestiny2.database.models.Collectables;

import java.util.Iterator;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jamie on 6/4/18.
 */

public class GetItemDatabase extends AsyncTask<Context, Void, Boolean> {


    public interface AsyncResponse {
        void onAsyncDone();
    }

    public AsyncResponse delegate = null;

    public GetItemDatabase(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Boolean doInBackground(Context... contexts) {

        final Context context = contexts[0];

        SharedPreferences savedPrefs;
        savedPrefs = context.getSharedPreferences("saved_prefs", Context.MODE_PRIVATE);
        Boolean firstRun = savedPrefs.getBoolean("firstRun", true);

        if(firstRun){
    //        db = new DatabaseHelper(context);

            //final AppDatabase mDb = Room.databaseBuilder(context, AppDatabase.class, "bungieAccount.db").build();

            final CollectablesDAO mCollectibleDAO = AppDatabase.getAppDatabase(context).getCollectablesDAO();

    //        final CollectablesDAO mCollectibleDAO = mDb.getCollectablesDAO();

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();


            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl("https://destiny.plumbing/en/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build());

            Retrofit retrofit = builder.build();

            BungieAPI bungieClient = retrofit.create(BungieAPI.class);
            Call<JsonElement> getCollectablesManifest = bungieClient.getCollectablesDatabase();

            getCollectablesManifest.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                    JsonElement json = response.body();
                    JsonObject responseObj = (JsonObject) json;

                    for(Iterator iterator = responseObj.keySet().iterator(); iterator.hasNext();) {

                        final String key = (String)iterator.next();
                        JsonObject collectableObj = (JsonObject) responseObj.get(key);

                        //get each item definition and store as string
                        final String currentItemDefinition = collectableObj.toString();

                        try{
                            //db.insertAccountData("collectables", key, currentItemDefinition);

                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    Collectables collectable = new Collectables();
                                    collectable.setKey(key);
                                    collectable.setValue(currentItemDefinition);

                                    mCollectibleDAO.insert(collectable);
                                }
                            });
                        }
                        catch(Exception e){
                            System.out.println("error: " + e);
                        }
                    }

                    delegate.onAsyncDone();
//                    return null;
    //                db.close();
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Toast.makeText(context, "Couldn't update manifest database.", Toast.LENGTH_SHORT).show();
                    delegate.onAsyncDone();
                }
            });

//            getSharedPreferences("saved_prefs", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = savedPrefs.edit();

            try{
                editor.putBoolean("firstRun", false);
                editor.apply();
            }
            catch(Exception e){
                System.out.println("onCompleteSplash: " + e);
            }
//            return true;
        }
        else{
            delegate.onAsyncDone();
//            return true;
        }
//        return true;
        return true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

//    @Override
//    protected void onPostExecute(Void aVoid) {
//        super.onPostExecute(aVoid);
//
//        delegate.onComplete();
//    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

//          delegate.onAsyncDone(aBoolean);
    }


}
