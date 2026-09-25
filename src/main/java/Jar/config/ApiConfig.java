package Jar.config;
import Jar.service.AuthService;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.*;
@Configuration
public class ApiConfig implements WebMvcConfigurer {
 private final AuthService auth;
 @Value("${app.cors.origins:http://localhost:5173,http://127.0.0.1:5173}") private String origins;
 public ApiConfig(AuthService auth){this.auth=auth;}
 @Override public void addCorsMappings(CorsRegistry registry){registry.addMapping("/api/**").allowedOrigins(origins.split(",")).allowedMethods("GET","POST","OPTIONS").allowedHeaders("Content-Type","Authorization").maxAge(3600);}
 @Override public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(new HandlerInterceptor(){
  @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler){
   if("OPTIONS".equals(request.getMethod()))return true;
   String path=request.getRequestURI();
   if(path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/api/health") || (path.equals("/api/users") && "POST".equals(request.getMethod())))return true;
   auth.current();return true;
  }
 }).addPathPatterns("/api/**");}
}
