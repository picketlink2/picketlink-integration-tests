package org.picketlink.test.integration.saml2;

import static org.picketlink.test.integration.util.TestUtil.getTargetURL;

import org.junit.runner.RunWith;
import org.picketlink.test.integration.util.PicketLinkIntegrationTests;
import org.picketlink.test.integration.util.TargetContainers;

@RunWith(PicketLinkIntegrationTests.class)
@TargetContainers({"jboss-as5"})
public abstract class AbstractSAMLIntegrationTests {

    String IDP_URL = getTargetURL("/idp/");
    static String IDP_SIG_URL = getTargetURL("/idp-sig/");

    String SALES_POST_URL = getTargetURL("/sales-post/");
    String SALES_POST_SIG_URL = getTargetURL("/sales-post-sig/");
    String SALES_POST_VALVE_URL = getTargetURL("/sales-post-valve/");

    String EMPLOYEE_REDIRECT_URL = getTargetURL("/employee/");
    String EMPLOYEE_REDIRECT_SIG_URL = getTargetURL("/employee-sig/");
    String EMPLOYEE_REDIRECT_VALVE_URL = getTargetURL("/employee-redirect-valve/");

    String LOGOUT_URL = "?GLO=true";

}
