package com.christophesmet.getiton.library.logging;

/**
 * Created by christophesmet on 26/03/15.
 */
public interface ILoggingService {
    public void log(String input);
    public void log(Throwable ex);
}
