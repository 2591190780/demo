package com.example.entity.vo.request;

import com.example.entity.vo.response.PendingApplicationVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

import java.util.List;

@Data
@AllArgsConstructor
public class ApplyHandlingRequestVO {
    private List<PendingApplicationVO> data;
    private byte answer;
}
