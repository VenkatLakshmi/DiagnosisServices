package com.apigee.diagnosis.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;

/**
 * Created by senthil on 18/07/16.
 */
public class JSONConverter {

    public static String ObjectToJSON(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.getSerializationConfig().withSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}