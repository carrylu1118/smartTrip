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

    private Map<String, Double> geocode(String city) throws Exception {
        //根据city名称拼接查询geo的url
        //官方参考：https://open-meteo.com/en/docs/geocoding-api
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = GEO_URL + "?name=" + encodedCity + "&count=1&language=zh&format=json";
        log.info("正在查询 {} 的地理坐标,url：{}", city, url);

        //发起http请求，请求上述url得到返回的json
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();

        //解析json，组装成Map，返回给调用方
        Map<String, Double> geo = new LinkedHashMap<>();
        geo.put("lat", 0d); //纬度 (Latitude)
        geo.put("lng", 0d); //经度 (Longitude)

        JsonNode root = mapper.readTree(json);
        JsonNode results = root.get("results");
        // 判断有返回结果数组且不为空
        if (results != null && results.isArray() && !results.isEmpty()) {
            JsonNode firstResult = results.get(0);
            double lat = firstResult.get("latitude").asDouble();
            double lng = firstResult.get("longitude").asDouble();
            geo.put("lat", lat);
            geo.put("lng", lng);
            log.info("城市[{}]获取坐标成功：lat={}, lng={}", city, lat, lng);
        } else {
            log.warn("城市[{}]未匹配到地理坐标，返回默认0,0", city);
        }
        return geo;
    }


    @Tool(description = "根据城市名查询天气")
    public String getWeather(@ToolParam(description = "城市名称，如：北京、济南") String city) throws Exception {
        log.info("AI调用tools工具查询天气：{}",city);
        //先根据城市名调取GEO坐标
        Map<String, Double> geo = geocode(city);

        //根据geo坐标拼接查询url
        //官方参考：https://open-meteo.com/en/docs/cma-api
        String url = WEATHER_URL + "?latitude=" + geo.get("lat") + "&longitude=" + geo.get("lng")
                + "&hourly=temperature_2m,precipitation,rain&daily=weather_code,temperature_2m_max,temperature_2m_min"
                + "&timezone=Asia%2FTokyo&forecast_days=3&models=cma_grapes_global";
        log.info("查询天气中,url：{}", url);

        //发起http请求，请求上述url得到返回的json
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //解析json，拼接成天气字符串，返回给大模型
        JsonNode root = mapper.readTree(response.body());
        JsonNode daily = root.get("daily");
        if(daily == null){
            return city + "天气查询失败，接口返回数据异常";
        }

        JsonNode timeArr = daily.get("time");
        JsonNode codeArr = daily.get("weather_code");
        JsonNode maxTempArr = daily.get("temperature_2m_max");
        JsonNode minTempArr = daily.get("temperature_2m_min");

        StringBuilder sb = new StringBuilder();
        sb.append("【").append(city).append(" 未来3天天气预报】\n");
        //循环3天
        for (int i = 0; i < 3; i++) {
            String date = timeArr.get(i).asText();
            int weatherCode = codeArr.get(i).asInt();
            double maxT = maxTempArr.get(i).asDouble();
            double minT = minTempArr.get(i).asDouble();
            String desc = weatherDesc(weatherCode);

            sb.append(date).append("：")
                    .append(desc)
                    .append("，温度：").append(minT).append("℃ ~ ").append(maxT).append("℃\n");
        }

        String weather = sb.toString();
        log.info("天气查询结果：{}", weather);
        return weather;
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
