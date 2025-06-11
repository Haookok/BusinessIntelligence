package com.businessintelligence.service;

import com.businessintelligence.entity.NewsBrowseRecord;

import java.util.List;

public interface BrowseRecordService {
    List<NewsBrowseRecord> getRecentBrowsedRecords(Integer userId, Integer timestamp);
}