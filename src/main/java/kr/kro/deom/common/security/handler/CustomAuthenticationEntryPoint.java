package kr.kro.deom.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.kro.deom.common.exception.exceptions.UnauthorizedException;
import kr.kro.deom.common.exception.messages.UnauthorizedMessages;
import kr.kro.deom.common.exception.response.GlobalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    response.setContentType("application/json; charset=UTF-8");
    response.setStatus(HttpStatus.UNAUTHORIZED.value());

    GlobalResponse<Object> errorResponse =
        GlobalResponse.fail(new UnauthorizedException(UnauthorizedMessages.AUTHENTICATION_FAILED));

    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getOutputStream(), errorResponse);
  }
}
