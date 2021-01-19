package com.example.scantopay;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PayActivity extends AppCompatActivity {
      TextView upiId,name;
      EditText amount_et;
      Button pay;

      public static final int UPI_PAY=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        upiId=findViewById(R.id.upi_id);
        name=findViewById(R.id.name);
        amount_et=findViewById(R.id.amount_et);
        pay=findViewById(R.id.pay_btn);

        upiId.setText(getIntent().getStringExtra("pa"));
        name.setText(getIntent().getStringExtra("pn"));


        pay.setOnClickListener(v -> {
            if (!isConnectionAvailable(this))
                Toast.makeText(this, "Internet Not Connected", Toast.LENGTH_SHORT).show();

           else if(amount_et.getText().toString().isEmpty() || Integer.parseInt(amount_et.getText().toString())<1)
               Toast.makeText(this, "Amount Invalid!", Toast.LENGTH_SHORT).show();
           else {
               String upi=getIntent().getStringExtra("pa");
               String name=getIntent().getStringExtra("pn");
               String mc=getIntent().getStringExtra("mc");
               String amount= amount_et.getText().toString().trim();
               startTransaction(upi,name,amount,mc);
           }
        });
    }

    private void startTransaction(String upi, String name, String amount, String mc) {
        Uri uri=Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa",upi)
                .appendQueryParameter("pn",name)
                .appendQueryParameter("am",amount)
                .appendQueryParameter("mc",mc)
                .appendQueryParameter("tn","Payment")
                .appendQueryParameter("tr","354654")
                .appendQueryParameter("cu","INR")
                .build();

        Intent upiPayIntent=new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        Intent chooser=Intent.createChooser(upiPayIntent, "Pay Using");

        if(upiPayIntent.resolveActivity(getPackageManager())!=null)
          startActivityForResult(chooser, UPI_PAY);
        else
          Toast.makeText(this, "No UPI Apps Found", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==UPI_PAY && resultCode==RESULT_OK){
            if(data!= null){
                String response= data.getStringExtra("response");
                checkPayment(response);
            }
        }
        else
            Toast.makeText(this, "Transaction Aborted", Toast.LENGTH_SHORT).show();
    }

    public void checkPayment(String data){
        String str=data;
        if(str==null)
            str="Null";

        String status = "";
        String approvalRefNo = "";
        String paymentCancel="";

        String[] response = str.split("&");
        for (int i = 0; i < response.length; i++) {
            String[] equalStr = response[i].split("=");
            if(equalStr.length >= 2) {
                if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                    status = equalStr[1].toLowerCase();
                }
                else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                    approvalRefNo = equalStr[1];
                }
            }
            else {
                paymentCancel = "Payment cancelled by user.";
            }
        }

        if (status.equals("success")) {
            Toast.makeText(PayActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
        }
        else if("Payment cancelled by user.".equals(paymentCancel)) {
            Toast.makeText(PayActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(PayActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable();
        }
        return false;
    }
}