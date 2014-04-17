package org.dreamer.examination.web.controller;

import org.codehaus.jackson.map.util.JSONPObject;
import org.dreamer.examination.business.ExaminationManager;
import org.dreamer.examination.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

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

    /**
     * <p>学生参加考试。根据学生专业到后台匹配考试模板，
     * 匹配到模板后,为学生返回试题列表，考试Id、试卷号</p>
     */
    @RequestMapping(value = "/new")
    @ResponseBody
    public JSONPObject takeExamination(String staffId, String major,String callback) {
        ExamAndQuestionVO vo = examManager.newExamination(staffId, major);
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
    @RequestMapping(value = "/commitAnswer")
    @ResponseBody
    public JSONPObject commitAnswer(List<Answer> answers) {
        examManager.commitAnswers(answers);
        return null;
    }

    @RequestMapping(value = "/commit")
    @ResponseBody
    public JSONPObject commitExam(long examId, String callback) {
        return null;
    }
}
