package com.hrpayroll.common;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
