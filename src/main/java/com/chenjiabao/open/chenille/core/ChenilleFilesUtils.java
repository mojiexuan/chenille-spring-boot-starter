package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.dto.ChenilleFileStatus;
import com.chenjiabao.open.chenille.model.property.ChenilleFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * 文件类
 *
 * @author ChenJiaBao
 */
@Slf4j
public record ChenilleFilesUtils(ChenilleFile fileProperty,
                                 ChenilleTimeUtils chenilleTimeUtils,
                                 ChenilleStringUtils chenilleStringUtils) {

    //项目目录
    public static String classesPath = System.getProperty("user.dir");

    /**
     * 创建文件目录及父目录
     */
    public boolean createDirectory(File dir) {
        // 如果传入的是文件路径，获取其父目录
        if (dir.isFile()) {
            dir = dir.getParentFile(); // 获取父目录
        }

        // 如果当前目录不存在，递归创建父目录
        if (!dir.exists()) {
            // 先递归创建父目录
            File parentFile = dir.getParentFile();
            if (parentFile != null && !parentFile.exists() && !createDirectory(parentFile)) {
                return false; // 如果父目录创建失败，返回false
            }
            // 创建当前目录
            return dir.mkdirs(); // 如果当前目录不存在，尝试创建它
        }
        return true; // 如果目录已经存在，直接返回true
    }

    /**
     * 判断文件是否存在
     *
     * @param file 文件
     * @return 是否存在
     */
    public boolean existFile(File file) {
        return file.exists();
    }

    /**
     * 判断文件是否存在
     *
     * @param path 文件路径
     * @return 是否存在
     */
    public boolean existFile(String path) {
        return existFile(Paths.get(path).toFile());
    }

    /**
     * 判断目录是否存在
     *
     * @param file 目录
     * @return 是否存在
     */
    public boolean existDir(File file) {
        return file.exists() && file.isDirectory();
    }

    /**
     * 判断目录是否存在
     *
     * @param path 目录路径
     * @return 是否存在
     */
    public boolean existDir(String path) {
        return existDir(Paths.get(path).toFile());
    }

    /**
     * 创建文件
     */
    public boolean createFile(File file) {
        boolean flag;
        try {
            //创建父目录，若存在
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                createDirectory(parentFile);
            }
            //创建文件
            flag = file.createNewFile();
        } catch (IOException e) {
            log.error("创建文件失败！");
            flag = false;
        }

        return flag;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(File file) {
        return file.delete();
    }

    /**
     * 字符流 缓冲流 读取文件
     */
    public ArrayList<String> readerFile(String file) {

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("999");

        Reader fr = null;
        BufferedReader br = null;

        try {
            fr = new FileReader(file);
            //构造方法需要一个FileReader对象
            br = new BufferedReader(fr);

            String data = null;
            while ((data = br.readLine()) != null) {
                //readLine()方法可以一行一行地读取
                arrayList.add(data);
            }
        } catch (IOException e) {
            //获取失败！
            arrayList.set(0, "-1");
            log.error("读取文件失败！");
        } finally {
            //关闭流
            closeFile(br);
            closeFile(fr);
        }

        return arrayList;
    }

    /**
     * 写入文件
     *
     * @param append 是否开启追加模式
     */
    public boolean writerFile(String file, String content, boolean append) {
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            fw = new FileWriter(file, append);
            bw = new BufferedWriter(fw);

            bw.write(content);
            bw.newLine(); // 换行
            return true;
        } catch (IOException e) {
            log.error("写入文件失败！");
            return false;
        } finally {
            closeFile(bw);
            closeFile(fw);
        }
    }

    /**
     * 关闭流
     */
    public boolean closeFile(Reader fileReader) {
        if (fileReader == null) {
            return true;
        }
        try {
            fileReader.close();
            return true;
        } catch (IOException e) {
            log.error("关闭流失败！");
            return false;
        }
    }

    public boolean closeFile(BufferedReader bufferedReader) {
        if (bufferedReader == null) {
            return true;
        }
        try {
            bufferedReader.close();
            return true;
        } catch (IOException e) {
            log.error("关闭流失败！");
            return false;
        }
    }

    public boolean closeFile(FileWriter fileWriter) {
        if (fileWriter == null) {
            return true;
        }
        try {
            fileWriter.close();
            return true;
        } catch (IOException e) {
            log.error("关闭流失败！");
            return false;
        }
    }

    public boolean closeFile(BufferedWriter bufferedWriter) {
        if (bufferedWriter == null) {
            return true;
        }
        try {
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            log.error("关闭流失败！");
            return false;
        }
    }

    /**
     * 检查文件是否合法
     *
     * @param file 文件
     * @return ApiResponse
     */
    public ChenilleFileStatus checkFile(MultipartFile file) {
        if (file.isEmpty()) {
            return new ChenilleFileStatus(406, "文件为空");
        }

        //获取文件名
        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.contains(".")) {
            return new ChenilleFileStatus(406, "文件名异常");
        }

        //获取文件后缀
        String fileSuffix = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        //判断是否支持的类型
        if (!fileProperty.getFormat().contains(fileSuffix)) {
            return new ChenilleFileStatus(406, "文件格式不支持");
        }

        long size = file.getSize();
        // 10MB
        if (size > fileProperty.getMaxSize()) {
            return new ChenilleFileStatus(406, "文件过大");
        }

        return new ChenilleFileStatus(200, "文件合法");
    }

    /**
     * 保存文件
     *
     * @param file     文件
     * @param savePath 保存路径,需要前导/，不要尾随/，如：/20200301
     * @return null为失败，成功则为路径
     */
    public String saveFile(MultipartFile file, String savePath) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        Paths.get("");

        String fileSuffix = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        //生成文件目录
        String dir = chenilleTimeUtils.getNowTime("yyyyMMdd");
        String newFileName = chenilleTimeUtils.getNowTime("yyyyMMddHHmmss");
        //生成随机5长度数字字符串
        String randomNumber = chenilleStringUtils.generateRandomNumberString(5);
        // 拼接新的文件名
        newFileName = newFileName + randomNumber + fileSuffix;

        String path = savePath + "/" + dir + "/";

        //自动创建父目录，若不存在
        if (!createDirectory(new File(classesPath + path))) {
            return null;
        }

        // 拼接完整文件名
        String allPath = classesPath + path + newFileName;
        // 将文件放进指定目录
        try {
            file.transferTo(new File(allPath));
        } catch (IOException e) {
            return null;
        }

        return path + newFileName;
    }

    /**
     * 保存文件
     *
     * @return null为失败，成功则为路径
     */
    public String saveFile(MultipartFile file) {
        return this.saveFile(file, fileProperty.getPath());
    }

}
