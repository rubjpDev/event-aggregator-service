package com.rubdev.eventsync.model.provider;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class BasePlanModel {
    @XmlAttribute(name = "base_plan_id")
    private String basePlanId;
    @XmlAttribute(name = "sell_mode")
    private String sellMode;
    @XmlAttribute(name = "title")
    private String title;
    @XmlAttribute(name = "organizer_company_id")
    private String organizerCompanyId;
    @XmlElement(name = "plan")
    private List<PlanModel> plans;
}
