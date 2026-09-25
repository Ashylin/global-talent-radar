package Jar.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Map;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<?> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"Request failed":e.getReason()));}
 @ExceptionHandler({IllegalArgumentException.class,org.springframework.http.converter.HttpMessageNotReadableException.class,org.springframework.web.bind.MissingServletRequestParameterException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> invalid(Exception e){return ResponseEntity.badRequest().body(Map.of("message","Invalid request. Check the required fields and numeric values."));}
 @ExceptionHandler(MaxUploadSizeExceededException.class) public ResponseEntity<?> size(){return ResponseEntity.status(413).body(Map.of("message","Resume must be 10 MB or smaller"));}
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<?> conflict(){return ResponseEntity.status(409).body(Map.of("message","This record conflicts with existing data. Check linked records and unique fields."));}
 @ExceptionHandler(java.io.IOException.class) public ResponseEntity<?> parse(){return ResponseEntity.unprocessableEntity().body(Map.of("message","Could not read this document. Upload a valid, unencrypted PDF or DOCX with selectable text."));}
}
