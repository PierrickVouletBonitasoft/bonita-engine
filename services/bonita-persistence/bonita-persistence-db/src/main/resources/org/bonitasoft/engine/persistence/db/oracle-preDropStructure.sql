ALTER TABLE breakpoint DROP CONSTRAINT fk_breakpoint_tenantId;
ALTER TABLE processcategorymapping DROP CONSTRAINT fk_procCatMap_tenId;
ALTER TABLE external_identity_mapping DROP CONSTRAINT fk_extIdMap_tenId;
ALTER TABLE event_trigger_instance DROP CONSTRAINT fk_EvtTrig_tenId;
ALTER TABLE datasourceparameter DROP CONSTRAINT fk_DSParam_tenId;
ALTER TABLE ref_biz_data_inst DROP CONSTRAINT fk_ref_biz_data_inst_tenantId;
ALTER TABLE theme DROP CONSTRAINT fk_theme_tenantId;
ALTER TABLE custom_usr_inf_def DROP CONSTRAINT fk_custom_usr_inf_def_tenantId;
ALTER TABLE custom_usr_inf_val DROP CONSTRAINT fk_custom_usr_inf_val_tenantId;

ALTER TABLE arch_document_mapping DROP CONSTRAINT fk_ADocMap_tenId;
ALTER TABLE arch_flownode_instance DROP CONSTRAINT fk_AFln_tenId;
ALTER TABLE arch_process_comment DROP CONSTRAINT fk_AProcCom_tenId;
ALTER TABLE arch_process_instance DROP CONSTRAINT fk_AProc_tenId;
ALTER TABLE arch_transition_instance DROP CONSTRAINT fk_ATrans_tenId;