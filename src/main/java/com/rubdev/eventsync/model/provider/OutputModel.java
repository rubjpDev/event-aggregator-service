package com.rubdev.eventsync.model.provider;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class OutputModel {
    @XmlElement(name = "base_plan")
    private List<BasePlanModel> basePlans;
}
