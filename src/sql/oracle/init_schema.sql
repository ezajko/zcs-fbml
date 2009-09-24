--
-- Module structure registration
--

delete from fnbl_sync_source_type where id='contactSyncSource';
delete from fnbl_sync_source_type where id='calendarSyncSource';
insert into fnbl_sync_source_type(id, description, class, admin_class)
values('contactSyncSource','ContactSyncSource','ru.korusconsulting.connector.funambol.ContactSyncSource','ru.korusconsulting.connector.config.ContactSyncSourceConfigPanel');
insert into fnbl_sync_source_type(id, description, class, admin_class)
values('calendarSyncSource','CalendarSyncSource','ru.korusconsulting.connector.funambol.CalendarSyncSource','ru.korusconsulting.connector.config.ContactSyncSourceConfigPanel');

delete from fnbl_module where id='zimbra';
insert into fnbl_module (id, name, description)
values('zimbra','zimbra','Zimbra Connector powered by korusconsulting.ru');

delete from fnbl_connector where id='zimbra';
insert into fnbl_connector(id, name, description, admin_class)
values('zimbra','FunambolZimbraConnector','Funambol Zimbra Connector','');

delete from fnbl_connector_source_type where connector='zimbra'
insert into fnbl_connector_source_type(connector, sourcetype)
values('zimbra','contactSyncSource');
insert into fnbl_connector_source_type(connector, sourcetype)
values('zimbra','calendarSyncSource');

delete from fnbl_module_connector where module='zimbra' and connector='zimbra';
insert into fnbl_module_connector(module, connector)
values('zimbra','zimbra');