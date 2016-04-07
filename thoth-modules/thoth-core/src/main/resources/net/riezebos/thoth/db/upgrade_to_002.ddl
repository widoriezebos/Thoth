alter table thoth_comments add column contextname varchar(200);
update thoth_comments set contextname = 'orphaned';
alter table thoth_comments alter column contextname set not null;

alter table thoth_comments alter column documentpath set not null;

create index ix_thoth_comm_ctxt on thoth_comments (contextname);

update thoth_version set version = 2 where name = 'thoth';

