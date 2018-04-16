/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ggg.ijkplayerdemo.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ggg.ijkplayerdemo.R;
import com.ggg.ijkplayerdemo.application.Settings;
import com.ggg.ijkplayerdemo.content.RecentMediaStorage;
import com.ggg.ijkplayerdemo.fragments.TracksFragment;
import com.ggg.ijkplayerdemo.widget.media.AndroidMediaController;
import com.ggg.ijkplayerdemo.widget.media.IjkVideoView;
import com.ggg.ijkplayerdemo.widget.media.MeasureHelper;
import com.ggg.ijkplayerdemo.widget.media.MediaController;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

public class VideoActivity extends AppCompatActivity implements TracksFragment.ITrackHolder {
    private static final String TAG = "VideoActivity";

    private String mVideoPath;
    private Uri mVideoUri;

    private AndroidMediaController mMediaController;
    private TextView mToastTextView;
    private TableLayout mHudView;//用来显示视频信息

    private IjkVideoView mVideoPlayer;
    private Settings mSettings;
    private boolean mBackPressed;

    public static Intent newIntent(Context context, String videoPath, String videoTitle) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);

        return intent;
    }

    public static void intentTo(Context context, String videoPath, String videoTitle) {
        context.startActivity(newIntent(context, videoPath, videoTitle));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mSettings = new Settings(this);

        // handle arguments
        mVideoPath = getIntent().getStringExtra("videoPath");
//        mVideoPath = "blob:https://www.iqiyi.com/c73feff2-ec1a-48c1-91d5-9346e8ab584a";

        Intent intent = getIntent();
        String intentAction = intent.getAction();
        if (!TextUtils.isEmpty(intentAction)) {
            if (intentAction.equals(Intent.ACTION_VIEW)) {
                mVideoPath = intent.getDataString();
            } else if (intentAction.equals(Intent.ACTION_SEND)) {
                mVideoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
//                mVideoUri = Uri.parse("http://127.0.0.1:8088/file/demo.mp4");
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    String scheme = mVideoUri.getScheme();
                    if (TextUtils.isEmpty(scheme)) {
                        Log.e(TAG, "Null unknown scheme\n");
                        finish();
                        return;
                    }
                    if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                        mVideoPath = mVideoUri.getPath();
                    } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                        Log.e(TAG, "Can not resolve content below Android-ICS\n");
                        finish();
                        return;
                    } else {
                        Log.e(TAG, "Unknown scheme " + scheme + "\n");
                        finish();
                        return;
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(mVideoPath)) {
            new RecentMediaStorage(this).saveUrlAsync(mVideoPath);
        }

        // init UI
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ActionBar actionBar = getSupportActionBar();
        mMediaController = new AndroidMediaController(this, false);
        mMediaController.setSupportActionBar(actionBar);

        mMediaController.setOnScaleListener(new MediaController.OnScaleListener() {
            @Override
            public void scaleListener(boolean isExpand) {
                MeasureHelper.getAspectRatioText(VideoActivity.this, mVideoPlayer.toggleAspectRatio
                        ());

            }
        });
        mToastTextView = (TextView) findViewById(R.id.toast_text_view);

        mHudView = (TableLayout) findViewById(R.id.hud_view);


        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mVideoPlayer = (IjkVideoView) findViewById(R.id.vv_player);
        mVideoPlayer.setHudView(mHudView);
        mVideoPlayer.setMediaController(mMediaController);

        // prefer mVideoPath
        if (mVideoPath != null)
            mVideoPlayer.setVideoPath(mVideoPath);
        else if (mVideoUri != null)
            mVideoPlayer.setVideoURI(mVideoUri);
        else {
            Log.e(TAG, "Null Data Source\n");
            finish();
            return;
        }
        mVideoPlayer.start();
    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;

        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBackPressed || !mVideoPlayer.isBackgroundPlayEnabled()) {
            mVideoPlayer.stopPlayback();
            mVideoPlayer.release(true);
            mVideoPlayer.stopBackgroundPlay();
        } else {
            mVideoPlayer.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_toggle_ratio) {
//            int aspectRatio = mVideoPlayer.toggleAspectRatio();
//
//            String aspectRatioText = MeasureHelper.getAspectRatioText(this, aspectRatio);
//            mToastTextView.setText(aspectRatioText);
//            mMediaController.showOnce(mToastTextView);
//            return true;
//        } else if (id == R.id.action_toggle_player) {
//            int player = mVideoPlayer.togglePlayer();
//            String playerText = IjkVideoView.getPlayerText(this, player);
//            mToastTextView.setText(playerText);
//            mMediaController.showOnce(mToastTextView);
//            return true;
//        } else if (id == R.id.action_toggle_render) {
//            int render = mVideoPlayer.toggleRender();
//            String renderText = IjkVideoView.getRenderText(this, render);
//            mToastTextView.setText(renderText);
//            mMediaController.showOnce(mToastTextView);
//            return true;
//        } else if (id == R.id.action_show_info) {
//            mVideoPlayer.showMediaInfo();
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public ITrackInfo[] getTrackInfo() {
        if (mVideoPlayer == null)
            return null;

        return null;
    }

    @Override
    public void selectTrack(int stream) {
        mVideoPlayer.selectTrack(stream);
    }

    @Override
    public void deselectTrack(int stream) {
        mVideoPlayer.deselectTrack(stream);
    }

    @Override
    public int getSelectedTrack(int trackType) {
        if (mVideoPlayer == null)
            return -1;

        return mVideoPlayer.getSelectedTrack(trackType);
    }
}
