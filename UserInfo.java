package com.ztcsoftware.lef.spacez;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.ztcsoftware.lef.spacez.model.Videotutorial;
import com.ztcsoftware.lef.spacez.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.ztcsoftware.lef.spacez.EntranceFragment.PREFS_NAME;

public class UserInfo extends FragmentActivity   {
    SwipeRefreshLayout pullToRefresh;
    SharedPreferences settings;
    String name,idGuest,email;
    ListView listVidTut;
    ImageView noVideotutorials;
    TextView username;
    Videotutorial videotutorialSelected = null; //object must be initialized with null
    List<Videotutorial> videotutorials = new ArrayList<>();

    public UserInfo(){}

    private static final int PAYPAL_REQUEST_CODE = 7171;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_PRODUCTION )     //Sandbox for testing or PRODUCTION for live
            .clientId(Constants.PAYPAL_CLIENT_ID);
    String amount ="";
    int paymentStatus,moneyInd;
    String message;

    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_user_info);
        setContentView(R.layout.activitytabuserinfo);
        //getSupportActionBar().hide();

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Video Tutorial"));
        tabLayout.addTab(tabLayout.newTab().setText("Photo Album"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new com.ztcsoftware.lef.spacez.adapter.PagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        username = findViewById(R.id.userName);
     //   pullToRefresh = findViewById(R.id.refreshTut);
//        listVidTut = findViewById(R.id.lstViewTutorials);
//        noVideotutorials = findViewById(R.id.noVideotutorials);

        final String lastname = getIntent().getStringExtra("lastname");
        final String firstname = getIntent().getStringExtra("firstname");
        final String userID = getIntent().getStringExtra("userID");
        final String username1 = getIntent().getStringExtra("username");

        //Start Paypal Service
        Intent intent = new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        settings = getApplication().getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        idGuest = settings.getString("idGuest",null); // id of guest who logged in
        name = settings.getString("name",null);       // name of guest who logged in
        email = settings.getString("email",null);     // email of guest who logged in


        username.setText(lastname+' '+firstname);
        RequestParams params = new RequestParams();
       

    }


    private void checkStatusPayment(String emailguest,String videotuttitle,String videoTutorialId, String userId){

        RequestParams params = new RequestParams();
        params.add("emailguest",emailguest);
        params.add("videotutorial_id",videoTutorialId);
        params.add("user_id",userId);
        params.add("videotuttitle",videotuttitle);

        ManagerNetwork.postPaymentResult(params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    message = response.getString("message");
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                if(message.equals("approved"))

                    paymentStatus=1;
                else
                    paymentStatus=8;  //cancel
            }
        });

    }
    private void changeMoneySign(String videoTutorialId){
        RequestParams params = new RequestParams();
        params.add("videotutorial_id",videoTutorialId);

        ManagerNetwork.postPaymentResult(params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    message = response.getString("message");
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                if(message.equals("approved"))

                    moneyInd=1;
                else
                    moneyInd=8;  //cancel
            }
        });
    }

    private void processPayment(double amnt) {

        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(String.valueOf(amnt)), "USD",
                "Payment to spaceZ|Tutorial platform", PayPalPayment.PAYMENT_INTENT_SALE);
        amount=String.valueOf(amnt);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
        startActivityForResult(intent,PAYPAL_REQUEST_CODE);
    }
 
    public class  VidtutAdapter extends BaseAdapter {
        private final JSONArray jsonArray;
        private final Activity activity;

        public VidtutAdapter(JSONArray jsonArray, Activity activity) {
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
                view = activity.getLayoutInflater().inflate(R.layout.row_videotutorial,null);
            }
            TextView vidTutTitle = view.findViewById(R.id.txtVidTutTitle);
            TextView vidTutDescription = view.findViewById(R.id.txtVidTutDescr);
            TextView categoryTitle = view.findViewById(R.id.vidTutCategory);
            TextView price = view.findViewById(R.id.priceUS);
            ImageView moneySign = view.findViewById(R.id.dollarIcon);

            JSONObject jsondata = getItem(position);
            try{
                Videotutorial videotutorial = new Videotutorial();

                //user.set_id(jsondata.getString("_id"));
                videotutorial.set_id(jsondata.getString("_id"));
//                changeMoneySign(jsondata.getString("_id"));
//
//
//                        if (moneyInd == 1) {
//                            moneySign.setImageResource(R.drawable.ic_action_money_off);
//                            moneyInd = 8;
//                        }
//                        else
//                            moneySign.setImageResource(R.drawable.ic_action_coin);



                        videotutorial.setTitle(jsondata.getString("title"));
                        vidTutTitle.setText(jsondata.getString("title"));

                        videotutorial.setDescription(jsondata.getString("description"));
                        vidTutDescription.setText(jsondata.getString("description"));

                        videotutorial.setCategory(jsondata.getString("category"));
                        categoryTitle.setText(jsondata.getString("category"));

                        videotutorial.setForSale(jsondata.getString("forSale"));
                        if (jsondata.getString("forSale").equals("No"))
                            price.setText("FREE");
                        else {
                            videotutorial.setUser(jsondata.getString("user"));
                            videotutorial.setPrice(jsondata.getDouble("price"));
                            price.setText(String.valueOf(jsondata.getDouble("price")));
                        }


                        videotutorials.add(videotutorial);

            }catch (JSONException e){
                e.printStackTrace();
            }
            return view;
        }
    }
  
}
