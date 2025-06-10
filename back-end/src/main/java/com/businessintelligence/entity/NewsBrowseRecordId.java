package com.businessintelligence.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class NewsBrowseRecordId implements Serializable {
    private Integer userId;
    private Integer newsId;

    // 必须实现 equals 和 hashCode，JPA 要求
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewsBrowseRecordId)) return false;
        NewsBrowseRecordId that = (NewsBrowseRecordId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(newsId, that.newsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, newsId);
    }
}

