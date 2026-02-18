package com.rubdev.eventsync.service;

import com.rubdev.eventsync.config.ProviderApiClient;
import com.rubdev.eventsync.model.entity.EventEntity;
import com.rubdev.eventsync.model.provider.PlanListModel;
import com.rubdev.eventsync.model.service.ProviderService;
import com.rubdev.eventsync.repository.EventRepository;
import com.rubdev.eventsync.utils.ConnectionUtils;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jaxb.runtime.v2.runtime.IllegalAnnotationsException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {
    private final ProviderApiClient apiClient;
    private final EventRepository eventRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void syncWithProvider() {
        try {
            log.info("SERVICE: -- syncWithProvider -- INIT");
            // QUICK FETCH TO THE EXTERNAL PROVIDER
            String xmlResponse = apiClient.fetchProviderEvents();
            if (xmlResponse == null || xmlResponse.isEmpty()) {
                log.warn("SERVICE: -- syncWithProvider -- WARN: EMPTY RESPONSE FROM PROVIDER");
                return;
            }
            // HERE I PARSE THE XML REQUEST TO MY MODEL

            PlanListModel planListModel = null;
            try {
                JAXBContext context = JAXBContext.newInstance(PlanListModel.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                JAXBElement<PlanListModel> tempElement = unmarshaller.unmarshal(
                        new StreamSource(new StringReader(xmlResponse)), PlanListModel.class);
                planListModel = tempElement.getValue();
            } catch (IllegalAnnotationsException e) {
                e.getErrors().forEach(er -> log.error("JAXB MAP ISSUE: {}", er.toString()));
            }

            List<EventEntity> eventEntities = EventEntity.transformFromPlanList(planListModel);
            if (eventEntities.isEmpty()) {
                log.warn("SERVICE: -- syncWithProvider -- WARN: NO XML RESPONSE PARSED");
                return;
            }

            List<EventEntity> saved = this.checkAndSaveEvent(eventEntities);

            if (!saved.isEmpty()) {
                redisTemplate.opsForValue().set(ConnectionUtils.CACHE_KEY.getValue(), saved, Duration.ofMinutes(16));
                log.info("SERVICE: -- syncWithProvider -- SUCCESS: CHANGED CACHED (TTL 16min)");
            }
            log.info("SERVICE: -- syncWithProvider -- END");
        } catch (Exception e) {
            log.error("SERVICE: -- syncWithProvider -- ERROR: {}", e.getMessage(), e);
        }

    }

    private List<EventEntity> checkAndSaveEvent(List<EventEntity> eventEntities) {
        List<EventEntity> saved = new ArrayList<>();
        for (EventEntity e : eventEntities) {
            Optional<EventEntity> existing = eventRepository.findByBasePlanIdAndStartDateAndEndDate(
                    e.getBasePlanId(), e.getStartDate(), e.getEndDate());
            if (existing.isPresent()) {
                EventEntity current = existing.get();
                Boolean isChange = Boolean.FALSE;

                if (!current.getTitle().equals(e.getTitle())) {
                    current.setTitle(e.getTitle());
                    isChange = Boolean.TRUE;
                }
                if (!current.getMaxPrice().equals(e.getMaxPrice())) {
                    current.setMaxPrice(e.getMaxPrice());
                    isChange = Boolean.TRUE;
                }
                if (!current.getMinPrice().equals(e.getMinPrice())) {
                    current.setMinPrice(e.getMinPrice());
                    isChange = Boolean.TRUE;
                }
                if (!current.getStartDate().equals(e.getStartDate())) {
                    current.setStartDate(e.getStartDate());
                    isChange = Boolean.TRUE;
                }
                if (!current.getEndDate().equals(e.getEndDate())) {
                    current.setEndDate(e.getEndDate());
                    isChange = Boolean.TRUE;
                }

                if (isChange) {
                    saved.add(eventRepository.save(current));
                    log.info("-- UPDATED EVENT ID={}", current.getBasePlanId());
                }
            } else {
                saved.add(eventRepository.save(e));
                log.info("-- NEW EVENT ID={}", e.getBasePlanId());
            }
        }
        log.info("SERVICE: -- syncWithProvider -- SUCCESS: STORED NEW EVENTS IN DB");
        return saved;
    }
}
