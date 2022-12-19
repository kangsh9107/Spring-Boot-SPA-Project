<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<!-- META -->
<meta charset="UTF-8">
<!-- CSS -->
<link rel="stylesheet" href="css/board.css">
<!-- JS -->
<script defer src="js/board.js"></script>
<title>WEB-INF/view/board/board_insert.jsp</title>
</head>
<body>
<!-- CONTENT -->
<div id='board'>
    <form class='frm frm_insert' method='post' enctype='multipart/form-data'>
        <label>작성자</label>
        <input type='text' name='id' value='hong'/><br/>
       
        <label>제목</label>
        <input type='text' name='subject' class='subject' value='제목'/><br/>
        <label></label>
        <textarea rows="5" cols="50" name='doc' class='doc'>내용입니다.</textarea><br/>
        <label>첨부</label>
        <input type='file' name='attFile' multiple="multiple"/>
        <div class='btnZone'>
            <input type='button' value='취소' class='btnSelect'>
            <input type='button' value='저장' class='btnInsertR'>
            <input type='hidden' name='nowPage' value='${pVo.nowPage }'>
            <input type='hidden' name='findStr' value='${pVo.findStr }'>
        </div>
    </form>
</div>
</body>
</html>