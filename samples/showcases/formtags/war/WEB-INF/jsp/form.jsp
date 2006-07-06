<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
  <head>
    <title>Spring 2.0 form tag showcase application</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <link rel="icon" href="<c:url value="/favicon.ico"/>" type="image/x-icon" />
    <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>" type="image/x-icon" />

    <link href="<c:url value="/css/style.css"/>" rel="stylesheet" type="text/css"/>

  </head>

<body>
  <div id="main">

    <div id="topmenu">
      <a href="http://www.interface21.com">Interface21</a><a href="http://www.springframework.org">Spring home</a><a href="http://forum.springframework.org">Spring forum</a><a href="http://www.dzone.com">Dzone.com</a><a href="http://www.infoq.com">Infoq.com</a>
    </div>

    <div id="header">
      <span class="title">spring<span class="darktitle">2.0</span> form tags</span><br/>
    </div>


    <div id="content">
        <h1>Edit apprentice magician <c:out value="${command.firstName}"/> <c:out value="${command.lastName}" /></h1>
       <form:form>
           <form:errors path="*" cssClass="errorBox" />

           <div class="first">
               <label>First Name:</label>
               <form:input path="firstName" />
               <form:errors path="firstName" cssClass="error" />
           </div>

           <div>
                <label>Last Name:</label>
                <form:input path="lastName" />
                <form:errors path="lastName" cssClass="error"  />
           </div>

           <div>
               <label>Country:</label>

               <form:select path="country">
                   <form:option value="" label="--Please Select"/>
                   <form:options items="${countryList}" itemValue="code" itemLabel="name"/>
               </form:select>
               <form:errors path="country" cssClass="error"/>
           </div>

           <div>
                <label>Skills:</label>
                <form:select path="skills" items="${skills}"/>
           </div>

           <div>
                <label>Notes:</label>
                <form:textarea path="notes" rows="3" cols="20" />
                <form:errors path="notes" cssClass="error" />
           </div>

           <div>
                <label>Sex:</label>
                    Male: <form:radiobutton path="sex" value="M"/>
                    Female: <form:radiobutton path="sex" value="F"/>
           </div>

           <div>
               <label>House:</label>

               <form:select path="house">
                   <form:option value="Gryffindor"/>
                   <form:option value="Hufflepuff"/>
                   <form:option value="Ravenclaw"/>
                   <form:option value="Slytherin"/>
               </form:select>
           </div>

           <div>
                <label>Subscribe to newsletter?:</label>
                <form:checkbox path="preferences.receiveNewsletter"/>
           </div>

           <div>
                <label>Interests:</label>
               <span>Quidditch:</span> <form:checkbox path="preferences.interests" value="Quidditch"/><br/>
               <span>Herbology:</span> <form:checkbox path="preferences.interests" value="Herbology"/><br/>
               <span>Defence Against the Dark Arts:</span> <form:checkbox path="preferences.interests" value="Defence Against the Dark Arts"/>
           </div>

           <div>
                <label>Favourite Word:</label>
                <span>Magic:</span> <form:checkbox path="preferences.favouriteWord" value="Magic"/>
           </div>

           <div>
                <label>Favourite Colour:</label>
               <form:select path="favouriteColour">
                   <form:option value="0" label="RED"/>
                   <form:option value="1" label="GREEN"/>
                   <form:option value="2" label="BLUE"/>
               </form:select>
           </div>

           <div>
                <label>Password:</label>
               <form:password path="password" />
           </div>

           <div>
               <input type="submit" value="Save Changes" />
           </div>
       </form:form>

       <br/>

       <h1>The JSP form tags:</h1>
       <textarea rows="10" cols="55" class="showjspcode">
&lt;form:form&gt;
   &lt;form:errors path="*" cssClass="errorBox" /&gt;

   &lt;div class="first"&gt;
       &lt;label&gt;First Name:&lt;/label&gt;
       &lt;form:input path="firstName" /&gt;
       &lt;form:errors path="firstName" cssClass="error" /&gt;
   &lt;/div&gt;

   &lt;div&gt;
        &lt;label&gt;Last Name:&lt;/label&gt;
        &lt;form:input path="lastName" /&gt;
        &lt;form:errors path="lastName" cssClass="error"  /&gt;
   &lt;/div&gt;

   &lt;div&gt;
       &lt;label&gt;Country:&lt;/label&gt;

       &lt;form:select path="country"&gt;
           &lt;form:option value="" label="--Please Select"/&gt;
           &lt;form:options items="&#36;{countryList}" itemValue="code" itemLabel="name"/&gt;
       &lt;/form:select&gt;
       &lt;form:errors path="country" cssClass="error"/&gt;
   &lt;/div&gt;

   &lt;div&gt;
        &lt;label&gt;Skills:&lt;/label&gt;
        &lt;form:select path="skills" items="&#36;{skills}"/&gt;
   &lt;/div&gt;

   &lt;div&gt;
        &lt;label&gt;Notes:&lt;/label&gt;
        &lt;form:textarea path="notes" rows="3" cols="20" /&gt;
        &lt;form:errors path="notes" cssClass="error" /&gt;
   &lt;/div&gt;

   &lt;div&gt;
        &lt;label&gt;Sex:&lt;/label&gt;
            Male: &lt;form:radiobutton path="sex" value="M"/&gt;
            Female: &lt;form:radiobutton path="sex" value="F"/&gt;
   &lt;/div&gt;

   &lt;div&gt;
       &lt;label&gt;House:&lt;/label&gt;

       &lt;form:select path="house"&gt;
           &lt;form:option value="Gryffindor"/&gt;
           &lt;form:option value="Hufflepuff"/&gt;
           &lt;form:option value="Ravenclaw"/&gt;
           &lt;form:option value="Slytherin"/&gt;
       &lt;/form:select&gt;
   &lt;/div&gt;

   &lt;div&gt;
        &lt;label&gt;Subscribe to newsletter?:&lt;/label&gt;
        &lt;form:checkbox path="preferences.receiveNewsletter"/&gt;
   &lt;/div&gt;

   &lt;div&gt;
        &lt;label&gt;Interests:&lt;/label&gt;
       &lt;span&gt;Quidditch:&lt;/span&gt; &lt;form:checkbox path="preferences.interests" value="Quidditch"/&gt;&lt;br/&gt;
       &lt;span&gt;Herbology:&lt;/span&gt; &lt;form:checkbox path="preferences.interests" value="Herbology"/&gt;&lt;br/&gt;
       &lt;span&gt;Defence Against the Dark Arts:&lt;/span&gt; &lt;form:checkbox path="preferences.interests" value="Defence Against the Dark Arts"/&gt;
   &lt;/div&gt;

   &lt;div&gt;
        &lt;label&gt;Favourite Word:&lt;/label&gt;
        &lt;span&gt;Magic:&lt;/span&gt; &lt;form:checkbox path="preferences.favouriteWord" value="Magic"/&gt;
   &lt;/div&gt;

   &lt;div&gt;
        &lt;label&gt;Favourite Colour:&lt;/label&gt;
       &lt;form:select path="favouriteColour"&gt;
           &lt;form:option value="0" label="RED"/&gt;
           &lt;form:option value="1" label="GREEN"/&gt;
           &lt;form:option value="2" label="BLUE"/&gt;
       &lt;/form:select&gt;
   &lt;/div&gt;

   &lt;div&gt;
        &lt;label&gt;Password:&lt;/label&gt;
       &lt;form:password path="password" /&gt;
   &lt;/div&gt;

   &lt;div&gt;
       &lt;input type="submit" value="Save Changes" /&gt;
   &lt;/div&gt;
&lt;/form:form&gt;
       </textarea>

    </div>

  <div class="lefty">
    <div class="menu">
      <c:forEach items="${userList}" var="user">
        <a href="form.htm?id=<c:out value="${user.id}"/>"><c:out value="${user.lastName}"/>, <c:out value="${user.firstName}"/></a>
      </c:forEach>

    </div>
    <p>New JSP tags in Spring 2.0 make building forms with Spring MVC much easier.</p>
    <div class="menu">
        <a href="<c:url value="/about.htm"/>">About</a>
        <a href="<c:url value="/list.htm"/>">Home</a>
    </div>
  </div>

</div>

</body>
</html>
