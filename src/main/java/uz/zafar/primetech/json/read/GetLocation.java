package uz.zafar.primetech.json.read;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import uz.zafar.primetech.json.model.Location;
import uz.zafar.primetech.json.model.TokenModel;

@Log4j2
public class GetLocation {


    public static Location getLocation(Double lat, Double lon) {
        Gson gson = new Gson();
        URL url;
        URLConnection connection = null;
        BufferedReader reader = null;
        try {
            url = new URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon + "&zoom=155&addressdetails=1");
            String ur = "%s" ;
            connection = url.openConnection();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String json = "", line;
            while ((line = reader.readLine()) != null)
                json = json.concat(line);
            return gson.fromJson(json, Location.class);
        } catch (Exception e) {
            log.error(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error closing reader", e);
                }
            }
        }
        return null;
    }
}
