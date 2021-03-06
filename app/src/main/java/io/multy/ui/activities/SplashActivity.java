/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.branch.referral.Branch;
import io.multy.BuildConfig;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.responses.ServerConfigResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.ui.fragments.dialogs.TermsDialogFragment;
import io.multy.util.Constants;
import io.multy.util.FirstLaunchHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SplashActivity extends AppCompatActivity {

    public static final String RESET_FLAG = "resetflag";
    public static final int REQUEST_CODE_TERMS = 101;

    @BindView(R.id.container)
    View container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        if (FirstLaunchHelper.preventRootIfDetected(this) && !BuildConfig.DEBUG) {
            try {
                Prefs.clear();
                RealmManager.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        logPushClicked();

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_emergency);
        animation.setDuration(350);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                initBranchIO();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (Prefs.getBoolean(Constants.PREF_TERMS_ACCEPTED, false)) {
                    getServerConfig();
                } else {
                    showTerms();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        container.startAnimation(animation);
    }

    private void showTerms() {
        TermsDialogFragment.newInstance(new TermsDialogFragment.OnTermsInteractionListener() {
            @Override
            public void onAccepted() {
                Prefs.putBoolean(Constants.PREF_TERMS_ACCEPTED, true);
                getServerConfig();
            }

            @Override
            public void onDiscarded() {
                Toast.makeText(SplashActivity.this, R.string.terms_please, Toast.LENGTH_LONG).show();
            }
        }).show(getSupportFragmentManager(), "");
    }

    private void getServerConfig() {
        if (getIntent().getBooleanExtra(RESET_FLAG, false)) {
            return;
        }
        MultyApi.INSTANCE.getServerConfig().enqueue(new Callback<ServerConfigResponse>() {
            @Override
            public void onResponse(Call<ServerConfigResponse> call, Response<ServerConfigResponse> response) {
                if (response.isSuccessful()) {
                    ServerConfigResponse configResponse = response.body();
                    ServerConfigResponse.AndroidConfig androidConfig = configResponse.getAndroidConfig();
                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        int versionCode = pInfo.versionCode;
                        if (versionCode < androidConfig.getSoftVersion()) {
                            //we can still use soft version
                            //leave this clause for future possible purposes
//                            showUpdateDialog();
                            showMainActivity();
                        } else if (BuildConfig.VERSION_CODE < androidConfig.getHardVersion()) {
                            showUpdateDialog();
                        } else {
                            showMainActivity();
                        }

                        if (configResponse.getDonates() != null) {
                            EventBus.getDefault().postSticky(configResponse);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
//                    showError(R.string.error_config_error);
                    showMainActivity();
                }
            }

            @Override
            public void onFailure(Call<ServerConfigResponse> call, Throwable t) {
//                showError(R.string.error_config_error);
                t.printStackTrace();
                showMainActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getBooleanExtra(RESET_FLAG, false)) {
            SimpleDialogFragment.newInstanceNegative(getString(R.string.database_error), getString(R.string.reset_db_with_wrong_key), v -> {
                try {
                    RealmManager.removeDatabase(this);
                    Prefs.clear();
                    Realm.init(getApplicationContext());
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
                startActivity(new Intent(this, SplashActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
                finish();
            }).show(getSupportFragmentManager(), SimpleDialogFragment.class.getSimpleName());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void showError(int message) {
        SimpleDialogFragment.newInstanceNegative(R.string.error, message, view -> {
            finish();
        }).show(getSupportFragmentManager(), "");
    }

    private void showUpdateDialog() {
        SimpleDialogFragment.newInstanceNegative("New version is coming.", "Please, uninstall Multy before using new version.", view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            finish();
        }).show(getSupportFragmentManager(), "");
    }

    private void showMainActivity() {
        Thread background = new Thread() {
            public void run() {
                try {
                    Intent mainActivityIntent = new Intent(SplashActivity.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    mainActivityIntent.putExtra(MainActivity.IS_ANIMATION_MUST_SHOW, true);
                    addDeepLinkExtra(mainActivityIntent);
                    startActivity(mainActivityIntent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Intent i = new Intent(getBaseContext(), MainActivity.class);
                    addDeepLinkExtra(i);
                    startActivity(i);
                }
            }
        };
        background.start();
    }

    private void initBranchIO() {
        Branch branch = Branch.getInstance(getApplicationContext());
        branch.initSession((referringParams, error) -> {
            if (error == null) {
                String address = referringParams.optString(getString(R.string.address));
                String amount = referringParams.optString(getString(R.string.amount));
                if (!TextUtils.isEmpty(address)) {
                    getIntent().putExtra(Constants.EXTRA_ADDRESS, address);
                }
                if (!TextUtils.isEmpty(amount)) {
                    getIntent().putExtra(Constants.EXTRA_AMOUNT, amount);
                }
            } else {
                Timber.i(error.getMessage());
            }
        }, this.getIntent().getData(), this);
    }

    private void addDeepLinkExtra(Intent intent) {
        if (getIntent().hasExtra(Constants.EXTRA_ADDRESS)) {
            intent.putExtra(Constants.EXTRA_ADDRESS, getIntent().getStringExtra(Constants.EXTRA_ADDRESS));
        }
        if (getIntent().hasExtra(Constants.EXTRA_AMOUNT)) {
            intent.putExtra(Constants.EXTRA_AMOUNT, getIntent().getStringExtra(Constants.EXTRA_AMOUNT));
        }
    }

    private void logPushClicked() {
        if (getIntent().hasExtra(getString(R.string.push_id))) {
            Analytics.getInstance(this).logPush(AnalyticsConstants.PUSH_OPEN,
                    getIntent().getStringExtra(getString(R.string.push_id)));
        }
    }
}
