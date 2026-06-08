create table if not exists timetable_import_file_metadata (
                                                              id bigserial primary key,
                                                              school_id varchar(20) not null,
    academic_year varchar(20),
    original_filename varchar(255) not null,
    storage_key varchar(500) not null,
    content_type varchar(150),
    file_size_bytes bigint,
    uploaded_by varchar(100),
    uploaded_at timestamp not null default current_timestamp,
    status varchar(40) not null default 'UPLOADED',
    import_batch_id varchar(50)
    );

create index if not exists idx_timetable_import_file_metadata_school_uploaded
    on timetable_import_file_metadata (school_id, uploaded_at desc);

create index if not exists idx_timetable_import_file_metadata_import_batch
    on timetable_import_file_metadata (import_batch_id);
