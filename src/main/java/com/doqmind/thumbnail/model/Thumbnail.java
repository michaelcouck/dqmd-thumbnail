package com.doqmind.thumbnail.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 09-11-2023
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
public class Thumbnail extends Base implements Serializable {

    @Column
    @ApiModelProperty(position = 1, value = "...")
    private String thumbnailId;

    @Column
    @ApiModelProperty(position = 2, value = "...")
    private String name;

    @Column
    @ApiModelProperty(position = 3, required = true, value = "...")
    private String path;

    @Transient
    @ApiModelProperty(position = 3, required = true, value = "...")
    private byte[] blob;

}