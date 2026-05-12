package cn.lilicould.liliblog.strategy.impl;

import cn.lilicould.liliblog.common.constant.LoginStrategyConstant;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.pojo.dto.request.LoginRequest;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import cn.lilicould.liliblog.strategy.LoginStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailLoginStrategy implements LoginStrategy {

    @Override
    public LoginVO login(LoginRequest request, HttpServletResponse response) {
        // todo 邮箱验证码登录实现

        log.error("邮箱验证码登录逻辑暂未实现");
        throw new BusinessException(-1,"敬请期待！");

    }

    @Override
    public String getType() {
        return LoginStrategyConstant.EMAIL;
    }
}
