package com.example.entity;

import com.example.entity.dto.NFTTransactionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddTransactionRequest {
    private Integer ans;
    private NFTTransactionDto dto;
}
