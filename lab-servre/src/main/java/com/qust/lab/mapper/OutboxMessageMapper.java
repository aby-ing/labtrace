package com.qust.lab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qust.lab.pojo.entity.OutboxMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OutboxMessageMapper
        extends BaseMapper<OutboxMessage> {

    @Select("""
            SELECT *
            FROM outbox_message
            WHERE status IN ('PENDING', 'FAILED')
              AND (
                    next_retry_at IS NULL
                    OR next_retry_at <= NOW()
                  )
            ORDER BY id ASC
            LIMIT 50
            """)
    List<OutboxMessage> selectReadyMessages();

    @Update("""
            UPDATE outbox_message
            SET status = 'SENDING',
                retry_count = retry_count + 1
            WHERE id = #{id}
              AND status IN ('PENDING', 'FAILED')
              AND (
                    next_retry_at IS NULL
                    OR next_retry_at <= NOW()
                  )
            """)
    int markSending(@Param("id") Long id);

    @Update("""
            UPDATE outbox_message
            SET status = 'PUBLISHED',
                published_at = NOW(),
                last_error = NULL
            WHERE id = #{id}
              AND status = 'SENDING'
            """)
    int markPublished(@Param("id") Long id);

    @Update("""
            UPDATE outbox_message
            SET status = 'FAILED',
                next_retry_at = DATE_ADD(NOW(), INTERVAL 30 SECOND),
                last_error = #{error}
            WHERE id = #{id}
              AND status = 'SENDING'
            """)
    int markFailed(
            @Param("id") Long id,
            @Param("error") String error
    );
}
