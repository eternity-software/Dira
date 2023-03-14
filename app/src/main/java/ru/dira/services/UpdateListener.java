package ru.dira.services;

import ru.dira.api.updates.Update;

public interface UpdateListener {
    void onUpdate(Update update);
}
