package com.example.ryudigitalprinting.CetakMMT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.ryudigitalprinting.Api.JSONResponse;
import com.example.ryudigitalprinting.BuildConfig;
import com.example.ryudigitalprinting.Model.ModelProfilUser;
import com.example.ryudigitalprinting.Api.RequestInterface;
import com.example.ryudigitalprinting.LoginRegister.LoginUser;
import com.example.ryudigitalprinting.R;
import com.example.ryudigitalprinting.Utility.AppController;
import com.example.ryudigitalprinting.Utility.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.ryudigitalprinting.BuildConfig.BASE_URL;

public class CetakKartuNama extends AppCompatActivity implements View.OnClickListener {

    private String url = BuildConfig.BASE_URL+"transaksi.php";
    private static final String TAG = CetakMMT.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    String tag_json_obj = "json_obj_req";
    JSONArray string_json = null;
    JSONParser jsonParser = new JSONParser();
    int success;
    Intent intent;

    EditText txtlebar, txtpanjang, txtjumlahorder, txttanggalpesan, txtnamalengkap, txtnomorhp;
    TextView txthargakartunama;
    CardView btncekharga, btnuploadgambar, btnpesankartunama;
    ImageView image;
    Bitmap bitmap, decoded;
    int PICK_IMAGE_REQUEST = 1;
    int bitmap_size = 60; // range 1 - 100
    SharedPreferences sharedpreferences;
    String id, id_transaksi, gambar;
    private ArrayList<ModelProfilUser> mArrayListUser;
    private int mYear, mMonth, mDay;
    ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cetak_kartu_nama);

        txtnamalengkap = findViewById(R.id.txtnamalengkap);
        txtnomorhp = findViewById(R.id.txtnomorhp);
        txtlebar = findViewById(R.id.txtlebar);
        txtpanjang = findViewById(R.id.txtpanjang);
        txtjumlahorder = findViewById(R.id.txtjumlahorder);
        txthargakartunama = findViewById(R.id.txthargakartunama);
        txttanggalpesan = findViewById(R.id.txttanggalpesan);
        image = findViewById(R.id.image);

        btnuploadgambar = findViewById(R.id.btnuploadgambar);
        btnuploadgambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        sharedpreferences = getSharedPreferences(LoginUser.my_shared_preferences, Context.MODE_PRIVATE);
        id = sharedpreferences.getString("id", "0");
       /// Toast.makeText(this, "ini id ke-"+ id, Toast.LENGTH_SHORT).show();


        btncekharga = findViewById(R.id.btncekharga);
        btncekharga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((txtjumlahorder.getText().length()>0)  )

                {

                    Locale localeID = new Locale("in", "ID");
                    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);


                    double jumlahorder = Double.parseDouble(txtjumlahorder.getText().toString());
                    double hitungan = jumlahorder * 45000;

                    ///double result =  hitungan * jumlahorder;
                    ///jumlahharga.setText("Rp. " +Double.toString(result));
                    txthargakartunama.setText(formatRupiah.format(hitungan));


                }


                else {
                    Toast toast = Toast.makeText(CetakKartuNama.this, "Masukan Lebar dan panjang ukuran MMT", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });


        btnpesankartunama = findViewById(R.id.btnpesankartunama);
        btnpesankartunama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String namalengkap = txtnamalengkap.getText().toString();
                String nomortelepon = txtnomorhp.getText().toString();
                String tanggal_pesan = txttanggalpesan.getText().toString();
                String lebar = txtlebar.getText().toString();
                String panjang = txtpanjang.getText().toString();
                String jumlah_pesanan = txtjumlahorder.getText().toString();
                String jumlah_harga = txthargakartunama.getText().toString();



                /// if (conMgr.getActiveNetworkInfo() != null
                ///     && conMgr.getActiveNetworkInfo().isAvailable()
                ///   && conMgr.getActiveNetworkInfo().isConnected()) {
                PostDataCustomers(namalengkap, nomortelepon, tanggal_pesan, lebar, panjang,panjang, jumlah_pesanan, jumlah_harga,gambar);
                /// } else {
                ///     Toast.makeText(getApplicationContext(), "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show();
                //   nn }


            }
        });

        txttanggalpesan = findViewById(R.id.txttanggalpesan);
        txttanggalpesan.setOnClickListener(this);

        ambilProfilUser();
    }

    public void ambilProfilUser(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface request = retrofit.create(RequestInterface.class);
        Call<JSONResponse> call = request.getProfilUser(id);
        call.enqueue(new Callback<JSONResponse>() {
            @Override
            public void onResponse(Call<JSONResponse> call, Response<JSONResponse> response) {
                JSONResponse jsonResponse = response.body();

                mArrayListUser = new ArrayList<>(Arrays.asList(jsonResponse.getDatauser()));
                String namalengkap = mArrayListUser.get(0).getNamalengkap();
                String email = mArrayListUser.get(0).getEmail();
                String nomortelepon = mArrayListUser.get(0).getNomortelepon();
                String password = mArrayListUser.get(0).getPassword();

                txtnamalengkap.setText(namalengkap);
                txtnomorhp.setText(nomortelepon);

            }

            @Override
            public void onFailure(Call<JSONResponse> call, Throwable t) {
                Log.d("Error",t.getMessage());
            }
        });
    }





    //untuk upload image, compress .JPEG ke bitmap
    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    //untuk memilih gambar
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //mengambil fambar dari Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                // 512 adalah resolusi tertinggi setelah image di resize, bisa di ganti.
                setToImageView(getResizedBitmap(bitmap, 512));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /*Menampilkan gambar*/
    private void setToImageView(Bitmap bmp) {
        //compress image
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, bytes);
        decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));

        //menampilkan gambar yang dipilih dari camera/gallery ke ImageView
        image.setImageBitmap(decoded);
    }

    // fungsi resize image
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void PostDataCustomers(final String namalengkap, final String nomortelepon, final String nomor_invoice, final String tanggal_pesan, final String lebar, final String panjang, final String jumlah_pesanan, final String gambar, final String jumlah_harga) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Silahkan Tunggu ...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);

                    // Check for error node in json
                    if (success == 1) {

                        Log.e("Registrasi berhasil!", jObj.toString());

                        goToPayment();
                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();


                    } else {
                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();

                final int random = new Random().nextInt(1000) + 20; // [0, 60] + 20 => [20, 80]
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH) + 1;
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                params.put("id", id);
                params.put("namalengkap", namalengkap);
                params.put("nomortelepon", nomortelepon);
                params.put("jenis_cetak", "Pesanan Cetak Kartu Nama");
                params.put("nomor_invoice", "#INVCETAK"+currentDay+currentMonth+currentYear+random);
                params.put("tanggal_pesan", txttanggalpesan.getText().toString());
                params.put("lebar", txtlebar.getText().toString());
                params.put("panjang", txtpanjang.getText().toString());
                params.put("jumlah_pesanan", txtjumlahorder.getText().toString()+ "Kotak");
                params.put("gambar", getStringImage(decoded));
                params.put("jumlah_harga", txthargakartunama.getText().toString());
                params.put("status_pesanan", "Menunggu Pembayaran");
                params.put("status_transfer", "Belum Lunas");
                params.put("status_transaksi", "Belum Lunas");

                return params;
            }

        };

        // Adding request to request queue

        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
        ///AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);

    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void goToPayment(){

        intent = new Intent(CetakKartuNama.this, OrderSukses.class);
        finish();
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txttanggalpesan:

                Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                txttanggalpesan.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
                break;
        }
    }
}
