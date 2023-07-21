package com.ztcsoftware.lef.spacez;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
//import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ztcsoftware.lef.spacez.model.Comment;
import com.ztcsoftware.lef.spacez.model.Video;
import com.ztcsoftware.lef.spacez.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class Content extends AppCompatActivity  {

    public static final String PREFS_NAME = "LOGIN";

    PlayerView playerView;

    List<Video> videos = new ArrayList<>();
   // List<Vote> votes = new ArrayList<>();
    List<Comment> comments = new ArrayList<>();

    ListView lstComments;
    TextView addComment,numberOfLikes;
    ImageView download,like;
    ProgressDialog progressDialog,mProgressDialog;
    SwipeRefreshLayout pullToRefresh;

    Video videoSelected = null;
    Comment commentSelected = null;
    static String username1;
    String name,idGuest;
    int counter=0,i=0;
    Boolean flag=false,voteResult,flagResult;
    SharedPreferences settings;
    String myDate,numOfLikes,vidtuttitle;
    static String filename;
    String mVideoUrl;
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        getSupportActionBar().hide();

        //Read current date and formatted for local user settings
        Date date = Calendar.getInstance().getTime();
        myDate = DateFormat.getDateInstance(DateFormat.FULL).format(date);

        filename                  = getIntent().getStringExtra("filename");
        final String userID       = getIntent().getStringExtra("userid");
        username1                 = getIntent().getStringExtra("username");
        vidtuttitle               = getIntent().getStringExtra("vidtuttitle");
        final String vidtitle     = getIntent().getStringExtra("videotitle");
        final String viddescr     = getIntent().getStringExtra("videodescription");
        final String picktutorial = getIntent().getStringExtra("picktutorial");
        final String videoId      = getIntent().getStringExtra("videoId");

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        //load counter and returns the name of person who voted for first time

        TextView title = findViewById(R.id.vidtitle);
        TextView description = findViewById(R.id.viddescr);
        addComment = findViewById(R.id.comments);
        download = findViewById(R.id.download);
        like = findViewById(R.id.like);
        numberOfLikes = findViewById(R.id.numberOfLikes);
        lstComments = findViewById(R.id.listOfComments);
        pullToRefresh = findViewById(R.id.refreshCommentList);


        getDataLikes(filename);

        //load comments if exists for current video
        //new GetDataComments().execute(Common.getAddressCommentsPerVideo(filename));
        comments.clear();
        loadComments(filename);

        settings = getApplication().getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        idGuest = settings.getString("idGuest",null); // id of guest who logged in
        name = settings.getString("name",null);       // name of guest who logged in

        if(name!=null) {
            //Read if logged in guest has already voted
            // new GetDataVotes().execute(Common.getAddressResultVottingAPI(name,filename));
            RequestParams params1 = new RequestParams();
            params1.add("guest",name);
            params1.add("videofilename",filename);

            ManagerNetwork.postGuestVotes(params1, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        voteResult = response.getBoolean("voted");
                        flagResult = response.getBoolean("lastaction");
                    }catch (JSONException e){
                        e.printStackTrace();
                    }


                }
            } );

        }

        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name == null) {
                    Toast.makeText(getApplicationContext(), "LogIn first", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                                AlertDialog.Builder mBuilder = new AlertDialog.Builder(Content.this);
                                View mView = getLayoutInflater().inflate(R.layout.dialog_comment, null);
                                EditText mInsertComment = mView.findViewById(R.id.editComment);
                                Button mAddComment = mView.findViewById(R.id.add);
                                Button mCancel = mView.findViewById(R.id.cancel);
                                mBuilder.setView(mView);
                                AlertDialog dialog = mBuilder.create();
                                mAddComment.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        if (mInsertComment.getText().toString().length() > 181) {
                                            Toast.makeText(getApplicationContext(), "Your comment must be less 180 characters", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        if (!mInsertComment.getText().toString().isEmpty()) {
                                            // new PostDataComments(name, filename, mInsertComment.getText().toString(),myDate).execute(Common.getAddressCommentsAPI());
                                            addNewComment(name,filename, mInsertComment.getText().toString(),myDate);
                                            Toast.makeText(getApplicationContext(), "Your comment submitted", Toast.LENGTH_SHORT).show();
                                            dialog.cancel();
                                            //load comments
                                            loadComments(filename);
                                            //load likes
                                            // new GetDataLikes().execute(Common.getAddressVideosAPI(filename));
                                            getDataLikes(filename);
                                        } else
                                            Toast.makeText(getApplicationContext(), "Your comment is empty", Toast.LENGTH_SHORT).show();


                                    }
                                });

                                dialog.show();
                                mCancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.cancel();
                                    }
                                });
                }
            }
        });

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(name==null){
                    Toast.makeText(getApplicationContext(),"LogIn first",Toast.LENGTH_SHORT).show();
                    return;
                }else{

                    if(!voteResult) {
                        if (!flag) {
                            counter++;
                            flag = true;
                            numberOfLikes.setText(String.valueOf(counter));
                            //find the current video and save like to server
                            putDataLike(counter,filename);
                            //Save the vote of guest

                            postDataVotesFirst(name,filename,true,true);
                            //update flag value

                            putDataVotes(true,name, filename);
                        } else {
                            if (counter == 0) return;
                            counter--;
                            flag = false;
                            numberOfLikes.setText(String.valueOf(counter));
                            //save like to server

                            putDataLike(counter,filename);
                            //update flag value

                            putDataVotes(false,name, filename);
                        }
                    }else{

//                        //read flag state after the guest 1st vote and logged in for 2nd time
//                        new GetDataFlag().execute(Common.getAddressResultVottingAPI(name,filename));
//                        //Receiver for DataVotes Flag
//                        LocalBroadcastManager.getInstance(Content.this).registerReceiver(mMessageReceiverVotesFlag,
//                                new IntentFilter("flag-result"));
                        if(!flagResult) {
                            counter++;
                            flag = true;
                            numberOfLikes.setText(String.valueOf(counter));
                            //find the current video and save like to server
                            putDataLike(counter,filename);
                            //update flag value
                            putDataVotes(true,name, filename);

                            flagResult = true;
                           // Toast.makeText(getApplicationContext(),"flagResult ++ "+flagResult,Toast.LENGTH_SHORT).show();

                        }else {
                            if (counter == 0) return;
                            counter--;
                            flag = false;
                            numberOfLikes.setText(String.valueOf(counter));
                            //save like to server

                            putDataLike(counter,filename);

                            //update flag value
                            putDataVotes(false, name, filename);


                            flagResult = false;
                          //  Toast.makeText(getApplicationContext(),"flagResult -- "+flagResult,Toast.LENGTH_SHORT).show();

                        }
                    }
                }

            }

        });

        //edit comments
        lstComments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (name == null) {
                    Toast.makeText(getApplicationContext(), "LogIn first", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    commentSelected = comments.get(position);
                    //Read the comment body for editing or deleting
                    if(commentSelected.getGuest().equals(name)) {
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(Content.this);
                        View mView = getLayoutInflater().inflate(R.layout.edit_dialog_comment, null);
                        EditText updateText = mView.findViewById(R.id.editLoadComment);
                        Button mUpdateComment = mView.findViewById(R.id.update);
                        Button mDelete = mView.findViewById(R.id.delete);
                        Button mCancel = mView.findViewById(R.id.cancel);

                        updateText.setText(commentSelected.getBody());

                        mBuilder.setView(mView);
                        AlertDialog dialog = mBuilder.create();
                        mUpdateComment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //update main body with updated text
                                updateComment(commentSelected.get_id() ,updateText.getText().toString(),myDate);

                                //load comments if exists for current video
                                loadComments(filename);
                                dialog.cancel();
                            }
                        });

                        mDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Delete comment
                                deleteComment(commentSelected.get_id());

                                //load comments if exists for current video
                                loadComments(filename);

                                dialog.cancel();
                            }
                        });

                        dialog.show();
                        mCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.cancel();
                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext(),"Only your comments you can edit/delete",Toast.LENGTH_SHORT ).show();
                        return;
                    }
                }
            }
        });

        title.setText(vidtitle);
        description.setText(viddescr);

        playerView = findViewById(R.id.playerView);



        /////////////////////////////////// REPLACE THE OLD CODE //////////////////////////////////////////

        //Create the player
        ExoPlayer player = new ExoPlayer.Builder(Content.this).build();

        // Bind the player to the view.
        playerView.setPlayer(player);

        mVideoUrl = Constants.BASE_URL +"/uploaded_videos/" + username1 + "/" + filename;

        // Build the media item.
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(mVideoUrl));

        // Set the media item to be played.
        player.setMediaItem(mediaItem);

        // Prepare the player.
        player.prepare();

        // Start the playback.
        player.play();

        /////////////////////////////////// REPLACE THE OLD CODE //////////////////////////////////////////

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Download Video");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name == null) {
                    Toast.makeText(getApplicationContext(), "LogIn first", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    // execute this when the downloader must be fired
                    final DownloadVideo downloadTask = new DownloadVideo(Content.this);
                    // the url to the downloaded file
                    downloadTask.execute(mVideoUrl);

                    mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            downloadTask.cancel(true); //cancel the task
                        }
                    });
                }
            }
        });

        //pull to refresh comments list
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadComments(filename);
                pullToRefresh.setRefreshing(false);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
            ExoPlayerViewManager.getInstance(Uri.parse(mVideoUrl)).goToForeground();
    }

    @Override
    public void onPause() {
        super.onPause();
        ExoPlayerViewManager.getInstance(Uri.parse(mVideoUrl)).goToBackground();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ExoPlayerViewManager.getInstance(Uri.parse(mVideoUrl)).releaseVideoPlayer();
    }
    private void setupPlayerView(final PlayerView videoView, final String videoUrl) {
        ExoPlayerViewManager.getInstance(Uri.parse(videoUrl)).prepareExoPlayer(getApplicationContext(), videoView);
        ExoPlayerViewManager.getInstance(Uri.parse(videoUrl)).goToForeground();

        View controlView = videoView.findViewById(R.id.exo_controller);
        controlView.findViewById(R.id.exo_fullscreen_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), FullscreenVideoActivity.class);
                        intent.putExtra(ExoPlayerViewManager.EXTRA_VIDEO_URI, videoUrl);
                        startActivity(intent);
                    }
                });
    }


    //comment methods
    private void addNewComment(String name, String filename, String body, String date){
        RequestParams params = new RequestParams();
//        params.add("guest",name);
//        params.add("videofilename",filename);
//        params.add("body",body);
//        params.add("insertdate",date);
        ManagerNetwork.postAddComment(name,filename,body,date,params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try{
                    String message = response.getString("message");
                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();


                }catch (JSONException ex){

                }
            }
        });

    }
    private void updateComment(String commentId, String text, String date){
        RequestParams params = new RequestParams();
        params.add("_id",commentId);
        params.add("body",text);
        params.add("insertdate",date);
        ManagerNetwork.putUpdateComment(params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try{
                    String message = response.getString("message");
                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();


                }catch (JSONException ex){

                }
            }
        } );
    }
    private void deleteComment(String commentId){
        RequestParams params = new RequestParams();
     //   params.add("_id",commentId);
        ManagerNetwork.deleteComment(commentId,params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try{
                    String message = response.getString("message");
                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

                }catch (JSONException ex){

                }
            }
        } );
    }
    private void loadComments(String filename) {
        RequestParams params = new RequestParams();
        // params.add("videofilename",filename); i pass filename with JWT encrypted
        ManagerNetwork.postVideoComments(filename,params,new JsonHttpResponseHandler(){
            @Override
            public void onStart() {
                progressDialog = new ProgressDialog(Content.this);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                lstComments.setAdapter(new CommentAdapter(response,Content.this));
                Log.i("onSuccess", "success");
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //  Log.d("error_found",errorResponse.toString());
                Log.i("statusCode", String.valueOf(statusCode));
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Cannot load comment, reload page"+statusCode,Toast.LENGTH_LONG).show();

            }
            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
            }
        });
    }

    //like methods
    private void postDataVotesFirst(String name, String filename, Boolean voted, Boolean lastaction){
        RequestParams params = new RequestParams();
        params.add("guest",name);
        params.add("videofilename",filename);
        params.add("voted", String.valueOf(voted));
        params.add("lastaction", String.valueOf(lastaction));

        ManagerNetwork.postDataVotes(params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    String message = response.getString("message");
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    private void putDataLike(int counter, String filename){
        RequestParams params = new RequestParams();
        params.put("likes",counter);
        params.add("videofilename",filename);
        ManagerNetwork.putVideoLike(params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try{
                    String message = response.getString("message");
                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();


                }catch (JSONException ex){
                    ex.printStackTrace();
                }
            }
        });
    }
    private void putDataVotes(Boolean choice,String guest, String filename){
        RequestParams params = new RequestParams();
        params.put("lastaction",choice);
        params.put("guest",guest);
        params.add("videofilename",filename);
        ManagerNetwork.putDataVotes(params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try{
                    String message = response.getString("message");
                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                }catch (JSONException ex){
                    ex.printStackTrace();
                }
            }
        });
    }
    private void getDataLikes(String filename){
        RequestParams params = new RequestParams();
       // params.add("videofilename",filename);
        ManagerNetwork.postVideoLikes(filename,params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    numOfLikes = response.getString("likes");
                }catch (JSONException e){
                    e.printStackTrace();
                }
                numberOfLikes.setText(numOfLikes);
                counter = Integer.valueOf(numOfLikes);
//                likeSettings = getApplicationContext().getSharedPreferences(PREFS_LIKES, Context.MODE_PRIVATE);
//
//                likeEditor = likeSettings.edit();
//                likeEditor.putString("likes",numOfLikes);
//                likeEditor.apply();
            }
        });
    }



    //inner class adapters
    public class CommentAdapter extends BaseAdapter {
        private final JSONArray jsonArray;
        private final Activity activity;

        public CommentAdapter(JSONArray jsonArray, Activity activity) {
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

            return jsonObject.optLong("_id");
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if(view ==null){
                view = activity.getLayoutInflater().inflate(R.layout.row_comment,null);
            }
            TextView guest = view.findViewById(R.id.guestName);
            TextView date = view.findViewById(R.id.insertDate);
            TextView body = view.findViewById(R.id.mainBody);
            JSONObject jsondata = getItem(position);

            Comment comment = new Comment();

            try{
                comment.set_id(jsondata.getString("_id"));

                comment.setGuest(jsondata.getString("guest"));
                guest.setText(jsondata.getString("guest"));

                comment.setInsertdate(jsondata.getString("insertdate"));
                date.setText(jsondata.getString("insertdate"));

                comment.setBody(jsondata.getString("body"));
                body.setText(jsondata.getString("body"));

                comments.add(comment);

            }catch (JSONException e){
                e.printStackTrace();
            }

            return view;
        }
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
                Glide.with(getApplicationContext()).load(filepath).into(image);

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

    //inner class model singleton class
    public static class ExoPlayerViewManager extends Content{
        private static final String TAG = "ExoPlayerViewManager";
        public static final String EXTRA_VIDEO_URI = "video_uri";
        public  static ExoPlayerViewManager instance;
        private Uri videoUri;
        // replaced
        // private SimpleExoPlayer player;
        private ExoPlayer player;
        private boolean isPlayerPlaying;

        public static ExoPlayerViewManager getInstance(Uri videoUri) {
            if (instance == null) {
                instance = new ExoPlayerViewManager(videoUri);
            }
            return instance;
        }

        private ExoPlayerViewManager(Uri videoUri) {
            this.videoUri = videoUri;
        }

        public void prepareExoPlayer(Context context, PlayerView exoPlayerView) {

            if (context == null || exoPlayerView == null) {
                return;
            }
            if (player == null) {
                // Create a new player if the player is null or
                // we want to play a new video

                //new
                player = new ExoPlayer.Builder(exoPlayerView.getContext()).build();

                // replaced
                    // player = ExoPlayerFactory.newSimpleInstance(exoPlayerView.getContext());

                // Do all the standard ExoPlayer code here...
                // Produces DataSource instances through which media data is loaded.

                // replaced
                    // DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("spaceZ");

                String mVideoUrl = Constants.BASE_URL +"/uploaded_videos/" + username1 + "/" + filename;

                // This is the MediaSource representing the media to be played.
                // replaced
                    // MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    //        .createMediaSource(Uri.parse(mVideoUrl));

                //replaced
                    // Prepare the player with the source.
                    //player.prepare(videoSource);

                // Build the media item.
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(mVideoUrl));
                // Set the media item to be played.
                player.setMediaItem(mediaItem);
                // Prepare the player.
                player.prepare();
                // Start the playback.
               // player.play();



            }
            player.clearVideoSurface();
            player.setVideoSurfaceView((SurfaceView) exoPlayerView.getVideoSurfaceView());
            player.seekTo(player.getCurrentPosition() + 1);
            exoPlayerView.setPlayer(player);
        }

        public void releaseVideoPlayer() {
            if (player != null) {
                player.release();
            }
            player = null;
        }

        public void goToBackground() {
            if (player != null) {
                isPlayerPlaying = player.getPlayWhenReady();
                player.setPlayWhenReady(false);
            }
        }

        public void goToForeground() {
            if (player != null) {
                player.setPlayWhenReady(isPlayerPlaying);
            }
        }
    }

    //AsyncTask for download video file
    private class DownloadVideo extends AsyncTask<String, Integer,String>{

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadVideo(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+filename);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
        }
    }
    
}
