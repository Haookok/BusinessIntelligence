// 文件路径：com/businessintelligence/dto/NewsLifecycleDTO.java
package com.businessintelligence.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsLifecycleDTO {
    private Integer newsId;
    private String headline;
    private Integer totalBrowseNum;
    private Integer totalBrowseDuration;
}
