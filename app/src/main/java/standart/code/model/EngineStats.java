package standart.code.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "engine_stats")
public class EngineStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "errors_count", nullable = false)
    private long errorsCount;

    @Column(name = "queue_capacity", nullable = false)
    private int queueCapacity;

    @Column(name = "process_count", nullable = false)
    private long processCount;

    @Column(name = "last_update", nullable = false)
    private OffsetDateTime updateTime;

    @PrePersist
    public void prePersist() {
        if (this.updateTime == null) {
            this.updateTime = OffsetDateTime.now();
        }
    }

}
