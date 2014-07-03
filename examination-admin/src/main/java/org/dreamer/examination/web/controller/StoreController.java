package org.dreamer.examination.web.controller;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.dreamer.examination.entity.*;
import org.dreamer.examination.rbac.ShiroDatabaseRealm;
import org.dreamer.examination.service.*;
import org.dreamer.examination.vo.QuestionStoreVO;
import org.dreamer.examination.search.QuestionIndexer;
import org.dreamer.examination.utils.SysUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author lcheng
 * @version 1.0
 *          ${tags}
 */
@Controller
@RequestMapping(value = "/store")
public class StoreController {

    @Autowired
    private QuestionStoreService storeService;
    @Autowired
    private ExamScheduleService examScheduleService;
    @Autowired
    private CollegeService collegeService;
    @Autowired
    private RBACService rbacService;
    @Autowired
    private StorePushSettingService storePushService;

    // private static String[] majors = {"M001", "M002", "M003", "M004", "M005"};
    @RequiresRoles(value = {"admin"})
    @RequestMapping(value = "/list")
    public ModelAndView getStoreInfoList(@PageableDefault Pageable page) {
        ModelAndView mv = new ModelAndView("exam.store-list");
        Page<QuestionStoreVO> vos = storeService.getStoreAndQuesCountInfo(page);
        mv.addObject("store", vos);
        mv.addObject("page", vos.getNumber() + 1);
        mv.addObject("totalPage", vos.getTotalPages());
        return mv;
    }

    @RequiresRoles(value = {"admin"})
    @RequestMapping(value = "/add")
    public String addStore(ModelMap map) {
        // map.addAttribute("majors", majors);
        return "exam.store-add";
    }

    @RequiresRoles(value = {"admin"})
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addStore(String name, String comment, Boolean generic, String storeMajor) {
        QuestionStore store = new QuestionStore(name, comment);
        if (generic != null) {
            store.setGeneric(generic);
        } else {
            store.setGeneric(false);
        }
        String[] majors = {};
        if (storeMajor != null) {
            majors = storeMajor.split(",");
        }
        storeService.addQuestionStore(store, majors);
        return "redirect:/store/list";
    }

    @RequiresRoles(value = {"admin"})
    @RequestMapping(value = "/edit/{id}")
    public ModelAndView editStore(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("exam.store-edit");
        QuestionStore store = storeService.getStore(id);
        List<MajorStoreRelation> rels = storeService.getMajorStoreRelation(id);
        StringBuilder sb = new StringBuilder();
        for (MajorStoreRelation rel : rels) {
            sb.append(rel.getMajor() + ",");
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        mv.addObject("store", store);
        mv.addObject("rels", sb.toString());
        //  mv.addObject("majors", majors);
        return mv;
    }

    @RequiresRoles(value = {"admin"})
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public String editStore(QuestionStore store, String storeMajor) {
        String[] majors = {};
        if (storeMajor != null && storeMajor.length() > 0) {
            majors = storeMajor.split(",");
        }
        storeService.updateQuestionStore(store, majors);
        return "redirect:/store/list";
    }

    @RequiresRoles(value = {"admin"})
    @RequestMapping(value = "/delete/{id}")
    public String deleteStore(@PathVariable("id") long id) {
        storeService.deleteStore(id);
        checkAndDeleteIndex(id);
        return "redirect:/store/list";
    }

    @RequestMapping(value = "/major")
    @ResponseBody
    public JSONPObject getMajorStore(Long major, String callback) {
        List<QuestionStore> stores = storeService.getStoreForMajor(major);
        JSONPObject jsonp = new JSONPObject(callback, stores);
        return jsonp;
    }

    @RequestMapping(value = "/pushSetting")
    public ModelAndView storePushSetting() {
        ModelAndView mv = new ModelAndView("exam.pushSetting");
        List<Integer> grades = examScheduleService.getStudentSessions();
        mv.addObject("grades", grades);
        ShiroDatabaseRealm.ShiroUser user = rbacService.getCurrentUser();
        if (user != null) {
            Long collegeId = user.getCollegeId();
            if (collegeId != null && collegeId == -1) {
                List<College> colleges = collegeService.getAllColleges();
                mv.addObject("colleges", colleges);
            } else {
                mv.addObject("college", collegeId);
            }
        }
        return mv;
    }
    @RequestMapping(value = "/pushSetting/get")
    @ResponseBody
    public StorePushSetting getStorePushSetting(
            Long collegeId, Integer grade, Integer degree) {
        Types.DegreeType degreeType = null;
        if (degree == 0){
            degreeType = Types.DegreeType.Bachelor;
        }else {
            degreeType = Types.DegreeType.Master;
        }
        StorePushSetting setting = null;
        setting = storePushService.getSetting(collegeId,grade,degreeType);
        if (setting==null){
            setting = new StorePushSetting(collegeId,grade,degreeType);
        }
        return setting;
    }
    @RequestMapping(value = "/pushSetting/add")
    @ResponseBody
    public Result saveStorePushSetting(Long id,Long collegeId,
                                       Integer grade, Integer degree,boolean pushDiscipline){
        Types.DegreeType degreeType = null;
        if (degree == 0){
            degreeType = Types.DegreeType.Bachelor;
        }else {
            degreeType = Types.DegreeType.Master;
        }
        StorePushSetting setting =new StorePushSetting(collegeId,grade,degreeType,pushDiscipline);
        if (id!=null){
            setting.setId(id);
        }
        Result result = null;
        try{
            storePushService.addSetting(setting);
            result = new Result(true,"");
        }catch (Exception e){
            result = new Result(false,"");
        }
        return result;
    }

    private void checkAndDeleteIndex(long storeId) {
        String type = SysUtils.getConfigValue("question.list.type", "db");
        boolean index = type.equals("index") ? true : false;
        if (index) {
            QuestionIndexer indexer = SysUtils.getBean(QuestionIndexer.class);
            indexer.deleteByStore(storeId);
        }
    }
}
