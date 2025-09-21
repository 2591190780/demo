package com.example.controller;

import com.alipay.api.domain.AccountVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.annotation.Auditable;
import com.example.entity.*;
import com.example.entity.dto.*;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.entity.vo.response.NFTCollectionVO;
import com.example.entity.vo.response.TransactionInfoVO;
import com.example.entity.vo.response.UserNFTVO;
import com.example.mapper.NFT.NFTInfoMapper;
import com.example.mapper.NFT.NFTTransactionMapper;
import com.example.mapper.NFT.UserNFTMapper;
import com.example.service.AccountService;
import com.example.service.NFT.*;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


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
    AccountService accountService;
    @Resource
    NFTTransactionService nftTransactionService;
    @Resource
    UserNFTService userNFTService;
    @Resource
    JwtUtils  jwtUtils;
    @Resource
    UserNFTMapper userNFTMapper;
    @Resource
    NFTTransactionMapper nftTransactionMapper;
    @Resource
    StringRedisTemplate stringRedisTemplate;


    @GetMapping("/transaction/get/all")
    public <T> RestBean<T> getAllNFT(@ModelAttribute PageParam pageParam,HttpServletResponse response)
            throws IOException {
        Page<NFTTransactionDto> page = this.nftTransactionMapper.selectPage(
                pageParam.toPage()
        );
        if (page.getTotal() == 0) {
            return RestBean.failure(401, "暂无NFT。");
        }
        List<NFTTransactionDto> dtoList = page.getRecords();
        // 构建分页结果
        PageResult<NFTTransactionDto> pageResult = new PageResult<>(
                page.getTotal(),
                dtoList,
                (int) page.getCurrent(),
                (int) page.getPages(),
                (int) page.getSize()
        );
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(pageResult).asJsonString());
        return null;
    }



    @GetMapping("/user/nft/NFTID")
    public <T> RestBean<T> userNFTSelectNFTID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @RequestParam("nftID") String id,
                                              @ModelAttribute PageParam pageParam) throws IOException {
        Integer nid = jwtUtils.convertToInteger(id);
        List<UserNFTDto> dtoList = this.userNFTService.selectNFTByNFTid(nid);
        if (dtoList == null) {
            return RestBean.failure(401, "暂时没人拥有NFT");
        }
        List<UserNFTVO> voList = new ArrayList<>();
        for (UserNFTDto dto : dtoList) {
            Account account = this.accountService.findAccountById(dto.getUserId());
            account.setPassword(null);
            UserNFTVO userNFTVO = new UserNFTVO(
                    dto,account
            );
            voList.add(userNFTVO);
        }
        PageResult<UserNFTVO> pageResult = ControllerPageHelper.paginateList(
                voList, pageParam
        );
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(pageResult).asJsonString());
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
                                         @RequestParam("userID") String id,@ModelAttribute PageParam pageParam
        ) throws IOException {

        Integer userId = jwtUtils.getRequesetId(request);
        if(!Objects.equals(userId, jwtUtils.convertToInteger(id))) return RestBean.failure(401,"无权限的操作方式。");
        // 直接调用Mapper的分页方法
        Page<UserNFTDto> page = this.userNFTMapper.selectByOwnerId(
                pageParam.toPage(),jwtUtils.convertToInteger(id)
        );
        if (page.getTotal() == 0) {
            return RestBean.failure(401, "暂时没有NFT。");
        }
        List<UserNFTDto> dtoList = page.getRecords();
        if(dtoList == null){
            return RestBean.failure(401,"暂时没有NFT");
        }

        List<NFTCollectionVO> nftInfoDtoList = new ArrayList<>();
        for(UserNFTDto dto : dtoList){
            NFTCollectionVO nftCollectionVO = this.UserNFTCollectionConvert(
                    userId,
                    dto,
                    this.nftInfoService.NFTInfoSelectByTemplateId(dto.getNftId()),
                    this.nftRuleService.nftRuleSelectByActId(dto.getNftId())
            );
            if(nftCollectionVO != null){
                nftInfoDtoList.add(nftCollectionVO);
            }else {
                return RestBean.failure(402,"请检查参数。");
            }
        }
        PageResult<NFTCollectionVO> pageResult = new PageResult<>(
                page.getTotal(),
                nftInfoDtoList,
                (int) page.getCurrent(),
                (int) page.getPages(),
                (int) page.getSize()
        );
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(pageResult).asJsonString());
        return null;
    }


    @GetMapping("/user/nft/all")
    public void userNFTSelectAll(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @ModelAttribute PageParam pageParam
    ) throws IOException{
        response.setContentType("application/json;Charset=utf-8");
        Page<UserNFTDto> page = this.userNFTMapper.selectALL(
                pageParam.toPage()
        );
        if (page.getTotal() == 0) {
            response.getWriter().write(RestBean.failure(401, "暂无订单信息。").asJsonString());
            return ;
        }
        List<UserNFTDto> dtoList = page.getRecords();
        List<UserNFTVO> voList = new ArrayList<>();
        for(UserNFTDto dto : dtoList){
            Account account = this.accountService.findAccountById(dto.getUserId());
            account.setPassword(null);
             UserNFTVO vo = new UserNFTVO(
                     dto,account
             );
            voList.add(vo);
        }
        // 构建分页结果
        PageResult<UserNFTVO> pageResult = new PageResult<>(
                page.getTotal(),
                voList,
                (int) page.getCurrent(),
                (int) page.getPages(),
                (int) page.getSize()
        );
        response.getWriter().write(RestBean.success(pageResult).asJsonString());
        return;
    }



    private NFTCollectionVO UserNFTCollectionConvert(Integer userId,UserNFTDto dto,NFTInfoDto dto1,NFTRuleDto dto2){

        NFTCollectionVO vo = new NFTCollectionVO();
        if (Objects.equals(dto2.getValidityPeriod(), "-1")){
            dto2.setPassActive(null);
        }else if (LocalDateTime.now().isAfter(dto2.getPassActive())){
            if(!this.userNFTService.updateNFTStatus(userId,dto.getNftId(),0)){
                return null;
            }
            vo.setStatus(0);
        }
        vo.setNftId(dto.getNftId());
        vo.setOwnerId(dto.getUserId());
        vo.setPublicBy(dto1.getPublicBy());
        vo.setGetNFTTime(dto.getMintTime());
        vo.setNftName(dto1.getName());
        vo.setNftCid(dto1.getImageUrl());
        vo.setTransactionHash(dto.getTxHash());
        vo.setStatus(dto.getStatus());
        vo.setNftContractAddress(dto1.getContractAddress());
        vo.setNftIssuance(dto1.getIssuanceLimit());
        vo.setMetadataUrl(dto1.getMetadataUrl());
        if (dto2.getPassActive()!=null){
            vo.setNftPassTime(dto2.getPassActive());
        }
        vo.setValidityPeriod(dto2.getValidityPeriod());
        vo.setNftCreateTime(dto1.getCreatedAt());
        vo.setDescription(dto1.getDescription());
        return vo;
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
                jwtUtils.convertToInteger(application.getBuyerID()))){
            return RestBean.failure(402,"申请人异常。");
        }
        if(jwtUtils.getRequesetId(request).equals(
                jwtUtils.convertToInteger(application.getSellerID()))){
            return RestBean.failure(402,"不可以向自己申请NFT交易。");
        }
        if(
                this.nftInfoService.NFTInfoSelectByTemplateId(
                        jwtUtils.convertToInteger(application.getNftID())).getIsActive()==0
        ){return RestBean.failure(401,"NFT暂未激活。");}
        return this.nftTransactionService.buyApplyForNFT(application) ?
                RestBean.success():RestBean.failure(401,"请勿重复提交申请。");
    }

    @Auditable(
            operationType = "NFT_TRANSACTION_PAY",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/transaction/reply") //交易申请回复
    public <T> RestBean<T> addTransaction(         HttpServletRequest request,
                                                   @RequestBody AddTransactionRequest req
    ) throws Exception {
        Integer ans = req.getAns();
        NFTTransactionDto dto = req.getDto();
        Integer uid = jwtUtils.getRequesetId(request);
        if(!Objects.equals(uid, dto.getFromUser())){return RestBean.failure(401,"权限不足");}
        return this.nftTransactionService.replyNFTTransaction(dto,ans)?RestBean.success()
                  :RestBean.failure(401,"交易失败");
    }


    @Auditable(
            operationType = "NFT_TRANSACTION_GET",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/transaction/get/apply")
    public <T> RestBean<T> getTransaction(HttpServletRequest request,@RequestParam String id,
                                          HttpServletResponse response) throws IOException {
        Integer uid = jwtUtils.convertToInteger(id);
        if(this.nftTransactionService.getApplyForNFT(uid)==null){
            return RestBean.failure(401,"暂时没有交易信息。");
        }
        List<NFTPendingApplication> applications = this.nftTransactionService.getApplyForNFT(uid);
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
    @GetMapping("/info/select/condition")
    public <T> RestBean<T> selectNFT (HttpServletResponse response,
                                       NFTInfoDto dto, @ModelAttribute PageParam pageParam) throws IOException {

        List<NFTInfoDto> dtoList = this.nftInfoService.infoSelectByCondition(dto);
        if (!dtoList.isEmpty()){
            response.setContentType("application/json;Charset=utf-8");
            PageResult<NFTInfoDto> pageResult = ControllerPageHelper.paginateList(
                    dtoList, pageParam
            );
            response.getWriter().write(RestBean.success(pageResult).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT");
    }
    @Resource
    NFTInfoMapper nftInfoMapper;

    @Auditable(
            operationType = "NFT_INFO_SELECT_ALL",
            captureBefore = true,
            captureAfter = true
    )//分页
    @GetMapping("/info/select/all")
    public <T>RestBean<T> selectAllInfo(HttpServletResponse response,HttpServletRequest request,
                                        @ModelAttribute PageParam pageParam) throws IOException {
        // 直接调用Mapper的分页方法
        Page<NFTInfoDto> page = this.nftInfoMapper.selectPage(
                pageParam.toPage()
        );
        if (page.getTotal() == 0) {
            return RestBean.failure(401, "暂无NFT。");
        }
        List<NFTInfoDto> dtoList = page.getRecords();
        // 构建分页结果
        PageResult<NFTInfoDto> pageResult = new PageResult<>(
                page.getTotal(),
                dtoList,
                (int) page.getCurrent(),
                (int) page.getPages(),
                (int) page.getSize()
        );
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(pageResult).asJsonString());
        return null;
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
    public <T> RestBean<T> selectPublicID(@RequestParam String id,@ModelAttribute PageParam pageParam,
                                          HttpServletResponse response) throws IOException {
        List<NFTInfoDto> dtoList = this.nftInfoService.NFTInfoSelectByPublic(jwtUtils.convertToInteger(id));
        if (!dtoList.isEmpty()){
            PageResult<NFTInfoDto> pageResult = ControllerPageHelper.paginateList(
                    dtoList, pageParam
            );
            response.setContentType("application/json;Charset=utf-8");
            response.getWriter().write(RestBean.success(pageResult).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT");
    }



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

    @GetMapping("/transaction/send")
    public <T> RestBean<T> nftTransactionSend(
            HttpServletRequest request,@RequestParam List<Integer> toIDList,@RequestParam Integer nID
    ) throws Exception {
        //  NFT_TRANSACTION:1:2:3:700.00 买家id+卖家id+nftid+价格
        //  构建交易对象
        if(toIDList.isEmpty()) return RestBean.failure(401,"参数不合法。");
        for(Integer toID : toIDList) {
            Integer fromID = jwtUtils.getRequesetId(request);
            NFTTransactionDto dto = new NFTTransactionDto(
                    null, nID, fromID, toID, null, null, 2, BigDecimal.ZERO, 0
            );
            //  构建RedisKey
            String saveKey = Const.NFT_TRANSACTION + ":"
                    + toID + ":" + fromID
                    + ":" + nID + ":" + 0;
            stringRedisTemplate.opsForValue().set(saveKey, "", 1, TimeUnit.DAYS);
            stringRedisTemplate.opsForSet().add(Const.NFT_TRANSACTION + ":" +
                    toID, saveKey);
            if(this.nftTransactionService.replyNFTTransaction(dto,1)){
                continue;
            }else{
                return RestBean.failure(500,"发放失败，用户ID:%d, 请联系管理员。".formatted(toID));
            }
        }
        return RestBean.success();
    }
}
