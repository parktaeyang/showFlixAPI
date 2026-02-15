package com.schedulemng.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/show-user")
@RequiredArgsConstructor
@Slf4j
public class ShowUserApiController {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	/**
	 * 신규 백엔드의 /api/schedule-users API 호출
	 * @param request 현재 요청 정보
	 * @return API 호출 결과
	 */
	@GetMapping("/schedule-users")
	public ResponseEntity<Map<String, Object>> getScheduleUsers(HttpServletRequest request) {
		// 현재 요청의 호스트와 포트를 가져와서 포트만 9000으로 변경
		String scheme = request.getScheme(); // http 또는 https
		String serverName = request.getServerName(); // 도메인 또는 IP
		
		// 같은 도메인에 포트만 9000으로 변경
		String baseUrl = String.format("%s://%s:9000", scheme, serverName);
		String apiUrl = baseUrl + "/api/schedule-users/first";
		
		log.info("신규 백엔드 API 호출: {}", apiUrl);
		
		try {
			// 외부 API 호출
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
					apiUrl,
					HttpMethod.GET,
					null,
					new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
			);
			
			Map<String, Object> result = response.getBody();
			log.info("API 호출 성공: {}", result);
			
			return ResponseEntity.ok(result != null ? result : Map.of("message", "응답 데이터가 없습니다."));
			
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			// 4xx, 5xx 에러 처리
			log.error("신규 백엔드 API 호출 중 HTTP 에러 발생 - Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
			
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("error", "API 호출 실패");
			errorResponse.put("status", e.getStatusCode().value());
			HttpStatus httpStatus = HttpStatus.resolve(e.getStatusCode().value());
			if (httpStatus != null) {
				errorResponse.put("statusText", httpStatus.getReasonPhrase());
			}
			errorResponse.put("url", apiUrl);
			
			// 응답 본문을 파싱 시도
			try {
				String responseBody = e.getResponseBodyAsString();
				if (responseBody != null && !responseBody.isEmpty()) {
					Map<String, Object> errorBody = objectMapper.readValue(
							responseBody, 
							new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
					);
					errorResponse.put("responseBody", errorBody);
				} else {
					errorResponse.put("responseBody", "응답 본문이 없습니다.");
				}
			} catch (Exception parseException) {
				log.warn("에러 응답 본문 파싱 실패", parseException);
				errorResponse.put("responseBody", e.getResponseBodyAsString());
			}
			
			return ResponseEntity.ok(errorResponse);
			
		} catch (RestClientException e) {
			// 네트워크 에러 등 기타 RestTemplate 에러
			log.error("신규 백엔드 API 호출 중 RestClient 에러 발생", e);
			
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("error", "API 호출 실패");
			errorResponse.put("message", e.getMessage());
			errorResponse.put("url", apiUrl);
			errorResponse.put("errorType", e.getClass().getSimpleName());
			
			return ResponseEntity.ok(errorResponse);
			
		} catch (Exception e) {
			// 기타 예상치 못한 에러
			log.error("신규 백엔드 API 호출 중 예상치 못한 오류 발생", e);
			
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("error", "API 호출 실패");
			errorResponse.put("message", e.getMessage());
			errorResponse.put("url", apiUrl);
			errorResponse.put("errorType", e.getClass().getSimpleName());
			
			return ResponseEntity.ok(errorResponse);
		}
	}
}
