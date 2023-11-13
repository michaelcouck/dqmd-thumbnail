package com.doqmind.thumbnail.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Getter
@Setter
@Entity
@SuppressWarnings("WeakerAccess")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Base implements Serializable {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", allocationSize = 1, sequenceName = "sequence")
    @ApiModelProperty(position = 1, required = true, value = "The technical identifier of the entity")
    private long id;

    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateDate;

    @PrePersist
    public void prePersist() {
        long now = System.currentTimeMillis();
        creationDate = new Timestamp(now);
        lastUpdateDate = new Timestamp(now);
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateDate = new Timestamp(System.currentTimeMillis());
    }

}