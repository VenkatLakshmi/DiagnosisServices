package com.apigee.diagnosis.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    public static List<String> getRevisions(String mgmtResponse) throws JSONException {
        List<String> revisions = new ArrayList<String>();
        try {
            JSONObject mgmtObject = new JSONObject(mgmtResponse);
            if (mgmtObject.has("revision") == false) {
                logger.error("No revisions deployed");
                return revisions;
            } else {
                JSONArray revisionObj = mgmtObject.getJSONArray("revision");
                if(revisionObj.length() == 0){
                    logger.error("No revisions deployed");
                    return revisions;
                }
                for(int i=0;i<revisionObj.length();i++) {
                    revisions.add(revisionObj.getJSONObject(i).getString("name"));
                }
            }
        } catch (JSONException e) {
            logger.error("Invalid JSON Message");
            return revisions;
        }
        return revisions;
    }

    public static String getBasepath(String mgmtResponse) {
        String basepath = new String();
        try {
            JSONObject mgmtObg = new JSONObject(mgmtResponse);
            JSONObject connectionObj = mgmtObg.getJSONObject("connection");
            basepath = connectionObj.getString("basePath");
        } catch (JSONException e) {
            logger.error("Invalid JSON Message");
            return "/";
        }
        return basepath;
    }
}


