package standart.code.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeObject {

    SMS("SMS"),
    EMAIl("EMAIL"),
    PUSH("PUSH");


    private final String instructions;

}
