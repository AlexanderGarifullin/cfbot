package com.ga.cfbot.application.service;

import com.ga.cfbot.infrastructure.codeforces.dto.ApiResponse;
import com.ga.cfbot.infrastructure.codeforces.CodeforcesApiService;
import com.ga.cfbot.infrastructure.codeforces.dto.CfUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class CodeforcesApiServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    CodeforcesApiService service;

    @Test
    void fetchRating_whenResponseIsNull_returnsNull() {
        when(restTemplate.getForObject(anyString(), eq(ApiResponse.class), anyString()))
                .thenReturn(null);

        Integer rating = service.fetchRating("someHandle");
        assertThat(rating).isNull();
    }

    @Test
    void fetchRating_whenRestTemplateThrows_returnsNull() {
        when(restTemplate.getForObject(anyString(), eq(ApiResponse.class), anyString()))
                .thenThrow(new RuntimeException("API error"));

        Integer rating = service.fetchRating("someHandle");
        assertThat(rating).isNull();
    }

    @Test
    void fetchRating_whenOkResponse_returnsRating() {
        CfUser user = CfUser.builder()
                .handle("someHandle")
                .rating(2025)
                .build();

        ApiResponse resp = ApiResponse.builder()
                .status("OK")
                .result(new CfUser[]{ user })
                .build();

        when(restTemplate.getForObject(anyString(), eq(ApiResponse.class), eq("someHandle")))
                .thenReturn(resp);

        Integer rating = service.fetchRating("someHandle");
        assertThat(rating).isEqualTo(2025);
    }
}
