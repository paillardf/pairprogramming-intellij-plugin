package com.sertook.pairprogramming;

/**
 * Created by florian on 13/11/2016.
 */
public interface ActionDelegate<T> {

    void onSuccess(T result);
    void onError(Exception error);
}
