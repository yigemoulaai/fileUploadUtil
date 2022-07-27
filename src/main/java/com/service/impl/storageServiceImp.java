package com.service.impl;

import com.condi.model.MultipartFileParam;
import com.service.storageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Author ACER
 * @Date:2022/7/28
 */
@Service
public class storageServiceImp implements storageService {
    private final Logger logger = LoggerFactory.getLogger(storageServiceImp.class);
    public Path rootPath;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${breakpoint.upload.chunkSize}")
    private long chunk_size;

    @Value("${breakpoint.upload.dir}")
    private String finalDirPath;

    @Autowired
    public storageServiceImp (@Value("${breakpoint.upload.dir}") String location){
        this.rootPath = Paths.get(location);
    }
    @Override
    public void uploadFileRandomAccessFile(MultipartFileParam param) throws IOException {
        String name = param.getName();
        String tmpDirPath = finalDirPath + param.getMd5();
        String tmpFileName= name +"_tmp";
        File tmpDir = new File(tmpDirPath);
        File tmpFile = new File(tmpDirPath, tmpFileName);
        if(!tmpDir.exists()){
            tmpDir.mkdir();
        }
        RandomAccessFile accessFile = new RandomAccessFile(tmpFile, "rw");
        long offset = chunk_size * param.getChunk();
        accessFile.seek(offset);
        accessFile.write(param.getFile().getBytes());
        accessFile.close();
        try {
            boolean hasCompleted= checkAndSetUploadProgress(param, tmpDirPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean  checkAndSetUploadProgress(MultipartFileParam param, String tmpDirPath)
    throws  Exception{
        String fileName = param.getName();
        File file = new File(tmpDirPath, fileName + ".conf");
        RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
        return  false;
    }

    @Override
    public void uploadFileByMappedByteBuffer(MultipartFileParam param) throws IOException {

    }
}
