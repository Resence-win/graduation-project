package com.qms.campuscard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadMaterialResponse {

    private String url;

    private String name;

    private String type;
}
