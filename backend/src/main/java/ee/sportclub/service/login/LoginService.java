package ee.sportclub.service.login;

import ee.sportclub.controller.login.dto.LoginRequestDto;
import ee.sportclub.controller.login.dto.LoginResponseDto;
import ee.sportclub.infrastructure.exception.ForbiddenException;
import ee.sportclub.persistence.user.User;
import ee.sportclub.persistence.user.UserMapper;
import ee.sportclub.persistence.user.UserRepository;
import ee.sportclub.service.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ee.sportclub.Error.INCORRECT_CREDENTIALS;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {
        User user = getValidUser(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        LoginResponseDto loginResponseDto = userMapper.toLoginResponseDto(user);
        return loginResponseDto;

    }

    private User getValidUser(String email, String password) {
        User user = userRepository.findUserBy(email, password, Status.STATUS_ACTIVE.getCode())
                .orElseThrow(() -> new ForbiddenException(INCORRECT_CREDENTIALS.getMessage(), INCORRECT_CREDENTIALS.name()));
        return user;
    }

}
