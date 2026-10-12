package standart.code.mapper;

import org.mapstruct.Condition;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

@Component
public class JsonNullableMapper {

    public <T> T unwrap(JsonNullable<T> jsonNullable) {
        return jsonNullable != null ? jsonNullable.orElse(null) : null;
    }

    @Condition
    public <T> boolean isPresent(JsonNullable<T> nullable) {
        return nullable != null && nullable.isPresent();
    }
}
