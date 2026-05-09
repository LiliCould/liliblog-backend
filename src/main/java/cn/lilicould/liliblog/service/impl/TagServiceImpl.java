package cn.lilicould.liliblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lilicould.liliblog.pojo.entity.Tag;
import cn.lilicould.liliblog.service.TagService;
import cn.lilicould.liliblog.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author Lili_Could
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:41
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




