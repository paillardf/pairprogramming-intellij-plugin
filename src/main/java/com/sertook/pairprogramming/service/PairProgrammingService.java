package com.sertook.pairprogramming.service;

import com.sertook.pairprogramming.ActionDelegate;

/**
 * Created by florian on 13/11/2016.
 */
public interface PairProgrammingService {
    boolean isStarted();

    void start(ActionDelegate<String> delegate);

    void stop(ActionDelegate<String> delegate);
}
