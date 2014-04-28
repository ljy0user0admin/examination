<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<link type="text/css" href="${ctx}/asset/js/plugins/chosen/chosen.bootstrap.css" rel="stylesheet"/>
<link type="text/css" href="${ctx}/asset/js/plugins/wizard/bwstep.css" rel="stylesheet"/>
<style>
    .temp-ques-conf a{
        text-decoration: none;border-bottom: solid 1px #0088cc;text-align: center
    }
    .temp-ques-conf a:hover{
        text-decoration: none;
    }
    .margin15 {margin-left: 15px;margin-bottom: 15px;}
    .margin10 {margin-left: 10px;margin-bottom: 10px;}
</style>

<script type="text/javascript" src="${ctx}/asset/js/plugins/chosen/chosen.jquery.js"></script>
<script type="text/javascript" src="${ctx}/asset/js/plugins/wizard/jquery.bootstrap.wizard.js"></script>
<script type="text/javascript" src="${ctx}/asset/js/plugins/bspaginator/bootstrap-paginator.js"></script>
<script type="text/javascript" src="${ctx}/asset/js/plugins/validation/jquery.validate.js"></script>
<script type="text/javascript" src="${ctx}/asset/js/plugins/validation/messages_zh.js"></script>
<script type="text/javascript" src="${ctx}/asset/js/plugins/template/jsrender.js"></script>

<script id="quesTmpl" type="text/x-jsrender">
   <tr>
      <td><input type="checkbox" name="idCheckbox" qid={{:id}} /></td>
      <td>{{:id}}</td>
      <td>{{:stem}}</td>
      {{if answer=='1'}}
        <td class="text-center">√</td>
      {{else answer=='0'}}
         <td class="text-center">×</td>
      {{else}}
         <td class="text-center">{{:answer}}</td>
      {{/if}}
      <td class="text-center"><input type="text" value="1"/></td>
   </tr>
</script>
<script id="tempQuesConfTmpl" type="text/x-jsrender">
     <div class="row temp-ques-conf">
        <a class="col-sm-2" sid="{{:sid}}">{{:sname}}</a>
        <a class="col-sm-1 margin10" count="{{:count}}">题数({{:count}})</a>
        <a class="col-sm-1 margin10" score="{{:score}}">每题({{:score}}分)</a>
        <a class="col-sm-1 margin10" onclick="deleteConfigedTemp(event);">删除</a>
     </div>
</script>

<script type="text/javascript">
    var mcq = [],
        mcqs = [],
        mcqt =[];
    function initWizard() {
        $("#wizard").bootstrapWizard({
            'tabClass': 'bwizard-steps'
        });
    }
    function initModalDialog() {
        $("#confModal").modal({
            show: false
        });
    }
    function initValidator() {
        return $("#tempConfForm").validate({
            rules: {
                "confCount": {required: true, number: true},
                "confScore": {required: true, number: true}
            }
        });
    }
    function createChosen(el, width) {
        $(el).chosen({
            no_results_text: "没有找到",
            max_selected_options: 5,
            width: width,
            disable_search_threshold: 10
        });
    }
    function bindTempConfEvent() {
        $("a.conf").on("click", function () {
            var title = $(this).attr("tname"),
                    tableId = $(this).attr("tid");
            $("#modalTitle").text(title);
            $("#modalTitle").attr("tid", tableId);
            $('#confModal').modal('show');
        });
    }
    function createConfigedTemp(storeId, storeName, count, score, cId){
        var tpl = $.templates("#tempQuesConfTmpl");
        var html = tpl.render({'sid':storeId,'sname':storeName,'count':count,'score':score});
        $(html).appendTo(cId);
    }
    function deleteConfigedTemp(e){
        var target;
        if (!e) var e = window.event;
        if (e.target) target = e.target;
        else if (e.srcElement) target = e.srcElement;
        if (target.nodeType == 3)
            target = targ.parentNode;
        if (target) {
            $(target).parent().remove();
        }
        return false;
    }
    function bindConfConfirmEvent() {
        $("#confConfirm").on("click", function () {
            var valid = $("#tempConfForm").valid();
            if (valid) {
                var sid = $("#confStoreId").val(),
                        sname = $("#confStoreId option:selected").text(),
                        count = $("#confCount").val(),
                        score = $("#confScore").val(),
                        tableId = $("#modalTitle").attr("tid");
                //createRow(sid, sname, count, score, tableId);
                createConfigedTemp(sid,sname,count,score,("#"+tableId));
                $('#confModal').modal('hide');
                //$("#tempConfForm")[0].reset();
                $("#confCount").val("");
                $("#confScore").val("");
            }
            return false;
        });
    }

    function bindSelectChangeEvent() {
        $("#mcStoreId").on("change", function () {
            loadMustChooseData(0, 10);
        });
        $("#mcqt").change(function () {
            loadMustChooseData(0, 10);
        });
    }

    function bindCheckboxChangeEvent(){
        $("input[name=idCheckbox]").on("change",function(){
            var qid = $(this).attr("qid");
            var sel = $(this).parent().parent().children("td.text-center")[1],
                iel= $(sel).children()[0],
                score = $(iel).val();
           if(this.checked){
               if($.inArray(qid,mcq)==-1){
                   mcq.push(qid);
                   mcqs.push(score);
                   mcqt.push($("#mcqt").val());
               }
           }else{
               var idx = $.inArray(qid,mcq);
               if(idx!=-1){
                   mcq.splice(idx,1);
                   mcqs.splice(idx,1);
                   mcqt.splice(idx,1);
               }
           }
        });
        $("#mcTableBody tr td.text-center input").on("change",function(){
           var score = $(this).val();
           var fel = $(this).parent().parent().children()[0],
               cel = $(fel).children()[0],
               sid = $(cel).attr("qid"),
               idx = $.inArray(sid,mcq);
           if($.isNumeric(score)&&parseInt(score)>0){
               if(idx!=-1){
                   mcqs.splice(idx,1,score);
               }
           }else{
               alert("请输入大于0的整数！");
               if(idx!=-1){
                   $(this).val(mcqs[idx]);
               }else{
                   $(this).val(1);
               }
           }

        });
    }

    function bindAllCheckEvent(){
        $("#allCheckbox").on("ifChanged",function(){
            if(this.checked){
//               $("input[name='idCheckbox']").each(function(){
//                   $(this).attr("checked",true);
//               });
               $("input[name='idCheckbox']").attr("checked",true);
                $("input[name='idCheckbox']").trigger("change");
            }else{
//                $("input[name='idCheckbox']").each(function(){
//                    $(this).attr("checked",false);
//                });
                $("input[name='idCheckbox']").attr("checked",false);
                $("input[name='idCheckbox']").trigger("change");
            }
        })
    }

    function reMark(){
        $.each(mcq,function(idx,val){
            var sel =$("#mcTableBody input[qid="+val+"]");
            if(sel){
                sel.attr("checked","checked");
                var tdel = sel.parent().parent().children("td.text-center")[1],
                    iel = $(tdel).children()[0];
                $(iel).val(mcqs[idx]);
            }
        });
    }

    function collectConfigedTempQues(cid,quesType){
       var rowSel = "#"+cid+" .row";
       var data = new Array();
       $(rowSel).each(function(idx,val){
           var obj = {};
           $(val).children().each(function(sidx,sval){
               switch (sidx){
                   case 0:
                       obj.storeId = $(sval).attr("sid");
                       break;
                   case 1:
                       obj.count = $(sval).attr("count");
                       break;
                   case 2:
                       obj.score = $(sval).attr("score");
                       break;
                   default :
                       break;
               }
           });
           obj.quesType =quesType;
           data.push(obj);
       });
       return data;
    }

    function collectAllTempQues(){
        var d1 = collectConfigedTempQues("chContainer","CH"),
            d2 = collectConfigedTempQues("mcContainer","MC"),
            d3 = collectConfigedTempQues("tfContainer","TF");
        var total = d1.concat(d2, d3);
        return total;
    }

    function collectAllData(){
        var result = {};
        result.name = $("#name").val();
        result.passScore = $("#passScore").val();
        result.multiChooseMixedIn = $("#multiChoiceMixedInChoice").parent().attr("aria-checked") == "true" ? true : false;
        var mcdef =[];
        $.each(mcq,function(idx,val){
            var obj = {};
            obj.quesId= val;
            obj.quesType=mcqt[idx];
            obj.score = mcqs[idx];
            mcdef.push(obj);
        });
        result.mustChooseDefs = mcdef;
        result.tempQuesDefs = collectAllTempQues();
        //console.log(JSON.stringify(result));
        return result;
    }

    function save(){
        var result = collectAllData();
        if(result.tempQuesDefs && result.tempQuesDefs.length>0){
            $.post("${ctx}/template/new",{
                "template":JSON.stringify(result)
            },function(data){
                 if(data.success){
                    window.location.href = "${ctx}/template/list";
                 }else{
                     alert(data.message);
                 }
            });
        }
    }

    function loadMustChooseData(page, size) {
        $.ajax("${ctx}/question/mclist", {
            dataType: "json",
            data: {
                storeId: $("#mcStoreId").val(),
                quesType: $("#mcqt").val(),
                page: page,
                size: size
            },
            success: function (data) {
                if (data.totalPages > 0) {
                    $("#mcTableBody").empty();
                    var tpl = $.templates("#quesTmpl");
                    $("#mcTableBody").html(tpl.render(data.content));
                    createPaginator("#paginator",(data.number+1),(data.totalPages));
                    reMark();
                    bindCheckboxChangeEvent();
                }else{
                    $("#mcTableBody").empty();
                    $("#paginator").empty();
                }
            }
        });
    }

    function createPaginator(el,curPage,totalPage){
        if(totalPage==0 || curPage>totalPage){
            return;
        }
        var pgOptions = {
            bootstrapMajorVersion : 3,
            currentPage: curPage,
            totalPages: totalPage,
            onPageClicked: function(e,originalEvent,type,page){
                var p = page - 1;
                loadMustChooseData(p,10);
            },
            elementCls:"pagination pagination-sm no-margin pull-right",
            itemTexts: function (type, page) {
                switch (type) {
                    case "first":
                        return "&laquo;";
                    case "prev":
                        return "&lsaquo;";
                    case "next":
                        return "&rsaquo;";
                    case "last":
                        return "&raquo;";
                    case "page":
                        return page;
                }
            }
        };
        $(el).bootstrapPaginator(pgOptions);
    }

    $(document).ready(function () {
        initWizard();
        initValidator();
        initModalDialog();
        createChosen("#confStoreId", "165px");
        createChosen("#mcStoreId", "250px");
        //bindAllCheckEvent();
        bindTempConfEvent();
        bindConfConfirmEvent();
        bindSelectChangeEvent();
    });
</script>