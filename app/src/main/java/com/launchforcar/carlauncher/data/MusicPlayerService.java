package com.launchforcar.carlauncher.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadata;
import android.os.Build;
import android.util.Log;

/**
 * 音乐播放器状态监听器
 * 监听系统媒体播放器的状态变化
 */
public class MusicPlayerService {
    
    private static final String TAG = "MusicPlayerService";
    
    public interface MusicCallback {
        void onMusicStateChanged(String title, String artist, boolean isPlaying);
    }
    
    private Context context;
    private MusicCallback callback;
    private BroadcastReceiver mediaButtonReceiver;
    
    public MusicPlayerService(Context context) {
        this.context = context;
    }
    
    /**
     * 注册音乐播放器监听
     */
    public void register(MusicCallback callback) {
        this.callback = callback;
        
        // 注册媒体按钮广播接收器
        IntentFilter filter = new IntentFilter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            filter.addAction("android.media.session.playstate");
        }
        filter.addAction(Intent.ACTION_MEDIA_BUTTON);
        
        mediaButtonReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "收到媒体广播: " + intent.getAction());
                // 这里可以解析播放状态
            }
        };
        
        try {
            context.registerReceiver(mediaButtonReceiver, filter);
        } catch (Exception e) {
            Log.e(TAG, "注册广播失败", e);
        }
    }
    
    /**
     * 取消注册
     */
    public void unregister() {
        if (mediaButtonReceiver != null) {
            try {
                context.unregisterReceiver(mediaButtonReceiver);
            } catch (Exception e) {
                Log.e(TAG, "注销广播失败", e);
            }
        }
    }
    
    /**
     * 获取当前播放的歌曲信息（需要查询 MediaSession）
     */
    public void getCurrentTrackInfo() {
        // 简化版：直接回调默认值
        // 实际项目中应该使用 MediaController 查询
        if (callback != null) {
            callback.onMusicStateChanged("未播放", "点击选择音乐应用", false);
        }
    }
}
