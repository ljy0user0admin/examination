package org.dreamer.examination.service;

import org.dreamer.examination.entity.Answer;
import org.dreamer.examination.repository.AnswerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lcheng on 2014/4/13.
 */
@Service
public class AnswerService {

    @Autowired
    private AnswerDao answerDao;

    public void addAnswers(List<Answer> answers){
        for (Answer a:answers){
            int count = answerDao.countByExamIdAndQuesId(a.getExamId(),a.getQuesId());
            if (count>0){
                answerDao.deleteByExamIdAndQuesId(a.getExamId(),a.getQuesId());
            }
        }
        answerDao.save(answers);
    }

    public void addAnswer(Answer a){
       if (a!=null){
           int count = answerDao.countByExamIdAndQuesId(a.getExamId(),a.getQuesId());
           if (count>0){
               answerDao.deleteByExamIdAndQuesId(a.getExamId(),a.getQuesId());
           }
           answerDao.save(a);
       }
    }
}