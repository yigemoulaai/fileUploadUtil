package com.condi.controller;

import com.condi.model.MultipartFileParam;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.util.List;

/**
 * @Author ACER
 * @Date:2022/7/27
 */
@Controller
public class fileController {


    private Logger logger = LoggerFactory.getLogger(fileController.class);
    private  static final String charEncode = "utf-8";
    @RequestMapping(value = "/upFile", method = RequestMethod.POST)
    @ResponseBody
    public String upload(HttpServletRequest request, MultipartFileParam param) throws Exception {

        //记录当前分片数
        int chunk = -1;
        //总分片数
        int chkCount = -1;
        String fileName = null;
        String path = null;
        BufferedOutputStream ops  = null; //输出到缓冲区

        boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
        if(isMultiPart){
            logger.info("上传文件starting:");
        }
        ServletFileUpload upload = new ServletFileUpload(); //fileUploader包
        upload.setFileSizeMax(128l * 1024 * 1024); //单个文件大小限制128M
        upload.setSizeMax(2l * 1024 * 1024 *1024);  //总分片文件限制 2G
        List<FileItem> items = upload.parseRequest(request); //解析出请求并包装成 FileItems
        for(FileItem item : items){
            if(item.isFormField()){
                String item_field = item.getFieldName();
                if(item_field.equals("chunk")){
                    chunk = Integer.parseInt(item.getString(charEncode));
                }
                else if(item_field.equals("chunks")){
                    chkCount = Integer.parseInt(item.getString(charEncode));
                }
                else if(item_field.equals("name")) {
                    fileName = item.getString(charEncode);
                }
            }
        }
        for(FileItem item : items){
            String tmpFileName = null;
            if(!item.isFormField()){
                if(fileName != null){
                    if(chunk != -1){
                        tmpFileName = fileName+"_"+chunk;
                    }
                    File file = new File(path, tmpFileName);
                    if(!file.exists()){
                        item.write(file);
                    }
                }
            }
        }
        return "搭建成功";
    }

}
