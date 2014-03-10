package org.dreamer.examination.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 试题
 * Created by lcheng on 14-3-9.
 */
@Entity
@Table(name = "questions")
@Inheritance
@DiscriminatorColumn(name = "QUES_TYPE")
public class Question implements Serializable {

    public enum Difficulty {
        Easy, Moderate, DIFFICULT
    }

    public Question(){}

    @Id()
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ID_QUES")
    @TableGenerator(name = "ID_QUES", table = "ids_gen", pkColumnName = "ID_NAME",
            valueColumnName = "ID_VALUE", initialValue = 1)

    private long id;

    //题干
    @Column(nullable = false, length = 300)
    private String stem;
    //难易程度
    @Enumerated
    private Difficulty difficulty;
    @Column(length = 100)
    private String answer;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStem() {
        return stem;
    }

    public void setStem(String stem) {
        this.stem = stem;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
