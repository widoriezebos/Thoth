-- Create the tables
create table thoth_identities (id bigint not null, identifier varchar(80) not null, primary key (id));
create table thoth_users (id bigint not null, passwordhash varchar(200), emailaddress varchar(80), firstname varchar(80), lastname varchar(80), blockeduntil timestamp, primary key (id));
create table thoth_groups (id bigint not null, primary key (id));
create table thoth_memberships (id bigint not null, grou_id bigint not null, iden_id bigint not null, primary key (id));
create table thoth_permissions (id bigint not null, grou_id bigint not null, permission int not null, primary key (id));

create table thoth_repositories (id bigint not null, name varchar(200), repotype varchar(30) not null, location varchar(400) not null, username varchar(400), password varchar(400), primary key (id));
create table thoth_contexts (id bigint not null, name varchar(200), repo_id bigint not null, branch varchar(400), library varchar(400), refreshinterval int, primary key (id));
create table thoth_version (name varchar(200), version int not null, primary key (name));

create table thoth_sequences (sequence_name varchar(50) not null, next_val bigint not null, primary key (sequence_name));

-- Add unique keys
alter table thoth_identities add constraint uk_thoth_identities unique (identifier);
alter table thoth_repositories add constraint uk_thoth_repositories unique (name);
alter table thoth_contexts add constraint uk_thoth_contexts unique (name);

-- Add foreign keys
alter table thoth_users add constraint fk_thoth_user_iden foreign key (id) references thoth_identities (id);
alter table thoth_groups add constraint fk_thoth_grou_iden foreign key (id) references thoth_identities (id);
alter table thoth_memberships add constraint fk_thoth_memb_grou foreign key (grou_id) references thoth_groups (id);
alter table thoth_memberships add constraint fk_thoth_memb_iden foreign key (iden_id) references thoth_identities (id);
alter table thoth_permissions add constraint fk_thoth_perm_grou foreign key (grou_id) references thoth_groups (id);

alter table thoth_contexts add constraint fk_thoth_cont_repo foreign key (repo_id) references thoth_repositories (id);

-- Create indexes for foreign keys
create index ix_thoth_perm_grou on thoth_permissions (grou_id);
create index ix_thoth_cont_repo on thoth_contexts (repo_id);
create index ix_thoth_memb_iden on thoth_memberships (iden_id);
create index ix_thoth_memb_grou on thoth_memberships (grou_id);

-- Create groups administators, writers, readers
insert into thoth_identities(id, identifier) values (1, 'thoth_administrators');
insert into thoth_groups(id) values (1);
insert into thoth_identities(id, identifier) values (2, 'thoth_writers');
insert into thoth_groups(id) values (2);
insert into thoth_identities(id, identifier) values (3, 'thoth_readers');
insert into thoth_groups(id) values (3);
insert into thoth_identities(id, identifier) values (4, 'thoth_anonymous');
insert into thoth_groups(id) values (4);

-- Create administrator user
insert into thoth_identities(id, identifier) values (10, 'administrator');
insert into thoth_users(id, passwordhash) values (10, 'VjSeugliQN3mnYmSOJ95RAgE2Mz4g8JcRbK6QbeTxSdM+rD20rP5FhUf0OJdgYQ/');

-- Make administrator member of administrators
insert into thoth_memberships(id, grou_id, iden_id) values (1, 1, 10);

-- Grant rights to administrators group
insert into thoth_permissions(id, grou_id, permission) values (1, 1, 1);
insert into thoth_permissions(id, grou_id, permission) values (2, 1, 2);
insert into thoth_permissions(id, grou_id, permission) values (3, 1, 3);
insert into thoth_permissions(id, grou_id, permission) values (4, 1, 4);
insert into thoth_permissions(id, grou_id, permission) values (5, 1, 5);
insert into thoth_permissions(id, grou_id, permission) values (6, 1, 6);
insert into thoth_permissions(id, grou_id, permission) values (7, 1, 7);
insert into thoth_permissions(id, grou_id, permission) values (8, 1, 8);
insert into thoth_permissions(id, grou_id, permission) values (9, 1, 9);
insert into thoth_permissions(id, grou_id, permission) values (10, 1, 10);
insert into thoth_permissions(id, grou_id, permission) values (11, 1, 11);
insert into thoth_permissions(id, grou_id, permission) values (12, 1, 12);
insert into thoth_permissions(id, grou_id, permission) values (13, 1, 13);
insert into thoth_permissions(id, grou_id, permission) values (14, 1, 14);

-- Grant rights to writers group
insert into thoth_permissions(id, grou_id, permission) values (30, 2, 1);
insert into thoth_permissions(id, grou_id, permission) values (31, 2, 2);
insert into thoth_permissions(id, grou_id, permission) values (32, 2, 3);
insert into thoth_permissions(id, grou_id, permission) values (33, 2, 4);
insert into thoth_permissions(id, grou_id, permission) values (34, 2, 5);
insert into thoth_permissions(id, grou_id, permission) values (35, 2, 6);
insert into thoth_permissions(id, grou_id, permission) values (36, 2, 7);
insert into thoth_permissions(id, grou_id, permission) values (37, 2, 10);
insert into thoth_permissions(id, grou_id, permission) values (38, 2, 11);
insert into thoth_permissions(id, grou_id, permission) values (39, 2, 12);

-- Grant rights to readers group
insert into thoth_permissions(id, grou_id, permission) values (50, 3, 1);
insert into thoth_permissions(id, grou_id, permission) values (51, 3, 2);
insert into thoth_permissions(id, grou_id, permission) values (52, 3, 4);

-- Set the sequences to the correct value
insert into thoth_sequences (sequence_name, next_val) values ('thoth_identities', 20);
insert into thoth_sequences (sequence_name, next_val) values ('thoth_memberships', 20);
insert into thoth_sequences (sequence_name, next_val) values ('thoth_permissions', 100);
insert into thoth_sequences (sequence_name, next_val) values ('thoth_repositories', 1);
insert into thoth_sequences (sequence_name, next_val) values ('thoth_contexts', 1);

-- Mark the version of this particular schema
insert into thoth_version(name, version) values('thoth', 1);
