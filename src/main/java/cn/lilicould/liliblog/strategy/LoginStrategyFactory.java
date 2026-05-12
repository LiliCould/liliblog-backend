package cn.lilicould.liliblog.strategy;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录策略工厂
 */
@Component
public class LoginStrategyFactory {

    // 自动注入所有 登录策略
    private final List<LoginStrategy> strategyList;

    // 存放：登录类型 -> 对应策略
    private final Map<String, LoginStrategy> strategyMap = new HashMap<>();

    // 构造器注入
    public LoginStrategyFactory(List<LoginStrategy> strategyList) {
        this.strategyList = strategyList;
    }

    // 项目启动时，把所有策略装进 map
    @PostConstruct
    public void init() {
        for (LoginStrategy strategy : strategyList) {
            strategyMap.put(strategy.getType(), strategy);
        }
    }

    // 根据类型获取策略（给 AuthService 调用）
    public LoginStrategy getStrategy(String loginType) {
        LoginStrategy strategy = strategyMap.get(loginType);
        if (strategy == null) {
            throw new BusinessException(CodeEnum.NOT_SUPPORTED);
        }
        return strategy;
    }
}
