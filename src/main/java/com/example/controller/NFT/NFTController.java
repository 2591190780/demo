package com.example.controller.NFT;



import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.RestBean;
import com.example.service.IPFSService;
import com.example.utils.MessageIntoIPFSUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/imgUpload")
@Tag(name="图片上传",description = "相关接口")
public class NFTController {

    @Resource
    MessageIntoIPFSUtil messageIntoIPFSUtil;

    @Resource
    IPFSService ipfsService;

    @PostMapping("/add")
    public <T> RestBean<String> imgSend (HttpServletRequest request,
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
        // 1. 判断传入图片的操作类型
        if(messageIntoIPFSUtil.sendMessageToRedis(request,operationType)!=null){
            IPFSService.IPFSResponse response = ipfsService.storeFile(file);// 需要在 增添的操作对应的表上进行。
            String cid = response.cid();
            return RestBean.success("请求已上传,图片IPFS地址为:"+cid);
        }
        // 上传到IPFS

        return RestBean.failure(401,"不支持的操作类型");

    }


}
