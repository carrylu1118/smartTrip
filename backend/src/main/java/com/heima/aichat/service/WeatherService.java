package com.heima.aichat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 天气查询服务 — Open-Meteo 免费 API（无需 Key）
 */
@Component
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private static final String GEO_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        WeatherService weatherService = new WeatherService();
        Map<String, Double> geo = weatherService.geocode("北京");
        System.out.println(geo);

        String bj = new WeatherService().getWeather("北京");
        System.out.println(bj);
    }

    /**
     * 根据城市名查询当前天气
     */
    @Tool(description = "根据城市名查询天气")
    public String getWeather(@ToolParam(description = "城市名称，如：北京、济南") String city) {
        log.info("AI调用tools工具查询天气：{}",city);
        try {
            Map<String, Double> geo = geocode(city);
            if (geo == null) {
                return "未找到城市：" + city;
            }

            String url = WEATHER_URL + "?latitude=" + geo.get("lat") + "&longitude=" + geo.get("lng")
                    + "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m"
                    + "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max"
                    + "&timezone=auto&forecast_days=3";

            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(resp.body());
            JsonNode current = root.get("current");
            JsonNode daily = root.get("daily");

            StringBuilder sb = new StringBuilder();
            sb.append("城市：").append(city).append("\n");
            sb.append("当前温度：").append(current.get("temperature_2m").asText()).append("℃\n");
            sb.append("湿度：").append(current.get("relative_humidity_2m").asText()).append("%\n");
            sb.append("风速：").append(current.get("wind_speed_10m").asText()).append(" km/h\n");
            int code = current.get("weather_code").asInt();
            sb.append("天气：").append(weatherDesc(code)).append("\n");

            sb.append("未来几天：\n");
            JsonNode dates = daily.get("time");
            JsonNode highs = daily.get("temperature_2m_max");
            JsonNode lows = daily.get("temperature_2m_min");
            JsonNode rain = daily.get("precipitation_probability_max");
            for (int i = 0; i < Math.min(dates.size(), 3); i++) {
                sb.append("  ").append(dates.get(i).asText())
                        .append(" ").append(highs.get(i).asText()).append("℃ / ")
                        .append(lows.get(i).asText()).append("℃")
                        .append(" 降水概率 ").append(rain.get(i).asText()).append("%\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Weather query failed: {}", e.getMessage());
            return "天气查询失败：" + e.getMessage();
        }
    }

    private Map<String, Double> geocode(String city) throws Exception {
        String url = GEO_URL + "?name=" + URLEncoder.encode(city, StandardCharsets.UTF_8)
                + "&count=1&language=zh";
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(resp.body());
        JsonNode results = root.get("results");
        if (results == null || results.isEmpty()) {
            return null;
        }
        JsonNode r = results.get(0);
        Map<String, Double> geo = new LinkedHashMap<>();
        geo.put("lat", r.get("latitude").asDouble());
        geo.put("lng", r.get("longitude").asDouble());
        return geo;
    }

    private String weatherDesc(int code) {
        return switch (code) {
            case 0 -> "晴天";
            case 1, 2, 3 -> "多云";
            case 45, 48 -> "雾";
            case 51, 53, 55 -> "小雨";
            case 61, 63, 65 -> "雨";
            case 71, 73, 75 -> "雪";
            case 80, 81, 82 -> "阵雨";
            case 95, 96, 99 -> "雷暴";
            default -> "未知(" + code + ")";
        };
    }
}
