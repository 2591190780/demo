package com.example.controller.datavisualization;

import com.example.entity.RestBean;
import com.example.entity.records.BestSellingNFT;
import com.example.entity.records.BestSellingProducts;
import com.example.entity.records.MostNFTNumberOwner;
import com.example.entity.records.SalesTrend;
import com.example.service.NFT.NFTTransactionService;
import com.example.service.NFT.UserNFTService;
import com.example.service.transaction.TransactionProcessService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data/visualization")
@Tag(name="可视化",description = "可视化界面接口")
public class DataVisualization {

    @Resource
    TransactionProcessService transactionProcessService;
    @Resource
    UserNFTService userNFTService;

    @Resource
    NFTTransactionService nftTransactionService;

    @GetMapping("/sales/trend")
    public void salesTrend(HttpServletResponse response,
                           @RequestParam(required = false)
                           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDay,

                           @RequestParam(required = false)
                           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDay) throws IOException {

        response.setContentType("application/json;charset=utf-8");

        LocalDate today = LocalDate.now();
        if (endDay == null) endDay = today;
        if (startDay == null) startDay = today.minusDays(30);

        // 转成左闭右开 LocalDateTime
        LocalDateTime startDateTime = startDay.atStartOfDay();
        LocalDateTime endDateTime = endDay.plusDays(1).atStartOfDay();

        List<SalesTrend> list = this.transactionProcessService
                .getTransactionMoneyByDate(startDateTime, endDateTime);

        response.getWriter()
                .write(list.isEmpty()
                        ? RestBean.success(null).asJsonString()
                        : RestBean.success(list).asJsonString());
    }

    @GetMapping("/best/selling/products")
    public void bestSellingProduct(HttpServletRequest request,HttpServletResponse response,
                                   @RequestParam(required = false)
                                   @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDay,
                                   @RequestParam(required = false)
                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDay
    ) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        LocalDate today = LocalDate.now();
        if (endDay == null) endDay = today;
        if (startDay == null) startDay = today.minusDays(30);

        // 转成左闭右开 LocalDateTime
        LocalDateTime startDateTime = startDay.atStartOfDay();
        LocalDateTime endDateTime = endDay.plusDays(1).atStartOfDay();

        List<BestSellingProducts> list = this.transactionProcessService.getBestSellingProductsByDate(startDateTime,endDateTime);
        response.getWriter()
                .write(list.isEmpty()
                        ? RestBean.success(null).asJsonString()
                        : RestBean.success(list).asJsonString());
    }

    @GetMapping("/most/nft/number/owner")
    public void mostNFTNumberOwner(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        List<MostNFTNumberOwner> list = this.userNFTService.getMostNFTNumberOwner();
        response.getWriter()
                .write(
                        list.isEmpty()
                                ? RestBean.success(null).asJsonString()
                                : RestBean.success(list).asJsonString()
                );
    }

    @GetMapping("/sales/nft/trend")
    public void salesNFTTrend(
            HttpServletRequest request,HttpServletResponse response,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDay,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDay
    )throws IOException{
        response.setContentType("application/json;charset=utf-8");

        LocalDate today = LocalDate.now();
        if (endDay == null) endDay = today;
        if (startDay == null) startDay = today.minusDays(30);

        // 转成左闭右开 LocalDateTime
        LocalDateTime startDateTime = startDay.atStartOfDay();
        LocalDateTime endDateTime = endDay.plusDays(1).atStartOfDay();

        List<BestSellingNFT> list = this.nftTransactionService.getSalesNFTTrend(startDateTime,endDateTime);
        response.getWriter()
                .write(list.isEmpty()
                        ? RestBean.success(null).asJsonString()
                        : RestBean.success(list).asJsonString());
    }

}
