package com.kahl.silir.main.navigationdrawermenu;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kahl.silir.R;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

/**
 * Created by Paskahlis Anjas Prabowo on 27/07/2017.
 */

public class HelpActivity extends AppCompatActivity {
    private EditText messageContent;
    private ImageButton sendButton;
    private Button callButton;

    private Activity activity = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        messageContent = (EditText) findViewById(R.id.message_content);
        sendButton = (ImageButton) findViewById(R.id.send_button);
        callButton = (Button) findViewById(R.id.call_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            private final String recipientEmail = "pjaswo1204@gmail.com";
            private final String emailSubject = "SILIR - User Feedback";

            @Override
            public void onClick(View v) {
                sendMessage(messageContent.getText().toString());
            }

            private void sendMessage(String messageContent) {
                if (messageContent.isEmpty()) {
                    Toast.makeText(activity, R.string.warning_empty_message, Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setType("message/rfc822");
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
                    intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                    intent.putExtra(Intent.EXTRA_TEXT, messageContent);
                    try {
                        startActivity(Intent.createChooser(intent, "Send email via..."));
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(activity, "No available email client installed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        callButton.setOnClickListener(new View.OnClickListener() {
            private final String phoneNumber = "08157058575";

            @Override
            public void onClick(View v) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber,
                        null));
                startActivity(dialIntent);
            }
        });

        Drawable sendIcon = MaterialDrawableBuilder.with(activity)
                .setIcon(MaterialDrawableBuilder.IconValue.SEND)
                .setSizeDp(35)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .build();
        sendButton.setImageDrawable(sendIcon);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
