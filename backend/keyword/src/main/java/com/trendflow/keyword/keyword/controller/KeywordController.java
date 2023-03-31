package com.trendflow.keyword.keyword.controller;

import com.trendflow.keyword.global.code.KeywordCode;
import com.trendflow.keyword.global.exception.NotFoundException;
import com.trendflow.keyword.global.response.BasicResponse;
import com.trendflow.keyword.keyword.dto.response.FindHotKeywordResponse;
import com.trendflow.keyword.keyword.dto.response.FindRecommendKeywordResponse;
import com.trendflow.keyword.keyword.dto.response.FindRelateKeywordResponse;
import com.trendflow.keyword.keyword.dto.response.FindWordCloudResponse;
import com.trendflow.keyword.keyword.entity.Keyword;
import com.trendflow.keyword.keyword.entity.KeywordCount;
import com.trendflow.keyword.keyword.service.KeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/keyword")
public class KeywordController {
    private final KeywordService keywordService;

    @GetMapping("/hot")
    public ResponseEntity<BasicResponse> findHotKeyword(){
        log.info("findHotKeyword - Call");

        try {
            FindHotKeywordResponse findHotKeywordResponse = keywordService.findHotKeyword();
            return ResponseEntity.ok().body(BasicResponse.Body(KeywordCode.SUCCESS, findHotKeywordResponse));
        } catch (NotFoundException e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(BasicResponse.Body(KeywordCode.FAIL, null));
        } catch (RuntimeException e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(BasicResponse.Body(KeywordCode.FAIL, null));
        }
    }

    @GetMapping("/recommend")
    public ResponseEntity<BasicResponse> findRecommendKeyword(){
        log.info("findRecommendKeyword - Call");

        try {
            List<FindRecommendKeywordResponse> findRecommendKeywordResponseList = keywordService.findRecommendKeyword();
            return ResponseEntity.ok().body(BasicResponse.Body(KeywordCode.SUCCESS, findRecommendKeywordResponseList));
        } catch (NotFoundException e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(BasicResponse.Body(KeywordCode.FAIL, null));
        } catch (RuntimeException e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(BasicResponse.Body(KeywordCode.FAIL, null));
        }
    }

    @GetMapping("/relate")
    public ResponseEntity<BasicResponse> findRelateKeyword(@RequestParam String keyword){
        log.info("findRelateKeyword - Call");

        try {
            List<FindRelateKeywordResponse> findRelateKeywordResponseList = keywordService.findRelateKeyword(keyword);
            return ResponseEntity.ok().body(BasicResponse.Body(KeywordCode.SUCCESS, findRelateKeywordResponseList));
        } catch (NotFoundException e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(BasicResponse.Body(KeywordCode.FAIL, null));
        } catch (RuntimeException e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(BasicResponse.Body(KeywordCode.FAIL, null));
        }
    }

    @GetMapping("/wordcloud")
    public ResponseEntity<BasicResponse> findWordCloudKeyword(@RequestParam String keyword){
        log.info("findWordCloudKeyword - Call");

        try {
            List<FindWordCloudResponse> findWordCloudResponseList = keywordService.findWordCloudKeyword(keyword);
            return ResponseEntity.ok().body(BasicResponse.Body(KeywordCode.SUCCESS, findWordCloudResponseList));
        } catch (NotFoundException e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(BasicResponse.Body(KeywordCode.FAIL, null));
        } catch (RuntimeException e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(BasicResponse.Body(KeywordCode.FAIL, null));
        }
    }


    @GetMapping("")
    public ResponseEntity<List<Keyword>> findKeyword(@RequestParam String keyword,
                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate){
        log.info("findKeyword - Call");

        try {
            List<Keyword> keywordList = keywordService.findKeyword(keyword, startDate, endDate);
            return ResponseEntity.ok().body(keywordList);
        } catch (NotFoundException e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/platform")
    public ResponseEntity<List<KeywordCount>> findKeywordCount(@RequestParam String keyword,
                                                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
                                                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate){
        log.info("findKeywordCount - Call");

        try {
            List<KeywordCount> keywordCountList = keywordService.findKeywordCount(keyword, startDate, endDate);
            return ResponseEntity.ok().body(keywordCountList);
        } catch (NotFoundException e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }
}