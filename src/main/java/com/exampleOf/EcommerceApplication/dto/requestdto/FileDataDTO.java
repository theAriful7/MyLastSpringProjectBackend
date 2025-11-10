package com.exampleOf.EcommerceApplication.dto.requestdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDataDTO {
    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String altText;
    private Integer sortOrder;
    private Boolean isPrimary;
    private String mimeType;
}
