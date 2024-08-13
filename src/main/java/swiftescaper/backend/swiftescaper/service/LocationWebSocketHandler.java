package swiftescaper.backend.swiftescaper.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import swiftescaper.backend.swiftescaper.domain.entity.Location;
import swiftescaper.backend.swiftescaper.domain.entity.Tunnel;
import swiftescaper.backend.swiftescaper.repository.LocationRepository;
import swiftescaper.backend.swiftescaper.repository.TunnelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class LocationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TunnelRepository tunnelRepository;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트로부터 받은 메시지 JSON(lat, lng, tunnelId, token)
        String payload = message.getPayload();
        System.out.println("Received: " + payload);

        // JSON -> Map
        Map<String, Object> locationData = objectMapper.readValue(payload, Map.class);

        // lat, lng, tunnelId, token 추출
        Double lat = (Double) locationData.get("lat");
        Double lng = (Double) locationData.get("lng");
        Long tunnelId = ((Number) locationData.get("tunnelId")).longValue();
        String token = (String) locationData.get("fcmToken");

        // Tunnel 엔티티 가져오기
        Tunnel tunnel = tunnelRepository.findById(tunnelId).orElseThrow(() ->
                new IllegalArgumentException("Invalid tunnel ID: " + tunnelId));

        // Location 엔티티 생성 및 데이터베이스에 저장
        Location location = Location.builder()
                .lat(lat)
                .lng(lng)
                .token(token)
                .tunnel(tunnel)
                .build();

        locationRepository.save(location);

        // DB에 잘 저장됐는지 Test 코드
        System.out.println("Saved location - X: " + lat + ", Y: " + lng + ", TunnelId: " + tunnelId + ", Token: " + token);


    }
}
