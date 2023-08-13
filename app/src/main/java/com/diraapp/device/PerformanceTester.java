package com.diraapp.device;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.FileFilter;
import java.io.RandomAccessFile;
import java.util.Locale;
import java.util.regex.Pattern;

public class PerformanceTester {

    private static final int[] LOW_SOC = {
            -1775228513, // EXYNOS 850
            802464304,  // EXYNOS 7872
            802464333,  // EXYNOS 7880
            802464302,  // EXYNOS 7870
            2067362118, // MSM8953
            2067362060, // MSM8937
            2067362084, // MSM8940
            2067362241, // MSM8992
            2067362117, // MSM8952
            2067361998, // MSM8917
            -1853602818 // SDM439
    };
    private static PerformanceClass measuredClass;

    public static PerformanceClass measureDevicePerformanceClass(Context context) {
        if (measuredClass != null) return measuredClass;
        int androidVersion = Build.VERSION.SDK_INT;
        int cpuCount;
        if (Build.VERSION.SDK_INT >= 17) {
            cpuCount = Runtime.getRuntime().availableProcessors();
        } else {
            // Use saurabh64's answer
            cpuCount = getNumCoresOldPhones();
        }
        int memoryClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.SOC_MODEL != null) {
            int hash = Build.SOC_MODEL.toUpperCase().hashCode();
            for (int i = 0; i < LOW_SOC.length; ++i) {
                if (LOW_SOC[i] == hash) {
                    measuredClass = PerformanceClass.POTATO;
                    return measuredClass;
                }
            }
        }

        int totalCpuFreq = 0;
        int freqResolved = 0;
        for (int i = 0; i < cpuCount; i++) {
            try {
                RandomAccessFile reader = new RandomAccessFile(String.format(Locale.ENGLISH, "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq", i), "r");
                String line = reader.readLine();
                if (line != null) {
                    totalCpuFreq += Integer.parseInt(line) / 1000;
                    freqResolved++;
                }
                reader.close();
            } catch (Throwable ignore) {
            }
        }
        int maxCpuFreq = freqResolved == 0 ? -1 : (int) Math.ceil(totalCpuFreq / (float) freqResolved);

        long ram = -1;
        try {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
            ram = memoryInfo.totalMem;
        } catch (Exception ignore) {
        }

        PerformanceClass performanceClass;
        if (
                androidVersion < 21 ||
                        cpuCount <= 2 ||
                        memoryClass <= 100 ||
                        cpuCount <= 4 && maxCpuFreq != -1 && maxCpuFreq <= 1250 ||
                        cpuCount <= 4 && maxCpuFreq <= 1600 && memoryClass <= 128 && androidVersion <= 21 ||
                        cpuCount <= 4 && maxCpuFreq <= 1300 && memoryClass <= 128 && androidVersion <= 24 ||
                        ram != -1 && ram < 2L * 1024L * 1024L * 1024L
        ) {
            performanceClass = PerformanceClass.POTATO;
        } else if (
                cpuCount < 8 ||
                        memoryClass <= 160 ||
                        maxCpuFreq != -1 && maxCpuFreq <= 2055 ||
                        maxCpuFreq == -1 && cpuCount == 8 && androidVersion <= 23
        ) {
            performanceClass = PerformanceClass.MEDIUM;
        } else {
            performanceClass = PerformanceClass.HIGH;
        }

        measuredClass = performanceClass;
        return performanceClass;
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    private static int getNumCoresOldPhones() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]+", pathname.getName());
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Default to return 1 core
            return 1;
        }
    }
}
