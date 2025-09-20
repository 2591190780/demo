package com.example.controller;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.entity.vo.request.ResetPasswordByPasswordVO;
import com.example.entity.vo.response.UserAccountAnalysisDataVO;
import com.example.mapper.NFT.NFTTransactionMapper;
import com.example.mapper.NFT.UserNFTMapper;
import com.example.mapper.product.ProductInfoSelectAccountMapper;
import com.example.mapper.transaction.TransactionProcessMapper;
import com.example.service.AccountService;
import com.example.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name="请求",description = "相关操作")
public class AuthorizeController {

    @Resource
    AccountService accountService;
    @Autowired
    private JwtUtils jwtUtils;

    @Auditable(
            operationType = "UPDATE_USER_NAME",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/update/username") //得在登录状态下
    public  RestBean<String>  updateUserName(HttpServletRequest request
            , @Parameter(ref = "newName") String name){
        if (accountService.findAccountByNameOrEmail(name)!=null) return RestBean.failure(401,"用户名已存在。");

        return this.accountService.updateName(request,name)? RestBean.success("请等待管理员处理。")
                : RestBean.failure(401,"请求失败。");
    }

    @Auditable(
            operationType = "UPDATE_ROLE_API_AUTH",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/update/role") //得在登录状态下
    public  RestBean<String>  updateRoleApply(HttpServletRequest request, @RequestBody Account account){
        return this.accountService.updateRoleByApply(request,account)? RestBean.success("请等待管理员处理。")
                : RestBean.failure(401,"请求失败。");
    }

    @Auditable(
            operationType = "ASK-CODE_USER",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/ask-code") //请求发送验证码
    public RestBean<Void> askVerifyCode(@RequestParam  @Email  String email ,
                                        @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                        HttpServletRequest request
                                        ){
        //从消息队列中消费
        accountService.registerEmailVerifyCode(type, email, request.getRemoteAddr());
        return RestBean.success();

    }

    @Auditable(
            operationType = "REGISTER_USER",
            captureBefore = true,
            captureAfter = true
    )
    @PostMapping("/register")  //注册
    public ResponseEntity<RestBean<String[]>> registerUser(@RequestBody @Valid EmailRegisterVO vo) {
          String[] message =  accountService.registerEmailAccount(vo).split(":");
          if(message.length != 2){
              return ResponseEntity.ok()
                      .body(
                              RestBean.failure(401,message[0])
                      );
          }
          return ResponseEntity.ok()
                  .body(
                          RestBean.success(message)
                  );
    }

    @Auditable(
            operationType = "RESET-CONFIRM_USER",
            captureBefore = true,
            captureAfter = true
    )
    @PostMapping("/reset-confirm") //验证码确认
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo){
        return this.messageHandle(vo, accountService::resetConfirm);
    }

    @Auditable(
            operationType = "RESET-PASSWORD_BY_CODE_USER",
            captureBefore = true,
            captureAfter = true
    )
    @PostMapping("/reset-password") //验证码修改密码
    public RestBean<Void> resetPasswordConfirm(@RequestBody @Valid EmailResetVO vo){
        return this.messageHandle(vo,accountService::resetEmailAccountPassword);
    }

    @Auditable(
            operationType = "RESET_PASSWORD_USER",
            captureBefore = true,
            captureAfter = true
    )
    @PostMapping("/resetPassword") //密码验证方式修改密码
    public RestBean<Void> resetPasswordConfirm(@RequestBody  ResetPasswordByPasswordVO dto,
                                             HttpServletRequest request ){
        String result = accountService.resetPasswordByPassword(dto, request);
        return result == null ? RestBean.success():RestBean.failure(401,result);
    }


    @Resource
    TransactionProcessMapper transactionProcessMapper;
    @Resource
    NFTTransactionMapper nftTransactionMapper;

    @GetMapping("/find/account")
    public void findAccount(HttpServletRequest request , HttpServletResponse response) throws IOException {
        Integer id = jwtUtils.getRequesetId(request);

        Account account = accountService. findAccountById(id);
        account.setPassword(null);
        //基本信息
        UserAccountAnalysisDataVO analysisDataVO = new UserAccountAnalysisDataVO();
        analysisDataVO.setId(id);
        analysisDataVO.setEmail(account.getEmail());
        analysisDataVO.setUsername(account.getUsername());
        analysisDataVO.setRole(account.getRole());
        analysisDataVO.setImgUrl(account.getUserImgurl());
        analysisDataVO.setWalletAddress(account.getWalletAddress());
        analysisDataVO.setPhoneNumber(account.getPhoneNumber());
        //进阶信息
        Integer countTransactionById = this.transactionProcessMapper.countNumTransactionByBuyerIdStatus(id);
        analysisDataVO.setNumOfTransactions(countTransactionById);

        Integer totalMoneyOfTransactions = this.transactionProcessMapper.sumTotalMoneyByBuyerIdStatus(id);
        analysisDataVO.setTotalMoneyOfTransactions(totalMoneyOfTransactions);

        Integer totalNFTTransactions = this.nftTransactionMapper.countNumByFromID(id);
        analysisDataVO.setTotalNFTTransactions(totalNFTTransactions);

        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(analysisDataVO).asJsonString());
    }




    private <T>RestBean<Void> messageHandle(T vo, Function<T, String> function) {
        return messageHandle(()->function.apply(vo));
    }

    private  RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        return message == null ? RestBean.success():RestBean.failure(400,message);
    }
}
