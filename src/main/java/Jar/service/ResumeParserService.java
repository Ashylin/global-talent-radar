package Jar.service;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.util.Locale;
@Service
public class ResumeParserService {
 private final Tika tika=new Tika();
 public String extractText(MultipartFile file) throws IOException {
  if(file.isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a non-empty resume");
  if(file.getSize()>10*1024*1024)throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Resume must be 10 MB or smaller");
  String name=file.getOriginalFilename()==null?"":file.getOriginalFilename().toLowerCase(Locale.ROOT);
  if(!name.endsWith(".pdf") && !name.endsWith(".docx"))throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,"Only PDF and DOCX resumes are supported");
  try(var input=file.getInputStream()){
   String type=tika.detect(input,file.getOriginalFilename());
   if(!(name.endsWith(".pdf") && type.equals("application/pdf")) && !(name.endsWith(".docx") && type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")))throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,"File contents do not match a PDF or DOCX document");
  }
  try(var input=file.getInputStream()){
   String text=tika.parseToString(input);
   if(text.isBlank())throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"No selectable text found. Scanned images need OCR before upload.");
   return text;
  }catch(ResponseStatusException e){throw e;}catch(Exception e){throw new IOException("Could not parse resume file",e);}
 }
}
