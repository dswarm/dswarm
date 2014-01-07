
    alter table ATTRIBUTES_ATTRIBUTE_PATHS 
        drop
        foreign key FK_qp5h79t2qm3x9dt0bvab84nwq;

    alter table ATTRIBUTE_PATHS_SCHEMAS
        drop
        foreign key FK_5b237a3jiliubkbk3b4w625r4;

    alter table ATTRIBUTE_PATHS_SCHEMAS
        drop
        foreign key FK_c5gir0ri2spql32cyff3tttst;

    alter table COMPONENT
        drop
        foreign key FK_ag5gnetntolrsj5c8x1pk0mbu;

    alter table COMPONENT
        drop
        foreign key FK_g82elj4ech037bcca6vqmufm1;

    alter table DATA_MODEL
        drop
        foreign key FK_hpe71t1t2cy8817cq6jcval7v;

    alter table DATA_MODEL
        drop
        foreign key FK_ixk5hb4bkl3vhu6agjra7mkr8;

    alter table DATA_MODEL
        drop
        foreign key FK_id7ig90c37glf3njn0928o0v0;

    alter table INPUT_ATTRIBUTE_PATHS_MAPPINGS
        drop
        foreign key FK_6c4ofueeyb3hbmv2wboyqw7n1;

    alter table INPUT_ATTRIBUTE_PATHS_MAPPINGS
        drop
        foreign key FK_e46064nsbvmdxm74jg1yhlouk;

    alter table MAPPING
        drop
        foreign key FK_1fdjucib6chk55t2evxrw6ugk;

    alter table MAPPING
        drop
        foreign key FK_h0b70ivxm1byvbabn6ic3slrp;

    alter table MAPPING
        drop
        foreign key FK_shvy8f39jmeyjgne94ntkyysn;

    alter table MAPPING
        drop
        foreign key FK_y31lfpmsdwyyjjubvff2p2tw;

    alter table OUTPUT_COMPONENTS_INPUT_COMPONENTS
        drop
        foreign key FK_f27jrrca3kaj4k5gkph7cmfhb;

    alter table OUTPUT_COMPONENTS_INPUT_COMPONENTS
        drop
        foreign key FK_evs3264hua8pae1hgl2g4fa4x;

    alter table PROJECT
        drop
        foreign key FK_nswwcscg2guqjctk1omny3loj;

    alter table PROJECT
        drop
        foreign key FK_6unsgihaswor2ftvtcwmwg4nc;

    alter table PROJECTS_FUNCTIONS
        drop
        foreign key FK_nx7uw5jry35jr2rxhngkypf8;

    alter table PROJECTS_FUNCTIONS
        drop
        foreign key FK_6ja5bqjo5suu7p0wa0ac1cxa8;

    alter table PROJECTS_MAPPINGS
        drop
        foreign key FK_8qrhjdabvk1ty4s9wuikcun2h;

    alter table PROJECTS_MAPPINGS
        drop
        foreign key FK_qhq2xm12uixmdqfaq1y3w8nht;

    alter table RESOURCES_CONFIGURATIONS
        drop
        foreign key FK_317homsxkat6e9lcmhs056nid;

    alter table RESOURCES_CONFIGURATIONS
        drop
        foreign key FK_ba7nn2952k54vm2rbd2k5gd42;

    alter table TRANSFORMATION
        drop
        foreign key FK_qk4t8c3cucrxqguipv9emdxpm;

    drop table if exists ATTRIBUTE;

    drop table if exists ATTRIBUTES_ATTRIBUTE_PATHS;

    drop table if exists ATTRIBUTE_PATH;

    drop table if exists ATTRIBUTE_PATHS_SCHEMAS;

    drop table if exists CLASS;

    drop table if exists COMPONENT;

    drop table if exists CONFIGURATION;

    drop table if exists DATA_MODEL;

    drop table if exists DATA_SCHEMA;

    drop table if exists FILTER;

    drop table if exists FUNCTION;

    drop table if exists INPUT_ATTRIBUTE_PATHS_MAPPINGS;

    drop table if exists MAPPING;

    drop table if exists OUTPUT_COMPONENTS_INPUT_COMPONENTS;

    drop table if exists PROJECT;

    drop table if exists PROJECTS_FUNCTIONS;

    drop table if exists PROJECTS_MAPPINGS;

    drop table if exists RESOURCE;

    drop table if exists RESOURCES_CONFIGURATIONS;

    drop table if exists TRANSFORMATION;

    create table ATTRIBUTE (
        ID VARCHAR(100) not null,
        NAME varchar(255),
        primary key (ID)
    ) ENGINE=InnoDB;

    create table ATTRIBUTES_ATTRIBUTE_PATHS (
        ATTRIBUTE_PATH_ID bigint not null,
        ATTRIBUTE_ID VARCHAR(100) not null,
        primary key (ATTRIBUTE_PATH_ID, ATTRIBUTE_ID)
    ) ENGINE=InnoDB;

    create table ATTRIBUTE_PATH (
        ID bigint not null auto_increment,
        ATTRIBUTE_PATH VARCHAR(4000),
        primary key (ID)
    ) ENGINE=InnoDB;

    create table ATTRIBUTE_PATHS_SCHEMAS (
        SCHEMA_ID bigint not null,
        ATTRIBUTE_PATH_ID bigint not null,
        primary key (SCHEMA_ID, ATTRIBUTE_PATH_ID)
    ) ENGINE=InnoDB;

    create table CLASS (
        ID VARCHAR(100) not null,
        NAME varchar(255),
        primary key (ID)
    ) ENGINE=InnoDB;

    create table COMPONENT (
        ID bigint not null auto_increment,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        PARAMETER_MAPPINGS VARCHAR(4000),
        FUNCTION bigint,
        TRANSFORMATION bigint,
        primary key (ID)
    ) ENGINE=InnoDB;

    create table CONFIGURATION (
        ID bigint not null auto_increment,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        parameters VARCHAR(4000),
        primary key (ID)
    ) ENGINE=InnoDB;

    create table DATA_MODEL (
        ID bigint not null auto_increment,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        CONFIGURATION bigint,
        DATA_RESOURCE bigint,
        DATA_SCHEMA bigint,
        primary key (ID)
    ) ENGINE=InnoDB;

    create table DATA_SCHEMA (
        ID bigint not null auto_increment,
        NAME varchar(255),
        RECORD_CLASS VARCHAR(100),
        primary key (ID)
    ) ENGINE=InnoDB;

    create table FILTER (
        ID bigint not null auto_increment,
        NAME varchar(255),
        EXPRESSION VARCHAR(4000),
        primary key (ID)
    ) ENGINE=InnoDB;

    create table FUNCTION (
        ID bigint not null auto_increment,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        FUNCTION_DESCRIPTION VARCHAR(4000),
        FUNCTION_TYPE varchar(255),
        PARAMETERS VARCHAR(4000),
        primary key (ID)
    ) ENGINE=InnoDB;

    create table INPUT_ATTRIBUTE_PATHS_MAPPINGS (
        MAPPING_ID bigint not null,
        INPUT_ATTRIBUTE_PATH_ID bigint not null,
        primary key (MAPPING_ID, INPUT_ATTRIBUTE_PATH_ID)
    ) ENGINE=InnoDB;

    create table MAPPING (
        ID bigint not null auto_increment,
        NAME varchar(255),
        INPUT_FILTER bigint,
        OUTPUT_ATTRIBUTE_PATH bigint,
        OUTPUT_FILTER bigint,
        TRANSFORMATION bigint,
        primary key (ID)
    ) ENGINE=InnoDB;

    create table OUTPUT_COMPONENTS_INPUT_COMPONENTS (
        INPUT_COMPONENT_ID bigint not null,
        OUTPUT_COMPONENT_ID bigint not null,
        primary key (INPUT_COMPONENT_ID, OUTPUT_COMPONENT_ID)
    ) ENGINE=InnoDB;

    create table PROJECT (
        ID bigint not null auto_increment,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        INPUT_DATA_MODEL bigint,
        OUTPUT_DATA_MODEL bigint,
        primary key (ID)
    ) ENGINE=InnoDB;

    create table PROJECTS_FUNCTIONS (
        FUNCTION_ID bigint not null,
        PROJECT_ID bigint not null,
        primary key (FUNCTION_ID, PROJECT_ID)
    ) ENGINE=InnoDB;

    create table PROJECTS_MAPPINGS (
        MAPPING_ID bigint not null,
        PROJECT_ID bigint not null,
        primary key (MAPPING_ID, PROJECT_ID)
    ) ENGINE=InnoDB;

    create table RESOURCE (
        ID bigint not null auto_increment,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        attributes VARCHAR(4000),
        TYPE varchar(255),
        primary key (ID)
    ) ENGINE=InnoDB;

    create table RESOURCES_CONFIGURATIONS (
        CONFIGURATION_ID bigint not null,
        RESOURCE_ID bigint not null,
        primary key (CONFIGURATION_ID, RESOURCE_ID)
    ) ENGINE=InnoDB;

    create table TRANSFORMATION (
        ID bigint not null,
        primary key (ID)
    ) ENGINE=InnoDB;

    alter table ATTRIBUTES_ATTRIBUTE_PATHS 
        add index FK_c0tpxqimg4bfkv8j62bcxkxyc (ATTRIBUTE_ID);

    alter table ATTRIBUTES_ATTRIBUTE_PATHS 
        add index FK_qp5h79t2qm3x9dt0bvab84nwq (ATTRIBUTE_PATH_ID), 
        add constraint FK_qp5h79t2qm3x9dt0bvab84nwq 
        foreign key (ATTRIBUTE_PATH_ID) 
        references ATTRIBUTE_PATH (ID);

    alter table ATTRIBUTE_PATHS_SCHEMAS 
        add index FK_5b237a3jiliubkbk3b4w625r4 (ATTRIBUTE_PATH_ID), 
        add constraint FK_5b237a3jiliubkbk3b4w625r4 
        foreign key (ATTRIBUTE_PATH_ID) 
        references ATTRIBUTE_PATH (ID);

    alter table ATTRIBUTE_PATHS_SCHEMAS 
        add index FK_c5gir0ri2spql32cyff3tttst (SCHEMA_ID), 
        add constraint FK_c5gir0ri2spql32cyff3tttst 
        foreign key (SCHEMA_ID) 
        references DATA_SCHEMA (ID);

    alter table COMPONENT 
        add index FK_ag5gnetntolrsj5c8x1pk0mbu (FUNCTION), 
        add constraint FK_ag5gnetntolrsj5c8x1pk0mbu 
        foreign key (FUNCTION) 
        references FUNCTION (ID);

    alter table COMPONENT 
        add index FK_g82elj4ech037bcca6vqmufm1 (TRANSFORMATION), 
        add constraint FK_g82elj4ech037bcca6vqmufm1 
        foreign key (TRANSFORMATION) 
        references TRANSFORMATION (ID);

    alter table DATA_MODEL 
        add index FK_hpe71t1t2cy8817cq6jcval7v (CONFIGURATION), 
        add constraint FK_hpe71t1t2cy8817cq6jcval7v 
        foreign key (CONFIGURATION) 
        references CONFIGURATION (ID);

    alter table DATA_MODEL 
        add index FK_ixk5hb4bkl3vhu6agjra7mkr8 (DATA_RESOURCE), 
        add constraint FK_ixk5hb4bkl3vhu6agjra7mkr8 
        foreign key (DATA_RESOURCE) 
        references RESOURCE (ID);

    alter table DATA_MODEL 
        add index FK_id7ig90c37glf3njn0928o0v0 (DATA_SCHEMA), 
        add constraint FK_id7ig90c37glf3njn0928o0v0 
        foreign key (DATA_SCHEMA) 
        references DATA_SCHEMA (ID);

    alter table INPUT_ATTRIBUTE_PATHS_MAPPINGS 
        add index FK_6c4ofueeyb3hbmv2wboyqw7n1 (INPUT_ATTRIBUTE_PATH_ID), 
        add constraint FK_6c4ofueeyb3hbmv2wboyqw7n1 
        foreign key (INPUT_ATTRIBUTE_PATH_ID) 
        references ATTRIBUTE_PATH (ID);

    alter table INPUT_ATTRIBUTE_PATHS_MAPPINGS 
        add index FK_e46064nsbvmdxm74jg1yhlouk (MAPPING_ID), 
        add constraint FK_e46064nsbvmdxm74jg1yhlouk 
        foreign key (MAPPING_ID) 
        references MAPPING (ID);

    alter table MAPPING 
        add index FK_1fdjucib6chk55t2evxrw6ugk (INPUT_FILTER), 
        add constraint FK_1fdjucib6chk55t2evxrw6ugk 
        foreign key (INPUT_FILTER) 
        references FILTER (ID);

    alter table MAPPING 
        add index FK_h0b70ivxm1byvbabn6ic3slrp (OUTPUT_ATTRIBUTE_PATH), 
        add constraint FK_h0b70ivxm1byvbabn6ic3slrp 
        foreign key (OUTPUT_ATTRIBUTE_PATH) 
        references ATTRIBUTE_PATH (ID);

    alter table MAPPING 
        add index FK_shvy8f39jmeyjgne94ntkyysn (OUTPUT_FILTER), 
        add constraint FK_shvy8f39jmeyjgne94ntkyysn 
        foreign key (OUTPUT_FILTER) 
        references FILTER (ID);

    alter table MAPPING 
        add index FK_y31lfpmsdwyyjjubvff2p2tw (TRANSFORMATION), 
        add constraint FK_y31lfpmsdwyyjjubvff2p2tw 
        foreign key (TRANSFORMATION) 
        references COMPONENT (ID);

    alter table OUTPUT_COMPONENTS_INPUT_COMPONENTS 
        add index FK_f27jrrca3kaj4k5gkph7cmfhb (OUTPUT_COMPONENT_ID), 
        add constraint FK_f27jrrca3kaj4k5gkph7cmfhb 
        foreign key (OUTPUT_COMPONENT_ID) 
        references COMPONENT (ID);

    alter table OUTPUT_COMPONENTS_INPUT_COMPONENTS 
        add index FK_evs3264hua8pae1hgl2g4fa4x (INPUT_COMPONENT_ID), 
        add constraint FK_evs3264hua8pae1hgl2g4fa4x 
        foreign key (INPUT_COMPONENT_ID) 
        references COMPONENT (ID);

    alter table PROJECT 
        add index FK_nswwcscg2guqjctk1omny3loj (INPUT_DATA_MODEL), 
        add constraint FK_nswwcscg2guqjctk1omny3loj 
        foreign key (INPUT_DATA_MODEL) 
        references DATA_MODEL (ID);

    alter table PROJECT 
        add index FK_6unsgihaswor2ftvtcwmwg4nc (OUTPUT_DATA_MODEL), 
        add constraint FK_6unsgihaswor2ftvtcwmwg4nc 
        foreign key (OUTPUT_DATA_MODEL) 
        references DATA_MODEL (ID);

    alter table PROJECTS_FUNCTIONS 
        add index FK_nx7uw5jry35jr2rxhngkypf8 (PROJECT_ID), 
        add constraint FK_nx7uw5jry35jr2rxhngkypf8 
        foreign key (PROJECT_ID) 
        references FUNCTION (ID);

    alter table PROJECTS_FUNCTIONS 
        add index FK_6ja5bqjo5suu7p0wa0ac1cxa8 (FUNCTION_ID), 
        add constraint FK_6ja5bqjo5suu7p0wa0ac1cxa8 
        foreign key (FUNCTION_ID) 
        references PROJECT (ID);

    alter table PROJECTS_MAPPINGS 
        add index FK_8qrhjdabvk1ty4s9wuikcun2h (PROJECT_ID), 
        add constraint FK_8qrhjdabvk1ty4s9wuikcun2h 
        foreign key (PROJECT_ID) 
        references MAPPING (ID);

    alter table PROJECTS_MAPPINGS 
        add index FK_qhq2xm12uixmdqfaq1y3w8nht (MAPPING_ID), 
        add constraint FK_qhq2xm12uixmdqfaq1y3w8nht 
        foreign key (MAPPING_ID) 
        references PROJECT (ID);

    alter table RESOURCES_CONFIGURATIONS 
        add index FK_317homsxkat6e9lcmhs056nid (RESOURCE_ID), 
        add constraint FK_317homsxkat6e9lcmhs056nid 
        foreign key (RESOURCE_ID) 
        references RESOURCE (ID);

    alter table RESOURCES_CONFIGURATIONS 
        add index FK_ba7nn2952k54vm2rbd2k5gd42 (CONFIGURATION_ID), 
        add constraint FK_ba7nn2952k54vm2rbd2k5gd42 
        foreign key (CONFIGURATION_ID) 
        references CONFIGURATION (ID);

    alter table TRANSFORMATION 
        add index FK_qk4t8c3cucrxqguipv9emdxpm (ID), 
        add constraint FK_qk4t8c3cucrxqguipv9emdxpm 
        foreign key (ID) 
        references FUNCTION (ID);
