/*
 * Copyright 1997-2010 Day Management AG
 * Barfuesserplatz 6, 4001 Basel, Switzerland
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Day Management AG, ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Day.
 */
/**
 * @class CQ.wcm.FeedImporter
 * @extends CQ.Ext.Viewport
 * The FeedImporter lets the user import RSS or Atom feeds and
 * create pages for each feed entry.
 * @constructor
 * Creates a new FeedImporter.
 * @param {Object} config The config object
 */
CQ.wcm.FeedImporter = function(config) {
    this.constructor.call(this, config);
};

CQ.wcm.FeedImporter = CQ.Ext.extend(CQ.Ext.Viewport, {

    viewConfig : {
        forceFit :true
    },

    constructor : function(config) {
        var myThis = this;
        // this.resourcePath = config.resourcePath;

        // store
        var store = new CQ.Ext.data.Store( {
            proxy :new CQ.Ext.data.HttpProxy( {
                url :"/etc/importers/polling/list.json"
            }),
            autoLoad :true,
            reader :new CQ.Ext.data.JsonReader( {
                root :'rows',
                totalProperty :'results',
                id :'path',
                fields : [ 'path', 'scheme', 'source', 'target', 'interval', 'storedInterval', 'login', 'password' ]
            })
        });
        store.setDefaultSort('path', 'ASC');

        var linkRenderer = function(v) {
            var href = (v.indexOf("/") == 0) ? v + ".html" : v;
            href = CQ.Util.externalize(href);
            var link = '<a target="_blank" href="{0}" style="cursor:pointer">{1}</a>';
            return CQ.Util.patchText(link, [href, v]);
        };

        // column model
        var cm = new CQ.Ext.grid.ColumnModel( [ new CQ.Ext.grid.RowNumberer(), {
            header :CQ.I18n.getMessage("Scheme"),
            dataIndex :'scheme',
            width :60
        }, {
            header :CQ.I18n.getMessage("URL"),
            "renderer": linkRenderer,
            dataIndex :'source',
            width :400
        }, {
            header :CQ.I18n.getMessage("Imported to Path"),
            dataIndex :'target',
            "renderer": linkRenderer,
            width :400
        }, {
            header :CQ.I18n.getMessage("Active Interval in Seconds"),
            dataIndex :'interval',
            width :140
        }, {
            header :CQ.I18n.getMessage("Path of Importer Configuration"),
            dataIndex :'path',
            width :200
        }

        ]);
        cm.defaultSortable = true;

        var removeAction = new CQ.Ext.Action({
            cls:'cq.wcm.FeedImporter.remove',
            text:CQ.I18n.getMessage('Remove'),
            handler: function() {
                CQ.Ext.Msg.show({
                    title:CQ.I18n.getMessage('Delete Configuration?'),
                    msg:CQ.I18n.getMessage('Would you really like to delete the importer configuration?'),
                    buttons:CQ.Ext.Msg.YESNO,
                    icon:CQ.Ext.MessageBox.QUESTION,
                    fn:function(btnId) {
                        if (btnId == 'yes') {
                            var selection = CQ.Ext.getCmp('cq-feedimporter-grid').getSelectionModel().getSelected();
                            var params = {};
                            params[CQ.Sling.STATUS] = CQ.Sling.STATUS_BROWSER;
                            params[CQ.Sling.OPERATION] = CQ.Sling.OPERATION_DELETE;
                            params[CQ.Sling.CHARSET] = "utf-8";
                            CQ.HTTP.post(
                                selection.id,
                                function(options, success, response) {
                                    if (success) {
                                        CQ.Ext.getCmp('cq-feedimporter-grid').store.reload();
                                    }
                                },
                                params,
                                this
                            );
                        }
                    }});
            },
            tooltip: {
                title:CQ.I18n.getMessage('Remove configuration entry'),
                text:CQ.I18n.getMessage('Removes the selected importer configuration'),
                autoHide:true
            }
        });
        removeAction.setDisabled(true);

        var editAction = new CQ.Ext.Action({
            cls:'cq.wcm.FeedImporter.edit',
            text:CQ.I18n.getMessage('Edit'),
            handler: function() {
                var selection = CQ.Ext.getCmp('cq-feedimporter-grid').getSelectionModel().getSelected();
                myThis.newDialog.form.url = selection.data.path;
                myThis.newDialog.form.reset();
                myThis.newDialog.getField("feedType").setValue(selection.data.scheme);
                myThis.newDialog.getField("feedUrl").setValue(selection.data.source);
                myThis.newDialog.getField("target").setValue(selection.data.target);
                myThis.newDialog.getField("interval").setValue(selection.data.interval);
                myThis.newDialog.getField("login").setValue(selection.data.login);
                myThis.newDialog.getField("password").setValue(selection.data.password);
                myThis.newDialog.show();
            },
            tooltip: {
                title:CQ.I18n.getMessage('Edit configuration entry'),
                text:CQ.I18n.getMessage('Edit the selected importer configuration'),
                autoHide:true
            }
        });
        editAction.setDisabled(true);


        this.newDialog = CQ.WCM.getDialog( {
            "jcr:primaryType" :"cq:Dialog",
            "xtype" :"dialog",

            "title" :'New Importer Configuration',
            "params" : {
                "_charset_" :"utf-8"
            },
            "items" : {
                "xtype" :'panel',
                "items" : [ {
                    xtype :'hidden',
                    name :'source'
                },{
                    "xtype":"selection",
                    "type":"select",
                    "name":"feedType",
                    "fieldLabel":CQ.I18n.getMessage("Type"),
                    "defaultValue":"atom",
                    "options":[{
                        "text": "Vacancy",
                        "value": "vacancy"
                    },{
                        "text":"RSS",
                        "value":"rss"
                    },{
                        "text":"Atom",
                        "value":"atom"
                    },{
                        "text":"Calendar",
                        "value":"ics"
                    },{
                        "text":"IMAP",
                        "value":"imap"
                    },{
                        "text":"IMAP (over SSL)",
                        "value":"imaps"
                    },{
                        "text":"POP3",
                        "value":"pop3"
                    },{
                        "text":"POP3 (over SSL)",
                        "value":"pop3s"
                    },{
                        "text":"SiteCatalyst Impressions",
                        "value":"sitecatalyst"
                    },{
                        "text":"SiteCatayst Report",
                        "value":"screport"
                    }],
                    "listeners":{
                        "selectionchanged": {
                            "fn":this.syncSource,
                            "scope":this
                        }
                    }
                },{
                    "xtype" :'textfield',
                    "name" :'feedUrl',
                    "fieldLabel" :CQ.I18n.getMessage("URL"),
                    "listeners":{
                        "change": {
                            "fn":this.syncSource,
                            "scope":this
                        }
                    }
                },{
                    "xtype":"pathfield",
                    "selectOnFocus":true,
                    "name":"target",
                    "fieldLabel":CQ.I18n.getMessage("Import to Path"),
                    "forceSelection":true,
                    "allowBlank": false,
                    "predicate": "nosystem",
                    "showTitlesInTree": false
                }, {
                    xtype :'numberfield',
                    name :'interval',
                    fieldSubLabel: CQ.I18n.getMessage("Optional"),
                    fieldLabel :CQ.I18n.getMessage("Update Interval in Seconds")
                }, {
                    xtype :'textfield',
                    name :'login',
                    fieldSubLabel: CQ.I18n.getMessage("Optional"),
                    fieldLabel :CQ.I18n.getMessage("Login")
                }, {
                    xtype :'password',
                    name :'password',
                    fieldSubLabel: CQ.I18n.getMessage("Optional"),
                    fieldLabel :CQ.I18n.getMessage("Password")
                }, {
                    xtype :'hidden',
                    ignoreData : false,
                    name :'password@Encrypted'
                }, {
                    xtype :'hidden',
                    name :'jcr:mixinTypes',
                    value :'cq:PollConfig'
                }, {
                    xtype :'hidden',
                    name :'hidden',
                    value :'true'
                } ]
            },
            "responseScope" :this,
            "success" : function() {
                CQ.Ext.getCmp('cq-feedimporter-grid').store.reload();
                this.newDialog.form.reset();
            },
            "failure" : function() {
                CQ.Ext.Msg.alert(CQ.I18n.getMessage("Error"), CQ.I18n.getMessage("Could not create feed configuration!"));
            },
            "buttons" :CQ.Dialog.OKCANCEL
        });

        var tb = new CQ.Ext.Toolbar({
            "id" :"cq-feedimporter-toolbar",
            "items" : [{
                "xtype" :"button",
                "text" :CQ.I18n.getMessage("Refresh"),
                "tooltip" :CQ.I18n.getMessage("Updates the list of feed importer configurations"),
                "handler" : function() {
                    CQ.Ext.getCmp('cq-feedimporter-grid').store.reload();
                }
            },{
                "xtype" :"tbseparator"
            },{
                "xtype" :"button",
                "text" :CQ.I18n.getMessage("Add"),
                "handler" : function() {
                    myThis.newDialog.form.url = '/etc/importers/polling/*';
                    myThis.newDialog.show();
                }
            },
                editAction,
                removeAction
            ]
        });

        CQ.wcm.FeedImporter.superclass.constructor.call(this, {
            "id": "cq-feedimporter-wrapper",
            "renderTo": "CQ",
            "layout": "fit",
            "items": [{
                xtype: "panel",
                layout :"border",
                tbar: tb,
                items: [{
                    "xtype" :"grid",
                    "id" :"cq-feedimporter-grid",
                    "region" :"center",
                    "margins" :"5 5 5 5",
                    "pageSize" :25,
                    "loadMask" :true,
                    "stripeRows" :true,
                    "cm" :cm,
                    "store" :store,
                    "sm" : new CQ.Ext.grid.RowSelectionModel({singleSelect:true,
                        listeners: {
                            selectionchange: function(selectionModel) {
                                removeAction.setDisabled(!selectionModel.hasSelection());
                                editAction.setDisabled(!selectionModel.hasSelection());
                            }
                        }
                    })
                }]
            }]
        });
    },

    syncSource: function() {
        var dialog = this.newDialog;
        var type = dialog.getField("feedType");
        var url = dialog.getField("feedUrl");
        var source = dialog.getField("source");
        source.setValue(type.getValue() + ":" + url.getValue());
    },
    initComponent : function() {
        CQ.wcm.FeedImporter.superclass.initComponent.call(this);
    }

});

CQ.Ext.reg("feedimporter", CQ.wcm.FeedImporter);
