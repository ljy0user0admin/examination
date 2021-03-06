package org.dreamer.examination.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dreamer.examination.entity.*;
import org.dreamer.examination.importer.DefaultExcelImporter;
import org.dreamer.examination.importer.Importer;
import org.dreamer.examination.search.NRTLuceneFacade;
import org.dreamer.examination.search.QuestionIndexer;
import org.dreamer.examination.service.QuestionService;
import org.dreamer.examination.service.QuestionStoreService;
import org.dreamer.examination.utils.SysUtils;
import org.dreamer.examination.vo.QuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by lcheng on 2014/4/22.
 */
@Controller
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService quesService;
    @Autowired
    private QuestionStoreService storeService;
    @Autowired
    private QuestionIndexer indexer;

    @RequestMapping("/list")
    public ModelAndView questionList(Long storeId, String quesType, String queryText, @PageableDefault Pageable page) {
        ModelAndView mv = new ModelAndView("exam.question-list");
        List<QuestionStore> stores = storeService.getAll();
        if (storeId == null && (stores != null && stores.size() > 0)) {
            storeId = stores.get(0).getId();
        }
        if (quesType == null) {
            quesType = "CH";
        }
        if (storeId != null) {
            Page<Question> questions = null;
            if (StringUtils.isNotEmpty(queryText)) {
                try {
                    queryText = new String(queryText.getBytes("ISO8859-1"), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                questions = quesService.getQuestions(storeId,
                        Types.QuestionType.getTypeFromShortName(quesType), "%" + queryText + "%", page);
            } else {
                questions = quesService.getQuestions(storeId,
                        Types.QuestionType.getTypeFromShortName(quesType), page);
            }
            mv.addObject("questions", questions.getContent());
            mv.addObject("storeId", storeId);
            mv.addObject("quesType", quesType);
            mv.addObject("queryText", queryText);
            mv.addObject("page", questions.getNumber() + 1);
            mv.addObject("totalPage", questions.getTotalPages());
        }
        mv.addObject("stores", stores);
        return mv;
    }

    @RequestMapping(value = "/indexedList")
    public ModelAndView indexedQuestionList(String storeId, String quesType, String queryText,
                                            @PageableDefault Pageable page) {
        ModelAndView mv = new ModelAndView("exam.question-indexed");
        if (StringUtils.isNotEmpty(queryText)) {
            try {
                queryText = new String(queryText.getBytes("ISO8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        List<QuestionStore> stores = storeService.getAll();
        int offset = page.getOffset();
        int pageSize = page.getPageSize();
        Query query = createQuery(storeId, quesType, queryText);
        long totalCount = NRTLuceneFacade.instance().count(query);
        long totalPage = (totalCount % pageSize == 0) ? (totalCount / pageSize) : ((totalCount / pageSize) + 1);
        List<Document> queryResult = NRTLuceneFacade.instance().search(query, new SortField[]{new SortField("id", SortField.Type.LONG)}, offset, pageSize);
        List<QuestionVO> vos = toQuestions(queryResult);
        mv.addObject("questions", vos);
        mv.addObject("storeId", storeId);
        mv.addObject("quesType", quesType);
        mv.addObject("queryText", queryText);
        mv.addObject("page", page.getPageNumber() + 1);
        mv.addObject("totalPage", totalPage);
        mv.addObject("stores", stores);
        return mv;
    }

    @RequestMapping(value = "/mclist")
    @ResponseBody
    public Page<QuestionVO> mustChooseList(Long storeId, String quesType,String queryText, @PageableDefault Pageable page) {
        if(StringUtils.isNotEmpty(queryText)){
           try{
               queryText = new String(queryText.getBytes("ISO8859-1"), "UTF-8");
               queryText = "%"+queryText+"%";
           }catch (UnsupportedEncodingException e){
               e.printStackTrace();
           }
           return quesService.getMustChooseQuestion(storeId,
                   Types.QuestionType.getTypeFromShortName(quesType),queryText,page);
        }
        return quesService.getMustChooseQuestion(storeId,
                Types.QuestionType.getTypeFromShortName(quesType), page);
    }

    @RequestMapping(value = "/mcNotChoosedlist")
    @ResponseBody
    public Page<QuestionVO> mustChooseNotChoosedList(Long storeId, String quesType, Long tempId,
                                                     @PageableDefault Pageable page) {
        return quesService.getMustChooseQuestionNotChoosed(storeId,
                Types.QuestionType.getTypeFromShortName(quesType), tempId, page);
    }

    @RequiresPermissions(value = {"question:edit"})
    @RequestMapping("/edit/{id}")
    public ModelAndView editQuestion(@PathVariable("id") Long id, Long storeId, String quesType, int page) {
        Question question = quesService.getQuestion(id);
        String type = getQuestionTypeFromClass(question);
        ModelAndView mv = new ModelAndView("exam.question-edit");
        mv.addObject("q", question);
        mv.addObject("storeId", storeId);
        mv.addObject("quesType", quesType);
        mv.addObject("qType", type);
        mv.addObject("page", page);
        return mv;
    }

    @RequiresPermissions(value = {"question:edit"})
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public Result editQuestion(String question) {
        Result result = new Result(true, getQuestionListURLPrefix());
        ObjectMapper mapper = new ObjectMapper();
        try {
            QuestionVO vo = mapper.readValue(question, QuestionVO.class);
            Question q = quesService.getQuestion(vo.getId());
            q.setStem(vo.getStem());
            q.setAnswer(vo.getAnswer());
            q.setMustChoose(vo.isMustChoose());
            q.setImgPath(vo.getImgPath());

            if (vo.getOptions() != null && vo.getOptions().length > 0) {
                List<QuestionOption> options = Arrays.asList(vo.getOptions());
                ((ChoiceQuestion) q).setQuestionOptions(options);
            }
            quesService.addQuestion(q);
            checkAndUpdateIndex(vo);
        } catch (IOException e) {
            result = new Result(false, "");
            e.printStackTrace();
        }
        return result;
    }

    @RequiresPermissions(value = {"question:delete"})
    @RequestMapping(value = "/delete/{id}")
    public String deleteQuestion(@PathVariable("id") Long id, Long storeId, String quesType, int page, int size) {
        quesService.deleteQuestion(id);
        checkAndDeleteIndex(id);
        String urlPrefix = getQuestionListURLPrefix();
        StringBuilder sb = new StringBuilder();
        if (storeId != null) {
            sb.append("storeId=" + storeId + "&");
        }
        if (quesType != null) {
            sb.append("quesType=" + quesType + "&");
        }
        sb.append("page=" + page + "&size=" + size);
        String redUrl = urlPrefix + "?" + sb.toString();
        return "redirect:" + redUrl;
    }

    @RequestMapping(value = "/option/delete/{id}")
    @ResponseBody
    public Result deleteQuestionOption(@PathVariable("id") Long id) {
        Result result = null;
        try {
            quesService.deleteQuestionOption(id);
            result = new Result(true, "删除选项成功!");
        } catch (Exception e) {
            result = new Result(false, "删除选项失败!");
        }
        return result;
    }

    @RequestMapping(value = "/import")
    public ModelAndView importQuestions(Long storeId) {
        ModelAndView mv = new ModelAndView("exam.question-import");
        List<QuestionStore> stores = storeService.getAll();
        mv.addObject("stores", stores);
        mv.addObject("storeId", storeId);
        return mv;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public String importQuestions(Long storeId, MultipartFile file) {
        if (!file.isEmpty()) {
            String name = file.getOriginalFilename();
            File local = new File(System.getProperty("java.io.tmpdir") + name);
            try {
                file.transferTo(local);
                Importer importer = new DefaultExcelImporter(quesService);
                importer.doImport(local, storeId);
                Files.delete(local.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String urlPrefix = getQuestionListURLPrefix();
        return "redirect:" + urlPrefix + "?storeId=" + storeId + "&quesType=CH&page=0";
    }

    @RequestMapping(value="/tempFile")
    public void tempFileDownload(HttpServletRequest request,HttpServletResponse response){
        String rootPath = request.getSession().getServletContext().getRealPath("/WEB-INF");
        String tempFilePath = rootPath+"/doc/题库格式.xlsx";
        try {
            response.reset();
            String fileName = new String("题库格式.xlsx".getBytes(),"ISO8859-1");
            response.addHeader("Content-Disposition","attachment; filename="+fileName );
            response.setContentType("application/octet-stream");
            FileInputStream fis = new FileInputStream(new File(tempFilePath));
            int fileSize = 1024;
            byte[] buffer = new byte[fileSize];
            int read = -1;
            OutputStream os = response.getOutputStream();
            while((read = fis.read(buffer))>0){
                os.write(buffer,0,read);
                os.flush();
            }
            fis.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/countForStoreGroup")
    @ResponseBody
    public List<Object[]> countForStoreGroup(){
        return quesService.countForStoreGroup();
    }

    @RequestMapping("/countForStoreTypeNotMust")
    @ResponseBody
    public Map<String,Long> countForStoreAndTypeNotMust(Long storeId,String quesType){
        Types.QuestionType type = Types.QuestionType.getTypeFromShortName(quesType);
        Long count = quesService.countOfTypeQuestionNotMust(storeId,type);
        Map<String,Long> result = new HashMap<>();
        result.put("count",count);
        return result;
    }

    private Query createQuery(String storeId, String quesType, String queryTxt) {
        boolean storeEmpty = StringUtils.isEmpty(storeId);
        boolean quesTypeEmpty = StringUtils.isEmpty(quesType);
        boolean queryTxtEmpty = StringUtils.isEmpty(queryTxt);
        Query query = null;
        if (storeEmpty && quesTypeEmpty && queryTxtEmpty) {
            QueryParser qp = NRTLuceneFacade.instance().newQueryParser("stem");
            try {
                query = qp.parse("*:*");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            BooleanQuery bq = new BooleanQuery();
            if (!storeEmpty) {
                TermQuery sidq = new TermQuery(new Term("sid", storeId));
                bq.add(sidq, BooleanClause.Occur.MUST);
            }
            if (!quesTypeEmpty) {
                TermQuery qtq = new TermQuery(new Term("qt", quesType));
                bq.add(qtq, BooleanClause.Occur.MUST);
            }
            if (!queryTxtEmpty) {
                QueryParser qp = NRTLuceneFacade.instance().newQueryParser("stem");
                try {
                    Query sq = qp.parse(queryTxt);
                    bq.add(sq, BooleanClause.Occur.MUST);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            query = bq;
        }
        return query;
    }

    private List<QuestionVO> toQuestions(List<Document> docs) {
        List<QuestionVO> vos = new ArrayList<>();
        if (docs != null && docs.size() > 0) {
            for (Document doc : docs) {
                Long id = Long.valueOf(doc.get("id"));
                String stem = doc.get("stem");
                String answer = doc.get("ans");
                boolean isMC = doc.get("mc").equals("1") ? true : false;
                QuestionVO vo = new QuestionVO(id, stem, answer);
                vo.setMustChoose(isMC);
                vos.add(vo);
            }
        }
        return vos;
    }

    /**
     * 根据配置文件，动态获得试题列表的URL
     *
     * @return
     */
    private String getQuestionListURLPrefix() {
        String listType = SysUtils.getConfigValue("question.list.type", "db");
        return listType.equals("db") ? "/question/list" : "/question/indexedList";
    }

    private void checkAndDeleteIndex(Long quesId) {
        String listType = SysUtils.getConfigValue("question.list.type", "db");
        if (listType.equals("index")) {
            indexer.deleteQuestionIndex(quesId);
        }
    }

    private void checkAndUpdateIndex(QuestionVO vo) {
        String listType = SysUtils.getConfigValue("question.list.type", "db");
        if (listType.equals("index")) {
            indexer.updateQuestionIndex(vo
            );
        }
    }

    private String getQuestionTypeFromClass(Question q) {
        String type = (q instanceof TrueOrFalseQuestion) ? "TF" :
                ((q instanceof MultipleChoiceQuestion) ? "MC" : "CH");
        return type;
    }

}
