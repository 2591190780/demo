package com.example.entity.vo.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImageData {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String filename;
    private String contentType;
    private Long size;
    private String cid;
    private String ipfsUrl;
    private LocalDateTime uploadTime;
    private String originalPath; // Windows 文件路径

}
