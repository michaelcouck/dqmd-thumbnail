package com.doqmind.thumbnail.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.io.Serializable;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 13-11-2023
 */
@Getter
@Setter
@Entity
@Builder
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Asset extends Base implements Serializable {

    @Column
    @ApiModelProperty(position = 1, value = "...")
    private String assetId;

    @Column
    @ApiModelProperty(position = 2, value = "...")
    private String name;

    @Column
    @ApiModelProperty(position = 3, required = true, value = "...")
    private String path;

}