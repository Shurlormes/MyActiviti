<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>Activiti6流程设计器Demo</title>

</head>
<body>
<h2>
	<form id="form" action="/activiti/create">
		name: <input id="name" name="name" type="text" /><br>
		key: <input id="key" name="key" type="text" /><br>
		<input id="add" type="button" value="绘制流程" />
	</form>
</h2>
<div>
	<table width="100%">
	    <tr>
	        <td width="10%">模型编号</td>
	        <td width="10%">版本</td>
	        <td width="20%">模型名称</td>
	        <td width="20%">模型key</td>
	        <td width="40%">操作</td>
	    </tr>
	        <#list modelList as model>
	        <tr>
	            <td width="10%">${model.id}</td>
	            <td width="10%">${model.version}</td>
	            <td width="20%"><#if (model.name)??>${model.name}<#else> </#if></td>
	            <td width="20%"><#if (model.key)??>${model.key}<#else> </#if></td>
	            <td width="40%">
	             <a href="/activiti/editor?modelId=${model.id}">编辑</a>
	             <a href="/activiti/publish?modelId=${model.id}">发布</a>
	             <a href="/activiti/revoke?modelId=${model.id}">撤销</a>
	             <a href="/activiti/delete?modelId=${model.id}">删除</a>
	            </td>
	        </tr>
	       </#list>
	</table>
</div>
</body>
</html>
<script>

	var addBtn = document.getElementById('add')
	addBtn.onclick = function() {
		var nameObj = document.getElementById('name');
		var keyObj = document.getElementById('key');

		var errorFlag = false;
		if(!nameObj.value) {
			error(nameObj);
			errorFlag = true;
		}

		if(!keyObj.value) {
			error(keyObj);
			errorFlag = true;
		}

		if(!errorFlag) {
			document.getElementById('form').submit();
		}
	}

	function error(obj) {
		obj.style.border = "1px solid red";
		window.setTimeout(function() {
			obj.style.border = "1px solid black";
		}, 3*400);
	}

</script>