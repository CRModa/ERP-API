package reset.reset.dto.document.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PDFResponse {
    private String fileName;
    private String fileBase64;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
}
