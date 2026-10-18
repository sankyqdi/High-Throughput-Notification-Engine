package standart.code.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiRoutes {

    public static final String API_V1 = "/api/v1";

    public static final String NOTIFICATIONS = API_V1 + "/notifications";
    public static final String SEND_NOTIFICATION = "/send";
    public static final String GET_BY_ID = "/{id}";

    public static final String API_REGISTER = API_V1 + "/senders";

    public static final String USER = API_V1 + "/users";
}
