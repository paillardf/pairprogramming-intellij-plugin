package com.sertook.pairprogramming.service;

import com.intellij.openapi.util.Key;
import com.sertook.pairprogramming.ActionDelegate;

/**
 * Created by florian on 13/11/2016.
 */
public interface PairProgrammingService {
    boolean isStarted();

    void start(String ip);

    void stop();


    Key<String> EXTRA_PAIR_PROGRAMMING_KEY = Key.create("extra.pairprogramming.ip");


}
