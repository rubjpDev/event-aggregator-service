package com.rubdev.eventsync.model.provider;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ZoneModel {
    @XmlAttribute(name = "zone_id")
    private String zoneId;
    @XmlAttribute(name = "capacity")
    private Integer capacity;
    @XmlAttribute(name = "price")
    private Double price;
    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "numbered")
    private Boolean isNumbered;
}
