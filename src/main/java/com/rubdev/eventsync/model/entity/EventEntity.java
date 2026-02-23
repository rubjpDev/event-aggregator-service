package com.rubdev.eventsync.model.entity;

import com.rubdev.eventsync.model.provider.PlanListModel;
import com.rubdev.eventsync.model.provider.ZoneModel;
import com.rubdev.eventsync.utils.ConnectionUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "EVENTS", indexes = @Index(name = "idx_event_start_end", columnList = "START_DATE, END_DATE"), uniqueConstraints = @UniqueConstraint(columnNames = {
        "BASE_PLAN_ID", "START_DATE", "END_DATE" }))
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "BASE_PLAN_ID", nullable = false)
    private Long basePlanId;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "START_DATE")
    private LocalDateTime startDate;
    @Column(name = "END_DATE")
    private LocalDateTime endDate;
    @Column(name = "MIN_PRICE")
    private Double minPrice;
    @Column(name = "MAX_PRICE")
    private Double maxPrice;
    @Column(name = "HAD_SELLMODE_ONLINE")
    private Boolean hadSellmodeOnline;

    public boolean hasChanges(EventEntity other) {
        return !Objects.equals(this.title, other.title)
                || !Objects.equals(this.maxPrice, other.maxPrice)
                || !Objects.equals(this.minPrice, other.minPrice)
                || !Objects.equals(this.startDate, other.startDate)
                || !Objects.equals(this.endDate, other.endDate);
    }

    public void updateFrom(EventEntity source) {
        this.title = source.title;
        this.maxPrice = source.maxPrice;
        this.minPrice = source.minPrice;
        this.startDate = source.startDate;
        this.endDate = source.endDate;
    }

    public static List<EventEntity> transformFromPlanList(PlanListModel planListModel) {
        List<EventEntity> eventEntities = new ArrayList<>();
        if (planListModel == null || planListModel.getOutput() == null) {
            return eventEntities;
        }
        planListModel.getOutput().getBasePlans().forEach(basePlan -> {
            if (ConnectionUtils.ONLINE_MODE.getValue().equals(basePlan.getSellMode())) {
                basePlan.getPlans().forEach(plan -> {
                    Double maxPrice = plan.getZones()
                            .stream().mapToDouble(ZoneModel::getPrice).max().orElse(0.0);
                    Double minPrice = plan.getZones()
                            .stream().mapToDouble(ZoneModel::getPrice).min().orElse(0.0);

                    eventEntities.add(EventEntity.builder()
                            .basePlanId(Long.parseLong(basePlan.getBasePlanId()))
                            .title(basePlan.getTitle())
                            .hadSellmodeOnline(Boolean.TRUE)
                            .startDate(plan.getPlanStartDate())
                            .endDate(plan.getPlanEndDate())
                            .minPrice(minPrice)
                            .maxPrice(maxPrice).build());
                });
            }
        });
        return eventEntities;
    }
}
