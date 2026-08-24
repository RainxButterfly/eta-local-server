// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.module;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * OCR 识别（TODO：接入云 OCR 或本地模型）。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/ocr")
public class OcrController {

    @PostMapping
    public Result<Map<String, String>> recognize(@RequestBody Map<String, Object> body) {
        Object image = body.get("image");
        if (image == null || String.valueOf(image).isBlank()) {
            throw new BizException(ErrorCode.OCR_IMAGE_EMPTY);
        }
        // 占位实现：返回空识别结果，待接入真实识别服务
        return Result.ok(Map.of("text", ""));
    }
}