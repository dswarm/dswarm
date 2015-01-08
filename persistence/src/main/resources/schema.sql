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
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        URI VARCHAR(255),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table ATTRIBUTE_PATH (
        UUID VARCHAR(160) not null,
        ATTRIBUTE_PATH BLOB,
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table ATTRIBUTE_PATHS_ATTRIBUTES (
        ATTRIBUTE_PATH_UUID VARCHAR(160) not null,
        ATTRIBUTE_UUID VARCHAR(160) not null,
        primary key (ATTRIBUTE_PATH_UUID, ATTRIBUTE_UUID)
    ) ENGINE=InnoDB;

    create table ATTRIBUTE_PATH_INSTANCE (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        ATTRIBUTE_PATH_INSTANCE_TYPE varchar(255),
        ATTRIBUTE_PATH VARCHAR(160),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table CLASS (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        URI VARCHAR(255),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table COMPONENT (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        PARAMETER_MAPPINGS BLOB,
        FUNCTION VARCHAR(160),
        TRANSFORMATION VARCHAR(160),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table CONFIGURATION (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        parameters BLOB,
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table CONFIGURATIONS_RESOURCES (
        CONFIGURATION_UUID VARCHAR(160) not null,
        RESOURCE_UUID VARCHAR(160) not null,
        primary key (CONFIGURATION_UUID, RESOURCE_UUID)
    ) ENGINE=InnoDB;

    create table CONTENT_SCHEMA (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        KEY_ATTRIBUTE_PATHS BLOB,
        RECORD_IDENTIFIER_ATTRIBUTE_PATH VARCHAR(160),
        VALUE_ATTRIBUTE_PATH VARCHAR(160),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS (
        CONTENT_SCHEMA_UUID VARCHAR(160) not null,
        ATTRIBUTE_PATH_UUID VARCHAR(160) not null,
        primary key (CONTENT_SCHEMA_UUID, ATTRIBUTE_PATH_UUID)
    ) ENGINE=InnoDB;

    create table DATA_MODEL (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        CONFIGURATION VARCHAR(160),
        DATA_RESOURCE VARCHAR(160),
        DATA_SCHEMA VARCHAR(160),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table DATA_SCHEMA (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        SCHEMA_ATTRIBUTE_PATH_INSTANCES LONGBLOB,
        CONTENT_SCHEMA VARCHAR(160),
        RECORD_CLASS VARCHAR(160),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table FILTER (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        EXPRESSION BLOB,
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table FUNCTION (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        FUNCTION_DESCRIPTION VARCHAR(4000),
        FUNCTION_TYPE varchar(255),
        PARAMETERS BLOB,
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table INPUT_COMPONENTS_OUTPUT_COMPONENTS (
        INPUT_COMPONENT_UUID VARCHAR(160) not null,
        OUTPUT_COMPONENT_UUID VARCHAR(160) not null,
        primary key (INPUT_COMPONENT_UUID, OUTPUT_COMPONENT_UUID)
    ) ENGINE=InnoDB;

    create table MAPPING (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        OUTPUT_ATTRIBUTE_PATH VARCHAR(160),
        TRANSFORMATION VARCHAR(160),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table MAPPINGS_INPUT_ATTRIBUTE_PATHS (
        MAPPING_UUID VARCHAR(160) not null,
        INPUT_ATTRIBUTE_PATH_UUID VARCHAR(160) not null,
        primary key (MAPPING_UUID, INPUT_ATTRIBUTE_PATH_UUID)
    ) ENGINE=InnoDB;

    create table MAPPING_ATTRIBUTE_PATH_INSTANCE (
        ORDINAL integer,
        UUID VARCHAR(160) not null,
        FILTER VARCHAR(160),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table PROJECT (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        INPUT_DATA_MODEL VARCHAR(160),
        OUTPUT_DATA_MODEL VARCHAR(160),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table PROJECTS_FUNCTIONS (
        PROJECT_UUID VARCHAR(160) not null,
        FUNCTION_UUID VARCHAR(160) not null,
        primary key (PROJECT_UUID, FUNCTION_UUID)
    ) ENGINE=InnoDB;

    create table PROJECTS_MAPPINGS (
        PROJECT_UUID VARCHAR(160) not null,
        MAPPING_UUID VARCHAR(160) not null,
        primary key (PROJECT_UUID, MAPPING_UUID)
    ) ENGINE=InnoDB;

    create table RESOURCE (
        UUID VARCHAR(160) not null,
        NAME varchar(255),
        DESCRIPTION VARCHAR(4000),
        attributes BLOB,
        TYPE varchar(255),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES (
        SCHEMA_UUID VARCHAR(160) not null,
        SCHEMA_ATTRIBUTE_PATH_INSTANCE_UUID VARCHAR(160) not null,
        primary key (SCHEMA_UUID, SCHEMA_ATTRIBUTE_PATH_INSTANCE_UUID)
    ) ENGINE=InnoDB;

    create table SCHEMA_ATTRIBUTE_PATH_INSTANCE (
        UUID VARCHAR(160) not null,
        SUB_SCHEMA VARCHAR(160),
        primary key (UUID)
    ) ENGINE=InnoDB;

    create table TRANSFORMATION (
        UUID VARCHAR(160) not null,
        primary key (UUID)
    ) ENGINE=InnoDB;

    alter table ATTRIBUTE
        add constraint UK_govks6nlmh5r4fx9qexkfqqjd unique (URI);

    alter table CLASS
        add constraint UK_a939ymvsn9fupg3bnearmr7mb unique (URI);

    alter table ATTRIBUTE_PATHS_ATTRIBUTES
        add index FK_fjtf2wiwdr5hoom410d37k76h (ATTRIBUTE_UUID),
        add constraint FK_fjtf2wiwdr5hoom410d37k76h
        foreign key (ATTRIBUTE_UUID)
        references ATTRIBUTE (UUID);

    alter table ATTRIBUTE_PATHS_ATTRIBUTES
        add index FK_25kn4b1qaajdntkiai3r37y3l (ATTRIBUTE_PATH_UUID),
        add constraint FK_25kn4b1qaajdntkiai3r37y3l
        foreign key (ATTRIBUTE_PATH_UUID)
        references ATTRIBUTE_PATH (UUID);

    alter table ATTRIBUTE_PATH_INSTANCE
        add index FK_6jt089wmlbkpdlj0x2rcxup8e (ATTRIBUTE_PATH),
        add constraint FK_6jt089wmlbkpdlj0x2rcxup8e
        foreign key (ATTRIBUTE_PATH)
        references ATTRIBUTE_PATH (UUID);

    alter table COMPONENT
        add index FK_ag5gnetntolrsj5c8x1pk0mbu (FUNCTION),
        add constraint FK_ag5gnetntolrsj5c8x1pk0mbu
        foreign key (FUNCTION)
        references FUNCTION (UUID);

    alter table COMPONENT
        add index FK_g82elj4ech037bcca6vqmufm1 (TRANSFORMATION),
        add constraint FK_g82elj4ech037bcca6vqmufm1
        foreign key (TRANSFORMATION)
        references TRANSFORMATION (UUID);

    alter table CONFIGURATIONS_RESOURCES
        add index FK_dtfx2ekmub8eo4kgi3yfg0gyx (RESOURCE_UUID),
        add constraint FK_dtfx2ekmub8eo4kgi3yfg0gyx
        foreign key (RESOURCE_UUID)
        references RESOURCE (UUID);

    alter table CONFIGURATIONS_RESOURCES
        add index FK_1umqe7aqc5k80n1pixjv34vpi (CONFIGURATION_UUID),
        add constraint FK_1umqe7aqc5k80n1pixjv34vpi
        foreign key (CONFIGURATION_UUID)
        references CONFIGURATION (UUID);

    alter table CONTENT_SCHEMA
        add index FK_8agn2bar1meoko64cxj0f7uhq (RECORD_IDENTIFIER_ATTRIBUTE_PATH),
        add constraint FK_8agn2bar1meoko64cxj0f7uhq
        foreign key (RECORD_IDENTIFIER_ATTRIBUTE_PATH)
        references ATTRIBUTE_PATH (UUID);

    alter table CONTENT_SCHEMA
        add index FK_9l1ily6bu8wklhep4d4t99qmh (VALUE_ATTRIBUTE_PATH),
        add constraint FK_9l1ily6bu8wklhep4d4t99qmh
        foreign key (VALUE_ATTRIBUTE_PATH)
        references ATTRIBUTE_PATH (UUID);

    alter table CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS
        add index FK_4r9rcnjvsc47fkrftfw7dfb7u (ATTRIBUTE_PATH_UUID),
        add constraint FK_4r9rcnjvsc47fkrftfw7dfb7u
        foreign key (ATTRIBUTE_PATH_UUID)
        references ATTRIBUTE_PATH (UUID);

    alter table CONTENT_SCHEMAS_KEY_ATTRIBUTE_PATHS
        add index FK_9j4vcly1i6pmvvs7349hj6fin (CONTENT_SCHEMA_UUID),
        add constraint FK_9j4vcly1i6pmvvs7349hj6fin
        foreign key (CONTENT_SCHEMA_UUID)
        references CONTENT_SCHEMA (UUID);

    alter table DATA_MODEL
        add index FK_hpe71t1t2cy8817cq6jcval7v (CONFIGURATION),
        add constraint FK_hpe71t1t2cy8817cq6jcval7v
        foreign key (CONFIGURATION)
        references CONFIGURATION (UUID);

    alter table DATA_MODEL
        add index FK_ixk5hb4bkl3vhu6agjra7mkr8 (DATA_RESOURCE),
        add constraint FK_ixk5hb4bkl3vhu6agjra7mkr8
        foreign key (DATA_RESOURCE)
        references RESOURCE (UUID);

    alter table DATA_MODEL
        add index FK_id7ig90c37glf3njn0928o0v0 (DATA_SCHEMA),
        add constraint FK_id7ig90c37glf3njn0928o0v0
        foreign key (DATA_SCHEMA)
        references DATA_SCHEMA (UUID);

    alter table DATA_SCHEMA
        add index FK_4d0x8vrycw2wftldagmv875vq (CONTENT_SCHEMA),
        add constraint FK_4d0x8vrycw2wftldagmv875vq
        foreign key (CONTENT_SCHEMA)
        references CONTENT_SCHEMA (UUID);

    alter table DATA_SCHEMA
        add index FK_67hdhd4o40jypqxwdcq7tai28 (RECORD_CLASS),
        add constraint FK_67hdhd4o40jypqxwdcq7tai28
        foreign key (RECORD_CLASS)
        references CLASS (UUID);

    alter table INPUT_COMPONENTS_OUTPUT_COMPONENTS
        add index FK_1bye4g7e7ib5wbeg37mmkejnj (OUTPUT_COMPONENT_UUID),
        add constraint FK_1bye4g7e7ib5wbeg37mmkejnj
        foreign key (OUTPUT_COMPONENT_UUID)
        references COMPONENT (UUID);

    alter table INPUT_COMPONENTS_OUTPUT_COMPONENTS
        add index FK_ew268gg7myf90otusn3nac4i (INPUT_COMPONENT_UUID),
        add constraint FK_ew268gg7myf90otusn3nac4i
        foreign key (INPUT_COMPONENT_UUID)
        references COMPONENT (UUID);

    alter table MAPPING
        add index FK_h0b70ivxm1byvbabn6ic3slrp (OUTPUT_ATTRIBUTE_PATH),
        add constraint FK_h0b70ivxm1byvbabn6ic3slrp
        foreign key (OUTPUT_ATTRIBUTE_PATH)
        references MAPPING_ATTRIBUTE_PATH_INSTANCE (UUID);

    alter table MAPPING
        add index FK_y31lfpmsdwyyjjubvff2p2tw (TRANSFORMATION),
        add constraint FK_y31lfpmsdwyyjjubvff2p2tw
        foreign key (TRANSFORMATION)
        references COMPONENT (UUID);

    alter table MAPPINGS_INPUT_ATTRIBUTE_PATHS
        add index FK_jv70xguk23noytu5v3ukd4g48 (INPUT_ATTRIBUTE_PATH_UUID),
        add constraint FK_jv70xguk23noytu5v3ukd4g48
        foreign key (INPUT_ATTRIBUTE_PATH_UUID)
        references MAPPING_ATTRIBUTE_PATH_INSTANCE (UUID);

    alter table MAPPINGS_INPUT_ATTRIBUTE_PATHS
        add index FK_hsj1yogl36hm49mjh76khjkqe (MAPPING_UUID),
        add constraint FK_hsj1yogl36hm49mjh76khjkqe
        foreign key (MAPPING_UUID)
        references MAPPING (UUID);

    alter table MAPPING_ATTRIBUTE_PATH_INSTANCE
        add index FK_gqbk1yvsk4obdq7awumondc49 (FILTER),
        add constraint FK_gqbk1yvsk4obdq7awumondc49
        foreign key (FILTER)
        references FILTER (UUID);

    alter table MAPPING_ATTRIBUTE_PATH_INSTANCE
        add index FK_numa6haek9lnkkd2caq9nf7sv (UUID),
        add constraint FK_numa6haek9lnkkd2caq9nf7sv
        foreign key (UUID)
        references ATTRIBUTE_PATH_INSTANCE (UUID);

    alter table PROJECT
        add index FK_nswwcscg2guqjctk1omny3loj (INPUT_DATA_MODEL),
        add constraint FK_nswwcscg2guqjctk1omny3loj
        foreign key (INPUT_DATA_MODEL)
        references DATA_MODEL (UUID);

    alter table PROJECT
        add index FK_6unsgihaswor2ftvtcwmwg4nc (OUTPUT_DATA_MODEL),
        add constraint FK_6unsgihaswor2ftvtcwmwg4nc
        foreign key (OUTPUT_DATA_MODEL)
        references DATA_MODEL (UUID);

    alter table PROJECTS_FUNCTIONS
        add index FK_6ja5bqjo5suu7p0wa0ac1cxa8 (FUNCTION_UUID),
        add constraint FK_6ja5bqjo5suu7p0wa0ac1cxa8
        foreign key (FUNCTION_UUID)
        references FUNCTION (UUID);

    alter table PROJECTS_FUNCTIONS
        add index FK_nx7uw5jry35jr2rxhngkypf8 (PROJECT_UUID),
        add constraint FK_nx7uw5jry35jr2rxhngkypf8
        foreign key (PROJECT_UUID)
        references PROJECT (UUID);

    alter table PROJECTS_MAPPINGS
        add index FK_qhq2xm12uixmdqfaq1y3w8nht (MAPPING_UUID),
        add constraint FK_qhq2xm12uixmdqfaq1y3w8nht
        foreign key (MAPPING_UUID)
        references MAPPING (UUID);

    alter table PROJECTS_MAPPINGS
        add index FK_8qrhjdabvk1ty4s9wuikcun2h (PROJECT_UUID),
        add constraint FK_8qrhjdabvk1ty4s9wuikcun2h
        foreign key (PROJECT_UUID)
        references PROJECT (UUID);

    alter table SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES
        add index FK_g2gpcehjy1n6n47h7iujc2rk (SCHEMA_ATTRIBUTE_PATH_INSTANCE_UUID),
        add constraint FK_g2gpcehjy1n6n47h7iujc2rk
        foreign key (SCHEMA_ATTRIBUTE_PATH_INSTANCE_UUID)
        references SCHEMA_ATTRIBUTE_PATH_INSTANCE (UUID);

    alter table SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES
        add index FK_i35wl7xdvfkks7cekmmm9wb56 (SCHEMA_UUID),
        add constraint FK_i35wl7xdvfkks7cekmmm9wb56
        foreign key (SCHEMA_UUID)
        references DATA_SCHEMA (UUID);

    alter table SCHEMA_ATTRIBUTE_PATH_INSTANCE
        add index FK_egcon8hcag84y1tpt0klt8m0a (SUB_SCHEMA),
        add constraint FK_egcon8hcag84y1tpt0klt8m0a
        foreign key (SUB_SCHEMA)
        references DATA_SCHEMA (UUID);

    alter table SCHEMA_ATTRIBUTE_PATH_INSTANCE
        add index FK_kdbuybigve4o0epqxkscgkfu7 (UUID),
        add constraint FK_kdbuybigve4o0epqxkscgkfu7
        foreign key (UUID)
        references ATTRIBUTE_PATH_INSTANCE (UUID);

    alter table TRANSFORMATION
        add index FK_qk4t8c3cucrxqguipv9emdxpm (UUID),
        add constraint FK_qk4t8c3cucrxqguipv9emdxpm
        foreign key (UUID)
        references FUNCTION (UUID);

SET foreign_key_checks = 1;
