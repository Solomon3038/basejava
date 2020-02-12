<%@ page import="ru.javawebinar.basejava.model.ContactsType" %>
<%@ page import="ru.javawebinar.basejava.model.SectionType" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.io.*,java.util.Locale" %>
<%@ page import="java.util.UUID" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html" charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <link rel="shortcut icon" href="img/favicon2.ico" type="image/x-icon">
    <link rel="stylesheet" href="css/styles.css">
    <script src="js/main.js" type="text/javascript" charset="UTF-8"></script>
    <jsp:useBean id="resume" type="ru.javawebinar.basejava.model.Resume" scope="request"/>
    <title>Создание Резюме</title>
</head>
<body>
<jsp:include page="fragments/header.jsp"/>
<section>
    <main class="content-wrapper">
        <section class="resume-content">
            <div class="container">
                <div class="resume-form">
                    <form method="post" action="resume" enctype="application/x-www-form-urlencoded">
                        <input type="hidden" name="uuid" value="${1}">
                        <div class="resume-group-contacts group-wide">
                            <span class="span-contacts"><b>Vollständiger Name:</b></span>
                            <input type="text" class="resume-input" name="fullName"
                                   required>
                        </div>
                        <div class="section-margin">
                            <div class="resume-section">
                                <h3 class="resume-heading">Kontaktdaten</h3>
                            </div>
                        </div>

                        <c:forEach var="type" items="<%=ContactsType.values()%>">
                        <div class="resume-group-contacts group-wide">
                            <span class="span-contacts"><b>${type.title}</b></span>
                            <input type="text" class="resume-input" name="${type.name()}">
                        </div>

                        </c:forEach>
                        <c:forEach var="typeSection" items="<%=SectionType.values()%>">
                        <c:if test="${typeSection.name()=='OBJECTIVE'||typeSection.name()=='PERSONAL'}">
                        <div class="section-margin">
                            <div class="resume-section">
                                <h3 class="resume-heading">${typeSection.title}</h3>
                                <div class="resume-group group-center">
                                    <textarea type="text" class="resume-input" name="${typeSection.name()}"></textarea>
                                </div>
                            </div>
                        </div>
                        </c:if>

                        <c:if test="${typeSection.name()=='ACHIEVEMENT'||typeSection.name()=='QUALIFICATIONS'}">

                        <div class="section-margin">
                            <div class="resume-section">
                                <h3 class="resume-heading">${typeSection.title}</h3>
                                <div class="resume-section">

                                    <c:forEach var="section" items="${resume.getSections(typeSection).getItems()}">
                                        <div class="resume-group group-center">
                                            <textarea type="text" class="resume-input"
                                                      name="${typeSection.name()}"></textarea>
                                        </div>
                                    </c:forEach>
                                </div>

                                <div id="${typeSection.name()}" class="resume-heading-extend" style="margin-top:3px;">
                                    <a href="javascript:addSinglePosition('${typeSection.name()}','<div style=margin-top:3px;><textarea type=text size=57 style=height:40px; name=${typeSection.name()}></textarea></div>')">
                                        <img src="img/add.png"> Hinzufügen Position</a>
                                </div>
                            </div>
                        </div>

                        </c:if>
                        <c:if test="${typeSection.name()=='EXPERIENCE'||typeSection.name()=='EDUCATION'}">

                        <div class="section-margin">
                            <c:choose>
                                <c:when test="${typeSection.title=='Опыт работы'}">
                                    <c:set var="nameCompany" value="компании" scope="page"/>
                                    <c:set var="nameCompany1" value="компанию" scope="page"/>
                                </c:when>

                                <c:otherwise>
                                    <c:set var="nameCompany" value="учереждения" scope="page"/>
                                    <c:set var="nameCompany1" value="учереждение" scope="page"/>
                                </c:otherwise>

                            </c:choose>
                            <div class="resume-section">
                                <h3 class="resume-heading">${typeSection.title}</h3>
                                <div id="${typeSection.name()}">
                                    <a id="myLink" href="#" style="padding: 0 0 0 320px;"
                                       onclick="javascript:addOrganization('${typeSection.name()}', 'fieldset', '${typeSection.name()}','${nameCompany}');return false;"><img
                                            src="img/add.png"> Hinzufügen ${nameCompany1}</a>
                                </div>
                            </div>
                        </div>

                        </c:if>
                        <input type="hidden" id="organizationCounter" name="organizationCounter" value="0">
                        <input type="hidden" id="positionCounter" name="positionCounter" value="0">
                        </c:forEach>

                        <br>

                        <button type="submit" class="btn btn-add" name="save" value="1">Speichern</button>

                        <button type="button" onclick="window.history.back()" class="btn btn-cancel"
                                name="CancelEdit" value="1">
                            Abbrechen
                        </button>
                </div>
                </form>
            </div>
            </div>
        </section>
    </main>
    <jsp:include page="fragments/footer.jsp"/>
</body>
</html>
