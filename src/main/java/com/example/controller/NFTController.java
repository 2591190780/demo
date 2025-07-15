package com.example.controller;

import com.example.annotation.Auditable;
import com.example.entity.NFTPendingApplication;
import com.example.entity.RestBean;
import com.example.entity.dto.NFTInfoDto;
import com.example.entity.dto.NFTRuleDto;
import com.example.entity.dto.NFTTransactionDto;
import com.example.entity.dto.UserNFTDto;
import com.example.service.AccountService;
import com.example.service.NFT.*;
import com.example.utils.JwtUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/api/nft")
@Tag(name = "NFT",description = "NFT操作")
public class NFTController {

    @Resource
    NFTInfoService nftInfoService;
    @Resource
    NFTRuleService nftRuleService;
    @Resource
    NFTAddInfoService nftAddInfoService;
    @Resource
    NFTAddRuleService nftAddRuleService;

    @Resource
    NFTTransactionService nftTransactionService;
    @Resource
    UserNFTService userNFTService;

    @Resource
    JwtUtils  jwtUtils;

    @Auditable(
            operationType = "USER_NFT_NFTID_API_NFT",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/user/nft/NFTID")
    public <T> RestBean<T> userNFTSelectNFTID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @RequestParam("userID") String id) throws IOException {
        List<UserNFTDto> dtoList = this.userNFTService.selectNFTByNFTid(jwtUtils.convertToInteger(id));
        if(dtoList == null){
            return RestBean.failure(401,"暂时没有NFT");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(dtoList).asJsonString());
        return null;
    }

    @Auditable(
            operationType = "USER_NFT_SELECT_USERID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/user/nft/selectUserID")
    public <T> RestBean<T> userNFTSelectUserID(HttpServletResponse response,
                                         HttpServletRequest request,
                                         @RequestParam("userID") String id) throws IOException {
        List<UserNFTDto> dtoList = this.userNFTService.selectNFTByUid(jwtUtils.convertToInteger(id));
        if(dtoList == null){
            return RestBean.failure(401,"暂时没有NFT");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(dtoList).asJsonString());
        return null;
    }

    @Auditable(
            operationType = "USER_NFT_SELECT_ID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/user/nft/selectID")
    public <T> RestBean<T> userNFTSelect(HttpServletResponse response,
                                         HttpServletRequest request,
                                         @RequestParam("ID") String id) throws IOException {
        UserNFTDto dto = this.userNFTService.selectNFTById(jwtUtils.convertToInteger(id));
        if(dto == null){
            return RestBean.failure(401,"暂时没有NFT");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(dto).asJsonString());
        return null;
    }


    @Auditable(
            operationType = "NFT_TRANSACTION_SELECT_BY_NFT_ID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/transaction/selectbyNFTID")
    public <T> RestBean<T> selectbyNFTID(@RequestParam("NFTID") String id,
                                         HttpServletResponse response) throws IOException {
        List<NFTTransactionDto> dtoList =  this.nftTransactionService.selectNFTTransactionByNFTId(
                jwtUtils.convertToInteger(id));
        if (dtoList.isEmpty()) {
            return RestBean.failure(401,"暂时没有此NFT交易信息");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(dtoList).asJsonString());
        return null;
    }

    @Auditable(
            operationType = "NFT_TRANSACTION_SELECT_ID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/transaction/selectID") //查看NFT最新的获取规则 （根据ID）
    public <T> RestBean<T> selectByID(HttpServletResponse response,
                                      @RequestParam("ID") String id) throws IOException {
        NFTTransactionDto dto = this.nftTransactionService.selectNFTTransactionById(jwtUtils.convertToInteger(id));
        if (dto == null) {
            return RestBean.failure(401,"暂无该NFT的发行规则");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(dto).asJsonString());
        return null;
    }

    @Auditable(
            operationType = "NFT_TRANSACTION_SELECT_BY_FORM_ID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/transaction/selectbyfromID")
    public <T> RestBean<T> selectbyfromid(@RequestParam("fromID") String fromID,
                                          HttpServletResponse response) throws IOException {
        List<NFTTransactionDto> dtoList =  this.nftTransactionService.selectNFTTransactionByFromId(
                jwtUtils.convertToInteger(fromID));
        if (dtoList.isEmpty()) {
            return RestBean.failure(401,"暂时没有此NFT交易信息");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(dtoList).asJsonString());
        return null;
    }

    @Auditable(
            operationType = "NFT_TRANSACTION_SELECT_BY_TO_ID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/transaction/selectbytoID")
    public <T> RestBean<T> selectbytoid(@RequestParam("toId") String toId,
                                        HttpServletResponse response) throws IOException {
        List<NFTTransactionDto> dtoList =  this.nftTransactionService.selectNFTTransactionByToId(
               jwtUtils.convertToInteger(toId));
        if (dtoList.isEmpty()) {
            return RestBean.failure(401,"暂时没有此NFT交易信息");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(dtoList).asJsonString());
        return null;
    }

    @Auditable(
            operationType = "NFT_TRANSACTION_SELECT_CONDITION",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/transaction/selectCondition")
    public <T> RestBean<T> selectCondition(@RequestBody NFTTransactionDto nftTransactionDto,
                                           HttpServletResponse response) throws IOException {
        List<NFTTransactionDto> dtoList = this.nftTransactionService.selectNFTCondition(nftTransactionDto);
        if (dtoList.isEmpty()) {
            return RestBean.failure(401,"暂时没有此NFT交易信息");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(dtoList).asJsonString());
        return null;
    }

    @Auditable(
            operationType = "NFT_RULE_ACTIVE",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/nftRule/active") //查看NFT最新的获取规则 （根据ID）
    public <T> RestBean<T> nftRuleActive(HttpServletResponse response,
                                         @RequestParam String id) throws IOException {
       NFTTransactionDto dto = this.nftTransactionService.selectOrderByTime(jwtUtils.convertToInteger(id));
       if (dto == null) {
           return RestBean.failure(401,"暂无该NFT的发行规则");
       }
        response.setContentType("application/json;Charset=utf-8");
       response.getWriter().write(RestBean.success(dto).asJsonString());
       return null;
    }

    @Auditable(
            operationType = "NFT_TRANSACTION_APPLY",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/transaction/apply")
    public <T> RestBean<T> applyNFTTransaction(HttpServletRequest request,
                                               @RequestBody NFTPendingApplication application){

        if(!jwtUtils.getRequesetId(request).equals(
                jwtUtils.convertToInteger(application.getBuyerID())))
            return RestBean.failure(402,"申请人异常。");
        if(
                this.nftInfoService.NFTInfoSelectByTemplateId(
                        jwtUtils.convertToInteger(application.getNftID())).getIsActive()==0
        ){return RestBean.failure(401,"NFT暂未激活。");}
        return this.nftTransactionService.buyApplyForNFT(application)?
                RestBean.success():RestBean.failure(401,"请勿重复提交申请。");
    }

    @Auditable(
            operationType = "NFT_TRANSACTION_PAY",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/transaction/pay") //同意交易申请
    public <T> RestBean<T> addTransaction( HttpServletRequest request,
            @RequestBody NFTTransactionDto dto) throws Exception {
        Integer uid = jwtUtils.getRequesetId(request);
        if(!Objects.equals(uid, dto.getFromUser())){return RestBean.failure(401,"权限不足");}
        return this.nftTransactionService.addAgreeNFTTransaction(dto)?RestBean.success()
                  :RestBean.failure(401,"交易失败");
    }


    @Auditable(
            operationType = "NFT_TRANSACTION_GET",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/transaction/get")
    public <T> RestBean<T> getTransaction(HttpServletRequest request,
                                          HttpServletResponse response) throws IOException {

        if(this.nftTransactionService.getApplyForNFT(request)==null){
            return RestBean.failure(401,"暂时没有交易信息。");
        }
        List<NFTPendingApplication> applications = this.nftTransactionService.getApplyForNFT(request);
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(applications).asJsonString());
        return null;
    }



    @Auditable(
            operationType = "RULE_ADD_SINGLE",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/rule/addSingle") //此处只提交NFT发布规则给管理员查看。
    public <T> RestBean<T> addSingleRule(HttpServletRequest request,
                                         @RequestBody NFTRuleDto dto) {
        return this.nftAddRuleService.NFTRuleAddSingle(request,dto)?
                RestBean.success():RestBean.failure(401,"添加失败，信息有误");
    }

    @Auditable(
            operationType = "RULE_SELECT_ID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/rule/select/id")
    public <T> RestBean<T> selectRule(HttpServletRequest request,
                                      HttpServletResponse response
            ,@RequestParam String id) throws IOException {
          Integer tid = jwtUtils.convertToInteger(id);
            NFTRuleDto dto = this.nftRuleService.nftRuleSelectByID(tid);
          if(dto!=null){
              response.setContentType("application/json;Charset=utf-8");
              response.getWriter().write(RestBean.success(dto).asJsonString());
              return null;
          }
          return RestBean.failure(401,"暂无该NFT规则");
    }

    @Auditable(
            operationType = "RULE_SELECT_NAME",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/rule/select/name")
    public <T>RestBean<T> selectRuleByName(HttpServletRequest request,
                                           HttpServletResponse response
    ,@RequestParam String name) throws IOException {
        List<NFTRuleDto> list = this.nftRuleService.nftRuleSelectByName(name);
        if(!list.isEmpty()){
            response.setContentType("application/json;Charset=utf-8");
            response.getWriter().write(RestBean.success(list).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT规则");
    }

    @Auditable(
            operationType = "RULE_SELECT_TEMPLATE_ID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/rule/select/templateid")
    public <T>RestBean<T> selectRuleByTemplateId(HttpServletRequest request,
                                                 HttpServletResponse response
    ,@RequestParam String templateId) throws IOException {
             Integer tid = jwtUtils.convertToInteger(templateId);
             List<NFTRuleDto> dto = this.nftRuleService.nftRuleSelectByTemplateID(tid);
             if(dto!=null){
                 response.setContentType("application/json;Charset=utf-8");
                 response.getWriter().write(RestBean.success(dto).asJsonString());
                 return null;
             }
        return RestBean.failure(401,"暂无该NFT规则");
    }

    @Auditable(
            operationType = "RULE_SELECT_HASH",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/rule/select/hash")
    public <T>RestBean<T> selectRuleByHash(HttpServletRequest request,
                                           HttpServletResponse response
            ,@RequestParam String hash) throws IOException {
            NFTRuleDto ruleDto = this.nftRuleService.nftRuleSelectByHash(hash);
            if(ruleDto!=null){
                response.setContentType("application/json;Charset=utf-8");
                response.getWriter().write(RestBean.success(ruleDto).asJsonString());
                return null;
            }
        return RestBean.failure(401,"暂无该NFT规则");
    }

    @Auditable(
            operationType = "RULE_SELECT_CONDITION",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/rule/select/condition")
    public <T>RestBean<T> selectRuleByCondition(HttpServletRequest request,
                                                HttpServletResponse response
    ,@RequestBody NFTRuleDto dto) throws IOException {
            List<NFTRuleDto> dtoList = this.nftRuleService.nftRuleSelectCondition(dto);
            if(dtoList!=null){
                response.setContentType("application/json;Charset=utf-8");
                response.getWriter().write(RestBean.success(dtoList).asJsonString());
                return null;
            }
            return RestBean.failure(401,"暂无该NFT规则");
    }

    @Auditable(
            operationType = "NFT_INFO_ADD_SINGLE",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/info/addSingle")
    public <T> RestBean<T> addSingleInfo(HttpServletRequest request,
                                         @RequestBody NFTInfoDto dto){
        return this.nftAddInfoService.NFTInfoAddSingle(request,dto)?
                RestBean.success():RestBean.failure(401,"添加失败，信息有误");
    }

    @Auditable(
            operationType = "NFT_INFO_SELECT",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/info/select")
    public <T> RestBean<T> selectNFT (HttpServletResponse response,
                                      NFTInfoDto dto) throws IOException {
        List<NFTInfoDto> dtoList = this.nftInfoService.infoSelectByCondition(dto);
        if (!dtoList.isEmpty()){
            response.setContentType("application/json;Charset=utf-8");
            response.getWriter().write(RestBean.success(dtoList).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT");
    }

    @Auditable(
            operationType = "NFT_INFO_SELECT_URL",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/info/select/url")
    public  <T> RestBean<T> selectNFTUrl (@RequestParam String url,
                                          HttpServletResponse response) throws IOException {
        NFTInfoDto dto =  this.nftInfoService.NFTInfoSelectByURL(url);
        if (dto!=null){
            response.setContentType("application/json;Charset=utf-8");
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT");
    }

    @Auditable(
            operationType = "NFT_INFO_SELECT_TEMPLATE_ID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/info/select/templateid")
    public  <T> RestBean<T> selectNFTID (@RequestParam String id,
                                         HttpServletResponse response) throws IOException {
        NFTInfoDto dto =  this.nftInfoService.NFTInfoSelectByTemplateId(jwtUtils.convertToInteger(id));
        if (dto!=null){
            response.setContentType("application/json;Charset=utf-8");
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT");
    }

    @Auditable(
            operationType = "NFT_INFO_SELECT_PUBLIC_ID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/info/select/publicid")
    public <T> RestBean<T> selectPublicID(@RequestParam String id,
                                          HttpServletResponse response) throws IOException {
        List<NFTInfoDto> dtoList = this.nftInfoService.NFTInfoSelectByPublic(jwtUtils.convertToInteger(id));
        if (!dtoList.isEmpty()){
            response.setContentType("application/json;Charset=utf-8");
            response.getWriter().write(RestBean.success(dtoList).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT");
    }


    @Resource
    AccountService accountService;

    @Auditable(
            operationType = "NFT_CONTRACT_ADD",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/nft/contract/")
    public  <T> RestBean<T> nftContractAdd (@RequestParam String id,
                                         @RequestParam String contractAddress,
                                         HttpServletRequest request)  {
        Integer uid = jwtUtils.getRequesetId(request);
        if (!Objects.equals(accountService.findAccountById(uid).getRole(), "3"))
            return RestBean.failure(401,"权限不足。");
        boolean flag = this.nftInfoService.nftUpdateContractAdmin(contractAddress,jwtUtils.convertToInteger(id));
        return flag ? RestBean.success():RestBean.failure(401,"请检查参数。");
    }


}
