package com.example.controller.blockchain;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.vo.request.ContractCallRequest;
import com.example.utils.WeBaseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contract")
@Tag(name="智能合约",description = "相关操作")
@RequiredArgsConstructor
public class ContractController {

    private final WeBaseUtils weBaseUtils;

    // 获取所有简化合约信息
    @Auditable(
            operationType = "CONTRACT_LIST",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/list")
    public void getSimplifiedContractList(HttpServletResponse response) throws IOException {
        List<WeBaseUtils.SimplifiedContractInfo> contracts = weBaseUtils.getAllSimplifiedContracts();
        response.setContentType("application/json;charset=UTF-8");

        if (!contracts.isEmpty()) {
            response.getWriter().write(RestBean.success(contracts).asJsonString());
        } else {
            response.getWriter().write(RestBean.failure(404, "未找到合约信息").asJsonString());
        }
    }

    @Auditable(
            operationType = "CONTRACT_SIMPLE_INFO",
            captureBefore = true,
            captureAfter = true
    )
    // 获取特定合约的简化信息
    @GetMapping("/info")
    public void getContractInfo(@RequestParam String address, HttpServletResponse response) throws IOException {
        WeBaseUtils.SimplifiedContractInfo contract = weBaseUtils.getSimplifiedContractInfo(address);
        response.setContentType("application/json;charset=UTF-8");

        if (contract != null) {
            response.getWriter().write(RestBean.success(contract).asJsonString());
        } else {
            response.getWriter().write(RestBean.failure(404, "未找到合约: " + address).asJsonString());
        }
    }

    @Auditable(
            operationType = "CONTRACT_ABI",
            captureBefore = true,
            captureAfter = true
    )
    // 获取合约ABI
    @GetMapping("/abi")
    public void getContractAbi(@RequestParam String address, HttpServletResponse response) throws IOException {
        String abi = weBaseUtils.getContractAbi(address);
        response.setContentType("application/json;charset=UTF-8");
        if (!abi.isEmpty()) {
            response.getWriter().write(RestBean.success(abi).asJsonString());
        } else {
            response.getWriter().write(RestBean.failure(404, "无法获取ABI: " + address).asJsonString());
        }
    }


    @Auditable(
            operationType = "CONTRACT_CALL",
            captureBefore = true,
            captureAfter = true
    )
    @PostMapping("/call")
    public ResponseEntity<?> callContractMethod(@RequestBody ContractCallRequest request) {
        // 参数校验
        if (request.getContractAddress() == null || request.getContractAddress().isEmpty()) {
            return ResponseEntity.badRequest().body("合约地址不能为空");
        }
        if (request.getMethodName() == null || request.getMethodName().isEmpty()) {
            return ResponseEntity.badRequest().body("方法名不能为空");
        }
        if (request.getUserAddress() == null || request.getUserAddress().isEmpty()) {
            return ResponseEntity.badRequest().body("用户地址不能为空");
        }
        try {
            // 调用工具类执行合约方法
            Map<String, Object> result = weBaseUtils.callContractMethod(
                    request.getUserAddress(),
                    request.getContractAddress(),
                    request.getMethodName(),
                    request.getParams() != null ? request.getParams() : List.of()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("调用失败: " + e.getMessage());
        }
    }
}