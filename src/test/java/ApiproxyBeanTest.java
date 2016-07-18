import com.apigee.diagnosis.beans.APIDeploymentState;
import com.apigee.diagnosis.beans.Server;
import com.apigee.diagnosis.util.JSONConverter;

import java.io.IOException;

/**
 * Created by senthil on 18/07/16.
 */
public class ApiproxyBeanTest {
    public static void main(String[] args) throws IOException {
        Server server1 = new Server("uuid1", "deployed", "");
        Server server2 = new Server("uuid2", "deployed", "noerror");
        Server server3 = new Server("uuid3", "error", "timeout");
        APIDeploymentState apiproxyDeploymentState = new APIDeploymentState("testorg", "prod", "apiproxyName", "1","deployed", "/" , new Server[]{server1, server2, server3});
        System.out.println(JSONConverter.ObjectToJSON(apiproxyDeploymentState));
    }
}
