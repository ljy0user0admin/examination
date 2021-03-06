package org.dreamer.examination.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lcheng
 * @version 1.0
 *          ${tags}
 */
public class ExamRecordVO implements Serializable {
    private String staffId;
    private Long examId;
    private Long scheduleId;
    private String examName;
    private float finalScore;
    private Date examDate;
    private Date examCommitDate;
    private Date scheduleStartDate;
    private Date scheduleEndDate;

    public ExamRecordVO() {
    }

    public ExamRecordVO(String staffId, Long examId, Long scheduleId, String examName,
                        float finalScore, Date examDate) {
        this.staffId = staffId;
        this.examId = examId;
        this.scheduleId = scheduleId;
        this.examName = examName;
        this.finalScore = finalScore;
        this.examDate = examDate;
    }

    public ExamRecordVO(String staffId, Long examId, Long scheduleId, String examName,
                        float finalScore, Date examDate, Date examCommitDate,
                        Date scheduleStartDate, Date scheduleEndDate) {
        this.staffId = staffId;
        this.examId = examId;
        this.scheduleId = scheduleId;
        this.examName = examName;
        this.finalScore = finalScore;
        this.examDate = examDate;
        this.examCommitDate = examCommitDate;
        this.scheduleStartDate = scheduleStartDate;
        this.scheduleEndDate = scheduleEndDate;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public float getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(float finalScore) {
        this.finalScore = finalScore;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Date getExamCommitDate() {
        return examCommitDate;
    }

    public void setExamCommitDate(Date examCommitDate) {
        this.examCommitDate = examCommitDate;
    }

    public Date getScheduleStartDate() {
        return scheduleStartDate;
    }

    public void setScheduleStartDate(Date scheduleStartDate) {
        this.scheduleStartDate = scheduleStartDate;
    }

    public Date getScheduleEndDate() {
        return scheduleEndDate;
    }

    public void setScheduleEndDate(Date scheduleEndDate) {
        this.scheduleEndDate = scheduleEndDate;
    }
}
