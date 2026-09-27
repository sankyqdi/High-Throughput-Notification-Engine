package standart.code.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "task_pull")
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "email")
    private String email;

    @Column(name = "processing")
    private boolean processing;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_object")
    private TypeObject typeObject;


    @Column(name = "date_time")
    private OffsetDateTime dateTime;

}
