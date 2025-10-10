package com.example.controller.blockchain;

import com.alipay.api.domain.AccountDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.annotation.Auditable;
import com.example.controller.ControllerPageHelper;
import com.example.entity.PageParam;
import com.example.entity.PageResult;
import com.example.entity.RestBean;
import com.example.entity.dto.*;
import com.example.entity.vo.request.ContractCallRequest;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.entity.vo.response.BlockChainResultVO;
import com.example.entity.vo.response.NFTRuleVO;
import com.example.mapper.NFT.NFTTransactionMapper;
import com.example.service.AccountService;
import com.example.service.BlockChainEvidenceService;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTRuleService;
import com.example.service.NFT.NFTTransactionService;
import com.example.service.blockchain.ConditionNFTRule;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import com.example.utils.WeBaseUtils;
import com.google.gson.Gson;
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
import java.util.*;

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
    @Resource
    NFTInfoService nftInfoService;
    @Resource
    NFTRuleService nftRuleService;
    @Resource
    NFTTransactionService nftTransactionService;
    @Resource
    BlockChainEvidenceService blockChainEvidenceService;
    @Resource
    NFTTransactionMapper nftTransactionMapper;

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
    public void ruleReport(HttpServletRequest request,HttpServletResponse response,
                                      @RequestBody @Valid Map<String, Object> body) throws Exception {

        String nftID = (String) body.get("nftID");
        Integer rId = jwtUtils.convertToInteger((String) body.get("ruleID")) ;
        Integer nId = jwtUtils.convertToInteger(nftID);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> ruleList = objectMapper.convertValue(
                body.get("ruleList"),
                new TypeReference<List<Map<String, Object>>>() {});
        Integer id = this.jwtUtils.getRequesetId(request);

        response.setContentType("application/json;charset=UTF-8");
        //智能合约只能由管理员上传。
        if(!Objects.equals(accountService.findAccountById(id).getRole(), "3")){
            response.getWriter().write(RestBean.failure(401,"权限不足").asJsonString());
        return;
        }
        if (
        this.conditionNFTRule.NFTRuleReport(accountService.findAccountById(id).getWalletAddress(),
                nId ,ruleList)
        ){
           if ( this.nftRuleService.updateStatusRuleInChainNode(rId , nId,1)) {
               response.getWriter().write(RestBean.success().asJsonString());
               return ;
           }
            response.getWriter().write(RestBean.failure(500,"该规则已上链，请勿重复提交。").asJsonString());
            return ;
        }
        response.getWriter().write(RestBean.failure(500,"请检查参数").asJsonString());
        return ;
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
        if(nftRuleService.nftRuleSelectByActId(jwtUtils.convertToInteger(nftID))==null){
            return RestBean.failure(401,"查询不到该NFT规则。");
        }
        List<Object> param = new ArrayList<>();
        param.add(0,jwtUtils.convertToInteger(nftID));
        Map<String, Object> result
                = weBaseUtils.callContractMethod(userCA,
                Const.CONTRACT_FOR_NFT_RULE,
                Const.CONTRACT_FOR_NFT_RULE_METHOD_GETRULEBYTOKENID
                ,param);
        System.out.println(result);
        NFTInfoDto nftInfoDto = this.nftInfoService.NFTInfoSelectByTemplateId(jwtUtils.convertToInteger(nftID));
        NFTRuleDto nftRuleDto = this.nftRuleService.nftRuleSelectByActId(jwtUtils.convertToInteger(nftID));
        Account account = this.accountService.findAccountById(nftInfoDto.getPublicBy());

        //构建返回参数
        NFTRuleVO nftRuleVO = new NFTRuleVO();
        //NFT基本信息
        nftRuleVO.setContractAddress(nftInfoDto.getContractAddress()); //NFT合约地址
        nftRuleVO.setTemplateId(jwtUtils.convertToInteger(nftID)); //NFT id
        nftRuleVO.setName(nftInfoDto.getName()); // NFT名称
        nftRuleVO.setDescription(nftInfoDto.getDescription()); //NFT 描述
        nftRuleVO.setImageUrl(nftInfoDto.getImageUrl()); // NFT 图像信息
        nftRuleVO.setNftLevel(nftInfoDto.getNftLevel());  //NFT 等级
        nftRuleVO.setIsActive(nftInfoDto.getIsActive()); // NFT 激活状态
        nftRuleVO.setIssuanceLimit(nftInfoDto.getIssuanceLimit()); // NFT 发行限制数量
        nftRuleVO.setRemainCount(nftInfoDto.getRemainCount()); //NFT 剩余数量
        nftRuleVO.setCreatedAt(nftInfoDto.getCreatedAt()); //NFT 创建时间
        nftRuleVO.setMetadataUrl(nftInfoDto.getMetadataUrl());  // NFT 元数据
        nftRuleVO.setObjectDimension(nftRuleDto.getObjectDimension());//聚合维度
        //NFT发布者信息
        nftRuleVO.setPublicBy(nftInfoDto.getPublicBy());  // NFT 发布者的ID
        nftRuleVO.setPublicName(account.getUsername()); //NFT 发布者的昵称
        nftRuleVO.setPublicEmail(account.getEmail()); // NFT 发布者的邮箱
        nftRuleVO.setPublicWalletAddress(account.getWalletAddress()); //NFT发布者的钱包地址
        nftRuleVO.setPublicImageUrl(account.getUserImgurl()); //NFT 发布者的头像CID
        //NFT链上规则信息、链下描述信息
        Gson gson = new Gson();
        String dataStr = gson.toJson(result.get("data"));
        nftRuleVO.setRules(dataStr);  // NFT 链上规则信息
        nftRuleVO.setRuleDescription(nftRuleDto.getRuleDescription()); // NFT的规则描述

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(RestBean.success(nftRuleVO).asJsonString());
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
                                                  @RequestParam("conditionList") List<String> conditionList,
                                                  @ModelAttribute PageParam pageParam) throws Exception {
//*  conditionList
        //检查nft的规则信息链上是否存在。
        if (this.nftRuleService.nftRuleSelectByActId(jwtUtils.convertToInteger(nftID)).getRuleInchainnode()==0){
            return RestBean.failure(401,"该规则链上信息不存在。");
        }
        List<Account> dtoList  = this.conditionNFTRule.selectSatisfyCondition(request
                ,jwtUtils.convertToInteger(nftID),conditionList);
        if(dtoList != null && !dtoList.isEmpty()){

            PageResult<Account> pageResult = ControllerPageHelper.paginateList(
                    dtoList, pageParam
            );

            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(RestBean.success(pageResult).asJsonString());
            return null;
        }
        return RestBean.failure(401,"未查询到满足条件的用户。");
    }

    @GetMapping("/nft/satisfied/rule/pre/check")
    public <T>RestBean<T> selectSatisfiedRulePreCheck(HttpServletRequest request,HttpServletResponse response,
                                                  @RequestParam("nftID") String nftID) throws Exception {
        //检查nft的规则信息链上是否存在。
        NFTRuleDto dto = this.nftRuleService.nftRuleSelectByActId(jwtUtils.convertToInteger(nftID));
        if (dto.getRuleInchainnode() == 0) {
            return RestBean.failure(401, "该规则链上信息不存在。");
        }else{
            return RestBean.success();
        }
    }

    @GetMapping("/nft/transaction/result")
    public void transactionResult(HttpServletRequest request,HttpServletResponse response,
                                                      @RequestParam("relateId") Integer txId) throws Exception {
        response.setContentType("application/json;charset=UTF-8");

        Integer userId = this.jwtUtils.getRequesetId(request);
        NFTTransactionDto nftTransactionDto = this.nftTransactionService.selectNFTTransactionById(txId);
        List<BlockChainEvidenceDto> blockChainEvidenceDtoList = this.blockChainEvidenceService.selectInfoByRelateId(txId);
        NFTInfoDto nftInfoDto = this.nftInfoService.NFTInfoSelectByTemplateId(nftTransactionDto.getNftId());
        Account account = this.accountService.findAccountById(userId);
        if(blockChainEvidenceDtoList==null || blockChainEvidenceDtoList.isEmpty()){
            response.getWriter().write(RestBean.failure(401,"认证查询失败，，暂未查询到认证信息。").asJsonString());
            return;
        }
        if(blockChainEvidenceDtoList.size()!=3 ){
            response.getWriter().write(RestBean.failure(401,"交易记录不完整。").asJsonString());
            return;
        }
        List<Object> param = new ArrayList<>();
        param.add(0,blockChainEvidenceDtoList.get(0).getSubmitHash());

        Map<String, Object> result = weBaseUtils.callContractMethod(
                account.getWalletAddress(),
                Const.CONTRACT_FOR_MESSAGE_REPORT,
                Const.CONTRACT_FOR_MESSAGE_REPORT_METHOD_GETFULLEVIDENCEBYHASH,
                param
        );

        ObjectMapper mapper = new ObjectMapper();
        ArrayList<?> data = (ArrayList<?>) result.get("data");
        String dataStr = (String) data.get(2);
        // 去掉首尾空格和中括号
        dataStr = dataStr.trim();
        if (dataStr.startsWith("[") && dataStr.endsWith("]")) {
            dataStr = dataStr.substring(1, dataStr.length() - 1);
        }else{

            response.getWriter().write(RestBean.failure(401,"未查询到对应记录。").asJsonString());
            return;
        }
        // 用 Jackson 解析成 List<String>
        List<String> addressList = mapper.readValue("[" + dataStr + "]", new TypeReference<List<String>>() {});
        // 提取 0x 地址
        String[] address = addressList.toArray(new String[0]);

        BlockChainResultVO vo = new BlockChainResultVO(
            address[1],address[0],nftInfoDto,blockChainEvidenceDtoList.get(0).getTxHash()
                ,blockChainEvidenceDtoList.get(2).getTxHash(),nftTransactionDto.getType()
        );
        response.getWriter().write(RestBean.success(vo).asJsonString());
        return ;
    }

    @GetMapping("/nft/myTransaction")
    public void getMyTransaction(
            HttpServletRequest request, HttpServletResponse response,
            @ModelAttribute PageParam pageParam
    )throws Exception{
        response.setContentType("application/json;charset=UTF-8");
        Integer userId = this.jwtUtils.getRequesetId(request);
        Page<NFTTransactionDto> page = this.nftTransactionMapper.selectMyTransaction(pageParam.toPage(),userId);
        if (page.getTotal() == 0) {
            response.getWriter().write(RestBean.failure(401,"未查询到您的NFT交易记录。").asJsonString());
            return;
        }
        PageResult<NFTTransactionDto> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                (int) page.getCurrent(),
                (int) page.getPages(),
                (int) page.getSize()
        );
        response.getWriter().write(RestBean.success(pageResult).asJsonString());
        return;
    }

}