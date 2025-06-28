package com.ga.cfbot.infrastructure.codeforces;

import com.ga.cfbot.infrastructure.codeforces.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CodeforcesApiService {
    private final RestTemplate restTemplate;

    @Cacheable("cfRatings")
    public Integer fetchRating(String handle) {
        String url = "https://codeforces.com/api/user.info?handles={handles}";
        try {
            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class, handle);
            if (response != null && "OK".equals(response.status)
                    && response.result != null && response.result.length > 0) {
                return response.result[0].rating;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
