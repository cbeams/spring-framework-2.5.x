<#--
   test template for FreeMarker macro test class
-->
<#import "spring.ftl" as spring />

NAME
${command.name}

AGE
${command.age}

MESSAGE
<@spring.message "hello"/> <@spring.message "world"/>

DEFAULTMESSAGE
<@spring.messageText "no.such.code", "hi"/> <@spring.messageText "no.such.code", "planet"/>

URL
<@spring.url "/aftercontext.html"/>

FORM1
<@spring.formInput "command.name", ""/>

FORM2
<@spring.formInput "command.name", 'class="myCssClass"'/>

FORM3
<@spring.formTextarea "command.name", ""/>

FORM4
<@spring.formTextarea "command.name", "rows=10 cols=30"/>

FORM5
<@spring.formSingleSelect "command.name", nameOptionMap, ""/>

<#-- WHY DOES THIS FAIL TO PARSE??
FORM6
<@spring.formMultiSelect "command.name", nameOptionMap, ""/>
-->

FORM7
<@spring.formRadioButtons "command.name", nameOptionMap, " ", ""/>

<#-- WHY DOES THIS FAIL TO PARSE??
FORM8
<@spring.formCheckboxes "command.name", nameOptionMap, " ", ""/>
-->

FORM9
<@spring.formPasswordInput "command.name", ""/>

FORM10
<@spring.formHiddenInput "command.name", ""/>

FORM11
<@spring.formInput "command.name", "", "text"/>

FORM12
<@spring.formInput "command.name", "", "hidden"/>

FORM13
<@spring.formInput "command.name", "", "password"/>
