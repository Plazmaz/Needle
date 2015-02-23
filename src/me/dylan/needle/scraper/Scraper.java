package me.dylan.needle.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Dylan on 2/21/2015.
 */
public class Scraper {
    private static final String ADDRESS_REGEX = "<p><div class=\"adressen online\">(.*?)</div><br/><br/>";

    public static List<String> getIpsFromMCSL() throws IOException {
        List<String> lines = new ArrayList<>();
        HttpURLConnection connection = (HttpURLConnection) new URL("http://minecraft-server-list.com/filter/").openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("spi", "on");
        connection.setRequestProperty("mg", "on");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        Pattern pattern = Pattern.compile(ADDRESS_REGEX);

        while((line = reader.readLine()) != null) {
            if(pattern.matcher(line).find()) {
                System.out.println(line);
                lines.add(line.replaceAll(ADDRESS_REGEX, "$1"));
            }
        }
        return lines;
    }
}
