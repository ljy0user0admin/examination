<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<script type="text/javascript" src="${ctx}/asset/js/plugins/validation/jquery.validate.js"></script>
<script type="text/javascript" src="${ctx}/asset/js/plugins/validation/messages_zh.js"></script>

<script type="text/javascript">
    function bindConfirmCheck() {
        $("#pwdConfirm").on("keyup", function () {
            var confirm = $(this).val(),
                newPwd = $("#newPwd").val();
            if (confirm == newPwd) {
                $("#confirmMsg").removeClass("glyphicon glyphicon-remove").addClass("glyphicon glyphicon-ok");
            } else {
                $("#confirmMsg").removeClass("glyphicon glyphicon-ok").addClass("glyphicon glyphicon-remove");
            }
        });
        $("#newPwd").on("keyup",function(){
            var confirm = $("#pwdConfirm").val(),
                    newPwd = $(this).val();
            if(!confirm||confirm.length==0){
                $("#confirmMsg").removeClass("glyphicon glyphicon-ok glyphicon-remove");
            }else{
                if (confirm == newPwd) {
                    $("#confirmMsg").removeClass("glyphicon glyphicon-remove").addClass("glyphicon glyphicon-ok");
                } else {
                    $("#confirmMsg").removeClass("glyphicon glyphicon-ok").addClass("glyphicon glyphicon-remove");
                }
            }
        });
    }
    function save() {
        var newPwd = $("#newPwd").val(),
                pwdConfirm = $("#pwdConfirm").val();
        if (newPwd == pwdConfirm) {
            var valid = $("#newPwdForm").valid();
            if(!valid){
                alert("新密码不能为空，并且至少6个字符长!");
                return;
            }
            $.post("${ctx}/rbac/modifyPwd", {'newPwd': newPwd}, function (data) {
                if (data.success) {
                    alert("密码修改成功!");
                } else {
                    alert("密码修改失败!");
                }
            });
        } else {
            alert("您两次输入的密码不一致!");
        }
    }

    function bindValidator(){
        $("#newPwdForm").validate({
            rules:{
                "newPwd":{required: true,minlength:6},
                "pwdConfirm":{required: true,minlength:6}
            }
        });
    }

    $(document).ready(function(){
        bindConfirmCheck();
        bindValidator();
    });

</script>
