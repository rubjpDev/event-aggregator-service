package com.rubdev.eventsync.model.provider;

import com.rubdev.eventsync.utils.LocalDateTimeAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanModel {
    @XmlAttribute(name = "plan_id")
    private String planId;
    @XmlAttribute(name = "plan_start_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime planStartDate;
    @XmlAttribute(name = "plan_end_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime planEndDate;
    @XmlAttribute(name = "sell_from")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime sellFrom;
    @XmlAttribute(name = "sell_to")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime sellTo;
    @XmlAttribute(name = "sold_out")
    private Boolean soldOut;
    @XmlElement(name = "zone")
    private List<ZoneModel> zones;
}
