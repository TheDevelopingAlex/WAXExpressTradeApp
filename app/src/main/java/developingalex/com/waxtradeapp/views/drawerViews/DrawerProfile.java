package developingalex.com.waxtradeapp.views.drawerViews;

import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import developingalex.com.waxtradeapp.R;
import developingalex.com.waxtradeapp.lib.OAuthImplementation;
import developingalex.com.waxtradeapp.lib.MyJobService;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class DrawerProfile extends Fragment {

    private OAuthImplementation oAuthImplementation;
    private SharedPreferences sharedPreferences;

    private final String mTradeURL = "Not Found";
    private final static int jobID = 9000;

    private RelativeLayout content;
    private ProgressDialog progressDialog;
    private TextView userProfileName;
    private ImageView userProfilePicture, QRCode;
    private EditText tradeURLText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.drawer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {

        oAuthImplementation = new OAuthImplementation(view.getContext());

        sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);

        // show ProgressDialog
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading Profile ...");
        progressDialog.show();

        // find views
        content = view.findViewById(R.id.drawer_profile_content);
        userProfilePicture = view.findViewById(R.id.drawer_profile_pic);
        userProfileName = view.findViewById(R.id.drawer_profile_username);
        QRCode = view.findViewById(R.id.drawer_profile_qrcode);
        tradeURLText = view.findViewById(R.id.drawer_profile_tradeURL);
        final Button copyButton = view.findViewById(R.id.drawer_profile_copyButton);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String tradeURL = oAuthImplementation.getUserTradeURL();
                    if (tradeURL != null) {

                        final MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                        final BitMatrix bitMatrix = multiFormatWriter.encode(tradeURL, BarcodeFormat.QR_CODE, 700, 700);
                        final BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

                        // create bitmap
                        final Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                        // only the original thread that created a view
                        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                QRCode.setImageBitmap(bitmap);
                                tradeURLText.setText(tradeURL);
                                content.setVisibility(View.VISIBLE);
                                progressDialog.dismiss();
                            }
                        });
                    }

                    // only the original thread that created a view
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // set user infos
                            Picasso.get()
                                    .load(oAuthImplementation.getUserProfilePicture())
                                    .error(R.drawable.opskins_logo_avatar)
                                    .into(userProfilePicture);

                            userProfileName.setText(oAuthImplementation.getUserProfileUsername());

                            content.setVisibility(View.VISIBLE);
                            progressDialog.dismiss();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Copy Button to copy tradeURL
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CLIPBOARD_SERVICE);
                final ClipData clip = ClipData.newPlainText("tradeURL", mTradeURL);
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), "Trade URL copied!", Toast.LENGTH_SHORT).show();
            }
        });



        // Switch for notification service
        final Switch notificationSwitch = view.findViewById(R.id.notificationSwitch);

        if (!sharedPreferences.contains("appNotifications")) {
            sharedPreferences.edit().putBoolean("appNotifications", false).apply();
        } else {
            if (sharedPreferences.getBoolean("appNotifications", false) && !notificationSwitch.isChecked()) {
                final JobScheduler scheduler = (JobScheduler) getContext().getSystemService(JOB_SCHEDULER_SERVICE);
                if (!scheduler.getAllPendingJobs().contains(jobID)) {
                    initializeJob();
                }
                notificationSwitch.toggle();
            }
        }

        if (!sharedPreferences.contains("appNotificationsTime"))
            sharedPreferences.edit().putInt("appNotificationsTime", 15).apply();

        notificationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notificationSwitch.isChecked()) {
                    sharedPreferences.edit().putBoolean("appNotifications", true).apply();
                    if (initializeJob())
                        Toast.makeText(getContext(),"Notifications ENABLED" , Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(),"ERROR: Can not activate notifications" , Toast.LENGTH_SHORT).show();
                } else {
                    sharedPreferences.edit().putBoolean("appNotifications", false).apply();
                    if (cancelJob())
                        Toast.makeText(getContext(), "Notifications DISABLED", Toast.LENGTH_SHORT).show();
                }
            }
        });

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                final RadioButton radioButton = view.findViewById(checkedId);

                switch(checkedId) {
                    case R.id.radio_1:
                        if (radioButton.isChecked())
                            sharedPreferences.edit().putInt("appNotificationsTime", 15).apply();
                        break;
                    case R.id.radio_2:
                        if (radioButton.isChecked())
                            sharedPreferences.edit().putInt("appNotificationsTime", 60).apply();
                        break;
                    case R.id.radio_3:
                        if (radioButton.isChecked())
                            sharedPreferences.edit().putInt("appNotificationsTime", 1440).apply();
                        break;
                }

                if (notificationSwitch.isChecked()) {
                    if (cancelJob()) {
                        if (initializeJob()) {
                            int time = sharedPreferences.getInt("appNotificationsTime", 15);
                            if (time == 1440)
                                Toast.makeText(getContext(), "Time changed to 24h", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getContext(), "Time changed to " +time+"min", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

    }

    private boolean initializeJob() {
        final ComponentName componentName = new ComponentName(Objects.requireNonNull(getContext()), MyJobService.class);

        final JobInfo info = new JobInfo.Builder(jobID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)
                .setPeriodic(TimeUnit.MINUTES.toMillis(sharedPreferences.getInt("appNotificationsTime", 15)))
                .build();

        final JobScheduler scheduler = (JobScheduler) getContext().getSystemService(JOB_SCHEDULER_SERVICE);

        return scheduler.schedule(info) == JobScheduler.RESULT_SUCCESS;
    }

    private boolean cancelJob() {
        final JobScheduler scheduler = (JobScheduler) Objects.requireNonNull(getContext()).getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancelAll();
        return true;
    }

}
