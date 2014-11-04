SET foreign_key_checks = 0;

    drop table if exists ATTRIBUTE;

    drop table if exists ATTRIBUTE_PATH;

    drop table if exists ATTRIBUTE_PATHS_ATTRIBUTES;

    drop table if exists ATTRIBUTE_PATH_INSTANCE;

    drop table if exists CLASS;

    drop table if exists COMPONENT;

    drop table if exists CONFIGURATION;

    drop table if exists CONFIGURATIONS_RESOURCES;

    drop table if exists CONTENT_SCHEMA;

    drop table if exists CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS;

    drop table if exists DATA_MODEL;

    drop table if exists DATA_SCHEMA;

    drop table if exists FILTER;

    drop table if exists FUNCTION;

    drop table if exists INPUT_COMPONENTS_OUTPUT_COMPONENTS;

    drop table if exists MAPPING;

    drop table if exists MAPPINGS_INPUT_ATTRIBUTE_PATHS;

    drop table if exists MAPPING_ATTRIBUTE_PATH_INSTANCE;

    drop table if exists PROJECT;

    drop table if exists PROJECTS_FUNCTIONS;

    drop table if exists PROJECTS_MAPPINGS;

    drop table if exists RESOURCE;

    drop table if exists SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES;

    drop table if exists SCHEMA_ATTRIBUTE_PATH_INSTANCE;

    drop table if exists TRANSFORMATION;

    create table ATTRIBUTE (
        ID bigint not null auto_increment,
        NAME varchar(255),
        URI VARCHAR(255),
        primary key (ID)
    ) ENGINE=InnoDB;

    create table ATTRIBUTE_PATH (
        ID bigint not null auto_increment,
        ATTRIBUTE_PATH VARCHAR(4000),
        primary key (ID)
    ) ENGINE=InnoDB;

    create table ATTRIBUTE_PATHS_ATTRIBUTES (
        ATTRIBUTE_PATH_ID bigint not null,
        ATTRIBUTE_ID bigint not null,
        primary key (ATTRIBUTE_PATH_ID, ATTRIBUTE_ID)
    ) ENGINE=InnoDB;

    create table ATTRIBUTE_PATH_INSTANCE (
        ID bigint not null auto_increment,
        NAME varchar(255),
        ATTRIBUTE_PATH_INSTANCE_TYPE varchar(255),
        ATTRIBUTE_PATH bigint,
        primary key (ID)
    ) ENGINE=InnoDB;

    create table CLASS (
        ID bigint not null auto_increment,
        NAME varchar(255),
        URI VARCHAR(255),
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

    create table CONFIGURATIONS_RESOURCES (
        CONFIGURATION_ID bigint not null,
        RESOURCE_ID bigint not null,
        primary key (CONFIGURATION_ID, RESOURCE_ID)
    ) ENGINE=InnoDB;

    create table CONTENT_SCHEMA (
        ID bigint not null auto_increment,
        NAME varchar(255),
        KEY_ATTRIBUTE_PATHS VARCHAR(4000),
        RECORD_IDENTIFIER_ATTRIBUTE_PATH bigint,
        VALUE_ATTRIBUTE_PATH bigint,
        primary key (ID)
    ) ENGINE=InnoDB;

    create table CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS (
        CONTENT_SCHEMA_ID bigint not null,
        ATTRIBUTE_PATH_ID bigint not null,
        primary key (CONTENT_SCHEMA_ID, ATTRIBUTE_PATH_ID)
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
        SCHEMA_ATTRIBUTE_PATH_INSTANCES VARCHAR(4000),
        CONTENT_SCHEMA bigint,
        RECORD_CLASS bigint,
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

    create table INPUT_COMPONENTS_OUTPUT_COMPONENTS (
        INPUT_COMPONENT_ID bigint not null,
        OUTPUT_COMPONENT_ID bigint not null,
        primary key (INPUT_COMPONENT_ID, OUTPUT_COMPONENT_ID)
    ) ENGINE=InnoDB;

    create table MAPPING (
        ID bigint not null auto_increment,
        NAME varchar(255),
        OUTPUT_ATTRIBUTE_PATH bigint,
        TRANSFORMATION bigint,
        primary key (ID)
    ) ENGINE=InnoDB;

    create table MAPPINGS_INPUT_ATTRIBUTE_PATHS (
        MAPPING_ID bigint not null,
        INPUT_ATTRIBUTE_PATH_ID bigint not null,
        primary key (MAPPING_ID, INPUT_ATTRIBUTE_PATH_ID)
    ) ENGINE=InnoDB;

    create table MAPPING_ATTRIBUTE_PATH_INSTANCE (
        ORDINAL integer,
        ID bigint not null,
        FILTER bigint,
        primary key (ID)
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
        PROJECT_ID bigint not null,
        FUNCTION_ID bigint not null,
        primary key (PROJECT_ID, FUNCTION_ID)
    ) ENGINE=InnoDB;

    create table PROJECTS_MAPPINGS (
        PROJECT_ID bigint not null,
        MAPPING_ID bigint not null,
        primary key (PROJECT_ID, MAPPING_ID)
    ) ENGINE=InnoDB;

    create table RESOURCE (
        ID bigint not null auto_increment,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        attributes VARCHAR(4000),
        TYPE varchar(255),
        primary key (ID)
    ) ENGINE=InnoDB;

    create table SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES (
        SCHEMA_ID bigint not null,
        SCHEMA_ATTRIBUTE_PATH_INSTANCE_ID bigint not null,
        primary key (SCHEMA_ID, SCHEMA_ATTRIBUTE_PATH_INSTANCE_ID)
    ) ENGINE=InnoDB;

    create table SCHEMA_ATTRIBUTE_PATH_INSTANCE (
        ID bigint not null,
        SUB_SCHEMA bigint,
        primary key (ID)
    ) ENGINE=InnoDB;

    create table TRANSFORMATION (
        ID bigint not null,
        primary key (ID)
    ) ENGINE=InnoDB;

    alter table ATTRIBUTE_PATHS_ATTRIBUTES 
        add index FK_fjtf2wiwdr5hoom410d37k76h (ATTRIBUTE_ID), 
        add constraint FK_fjtf2wiwdr5hoom410d37k76h 
        foreign key (ATTRIBUTE_ID) 
        references ATTRIBUTE (ID);

    alter table ATTRIBUTE_PATHS_ATTRIBUTES 
        add index FK_25kn4b1qaajdntkiai3r37y3l (ATTRIBUTE_PATH_ID), 
        add constraint FK_25kn4b1qaajdntkiai3r37y3l 
        foreign key (ATTRIBUTE_PATH_ID) 
        references ATTRIBUTE_PATH (ID);

    alter table ATTRIBUTE_PATH_INSTANCE 
        add index FK_6jt089wmlbkpdlj0x2rcxup8e (ATTRIBUTE_PATH), 
        add constraint FK_6jt089wmlbkpdlj0x2rcxup8e 
        foreign key (ATTRIBUTE_PATH) 
        references ATTRIBUTE_PATH (ID);

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

    alter table CONFIGURATIONS_RESOURCES 
        add index FK_dtfx2ekmub8eo4kgi3yfg0gyx (RESOURCE_ID), 
        add constraint FK_dtfx2ekmub8eo4kgi3yfg0gyx 
        foreign key (RESOURCE_ID) 
        references RESOURCE (ID);

    alter table CONFIGURATIONS_RESOURCES 
        add index FK_1umqe7aqc5k80n1pixjv34vpi (CONFIGURATION_ID), 
        add constraint FK_1umqe7aqc5k80n1pixjv34vpi 
        foreign key (CONFIGURATION_ID) 
        references CONFIGURATION (ID);

    alter table CONTENT_SCHEMA 
        add index FK_8agn2bar1meoko64cxj0f7uhq (RECORD_IDENTIFIER_ATTRIBUTE_PATH), 
        add constraint FK_8agn2bar1meoko64cxj0f7uhq 
        foreign key (RECORD_IDENTIFIER_ATTRIBUTE_PATH) 
        references ATTRIBUTE_PATH (ID);

    alter table CONTENT_SCHEMA 
        add index FK_9l1ily6bu8wklhep4d4t99qmh (VALUE_ATTRIBUTE_PATH), 
        add constraint FK_9l1ily6bu8wklhep4d4t99qmh 
        foreign key (VALUE_ATTRIBUTE_PATH) 
        references ATTRIBUTE_PATH (ID);

    alter table CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS 
        add index FK_4r9rcnjvsc47fkrftfw7dfb7u (ATTRIBUTE_PATH_ID), 
        add constraint FK_4r9rcnjvsc47fkrftfw7dfb7u 
        foreign key (ATTRIBUTE_PATH_ID) 
        references ATTRIBUTE_PATH (ID);

    alter table CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS 
        add index FK_9j4vcly1i6pmvvs7349hj6fin (CONTENT_SCHEMA_ID), 
        add constraint FK_9j4vcly1i6pmvvs7349hj6fin 
        foreign key (CONTENT_SCHEMA_ID) 
        references CONTENT_SCHEMA (ID);

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

    alter table DATA_SCHEMA 
        add index FK_4d0x8vrycw2wftldagmv875vq (CONTENT_SCHEMA), 
        add constraint FK_4d0x8vrycw2wftldagmv875vq 
        foreign key (CONTENT_SCHEMA) 
        references CONTENT_SCHEMA (ID);

    alter table DATA_SCHEMA 
        add index FK_67hdhd4o40jypqxwdcq7tai28 (RECORD_CLASS), 
        add constraint FK_67hdhd4o40jypqxwdcq7tai28 
        foreign key (RECORD_CLASS) 
        references CLASS (ID);

    alter table INPUT_COMPONENTS_OUTPUT_COMPONENTS 
        add index FK_1bye4g7e7ib5wbeg37mmkejnj (OUTPUT_COMPONENT_ID), 
        add constraint FK_1bye4g7e7ib5wbeg37mmkejnj 
        foreign key (OUTPUT_COMPONENT_ID) 
        references COMPONENT (ID);

    alter table INPUT_COMPONENTS_OUTPUT_COMPONENTS 
        add index FK_ew268gg7myf90otusn3nac4i (INPUT_COMPONENT_ID), 
        add constraint FK_ew268gg7myf90otusn3nac4i 
        foreign key (INPUT_COMPONENT_ID) 
        references COMPONENT (ID);

    alter table MAPPING 
        add index FK_h0b70ivxm1byvbabn6ic3slrp (OUTPUT_ATTRIBUTE_PATH), 
        add constraint FK_h0b70ivxm1byvbabn6ic3slrp 
        foreign key (OUTPUT_ATTRIBUTE_PATH) 
        references MAPPING_ATTRIBUTE_PATH_INSTANCE (ID);

    alter table MAPPING 
        add index FK_y31lfpmsdwyyjjubvff2p2tw (TRANSFORMATION), 
        add constraint FK_y31lfpmsdwyyjjubvff2p2tw 
        foreign key (TRANSFORMATION) 
        references COMPONENT (ID);

    alter table MAPPINGS_INPUT_ATTRIBUTE_PATHS 
        add index FK_jv70xguk23noytu5v3ukd4g48 (INPUT_ATTRIBUTE_PATH_ID), 
        add constraint FK_jv70xguk23noytu5v3ukd4g48 
        foreign key (INPUT_ATTRIBUTE_PATH_ID) 
        references MAPPING_ATTRIBUTE_PATH_INSTANCE (ID);

    alter table MAPPINGS_INPUT_ATTRIBUTE_PATHS 
        add index FK_hsj1yogl36hm49mjh76khjkqe (MAPPING_ID), 
        add constraint FK_hsj1yogl36hm49mjh76khjkqe 
        foreign key (MAPPING_ID) 
        references MAPPING (ID);

    alter table MAPPING_ATTRIBUTE_PATH_INSTANCE 
        add index FK_gqbk1yvsk4obdq7awumondc49 (FILTER), 
        add constraint FK_gqbk1yvsk4obdq7awumondc49 
        foreign key (FILTER) 
        references FILTER (ID);

    alter table MAPPING_ATTRIBUTE_PATH_INSTANCE 
        add index FK_numa6haek9lnkkd2caq9nf7sv (ID), 
        add constraint FK_numa6haek9lnkkd2caq9nf7sv 
        foreign key (ID) 
        references ATTRIBUTE_PATH_INSTANCE (ID);

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
        add index FK_6ja5bqjo5suu7p0wa0ac1cxa8 (FUNCTION_ID), 
        add constraint FK_6ja5bqjo5suu7p0wa0ac1cxa8 
        foreign key (FUNCTION_ID) 
        references FUNCTION (ID);

    alter table PROJECTS_FUNCTIONS 
        add index FK_nx7uw5jry35jr2rxhngkypf8 (PROJECT_ID), 
        add constraint FK_nx7uw5jry35jr2rxhngkypf8 
        foreign key (PROJECT_ID) 
        references PROJECT (ID);

    alter table PROJECTS_MAPPINGS 
        add index FK_qhq2xm12uixmdqfaq1y3w8nht (MAPPING_ID), 
        add constraint FK_qhq2xm12uixmdqfaq1y3w8nht 
        foreign key (MAPPING_ID) 
        references MAPPING (ID);

    alter table PROJECTS_MAPPINGS 
        add index FK_8qrhjdabvk1ty4s9wuikcun2h (PROJECT_ID), 
        add constraint FK_8qrhjdabvk1ty4s9wuikcun2h 
        foreign key (PROJECT_ID) 
        references PROJECT (ID);

    alter table SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES 
        add index FK_g2gpcehjy1n6n47h7iujc2rk (SCHEMA_ATTRIBUTE_PATH_INSTANCE_ID), 
        add constraint FK_g2gpcehjy1n6n47h7iujc2rk 
        foreign key (SCHEMA_ATTRIBUTE_PATH_INSTANCE_ID) 
        references SCHEMA_ATTRIBUTE_PATH_INSTANCE (ID);

    alter table SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES 
        add index FK_i35wl7xdvfkks7cekmmm9wb56 (SCHEMA_ID), 
        add constraint FK_i35wl7xdvfkks7cekmmm9wb56 
        foreign key (SCHEMA_ID) 
        references DATA_SCHEMA (ID);

    alter table SCHEMA_ATTRIBUTE_PATH_INSTANCE 
        add index FK_egcon8hcag84y1tpt0klt8m0a (SUB_SCHEMA), 
        add constraint FK_egcon8hcag84y1tpt0klt8m0a 
        foreign key (SUB_SCHEMA) 
        references DATA_SCHEMA (ID);

    alter table SCHEMA_ATTRIBUTE_PATH_INSTANCE 
        add index FK_kdbuybigve4o0epqxkscgkfu7 (ID), 
        add constraint FK_kdbuybigve4o0epqxkscgkfu7 
        foreign key (ID) 
        references ATTRIBUTE_PATH_INSTANCE (ID);

    alter table TRANSFORMATION 
        add index FK_qk4t8c3cucrxqguipv9emdxpm (ID), 
        add constraint FK_qk4t8c3cucrxqguipv9emdxpm 
        foreign key (ID) 
        references FUNCTION (ID);

SET foreign_key_checks = 1;
