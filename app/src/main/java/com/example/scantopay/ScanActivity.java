package com.example.scantopay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class ScanActivity extends AppCompatActivity {

    CodeScanner codeScanner;
    CodeScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        scannerView=findViewById(R.id.scanner_view);
        codeScanner= new CodeScanner(this, scannerView);

        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            String name="",upi="",mc="";
            if (result.toString().startsWith("upi://pay")){
                int i =result.toString().indexOf("pa=");
                i+=3;
                while (i<result.toString().length()){
                    if(result.toString().charAt(i) != '&')
                        upi=upi+(result.toString().charAt(i));
                    else
                        break;
                    ++i;
                }
                i =result.toString().indexOf("pn=");
                i+=3;
                while (i<result.toString().length()){
                    if(result.toString().charAt(i) != '&')
                        name=name+(result.toString().charAt(i));
                    else
                        break;
                    ++i;
                }
                if(result.toString().contains("mc=")) {
                    i = result.toString().indexOf("mc=");
                    i += 3;
                    while (i<result.toString().length()){
                        if (result.toString().charAt(i) != '&')
                            mc = mc + (result.toString().charAt(i));
                        else
                            break;
                        ++i;
                    }
                }
                else
                    mc="0000";

                Intent intent=new Intent(ScanActivity.this,PayActivity.class);
                intent.putExtra("pa",upi);
                intent.putExtra("pn",name);
                intent.putExtra("mc",mc);
                startActivity(intent);
            }
            else
              Toast.makeText(this, "Not a UPI QR!", Toast.LENGTH_SHORT).show();
        }));

        scannerView.setOnClickListener(view -> codeScanner.startPreview());
    }

    @Override
    protected void onResume(){
        super.onResume();
        requestCamera();
    }

    public void requestCamera(){
        Dexter.withContext(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                codeScanner.startPreview();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Toast.makeText(ScanActivity.this, "Camera Permission Required To Scan", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }
}