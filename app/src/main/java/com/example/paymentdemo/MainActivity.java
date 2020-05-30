package com.example.paymentdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.gson.JsonObject;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    private EditText orderTv,amountTv,custTv;
    private Button payBtn;
    private PaytmPGService Service = PaytmPGService.getStagingService("");
    private String MID="ZBJrph96773099546467";
    private String ORDER_ID="order1";
    private String CUST_ID="cust123";
    private String amount="100";
    private String CHECK_SUM="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        payBtn=findViewById(R.id.pay_btn);
      orderTv=findViewById(R.id.orderTv);
      amountTv=findViewById(R.id.amountTv);
      custTv=findViewById(R.id.custTv);

        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               getValues();
            }
        });

        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent());



        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }
    }

    private void addParams(){

        HashMap<String, String> paramMap = new HashMap<String,String>();
        paramMap.put( "MID" , MID);
        // Key in your staging and production MID available in your dashboard
        paramMap.put( "ORDER_ID" , ORDER_ID);
        paramMap.put( "CUST_ID" , CUST_ID);
        //paramMap.put( "MOBILE_NO" , "7777777777");
      //  paramMap.put( "EMAIL" , "username@emailprovider.com");
        paramMap.put( "CHANNEL_ID" , "WAP");
        paramMap.put( "TXN_AMOUNT" , amount);
        paramMap.put( "WEBSITE" , "WEBSTAGING");
        // This is the staging value. Production value is available in your dashboard
        paramMap.put( "INDUSTRY_TYPE_ID" , "Retail");
        // This is the staging value. Production value is available in your dashboard
        paramMap.put( "CALLBACK_URL", "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp");
        paramMap.put( "CHECKSUMHASH" , CHECK_SUM);

        PaytmOrder Order = new PaytmOrder(paramMap);

        initializePayment(Order);


    }

    private void getCheckSumHash(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        //  https://us-central1-paymentdemo-a3643.cloudfunctions.net/getCheckSum?oId=order1&custId=cust123&amount=100
        String url ="https://us-central1-paymentdemo-a3643.cloudfunctions.net/getCheckSum?oId="+ORDER_ID+"&custId="+CUST_ID+"&amount="+amount;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            CHECK_SUM=jsonObject.getString("checksum");
                            Log.i("Volley ", "Response is: "+ CHECK_SUM);
                            addParams();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Error :", "onErrorResponse: "+ error);
            }
        });
    // Add the request to the RequestQueue.
        queue.add(stringRequest);


    }

    private void initializePayment(PaytmOrder Order){
        Service.initialize(Order, null);

        Service.startPaymentTransaction(MainActivity.this, true, true, new PaytmPaymentTransactionCallback() {
            /*Call Backs*/
            public void someUIErrorOccurred(String inErrorMessage) {
                /*Display the error message as below */
                Toast.makeText(getApplicationContext(), "UI Error " + inErrorMessage , Toast.LENGTH_LONG).show();
            }

            public void networkNotAvailable() {
                /*Display the message as below */
                Toast.makeText(getApplicationContext(), "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();
            }

            public void clientAuthenticationFailed(String inErrorMessage)  {
                /*Display the message as below */
                Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage.toString(), Toast.LENGTH_LONG).show();
            }

            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl)  {
                /*Display the message as below */
                Toast.makeText(getApplicationContext(), "Unable to load webpage " + inErrorMessage.toString(), Toast.LENGTH_LONG).show();
            }

            public void onBackPressedCancelTransaction(){
                /*Display the message as below */
                Toast.makeText(getApplicationContext(), "Transaction cancelled" , Toast.LENGTH_LONG).show();
            }


            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {}

            public void onTransactionResponse(Bundle inResponse) {
                /*Display the message as below */
                Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();
            }
        });


    }

    private void getValues(){
        CUST_ID=custTv.getText().toString();
        amount=amountTv.getText().toString();
        ORDER_ID=orderTv.getText().toString();

        getCheckSumHash();
    }
}
