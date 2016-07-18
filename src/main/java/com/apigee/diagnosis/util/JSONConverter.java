package com.apigee.diagnosis.util;

import org.codehaus.jackson.map.ObjectMapper;
import java.io.IOException;

/**
 * Created by senthil on 18/07/16.
 */
public class JSONConverter {

    public static String ObjectToJSON(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
