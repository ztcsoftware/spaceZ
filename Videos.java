package com.ztcsoftware.lef.spacez;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ztcsoftware.lef.spacez.model.Video;
import com.ztcsoftware.lef.spacez.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class Videos  extends AppCompatActivity {
    SwipeRefreshLayout pullToRefresh;
    ListView lstViewVideos;
    GridView gridView;
    ImageView noContent;
    TextView tutorialTitle;
    Video videoSelected = null;
    List<Video> videos = new ArrayList<>();
    String username1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_videos);
        setContentView(R.layout.activity_grid_video);
        getSupportActionBar().hide();
        gridView = findViewById(R.id.gridview);
        noContent = findViewById(R.id.noContent);
        tutorialTitle = findViewById(R.id.tutorialTitle);
        pullToRefresh = findViewById(R.id.refreshGridViewList);

        //Reset List of object videos so every time who execute Video class start the list from zero.
        if(!videos.isEmpty())
                    videos.clear();

        final String userID = getIntent().getStringExtra("userid");
        final String name = getIntent().getStringExtra("lastname");
        final String vidtuttitle = getIntent().getStringExtra("vidtuttitle");
        username1 = getIntent().getStringExtra("username");
        tutorialTitle.setText(vidtuttitle);
        RequestParams params = new RequestParams();
//        params.add("user",userID);
//        params.add("picktutorial",vidtuttitle);
        ManagerNetwork.postVideosPerTut(userID,vidtuttitle,params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                gridView.setAdapter(new VideosPerTutAdapter(response, Videos.this));

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        videoSelected = videos.get(position + 1);
                        Intent intent = new Intent(getApplication(), Content.class);
                        intent.putExtra("userid", userID);
                        intent.putExtra("videotitle", videoSelected.getVideotitle());
                        intent.putExtra("picktutorial", videoSelected.getPicktutorial());
                        intent.putExtra("filename", videoSelected.getVideofilename());
                        intent.putExtra("videotitle", videoSelected.getVideotitle());
                        intent.putExtra("videodiscription", videoSelected.getVideotitle());
                        intent.putExtra("username", username1);
                        intent.putExtra("vidtuttitle", vidtuttitle);
                        intent.putExtra("videoId", videoSelected.get_id());
                      //  Toast.makeText(getApplicationContext(), "position : " + position + "filename" + videoSelected.getVideofilename(), Toast.LENGTH_LONG).show();
                        startActivity(intent);
                    }
                });

                gridView.setEmptyView(findViewById(R.id.noContent));

            }
        });

       // nameUser.setText(name);
        //Read data from videos
       // new GetData().execute(Common.getAddressVideosAPI(userID));
       // new GetData().execute(Common.getAddressVideosPerVideoTutorialTitleAPI(userID,vidtuttitle));

        //Refresh gridView list
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Read data from videos
                //new GetData().execute(Common.getAddressVideosAPI(userID));
               // new GetData().execute(Common.getAddressVideosPerVideoTutorialTitleAPI(userID, vidtuttitle));
                ManagerNetwork.postVideosPerTut(userID,vidtuttitle,params,new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);

                        gridView.setAdapter(new VideosPerTutAdapter(response, Videos.this));

                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                                videoSelected = videos.get(position + 1);
                                Intent intent = new Intent(getApplication(), Content.class);
                                intent.putExtra("userid", userID);
                                intent.putExtra("videotitle", videoSelected.getVideotitle());
                                intent.putExtra("picktutorial", videoSelected.getPicktutorial());
                                intent.putExtra("filename", videoSelected.getVideofilename());
                                intent.putExtra("videotitle", videoSelected.getVideotitle());
                                intent.putExtra("videodiscription", videoSelected.getVideotitle());
                                intent.putExtra("username", username1);
                                intent.putExtra("vidtuttitle", vidtuttitle);
                                intent.putExtra("videoId", videoSelected.get_id());
                                //  Toast.makeText(getApplicationContext(), "position : " + position + "filename" + videoSelected.getVideofilename(), Toast.LENGTH_LONG).show();
                                startActivity(intent);
                            }
                        });

                        gridView.setEmptyView(findViewById(R.id.noContent));

                    }
                });
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    public class VideosPerTutAdapter extends BaseAdapter {
        private final JSONArray jsonArray;
        private final Activity activity;

        public VideosPerTutAdapter(JSONArray jsonArray, Activity activity) {
            this.jsonArray = jsonArray;
            this.activity = activity;
        }

        @Override
        public int getCount() {
            if (null == jsonArray) return 0;
            return jsonArray.length();
        }

        @Override
        public JSONObject getItem(int position) {
            if(null == jsonArray) return null;
            JSONObject x = null;
            try{
                x=jsonArray.getJSONObject(position);
            }catch (JSONException e){
                e.printStackTrace();
            }
            return x;
        }

        @Override
        public long getItemId(int position) {
            JSONObject jsonObject = getItem(position);
            return jsonObject.optLong("id");
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if(view ==null){
                view = activity.getLayoutInflater().inflate(R.layout.row_thumb_video,null);
            }

            ImageView image = view.findViewById(R.id.image);
            TextView title = view.findViewById(R.id.infothumb);

            JSONObject jsondata = getItem(position);
            try{

                Video video = new Video();
                Uri filepath = Uri.parse(Constants.BASE_URL +"/uploaded_videos/" + username1 + "/" + jsondata.getString("videofilename"));
                //user.set_id(jsondata.getString("_id"));
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(filepath)
                        .into(image);

                title.setText(jsondata.getString("videotitle"));
                video.setVideotitle(jsondata.getString("videotitle"));
                video.setVideofilename(jsondata.getString("videofilename"));
                video.setPicktutorial(jsondata.getString("picktutorial"));

                videos.add(video);
            }catch (JSONException e){
                e.printStackTrace();
            }
            return view;
        }
    }
}
