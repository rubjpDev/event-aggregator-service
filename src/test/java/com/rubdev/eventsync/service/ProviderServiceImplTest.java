package com.rubdev.eventsync.service;

import com.rubdev.eventsync.config.ProviderApiClient;
import com.rubdev.eventsync.model.entity.EventEntity;
import com.rubdev.eventsync.repository.EventRepository;
import com.rubdev.eventsync.utils.ConnectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class ProviderServiceImplTest {

    @Mock
    private ProviderApiClient apiClient;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private ProviderServiceImpl providerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    /**
     * THIS TEST THE HAPPY PATH WHERE WE GET AN EVENT AND CACHE IT
     */
    @Test
    void testSyncWithProvider_ValidXml_SavesEventsAndCaches() {
        //THIS XML EXAMPLE COMES FROM THE ONES THAT RETURNS THE PROVIDER
        String xmlResponse = """
                <?xml version="1.0" encoding="UTF-8"?>
                <planList xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
                    <output>
                        <base_plan base_plan_id="1591" sell_mode="online" title="Los Morancos">
                            <plan plan_start_date="2021-07-31T20:00:00"
                                  plan_end_date="2021-07-31T21:20:00"
                                  plan_id="1642"
                                  sell_from="2021-06-26T00:00:00"
                                  sell_to="2021-07-31T19:50:00"
                                  sold_out="false">
                                <zone zone_id="186" capacity="0" price="75.00" name="Amfiteatre" numbered="true"/>
                                <zone zone_id="186" capacity="14" price="65.00" name="Amfiteatre" numbered="false"/>
                            </plan>
                        </base_plan>
                    </output>
                </planList>
                """;

        //SERVICE CALL MOCKUPS
        when(apiClient.fetchProviderEvents()).thenReturn(xmlResponse);
        when(eventRepository.findByBasePlanIdAndStartDateAndEndDate(anyLong(), any(), any()))
                .thenReturn(Optional.empty());
        when(eventRepository.save(any(EventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        providerService.syncWithProvider();

        //VALIDATONS
        verify(eventRepository, atLeastOnce()).save(any(EventEntity.class));
        verify(valueOps, times(1))
                .set(eq(ConnectionUtils.CACHE_KEY.getValue()), anyList(), any());
    }

    /**
     * THIS TEST WHAT HAPPENS IF THE RESPONSE IS EMPTY
     */
    @Test
    void testSyncWithProvider_EmptyResponse_NoAction() {
        when(apiClient.fetchProviderEvents()).thenReturn("");
        providerService.syncWithProvider();
        verifyNoInteractions(eventRepository);
    }
}
