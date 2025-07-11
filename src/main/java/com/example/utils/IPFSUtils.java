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