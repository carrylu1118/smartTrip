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

    /*
     * TODO: 任务7.2.1 - 根据城市名查询GEO坐标，天气查询服务要用坐标而不是城市名
     */
    private Map<String, Double> geocode(String city) throws Exception {
        //根据city名称拼接查询geo的url
        //官方参考：https://open-meteo.com/en/docs/geocoding-api

        //发起http请求，请求上述url得到返回的json

        //解析json，组装成Map，返回给调用方

        Map<String, Double> geo = new LinkedHashMap<>();
        geo.put("lat", 0d); //纬度 (Latitude)
        geo.put("lng", 0d); //经度 (Longitude)
        return geo;
    }

    /**
     * TODO: 任务7.2.1 - 根据城市名查询当前天气
     */
    @Tool(description = "根据城市名查询天气")
    public String getWeather(@ToolParam(description = "城市名称，如：北京、济南") String city) throws Exception {
        log.info("AI调用tools工具查询天气：{}",city);
        //先根据城市名调取GEO坐标
        Map<String, Double> geo = geocode(city);

        //根据geo坐标拼接查询url
        //官方参考：https://open-meteo.com/en/docs/cma-api

        //发起http请求，请求上述url得到返回的json

        //解析json，拼接成天气字符串，返回给大模型

        String weather = city+"天气很好！";

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
