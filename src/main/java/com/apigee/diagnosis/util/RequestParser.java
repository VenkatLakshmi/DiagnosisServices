package com.apigee.diagnosis.util;

/**
 * Created by venkataraghavan on 17/07/16.
 */

import java.net.URL;


public class RequestParser {

    public static final int MAX_QUERY_PARAMS = 4;

    public String org;
    public String env;
    public String api;
    public String revision;

    public RequestParser() {
    }

    public String getQueries(URL url) {

        String queryList = url.getQuery();

        return queryList;

    }

    public void parseQueries(String queryList) {

        if (queryList != null) {

            String delim = ";";
            String[] tokens = queryList.split(delim);

            if (tokens.length > MAX_QUERY_PARAMS) {

                System.out.println("Incorrect URL : Query Param Length exceeded" + tokens.length);
                return;

            }

            for (int i = 0; i < tokens.length; i++) {
                String query = tokens[i];
                String delim2 = "=";
                String[] tokens2 = query.split(delim2);

                if (tokens2[0].equals("org")) {
                    org = tokens2[1];

                }else if (tokens2[0].equals("env")) {
                    env = tokens2[1];

                }else if (tokens2[0].equals("api")) {
                    api = tokens2[1];

                }else if (tokens2[0].equals("revision")) {
                    revision = tokens2[1];

                }else {
                    System.out.println("Incorrect Request URL - query param  should have ?org=;env=;api=;revision=");
                }

            }

        }
    }
}