package com.trendflow.keyword.keyword.service;

import com.trendflow.keyword.global.code.KeywordCacheCode;
import com.trendflow.keyword.global.redis.*;
import com.trendflow.keyword.keyword.Repository.KeywordRepository;
import com.trendflow.keyword.keyword.dto.response.*;
import com.trendflow.keyword.keyword.entity.Keyword;
import com.trendflow.keyword.keyword.entity.KeywordCount;
import com.trendflow.keyword.keyword.entity.KeywordDistinct;
import com.trendflow.keyword.msa.service.AnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeywordService {
    private final KeywordRepository keywordRepository;

    private final HotKeywordRepository hotKeywordRepository;
    private final RecommendKeywordRepository recommendKeywordRepository;
    private final RelateKeywordRepository relateKeywordRepository;
    private final WordCloudKeywordRepository wordCloudKeywordRepository;

    private final AnalyzeService analyzeService;

    @Value("${keyword.hot.expire}")
    private Integer hotExpire;
    @Value("${keyword.recommend.expire}")
    private Integer recommendExpire;
    @Value("${keyword.relate.expire}")
    private Integer relateExpire;
    @Value("${keyword.word-cloud.expire}")
    private Integer wordCloudExpire;


    @Transactional
    public FindHotKeywordResponse findHotKeyword() throws RuntimeException {
        /*
            60 분마다 갱신
         */

//        Integer today = Integer.parseInt(LocalDate.now().toString().replace("-", ""));
        Integer today = Integer.parseInt(LocalDate.of(2023, 2, 28).toString().replace("-", ""));

        // 일간 HOT Keyword
        // 이미 계산된 결과가 없으면 (만료시간이 되서 결과가 사라져 갱신해야되는 경우) 계산 시작
        List<HotKeyword> dayNow = hotKeywordRepository.findById(KeywordCacheCode.DAY_HOT_KEYWORD_RESULT.getCode())
                .orElseGet(() -> {
                    // 새로운 결과를 가져와 리스트 생성 (현재 기준 값)
                    List<KeywordDistinct> dayKeywordList = keywordRepository.findByRegDt(today, today, 8);

                    AtomicInteger rank = new AtomicInteger();
                    List<HotKeyword> now = dayKeywordList.stream()
                            .map(keyword ->
                                HotKeyword.builder()
                                    .rank(rank.getAndIncrement() + 1)
                                    .keyword(keyword.getKeyword())
                                    .type(KeywordCacheCode.TYPE_NEW.getCode())
                                    .step(0)
                                    .mentionCount(keyword.getCount())
                                    .build())
                            .collect(Collectors.toList());

                    // 과거 결과를 가져옴
                    Optional<List<HotKeyword>> dayPast = hotKeywordRepository.findById(KeywordCacheCode.DAY_HOT_KEYWORD.getCode());
                    // 과거 결과가 있으면 비교 로직 수행
                    if (dayPast.isPresent()) now = rankHotKeyword(now, dayPast.get());

                    hotKeywordRepository.save(KeywordCacheCode.DAY_HOT_KEYWORD.getCode(), now);
                    hotKeywordRepository.saveResult(KeywordCacheCode.DAY_HOT_KEYWORD_RESULT.getCode(), now, hotExpire);
                    return now;
                });

        // 주간 HOT Keyword
        // 이미 계산된 결과가 없으면 (만료시간이 되서 결과가 사라져 갱신해야되는 경우) 계산 시작
        List<HotKeyword> weekNow = hotKeywordRepository.findById(KeywordCacheCode.WEEK_HOT_KEYWORD_RESULT.getCode())
                .orElseGet(() -> {

//                    Integer startDate = Integer.parseInt(LocalDate.now().minusDays(7).toString().replace("-", ""));
                    Integer startDate = Integer.parseInt(LocalDate.of(2023, 2, 28).minusDays(7).toString().replace("-", ""));

                    List<KeywordDistinct> weekKeywordList = keywordRepository.findByRegDt(startDate, today, 8);

                    AtomicInteger rank = new AtomicInteger();
                    List<HotKeyword> now = weekKeywordList.stream()
                            .map(keyword ->
                                    HotKeyword.builder()
                                            .rank(rank.getAndIncrement() + 1)
                                            .keyword(keyword.getKeyword())
                                            .type(KeywordCacheCode.TYPE_NEW.getCode())
                                            .step(0)
                                            .mentionCount(keyword.getCount())
                                            .build())
                            .collect(Collectors.toList());
                    // 기존 데이터가 있으면 비교해서 리스트 변경
                    Optional<List<HotKeyword>> weekPast = hotKeywordRepository.findById(KeywordCacheCode.WEEK_HOT_KEYWORD.getCode());
                    if (weekPast.isPresent()) now = rankHotKeyword(now, weekPast.get());

                    hotKeywordRepository.save(KeywordCacheCode.WEEK_HOT_KEYWORD.getCode(), now);
                    hotKeywordRepository.saveResult(KeywordCacheCode.WEEK_HOT_KEYWORD_RESULT.getCode(), now, hotExpire);
                    return now;
                });

        return FindHotKeywordResponse.builder()
                .day(dayNow)
                .week(weekNow)
                .build();
    }

    @Transactional
    public List<FindRecommendKeywordResponse> findRecommendKeyword() throws RuntimeException {
        List<RecommendKeyword> recommendKeywordList = recommendKeywordRepository.findById(KeywordCacheCode.RECOMMEND_KEYWORD.getCode())
                .orElseGet(() -> {

//                    Integer today = Integer.parseInt(LocalDate.now().toString().replace("-", ""));
                    Integer today = Integer.parseInt(LocalDate.of(2023, 2, 28).toString().replace("-", ""));

                    List<KeywordDistinct> keywordList = keywordRepository.findByRegDt(today, today, 10);

                    AtomicLong id = new AtomicLong();
                    List<RecommendKeyword> now = keywordList.stream()
                            .map(keyword ->
                                    RecommendKeyword.builder()
                                            .id(id.getAndIncrement())
                                            .keyword(keyword.getKeyword())
                                            .build())
                            .collect(Collectors.toList());

                    recommendKeywordRepository.saveResult(KeywordCacheCode.RECOMMEND_KEYWORD.getCode(), now, recommendExpire);
                    return now;
                });

        return FindRecommendKeywordResponse.toList(recommendKeywordList);
    }

    @Transactional
    public List<FindRelateKeywordResponse> findRelateKeyword(String keyword) throws RuntimeException {
        List<Keyword> keywordIdList = keywordRepository.findByKeyword(keyword);

        String key = String.format("%s_%s", KeywordCacheCode.RELATE_KEYWORD_RESULT.getCode(), keyword);

        AtomicInteger rank = new AtomicInteger();
        List<RelateKeyword> relateKeywordList = relateKeywordRepository.findById(key)
                .orElseGet(() -> {
                    List<RelateKeyword> now = analyzeService.getRelation(keywordIdList.stream()
                                    .map(Keyword::getKeywordId)
                                    .collect(Collectors.toList())
                            ).stream()
                            .map(relation ->
                                    RelateKeyword.builder()
                                        .rank(rank.getAndIncrement() + 1)
                                        .keyword(relation.getRelationKeyword())
                                        .type(KeywordCacheCode.TYPE_NEW.getCode())
                                        .step(0)
                                        .relatedCount(relation.getCount())
                                        .build())
                            .collect(Collectors.toList());

                    // 기존 데이터가 있으면 비교해서 리스트 변경
                    Optional<List<RelateKeyword>> relatePast = relateKeywordRepository.findById(KeywordCacheCode.RELATE_KEYWORD.getCode());
                    if (relatePast.isPresent()) now = rankRelateKeyword(now, relatePast.get());

                    relateKeywordRepository.save(key, now);
                    relateKeywordRepository.saveResult(key, now, relateExpire);
                    return now;
                });

        return FindRelateKeywordResponse.toList(relateKeywordList);
    }

    @Transactional
    public List<FindWordCloudResponse> findWordCloudKeyword(String keyword) throws RuntimeException {
        List<Keyword> keywordIdList = keywordRepository.findByKeyword(keyword);

        String key = String.format("%s_%s", KeywordCacheCode.WORDCLOUD_KEYWORD.getCode(), keyword);

        List<WordCloudKeyword> wordCloudKeywordList = wordCloudKeywordRepository.findById(key)
                .orElseGet(() -> {
                    List<WordCloudKeyword> now = analyzeService.getRelationForWordCloud(keywordIdList.stream()
                                .map(Keyword::getKeywordId)
                                .collect(Collectors.toList())
                            ).stream()
                            .map(relation ->
                                    WordCloudKeyword.builder()
                                            .text(relation.getRelationKeyword())
                                            .value(relation.getCount().intValue())
                                            .build())
                            .collect(Collectors.toList());

                    wordCloudKeywordRepository.saveResult(key, now, wordCloudExpire);
                    return now;

                });

        return FindWordCloudResponse.toList(wordCloudKeywordList);
    }

    @Transactional
    public List<Keyword> findKeyword(String keyword, LocalDate startDate, LocalDate endDate) {

        Integer start = Integer.parseInt(startDate.toString().replace("-", ""));
        Integer end = Integer.parseInt(endDate.toString().replace("-", ""));

        return keywordRepository.findByKeywordAndRegDtBetweenOrderBySourceId(keyword, start, end);
    }

    @Transactional
    public List<KeywordCount> findKeywordCount(String keyword, LocalDate startDate, LocalDate endDate) {

        Integer start = Integer.parseInt(startDate.toString().replace("-", ""));
        Integer end = Integer.parseInt(endDate.toString().replace("-", ""));

        List<KeywordCount> keywordCountList =
                keywordRepository.countByPlatformCodeAndRegDt(keyword, start, end);
        return keywordCountList;
    }

    private List<HotKeyword> rankHotKeyword(List<HotKeyword> now, List<HotKeyword> past) {
        List<HotKeyword> hotKeywordList = new ArrayList<>();

        for (Integer src = 0; src < now.size(); src++){
            HotKeyword hotKeyword = now.get(src);

            Boolean isNew = true;

            for (Integer search = 0; search < past.size(); search++) {
                if (hotKeyword.getKeyword().equals(past.get(search).getKeyword())) {
                    isNew = false;
                    Integer typeValue = src - search;
                    Integer step = Math.abs(src - search);

                    // 순위 떨어짐
                    if (typeValue > 0) {
                        hotKeyword.setType(KeywordCacheCode.TYPE_DOWN.getCode());
                        hotKeyword.setStep(step);
                    }
                    // 순위 고정
                    else if (typeValue == 0) {
                        hotKeyword.setType(KeywordCacheCode.TYPE_SAME.getCode());
                        hotKeyword.setStep(step);
                    }
                    // 순위 올라감
                    else {
                        hotKeyword.setType(KeywordCacheCode.TYPE_UP.getCode());
                        hotKeyword.setStep(step);
                    }
                    break;
                }
            }
            // 새로운 키워드가 온 경우
            if (isNew) {
                hotKeyword.setType(KeywordCacheCode.TYPE_NEW.getCode());
                hotKeyword.setStep(0);
            }
            hotKeywordList.add(hotKeyword);
        }

        for (Integer rank = 1; rank <= hotKeywordList.size(); rank++){
            hotKeywordList.get(rank - 1).setRank(rank);
        }

        return hotKeywordList;
    }

    private List<RelateKeyword> rankRelateKeyword(List<RelateKeyword> now, List<RelateKeyword> past) {
        List<RelateKeyword> relateKeywordList = new ArrayList<>();

        for (Integer src = 0; src < now.size(); src++){
            RelateKeyword relateKeyword = now.get(src);

            Boolean isNew = true;

            for (Integer search = 0; search < past.size(); search++) {
                if (relateKeyword.getKeyword().equals(past.get(search).getKeyword())) {
                    isNew = false;
                    Integer typeValue = src - search;
                    Integer step = Math.abs(src - search);

                    // 순위 떨어짐
                    if (typeValue > 0) {
                        relateKeyword.setType(KeywordCacheCode.TYPE_DOWN.getCode());
                        relateKeyword.setStep(step);
                    }
                    // 순위 고정
                    else if (typeValue == 0) {
                        relateKeyword.setType(KeywordCacheCode.TYPE_SAME.getCode());
                        relateKeyword.setStep(step);
                    }
                    // 순위 올라감
                    else {
                        relateKeyword.setType(KeywordCacheCode.TYPE_UP.getCode());
                        relateKeyword.setStep(step);
                    }
                    break;
                }
            }
            // 새로운 키워드가 온 경우
            if (isNew) {
                relateKeyword.setType(KeywordCacheCode.TYPE_NEW.getCode());
                relateKeyword.setStep(0);
            }
            relateKeywordList.add(relateKeyword);
        }

        for (Integer rank = 1; rank <= relateKeywordList.size(); rank++){
            relateKeywordList.get(rank - 1).setRank(rank);
        }

        return relateKeywordList;
    }

}
