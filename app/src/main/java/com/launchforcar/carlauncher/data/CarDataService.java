package com.launchforcar.carlauncher.data;

import android.os.Handler;
import android.os.Looper;

import java.util.Random;

/**
 * 车辆数据模拟器
 * 在实际项目中，这里应该连接 CAN 总线或车辆 API
 */
public class CarDataService {
    
    private static final Random random = new Random();
    private static final Handler handler = new Handler(Looper.getMainLooper());
    
    public interface CarDataCallback {
        void onCarDataLoaded(CarData data);
    }
    
    public static class CarData {
        public String range;      // 续航里程
        public String fuel;       // 油量/电量
        public String tirePressure; // 胎压
        public String mileage;    // 总里程
        
        public CarData(String range, String fuel, String tirePressure, String mileage) {
            this.range = range;
            this.fuel = fuel;
            this.tirePressure = tirePressure;
            this.mileage = mileage;
        }
    }
    
    /**
     * 模拟获取车辆数据（实际应该从 CAN 总线或车辆 API 获取）
     */
    public static void getCarData(CarDataCallback callback) {
        // 模拟网络延迟
        handler.postDelayed(() -> {
            // 生成随机但合理的车辆数据
            int range = 350 + random.nextInt(150); // 350-500 km
            int fuel = 60 + random.nextInt(35);    // 60-95%
            float tirePressure = 2.3f + (random.nextFloat() * 0.4f); // 2.3-2.7 bar
            int mileage = 15000 + random.nextInt(5000); // 15000-20000 km
            
            CarData data = new CarData(
                range + " km",
                fuel + "%",
                String.format("%.1f bar", tirePressure),
                mileage + " km"
            );
            
            callback.onCarDataLoaded(data);
        }, 500);
    }
    
    /**
     * 定期更新车辆数据（每 30 秒）
     */
    public static void startPeriodicUpdate(CarDataCallback callback) {
        final Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                getCarData(callback);
                handler.postDelayed(this, 30000); // 30 秒后再次更新
            }
        };
        handler.post(updateRunnable);
    }
}
