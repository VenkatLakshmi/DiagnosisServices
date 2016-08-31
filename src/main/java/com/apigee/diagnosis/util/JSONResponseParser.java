package com.apigee.diagnosis.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by senthil on 24/08/16.
 */
public class JSONResponseParser {
    public static List<String> getMessageProcessorList(String jsonResponse) {
        List<String> servers = new ArrayList<String>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray serversObj = jsonObject.getJSONArray("server");
                for(int i=0;i<serversObj.length();i++) {
                    servers.add(serversObj.getJSONObject(i).getString("uUID"));
                }
        } catch (JSONException e) {
            return servers;
        }
        return servers;
    }
}


