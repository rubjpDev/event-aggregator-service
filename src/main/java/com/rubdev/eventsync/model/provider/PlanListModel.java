package com.rubdev.eventsync.model.provider;


import jakarta.xml.bind.annotation.*;
import lombok.Data;

@Data
@XmlRootElement(name = "planList")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanListModel {
    @XmlElement(name = "output")
    private OutputModel output;
    @XmlAttribute(name = "version")
    private String version;
}
