package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.entity.ChatMessage;
import cn.lilicould.liliblog.mapper.ChatMessageMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ChatMessageServiceImpl测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatMessageServiceImplTest {

    @Mock
    private ChatMessageMapper chatMessageMapper;

    private ChatMessageServiceImpl chatMessageService;

    @BeforeEach
    void setUp() {
        chatMessageService = new ChatMessageServiceImpl();
        ReflectionTestUtils.setField(chatMessageService, "baseMapper", chatMessageMapper);
    }

    private ChatMessage buildMessage() {
        ChatMessage message = new ChatMessage();
        message.setId(1L);
        message.setSenderId(10L);
        message.setContent("你好");
        message.setType("text");
        message.setStatus(1);
        return message;
    }

    @Test
    void saveInsertsEntity() {
        ChatMessage message = buildMessage();
        when(chatMessageMapper.insert(message)).thenReturn(1);

        boolean result = chatMessageService.save(message);

        assertTrue(result);
        verify(chatMessageMapper).insert(message);
    }

    @Test
    void getByIdReturnsEntity() {
        ChatMessage message = buildMessage();
        when(chatMessageMapper.selectById(1L)).thenReturn(message);

        ChatMessage result = chatMessageService.getById(1L);

        assertEquals("你好", result.getContent());
        assertEquals(10L, result.getSenderId());
    }

    @Test
    void getByIdNotExistReturnsNull() {
        when(chatMessageMapper.selectById(1L)).thenReturn(null);
        assertNull(chatMessageService.getById(1L));
    }

    @Test
    void updateByIdUpdatesEntity() {
        ChatMessage message = buildMessage();
        message.setContent("更新内容");
        when(chatMessageMapper.updateById(message)).thenReturn(1);

        boolean result = chatMessageService.updateById(message);

        assertTrue(result);
        verify(chatMessageMapper).updateById(message);
    }

    @Test
    void removeByIdDeletes() {
        when(chatMessageMapper.deleteById(1L)).thenReturn(1);
        assertTrue(chatMessageService.removeById(1L));
        verify(chatMessageMapper).deleteById(1L);
    }

    @Test
    void pageReturnsPagedData() {
        ChatMessage message = buildMessage();
        Page<ChatMessage> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(message));
        when(chatMessageMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        Page<ChatMessage> result = chatMessageService.page(new Page<>(1, 10), new LambdaQueryWrapper<>());

        assertEquals(1, result.getRecords().size());
        assertEquals(1L, result.getTotal());
    }

    @Test
    void existsChecksRecord() {
        when(chatMessageMapper.exists(any(Wrapper.class))).thenReturn(true);
        assertTrue(chatMessageService.exists(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSenderId, 10L)));
        when(chatMessageMapper.exists(any(Wrapper.class))).thenReturn(false);
        assertFalse(chatMessageService.exists(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSenderId, 99L)));
    }
}