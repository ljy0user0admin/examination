package org.dreamer.examination.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.dreamer.examination.business.ExaminationManager;
import org.dreamer.examination.entity.*;
import org.dreamer.examination.service.ExamScheduleService;
import org.dreamer.examination.service.ExaminationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 考试控制器
 *
 * @author lcheng
 * @version 1.0
 *          ${tags}
 */
@Controller
@RequestMapping(value = "/exam")
public class ExamController {

    @Autowired
    private ExaminationManager examManager;
    @Autowired
    private ExaminationService examService;
    @Autowired
    private ExamScheduleService scheduleService;

    /**
     * <p>学生参加考试。根据学生专业到后台匹配考试模板，
     * 匹配到模板后,为学生返回试题列表，考试Id、试卷号</p>
     */
    @RequestMapping(value = "/new")
    @ResponseBody
    public JSONPObject takeExamination(String staffId, String major,long scheduleId,String callback) {
        ExamAndQuestionVO vo = examManager.newExamination(staffId, major,scheduleId);
        JSONPObject r = new JSONPObject(callback, vo);
        return r;
    }

    /**
     * 获取试卷中试卷某类型试题
     *
     * @param examId
     * @param quesType
     * @param callback
     * @return
     */
    @RequestMapping(value = "/fetch")
    @ResponseBody
    public JSONPObject fetchExamQuestions(long examId, String quesType, String callback) {
        List<ExamQuestionVO> quesVO = examManager.getPaperQuestions(examId,
                Types.QuestionType.getTypeFromShortName(quesType));
        JSONPObject jsop = new JSONPObject(callback, quesVO);
        return jsop;
    }

    /**
     * <p>提交答案</p>
     *
     * @param answers
     * @return
     */
    @RequestMapping(value = "/commitAnswer", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Result commitAnswer(String answers) {
        Result r = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Answer> answerList = mapper.readValue(answers,
                    TypeFactory.defaultInstance().constructCollectionType(List.class, Answer.class));
            examManager.commitAnswers(answerList);
            r = new Result(true, "提交成功！");
        } catch (IOException e) {
            r = new Result(false, "提交失败！");
            e.printStackTrace();
        }
        return r;
    }

    @RequestMapping(value = "/commit")
    @ResponseBody
    public JSONPObject commitExam(long examId, String callback) {
        float score = examService.scoreExam(examId);
        JSONPObject jsonp = new JSONPObject(callback, score);
        return jsonp;
    }

    @RequestMapping(value = "/examRecords")
    @ResponseBody
    public JSONPObject examRecords(String staffId, String callback, Pageable page) {
        Page<ExamRecordVO> examRecordVOs = examService.getExamRecords(staffId, page);
        return new JSONPObject(callback, examRecordVOs);
    }

    @RequestMapping(value = "/scheduleExamRecords")
    @ResponseBody
    public JSONPObject examRecords(String staffId,Long scheduleId, String callback) {
        List<ExamRecordVO> examRecordVOs = examService.getExamRecords(staffId, scheduleId);
        return new JSONPObject(callback, examRecordVOs);
    }

    @RequestMapping(value = "/examSchedule")
    @ResponseBody
    public JSONPObject examSchedule(String major, String callback) {
        List<ExamScheduleVO> scheduleVOs = scheduleService.getExamSchedule(major);
        return new JSONPObject(callback, scheduleVOs);
    }
}
