package com.diraapp.ui.components.quickvideoplayer;

import com.diraapp.device.PerformanceClass;
import com.diraapp.device.PerformanceTester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TaggedThreadExecutor {

    private HashMap<String, Runnable> tasks = new HashMap<>();


    public TaggedThreadExecutor()
    {
        Thread thread = new Thread(() -> {
            while (true) {
                try {


                    List<String> ids = new ArrayList<>(tasks.keySet());
                    for (String id : ids) {
                        Runnable runnable = tasks.get(id);
                        tasks.remove(id);
                        try {
                            runnable.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
                catch (Exception ignored) {

                }
            }
        });
        thread.start();
    }
    public void execute(Runnable runnable, String tag)
    {
        tasks.remove(tag);
        tasks.put(tag, runnable);
    }

}
