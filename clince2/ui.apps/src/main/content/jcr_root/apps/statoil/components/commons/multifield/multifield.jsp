<%--
	This custom jsp is responsoble for making multifield work in AEM 6.3
    OOTB multifield has few issue in rendering the data. This issues might be fixed in future versions of AEM.
    Custom multifield functionality can be safely migrated for all the components once it is fixed and this file can be deleted.
--%>
<%@ page import="com.adobe.granite.ui.components.Config" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.adobe.granite.ui.components.Value" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@include file="/libs/granite/ui/global.jsp" %>

<%--include ootb multifield--%>
<sling:include resourceType="/libs/granite/ui/components/coral/foundation/form/multifield"/>

<%!
    private final Logger mLog = LoggerFactory.getLogger(this.getClass());
%>

<%
    Config mCfg = cmp.getConfig();
    Resource mField = mCfg.getChild("field");
    if (mField == null) {
        mLog.warn("Field node doesn't exist");
        return;
    }
    ValueMap mVM = mField.adaptTo(ValueMap.class);
    String mName = mVM.get("name", "");
    if ("".equals(mName)) {
        mLog.warn("name property doesn't exist on field node");
        return;
    }
    Value mValue = ((ComponentHelper) cmp).getValue();
    //get the values added in multifield
    String[] mItems = mValue.get(mName, String[].class);
%>

<script>
    (function () {
        //function to add values into multifield widgets. The values are stored in CRX by collectDataFromFields() as json
        //eg. {"page":"English","path":"/content/geometrixx/en"}
        var addDataInFields = function () {
            var mValues = [ <%= StringUtils.join(mValue.get(mName, String[].class), ",") %> ],
                    mName = '<%=mName%>',
                    $fieldSets = $("[class='coral-Form-field coral-Multifield'][data-granite-coral-multifield-name='" + mName + "']").find("[class='coral-Multifield-item']");
            var record, $fields, $field, name, $tags, tagPropertyName, tagPropertyValue, tagUI, tagULSt, tagPropertyValueIterator, tagPropertyTitle;
            $fieldSets.each(function (i, fieldSet) {
				 
                record = mValues[i];
                if (!record) {
                    return;
                }            	
				
                $fields = $(fieldSet).find("[name]");
                $fields.each(function (j, field) {
                    $field = $(field);
                    name = $field.attr("name");
                    if (!name) {
                        return;
                    }
                    //strip ./
                    if (name.indexOf("./") == 0) {
                        name = name.substring(2);
                    }
                    $field.val(record[name]);
                });
              //adding code to handle tagpicker           	
			 $tags = $(fieldSet).find(".cq-ui-tagfield").find(".coral-TagList");
			 tagPropertyName= $tags.attr("name");
			 if(!tagPropertyName || $tags.length == 0){
				 return;
			 }
			 tagUI = $('<div>').append($tags.find('.coral-Tag').clone()).html();
             if (tagPropertyName.indexOf("./") == 0) {
                 tagPropertyName = tagPropertyName.substring(2);
		      }
			 tagPropertyValue = record[tagPropertyName];
			 if(tagPropertyValue){
                 tagPropertyValueIterator = tagPropertyValue.toString().split(',')
				 for (var i = 0; i < tagPropertyValueIterator.length; i++) {						
                     tagPropertyTitle = tagPropertyValueIterator[i];
                     tagULSt = tagUI.replace(new RegExp(tagPropertyValue.toString(), 'g'), tagPropertyTitle)
                     $tags.find('.coral-Tag:last').after(tagULSt);
                 }
                 $tags.find('.coral-Tag:first').remove();
	          } 
           });
        };
        //collect data from widgets in multifield and POST them to CRX as JSON
        var collectDataFromFields = function(){
        	
        	// Unbind click function so multiple submits doesnt occue
           // $(document).off("click", ".cq-dialog-submit");
            
            $(document).on("click", ".cq-dialog-submit", function () {
            	
                var $form = $(this).closest("form.foundation-form"), mName = '<%=mName%>';
                //get all the input fields of multifield
                var $fieldSets = $("[class='coral-Form-field coral-Multifield'][data-granite-coral-multifield-name='" + mName + "']").find("[class='coral-Multifield-item']");
                
                var $tags, tagPropertyName, $tagULSt, tagList, $checkbox, checkboxFieldValue, checkboxField;
                $fieldSets.each(function (i, fieldSet) {   				 
                    
                	//adding code to handle tagpicker
                    	$tags = $(fieldSet).find(".cq-ui-tagfield").find(".coral-TagList");    				
    					tagList='';
    	                tagPropertyName= $tags.attr("name");
    					$tagULSt = $tags.find("input[name='"+tagPropertyName+"']");
    					$tagULSt.each(function (j, field) {
        	                $field = $(field);
                            if(tagList.length== 0){
                                tagList = '["'+$field.val()+'"';
                            }else{
								tagList = tagList + ',"' + $field.val() +'"';
                            }
           				});
                        if(tagList.length> 0){
                        tagList = tagList+ ']';
                        }
                        $tagULSt.each(function (j, field) {
        	                $field = $(field);        	                
                            $field.val(tagList);
                            $field.parent('.coral-Tag.coral-Tag--large').val(tagList);
           				});
       				
    				$checkbox = $(fieldSet).find(".coral-Checkbox");
    				$checkbox.each(function (j, field) {
    	                $field = $(field);
    	                checkboxFieldValue= $field.attr('value');	
    	                //console.log("Index == "+ j +" Value == "+checkboxFieldValue)
    					checkboxField = $field.find(".coral-Checkbox-input");    
    					if(checkboxField.is(':checked')){
    						checkboxField.val('['+ checkboxFieldValue +']');
    					}else{
    						checkboxField.val('[]');
    					}                                         
                	});
                });

                var record, $fields, $field, name;
                var jsonRecords = $fieldSets
                    .map(function (i, fieldSet) {
                        $fields = $(fieldSet).find("[name],[mf-name]");
                        record = {};
                        $fields.each(function (j, field) {
                            $field = $(field);
                            name = $field.attr("name") || $field.attr("mf-name");
                            if (!name) {
                                return;
                            } else {
                                //remove the field name, so that individual values are not POSTed
                                $field.attr("mf-name", name);
                                $field.removeAttr("name");
                            }
                            //strip ./
                            if (name.indexOf("./") == 0) {
                                name = name.substring(2);
                            }

                            if($field.val().substring(0, 1)=='['){
                               record[name] = JSON.parse($field.val());
                            }else{
                               record[name] = $field.val();
                            }
                        });
                        if ($.isEmptyObject(record)) {
                            return;
                        }
                        return record;
                    })
                    .get();
                //add JSON records each into their own hidden input field
                //delete them first in case this process ran before and validation failed
                $("input[name='"+mName+"']").remove();
                $.each(jsonRecords, function(i, record) {
                    $('<input />').attr('type', 'hidden')
                                  .attr('name', mName)
                    .attr('value', JSON.stringify(record))
                                  .appendTo($form);
                });
                
            });
            
           
        };
        $(document).ready(function () {
            addDataInFields();
            collectDataFromFields();
        });
    })();
</script>
