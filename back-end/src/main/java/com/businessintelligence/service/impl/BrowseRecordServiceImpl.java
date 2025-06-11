package com.businessintelligence.service.impl;

import com.businessintelligence.entity.NewsBrowseRecord;
import com.businessintelligence.repository.NewsBrowseRecordRepository;
import com.businessintelligence.service.BrowseRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrowseRecordServiceImpl implements BrowseRecordService {

    private final NewsBrowseRecordRepository browseRecordRepository;

    public BrowseRecordServiceImpl(NewsBrowseRecordRepository browseRecordRepository) {
        this.browseRecordRepository = browseRecordRepository;
    }

    @Override
    public List<NewsBrowseRecord> getRecentBrowsedRecords(Integer userId, Integer timestamp) {
        return browseRecordRepository.findTop20ByUserIdAndStartTsGreaterThanOrderByStartTsDesc(userId, timestamp);
    }
}
