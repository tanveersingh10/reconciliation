package com.xion.backend.services;

import com.xion.data.PredicatedReferenceObject;
import com.xion.data.enums.PipelineState;
import com.xion.data.enums.Topics;
import com.xion.data.proSubClasses.DataState;
import com.xion.data.proSubClasses.ProMetaData;
import com.xion.data.proSubClasses.WorkFlowState;
import com.xion.hadesComponents.interfaces.PipelinePersistenceHadesRepository;
import com.xion.models.ap.PipelinePersistence;
import com.xion.resultObjectModel.resultSummeries.TypeMapping;
import com.xion.services.FederationService;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

@Service
public class AthenaService {

    private static Logger logger = Logger.getLogger(AthenaService.class.getName());

    private FederationService federationService;

    private PipelinePersistenceHadesRepository pipelinePersistenceHadesRepository;

    public AthenaService(FederationService federationService, PipelinePersistenceHadesRepository pipelinePersistenceHadesRepository) {
        this.federationService = federationService;
        this.pipelinePersistenceHadesRepository = pipelinePersistenceHadesRepository;
    }

    public void releasePro(PipelinePersistence pipelinePersistence, TypeMapping typeMapping) throws Exception {
        logger.info("releasePro entered for " + typeMapping.getName());

        PipelinePersistence byId = pipelinePersistenceHadesRepository.findById(pipelinePersistence.getPipelinePersistenceId());
        if (!byId.getPipelineState().equals(PipelineState.DQM_PROCESS))
            throw new Exception("File has already left DQM");

        PredicatedReferenceObject pro = new PredicatedReferenceObject();

        ProMetaData metaData = new ProMetaData();
        DataState dataState = new DataState();
        WorkFlowState workFlowState = new WorkFlowState();

        DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        metaData.setProId(pipelinePersistence.getProId());
        metaData.setInitializedDate(sdf.format(pipelinePersistence.getUploadDate()));

        dataState.setDocumentId(typeMapping.getId());
        dataState.setCompanyId(typeMapping.getCompanyId());
        dataState.setFileName(typeMapping.getName());
        dataState.setMisc(new HashMap<>());

        workFlowState.setCurrentState(PipelineState.POST_PROCESS);
        workFlowState.setEdits(new ArrayList<>());

        pro.setProMetaData(metaData);
        pro.setDataState(dataState);
        pro.setWorkFlowState(workFlowState);

        federationService.publishPro(Topics.RULES, pro);
    }

}
