package com.emobot.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class EmoBotMcpTools {

    @Tool(description = "根据关键词搜索相关图片，可用于情感表达、放松心情等场景。")
    public String searchImage(String keyword) {
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        return "图片搜索完成。关键词: " + keyword + "。链接: https://www.google.com/search?tbm=isch&q=" + encoded + " 或 https://cn.bing.com/images/search?q=" + encoded;
    }

    @Tool(description = "查询地点位置信息，可推荐放松场所、心理咨询机构、公园等。")
    public String searchMap(String location) {
        String encoded = URLEncoder.encode(location, StandardCharsets.UTF_8);
        return "地点: " + location + "。地图: https://www.google.com/maps/search/" + encoded + " 或 https://uri.amap.com/search?keyword=" + encoded;
    }

    @Tool(description = "记录用户当前的情绪状态。")
    public String recordEmotion(String emotion, Integer intensity) {
        int level = intensity != null ? Math.max(1, Math.min(10, intensity)) : 5;
        return "已记录情绪: " + emotion + "，强度: " + level + "/10。";
    }

    @Tool(description = "提供深呼吸/放松呼吸练习指导。")
    public String breathingExercise(String type) {
        String t = (type == null || type.isBlank()) ? "478呼吸法" : type.trim();
        return "放松呼吸 - " + t + "：吸气4秒、屏息7秒、呼气8秒，重复3-5轮。";
    }

    @Tool(description = "根据用户当前状态推荐正念冥想资源。")
    public String recommendMeditation(String state) {
        return "针对「" + state + "」：可尝试专注呼吸冥想、身体扫描。推荐: 潮汐、小睡眠、Headspace。";
    }
}
