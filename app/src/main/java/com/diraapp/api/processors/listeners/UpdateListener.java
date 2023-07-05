package com.diraapp.api.processors.listeners;

import com.diraapp.api.updates.Update;

public interface UpdateListener {
    void onUpdate(Update update);
}
