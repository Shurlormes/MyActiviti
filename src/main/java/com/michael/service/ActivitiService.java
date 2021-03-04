package com.michael.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.michael.exception.BaseException;
import com.michael.util.Image2Base64Util;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ActivitiService {

    private static final Logger log = LoggerFactory.getLogger(ActivitiService.class);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;



    public Model createModel(String name, String key) {
        List<Model> models = repositoryService.createModelQuery().modelKey(key).list();
        if (CollectionUtils.isNotEmpty(models)) {
            throw new BaseException("已存在相同的KEY");
        }

        Model model = repositoryService.newModel();
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, "");
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        model.setName(name);
        model.setKey(key);
        model.setMetaInfo(modelNode.toString());
        repositoryService.saveModel(model);
        createObjectNode(model.getId());
        return model;
    }

    /**
     * 创建模型时完善ModelEditorSource
     */
    @SuppressWarnings("deprecation")
    private void createObjectNode(String modelId) {
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        try {
            repositoryService.addModelEditorSource(modelId, editorNode.toString().getBytes("utf-8"));
        } catch (Exception e) {
            log.error("创建模型时完善ModelEditorSource服务异常", e);
        }
    }

    public void publishModel(String modelId) {
        Model modelData = repositoryService.getModel(modelId);
        List<Deployment> db = repositoryService.createDeploymentQuery().deploymentKey(modelData.getKey()).list();
        if (CollectionUtils.isNotEmpty(db)) {
            throw new BaseException("该模型已发布");
        }

        try {
            byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
            if (bytes == null) {
                throw new BaseException("模型数据为空，请先设计流程并成功保存，再进行发布");
            }
            JsonNode modelNode = new ObjectMapper().readTree(bytes);
            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            Deployment deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .key(modelData.getKey())
                    .addBpmnModel(modelData.getKey() + ".bpmn20.xml", model)
                    .deploy();
            modelData.setDeploymentId(deployment.getId());
            repositoryService.saveModel(modelData);
        } catch (Exception e) {
            log.error("发布modelId:{}模型服务异常", modelId, e);
            throw new BaseException("发布失败，请检查流程图绘制是否正确");
        }
    }

    public void deleteModel(String modelId) {
        Model modelData = repositoryService.getModel(modelId);
        List<Deployment> db = repositoryService.createDeploymentQuery().deploymentKey(modelData.getKey()).list();
        if (null != modelData) {
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processDefinitionKey(modelData.getKey()).singleResult();
            if (null != pi) {
                runtimeService.deleteProcessInstance(pi.getId(), "");
                historyService.deleteHistoricProcessInstance(pi.getId());
            }
            for (Deployment deployment : db) {
                repositoryService.deleteDeployment(deployment.getId(), true);
            }

            repositoryService.deleteModel(modelId);
        }
    }


    /**
     * 启动一个流程，并返回流程实例
     * @param modelKey
     * @param variables
     * @return
     */
    public ProcessInstance startProcessByModelKey(String modelKey, Map<String, Object> variables) {
        Model model = repositoryService.createModelQuery().modelKey(modelKey).singleResult();
        if(model == null) {
            log.error("start process by model key failure, not found model by key: {}", modelKey);
            throw new BaseException("流程模型未找到");
        }

        if(StringUtils.isEmpty(model.getDeploymentId())) {
            throw new BaseException("流程模型尚未发布");
        }

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(model.getDeploymentId()).singleResult();

        if(processDefinition == null) {
            throw new BaseException("流程定义未找到");
        }

        return runtimeService.startProcessInstanceById(processDefinition.getId(), variables);
    }


    /**
     * 判断流程是否已经全部走完
     * @param processInstanceId
     * @return
     */
    public boolean isProcessFinished(String processInstanceId) {
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        return task == null;
    }


    /**
     * 认领任务
     * @param user
     * @param taskId
     */
    public void claimTask(String user, String taskId) {
        taskService.claim(taskId, user);
    }

    /**
     * 获取流程图
     * @param modelId
     * @return
     */
    public String getImage(String modelId) {
        return Image2Base64Util.byteImageToBase64(repositoryService.getModelEditorSourceExtra(modelId));
    }
}
