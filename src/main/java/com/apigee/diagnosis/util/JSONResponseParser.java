package com.apigee.diagnosis.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by senthil on 24/08/16.
 */
public class JSONResponseParser {
    private static Logger logger = LoggerFactory.getLogger(JSONResponseParser.class);

    public static List<String> getMessageProcessorList(JSONArray mpservers) {
        List<String> servers = new ArrayList<String>();
        try {
            if (mpservers.length() == 0){
                logger.error("No Servers");
                return servers;
            }
            for(int i=0;i<mpservers.length();i++) {
               servers.add(mpservers.getJSONObject(i).getString("uuid"));
            }
        } catch (JSONException e) {
            logger.error("Invalid JSON Message");
            return servers;
        }
        return servers;
    }
}


