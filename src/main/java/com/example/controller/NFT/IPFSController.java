package com.example.controller.NFT;



import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.response.ProductVO;
import com.example.service.AccountService;
import com.example.service.IPFSService;
import com.example.service.product.ProductInfoSelectAccountService;
import com.example.service.product.ProductInfoUpdateAccountService;
import com.example.utils.JwtUtils;
import com.example.utils.MessageIntoIPFSUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/api/imgUpload")
@Tag(name="图片上传",description = "相关接口")
public class IPFSController {

    @Resource
    MessageIntoIPFSUtil messageIntoIPFSUtil;

    @Resource
    IPFSService ipfsService;

    @Resource
    JwtUtils jwtUtils;

    @Resource
    ProductInfoUpdateAccountService productInfoUpdateAccountService;

    @Resource
    ProductInfoSelectAccountService productInfoSelectAccountService;

    @Resource
    AccountService accountService;

    @PostMapping("/add")
    public <T> RestBean<String> imgAdd(HttpServletRequest request,
                                       @RequestParam String id,  //传入要修改的目标记录ID
                                       @RequestParam("file") MultipartFile file,
                                       @RequestParam("operationType") String operationType) throws IOException {
        // 验证文件是否为空
        if (file.isEmpty()) {
            return RestBean.failure(401,"上传的文件为空");
        }
        // 验证文件类型（可选）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return RestBean.failure(401,"仅支持图片文件上传");
        }
        Integer userId = jwtUtils.getRequesetId(request);
        // 1. 生成CID值
        IPFSService.IPFSResponse response = ipfsService.storeFile(file);  // 需要在 增添的操作对应的表上进行。
        String cid = response.cid();
        // 2. 在目标对象进行操作
            /**
             * 图片上传后获得CID `QmXarR6rgkQ2fDSHjSY5nM2kuCXKYGViky5nohtwgF65Ec`
             *     - 直接访问：`ipfs://QmXarR6rgkQ2fDSHjSY5nM2kuCXKYGViky5nohtwgF65Ec`
             *     - 通过网关：`https://ipfs.io/ipfs/QmXarR6rgkQ2fDSHjSY5nM2kuCXKYGViky5nohtwgF65Ec`
             */

            //添加对象操作
        if (this.operationTypeImgAdd(operationType,request,id,cid)){
            return RestBean.success("上传成功，图片的CID为"+cid);
        };
        // 上传到IPFS
        return RestBean.failure(401,"不支持的操作类型");

    }



    private boolean operationTypeImgAdd(String operationType ,HttpServletRequest request
            ,String id //修改产品传入的就是产品id 修改用户传的就是用户id
            ,String cid){
        Integer targetId = jwtUtils.convertToInteger(id);
        if (operationType.equals("product")) {
            ProductVO vo = this.productInfoSelectAccountService.getProductInfoAccountByProductId(
                    targetId);
            if (!Objects.equals(vo.getFarmerId(), targetId)) return false; //操作目标的所属农户不属于当前登录状态下的用户 则操作失败
            ProductInfoAccountDto dto = new ProductInfoAccountDto();
            BeanUtils.copyProperties(vo,dto);
            dto.setProductId(jwtUtils.convertToInteger(id));
            dto.setProductImgurl(cid);
            this.productInfoUpdateAccountService.updateSingleProductInfo(request,dto);
            return true;
        } else if (operationType.equals("userInfo")) {
            Account account = this.accountService.findAccountById(targetId);
            if (!Objects.equals(account.getId(), targetId)) return false;
            account.setUserImgurl(cid);
            this.accountService.updateImg(request,account);
        }

        return false;
    }


}
