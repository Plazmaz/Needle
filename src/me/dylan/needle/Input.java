package me.dylan.needle;

import me.dylan.needle.logging.LogLevel;
import me.dylan.needle.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created with love by Dylan on 11/8/2014.
 *
 * @author Dylan Thomas Katz
 */
public class Input {
    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public static String prompt(String prompt) {
        Logger.log(prompt, LogLevel.INFO);
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
