package com.christophesmet.getiton.library.logging;

/**
 * Created by christophesmet on 26/03/15.
 */

public class EmptyLoggingService implements ILoggingService {
    @Override
    public void log(String input) {

    }

    @Override
    public void log(Throwable ex) {

    }
}