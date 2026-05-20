package com.foodorderingsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
// cho phép truy cập file upload (ảnh, file...) từ thư mục trên máy thành URL trên web.
public class WebConfig implements WebMvcConfigurer {

    @Override
    //  đăng ký đường dẫn tài nguyên tĩnh. nói với Spring: hãy map thư mục uploads thành tài nguyên web”
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        exposeDirectory("uploads", registry);
    }

    //biến folder local thành URL public
    private void exposeDirectory(String dirName, ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(dirName);  //lấy đường dẫn thật
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        
        if (dirName.startsWith("../")) dirName = dirName.replace("../", ""); //xử lý bảo mật đường dẫn
        
        registry.addResourceHandler("/" + dirName + "/**") //Khi người dùng truy cập URL /uploads/... thì Spring sẽ lấy file tương ứng thư mục thật trên ổ đĩa.
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}
