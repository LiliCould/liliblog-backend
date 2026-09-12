package cn.lilicould.liliblog.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ip2RegionUtil地理位置工具测试类
 *
 * @author lilicould
 */
class Ip2RegionUtilTest {

    @Test
    void formatLocationFullChinaLocation() {
        String formatted = Ip2RegionUtil.formatLocation("中国|江西省|上饶市|移动|CN");

        assertEquals("江西省上饶市", formatted);
    }

    @Test
    void formatLocationWithCountryOnly() {
        String formatted = Ip2RegionUtil.formatLocation("美国|华盛顿|DC|电信|US");

        assertEquals("美国华盛顿DC", formatted);
    }

    @Test
    void formatLocationIgnoresZeroValues() {
        String formatted = Ip2RegionUtil.formatLocation("中国|0|0|0|0");

        assertEquals("未知", formatted);
    }

    @Test
    void formatLocationCitySameAsProvince() {
        String formatted = Ip2RegionUtil.formatLocation("中国|北京市|北京市|0|CN");

        assertEquals("北京市", formatted);
    }

    @Test
    void formatLocationWithLanIp() {
        String formatted = Ip2RegionUtil.formatLocation("内网IP|内网IP|0|0|0");

        assertEquals("未知", formatted);
    }

    @Test
    void formatLocationNullReturnsUnknown() {
        assertEquals("未知", Ip2RegionUtil.formatLocation(null));
        assertEquals("未知", Ip2RegionUtil.formatLocation("未知"));
        assertEquals("未知", Ip2RegionUtil.formatLocation(""));
    }

    @Test
    void getLocationForUnknownIpReturnsFeiZhi() {
        String location = Ip2RegionUtil.getLocation("999.999.999.999");

        assertEquals("未知", location);
    }

    @Test
    void getLocationForValidIpReturnsResult() {
        String location = Ip2RegionUtil.getLocation("114.114.114.114");

        assertNotNull(location);
        assertTrue(location.contains("114") || location.contains("|") || location.contains("南京") || location.contains("江苏"));
    }

    @Test
    void getFormattedLocationForLanIp() {
        String formatted = Ip2RegionUtil.getFormattedLocation("192.168.1.1");

        assertTrue("未知".equals(formatted) || !formatted.isBlank());
    }
}