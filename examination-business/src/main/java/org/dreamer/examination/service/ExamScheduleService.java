package org.dreamer.examination.service;

import org.dreamer.examination.entity.ExamSchedule;
import org.dreamer.examination.entity.ExamScheduleVO;
import org.dreamer.examination.entity.ExamScheduleViewVO;
import org.dreamer.examination.entity.ExaminationViewVO;
import org.dreamer.examination.repository.ExamScheduleDao;
import org.dreamer.examination.repository.ExamScheduleViewDao;
import org.dreamer.examination.sql.model.SqlQueryItem;
import org.dreamer.examination.sql.model.SqlSortItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamScheduleService {

    @Autowired
    private ExamScheduleDao scheduleDao;

    @Autowired
    private ExamScheduleViewDao scheduleViewDao;

    public void addExamSchedule(ExamSchedule schedule) {
        scheduleDao.save(schedule);
    }

    public ExamSchedule getExamSchedule(long id) {
        return scheduleDao.findOne(id);
    }

    public Long getExamTemplateId(String major) {
        Pageable p = new PageRequest(0, 1);
        List<Long> list = scheduleDao.findScheduleByDate(major, p);
        return (list != null && list.size() == 1) ? list.get(0) : null;
    }

    public List<ExamScheduleVO> getExamSchedule(String major){
        return scheduleDao.findSchedule(major);
    }

    /**
     * 获取所有考试安排
     * @return
     */
    public List<ExamSchedule> getAllSchedule( )
    {
        return scheduleDao.findAll();
    }

    /**
     * 按条件查询你考试计划
     * @param _paramList
     * @param _sortList
     * @param _page
     * @return
     */
    public Page<ExamScheduleViewVO> getScheduleByFilter(List<SqlQueryItem> _paramList, List<SqlSortItem> _sortList, Pageable _page  ){
        Page<ExamScheduleViewVO> examPage =  scheduleViewDao.queryResult(_paramList,_sortList,_page);
        return examPage;
    }

    public ExamScheduleViewVO getScheduleViewVO( Long id )
    {
        return scheduleViewDao.findOne( id  );
    }


    public void delete(Long id ) {
        scheduleDao.delete( id );
    }
}
