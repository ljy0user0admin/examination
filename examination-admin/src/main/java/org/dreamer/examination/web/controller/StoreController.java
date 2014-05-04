package org.dreamer.examination.web.controller;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.dreamer.examination.entity.MajorStoreRelation;
import org.dreamer.examination.entity.QuestionStore;
import org.dreamer.examination.entity.QuestionStoreVO;
import org.dreamer.examination.service.MajorStoreRelationService;
import org.dreamer.examination.service.QuestionStoreService;
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
    private MajorStoreRelationService storeRelService;

    private static String[] majors = {"M001","M002","M003","M004","M005"};

    @RequestMapping(value = "/list")
    public ModelAndView getStoreInfoList(@PageableDefault Pageable page){
        ModelAndView mv = new ModelAndView("exam.store-list");
        Page<QuestionStoreVO> vos =storeService.getStoreAndQuesCountInfo(page);
        mv.addObject("store",vos);
        mv.addObject("page",vos.getNumber()+1);
        mv.addObject("totalPage",vos.getTotalPages());
        return mv;
    }
    @RequestMapping(value = "/add")
    public String addStore(ModelMap map){
        map.addAttribute("majors",majors);
        return "exam.store-add";
    }
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public String addStore(String name,String comment,Boolean generic,String storeMajor){
        QuestionStore store = new QuestionStore(name,comment);
        if (generic!=null){
            store.setGeneric(generic);
        }else {
            store.setGeneric(false);
        }
        String[] majors = {};
        if (storeMajor!=null){
            majors = storeMajor.split(",");
        }
        storeService.addQuestionStore(store,majors);
        return "redirect:/store/list";
    }

    @RequestMapping(value = "/edit/{id}")
    public ModelAndView editStore(@PathVariable("id")long id){
        ModelAndView mv = new ModelAndView("exam.store-edit");
        QuestionStore store = storeService.getStore(id);
        List<MajorStoreRelation> rels = storeService.getMajorStoreRelation(id);
        StringBuilder sb = new StringBuilder();
        for (MajorStoreRelation rel :rels){
            sb.append(rel.getMajor()+",");
        }
        mv.addObject("store",store);
        mv.addObject("rels",sb.toString());
        mv.addObject("majors",majors);
        return mv;
    }
    @RequestMapping(value = "/edit",method = RequestMethod.POST)
    public String editStore(QuestionStore store,String storeMajor){
        String[] majors = {};
        if (storeMajor!=null){
            majors = storeMajor.split(",");
        }
         storeService.updateQuestionStore(store,majors);
        return "redirect:/store/list";
    }
    @RequestMapping(value = "/delete/{id}")
    public String deleteStore(@PathVariable("id") long id){
        storeService.deleteStore(id);
        return "redirect:/store/list";
    }

    @RequestMapping(value = "/major")
    @ResponseBody
    public JSONPObject getMajorStore(String major,String callback){
        List<QuestionStore> stores = storeService.getStoreForMajor(major);
        JSONPObject jsonp = new JSONPObject(callback,stores);
        return jsonp;
    }
}
