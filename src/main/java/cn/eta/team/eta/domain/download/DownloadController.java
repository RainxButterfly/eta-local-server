package cn.eta.team.eta.domain.download;
 
import cn.eta.team.eta.common.util.FileStorageUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件下载控制器
 * <p>
 * 提供文件下载接口，配合 FileStorageUtil 使用
 *
 * @author ormisnal
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/download")
public class DownloadController {

    /**
     * 下载文件（按文件名）
     *
     * @param fileName 文件名
     * @param response HTTP 响应对象
     */
    @GetMapping("/{fileName}")
    public void download(
            @PathVariable String fileName,
            HttpServletResponse response
    ) throws IOException {
        String relativePath = fileName;

        if (!FileStorageUtils.exists(relativePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":404,\"message\":\"文件不存在\"}");
            response.getWriter().flush();
            return;
        }

        byte[] content = FileStorageUtils.readFromLocal(relativePath);

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setContentLength(content.length);

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", 
                "attachment; filename*=UTF-8''" + encodedFileName);

        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }
}