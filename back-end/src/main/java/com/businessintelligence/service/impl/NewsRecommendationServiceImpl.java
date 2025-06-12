package com.businessintelligence.service.impl;

import com.businessintelligence.DTO.RecommendRequest;
import com.businessintelligence.entity.NewsBrowseRecord;
import com.businessintelligence.entity.News;
import com.businessintelligence.repository.NewsRepository;
import com.businessintelligence.service.BrowseRecordService;
import com.businessintelligence.Infrastracture.util.DatabaseQueryExecutor;
import com.businessintelligence.Infrastracture.client.LLMClient;
import com.businessintelligence.service.NewsRecommendationService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewsRecommendationServiceImpl implements NewsRecommendationService {

    private final NewsRepository newsRepository;
    private final BrowseRecordService browseRecordService;
    private final LLMClient llmClient;
    private final DatabaseQueryExecutor queryExecutor;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NewsRecommendationServiceImpl.class);

    public NewsRecommendationServiceImpl(NewsRepository newsRepository,
                                         BrowseRecordService browseRecordService,
                                         LLMClient llmClient,
                                         DatabaseQueryExecutor queryExecutor) {
        this.newsRepository = newsRepository;
        this.browseRecordService = browseRecordService;
        this.llmClient = llmClient;
        this.queryExecutor = queryExecutor;
    }

    @Override
    public List<News> getRecommendations(RecommendRequest request) {
        try {
            // 1. 获取当前新闻
            log.info("开始获取推荐新闻，请求参数：userId={}, newsId={}",
                    request.getUserId(), request.getNewsId());
            News currentNews = newsRepository.findById(request.getNewsId())
                    .orElseThrow(() -> new RuntimeException("新闻不存在"));
            log.info("当前新闻分类：{}", currentNews.getCategory());

            // 2. 获取用户历史浏览记录（最近20条）
            List<NewsBrowseRecord> historyRecords = browseRecordService.getRecentBrowsedRecords(
                    request.getUserId(), request.getTimestamp());
            log.info("获取到历史浏览记录数量：{}", historyRecords.size());
            if (!historyRecords.isEmpty()) {
                log.debug("历史浏览记录详情：{}", historyRecords);
            }

            // 3. 提取历史记录中的分类信息
            List<String> historyCategories;
            if (!historyRecords.isEmpty()) {
                // 提取所有浏览记录中的新闻ID
                List<Integer> newsIds = historyRecords.stream()
                        .map(NewsBrowseRecord::getNewsId)
                        .collect(Collectors.toList());
                log.info("待查询的新闻ID数量：{}", newsIds.size());

                // 使用 findByNewsIdIn 一次性查询所有新闻
                List<News> newsList = newsRepository.findByNewsIdIn(newsIds);
                log.info("通过ID查询到的新闻数量：{}", newsList.size());
                if (!newsList.isEmpty()) {
                    log.debug("查询到的新闻详情：{}", newsList);
                }

                // 将新闻列表转换为分类列表
                historyCategories = newsList.stream()
                        .map(News::getCategory)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                log.info("提取到的历史分类数量：{}", historyCategories.size());
                log.debug("历史分类详情：{}", historyCategories);
            } else {
                historyCategories = Collections.emptyList();
                log.warn("用户无历史浏览记录，历史分类列表为空");
            }

            // 4. 构建大语言模型提示词
            String prompt = buildLLMPrompt(currentNews, historyCategories);
            log.info("LLM提示词长度：{}", prompt.length());
            log.debug("LLM提示词内容：\n{}", prompt);

            // 5. 调用大语言模型获取推荐SQL
            String recommendedSql = llmClient.invoke(prompt);
            log.info("LLM返回的SQL：{}", recommendedSql);
            // 新增：预处理LLM返回的SQL
            recommendedSql = preprocessLLMSql(recommendedSql);
            log.info("预处理后的SQL：{}", recommendedSql);
            // 安全增强：替换用户ID占位符（临时方案，推荐使用参数化查询）
            recommendedSql = recommendedSql.replace("CURRENT_USER_ID", request.getUserId().toString());
            log.info("LLM返回并替换后的SQL：{}", recommendedSql);

            // 6. 执行SQL查询获取推荐新闻
            List<Map<String, Object>> queryResult = queryExecutor.executeQuery(recommendedSql);

            log.info("SQL查询结果数量：{}", queryResult.size());

            return convertToNewsEntities(queryResult);


        } catch (Exception e) {
            log.error("推荐服务异常", e);
            e.printStackTrace();
            // 降级策略：返回热门新闻,现在先返回空的
            return Collections.emptyList();
        }
    }

    private List<News> convertToNewsEntities(List<Map<String, Object>> queryResult) {
        List<Integer> newsIds = queryResult.stream()
                .map(row -> (Integer) row.get("news_id"))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return newsIds.isEmpty()
                ? Collections.emptyList()
                : newsRepository.findByNewsIdIn(newsIds);
    }

    private String buildLLMPrompt(News currentNews, List<String> historyCategories) {
        StringBuilder sb = new StringBuilder();

        // 系统角色定义（更简洁明确）
        sb.append("你是一个智能新闻推荐系统SQL生成器，需根据用户行为生成高效、安全的MySQL查询。\n\n");

        // 数据库结构（突出关键表和字段）
        sb.append("### 数据库核心结构:\n");
        sb.append("1. t_news (news_id, category, total_browse_num, total_browse_duration)\n");
        sb.append("2. t_news_browse_record (user_id, news_id, start_ts,duration" +
                ")\n\n");

        // 当前用户浏览信息
        sb.append("### 当前浏览新闻:\n");
        sb.append("- 分类: ").append(currentNews.getCategory()).append("\n");
        sb.append("- 标题: ").append(currentNews.getTopic()).append("\n\n");

        // 用户历史浏览分类（简化展示）
        sb.append("### 用户历史浏览分类:\n");
        if (historyCategories.isEmpty()) {
            sb.append("- 无历史记录\n");
        } else {
            sb.append(String.join(", ", historyCategories)).append("\n");
        }

        // 核心推荐逻辑要求（重点突出）
        sb.append("\n### 推荐SQL生成规则:\n");
        sb.append("1. 必须从t_news表查询news_id\n");
        sb.append("2. 推荐与当前分类或历史分类相似的新闻,历史浏览新闻duration也要考虑进来\n");
        sb.append("3. 优先推荐浏览量高且浏览时间长的新闻，生成你自己的推荐算法\n");
        sb.append("4. 最终结果LIMIT 10\n\n");

        // 安全与性能约束（简洁明了）
        sb.append("### 安全与性能约束:\n");
        sb.append("1. 禁止DELETE/UPDATE等危险操作\n");
        sb.append("2. 避免复杂子查询，优先使用JOIN或IN\n");
        sb.append("3. 自动为SELECT语句添加LIMIT限制，一定要避免语法错误\n\n");

        return sb.toString();
    }
    private String preprocessLLMSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // 去除可能的代码块标记（如``sql和``）
        String cleanedSql = sql
                .replaceFirst("^``[a-z]*\\s*", "") // 移除开头的``sql或``标记
                .replaceFirst("``\\s*$", "");      // 移除结尾的``标记

        String trimmedSql = cleanedSql.trim();

        // 修复可能缺失的SELECT首字母
        if (trimmedSql.toLowerCase().startsWith("elect")) {
            log.info("检测到SQL可能缺少SELECT首字母，自动修复");
            return "SELECT" + trimmedSql.substring(5);
        }

        return trimmedSql;
    }


}