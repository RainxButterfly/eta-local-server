// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.ocr;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * OCR
 *
 * @author ormisnal
 * @since 2026-08-24
 */
@Service
public class OcrService {

    private ITesseract tesseract;

    @PostConstruct
    public void init() {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("src/main/resources/tessdata");
        this.tesseract.setLanguage("chi_sim+chi_tra+equ+eng");
    }

    public String extractText(File imageFile) throws TesseractException {
        return tesseract.doOCR(imageFile);
    }

    public String extractText(byte[] imageBytes) throws IOException, TesseractException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new IOException("无法解码图片，格式可能不受支持或数据损坏");
            }
            return tesseract.doOCR(image);
        }
    }
}