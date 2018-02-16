package me.palmz.redtube.redtube;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private EditText edtSearch;
    private Button btnSubmit;
    private TextView tvResult;

    private LinearLayout llContainer;
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        edtSearch = (EditText) findViewById(R.id.edtSearch);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        llContainer = (LinearLayout) findViewById(R.id.llContainer);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONParse().execute();
            }

        });

    }

    private class JSONParse extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        @Override
        protected JSONObject doInBackground(String... args) {
            HttpsURLConnection con = null;
            try {
                URL u = new URL("https://api.redtube.com/?data=redtube.Videos.searchVideos&output=json&search=" + edtSearch.getText() + "&tags[]=Teen&thumbsize=medium");
                con = (HttpsURLConnection) u.openConnection();

                con.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();

                JSONObject res = new JSONObject(sb.toString());
                return res;

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    try {
                        con.disconnect();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            llContainer.removeAllViews();
            try {
                JSONArray videos = json.getJSONArray("videos");
                LinearLayout layout = new LinearLayout(context);

                for (int i = 0; i < videos.length(); i++) {
                    JSONObject v = (videos.getJSONObject(i)).getJSONObject("video");
                    Log.d("Debug", "onPostExecute: " + v.toString());

                    if (i % 4 == 0 || i == videos.length() - 1) {
                        layout = new LinearLayout(context);
                        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        llContainer.addView(layout);
                    }

                    ImageView iv = new ImageView(context);

                    new DownloadImageTask(iv)
                            .execute(v.getString("default_thumb"));

                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    param.weight = 1;
                    iv.setLayoutParams(param);
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    layout.addView(iv);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
            ImageView bmImage;

            public DownloadImageTask(ImageView bmImage) {
                this.bmImage = bmImage;
            }

            protected Bitmap doInBackground(String... urls) {
                String urldisplay = urls[0];
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return mIcon11;
            }

            protected void onPostExecute(Bitmap result) {
                bmImage.setImageBitmap(result);
            }
        }
    }
}
