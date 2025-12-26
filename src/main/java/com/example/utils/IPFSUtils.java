package com.example.utils;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class IPFSUtils {


    private final IPFS ipfs;
    private final String gatewayUrl;

    public IPFSUtils(IPFS ipfs, @Value("${ipfs.gateway}") String gatewayUrl) {
        this.ipfs = ipfs;
        this.gatewayUrl = gatewayUrl;
    }

    public String uploadImg(MultipartFile file) throws IOException {
        // 验证文件是否为空
        if (file.isEmpty()) {
            return "上传的文件为空";
        }
        // 验证文件类型（可选）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return "仅支持图片文件上传";
        }
        // 1. 生成CID值
        IPFSUtils.IPFSResponse response = this.storeFile(file);  // 需要在 增添的操作对应的表上进行。
        String cid = response.cid();
        return "CID:"+cid;
    }

    public String returnUpLoadSuccess(MultipartFile file) throws IOException {
        String Rcid = this.uploadImg(file);
        if (Rcid.startsWith("CID:")){
            String cid = Rcid.split("CID:")[1];
            if(cid.startsWith("Qm")) return cid;
            return null;
        }
        return null;
    }

    public IPFSResponse storeFile(MultipartFile file) throws IOException {
        try {
            // 添加超时设置（Windows 环境可能需要）
            ipfs.timeout(30);

            // 上传到 IPFS
            NamedStreamable.InputStreamWrapper is = new NamedStreamable.InputStreamWrapper(file.getInputStream());
            MerkleNode result = ipfs.add(is).get(0);

            String cid = result.hash.toString();

            // 在控制台显示结果
            System.out.println("✅ 文件已上传至 IPFS");
            System.out.println("🆔 CID: " + cid);
            /**
             * CID 形式是 QmUoqPMfF2zekW5GiDCsBsVaa794n8u118dtLU7quUVeSn 大致这样
             * 可访问连接形式  https://ipfs.globalupload.io/Qm......（加上ipfs前缀）
             */

            return new IPFSResponse(cid);
        } catch (Exception e) {
            // 添加 Windows 环境下的特定错误处理
            if (e.getMessage().contains("Connection refused")) {
                throw new IOException("无法连接 IPFS 节点。请确保 IPFS Desktop 正在运行并已启用 API 访问", e);
            }
            throw new IOException("IPFS 上传失败: " + e.getMessage(), e);
        }
    }

    public record IPFSResponse(String cid) {}
}