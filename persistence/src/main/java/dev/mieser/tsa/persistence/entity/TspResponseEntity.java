package dev.mieser.tsa.persistence.entity;

import dev.mieser.tsa.domain.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TSP_RESPONSE")
public class TspResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ResponseStatus status;

    private String statusString;

    private Integer failureInfo;

    private ZonedDateTime generationTime;

    private String serialNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUEST_ID")
    private TspRequestEntity request;

    private String asnEncoded;

}
