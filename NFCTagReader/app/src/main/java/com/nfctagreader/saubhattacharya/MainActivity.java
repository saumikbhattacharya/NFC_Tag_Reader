package com.nfctagreader.saubhattacharya;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    Button read_data;
    TextView show_data;
    Tag detected_tag;
    NfcAdapter nfcAdapter;
    IntentFilter[] intentFilters;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String MIME_IMAGE_ALL = "image/*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.main_ll);

        final PackageManager pm = this.getPackageManager();
        show_data = (TextView) findViewById(R.id.show_data);
        show_data.setGravity(Gravity.CENTER_HORIZONTAL);
        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        read_data = (Button) findViewById(R.id.read_nfc);
        read_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("NFC feature is not available on this device!")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "NFC feature is available on this device!", Toast.LENGTH_SHORT).show();
                    HandleIntent(getIntent());
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void HandleIntent(Intent intent)
    {
        String action = intent.getAction();
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
        {
            detected_tag = getIntent().getParcelableExtra(nfcAdapter.EXTRA_TAG);
            NDefReaderTask NDefReader = new NDefReaderTask();
            NDefReader.execute();
        }
    }

    public void onNewIntent(Intent intent)
    {
        HandleIntent(intent);
    }

    public class NDefReaderTask extends AsyncTask<Tag, Void, String>
    {
        @Override
        protected String doInBackground(Tag... params)
        {
            try
            {
                //detected_tag = params[0];
                Ndef ndef = Ndef.get(detected_tag);
                ndef.connect();
                if(ndef != null)
                {
                    NdefMessage ndefMessage = ndef.getCachedNdefMessage();
                    NdefRecord[] records = ndefMessage.getRecords();
                    for(NdefRecord ndefRecord : records)
                    {
                        if((ndefRecord.getTnf() == NdefRecord.TNF_ABSOLUTE_URI) || (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN))
                        {
                            byte[] payload = ndefRecord.getPayload();
                            String encoding1 = "UTF-8";
                            String encoding2 = "UTF-16";
                            String textEncoding = ((payload[0] & 128) == 0) ? encoding1 : encoding2;
                            int languageCodeLength = payload[0] & 0063;
                            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
                        }
                    }
                }
                ndef.close();
            }
            catch (UnsupportedEncodingException UE)
            {
                UE.printStackTrace();
            }
            catch (IOException IE)
            {
                IE.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute()
        {

        }

        protected void onPostExecute(String result)
        {
            if(result != null)
            {
                show_data.setText(result);
            }
        }
    }
}
