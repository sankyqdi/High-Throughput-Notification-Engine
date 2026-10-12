package standart.code.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiRoutes {

    //к отправке сообщений
    public static final String API_V1 = "/api/v1";

    public static final String NOTIFICATIONS = API_V1 + "/notifications";

    public static final String SEND_NOTIFICATION = "/send";
    public static final String GET_BY_ID = "/{id}";

    //к регистрации аккаунта

    public static final String API_REGISTER = "/api/v1/senders";

    public static final String USER = "/api/user";
}
