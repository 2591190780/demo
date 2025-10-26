package com.example.entity.vo.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SalesTrend(BigDecimal money, LocalDate time) {}
