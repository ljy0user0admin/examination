package org.dreamer.examination.service;

import org.dreamer.examination.entity.*;
import org.dreamer.examination.repository.ExamScheduleDao;
import org.dreamer.examination.repository.ExamScheduleViewDao;
import org.dreamer.examination.repository.ScheduleMajorDao;
import org.dreamer.examination.sql.model.SqlQueryItem;
import org.dreamer.examination.sql.model.SqlSortItem;
import org.dreamer.examination.vo.ExamScheduleVO;
import org.dreamer.examination.vo.ScheduleDateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ExamScheduleService {

    @Autowired
    private ExamScheduleDao scheduleDao;

    @Autowired
    private ExamScheduleViewDao scheduleViewDao;
    @Autowired
    private ScheduleMajorDao scheduleMajorDao;

    public void addExamSchedule(ExamSchedule schedule) {
        scheduleDao.save(schedule);
    }

    public void addExamSchedule(ExamSchedule schedule, List<ScheduleMajor> majors) {
        scheduleDao.save(schedule);
        Long sid = schedule.getId();
        scheduleMajorDao.deleteByScheduleId(sid);
        for (ScheduleMajor major : majors) {
            major.setScheduleId(sid);
        }
        scheduleMajorDao.save(majors);
    }

    public void addExamSchedule(ExamSchedule schedule, String majors) {
        String[] majorArr = majors.split(",");
        if (majorArr.length > 0) {
            List<ScheduleMajor> majorList = new ArrayList<>();
            for (String major : majorArr) {
                ScheduleMajor sm = new ScheduleMajor();
                sm.setMajorId(major);
                majorList.add(sm);
            }
            addExamSchedule(schedule, majorList);
        }
    }

    public int getScheduleCountUseTemp(Long tempId) {
        return scheduleDao.countByTemplateId(tempId);
    }

    public ExamSchedule getExamSchedule(long id) {
        return scheduleDao.findOne(id);
    }

    public List<String> getExamScheduleMajors(long sid) {
        return scheduleMajorDao.findByScheduleId(sid);
    }

    public Long getExamTemplateId(String major) {
        Pageable p = new PageRequest(0, 1);
        List<Long> list = scheduleDao.findScheduleByDate(major, p);
        return (list != null && list.size() == 1) ? list.get(0) : null;
    }

    public List<ExamScheduleVO> getExamSchedule(String major) {
        return scheduleDao.findSchedule(major);
    }

    /**
     * 获取所有考试安排
     *
     * @return
     */
    public List<ExamSchedule> getAllSchedule() {
        return scheduleDao.findAll();
    }

    /**
     * 分页获得所有的考试安排
     *
     * @param pageable
     * @return
     */
    public Page<ExamSchedule> getScheduleByName(String name, Pageable pageable) {
        return scheduleDao.findByNameLike(name, pageable);
    }

    /**
     * 按条件查询你考试计划
     *
     * @param _paramList
     * @param _sortList
     * @param _page
     * @return
     */
    public Page<ExamScheduleViewVO> getScheduleByFilter(List<SqlQueryItem> _paramList, List<SqlSortItem> _sortList, Pageable _page) {
        Page<ExamScheduleViewVO> examPage = scheduleViewDao.queryResult(_paramList, _sortList, _page);
        return examPage;
    }

    public ExamScheduleViewVO getScheduleViewVO(Long id) {
        return scheduleViewDao.findOne(id);
    }


    public void delete(Long id) {
        scheduleDao.delete(id);
    }

    public List<ScheduleDateVO> getScheduleDataByData(Date begin, Date end) {
        List<ScheduleDateVO> scheduleList = scheduleDao.findScheduleByDateFilter(begin, end);
        return scheduleList;
    }

    public List<Integer> getStudentSessions() {
        return scheduleDao.findStudentSession();
    }
}
