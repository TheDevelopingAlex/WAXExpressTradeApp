package developingalex.com.waxtradeapp;

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

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class DrawerProfile extends Fragment {

    private OAuth oAuth;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String mTradeURL = "Not Found";
    private static int jobID = 9000;

    private RelativeLayout content;
    private ProgressDialog progressDialog;
    private TextView userProfileName;
    private ImageView userProfilePicture, QRCode;
    private EditText tradeURLText;
    private Button copyButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.drawer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {

        oAuth = new OAuth(view.getContext());

        sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

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
        copyButton = view.findViewById(R.id.drawer_profile_copyButton);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String tradeURL = oAuth.getUserTradeURL();
                    if (tradeURL != null) {

                        mTradeURL = tradeURL;

                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                        BitMatrix bitMatrix = multiFormatWriter.encode(tradeURL, BarcodeFormat.QR_CODE, 700, 700);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

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
                                    .load(oAuth.getUserProfilePicture())
                                    .error(R.drawable.opskins_logo_avatar)
                                    .into(userProfilePicture);

                            userProfileName.setText(oAuth.getUserProfileUsername());

                            content.setVisibility(View.VISIBLE);
                            progressDialog.dismiss();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("tradeURL", mTradeURL);
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), "Trade URL copied!", Toast.LENGTH_SHORT).show();
            }
        });

        final Switch notificationSwitch = view.findViewById(R.id.notificationSwitch);

        if (!sharedPreferences.contains("appNotifications")) {
            editor.putBoolean("appNotifications", false).commit();
        } else {
            if (sharedPreferences.getBoolean("appNotifications", false) && !notificationSwitch.isChecked()) {
                JobScheduler scheduler = (JobScheduler) getContext().getSystemService(JOB_SCHEDULER_SERVICE);
                if (!scheduler.getAllPendingJobs().contains(jobID)) {
                    initializeJob();
                }
                notificationSwitch.toggle();
            }
        }

        if (!sharedPreferences.contains("appNotificationsTime"))
            editor.putInt("appNotificationsTime", 15).commit();

        notificationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notificationSwitch.isChecked()) {
                    editor.putBoolean("appNotifications", true).apply();
                    if (initializeJob())
                        Toast.makeText(getContext(),"Notifications ENABLED" , Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(),"Notifications ERROR: Can not activate notifications" , Toast.LENGTH_SHORT).show();
                } else {
                    editor.putBoolean("appNotifications", false).apply();
                    if (cancelJob())
                        Toast.makeText(getContext(), "Notifications DISABLED", Toast.LENGTH_SHORT).show();
                }
            }
        });

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                RadioButton radioButton = view.findViewById(checkedId);
                boolean checked = radioButton.isChecked();

                switch(checkedId) {
                    case R.id.radio_1:
                        if (checked)
                            editor.putInt("appNotificationsTime", 15).commit();
                        break;
                    case R.id.radio_2:
                        if (checked)
                            editor.putInt("appNotificationsTime", 60).commit();
                        break;
                    case R.id.radio_3:
                        if (checked)
                            editor.putInt("appNotificationsTime", 1440).commit();
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
        ComponentName componentName = new ComponentName(Objects.requireNonNull(getContext()), myJobService.class);

        JobInfo info = new JobInfo.Builder(jobID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)
                .setPeriodic(TimeUnit.MINUTES.toMillis(sharedPreferences.getInt("appNotificationsTime", 15)))
                .build();

        JobScheduler scheduler = (JobScheduler) getContext().getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);

        return resultCode == JobScheduler.RESULT_SUCCESS;
    }

    private boolean cancelJob() {
        JobScheduler scheduler = (JobScheduler) Objects.requireNonNull(getContext()).getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancelAll();
        return true;
    }

}
