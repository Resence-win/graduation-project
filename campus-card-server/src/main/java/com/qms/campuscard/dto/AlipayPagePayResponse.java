package com.qms.campuscard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlipayPagePayResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String outTradeNo;
    private String formHtml;
}
