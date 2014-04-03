package org.dreamer.examination.repository;

import org.dreamer.examination.entity.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author lcheng
 * @version 1.0
 *          ${tags}
 */
public interface PaperDao extends JpaRepository<Paper, Long> {

    public List<Paper> findByTemplateId(Long templateId);

    public Page<Paper> findByTemplateId(Long templateId, Pageable pageable);

}
