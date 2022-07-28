package com.condi.service;

import com.condi.model.MultipartFileParam;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Author ACER
 * @Date:2022/7/28
 */
@Service
public interface storageService {
    /**
     * 上传文件方法1
     *
     * @param param
     * @throws IOException
     */
    void uploadFileRandomAccessFile(MultipartFileParam param) throws IOException;

    /**
     * 上传文件方法2
     * 处理文件分块，基于MappedByteBuffer来实现文件的保存
     *
     * @param param
     * @throws IOException
     */
    void uploadFileByMappedByteBuffer(MultipartFileParam param) throws IOException;
}
