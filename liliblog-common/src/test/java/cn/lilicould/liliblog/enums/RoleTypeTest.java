package cn.lilicould.liliblog.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RoleType角色枚举测试类
 *
 * @author lilicould
 */
class RoleTypeTest {

    @Test
    void getRoleByCodeAdmin() {
        assertEquals("ROLE_ADMIN", RoleType.getRoleByCode(0));
    }

    @Test
    void getRoleByCodeUser() {
        assertEquals("ROLE_USER", RoleType.getRoleByCode(1));
    }

    @Test
    void getRoleByCodeUnknownFallsBackToUser() {
        assertEquals("ROLE_USER", RoleType.getRoleByCode(null));
        assertEquals("ROLE_USER", RoleType.getRoleByCode(99));
    }

    @Test
    void enumValues() {
        assertEquals(0, RoleType.ADMIN.getCode());
        assertEquals("ROLE_ADMIN", RoleType.ADMIN.getName());
        assertEquals(1, RoleType.USER.getCode());
        assertEquals("ROLE_USER", RoleType.USER.getName());
    }
}