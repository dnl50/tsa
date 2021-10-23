package dev.mieser.tsa.persistence.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Data
@Entity
@Builder
@Table(name = "TSP_RESPONSE")
public class TspResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int status;

    private String statusString;

    private Integer failureInfo;

    private ZonedDateTime generationTime;

    private String serialNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUEST_ID")
    private TspRequestEntity requestData;

    private String asnEncoded;

}
