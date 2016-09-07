import com.apigee.diagnosis.beans.APIDeploymentState;
import com.apigee.diagnosis.beans.Report;
import com.apigee.diagnosis.beans.Revision;
import com.apigee.diagnosis.beans.Server;
import com.apigee.diagnosis.util.JSONConverter;

import java.io.IOException;

/**
 * Created by senthil on 18/07/16.
 */
public class ApiproxyBeanTest {
    public static void main(String[] args) throws IOException {
        Server server1 = new Server("host1","uuid1", "deployed", "",null);
        Server server2 = new Server("host2","uuid2", "deployed", "noerror",null);
        Server server3 = new Server("host3","uuid3", "error", "timeout",null);
        Revision revision = new Revision("1",new Server[]{server1,server2,server3});
        APIDeploymentState apiproxyDeploymentState = new APIDeploymentState("testorg", "prod", "apiproxyName", new Revision[]{revision}, new Report[]{new Report("RPC Call Timeout","Restart MP")});
        System.out.println(JSONConverter.ObjectToJSON(apiproxyDeploymentState));
    }
}
