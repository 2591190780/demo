package com.example.controller.blockchain;

import com.alipay.api.domain.AccountDTO;
import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.vo.request.ContractCallRequest;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.service.AccountService;
import com.example.service.blockchain.ConditionNFTRule;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import com.example.utils.WeBaseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/contract")
@Tag(name="智能合约",description = "相关操作")
@RequiredArgsConstructor
public class ContractController {

    private final WeBaseUtils weBaseUtils;
    @Resource
    ConditionNFTRule conditionNFTRule;
    @Resource
    JwtUtils jwtUtils;
    @Resource
    AccountService accountService;

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


    @Auditable(
            operationType = "CONTRACT_RULE_SELECT",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/nft/rule/report")
    public <T> RestBean<T> ruleReport(HttpServletRequest request,
                                      @RequestBody @Valid Map<String, Object> body) throws Exception {

        String nftID = (String) body.get("nftID");
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> ruleList = objectMapper.convertValue(
                body.get("ruleList"),
                new TypeReference<List<Map<String, Object>>>() {});
        Integer id = this.jwtUtils.getRequesetId(request);
        //智能合约只能由管理员上传。
        if(!Objects.equals(accountService.findAccountById(id).getRole(), "3"))
            return RestBean.failure(401,"权限不足");
        if (
        this.conditionNFTRule.NFTRuleReport(accountService.findAccountById(id).getWalletAddress(),
                jwtUtils.convertToInteger(nftID),ruleList))
            return  RestBean.success();
        return RestBean.failure(500,"请检查参数");
    }


    @Auditable(
            operationType = "CONTRACT_RULE_REPORT",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/nft/rule/bc/select")
    public <T>RestBean<T> findRuleInBC(HttpServletRequest request,HttpServletResponse response,
                                       @RequestParam("nftID") String nftID) throws Exception {
        String userCA = accountService.findAccountById(jwtUtils.getRequesetId(request)).getWalletAddress();
        if (userCA==null) return RestBean.failure(401,"错误的账户");
        List<Object> param = new ArrayList<>();
        param.add(0,jwtUtils.convertToInteger(nftID));
        Map<String, Object> result
                = weBaseUtils.callContractMethod(userCA,
                Const.CONTRACT_FOR_NFT_RULE,
                Const.CONTRACT_FOR_NFT_RULE_METHOD_GETRULEBYTOKENID
                ,param);
        System.out.println(result);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(RestBean.success(result.get("data")).asJsonString());
        return  null;

    }


    @Auditable(
            operationType = "CONTRACT_FIND_SATISFIED_USER",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/nft/satisfied/rule")
    public <T>RestBean<T> selectSatisfiedRuleUser(HttpServletRequest request,HttpServletResponse response,
                                       @RequestParam("nftID") String nftID,
                                                  @RequestParam("conditionList") List<String> conditionList) throws Exception {

        List<AuthorizeVO> dtoList  = this.conditionNFTRule.selectSatisfyCondition(request
                ,jwtUtils.convertToInteger(nftID),conditionList);
        if(dtoList != null && !dtoList.isEmpty()){
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(RestBean.success(dtoList).asJsonString());
            return null;
        }
        return RestBean.failure(401,"未查询到满足条件的用户。");
    }



}