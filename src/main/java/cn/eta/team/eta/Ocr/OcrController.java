// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.Ocr;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @PostMapping
    public Result<Map<String, String>> recognize(@RequestBody Map<String, String> body) {
        String base64Image = body.get("image");
        if (base64Image == null || base64Image.isBlank()) {
            throw new BizException(ErrorCode.OCR_IMAGE_EMPTY);
        }

        if (base64Image.startsWith("data:image")) {
            base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            throw new BizException(ErrorCode.OCR_IMAGE_TYPE_UNSUPPORTED);
        }

        try {
            String text = ocrService.extractText(imageBytes);
            return Result.ok(Map.of("text", text));
        } catch (IOException e) {
            throw new BizException(ErrorCode.OCR_IMAGE_TYPE_UNSUPPORTED);
        } catch (TesseractException e) {
            throw new BizException(ErrorCode.OCR_FAILED);
        }
    }
}