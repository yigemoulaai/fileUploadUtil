package com.condi.service.impl;

import com.condi.model.MultipartFileParam;
import com.condi.constant.FileStatus;
import com.condi.service.storageService;
import org.apache.commons.io.FileUtils;
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
            //
            boolean hasCompleted= checkAndSetUploadProgress(param, tmpDirPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /* *
     * @description: 检查并修改文件上传进度
     * @params: [param, tmpDirPath]
     * @return: boolean
     */
    private boolean  checkAndSetUploadProgress(MultipartFileParam param, String tmpDirPath)
    throws  Exception{
        String fileName = param.getName();
        //写入配置文件  每一个分块都会写入1B信息
        File conFile = new File(tmpDirPath, fileName + ".conf");
        RandomAccessFile accessFile = new RandomAccessFile(conFile, "rw");
        accessFile.setLength(param.getChunks());
        accessFile.seek(param.getChunk());
        accessFile.write(Byte.MAX_VALUE);
        //检查是否全部上传完成
        byte[] fileArray = FileUtils.readFileToByteArray(conFile);
        byte flag = Byte.MAX_VALUE;
        for(int i = 0;i < fileArray.length && flag == Byte.MAX_VALUE ;i++){
            flag = (byte) (flag & fileArray[i]);
        }
        accessFile.close();
        //所有分片已到达
        if(flag == Byte.MAX_VALUE){
                redisTemplate.opsForValue().set(param.getMd5(), tmpDirPath+"/"+fileName);
                redisTemplate.opsForHash().put(FileStatus.FILE_UPLOAD_STATUS, param.getMd5(), "true");
        }
        else {
            if (!redisTemplate.opsForHash().hasKey(FileStatus.FILE_UPLOAD_STATUS, param.getMd5())) {
                redisTemplate.opsForHash().put(FileStatus.FILE_UPLOAD_STATUS, param.getMd5(), "false");
            }
            if (redisTemplate.hasKey(FileStatus.FILE_MD5_KEY + param.getMd5())) {
                redisTemplate.opsForValue().set(FileStatus.FILE_MD5_KEY + param.getMd5(), tmpDirPath + "/" + fileName + ".conf");
            }
            return  false;
        }
        return  true;
    }

    @Override
    public void uploadFileByMappedByteBuffer(MultipartFileParam param) throws IOException {

    }
}
