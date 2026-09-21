package com.qust.lab.srevice;
import com.qust.lab.pojo.dto.SampleHandoverCreateDTO;
import com.qust.lab.pojo.dto.SampleStatusChangeDTO;
import com.qust.lab.pojo.dto.SampleCreateDTO;
import com.qust.lab.pojo.vo.SampleVO;
import com.qust.lab.pojo.dto.SampleUpdateDTO;
import java.util.List;
import com.qust.lab.pojo.vo.SampleHandoverVO;
import com.qust.lab.common.result.PageResult;
import com.qust.lab.pojo.dto.SamplePageQueryDTO;


public interface SampleService {

    List<SampleVO> listAll();
    SampleVO create(
            Long creatorId,
            String idempotencyKey,
            SampleCreateDTO dto
    );
    SampleVO getById(Long id);
    SampleVO changeStatus(
            Long operatorId,
            Long id,
            SampleStatusChangeDTO dto
    );
    SampleVO update(
            Long operatorId,
            String role,
            Long id,
            SampleUpdateDTO dto
    );
    void delete(Long id);
    SampleVO handover(
            Long operatorId,
            Long id,
            String idempotencyKey,
            SampleHandoverCreateDTO dto
    );
    List<SampleHandoverVO> listHandoverHistory(Long sampleId);
    PageResult<SampleVO> page(SamplePageQueryDTO queryDTO);
}
